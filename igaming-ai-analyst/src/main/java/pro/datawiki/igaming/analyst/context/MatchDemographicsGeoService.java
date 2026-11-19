package pro.datawiki.igaming.analyst.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.ml.vector.ClickHouseDdsClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Resolves city demographics, capital status, stadium metadata, travel distance,
 * altitude differential, and timezone jetlag strictly by canonical numeric IDs
 * via ClickHouse dictionaries (dict_team_geo_by_sofa / dict_team_geo -> dict_city / dict_venue).
 * ZERO string-matching in runtime.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchDemographicsGeoService {

    private final ClickHouseDdsClient clickHouseClient;

    public record TeamGeoProfile(
            Long mdmTeamId,
            Long cityId,
            String cityName,
            long population,
            boolean capital,
            double latitude,
            double longitude,
            int elevationM,
            String timezone,
            int venueCapacity,
            boolean indoor
    ) {}

    public record MatchGeoContext(
            TeamGeoProfile home,
            TeamGeoProfile away,
            Double metroBiasIndex,
            Integer travelDistanceKm,
            Integer altitudeDiffM,
            Double timezoneDiffHours,
            boolean bigMarketHomePublicBias,
            String summaryNote
    ) {
        public static MatchGeoContext empty() {
            return new MatchGeoContext(null, null, null, null, null, null, false, null);
        }
    }

    public MatchGeoContext resolveMatchGeoContext(Map<String, Object> dossierMeta) {
        if (dossierMeta == null) return MatchGeoContext.empty();

        Long hSofaId = extractPositiveLong(dossierMeta, "team1_sofascore_id", "home_sofascore_id");
        Long aSofaId = extractPositiveLong(dossierMeta, "team2_sofascore_id", "away_sofascore_id");
        Long hMdmId = extractPositiveLong(dossierMeta, "mdm_home_team_id", "team1_mdm_id", "team1_id");
        Long aMdmId = extractPositiveLong(dossierMeta, "mdm_away_team_id", "team2_mdm_id", "team2_id");

        TeamGeoProfile home = fetchTeamGeo(hSofaId, hMdmId);
        TeamGeoProfile away = fetchTeamGeo(aSofaId, aMdmId);

        if (home == null && away == null) {
            return MatchGeoContext.empty();
        }

        Double metroBias = null;
        boolean bigMarketHomeBias = false;
        Integer travelKm = null;
        Integer altDiff = null;
        Double tzDiff = null;

        if (home != null && away != null) {
            if (home.population() > 0 && away.population() > 0) {
                metroBias = round3(Math.log10((double) home.population() / (double) away.population()));
                // Strong home metropolitan/capital bias: home city >= 4x larger (log10 >= 0.60) OR capital vs non-capital with >= 2x pop
                bigMarketHomeBias = (metroBias >= 0.60 && home.population() >= 500_000L)
                        || (home.capital() && !away.capital() && metroBias >= 0.30);
            }
            if (isValidCoord(home.latitude(), home.longitude()) && isValidCoord(away.latitude(), away.longitude())) {
                travelKm = (int) Math.round(haversineKm(home.latitude(), home.longitude(), away.latitude(), away.longitude()));
            }
            altDiff = home.elevationM() - away.elevationM();
            tzDiff = computeTimezoneDiffHours(home.timezone(), away.timezone());
        }

        StringBuilder note = new StringBuilder();
        if (bigMarketHomeBias && home != null && away != null) {
            note.append(String.format(Locale.US,
                    "🏙️ METRO BIAS: %s (%s%s) принимает %s (%s) [log10=%+.2f] — повышенный риск перегруза публикой хозяев.",
                    home.cityName(), formatPop(home.population()), home.capital() ? ", столица" : "",
                    away.cityName(), formatPop(away.population()), metroBias));
        }
        if (travelKm != null && travelKm >= 1500) {
            if (!note.isEmpty()) note.append(" ");
            note.append(String.format(Locale.US, "✈️ Дальний перелёт гостей: %d км", travelKm));
            if (tzDiff != null && Math.abs(tzDiff) >= 2.0) {
                note.append(String.format(Locale.US, " (джетлаг %.0fч)", Math.abs(tzDiff)));
            }
            note.append(".");
        }
        if (altDiff != null && altDiff >= 1200 && home != null && home.elevationM() >= 1800) {
            if (!note.isEmpty()) note.append(" ");
            note.append(String.format(Locale.US, "⛰️ Высокогорье: %dм (+%dм перепад).", home.elevationM(), altDiff));
        }

        return new MatchGeoContext(
                home, away, metroBias, travelKm, altDiff, tzDiff,
                bigMarketHomeBias,
                note.isEmpty() ? null : note.toString()
        );
    }

    TeamGeoProfile fetchTeamGeo(Long sofaId, Long mdmTeamId) {
        if ((sofaId == null || sofaId <= 0) && (mdmTeamId == null || mdmTeamId <= 0)) {
            return null;
        }
        try {
            String sql;
            if (sofaId != null && sofaId > 0) {
                sql = "SELECT " +
                        "dictGetUInt64('igaming_dds.dict_team_geo_by_sofa', 'mdm_team_id', toUInt64(" + sofaId + ")) AS mdm_team_id, " +
                        "dictGetUInt64('igaming_dds.dict_team_geo_by_sofa', 'home_city_id', toUInt64(" + sofaId + ")) AS city_id, " +
                        "dictGetUInt64('igaming_dds.dict_team_geo_by_sofa', 'home_venue_id', toUInt64(" + sofaId + ")) AS venue_id";
            } else {
                sql = "SELECT " +
                        "toUInt64(" + mdmTeamId + ") AS mdm_team_id, " +
                        "dictGetUInt64('igaming_dds.dict_team_geo', 'home_city_id', toUInt64(" + mdmTeamId + ")) AS city_id, " +
                        "dictGetUInt64('igaming_dds.dict_team_geo', 'home_venue_id', toUInt64(" + mdmTeamId + ")) AS venue_id";
            }

            List<Map<String, Object>> teamRows = clickHouseClient.queryJson(sql);
            if (teamRows == null || teamRows.isEmpty()) return null;
            Map<String, Object> tr = teamRows.get(0);
            long resolvedMdmId = toLong(tr.get("mdm_team_id"));
            long cityId = toLong(tr.get("city_id"));
            long venueId = toLong(tr.get("venue_id"));
            if (cityId <= 0 && venueId <= 0 && sofaId != null && sofaId > 0 && mdmTeamId != null && mdmTeamId > 0) {
                String fallbackSql = "SELECT " +
                        "toUInt64(" + mdmTeamId + ") AS mdm_team_id, " +
                        "dictGetUInt64('igaming_dds.dict_team_geo', 'home_city_id', toUInt64(" + mdmTeamId + ")) AS city_id, " +
                        "dictGetUInt64('igaming_dds.dict_team_geo', 'home_venue_id', toUInt64(" + mdmTeamId + ")) AS venue_id";
                List<Map<String, Object>> fbRows = clickHouseClient.queryJson(fallbackSql);
                if (fbRows != null && !fbRows.isEmpty()) {
                    Map<String, Object> fr = fbRows.get(0);
                    resolvedMdmId = toLong(fr.get("mdm_team_id"));
                    cityId = toLong(fr.get("city_id"));
                    venueId = toLong(fr.get("venue_id"));
                }
            }
            if (cityId <= 0 && venueId <= 0) return null;

            String detailSql = "SELECT " +
                    "dictGetString('igaming_dds.dict_city', 'canonical_name', toUInt64(" + cityId + ")) AS city_name, " +
                    "dictGetUInt64('igaming_dds.dict_city', 'population', toUInt64(" + cityId + ")) AS population, " +
                    "dictGetUInt8('igaming_dds.dict_city', 'is_capital', toUInt64(" + cityId + ")) AS is_capital, " +
                    "dictGetFloat64('igaming_dds.dict_city', 'latitude', toUInt64(" + cityId + ")) AS city_lat, " +
                    "dictGetFloat64('igaming_dds.dict_city', 'longitude', toUInt64(" + cityId + ")) AS city_lon, " +
                    "dictGetInt32('igaming_dds.dict_city', 'elevation_m', toUInt64(" + cityId + ")) AS city_elev, " +
                    "dictGetString('igaming_dds.dict_city', 'timezone', toUInt64(" + cityId + ")) AS timezone, " +
                    "dictGetFloat64('igaming_dds.dict_venue', 'latitude', toUInt64(" + venueId + ")) AS venue_lat, " +
                    "dictGetFloat64('igaming_dds.dict_venue', 'longitude', toUInt64(" + venueId + ")) AS venue_lon, " +
                    "dictGetInt32('igaming_dds.dict_venue', 'altitude_m', toUInt64(" + venueId + ")) AS venue_alt, " +
                    "dictGetUInt32('igaming_dds.dict_venue', 'capacity', toUInt64(" + venueId + ")) AS venue_cap, " +
                    "dictGetUInt8('igaming_dds.dict_venue', 'is_indoor', toUInt64(" + venueId + ")) AS is_indoor";

            List<Map<String, Object>> dRows = clickHouseClient.queryJson(detailSql);
            if (dRows == null || dRows.isEmpty()) return null;
            Map<String, Object> d = dRows.get(0);

            String cityName = String.valueOf(d.getOrDefault("city_name", "")).trim();
            long pop = toLong(d.get("population"));
            boolean cap = toLong(d.get("is_capital")) == 1L;
            double vLat = toDouble(d.get("venue_lat"));
            double vLon = toDouble(d.get("venue_lon"));
            double cLat = toDouble(d.get("city_lat"));
            double cLon = toDouble(d.get("city_lon"));
            double lat = isValidCoord(vLat, vLon) ? vLat : cLat;
            double lon = isValidCoord(vLat, vLon) ? vLon : cLon;
            int vAlt = (int) toLong(d.get("venue_alt"));
            int cElev = (int) toLong(d.get("city_elev"));
            int elev = vAlt != 0 ? vAlt : cElev;
            String tz = String.valueOf(d.getOrDefault("timezone", "")).trim();
            int capacity = (int) toLong(d.get("venue_cap"));
            boolean indoor = toLong(d.get("is_indoor")) == 1L;

            if (cityName.isEmpty() && !isValidCoord(lat, lon)) return null;

            return new TeamGeoProfile(
                    resolvedMdmId > 0 ? resolvedMdmId : mdmTeamId,
                    cityId > 0 ? cityId : null,
                    cityName,
                    pop,
                    cap,
                    lat,
                    lon,
                    elev,
                    tz,
                    capacity,
                    indoor
            );
        } catch (Exception e) {
            log.debug("[GEO-CONTEXT] ClickHouse geo lookup failed (sofaId={}, mdmId={}): {}", sofaId, mdmTeamId, e.getMessage());
            return null;
        }
    }

    static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double p1 = Math.toRadians(lat1);
        double p2 = Math.toRadians(lat2);
        double dp = Math.toRadians(lat2 - lat1);
        double dl = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dp / 2.0) * Math.sin(dp / 2.0)
                + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2.0) * Math.sin(dl / 2.0);
        return 2.0 * r * Math.asin(Math.sqrt(Math.max(0.0, Math.min(1.0, a))));
    }

    static Double computeTimezoneDiffHours(String tzHome, String tzAway) {
        if (tzHome == null || tzHome.isBlank() || tzAway == null || tzAway.isBlank()) return null;
        try {
            int offH = ZonedDateTime.now(ZoneId.of(tzHome)).getOffset().getTotalSeconds();
            int offA = ZonedDateTime.now(ZoneId.of(tzAway)).getOffset().getTotalSeconds();
            return Math.round(((offH - offA) / 3600.0) * 10.0) / 10.0;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isValidCoord(double lat, double lon) {
        return Math.abs(lat) > 0.01 && Math.abs(lon) > 0.01 && Math.abs(lat) <= 90.0 && Math.abs(lon) <= 180.0;
    }

    private static Long extractPositiveLong(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v instanceof Number n && n.longValue() > 0) return n.longValue();
            if (v instanceof String s && !s.isBlank()) {
                try {
                    long parsed = Long.parseLong(s.trim());
                    if (parsed > 0) return parsed;
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private static long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString().trim()); } catch (Exception e) { return 0L; }
    }

    private static double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(o.toString().trim()); } catch (Exception e) { return 0.0; }
    }

    private static double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private static String formatPop(long pop) {
        if (pop >= 1_000_000L) return String.format(Locale.US, "%.1fM", pop / 1_000_000.0);
        if (pop >= 1_000L) return String.format(Locale.US, "%dk", Math.round(pop / 1_000.0));
        return String.valueOf(pop);
    }
}
