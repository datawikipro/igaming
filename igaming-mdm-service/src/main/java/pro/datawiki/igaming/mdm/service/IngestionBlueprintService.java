package pro.datawiki.igaming.mdm.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.mdm.domain.MdmExternalSystem;
import pro.datawiki.igaming.mdm.dto.IngestionBlueprintDto;
import pro.datawiki.igaming.mdm.dto.IngestionBlueprintDto.CanonicalSportDto;
import pro.datawiki.igaming.mdm.dto.IngestionBlueprintDto.CanonicalTeamDto;
import pro.datawiki.igaming.mdm.dto.IngestionBlueprintDto.CanonicalTournamentDto;
import pro.datawiki.igaming.mdm.repository.MdmExternalSystemRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionBlueprintService {

    private final MdmExternalSystemRepository externalSystemRepository;
    private final JdbcTemplate jdbcTemplate;
    private final AtomicLong lastInvalidateAllTime = new AtomicLong(0);

    private final Cache<String, IngestionBlueprintDto> blueprintCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(500)
            .build();

    private final Cache<String, List<CanonicalMatchHeaderDto>> activeMatchesCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(3))
            .maximumSize(1)
            .build();

    @Transactional(readOnly = true)
    public IngestionBlueprintDto getBlueprint(String externalSystemCode) {
        return blueprintCache.get(externalSystemCode, this::buildBlueprint);
    }

    public void invalidate(String externalSystemCode) {
        log.info("Invalidating Ingestion Blueprint cache for system: {}", externalSystemCode);
        blueprintCache.invalidate(externalSystemCode);
    }

    public void invalidateAll() {
        long now = System.currentTimeMillis();
        // Throttle invalidation to at most once per 60s to prevent destructive cache thrashing from concurrent crawlers
        if (now - lastInvalidateAllTime.get() < 60_000) {
            return;
        }
        lastInvalidateAllTime.set(now);
        log.info("Invalidating all Ingestion Blueprint caches (throttled 60s)");
        blueprintCache.invalidateAll();
        activeMatchesCache.invalidateAll();
    }

    private IngestionBlueprintDto buildBlueprint(String externalSystemCode) {
        long startTime = System.currentTimeMillis();
        log.info("Building Ingestion Blueprint for external system: {}", externalSystemCode);
        MdmExternalSystem system = externalSystemRepository.findByCode(externalSystemCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown external system code: " + externalSystemCode));

        // 1. Sports
        Map<String, CanonicalSportDto> sportsMap = new HashMap<>(256);
        String sportsSql = """
                SELECT sim.raw_key, s.id, s.slug, s.domain_key, s.is_esports, s.is_team_sport
                FROM mdm_sport_ingestion_map sim
                JOIN mdm_sport s ON sim.canonical_sport_id = s.id
                WHERE sim.external_system_id = ?
                """;
        jdbcTemplate.query(sportsSql, rs -> {
            CanonicalSportDto dto = CanonicalSportDto.builder()
                    .sportId(rs.getInt("id"))
                    .slug(rs.getString("slug"))
                    .domainKey(rs.getString("domain_key"))
                    .isEsports(rs.getBoolean("is_esports"))
                    .isTeamSport(rs.getBoolean("is_team_sport"))
                    .build();
            String rawKey = rs.getString("raw_key");
            sportsMap.put(rawKey, dto);
            if (rawKey != null) {
                sportsMap.putIfAbsent(rawKey.trim().toLowerCase(), dto);
            }
        }, system.getId());

        // 2. Countries
        Map<String, IngestionBlueprintDto.CanonicalCountryDto> countriesMap = new HashMap<>(2048);
        String countriesSql = """
                SELECT cim.raw_key, c.id, c.iso_code, c.canonical_name, c.flag_code
                FROM mdm_country_ingestion_map cim
                JOIN mdm_country c ON cim.canonical_country_id = c.id
                WHERE cim.external_system_id = ?
                """;
        try {
            jdbcTemplate.query(countriesSql, rs -> {
                IngestionBlueprintDto.CanonicalCountryDto dto = IngestionBlueprintDto.CanonicalCountryDto.builder()
                        .countryId(rs.getInt("id"))
                        .isoCode(rs.getString("iso_code"))
                        .canonicalName(rs.getString("canonical_name"))
                        .flagCode(rs.getString("flag_code"))
                        .build();
                String rawKey = rs.getString("raw_key");
                countriesMap.put(rawKey, dto);
                if (rawKey != null) {
                    countriesMap.putIfAbsent(rawKey.trim().toLowerCase(), dto);
                }
            }, system.getId());
        } catch (Exception e) {
            log.warn("Could not query mdm_country_ingestion_map: {}", e.getMessage());
        }

        // 3. Tournaments
        Map<String, CanonicalTournamentDto> tourneysMap = new HashMap<>(32768);
        String tourneysSql = """
                SELECT tim.raw_key, tim.source_id, t.id, t.sport_id, t.category_id, t.country_id, t.country_name, t.slug, t.sofascore_id,
                       t.canonical_name, t.gender, t.age_category
                FROM mdm_tournament_ingestion_map tim
                JOIN mdm_tournament t ON tim.canonical_tournament_id = t.id
                WHERE tim.external_system_id = ?
                """;
        jdbcTemplate.query(tourneysSql, rs -> {
            CanonicalTournamentDto dto = CanonicalTournamentDto.builder()
                    .tournamentId(rs.getLong("id"))
                    .sportId(rs.getInt("sport_id"))
                    .categoryId(rs.getObject("category_id", Integer.class))
                    .countryId(rs.getObject("country_id", Integer.class))
                    .countryName(rs.getString("country_name"))
                    .slug(rs.getString("slug"))
                    .sofascoreId(rs.getObject("sofascore_id", Long.class))
                    .canonicalName(rs.getString("canonical_name"))
                    .gender(rs.getString("gender"))
                    .ageCategory(rs.getString("age_category"))
                    .build();
            String rawKey = rs.getString("raw_key");
            tourneysMap.put(rawKey, dto);
            if (rawKey != null) {
                tourneysMap.putIfAbsent(rawKey.trim().toLowerCase(), dto);
            }
            String sourceId = rs.getString("source_id");
            if (sourceId != null && !sourceId.isBlank()) {
                tourneysMap.put(sourceId.trim(), dto);
            }
        }, system.getId());

        // 4. Teams
        Map<String, CanonicalTeamDto> teamsMap = new HashMap<>(131072);
        String teamsSql = """
                SELECT tm.raw_key, tm.source_id, t.id, t.sport_id, t.category_id, t.country_id, t.country_name, t.slug, t.sofascore_id,
                       t.canonical_name, t.is_national, t.is_individual, t.gender, t.age_category, t.organization_id
                FROM mdm_team_ingestion_map tm
                JOIN mdm_team t ON tm.canonical_team_id = t.id
                WHERE tm.external_system_id = ?
                """;
        jdbcTemplate.query(teamsSql, rs -> {
            CanonicalTeamDto dto = CanonicalTeamDto.builder()
                    .teamId(rs.getLong("id"))
                    .sportId(rs.getInt("sport_id"))
                    .categoryId(rs.getObject("category_id", Integer.class))
                    .countryId(rs.getObject("country_id", Integer.class))
                    .countryName(rs.getString("country_name"))
                    .slug(rs.getString("slug"))
                    .sofascoreId(rs.getObject("sofascore_id", Long.class))
                    .canonicalName(rs.getString("canonical_name"))
                    .isNational(rs.getObject("is_national", Boolean.class))
                    .isIndividual(rs.getObject("is_individual", Boolean.class))
                    .gender(rs.getString("gender"))
                    .ageCategory(rs.getString("age_category"))
                    .organizationId(rs.getObject("organization_id", Long.class))
                    .build();
            String rawKey = rs.getString("raw_key");
            teamsMap.put(rawKey, dto);
            if (rawKey != null) {
                teamsMap.putIfAbsent(rawKey.trim().toLowerCase(), dto);
            }
            String sourceId = rs.getString("source_id");
            if (sourceId != null && !sourceId.isBlank()) {
                teamsMap.put(sourceId.trim(), dto);
            }
        }, system.getId());

        // 5. Active Upcoming Matches (Shared across all blueprints to eliminate 146x duplicate queries)
        List<CanonicalMatchHeaderDto> activeMatchesList = activeMatchesCache.get("ACTIVE_MATCHES", k -> loadActiveMatches());

        log.info("Built Ingestion Blueprint for '{}' in {} ms: {} sports, {} countries, {} tournaments, {} teams, {} active matches",
                externalSystemCode, (System.currentTimeMillis() - startTime), sportsMap.size(), countriesMap.size(), tourneysMap.size(), teamsMap.size(), activeMatchesList != null ? activeMatchesList.size() : 0);

        return IngestionBlueprintDto.builder()
                .externalSystemCode(externalSystemCode)
                .sports(sportsMap)
                .countries(countriesMap)
                .tournaments(tourneysMap)
                .teams(teamsMap)
                .activeMatches(activeMatchesList)
                .build();
    }

    private List<CanonicalMatchHeaderDto> loadActiveMatches() {
        long startTime = System.currentTimeMillis();
        List<CanonicalMatchHeaderDto> activeMatchesList = new ArrayList<>(8192);
        String matchesSql = """
                SELECT m.id, m.sport_id, s.slug AS sport_slug,
                       m.tournament_id, tr.canonical_name AS tournament_name,
                       tr.ml_eligible, tr.coverage_tier,
                       m.home_team_id, ht.canonical_name AS home_team_name,
                       m.away_team_id, at.canonical_name AS away_team_name,
                       EXTRACT(EPOCH FROM m.start_time)::bigint AS start_time_sec,
                       m.sofascore_id, m.source_origin, m.status, m.stage, m.round,
                       m.best_of
                FROM mdm_match m
                JOIN mdm_sport s ON m.sport_id = s.id
                JOIN mdm_tournament tr ON m.tournament_id = tr.id
                JOIN mdm_team ht ON m.home_team_id = ht.id
                JOIN mdm_team at ON m.away_team_id = at.id
                WHERE m.status IN ('SCHEDULED', 'LIVE', 'IN_PLAY')
                  AND m.start_time >= NOW() - INTERVAL '3 hours'
                ORDER BY m.start_time ASC
                """;
        jdbcTemplate.query(matchesSql, rs -> {
            CanonicalMatchHeaderDto header = CanonicalMatchHeaderDto.builder()
                    .matchId(rs.getLong("id"))
                    .sportId(rs.getInt("sport_id"))
                    .sportSlug(rs.getString("sport_slug"))
                    .tournamentId(rs.getLong("tournament_id"))
                    .tournamentName(rs.getString("tournament_name"))
                    .homeTeamId(rs.getLong("home_team_id"))
                    .homeTeamName(rs.getString("home_team_name"))
                    .awayTeamId(rs.getLong("away_team_id"))
                    .awayTeamName(rs.getString("away_team_name"))
                    .startTimeEpochSec(rs.getLong("start_time_sec"))
                    .sofascoreId(rs.getObject("sofascore_id", Long.class))
                    .mlEligible(rs.getBoolean("ml_eligible"))
                    .coverageTier(rs.getString("coverage_tier"))
                    .sourceOrigin(rs.getString("source_origin"))
                    .status(rs.getString("status"))
                    .stage(rs.getString("stage"))
                    .round(rs.getString("round"))
                    .bestOf(rs.getObject("best_of", Integer.class))
                    .build();
            activeMatchesList.add(header);
        });

        log.info("Loaded {} active matches into shared blueprint cache in {} ms",
                activeMatchesList.size(), (System.currentTimeMillis() - startTime));
        return java.util.Collections.unmodifiableList(activeMatchesList);
    }
}
