package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.util.SlugUtils;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.dossier.DossierOddsConsensusDto;
import pro.datawiki.igaming.dto.dossier.DossierPlayerDto;
import pro.datawiki.igaming.dto.dossier.DossierRecentMatchDto;
import pro.datawiki.igaming.dto.dossier.MatchDossierDto;
import pro.datawiki.igaming.analyst.ml.vector.ClickHouseDdsClient;
import pro.datawiki.igaming.analyst.ml.vector.SportVectorExtractorRegistry;
import java.util.concurrent.ConcurrentHashMap;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.dto.sport.racket.CourtSurface;
import pro.datawiki.igaming.dto.util.SimilarityUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Match Dossier Service fully decoupled from legacy match_record and stored procedures.
 * Fetches canonical Ground Truth match metadata directly from MDM Service (/api/v1/mdm/matches).
 * Formats prompt in pure Java and strictly enforces the Zero-Odds Protocol.
 * Supported by strongly-typed MatchDossierDto and zero procedural sport-branching.
 */
@Slf4j
@Service
public class MatchDossierService {

    private final JdbcTemplate jdbcTemplate;
    private final RedisOddsConsensusService redisOddsConsensusService;
    private final ObjectMapper objectMapper;
    private final ClickHouseDdsClient clickHouse;
    private final DossierPromptFormatter promptFormatter;
    private final MatchDossierHistoryService historyService;
    private final SportVectorExtractorRegistry extractorRegistry;

    @org.springframework.beans.factory.annotation.Autowired
    public MatchDossierService(
            JdbcTemplate jdbcTemplate,
            RedisOddsConsensusService redisOddsConsensusService,
            ObjectMapper objectMapper,
            ClickHouseDdsClient clickHouse,
            DossierPromptFormatter promptFormatter,
            MatchDossierHistoryService historyService,
            SportVectorExtractorRegistry extractorRegistry
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisOddsConsensusService = redisOddsConsensusService;
        this.objectMapper = objectMapper;
        this.clickHouse = clickHouse;
        this.promptFormatter = promptFormatter;
        this.historyService = historyService;
        this.extractorRegistry = extractorRegistry;
    }

