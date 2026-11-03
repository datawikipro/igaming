/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.benmanes.caffeine.cache.Cache
 *  com.github.benmanes.caffeine.cache.Caffeine
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.jdbc.core.JdbcTemplate
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 *  pro.datawiki.igaming.dto.CanonicalMatchHeaderDto
 */
package pro.datawiki.igaming.mdm.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.mdm.domain.MdmExternalSystem;
import pro.datawiki.igaming.mdm.dto.IngestionBlueprintDto;
import pro.datawiki.igaming.mdm.repository.MdmExternalSystemRepository;

@Service
public class IngestionBlueprintService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(IngestionBlueprintService.class);
    private final MdmExternalSystemRepository externalSystemRepository;
    private final JdbcTemplate jdbcTemplate;
    private final AtomicLong lastInvalidateAllTime = new AtomicLong(0L);
    private final Cache<String, IngestionBlueprintDto> blueprintCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(15L)).maximumSize(500L).build();
    private final Cache<String, List<CanonicalMatchHeaderDto>> activeMatchesCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(3L)).maximumSize(1L).build();

    @Transactional(readOnly=true)
    public IngestionBlueprintDto getBlueprint(String externalSystemCode) {
        return (IngestionBlueprintDto)this.blueprintCache.get((Object)externalSystemCode, this::buildBlueprint);
    }

    public void invalidate(String externalSystemCode) {
        log.info("Invalidating Ingestion Blueprint cache for system: {}", (Object)externalSystemCode);
        this.blueprintCache.invalidate((Object)externalSystemCode);
    }

    public void invalidateAll() {
        long now = System.currentTimeMillis();
        if (now - this.lastInvalidateAllTime.get() < 60000L) {
            return;
        }
        this.lastInvalidateAllTime.set(now);
        log.info("Invalidating all Ingestion Blueprint caches (throttled 60s)");
        this.blueprintCache.invalidateAll();
        this.activeMatchesCache.invalidateAll();
    }

    private IngestionBlueprintDto buildBlueprint(String externalSystemCode) {
        long startTime = System.currentTimeMillis();
        log.info("Building Ingestion Blueprint for external system: {}", (Object)externalSystemCode);
        MdmExternalSystem system = this.externalSystemRepository.findByCode(externalSystemCode).orElseThrow(() -> new IllegalArgumentException("Unknown external system code: " + externalSystemCode));
        HashMap<String, IngestionBlueprintDto.CanonicalSportDto> sportsMap = new HashMap<String, IngestionBlueprintDto.CanonicalSportDto>(256);
        String sportsSql = "SELECT sim.raw_key, s.id, s.slug, s.domain_key, s.is_esports, s.is_team_sport\nFROM mdm_sport_ingestion_map sim\nJOIN mdm_sport s ON sim.canonical_sport_id = s.id\nWHERE sim.external_system_id = ?\n";
        this.jdbcTemplate.query(sportsSql, rs -> {
            IngestionBlueprintDto.CanonicalSportDto dto = IngestionBlueprintDto.CanonicalSportDto.builder().sportId(rs.getInt("id")).slug(rs.getString("slug")).domainKey(rs.getString("domain_key")).isEsports(rs.getBoolean("is_esports")).isTeamSport(rs.getBoolean("is_team_sport")).build();
            String rawKey = rs.getString("raw_key");
            sportsMap.put(rawKey, dto);
            if (rawKey != null) {
                sportsMap.putIfAbsent(rawKey.trim().toLowerCase(), dto);
            }
        }, new Object[]{system.getId()});
        HashMap<String, IngestionBlueprintDto.CanonicalCountryDto> countriesMap = new HashMap<String, IngestionBlueprintDto.CanonicalCountryDto>(2048);
        String countriesSql = "SELECT cim.raw_key, c.id, c.iso_code, c.canonical_name, c.flag_code\nFROM mdm_country_ingestion_map cim\nJOIN mdm_country c ON cim.canonical_country_id = c.id\nWHERE cim.external_system_id = ?\n";
        try {
            this.jdbcTemplate.query(countriesSql, rs -> {
                IngestionBlueprintDto.CanonicalCountryDto dto = IngestionBlueprintDto.CanonicalCountryDto.builder().countryId(rs.getInt("id")).isoCode(rs.getString("iso_code")).canonicalName(rs.getString("canonical_name")).flagCode(rs.getString("flag_code")).build();
                String rawKey = rs.getString("raw_key");
                countriesMap.put(rawKey, dto);
                if (rawKey != null) {
                    countriesMap.putIfAbsent(rawKey.trim().toLowerCase(), dto);
                }
            }, new Object[]{system.getId()});
        }
        catch (Exception e) {
            log.warn("Could not query mdm_country_ingestion_map: {}", (Object)e.getMessage());
        }
        HashMap<String, IngestionBlueprintDto.CanonicalTournamentDto> tourneysMap = new HashMap<String, IngestionBlueprintDto.CanonicalTournamentDto>(32768);
        String tourneysSql = "SELECT tim.raw_key, tim.source_id, t.id, t.sport_id, t.category_id, t.country_id, t.country_name, t.slug, t.sofascore_id,\n       t.canonical_name, t.gender, t.age_category\nFROM mdm_tournament_ingestion_map tim\nJOIN mdm_tournament t ON tim.canonical_tournament_id = t.id\nWHERE tim.external_system_id = ?\n";
        this.jdbcTemplate.query(tourneysSql, rs -> {
            String sourceId;
            IngestionBlueprintDto.CanonicalTournamentDto dto = IngestionBlueprintDto.CanonicalTournamentDto.builder().tournamentId(rs.getLong("id")).sportId(rs.getInt("sport_id")).categoryId(rs.getObject("category_id", Integer.class)).countryId(rs.getObject("country_id", Integer.class)).countryName(rs.getString("country_name")).slug(rs.getString("slug")).sofascoreId(rs.getObject("sofascore_id", Long.class)).canonicalName(rs.getString("canonical_name")).gender(rs.getString("gender")).ageCategory(rs.getString("age_category")).build();
            String rawKey = rs.getString("raw_key");
            tourneysMap.put(rawKey, dto);
            if (rawKey != null) {
                tourneysMap.putIfAbsent(rawKey.trim().toLowerCase(), dto);
            }
            if ((sourceId = rs.getString("source_id")) != null && !sourceId.isBlank()) {
                tourneysMap.put(sourceId.trim(), dto);
            }
        }, new Object[]{system.getId()});
        HashMap<String, IngestionBlueprintDto.CanonicalTeamDto> teamsMap = new HashMap<String, IngestionBlueprintDto.CanonicalTeamDto>(131072);
        String teamsSql = "SELECT tm.raw_key, tm.source_id, t.id, t.sport_id, t.category_id, t.country_id, t.country_name, t.slug, t.sofascore_id,\n       t.canonical_name, t.is_national, t.is_individual, t.gender, t.age_category, t.organization_id\nFROM mdm_team_ingestion_map tm\nJOIN mdm_team t ON tm.canonical_team_id = t.id\nWHERE tm.external_system_id = ?\n";
        this.jdbcTemplate.query(teamsSql, rs -> {
            String sourceId;
            IngestionBlueprintDto.CanonicalTeamDto dto = IngestionBlueprintDto.CanonicalTeamDto.builder().teamId(rs.getLong("id")).sportId(rs.getInt("sport_id")).categoryId(rs.getObject("category_id", Integer.class)).countryId(rs.getObject("country_id", Integer.class)).countryName(rs.getString("country_name")).slug(rs.getString("slug")).sofascoreId(rs.getObject("sofascore_id", Long.class)).canonicalName(rs.getString("canonical_name")).isNational(rs.getObject("is_national", Boolean.class)).isIndividual(rs.getObject("is_individual", Boolean.class)).gender(rs.getString("gender")).ageCategory(rs.getString("age_category")).organizationId(rs.getObject("organization_id", Long.class)).build();
            String rawKey = rs.getString("raw_key");
            teamsMap.put(rawKey, dto);
            if (rawKey != null) {
                teamsMap.putIfAbsent(rawKey.trim().toLowerCase(), dto);
            }
            if ((sourceId = rs.getString("source_id")) != null && !sourceId.isBlank()) {
                teamsMap.put(sourceId.trim(), dto);
            }
        }, new Object[]{system.getId()});
        List activeMatchesList = (List)this.activeMatchesCache.get((Object)"ACTIVE_MATCHES", k -> this.loadActiveMatches());
        log.info("Built Ingestion Blueprint for '{}' in {} ms: {} sports, {} countries, {} tournaments, {} teams, {} active matches", new Object[]{externalSystemCode, System.currentTimeMillis() - startTime, sportsMap.size(), countriesMap.size(), tourneysMap.size(), teamsMap.size(), activeMatchesList != null ? activeMatchesList.size() : 0});
        return IngestionBlueprintDto.builder().externalSystemCode(externalSystemCode).sports(sportsMap).countries(countriesMap).tournaments(tourneysMap).teams(teamsMap).activeMatches(activeMatchesList).build();
    }

    private List<CanonicalMatchHeaderDto> loadActiveMatches() {
        long startTime = System.currentTimeMillis();
        ArrayList activeMatchesList = new ArrayList(8192);
        String matchesSql = "SELECT m.id, m.sport_id, s.slug AS sport_slug,\n       m.tournament_id, tr.canonical_name AS tournament_name,\n       tr.ml_eligible, tr.coverage_tier,\n       m.home_team_id, ht.canonical_name AS home_team_name,\n       m.away_team_id, at.canonical_name AS away_team_name,\n       EXTRACT(EPOCH FROM m.start_time)::bigint AS start_time_sec,\n       m.sofascore_id, m.source_origin, m.status, m.stage, m.round,\n       m.best_of\nFROM mdm_match m\nJOIN mdm_sport s ON m.sport_id = s.id\nJOIN mdm_tournament tr ON m.tournament_id = tr.id\nJOIN mdm_team ht ON m.home_team_id = ht.id\nJOIN mdm_team at ON m.away_team_id = at.id\nWHERE m.status IN ('SCHEDULED', 'LIVE', 'IN_PLAY')\n  AND m.start_time >= NOW() - INTERVAL '3 hours'\nORDER BY m.start_time ASC\n";
        this.jdbcTemplate.query(matchesSql, rs -> {
            CanonicalMatchHeaderDto header = CanonicalMatchHeaderDto.builder().matchId(Long.valueOf(rs.getLong("id"))).sportId(Integer.valueOf(rs.getInt("sport_id"))).sportSlug(rs.getString("sport_slug")).tournamentId(Long.valueOf(rs.getLong("tournament_id"))).tournamentName(rs.getString("tournament_name")).homeTeamId(Long.valueOf(rs.getLong("home_team_id"))).homeTeamName(rs.getString("home_team_name")).awayTeamId(Long.valueOf(rs.getLong("away_team_id"))).awayTeamName(rs.getString("away_team_name")).startTimeEpochSec(Long.valueOf(rs.getLong("start_time_sec"))).sofascoreId(rs.getObject("sofascore_id", Long.class)).mlEligible(Boolean.valueOf(rs.getBoolean("ml_eligible"))).coverageTier(rs.getString("coverage_tier")).sourceOrigin(rs.getString("source_origin")).status(rs.getString("status")).stage(rs.getString("stage")).round(rs.getString("round")).bestOf(rs.getObject("best_of", Integer.class)).build();
            activeMatchesList.add(header);
        });
        log.info("Loaded {} active matches into shared blueprint cache in {} ms", (Object)activeMatchesList.size(), (Object)(System.currentTimeMillis() - startTime));
        return Collections.unmodifiableList(activeMatchesList);
    }

    @Generated
    public IngestionBlueprintService(MdmExternalSystemRepository externalSystemRepository, JdbcTemplate jdbcTemplate) {
        this.externalSystemRepository = externalSystemRepository;
        this.jdbcTemplate = jdbcTemplate;
    }
}

