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
import java.util.concurrent.ConcurrentHashMap;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;
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
@RequiredArgsConstructor
public class MatchDossierService {

    public static final Set<Sport> SUPPORTED_ML_SPORTS = Set.of(
            Sports.SOCCER,
            Sports.TENNIS,
            Sports.TABLE_TENNIS,
            Sports.BASKETBALL,
            Sports.ICE_HOCKEY,
            Sports.CS2,
            Sports.DOTA2
    );

    private final JdbcTemplate jdbcTemplate;
    private final RedisOddsConsensusService redisOddsConsensusService;
    private final ObjectMapper objectMapper;
    private final ClickHouseDdsClient clickHouse;
    private final Map<Long, Optional<Long>> sofaIdCache = new ConcurrentHashMap<>();

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

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("## ⚽ МАТЧ: %s vs %s\n", dossier.getTeam1Name(), dossier.getTeam2Name()));
        sb.append(String.format("- **Вид спорта**: %s\n", dossier.getSportName()));
        sb.append(String.format("- **Турнир / Лига**: %s\n", dossier.getLeagueName()));
        sb.append(String.format("- **Время начала (UTC)**: %s\n", dossier.getStartTimeUtc()));
        sb.append(String.format("- **Match ID**: %s\n", dossier.getMatchId()));
        if (dossier.getSofascoreId() != null) {
            sb.append(String.format("- **Ground Truth SofaScore ID**: %s\n", dossier.getSofascoreId()));
        }

        // Lineup context if available
        if (Boolean.TRUE.equals(dossier.getIsLineupConfirmed())) {
            sb.append("\n### 👥 ПОДТВЕРЖДЁННЫЕ СОСТАВЫ:\n");
            sb.append("- Стартовые составы и скамейка официально подтверждены.\n");
        }

        // Formatted recent match history
        appendRecentMatchesSection(sb, dossier.getTeam1Name(), dossier.getTeam1RecentMatches());
        appendRecentMatchesSection(sb, dossier.getTeam2Name(), dossier.getTeam2RecentMatches());

        // Formatted Lineups, Bench and Injuries
        appendLineupsSection(sb, dossier.getTeam1Name(), dossier.getTeam1Starters(), dossier.getTeam1Bench(), dossier.getTeam1Missing());
        appendLineupsSection(sb, dossier.getTeam2Name(), dossier.getTeam2Starters(), dossier.getTeam2Bench(), dossier.getTeam2Missing());

        // Storyline narrative if present
        if (dossier.getStorylineJson() != null) {
            sb.append(String.format("\n### 📖 КОНТЕКСТ МАТЧА:\n%s\n", dossier.getStorylineJson()));
        }

        return sb.toString();
    }

    private void appendRecentMatchesSection(StringBuilder sb, String teamName, List<DossierRecentMatchDto> list) {
        if (list == null || list.isEmpty()) return;
        sb.append(String.format("\n### 📊 ПОСЛЕДНИЕ МАТЧИ: %s (Всего: %d):\n", teamName, list.size()));
        int over25Count = 0;
        int totalGoals = 0;
        int cleanSheets = 0;

        for (DossierRecentMatchDto m : list) {
            String res = m.getResult() != null ? m.getResult() : "WIN";
            int tScore = m.getTeamScore();
            int oScore = m.getOpponentScore();
            boolean cs = m.isCleanSheet();
            int sumGoals = m.getTotalGoals();
            if (m.isOver25()) over25Count++;
            if (cs) cleanSheets++;
            totalGoals += sumGoals;
            sb.append(String.format("  * %s %d:%d (Тотал: %d, Сухой матч: %s)\n", res, tScore, oScore, sumGoals, cs ? "Да" : "Нет"));
        }
        double avgGoals = (double) totalGoals / list.size();
        sb.append(String.format("  -> Итог по тоталам: Средний тотал = %.2f гола/матч, Матчей с ТБ 2.5 = %d/%d (%.0f%%), Сухих матчей = %d/%d\n",
                avgGoals, over25Count, list.size(), (over25Count * 100.0) / list.size(), cleanSheets, list.size()));
    }

    private void appendLineupsSection(StringBuilder sb, String teamName, List<DossierPlayerDto> starters, List<DossierPlayerDto> bench, List<String> missing) {
        if (starters != null && !starters.isEmpty()) {
            sb.append(String.format("\n### 🟢 СТАРТОВЫЙ СОСТАВ: %s (%d игроков):\n", teamName, starters.size()));
            for (DossierPlayerDto p : starters) {
                sb.append(String.format("  * %s (%s, рейтинг: %s)\n",
                        p.getName(), p.getPosition(), p.getRating() != null ? String.format("%.1f", p.getRating()) : "0.0"));
            }
        }
        if (bench != null && !bench.isEmpty()) {
            sb.append(String.format("\n### 🟡 СКАМЕЙКА ЗАПАСНЫХ: %s (%d игроков):\n", teamName, bench.size()));
            for (DossierPlayerDto p : bench) {
                String pos = p.getPosition() != null ? p.getPosition().toUpperCase() : "";
                double rating = p.getRating() != null ? p.getRating() : 0.0;
                sb.append(String.format("  * %s (%s, рейтинг: %.1f)%s\n",
                        p.getName(), pos, rating, p.isImpactSub() ? " ⚡ [Опция усиления атаки / ротации]" : ""));
            }
        }
        if (missing != null && !missing.isEmpty()) {
            sb.append(String.format("\n### 🚨 ТРАВМЫ И ПОТЕРИ: %s (%d игроков):\n", teamName, missing.size()));
            for (String m : missing) {
                sb.append(String.format("  * %s\n", m));
            }
        }
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

        Sport sport = Sports.resolveSport(header.getSportSlug(), header.getTournamentName());
        String resolvedSport = sport.getSlug();

        // Dynamic Tournament Baseline & Scoring Category from ClickHouse DDS (No MDM hardcoding!)
        Map<String, Object> chBaseline = clickHouse.getTournamentScoringBaseline(resolvedSport, header.getTournamentId(), header.getTournamentName());
        Double expTotal = null;
        Double cushionLow = null;
        Double cushionHigh = null;
        Double handicapCushionDelta = null;
        String scoringCategory = "NORMAL";

        if (!chBaseline.isEmpty() && chBaseline.get("regulation_median_total") instanceof Number n) {
            expTotal = n.doubleValue();
            if (chBaseline.get("cushion_low_total") instanceof Number cl) cushionLow = cl.doubleValue();
            if (chBaseline.get("cushion_high_total") instanceof Number ch) cushionHigh = ch.doubleValue();
            if (chBaseline.get("handicap_cushion_delta") instanceof Number hd && hd.doubleValue() > 0.0) {
                handicapCushionDelta = hd.doubleValue();
            }
            if (chBaseline.get("scoring_category") != null) scoringCategory = chBaseline.get("scoring_category").toString();
        } else if (header.getExpectedTotalBaseline() != null && header.getExpectedTotalBaseline() > 0.0) {
            expTotal = header.getExpectedTotalBaseline();
        } else {
            expTotal = sport.getDefaultTotalBaseline();
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

    public static boolean isSupportedMlSport(Sport sport) {
        return sport != null && SUPPORTED_ML_SPORTS.contains(sport);
    }

    public static boolean isSupportedMlSport(String sportSlug) {
        if (sportSlug == null || sportSlug.isBlank()) return false;
        return isSupportedMlSport(Sports.get(sportSlug));
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
            for (Sport sp : SUPPORTED_ML_SPORTS) {
                bySport.put(sp.getSlug(), new ArrayList<>());
            }

            for (CanonicalMatchHeaderDto m : activeMatches) {
                if (m.getMatchId() == null || m.getStartTimeEpochSec() == null) continue;
                long startMs = m.getStartTimeEpochSec() * 1000L;
                if (startMs < windowFrom || startMs > windowTo) continue;

                Sport sport = Sports.resolveSport(m.getSportSlug(), m.getTournamentName());
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
                    Sport sport = Sports.resolveSport(m.getSportSlug(), m.getTournamentName());
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

    private boolean isAlreadyEvaluated(Long matchId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT lineup_status, evaluation_stage, result_status, match_start_time, sport_name, updated_at FROM match_ai_recommendation WHERE match_id = ?", matchId);
            if (rows.isEmpty()) return false;
            Map<String, Object> r = rows.get(0);
            String resStatus = (String) r.get("result_status");
            // 1. Settled or cancelled outcomes are permanently immutable
            if ("WON".equals(resStatus) || "LOST".equals(resStatus) || "VOID".equals(resStatus) || "CANCELLED".equals(resStatus)) {
                return true;
            }
            String stage = (String) r.get("evaluation_stage");
            String sportName = (String) r.get("sport_name");
            Sport sp = sportName != null ? Sports.get(sportName) : null;

            // 2. Final stage already completed
            if ("KICKOFF_VERIFIED".equals(stage) || "LINEUP_CONFIRMED".equals(stage) || "IN_PLAY".equals(stage) || "FINAL_PASS".equals(stage)) {
                return true;
            }

            // 3. Solo/racket sports (Tennis, Table Tennis) have fixed participants.
            // If already recommended as PENDING, keep active recommendation.
            // If SKIPPED, allow re-evaluation after 20 minutes (rate-limit item 4) to pick up fixed matching/odds.
            boolean isSolo = sp != null && !sp.isTeamSport();
            if (isSolo && "PENDING".equals(resStatus)) {
                return true;
            }

            // 3.1 Verify match has not already started
            Object stObj = r.get("match_start_time");
            long nowMs = System.currentTimeMillis();
            if (stObj instanceof java.sql.Timestamp stTs) {
                if (stTs.getTime() <= nowMs) {
                    return true; // Match already started or finished
                }
            } else if (stObj instanceof java.time.LocalDateTime ldt) {
                long startMs = ldt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                if (startMs <= nowMs) {
                    return true;
                }
            }

            // 4. Rate-limit re-evaluation: if evaluated less than 20 minutes ago, avoid busy looping unless confirmed lineups arrive
            Object updObj = r.get("updated_at");
            if (updObj instanceof java.sql.Timestamp updTs) {
                long elapsedMinutes = (nowMs - updTs.getTime()) / (60L * 1000L);
                if (elapsedMinutes < 20L && !"WAIT_KICKOFF".equals(stage)) {
                    Boolean isConfirmed = isLineupNowConfirmed(matchId);
                    if (!Boolean.TRUE.equals(isConfirmed)) {
                        return true; // Already recently evaluated, skip
                    }
                }
            }

            // 5. Pre-Lineup stage: check if official lineups have arrived now!
            Boolean isConfirmed = isLineupNowConfirmed(matchId);
            if (Boolean.TRUE.equals(isConfirmed)) {
                log.info("Match #{} has newly confirmed lineups! Triggering Stage 2 re-evaluation.", matchId);
                return false;
            }

            // 6. Check if team match has entered the final pre-match verification window (sport-specific: 45m football, 15m basketball)
            int finalWindow = sp != null ? sp.getLineupConfirmationWindowMinutes() : 45;

            long diffMinutes = 9999L;
            Object startTimeObj = r.get("match_start_time");
            if (startTimeObj instanceof java.sql.Timestamp ts) {
                diffMinutes = (ts.getTime() - nowMs) / (60L * 1000L);
            } else if (startTimeObj instanceof java.time.LocalDateTime ldt) {
                long startMs = ldt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                diffMinutes = (startMs - nowMs) / (60L * 1000L);
            }

            if (diffMinutes <= finalWindow && diffMinutes >= -15) {
                if ("WAIT_KICKOFF".equals(stage) && diffMinutes > 1) {
                    if (Boolean.TRUE.equals(isLineupNowConfirmed(matchId))) {
                        log.info("Match #{} in WAIT_KICKOFF now has confirmed official lineups! Triggering pre-match re-evaluation.", matchId);
                        return false;
                    }
                    return true; // Still waiting for kickoff (1st minute)
                }
                if ("STAGE_2_PREVIEW".equals(stage)) {
                    return true; // Already reached Stage 2
                }
                log.info("Match #{} is in final pre-match window (starts in {} mins, window {}m). Triggering Stage 2 evaluation.", matchId, diffMinutes, finalWindow);
                return false;
            }

            return true; // Lineups not yet confirmed and kickoff is > finalWindow minutes away
        } catch (Exception e) {
            log.warn("Failed to check evaluation status for match {}: {}", matchId, e.getMessage());
            return false;
        }
    }

    public Boolean isLineupNowConfirmed(Long matchId) {
        try {
            CanonicalMatchHeaderDto header = fetchMdmMatchHeader(matchId);
            if (header != null && header.getSofascoreId() != null) {
                Integer sofaCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM sofascore_raw_events WHERE event_id = ? " +
                        "AND lineups_json IS NOT NULL AND lineups_json != '' AND lineups_json != '{}' " +
                        "AND (lineups_json ILIKE '%\"confirmed\":true%' OR lineups_json ILIKE '%\"confirmed\": true%')",
                        Integer.class, header.getSofascoreId());
                return sofaCount != null && sofaCount > 0;
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

    private String stripAccents(String s) {
        if (s == null) return "";
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("(?i)^(FC|CF|AC|AS|FK|SC|Club|Deportivo|Real|KK|BC|BK|CSKA|SK|HC|HK|EHC|EC)\\s+", "")
                .replaceAll("(?i)\\s+(FC|CF|FK|SC|BC|KK)$", "")
                .trim();
    }

    private List<DossierRecentMatchDto> fetchRecentMatches(Sport sport, Long teamId, String teamName) {
        String sClean = sport != null ? sport.getSlug() : "football";
        List<DossierRecentMatchDto> list = new ArrayList<>();

        // Priority 1: O(1) indexed lookup by canonical MDM team ID
        if (teamId != null && teamId > 0) {
            list = fetchRecentMatchesByMdmTeamId(sport, teamId);
        }

        // Priority 1.5: If fewer than 2 matches by ID, check mdm_team canonical name if available
        if (list.size() < 2 && teamId != null && teamId > 0) {
            try {
                String mdmCanonical = jdbcTemplate.queryForObject(
                        "SELECT canonical_name FROM mdm_team WHERE id = ?", String.class, teamId);
                if (mdmCanonical != null && !mdmCanonical.isBlank() && !mdmCanonical.equalsIgnoreCase(teamName)) {
                    List<DossierRecentMatchDto> mdmMatches = fetchRecentMatchesInternal(sport, mdmCanonical);
                    if (mdmMatches.size() > list.size()) {
                        list = mdmMatches;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Priority 2: Fallback to exact string matching if fewer than 2 matches found by ID
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            List<DossierRecentMatchDto> exactMatches = fetchRecentMatchesInternal(sport, teamName);
            if (exactMatches.size() > list.size()) {
                list = exactMatches;
            }
        }

        // Priority 2.5: Fallback to stripped gender/age markers
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            String base = teamName.replaceAll("(?i)\\s*\\([жw]\\)", "")
                    .replaceAll("(?i)\\s*\\(жен\\)", "")
                    .replaceAll("(?i)\\s*\\(women\\)", "")
                    .replaceAll("(?i)\\s*\\(d\\)", "")
                    .replaceAll("(?i)\\s*\\(games\\)", "")
                    .trim();
            if (!base.equalsIgnoreCase(teamName) && base.length() >= 3) {
                List<DossierRecentMatchDto> baseMatches = fetchRecentMatchesInternal(sport, base);
                if (baseMatches.size() > list.size()) {
                    list = baseMatches;
                }
            }
        }

        // Priority 3: Cross-language Cyrillic -> Latin transliteration using SimilarityUtils
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            if (SimilarityUtils.hasCyrillic(teamName)) {
                String cyrTranslit = SimilarityUtils.transliterateCyrillic(teamName).trim();
                if (!cyrTranslit.equalsIgnoreCase(teamName) && cyrTranslit.length() >= 3) {
                    List<DossierRecentMatchDto> cyrMatches = fetchRecentMatchesInternal(sport, cyrTranslit);
                    if (cyrMatches.size() > list.size()) {
                        list = cyrMatches;
                    }
                }
            }
        }

        // Priority 3.5: Fallback to normalized, unaccented and transliterated exact matching (Strict Zero-Wildcard Policy)
        if (list.size() < 2 && teamName != null && !teamName.isBlank()) {
            String cleanTeam = stripAccents(teamName);
            if (!cleanTeam.equalsIgnoreCase(teamName) && cleanTeam.length() >= 3) {
                List<DossierRecentMatchDto> fallback = fetchRecentMatchesInternal(sport, cleanTeam);
                if (fallback.size() > list.size()) {
                    list = fallback;
                }
            }
            if (list.size() < 2) {
                String transliterated = SlugUtils.transliterate(teamName).trim();
                if (!transliterated.equalsIgnoreCase(teamName) && transliterated.length() >= 3) {
                    List<DossierRecentMatchDto> transMatches = fetchRecentMatchesInternal(sport, transliterated);
                    if (transMatches.size() > list.size()) {
                        list = transMatches;
                    }
                }
            }
        }

        // Priority 4: For solo/racket sports (table-tennis, tennis), match by player surname prefix
        if (list.size() < 2 && teamName != null && !teamName.isBlank() && sport != null && !sport.isTeamSport()) {
            List<DossierRecentMatchDto> surnameMatches = fetchRecentMatchesBySurname(sport, teamName);
            if (surnameMatches.size() > list.size()) {
                list = surnameMatches;
            }
        }

        // Priority 5: If fewer than 2 matches, query ClickHouse DDS by resolved SofaScore ID
        if (list.size() < 2) {
            Long sofaId = resolveTeamSofaScoreId(teamId, teamName);
            if (sofaId != null && sofaId > 0) {
                List<DossierRecentMatchDto> chMatches = fetchRecentMatchesFromClickHouse(sport, sofaId);
                if (!chMatches.isEmpty()) {
                    Set<Long> existingDates = list.stream()
                            .map(DossierRecentMatchDto::getMatchDate)
                            .filter(Objects::nonNull)
                            .map(val -> val > 0 ? (val < 10_000_000_000L ? val * 1000L : val) : null)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    for (DossierRecentMatchDto chMatch : chMatches) {
                        Long dateMs = chMatch.getMatchDate();
                        if (dateMs == null || !existingDates.contains(dateMs)) {
                            list.add(chMatch);
                            if (dateMs != null) existingDates.add(dateMs);
                        }
                        if (list.size() >= 10) break;
                    }
                    list.sort((a, b) -> {
                        long da = a.getMatchDate() != null ? a.getMatchDate() : 0L;
                        long db = b.getMatchDate() != null ? b.getMatchDate() : 0L;
                        return Long.compare(db, da);
                    });
                }
            }
        }

        return list;
    }

    public Long resolveTeamSofaScoreId(Long teamId, String teamName) {
        if (teamId == null || teamId <= 0) return null;
        return sofaIdCache.computeIfAbsent(teamId, id -> {
            try {
                // 1. Direct indexed lookup on mdm_team
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                        "SELECT sofascore_id, canonical_name, normalized_signature FROM mdm_team WHERE id = ?", id);
                if (rows.isEmpty()) return Optional.empty();
                Map<String, Object> r = rows.get(0);
                Number sofaNum = (Number) r.get("sofascore_id");
                if (sofaNum != null && sofaNum.longValue() > 0) {
                    return Optional.of(sofaNum.longValue());
                }

                // 2. Transliteration & inverted token matching for Cyrillic / bookmaker name order [Фамилия Имя] vs [First Last]
                String cName = (String) r.get("canonical_name");
                String queryName = (cName != null && !cName.isBlank()) ? cName : teamName;
                if (queryName != null && !queryName.isBlank()) {
                    String clean = queryName.replaceAll("\\s*\\([^)]*\\)", "").trim();
                    String latin = SimilarityUtils.transliterateCyrillic(clean).trim();
                    String[] tokens = latin.split("\\s+");

                    // Check individual surname / name tokens (length >= 4 to avoid noise)
                    for (String tok : tokens) {
                        String tClean = tok.toLowerCase().replaceAll("[^a-z]", "");
                        if (tClean.length() < 4) continue;
                        String tAlt = tClean.replace("yrou", "jirou").replace("yro", "jiro");

                        List<Long> matched = jdbcTemplate.query(
                                "SELECT sofascore_id FROM mdm_team WHERE sofascore_id IS NOT NULL AND " +
                                "(canonical_name ILIKE ? OR canonical_name ILIKE ? OR normalized_signature ILIKE ? OR normalized_signature ILIKE ?) " +
                                "ORDER BY id ASC LIMIT 1",
                                (rs, rowNum) -> rs.getLong("sofascore_id"),
                                "%" + tClean + "%", "%" + tAlt + "%", "%" + tClean + "%", "%" + tAlt + "%");

                        if (!matched.isEmpty() && matched.get(0) != null && matched.get(0) > 0) {
                            log.info("Resolved entity #{} ('{}', token '{}'/'{}') to canonical SofaScore ID #{}",
                                    id, queryName, tClean, tAlt, matched.get(0));
                            return Optional.of(matched.get(0));
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to resolve sofascore_id for team {}: {}", id, e.getMessage());
            }
            return Optional.empty();
        }).orElse(null);
    }

    private List<DossierRecentMatchDto> fetchRecentMatchesFromClickHouse(Sport sport, Long sofaId) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        if (sofaId == null || sofaId <= 0 || clickHouse == null || sport == null || !sport.isDdsEnabled()) {
            return list;
        }
        try {
            String table = sport.getDdsTableName();
            String p1Col = sport.isTeamSport() ? "home_team_id" : "player1_id";
            String p2Col = sport.isTeamSport() ? "away_team_id" : "player2_id";
            String tCol = "match_time";

            String sql = "SELECT " +
                    "if(" + p1Col + " = " + sofaId + ", score1, score2) as team_score, " +
                    "if(" + p1Col + " = " + sofaId + ", score2, score1) as opponent_score, " +
                    "if(if(" + p1Col + " = " + sofaId + ", score1, score2) > if(" + p1Col + " = " + sofaId + ", score2, score1), 'WIN', 'LOSS') as result, " +
                    "toUnixTimestamp(" + tCol + ") * 1000 as match_date " +
                    "FROM " + table + " " +
                    "WHERE " + p1Col + " = " + sofaId + " OR " + p2Col + " = " + sofaId + " " +
                    "ORDER BY " + tCol + " DESC LIMIT 10";

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("recent_ch:" + table + ":" + sofaId, sql);
            for (Map<String, Object> r : rows) {
                int ts = toIntSafe(r.get("team_score"), 0);
                int os = toIntSafe(r.get("opponent_score"), 0);
                long dateMs = toLongSafe(r.get("match_date"), 0L);
                String res = String.valueOf(r.getOrDefault("result", ts > os ? "WIN" : "LOSS"));

                DossierRecentMatchDto m = DossierRecentMatchDto.builder()
                        .result(res)
                        .teamScore(ts)
                        .opponentScore(os)
                        .matchDate(dateMs)
                        .cleanSheet(os == 0)
                        .failedToScore(ts == 0)
                        .build();
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("Failed ClickHouse DDS recent matches for {} sofaId {}: {}", sport.getSlug(), sofaId, e.getMessage());
        }
        return list;
    }

    private static int toIntSafe(Object obj, int def) {
        if (obj == null) return def;
        if (obj instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return def;
        }
    }

    private static long toLongSafe(Object obj, long def) {
        if (obj == null) return def;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return def;
        }
    }

    private static Double toBdDouble(java.math.BigDecimal bd) {
        return bd != null ? bd.doubleValue() : null;
    }

    private List<DossierRecentMatchDto> fetchRecentMatchesBySurname(Sport sport, String rawName) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        try {
            // Strip any parenthesized annotations: (Чех), (Рос), (Cze), (W), (D), etc.
            String clean = rawName.replaceAll("\\s*\\([^)]*\\)", "").trim();
            if (SimilarityUtils.hasCyrillic(clean)) {
                clean = SimilarityUtils.transliterateCyrillic(clean).trim();
            }
            String[] parts = clean.split("\\s+");
            if (parts.length == 0) return list;

            // Collect candidate surname words: both parts[0] (e.g. Nokun) and parts[last] (e.g. Balcar)
            java.util.Set<String> candidates = new java.util.LinkedHashSet<>();
            if (parts[0].length() >= 3) candidates.add(parts[0]);
            if (parts[parts.length - 1].length() >= 3) candidates.add(parts[parts.length - 1]);

            // Also add phonetic variants for Polish / Czech transliterations:
            // 'k' <-> 'c' (Nokun <-> Nocun), 'w' <-> 'v' (Nowak <-> Novak)
            java.util.Set<String> allVariants = new java.util.LinkedHashSet<>(candidates);
            for (String cand : candidates) {
                if (cand.toLowerCase().contains("k")) {
                    allVariants.add(cand.replaceAll("(?i)k", "c"));
                }
                if (cand.toLowerCase().contains("c")) {
                    allVariants.add(cand.replaceAll("(?i)c", "k"));
                }
                if (cand.toLowerCase().contains("w")) {
                    allVariants.add(cand.replaceAll("(?i)w", "v"));
                }
                if (cand.toLowerCase().contains("v")) {
                    allVariants.add(cand.replaceAll("(?i)v", "w"));
                }
            }

            String sql = "SELECT home_team, away_team, home_score, away_score, match_date " +
                         "FROM sofascore_raw_events " +
                         "WHERE sport = ? AND (home_team ILIKE ? OR away_team ILIKE ?) " +
                         "  AND status = 'finished' AND home_score IS NOT NULL " +
                         "ORDER BY match_date DESC LIMIT 10";

            List<Map<String, Object>> rows = Collections.emptyList();
            String matchedVariant = null;

            for (String variant : allVariants) {
                if (variant.length() < 3) continue;
                // Pattern 1: "Surname %" (e.g. "Nocun Z.", "Heuman I.")
                rows = jdbcTemplate.queryForList(sql, sport.getSlug(), variant + " %", variant + " %");
                if (!rows.isEmpty()) {
                    matchedVariant = variant;
                    break;
                }
                // Pattern 2: "% Surname" (e.g. "Zbigniew Nocun", "Tomas Balcar")
                rows = jdbcTemplate.queryForList(sql, sport.getSlug(), "% " + variant, "% " + variant);
                if (!rows.isEmpty()) {
                    matchedVariant = variant;
                    break;
                }
                // Pattern 3: substring if length >= 4
                if (variant.length() >= 4) {
                    rows = jdbcTemplate.queryForList(sql, sport.getSlug(), "%" + variant + "%", "%" + variant + "%");
                    if (!rows.isEmpty()) {
                        matchedVariant = variant;
                        break;
                    }
                }
            }

            if (matchedVariant == null || rows.isEmpty()) {
                return list;
            }

            String targetLower = matchedVariant.toLowerCase();
            for (Map<String, Object> r : rows) {
                Number hsNum = (Number) r.get("home_score");
                Number asNum = (Number) r.get("away_score");
                if (hsNum == null || asNum == null) continue;
                int hs = hsNum.intValue();
                int as = asNum.intValue();
                String ht = (String) r.get("home_team");
                boolean isHome = ht != null && ht.toLowerCase().contains(targetLower);
                int myScore = isHome ? hs : as;
                int oppScore = isHome ? as : hs;
                String res = myScore > oppScore ? "WIN" : (myScore < oppScore ? "LOSS" : "DRAW");

                DossierRecentMatchDto m = DossierRecentMatchDto.builder()
                        .result(res)
                        .teamScore(myScore)
                        .opponentScore(oppScore)
                        .matchDate(toLongSafe(r.get("match_date"), 0L))
                        .cleanSheet(oppScore == 0)
                        .failedToScore(myScore == 0)
                        .build();
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("Surname match fallback failed for {}: {}", rawName, e.getMessage());
        }
        return list;
    }

    private List<DossierRecentMatchDto> fetchRecentMatchesByMdmTeamId(Sport sport, Long teamId) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        try {
            String sql = "SELECT home_team, away_team, home_score, away_score, match_date, mdm_home_team_id, mdm_away_team_id " +
                         "FROM sofascore_raw_events " +
                         "WHERE sport = ? AND (mdm_home_team_id = ? OR mdm_away_team_id = ?) " +
                         "  AND status = 'finished' AND home_score IS NOT NULL " +
                         "ORDER BY match_date DESC LIMIT 10";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sport.getSlug(), teamId, teamId);
            for (Map<String, Object> r : rows) {
                Number hsNum = (Number) r.get("home_score");
                Number asNum = (Number) r.get("away_score");
                if (hsNum == null || asNum == null) continue;
                int hs = hsNum.intValue();
                int as = asNum.intValue();
                Number hId = (Number) r.get("mdm_home_team_id");
                boolean isHome = hId != null && hId.longValue() == teamId;

                int myScore = isHome ? hs : as;
                int oppScore = isHome ? as : hs;
                String res = myScore > oppScore ? "WIN" : (myScore < oppScore ? "LOSS" : "DRAW");

                DossierRecentMatchDto m = DossierRecentMatchDto.builder()
                        .result(res)
                        .teamScore(myScore)
                        .opponentScore(oppScore)
                        .matchDate(toLongSafe(r.get("match_date"), 0L))
                        .cleanSheet(oppScore == 0)
                        .failedToScore(myScore == 0)
                        .build();
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("Failed MDM ID recent matches for teamId {}: {}", teamId, e.getMessage());
        }
        return list;
    }

    private List<DossierRecentMatchDto> fetchRecentMatchesInternal(Sport sport, String teamName) {
        List<DossierRecentMatchDto> list = new ArrayList<>();
        try {
            String sql = "SELECT home_team, away_team, home_score, away_score, match_date " +
                         "FROM sofascore_raw_events " +
                         "WHERE sport = ? AND (LOWER(home_team) = LOWER(?) OR LOWER(away_team) = LOWER(?)) " +
                         "  AND status = 'finished' AND home_score IS NOT NULL " +
                         "  AND home_team NOT ILIKE '%Reserve%' AND away_team NOT ILIKE '%Reserve%' " +
                         "  AND home_team NOT ILIKE '% U1%' AND away_team NOT ILIKE '% U1%' " +
                         "  AND home_team NOT ILIKE '% U2%' AND away_team NOT ILIKE '% U2%' " +
                         "ORDER BY match_date DESC LIMIT 10";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sport.getSlug(), teamName, teamName);
            for (Map<String, Object> r : rows) {
                String ht = (String) r.get("home_team");
                Number hsNum = (Number) r.get("home_score");
                Number asNum = (Number) r.get("away_score");
                if (hsNum == null || asNum == null) continue;
                int hs = hsNum.intValue();
                int as = asNum.intValue();
                boolean isHome = teamName.equalsIgnoreCase(ht);
                int myScore = isHome ? hs : as;
                int oppScore = isHome ? as : hs;
                String res = myScore > oppScore ? "WIN" : (myScore < oppScore ? "LOSS" : "DRAW");

                DossierRecentMatchDto m = DossierRecentMatchDto.builder()
                        .result(res)
                        .teamScore(myScore)
                        .opponentScore(oppScore)
                        .matchDate(toLongSafe(r.get("match_date"), 0L))
                        .cleanSheet(oppScore == 0)
                        .failedToScore(myScore == 0)
                        .build();
                list.add(m);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch recent matches for team {}: {}", teamName, e.getMessage());
        }
        return list;
    }
}