    public MatchDossierService(
            JdbcTemplate jdbcTemplate,
            RedisOddsConsensusService redisOddsConsensusService,
            ObjectMapper objectMapper,
            ClickHouseDdsClient clickHouse
    ) {
        this(jdbcTemplate, redisOddsConsensusService, objectMapper, clickHouse,
                new DossierPromptFormatter(), new MatchDossierHistoryService(jdbcTemplate, clickHouse),
                null);
    }

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095}")
    private String mdmUrl;

    @Value("${app.sofascore.ondemand-url:http://igaming-capture-sofascore-on-demand.igaming-enrichment.svc.cluster.local:3041/api/v1/sofascore/on-demand}")
    private String sofaOnDemandUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private volatile List<CanonicalMatchHeaderDto> cachedActiveMatches = null;
    private volatile long lastActiveMatchesFetchTime = 0L;

    /**
     * Builds structured match analyst prompt in pure Java directly from MDM Ground Truth.
     * Strictly enforces the Zero-Odds Protocol (Zero odds in prompt).
     */
    public String getFormattedPrompt(Long matchId) {
        MatchDossierDto dossier = getMatchDossierDto(matchId);
        if (dossier == null) {
            return "MATCH_NOT_FOUND";
        }
        return promptFormatter.formatPrompt(dossier);
    }

    /**
     * Retrieves match dossier directly from MDM Ground Truth service or operational match_record
     * and enriches with Redis odds consensus, returning backwards-compatible Map&lt;String, Object&gt;.
     */
    public Map<String, Object> getMatchDossier(Long matchId) {
        MatchDossierDto dto = getMatchDossierDto(matchId);
        return dto != null ? dto.toMap() : null;
    }

    /**
     * Retrieves strongly-typed match dossier directly from MDM Ground Truth service
     * and enriches with Redis odds consensus and ClickHouse DDS baseline metrics.
     */
    public MatchDossierDto getMatchDossierDto(Long matchId) {
        if (matchId == null) return null;

        CanonicalMatchHeaderDto header = fetchMdmMatchHeader(matchId);
        if (header == null) {
            log.warn("Match #{} not found in MDM Ground Truth Service", matchId);
            return null;
        }

        Sport sport = header.getSportId() != null ? Sports.get(header.getSportId()) : Sports.get(header.getSportSlug());
        String resolvedSport = sport.getSlug();

        // 1. Dynamic Tournament Baseline & Scoring Category from ClickHouse DDS (No MDM hardcoding!)
        Map<String, Object> chBaseline = clickHouse.getTournamentScoringBaseline(resolvedSport, header.getTournamentId(), header.getTournamentName());
        Double expTotal = null;
        Double cushionLow = null;
        Double cushionHigh = null;
        Double handicapCushionDelta = null;
        String scoringCategory = "NORMAL";

        boolean hasSeasonalStats = !chBaseline.isEmpty()
                && chBaseline.get("regulation_median_total") instanceof Number
                && (chBaseline.get("matches_count") instanceof Number mc && mc.intValue() >= 5);

        if (hasSeasonalStats) {
            expTotal = ((Number) chBaseline.get("regulation_median_total")).doubleValue();
            if (chBaseline.get("cushion_low_total") instanceof Number cl) cushionLow = cl.doubleValue();
            if (chBaseline.get("cushion_high_total") instanceof Number ch) cushionHigh = ch.doubleValue();
            if (chBaseline.get("handicap_cushion_delta") instanceof Number hd && hd.doubleValue() > 0.0) {
                handicapCushionDelta = hd.doubleValue();
            }
            if (chBaseline.get("scoring_category") != null) scoringCategory = chBaseline.get("scoring_category").toString();
        } else {
            // 2. Strict Rule: If no statistics for this tournament/season, use domestic country baseline (and average if two countries).
            // Zero string-matching crutches (never check "name contains"!).
            Integer c1 = header.getHomeCountryId() != null ? header.getHomeCountryId() : header.getCountryId();
            Integer c2 = header.getAwayCountryId() != null ? header.getAwayCountryId() : header.getCountryId();
            String g = header.getGender() != null ? header.getGender() : "MALE";
            String a = header.getAgeCategory() != null ? header.getAgeCategory() : "SENIOR";

            Map<String, Object> baseMap1 = c1 != null ? clickHouse.getCountryScoringBaseline(resolvedSport, c1, g, a) : Collections.emptyMap();
            Map<String, Object> baseMap2 = (c2 != null && !Objects.equals(c1, c2))
                    ? clickHouse.getCountryScoringBaseline(resolvedSport, c2, g, a)
                    : baseMap1;

            Double b1 = baseMap1.get("regulation_median_total") instanceof Number n1 ? n1.doubleValue() : null;
            Double b2 = baseMap2.get("regulation_median_total") instanceof Number n2 ? n2.doubleValue() : null;

            if (b1 != null && b2 != null) {
                expTotal = Math.round(((b1 + b2) / 2.0) * 2.0) / 2.0;
                Double cl1 = baseMap1.get("cushion_low_total") instanceof Number cl ? cl.doubleValue() : null;
                Double cl2 = baseMap2.get("cushion_low_total") instanceof Number cl ? cl.doubleValue() : null;
                if (cl1 != null && cl2 != null) {
                    cushionLow = Math.round(((cl1 + cl2) / 2.0) * 2.0) / 2.0;
                }
                Double ch1 = baseMap1.get("cushion_high_total") instanceof Number ch ? ch.doubleValue() : null;
                Double ch2 = baseMap2.get("cushion_high_total") instanceof Number ch ? ch.doubleValue() : null;
                if (ch1 != null && ch2 != null) {
                    cushionHigh = Math.round(((ch1 + ch2) / 2.0) * 2.0) / 2.0;
                }
                if (baseMap1.get("scoring_category") != null) scoringCategory = baseMap1.get("scoring_category").toString();
            } else if (b1 != null) {
                expTotal = b1;
                if (baseMap1.get("cushion_low_total") instanceof Number cl) cushionLow = cl.doubleValue();
                if (baseMap1.get("cushion_high_total") instanceof Number ch) cushionHigh = ch.doubleValue();
                if (baseMap1.get("scoring_category") != null) scoringCategory = baseMap1.get("scoring_category").toString();
            } else if (b2 != null) {
                expTotal = b2;
                if (baseMap2.get("cushion_low_total") instanceof Number cl) cushionLow = cl.doubleValue();
                if (baseMap2.get("cushion_high_total") instanceof Number ch) cushionHigh = ch.doubleValue();
                if (baseMap2.get("scoring_category") != null) scoringCategory = baseMap2.get("scoring_category").toString();
            } else if (header.getExpectedTotalBaseline() != null && header.getExpectedTotalBaseline() > 0.0) {
                expTotal = header.getExpectedTotalBaseline();
            } else {
                // 3. Fallback to sport default
                expTotal = sport.getDefaultTotalBaseline();
            }
        }

        // Mathematical dynamic cushion boundaries when not provided directly by ClickHouse DDS
        if (expTotal != null && expTotal > 0.0) {
            if (cushionLow == null) {
                cushionLow = Math.round(expTotal * 0.75 * 2) / 2.0;
            }
            if (cushionHigh == null) {
                cushionHigh = Math.round(expTotal * 1.25 * 2) / 2.0;
            }
            if (handicapCushionDelta == null) {
                handicapCushionDelta = Math.max(1.5, Math.round(expTotal * 0.060 * 2) / 2.0);
            }
        }

        // Dynamic regulation minutes and pace from MDM header (no hardcoding in Sport classes!)
        Integer regulationMinutes = (header.getRegulationMinutes() != null && header.getRegulationMinutes() > 0)
                ? header.getRegulationMinutes()
                : sport.getDefaultRegulationMinutes();
        Double paceMultiplier = (header.getPaceMultiplier() != null && header.getPaceMultiplier() > 0.0)
                ? header.getPaceMultiplier()
                : 1.0;

        Long t1SofaId = resolveTeamSofaScoreId(header.getHomeTeamId(), header.getHomeTeamName());
        Long t2SofaId = resolveTeamSofaScoreId(header.getAwayTeamId(), header.getAwayTeamName());

        long startMs = header.getStartTimeEpochSec() != null ? header.getStartTimeEpochSec() * 1000L : System.currentTimeMillis();
        String startUtc = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(startMs));

        MatchDossierDto dossier = MatchDossierDto.builder()
                .matchId(header.getMatchId())
                .sport(sport)
                .sportName(sport.getCode())
                .sportSlug(resolvedSport)
                .tournamentId(header.getTournamentId())
                .tournamentName(header.getTournamentName() != null ? header.getTournamentName() : "Tournament")
                .leagueName(header.getTournamentName() != null ? header.getTournamentName() : "Tournament")
                .expectedTotalBaseline(expTotal)
                .tournamentBaselineTotal(expTotal)
                .tournamentCushionLow(cushionLow)
                .tournamentCushionHigh(cushionHigh)
                .tournamentHandicapDelta(handicapCushionDelta)
                .handicapCushionDelta(handicapCushionDelta)
                .tournamentScoringCategory(scoringCategory)
                .regulationMinutes(regulationMinutes)
                .paceMultiplier(paceMultiplier)
                .team1Name(header.getHomeTeamName())
                .team2Name(header.getAwayTeamName())
                .team1Id(header.getHomeTeamId())
                .team2Id(header.getAwayTeamId())
                .team1SofascoreId(t1SofaId)
                .team2SofascoreId(t2SofaId)
                .startTime(startMs)
                .startTimeUtc(startUtc)
                .isLive(false)
                .sofascoreId(header.getSofascoreId())
                .build();

        // Check if raw lineups exist in sofascore_raw_events and resolve canonical SofaScore team names
        String sofaTeam1 = null;
        String sofaTeam2 = null;
        if (header.getSofascoreId() != null) {
            enrichFromSofaRaw(dossier, header.getSofascoreId());
            try {
                List<Map<String, Object>> sofaRows = jdbcTemplate.queryForList(
                        "SELECT home_team, away_team FROM sofascore_raw_events WHERE event_id = ? LIMIT 1",
                        header.getSofascoreId());
                if (!sofaRows.isEmpty()) {
                    sofaTeam1 = (String) sofaRows.get(0).get("home_team");
                    sofaTeam2 = (String) sofaRows.get(0).get("away_team");
                }
            } catch (Exception ignored) {}
        }

        if (dossier.getGroundType() == null && "tennis".equalsIgnoreCase(sport.getSlug())) {
            CourtSurface detected = CourtSurface.resolveSurface(null, header.getTournamentName());
            if (detected != CourtSurface.UNKNOWN) {
                dossier.setGroundType(detected);
            }
        }

        // Enrich recent matches for team1 and team2 using canonical MDM team IDs first, with exact/fuzzy name fallbacks
        String t1Name = sofaTeam1 != null ? sofaTeam1 : header.getHomeTeamName();
        String t2Name = sofaTeam2 != null ? sofaTeam2 : header.getAwayTeamName();
        dossier.setTeam1RecentMatches(fetchRecentMatches(sport, header.getHomeTeamId(), t1Name));
        dossier.setTeam2RecentMatches(fetchRecentMatches(sport, header.getAwayTeamId(), t2Name));

        // Enrich with Redis odds consensus (checks MDM match_id or sofascore_id)
        try {
            RedisOddsConsensusService.OddsConsensusDto consensus = redisOddsConsensusService.getConsensus(matchId);
            if (consensus == null && header.getSofascoreId() != null) {
                consensus = redisOddsConsensusService.getConsensus(header.getSofascoreId());
            }
            if (consensus != null && consensus.bookmakersCount() > 0) {
                DossierOddsConsensusDto odds = DossierOddsConsensusDto.builder()
                        .bookmakersCount((long) consensus.bookmakersCount())
                        .bestWin1(toBdDouble(consensus.bestWin1()))
                        .avgWin1(toBdDouble(consensus.avgWin1()))
                        .bestDraw(toBdDouble(consensus.bestDraw()))
                        .avgDraw(toBdDouble(consensus.avgDraw()))
                        .bestWin2(toBdDouble(consensus.bestWin2()))
                        .avgWin2(toBdDouble(consensus.avgWin2()))
                        .bestTo25(toBdDouble(consensus.bestOver25()))
                        .bestTu25(toBdDouble(consensus.bestUnder25()))
                        .build();
                dossier.setOddsConsensus(odds);
            }
        } catch (Exception e) {
            log.debug("Failed to enrich dossier metadata from Redis for matchId={}: {}", matchId, e.getMessage());
        }

        return dossier;
    }

    public boolean isSupportedMlSport(Sport sport) {
        if (sport == null) return false;
        return extractorRegistry != null && extractorRegistry.hasExtractor(sport);
    }

    public boolean isSupportedMlSport(String sportSlug) {
        if (sportSlug == null || sportSlug.isBlank()) return false;
        return isSupportedMlSport(Sports.get(sportSlug));
    }

    public Set<Sport> getSupportedSports() {
        if (extractorRegistry != null) {
            return extractorRegistry.getSupportedSports();
        }
        return Collections.emptySet();
    }

    /**
     * Finds upcoming matches across verified bookmaker fixtures with live Redis odds within lead-time window [now - 15m, now + 36h].
     */
    public List<Long> findUpcomingMultiSportMatchesForEvaluation(int limit) {
        long nowMillis = System.currentTimeMillis();
        long windowFrom = nowMillis - (15L * 60L * 1000L); // Only matches kicked off within last 15 mins or upcoming (respects Gatekeeper 30m guard)
        long windowTo   = nowMillis + (36L * 60L * 60L * 1000L);

        Set<String> activeOddsMatchIds = redisOddsConsensusService.getActiveMatchIds();
        log.info("Scanner found {} active match IDs in Redis", activeOddsMatchIds != null ? activeOddsMatchIds.size() : 0);

        List<Long> candidates = new ArrayList<>();

        // Priority 1: Canonical MDM matches that have active odds in Redis
        try {
            List<CanonicalMatchHeaderDto> activeMatches = fetchMdmActiveMatches();
            Map<Long, Long> busyTeamTimes = new HashMap<>();

            // Group candidate matches by normalized sport slug to enforce fair, balanced multi-sport representation
            Map<String, List<Long>> bySport = new LinkedHashMap<>();
            for (Sport sp : getSupportedSports()) {
                bySport.put(sp.getSlug(), new ArrayList<>());
            }

            for (CanonicalMatchHeaderDto m : activeMatches) {
                if (m.getMatchId() == null || m.getStartTimeEpochSec() == null) continue;
                long startMs = m.getStartTimeEpochSec() * 1000L;
                if (startMs < windowFrom || startMs > windowTo) continue;

                Sport sport = m.getSportId() != null ? Sports.get(m.getSportId()) : Sports.get(m.getSportSlug());
                if (!isSupportedMlSport(sport)) {
                    continue;
                }
                String sportSlug = sport.getSlug();

                if (sportSlug.contains("cyber") || sportSlug.contains("virtual") || sportSlug.contains("synthetic")) {
                    continue;
                }
                if (sportSlug.contains("esport") && !sport.equals(Sports.CS2) && !sport.equals(Sports.DOTA2)) {
                    continue;
                }
                String tournament = m.getTournamentName() != null ? m.getTournamentName().toLowerCase().trim() : "";
                if (tournament.contains("short") || tournament.contains("3x3") || tournament.contains("4x4")
                        || tournament.contains("5x5") || tournament.contains("2x2") || tournament.contains("battle")
                        || tournament.contains("2x") || tournament.contains("люб")
                        || tournament.matches("^league\\s*\\d+$")) {
                    continue;
                }

                // Physical Reality Invariant: Zero Team Overlap
                // A team cannot play 2 matches simultaneously or within minimum match interval of each other
                Long hId = m.getHomeTeamId();
                Long aId = m.getAwayTeamId();
                long startSec = m.getStartTimeEpochSec();
                long minIntervalSec = (sport != null ? (long) sport.getMinimumMatchIntervalMinutes() * 60L : 7200L);

                if (hId != null && busyTeamTimes.containsKey(hId) && Math.abs(busyTeamTimes.get(hId) - startSec) < minIntervalSec) {
                    log.warn("Skipping match #{} ({} vs {}): home team #{} is already scheduled in another match at {}s (diff: {}s < {}s)",
                            m.getMatchId(), m.getHomeTeamName(), m.getAwayTeamName(), hId, busyTeamTimes.get(hId), Math.abs(busyTeamTimes.get(hId) - startSec), minIntervalSec);
                    continue;
                }
                if (aId != null && busyTeamTimes.containsKey(aId) && Math.abs(busyTeamTimes.get(aId) - startSec) < minIntervalSec) {
                    log.warn("Skipping match #{} ({} vs {}): away team #{} is already scheduled in another match at {}s (diff: {}s < {}s)",
                            m.getMatchId(), m.getHomeTeamName(), m.getAwayTeamName(), aId, busyTeamTimes.get(aId), Math.abs(busyTeamTimes.get(aId) - startSec), minIntervalSec);
                    continue;
                }

                if (!isAlreadyEvaluated(m.getMatchId())) {
                    boolean hasOdds = (activeOddsMatchIds == null || activeOddsMatchIds.isEmpty() || activeOddsMatchIds.contains(m.getMatchId().toString()));
                    if (hasOdds) {
                        List<Long> sportList = bySport.get(sportSlug);
                        if (sportList != null) {
                            sportList.add(m.getMatchId());
                            if (hId != null) busyTeamTimes.put(hId, startSec);
                            if (aId != null) busyTeamTimes.put(aId, startSec);
                        }
                    }
                }
            }

            // Multi-Sport Balanced Ingestion: Round-robin drain up to configured limit
            // Ensures equal representation across all supported disciplines without any sport starving the others
            int quotaPerSport = Math.max(15, limit / Math.max(1, bySport.size()));
            for (Map.Entry<String, List<Long>> entry : bySport.entrySet()) {
                List<Long> sList = entry.getValue();
                int take = Math.min(sList.size(), quotaPerSport);
                candidates.addAll(sList.subList(0, take));
            }
            // Fill any remaining available slots up to configured limit
            if (candidates.size() < limit) {
                for (Map.Entry<String, List<Long>> entry : bySport.entrySet()) {
                    List<Long> sList = entry.getValue();
                    if (sList.size() > quotaPerSport) {
                        for (int i = quotaPerSport; i < sList.size() && candidates.size() < limit; i++) {
                            candidates.add(sList.get(i));
                        }
                    }
                    if (candidates.size() >= limit) break;
                }
            }

            log.info("Found {} candidate matches across sports: {}", candidates.size(),
                    bySport.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().size()).collect(Collectors.joining(", ")));
        } catch (Exception e) {
            log.warn("Failed to query upcoming matches from MDM: {}", e.getMessage());
        }

        log.info("Total candidates selected for AI evaluation: {}", candidates.size());
        return candidates;
    }

    public List<Long> findUpcomingMatchesForEvaluation(int minMinutes, int maxMinutes) {
        long nowMillis = System.currentTimeMillis();
        long startFrom = nowMillis + (minMinutes * 60L * 1000L);
        long startTo   = nowMillis + (maxMinutes * 60L * 1000L);

        List<Long> list = new ArrayList<>();
        // Priority 1: MDM canonical matches
        try {
            List<CanonicalMatchHeaderDto> activeMatches = fetchMdmActiveMatches();
            Map<Long, Long> busyTeamTimes = new HashMap<>();

            for (CanonicalMatchHeaderDto m : activeMatches) {
                if (m.getMatchId() == null || m.getStartTimeEpochSec() == null) continue;
                long startMs = m.getStartTimeEpochSec() * 1000L;
                if (startMs >= startFrom && startMs <= startTo && !isAlreadyEvaluated(m.getMatchId())) {
                    Sport sport = m.getSportId() != null ? Sports.get(m.getSportId()) : Sports.get(m.getSportSlug());
                    if (!isSupportedMlSport(sport)) {
                        continue;
                    }
                    String sportSlug = sport.getSlug();

                    if (sportSlug.contains("cyber") || sportSlug.contains("virtual") || sportSlug.contains("synthetic")) {
                        continue;
                    }
                    if (sportSlug.contains("esport") && !sport.equals(Sports.CS2) && !sport.equals(Sports.DOTA2)) {
                        continue;
                    }
                    String tournament = m.getTournamentName() != null ? m.getTournamentName().toLowerCase().trim() : "";
                    if (tournament.contains("short") || tournament.contains("3x3") || tournament.contains("4x4")
                            || tournament.contains("5x5") || tournament.contains("2x2") || tournament.contains("battle")
                            || tournament.contains("2x") || tournament.contains("люб")
                            || tournament.matches("^league\\s*\\d+$")) {
                        continue;
                    }

                    Long hId = m.getHomeTeamId();
                    Long aId = m.getAwayTeamId();
                    long startSec = m.getStartTimeEpochSec();
                    long minIntervalSec = (long) sport.getMinimumMatchIntervalMinutes() * 60L;

                    if (hId != null && busyTeamTimes.containsKey(hId) && Math.abs(busyTeamTimes.get(hId) - startSec) < minIntervalSec) {
                        continue;
                    }
                    if (aId != null && busyTeamTimes.containsKey(aId) && Math.abs(busyTeamTimes.get(aId) - startSec) < minIntervalSec) {
                        continue;
                    }

                    list.add(m.getMatchId());
                    if (hId != null) busyTeamTimes.put(hId, startSec);
                    if (aId != null) busyTeamTimes.put(aId, startSec);
                    if (list.size() >= 15) break;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to query upcoming matches from MDM: {}", e.getMessage());
        }

        return list;
    }

        private volatile int cachedTotalStrategies = -1;
    private volatile long lastStrategiesCountCheck = 0L;

    private int getRequiredStrategyTemplateCount() {
        long now = System.currentTimeMillis();
        if (cachedTotalStrategies < 0 || (now - lastStrategiesCountCheck) > 60_000L) {
            try {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM ai_strategy_template", Integer.class);
                cachedTotalStrategies = count != null ? count : 0;
                lastStrategiesCountCheck = now;
            } catch (Exception e) {
                cachedTotalStrategies = 7;
            }
        }
        return cachedTotalStrategies;
    }

    private boolean isMissingRequiredStrategyTemplates(Long recId) {
        if (recId == null) return false;
        try {
            int required = getRequiredStrategyTemplateCount();
            if (required <= 0) return false;

            Integer calculated = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT strategy_slug) FROM ai_strategy_vote WHERE recommendation_id = ?",
                    Integer.class, recId);
            return (calculated != null ? calculated : 0) < required;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAlreadyEvaluated(Long matchId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, lineup_status, evaluation_stage, result_status, recommended_market, match_start_time, sport_name, updated_at " +
                    "FROM match_ai_recommendation WHERE match_id = ?", matchId);
            if (rows.isEmpty()) return false;
            Map<String, Object> r = rows.get(0);
            Long recId = r.get("id") instanceof Number num ? num.longValue() : null;
            String resStatus = (String) r.get("result_status");

            // 1. Settled outcomes are permanently immutable
            if ("WON".equals(resStatus) || "LOST".equals(resStatus) || "VOID".equals(resStatus) || "CANCELLED".equals(resStatus)) {
                return true;
            }

            // 2. Verify match has not already started or finished
            Object stObj = r.get("match_start_time");
            long nowMs = System.currentTimeMillis();
            long matchStartMs = 0L;
            if (stObj instanceof java.sql.Timestamp stTs) {
                matchStartMs = stTs.getTime();
            } else if (stObj instanceof java.time.LocalDateTime ldt) {
                matchStartMs = ldt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
            }
            if (matchStartMs > 0 && matchStartMs <= nowMs) {
                return true; // Match already started or finished
            }

            // Calculate elapsed minutes since last evaluation
            long elapsedMinutes = 9999L;
            Object updObj = r.get("updated_at");
            if (updObj instanceof java.sql.Timestamp updTs) {
                elapsedMinutes = (nowMs - updTs.getTime()) / (60L * 1000L);
            } else if (updObj instanceof java.time.LocalDateTime updLdt) {
                long updMs = updLdt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                elapsedMinutes = (nowMs - updMs) / (60L * 1000L);
            }

            String stage = (String) r.get("evaluation_stage");
            String market = (String) r.get("recommended_market");
            String sportName = (String) r.get("sport_name");
            Sport sp = sportName != null ? Sports.get(sportName) : null;
            boolean isSolo = sp != null && !sp.isTeamSport();

            // 3. Dynamic Re-evaluation for PASS / UNMAPPED / NO_VALUE matches
            // If the match starts in the future and currently has NO active bet (PASS, WAIT, UNMAPPED, CONFLICT),
            // we must NOT lock it out permanently! Inputs (odds, MDM mapping, lineups, model fixes) change over time.
            boolean isPassOrNoBet = market == null
                    || "PASS".equalsIgnoreCase(market)
                    || "WAIT".equalsIgnoreCase(market)
                    || "NO_VALUE_CONSENSUS".equalsIgnoreCase(market)
                    || "UNMAPPED_TEAM_PASS".equals(stage)
                    || "UNRESOLVED_PLAYER_PASS".equals(stage)
                    || "SCHEDULE_CONFLICT_PENDING_RESOLUTION".equals(stage)
                    || "CANCELLED_SCHEDULE_CONFLICT".equals(stage)
                    || "CANCELLED_SYNTHETIC_COLLISION".equals(stage)
                    || "FINAL_PASS".equals(stage)
                    || "WAIT_KICKOFF".equals(stage);

            if (isPassOrNoBet) {
                // If official lineups just arrived, re-evaluate immediately!
                Boolean isConfirmed = isLineupNowConfirmed(matchId);
                String currentLineupStatus = (String) r.get("lineup_status");
                if (Boolean.TRUE.equals(isConfirmed) && !"CONFIRMED".equalsIgnoreCase(currentLineupStatus)) {
                    log.info("Future match #{} (currently {}) has newly confirmed lineups! Triggering immediate re-evaluation.", matchId, stage);
                    return false;
                }

                // Rate-limit: wait at least 20 minutes between re-evaluations to avoid busy loops
                if (elapsedMinutes < 20L) {
                    return true;
                }

                log.info("Future match #{} (stage={}, market={}, elapsed={}m) has no active bet. Triggering dynamic re-evaluation with fresh odds/mappings.",
                        matchId, stage, market, elapsedMinutes);
                return false;
            }

            // 4. Invariant completeness: ensure all active strategy template invariants exist
            if (recId != null && isMissingRequiredStrategyTemplates(recId)) {
                log.info("Match #{} (rec #{}) is missing strategy template invariants. Re-evaluation scheduled.", matchId, recId);
                return false;
            }

            // 5. Active bet already finalized at terminal stage
            if ("KICKOFF_VERIFIED".equals(stage) || "LINEUP_CONFIRMED".equals(stage) || "IN_PLAY".equals(stage)) {
                return true;
            }

            // 6. Solo sports (Tennis, Table Tennis) with active recommendation
            if (isSolo && "PENDING".equals(resStatus)) {
                return true;
            }

            // 7. Rate-limit active bets: do not re-evaluate within 20 minutes unless newly confirmed lineups arrive
            if (elapsedMinutes < 20L) {
                Boolean isConfirmed = isLineupNowConfirmed(matchId);
                String currentLineupStatus = (String) r.get("lineup_status");
                if (!Boolean.TRUE.equals(isConfirmed) || "CONFIRMED".equalsIgnoreCase(currentLineupStatus)) {
                    return true;
                }
            }

            // 8. Newly confirmed lineups for active bet
            Boolean isConfirmed = isLineupNowConfirmed(matchId);
            String currentLineupStatus = (String) r.get("lineup_status");
            if (Boolean.TRUE.equals(isConfirmed) && !"CONFIRMED".equalsIgnoreCase(currentLineupStatus)) {
                log.info("Match #{} with active bet has newly confirmed lineups! Triggering Stage 2 re-evaluation.", matchId);
                return false;
            }

            // 9. Final pre-match window for team sport verification
            int finalWindow = sp != null ? sp.getLineupConfirmationWindowMinutes() : 45;
            long diffMinutes = (matchStartMs - nowMs) / (60L * 1000L);

            if (diffMinutes <= finalWindow && diffMinutes >= -15) {
                if ("STAGE_2_PREVIEW".equals(stage)) {
                    return true; // Already reached Stage 2
                }
                log.info("Match #{} is in final pre-match window (starts in {} mins, window {}m). Triggering Stage 2 evaluation.", matchId, diffMinutes, finalWindow);
                return false;
            }

            return true; // Active bet intact, kickoff > finalWindow minutes away
        } catch (Exception e) {
            log.warn("Failed to check evaluation status for match {}: {}", matchId, e.getMessage());
            return false;
        }
    }

    public Boolean isLineupNowConfirmed(Long matchId) {
        try {
            CanonicalMatchHeaderDto header = fetchMdmMatchHeader(matchId);
            if (header != null && header.getSofascoreId() != null) {
                List<String> rows = jdbcTemplate.queryForList(
                        "SELECT lineups_json FROM sofascore_raw_events WHERE event_id = ? " +
                        "AND lineups_json IS NOT NULL AND lineups_json != '' AND lineups_json != '{}' " +
                        "ORDER BY captured_at DESC LIMIT 1",
                        String.class, header.getSofascoreId());
                if (!rows.isEmpty() && rows.get(0) != null) {
                    JsonNode root = objectMapper.readTree(rows.get(0));
                    return root.path("confirmed").asBoolean(false);
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Actively checks if official lineups are confirmed or triggers an on-demand poll
     * to SofaScore capture service for matches in the pre-kickoff window [T-45 ... T-1].
     */
    public boolean checkOrRefreshLineupsOnDemand(Long matchId) {
        if (Boolean.TRUE.equals(isLineupNowConfirmed(matchId))) {
            return true;
        }
        try {
            CanonicalMatchHeaderDto header = fetchMdmMatchHeader(matchId);
            if (header != null && header.getSofascoreId() != null) {
                String onDemandUrl = sofaOnDemandUrl + "/match/" + header.getSofascoreId();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(onDemandUrl))
                        .GET()
                        .timeout(Duration.ofSeconds(3))
                        .build();
                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200 && resp.body() != null) {
                    JsonNode node = objectMapper.readTree(resp.body());
                    boolean confirmed = node.path("lineups_confirmed").asBoolean(false);
                    if (confirmed) {
                        log.info("On-demand refresh discovered confirmed lineups for match #{} (sofaEvent={})", matchId, header.getSofascoreId());
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("On-demand lineup poll skipped or timed out for match #{}: {}", matchId, e.getMessage());
        }
        return false;
    }

    public pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto fetchMdmLiveScore(Long matchId) {
        if (matchId == null) return null;
        try {
            String url = mdmUrl + "/api/v1/mdm/matches/" + matchId + "/live-score";
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().timeout(Duration.ofSeconds(10)).build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                return objectMapper.readValue(resp.body(), pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto.class);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch MDM live score for match #{}: {}", matchId, e.getMessage());
        }
        return null;
    }

    public CanonicalMatchHeaderDto fetchMdmMatchHeader(Long matchId) {
        try {
            String url = mdmUrl + "/api/v1/mdm/matches/" + matchId;
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().timeout(Duration.ofSeconds(15)).build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null) {
                return objectMapper.readValue(resp.body(), CanonicalMatchHeaderDto.class);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch MDM match #{}: {}", matchId, e.getMessage());
        }
        return null;
    }

    private synchronized List<CanonicalMatchHeaderDto> fetchMdmActiveMatches() {
        long now = System.currentTimeMillis();
        if (cachedActiveMatches != null && (now - lastActiveMatchesFetchTime) < 90_000L) {
            return cachedActiveMatches;
        }
        try {
            String url = mdmUrl + "/api/v1/mdm/matches/active?since_hours=2&until_hours=36";
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().timeout(Duration.ofSeconds(45)).build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null) {
                List<CanonicalMatchHeaderDto> list = objectMapper.readValue(resp.body(), new TypeReference<List<CanonicalMatchHeaderDto>>() {});
                cachedActiveMatches = list;
                lastActiveMatchesFetchTime = now;
                return list;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch active matches from MDM: {}", e.getMessage());
        }
        return cachedActiveMatches != null ? cachedActiveMatches : Collections.emptyList();
    }

    private void enrichFromSofaRaw(MatchDossierDto dossier, Long sofaEventId) {
        try {
            String sql = "SELECT event_json, lineups_json FROM sofascore_raw_events WHERE event_id = ? ORDER BY captured_at DESC LIMIT 1";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sofaEventId);
            if (!rows.isEmpty()) {
                // 1. Extract groundType (court surface) for tennis / racket sports
                Object eventJsonObj = rows.get(0).get("event_json");
                if (eventJsonObj != null) {
                    String eventJsonStr = eventJsonObj.toString();
                    if (!eventJsonStr.isBlank()) {
                        try {
                            JsonNode eventRoot = objectMapper.readTree(eventJsonStr);
                            String groundType = eventRoot.path("groundType").asText(null);
                            if (groundType == null || groundType.isBlank()) {
                                groundType = eventRoot.path("event").path("groundType").asText(null);
                            }
                            CourtSurface detected = CourtSurface.resolveSurface(groundType, dossier.getTournamentName());
                            if (detected != CourtSurface.UNKNOWN) {
                                dossier.setGroundType(detected);
                            }
                        } catch (Exception e) {
                            log.debug("Failed parsing groundType from event_json for sofaEventId {}: {}", sofaEventId, e.getMessage());
                        }
                    }
                }

                // 2. Extract lineups
                String lineups = (String) rows.get(0).get("lineups_json");
                if (lineups != null && !lineups.isBlank()) {
                    JsonNode root = objectMapper.readTree(lineups);
                    if (root.has("confirmed")) {
                        dossier.setIsLineupConfirmed(root.path("confirmed").asBoolean(false));
                    }
                    parseLineupSide(root.path("home"), dossier.getTeam1Bench(), dossier.getTeam1Starters(), dossier.getTeam1Missing());
                    parseLineupSide(root.path("away"), dossier.getTeam2Bench(), dossier.getTeam2Starters(), dossier.getTeam2Missing());
                }
            }
        } catch (Exception e) {
            log.debug("No raw lineups payload for sofaEventId {}: {}", sofaEventId, e.getMessage());
        }
    }

    private void parseLineupSide(JsonNode sideNode, List<DossierPlayerDto> bench, List<DossierPlayerDto> starters, List<String> missing) {
        if (sideNode == null || !sideNode.has("players")) return;
        for (JsonNode pNode : sideNode.path("players")) {
            JsonNode player = pNode.path("player");
            String name = player.path("name").asText(player.path("shortName").asText("Player"));
            String pos = pNode.path("position").asText(player.path("position").asText(""));
            double rating = pNode.path("rating").asDouble(player.path("rating").asDouble(0.0));
            boolean isSub = pNode.path("substitute").asBoolean(false);

            long pid = player.path("id").asLong(0L);
            String posUpper = pos.toUpperCase();
            boolean isAttacker = posUpper.contains("F") || posUpper.contains("FW") || posUpper.contains("W") || posUpper.contains("ST") || posUpper.contains("ATTACK");
            boolean isImpactSub = (isAttacker && rating >= 7.0) || rating >= 7.3;

            DossierPlayerDto pDto = DossierPlayerDto.builder()
                    .name(name)
                    .position(pos)
                    .rating(rating)
                    .id(pid > 0 ? pid : null)
                    .sofascoreId(pid > 0 ? pid : null)
                    .substitute(isSub)
                    .impactSub(isImpactSub)
                    .build();

            if (isSub) {
                bench.add(pDto);
            } else {
                starters.add(pDto);
            }
        }

        if (sideNode.has("missingPlayers") && missing != null) {
            for (JsonNode mNode : sideNode.path("missingPlayers")) {
                JsonNode p = mNode.path("player");
                String mName = p.path("name").asText(p.path("shortName").asText("Игрок"));
                String reason = mNode.path("reason").asText(mNode.path("type").asText("Травма / Пропуск"));
                missing.add(mName + " (" + reason + ")");
            }
        }
    }

    private static Double toBdDouble(java.math.BigDecimal bd) {
        return bd != null ? bd.doubleValue() : null;
    }

    private List<DossierRecentMatchDto> fetchRecentMatches(Sport sport, Long teamId, String teamName) {
        return historyService.fetchRecentMatches(sport, teamId, teamName);
    }

    public Long resolveTeamSofaScoreId(Long teamId, String teamName) {
        return historyService.resolveTeamSofaScoreId(teamId, teamName);
    }
}
