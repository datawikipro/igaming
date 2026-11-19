package pro.datawiki.igaming.analyst.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.entity.MatchExternalContextEntity;
import pro.datawiki.igaming.analyst.repository.MatchExternalContextRepository;
import pro.datawiki.igaming.analyst.service.odds.AntiPublicLineDetectorService;
import pro.datawiki.igaming.dto.sport.Sport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Unified Odds Quality & Risk-Reward Advisor ("Советник по качеству кэфа").
 * <p>
 * Combines:
 * 1. Market & Sharp-vs-Soft Anchor (Pinnacle/Bet365 vs Soft bookmaker + opening drift)
 * 2. City Demographics & Geography (mdm_city population / capital bias / travel / altitude)
 * 3. Stadium Match-Hour Weather (Open-Meteo)
 * 4. Anomaly-Triggered News Radar (Google News RSS when |drift| >= 8%)
 * <p>
 * Persists the entire external context snapshot into {@code match_external_context}
 * so all collected intelligence is stored for both current decisions and future research.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OddsQualityAssessmentService {

    private final AntiPublicLineDetectorService antiPublicDetector;
    private final MatchDemographicsGeoService geoService;
    private final MatchWeatherService weatherService;
    private final MarketAnomalyNewsRadarService newsRadarService;
    private final MatchExternalContextRepository contextRepository;
    private final ObjectMapper objectMapper;

    public record OddsQualityVerdict(
            String qualityGrade,
            double riskRewardScore,
            AntiPublicLineDetectorService.AntiPublicSignal lineSignal,
            MatchDemographicsGeoService.MatchGeoContext geoContext,
            MatchWeatherService.WeatherForecast weather,
            MarketAnomalyNewsRadarService.NewsRadarResult newsRadar,
            String humanSummary,
            String riskWarning
    ) {}

    public OddsQualityVerdict assessAndPersist(
            Long matchId,
            String targetMarket,
            double mlFairProbPct,
            double fallbackExecutableOdd,
            String fallbackBookmaker,
            Map<String, Object> dossierMeta,
            Sport sport
    ) {
        String sportSlug = sport != null ? sport.getSlug()
                : (dossierMeta != null && dossierMeta.get("sport_slug") != null ? String.valueOf(dossierMeta.get("sport_slug")) : "football");

        // 1. Sharp Anchor & Opening Line Drift
        AntiPublicLineDetectorService.AntiPublicSignal line = antiPublicDetector.analyzeMarketMovement(matchId, targetMarket, mlFairProbPct);
        double execOdd = line.executableOdd() > 1.01 ? line.executableOdd() : fallbackExecutableOdd;
        double sharpOdd = line.currentConsensusOdd() > 1.01 ? line.currentConsensusOdd() : execOdd;
        double openingOdd = line.openingOdd() > 1.01 ? line.openingOdd() : sharpOdd;

        // 2. City Demographics & Geography (strictly by numeric team IDs)
        MatchDemographicsGeoService.MatchGeoContext geo = geoService.resolveMatchGeoContext(dossierMeta);

        // 3. Stadium Weather (Open-Meteo on venue GPS)
        Long kickoffMs = (dossierMeta != null && dossierMeta.get("start_time") instanceof Number n) ? n.longValue() : null;
        MatchWeatherService.WeatherForecast weather = MatchWeatherService.WeatherForecast.unavailable();
        if (geo.home() != null) {
            weather = weatherService.fetchMatchWeather(
                    geo.home().latitude(), geo.home().longitude(), geo.home().indoor(), kickoffMs, sportSlug
            );
        } else {
            weather = weatherService.fetchMatchWeather(0.0, 0.0, false, kickoffMs, sportSlug);
        }

        // 4. Anomaly-Triggered News Radar (fires ONLY if |driftPct| >= 8%)
        String t1 = dossierMeta != null && dossierMeta.get("team1_name") != null ? String.valueOf(dossierMeta.get("team1_name")) : "";
        String t2 = dossierMeta != null && dossierMeta.get("team2_name") != null ? String.valueOf(dossierMeta.get("team2_name")) : "";
        MarketAnomalyNewsRadarService.NewsRadarResult news = newsRadarService.scanAnomalyNews(matchId, t1, t2, line.driftPct());

        // 5. Evaluate Odds Quality Grade & Risk-Reward Score
        double impliedProbPct = 100.0 / Math.max(1.01, execOdd);
        double sharpImpliedPct = 100.0 / Math.max(1.01, sharpOdd);
        double mlEvPct = ((mlFairProbPct / 100.0) * execOdd - 1.0) * 100.0;
        boolean bettingAwayOrUnderdog = isAwayMarket(targetMarket);
        boolean metroSupportsBet = geo.bigMarketHomePublicBias() && bettingAwayOrUnderdog;

        boolean hasInsightDanger = news.triggered()
                && "INJURY_OR_ROTATION_INSIGHT".equals(news.signalType())
                && line.driftPct() >= 8.0;

        String grade;
        double score = 50.0;

        if (hasInsightDanger) {
            grade = "INSIGHT_WARNING";
            score = 15.0;
        } else if (("ANTI_PUBLIC_FADE".equals(line.steamDirection()) || "SHARP_VS_SOFT_VALUE".equals(line.steamDirection()))
                && mlEvPct >= 3.0) {
            grade = "ELITE_VALUE";
            score = 80.0 + Math.min(15.0, Math.max(0.0, line.sharpVsSoftDivergencePct() * 1.5))
                    + (metroSupportsBet ? 5.0 : 0.0);
        } else if (mlEvPct >= 3.0 && line.sharpVsSoftDivergencePct() >= -1.5) {
            grade = "GOOD_VALUE";
            score = 65.0 + Math.min(15.0, mlEvPct * 0.8) + (metroSupportsBet ? 5.0 : 0.0);
        } else if (line.sharpAnchored() && line.sharpVsSoftDivergencePct() <= -3.5) {
            grade = "PUBLIC_TRAP";
            score = 25.0;
        } else {
            grade = "FAIR_MARKET";
            score = 48.0 + Math.max(-15.0, Math.min(12.0, mlEvPct * 0.5));
        }

        // Weather risk penalty on totals / high-wind outdoor matches
        boolean weatherCaution = weather != null && List.of("HEAVY_RAIN", "HIGH_WIND", "EXTREME_HEAT", "FREEZING").contains(weather.riskLevel());
        if (weatherCaution) {
            score = Math.max(10.0, score - 6.0);
        }
        score = Math.round(Math.max(0.0, Math.min(100.0, score)) * 10.0) / 10.0;

        // Build clean human summary (as described by user: bookmaker odd vs Pinnacle anchor + grade + context)
        List<String> summaryParts = new ArrayList<>();
        String bmLabel = (fallbackBookmaker != null && !fallbackBookmaker.isBlank()) ? fallbackBookmaker : "БК";
        if (line.sharpAnchored() && sharpOdd > 1.01) {
            summaryParts.add(String.format(Locale.US,
                    "📊 Оценка кэфа [%s | %.0f/100]: %s даёт %.2f против эталона Pinnacle/Sharp %.2f (рынок %.1f%%, перекос %+.1f%%).",
                    grade, score, bmLabel, execOdd, sharpOdd, sharpImpliedPct, line.sharpVsSoftDivergencePct()));
        } else if (execOdd > 1.01) {
            summaryParts.add(String.format(Locale.US,
                    "📊 Оценка кэфа [%s | %.0f/100]: %s %.2f (консенсус %.2f, ML %.1f%% vs рынок %.1f%%).",
                    grade, score, bmLabel, execOdd, sharpOdd, mlFairProbPct, impliedProbPct));
        }
        if (line.rationaleNote() != null && !line.rationaleNote().isBlank()) {
            summaryParts.add(line.rationaleNote());
        }
        if (geo.summaryNote() != null && !geo.summaryNote().isBlank()) {
            summaryParts.add(geo.summaryNote());
        }
        if (weather != null && weather.summary() != null && weatherCaution) {
            summaryParts.add(weather.summary());
        }
        if (news.triggered() && news.headlineSummary() != null) {
            summaryParts.add(news.headlineSummary());
        }

        // 6. Schedule Congestion / Calendar Fatigue (zero-latency from recent matches timestamps in dossierMeta)
        ScheduleCongestion congestion = computeScheduleCongestion(dossierMeta, kickoffMs);
        if (congestion.fatigueDiff7d() != null && Math.abs(congestion.fatigueDiff7d()) >= 2) {
            if (congestion.fatigueDiff7d() >= 2) {
                summaryParts.add(String.format(Locale.US,
                        "🔋 Усталость хозяев: %d матчей за 7д против %d у гостей.",
                        congestion.home7d(), congestion.away7d()));
            } else {
                summaryParts.add(String.format(Locale.US,
                        "🔋 Усталость гостей: %d матчей за 7д против %d у хозяев.",
                        congestion.away7d(), congestion.home7d()));
            }
        }

        // 7. Referee discipline metadata (if present in dossierMeta)
        RefereeSnapshot referee = extractRefereeSnapshot(dossierMeta);

        String humanSummary = summaryParts.isEmpty() ? null : String.join(" ", summaryParts);
        String riskWarning = hasInsightDanger ? news.headlineSummary()
                : (weatherCaution ? weather.summary() : null);

        // Expose verdict grade & score into dossierMeta for downstream StrategyEngineService Risk/Reward evaluation
        if (dossierMeta != null) {
            try {
                dossierMeta.put("odds_quality_grade", grade);
                dossierMeta.put("odds_quality_score", score);
                dossierMeta.put("line_drift_pct", line.driftPct());
                dossierMeta.put("sharp_vs_soft_pct", line.sharpVsSoftDivergencePct());
            } catch (UnsupportedOperationException ignored) {
                // Unmodifiable map in unit tests
            }
        }

        // Persist snapshot into match_external_context warehouse
        persistContextSnapshot(matchId, sportSlug, targetMarket, geo, weather, news, congestion, referee,
                grade, score, sharpOdd, execOdd, bmLabel, openingOdd, line.driftPct(), line.sharpVsSoftDivergencePct(), humanSummary);

        return new OddsQualityVerdict(grade, score, line, geo, weather, news, humanSummary, riskWarning);
    }

    record ScheduleCongestion(Integer home7d, Integer away7d, Integer home14d, Integer away14d, Integer fatigueDiff7d) {}
    record RefereeSnapshot(String name, Double avgYellow, Double avgPenalties) {}

    private ScheduleCongestion computeScheduleCongestion(Map<String, Object> dossierMeta, Long kickoffMs) {
        if (dossierMeta == null) return new ScheduleCongestion(null, null, null, null, null);
        long refMs = (kickoffMs != null && kickoffMs > 0) ? kickoffMs : System.currentTimeMillis();
        int[] h = countRecentMatchesWindow(dossierMeta.get("team1_recent_matches"), refMs);
        int[] a = countRecentMatchesWindow(dossierMeta.get("team2_recent_matches"), refMs);
        if (h == null && a == null) return new ScheduleCongestion(null, null, null, null, null);
        Integer h7 = h != null ? h[0] : null;
        Integer a7 = a != null ? a[0] : null;
        Integer h14 = h != null ? h[1] : null;
        Integer a14 = a != null ? a[1] : null;
        Integer diff = (h7 != null && a7 != null) ? (h7 - a7) : null;
        return new ScheduleCongestion(h7, a7, h14, a14, diff);
    }

    private int[] countRecentMatchesWindow(Object rawList, long refMs) {
        if (!(rawList instanceof List<?> list) || list.isEmpty()) return null;
        long w7 = 7L * 86_400_000L;
        long w14 = 14L * 86_400_000L;
        int c7 = 0;
        int c14 = 0;
        boolean parsedAny = false;
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> m)) continue;
            Object tsObj = m.get("match_date");
            if (tsObj == null) tsObj = m.get("start_time");
            if (tsObj == null) tsObj = m.get("timestamp");
            long ts = 0L;
            if (tsObj instanceof Number n) {
                ts = n.longValue();
                if (ts > 0 && ts < 10_000_000_000L) ts *= 1000L;
            } else if (tsObj instanceof String s && !s.isBlank()) {
                try {
                    if (s.matches("\\d+")) {
                        ts = Long.parseLong(s);
                        if (ts > 0 && ts < 10_000_000_000L) ts *= 1000L;
                    } else if (s.length() >= 10) {
                        ts = java.time.LocalDate.parse(s.substring(0, 10)).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
                    }
                } catch (Exception ignored) {}
            }
            if (ts <= 0 || ts >= refMs) continue;
            parsedAny = true;
            long age = refMs - ts;
            if (age <= w7) c7++;
            if (age <= w14) c14++;
        }
        return parsedAny ? new int[]{c7, c14} : null;
    }

    private RefereeSnapshot extractRefereeSnapshot(Map<String, Object> dossierMeta) {
        if (dossierMeta == null) return new RefereeSnapshot(null, null, null);
        Object refObj = dossierMeta.get("referee_name");
        if (refObj == null) refObj = dossierMeta.get("referee");
        String name = refObj != null ? String.valueOf(refObj).trim() : null;
        if (name != null && (name.isBlank() || "null".equalsIgnoreCase(name))) name = null;
        Double avgY = dossierMeta.get("referee_avg_yellow_cards") instanceof Number n ? n.doubleValue() : null;
        Double avgP = dossierMeta.get("referee_avg_penalties") instanceof Number n ? n.doubleValue() : null;
        return new RefereeSnapshot(name, avgY, avgP);
    }

    private void persistContextSnapshot(
            Long matchId, String sportSlug, String targetMarket,
            MatchDemographicsGeoService.MatchGeoContext geo,
            MatchWeatherService.WeatherForecast weather,
            MarketAnomalyNewsRadarService.NewsRadarResult news,
            ScheduleCongestion congestion,
            RefereeSnapshot referee,
            String grade, double score,
            double sharpOdd, double execOdd, String execBm,
            double openingOdd, double driftPct, double sharpDivPct,
            String summary
    ) {
        if (matchId == null || contextRepository == null) return;
        try {
            OffsetDateTime now = OffsetDateTime.now();
            MatchExternalContextEntity existing = contextRepository.findById(matchId).orElse(null);
            MatchExternalContextEntity entity = existing != null ? existing : MatchExternalContextEntity.builder()
                    .matchId(matchId)
                    .createdAt(now)
                    .build();

            entity.setSportSlug(sportSlug);
            if (geo != null) {
                if (geo.home() != null) {
                    entity.setMdmHomeTeamId(geo.home().mdmTeamId());
                    entity.setHomeCityId(geo.home().cityId());
                    entity.setHomeCityName(geo.home().cityName());
                    entity.setHomeCityPopulation(geo.home().population() > 0 ? geo.home().population() : null);
                    entity.setHomeIsCapital(geo.home().capital());
                    entity.setVenueCapacity(geo.home().venueCapacity() > 0 ? geo.home().venueCapacity() : null);
                    entity.setIsIndoor(geo.home().indoor());
                }
                if (geo.away() != null) {
                    entity.setMdmAwayTeamId(geo.away().mdmTeamId());
                    entity.setAwayCityId(geo.away().cityId());
                    entity.setAwayCityName(geo.away().cityName());
                    entity.setAwayCityPopulation(geo.away().population() > 0 ? geo.away().population() : null);
                    entity.setAwayIsCapital(geo.away().capital());
                }
                entity.setMetroBiasIndex(geo.metroBiasIndex() != null ? bd(geo.metroBiasIndex(), 3) : null);
                entity.setTravelDistanceKm(geo.travelDistanceKm());
                entity.setAltitudeDiffM(geo.altitudeDiffM());
                entity.setTimezoneDiffHours(geo.timezoneDiffHours() != null ? bd(geo.timezoneDiffHours(), 1) : null);
            }

            if (weather != null && !"UNKNOWN".equals(weather.riskLevel())) {
                entity.setWeatherTempC(weather.tempC() != null ? bd(weather.tempC(), 1) : null);
                entity.setWeatherWindKmh(weather.windKmh() != null ? bd(weather.windKmh(), 1) : null);
                entity.setWeatherPrecipMm(weather.precipMm() != null ? bd(weather.precipMm(), 2) : null);
                entity.setWeatherCode(weather.weatherCode());
                entity.setWeatherRiskLevel(weather.riskLevel());
                entity.setWeatherSummary(weather.summary());
            }

            if (news != null && news.triggered()) {
                entity.setNewsRadarTriggered(true);
                entity.setNewsSignalType(news.signalType());
                entity.setNewsHeadlineSummary(news.headlineSummary());
                entity.setNewsCheckedAt(now);
                try {
                    entity.setNewsRawJson(objectMapper.writeValueAsString(news.headlines()));
                } catch (Exception ignored) {}
            } else if (entity.getNewsRadarTriggered() == null) {
                entity.setNewsRadarTriggered(false);
            }

            entity.setOddsQualityGrade(grade);
            entity.setOddsQualityScore(bd(score, 2));
            entity.setTargetMarket(targetMarket);
            entity.setSharpAnchorOdd(sharpOdd > 1.01 ? bd(sharpOdd, 3) : null);
            entity.setExecutableOdd(execOdd > 1.01 ? bd(execOdd, 3) : null);
            entity.setExecutableBookmaker(execBm);
            entity.setOpeningOdd(openingOdd > 1.01 ? bd(openingOdd, 3) : null);
            entity.setLineDriftPct(bd(driftPct, 2));
            entity.setSharpVsSoftPct(bd(sharpDivPct, 2));
            entity.setOddsQualitySummary(summary);
            if (congestion != null) {
                if (congestion.home7d() != null) entity.setHomeMatches7d(congestion.home7d());
                if (congestion.away7d() != null) entity.setAwayMatches7d(congestion.away7d());
                if (congestion.home14d() != null) entity.setHomeMatches14d(congestion.home14d());
                if (congestion.away14d() != null) entity.setAwayMatches14d(congestion.away14d());
                if (congestion.fatigueDiff7d() != null) entity.setFatigueDiff7d(congestion.fatigueDiff7d());
            }
            if (referee != null && referee.name() != null) {
                entity.setRefereeName(referee.name());
                if (referee.avgYellow() != null) entity.setRefereeAvgYellowCards(bd(referee.avgYellow(), 2));
                if (referee.avgPenalties() != null) entity.setRefereeAvgPenalties(bd(referee.avgPenalties(), 2));
            }
            entity.setUpdatedAt(now);

            contextRepository.save(entity);
        } catch (Exception e) {
            log.debug("[ODDS-QUALITY] Failed persisting match_external_context for match #{}: {}", matchId, e.getMessage());
        }
    }

    private static boolean isAwayMarket(String market) {
        if (market == null) return false;
        String m = market.toUpperCase(Locale.ROOT);
        return m.contains("WIN2") || m.contains("X2") || m.contains("AWAY") || m.contains("HANDICAP_2");
    }

    private static BigDecimal bd(double v, int scale) {
        return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
    }
}
