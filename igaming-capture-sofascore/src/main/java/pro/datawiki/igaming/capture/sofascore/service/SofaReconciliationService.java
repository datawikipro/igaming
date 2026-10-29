package pro.datawiki.igaming.capture.sofascore.service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.capture.sofascore.provider.SofaScoreFixtureProvider;
import pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto;
import pro.datawiki.igaming.dto.live.MatchStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.loader.SofaTaskProcessor;
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer;
import pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer.ParsedSignature;
import pro.datawiki.igaming.dto.util.SimilarityUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * High-performance autonomous reconciliation service.
 * Ingests and links SofaScore ground truth events from sofascore_raw_events to match_record in PostgreSQL.
 * Guarantees 100% strict adherence to domain rules:
 * 1. Gender isolation: Men never match women.
 * 2. Senior vs Youth isolation: Senior teams never match youth teams.
 * 3. Youth compatibility: Generic youth matches specific youth tiers without numeric cross-collision (e.g. U17 != U19).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SofaReconciliationService {

    private final JdbcTemplate jdbcTemplate;
    private final SofaScoreFixtureProvider fixtureProvider;
    private final RestTemplate restTemplate;

    @Value("${app.mdm.url:http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095}")
    private String mdmUrl;

    private final AtomicBoolean backfillRunning = new AtomicBoolean(false);

    private static final List<String> PRIMARY_SPORTS = List.of(
            "football", "basketball", "ice-hockey", "tennis", "table-tennis", "volleyball",
            "handball", "esports", "futsal", "baseball", "cricket", "darts", "snooker",
            "american-football", "rugby", "waterpolo", "floorball"
    );

    private static final Map<String, String> DB_TO_SOFA_SPORT = Map.ofEntries(
            Map.entry("FOOTBALL", "football"),
            Map.entry("BASKETBALL", "basketball"),
            Map.entry("BASKETBALL_3X3", "basketball"),
            Map.entry("KORIPALLO", "basketball"),
            Map.entry("HOCKEY", "ice-hockey"),
            Map.entry("TENNIS", "tennis"),
            Map.entry("TABLE_TENNIS", "table-tennis"),
            Map.entry("PINGPONG", "table-tennis"),
            Map.entry("VOLLEYBALL", "volleyball"),
            Map.entry("BEACH_VOLLEYBALL", "volleyball"),
            Map.entry("LENTOPALLO", "volleyball"),
            Map.entry("HANDBALL", "handball"),
            Map.entry("BASEBALL", "baseball"),
            Map.entry("AMERICAN_FOOTBALL", "american-football"),
            Map.entry("RUGBY_UNION", "rugby"),
            Map.entry("RUGBY_LEAGUE", "rugby"),
            Map.entry("AUSTRALIAN_RULES", "aussie-rules"),
            Map.entry("CRICKET", "cricket"),
            Map.entry("DARTS", "darts"),
            Map.entry("SNOOKER", "snooker"),
            Map.entry("BILLIARDS", "snooker"),
            Map.entry("BADMINTON", "badminton"),
            Map.entry("FUTSAL", "futsal"),
            Map.entry("WATER_POLO", "waterpolo"),
            Map.entry("FLOORBALL", "floorball"),
            Map.entry("MMA", "mma"),
            Map.entry("BOXING", "mma"),
            Map.entry("MOTORSPORTS", "motorsport"),
            Map.entry("CYCLING", "cycling"),
            Map.entry("ESPORTS", "esports"),
            Map.entry("CS2", "esports"),
            Map.entry("DOTA2", "esports"),
            Map.entry("LEAGUE_OF_LEGENDS", "esports"),
            Map.entry("VALORANT", "esports"),
            Map.entry("CYBER_FOOTBALL", "esports"),
            Map.entry("CYBER_BASKETBALL", "esports"),
            Map.entry("CYBER_HOCKEY", "esports")
    );

    @Data
    @Builder
    public static class ReconciliationStats {
        private int totalProcessed;
        private int newlyMatched;
        private int womenMatches;
        private int youthMatches;
        private long durationMs;
    }

    /**
     * Reconciles unmapped matches within the specified time window.
     */
    public ReconciliationStats reconcileWindow(Instant windowStart, Instant windowEnd, boolean forceAll) {
        long t0 = System.currentTimeMillis();
        long startMs = windowStart.toEpochMilli();
        long endMs = windowEnd.toEpochMilli();

        log.info("[SofaReconciliation] Starting reconciliation for window {} to {}...", windowStart, windowEnd);

        // 1. Query raw events in window
        String rawSql = """
            SELECT event_id, sport, match_date, home_team, away_team, tournament, home_score, away_score, status, lineups_json, h2h_json, streaks_json
            FROM sofascore_raw_events
            WHERE match_date BETWEEN ? AND ?
            ORDER BY match_date DESC
        """;

        List<RawEventIndexed> rawEvents = jdbcTemplate.query(rawSql, (rs, rowNum) -> {
            long eventId = rs.getLong("event_id");
            String sport = rs.getString("sport");
            long matchDate = rs.getLong("match_date");
            String home = rs.getString("home_team");
            String away = rs.getString("away_team");
            String tourn = rs.getString("tournament");
            Integer homeScore = (Integer) rs.getObject("home_score");
            Integer awayScore = (Integer) rs.getObject("away_score");
            String status = rs.getString("status");
            String lineups = rs.getString("lineups_json");
            String h2h = rs.getString("h2h_json");
            String streaks = rs.getString("streaks_json");

            ParsedSignature shome = TeamSignatureNormalizer.parse(home, tourn, null);
            ParsedSignature saway = TeamSignatureNormalizer.parse(away, tourn, null);

            boolean isWomen = shome.gender() == TeamSignatureNormalizer.Gender.FEMALE
                    || saway.gender() == TeamSignatureNormalizer.Gender.FEMALE;
            boolean isYouth = shome.isYouth() || saway.isYouth();

            Set<String> tokens = new HashSet<>();
            tokens.addAll(extractTokens(shome.coreClubName()));
            tokens.addAll(extractTokens(saway.coreClubName()));

            boolean confirmed = lineups != null && lineups.toLowerCase().contains("\"confirmed\":true");

            return new RawEventIndexed(
                    String.valueOf(eventId),
                    sport != null ? sport.toLowerCase() : "",
                    matchDate,
                    home,
                    away,
                    tourn,
                    shome,
                    saway,
                    isWomen,
                    isYouth,
                    tokens,
                    lineups,
                    h2h,
                    streaks,
                    confirmed,
                    homeScore,
                    awayScore,
                    status
            );
        }, startMs - 4 * 3600 * 1000L, endMs + 4 * 3600 * 1000L);

        log.info("[SofaReconciliation] Loaded {} raw events from sofascore_raw_events in {} ms",
                rawEvents.size(), System.currentTimeMillis() - t0);

        if (rawEvents.isEmpty()) {
            return ReconciliationStats.builder().durationMs(System.currentTimeMillis() - t0).build();
        }

        // 2. Build multi-sport index
        Map<String, SportIndex> sportIndexes = new HashMap<>();
        for (RawEventIndexed ev : rawEvents) {
            SportIndex idx = sportIndexes.computeIfAbsent(ev.sport(), s -> new SportIndex());
            long hour = ev.matchDate() / (3600 * 1000L);
            idx.byHour.computeIfAbsent(new HourBucket(hour, ev.isWomen(), ev.isYouth()), k -> new ArrayList<>()).add(ev);
            for (String tok : ev.tokens()) {
                if (tok.length() >= 4) {
                    idx.byToken.computeIfAbsent(tok, k -> new ArrayList<>()).add(ev);
                }
            }
        }

        // 3. Query unmapped matches in window from match_enrichment
        String unmappedFilter = forceAll ? "" : "AND (m.reference_provider IS NULL OR m.reference_fixture_id IS NULL)";
        String matchSql = """
            SELECT m.match_id as id, m.start_time, m.sport_name, t1.default_display_name as team1, t2.default_display_name as team2, l.name as league
            FROM match_enrichment m
            LEFT JOIN team t1 ON m.team1_id = t1.id
            LEFT JOIN team t2 ON m.team2_id = t2.id
            LEFT JOIN league l ON m.league_id = l.id
            WHERE m.start_time BETWEEN ? AND ?
        """ + unmappedFilter;

        List<CandidateDbMatch> candidateMatches = jdbcTemplate.query(matchSql, (rs, rowNum) -> new CandidateDbMatch(
                rs.getLong("id"),
                rs.getLong("start_time"),
                rs.getString("sport_name"),
                rs.getString("team1"),
                rs.getString("team2"),
                rs.getString("league")
        ), startMs, endMs);

        log.info("[SofaReconciliation] Loaded {} candidate matches to reconcile in {} ms",
                candidateMatches.size(), System.currentTimeMillis() - t0);

        // 4. Match candidates
        List<MatchUpdate> updates = new ArrayList<>();
        int womenCount = 0;
        int youthCount = 0;

        for (CandidateDbMatch m : candidateMatches) {
            String dbSport = m.sportName() != null ? m.sportName().toUpperCase() : "";
            String sofaSport = DB_TO_SOFA_SPORT.get(dbSport);
            if (sofaSport == null) continue;

            SportIndex sIdx = sportIndexes.get(sofaSport);
            if (sIdx == null) continue;

            ParsedSignature s1 = TeamSignatureNormalizer.parse(m.team1(), m.league(), null);
            ParsedSignature s2 = TeamSignatureNormalizer.parse(m.team2(), m.league(), null);

            boolean mWomen = s1.gender() == TeamSignatureNormalizer.Gender.FEMALE
                    || s2.gender() == TeamSignatureNormalizer.Gender.FEMALE;
            boolean mYouth = s1.isYouth() || s2.isYouth();

            long mTime = m.startTime();
            long mHour = mTime / (3600 * 1000L);

            Set<String> mTokens = new HashSet<>();
            mTokens.addAll(extractTokens(s1.coreClubName()));
            mTokens.addAll(extractTokens(s2.coreClubName()));

            List<RawEventIndexed> candidates = new ArrayList<>();
            Set<String> seenIds = new HashSet<>();

            // Token candidates
            for (String tok : mTokens) {
                if (tok.length() >= 4) {
                    List<RawEventIndexed> evs = sIdx.byToken.get(tok);
                    if (evs != null) {
                        for (RawEventIndexed ev : evs) {
                            if (ev.isWomen() == mWomen && ev.isYouth() == mYouth
                                    && Math.abs(ev.matchDate() - mTime) <= 4 * 3600 * 1000L) {
                                if (seenIds.add(ev.eventId())) {
                                    candidates.add(ev);
                                }
                            }
                        }
                    }
                }
            }

            // Fallback: Hour bucket with fast pre-filter
            if (candidates.isEmpty()) {
                for (long delta = -1; delta <= 1; delta++) {
                    List<RawEventIndexed> hourEvs = sIdx.byHour.get(new HourBucket(mHour + delta, mWomen, mYouth));
                    if (hourEvs != null) {
                        for (RawEventIndexed ev : hourEvs) {
                            if (seenIds.add(ev.eventId())) {
                                // Fast heuristic check: token intersection or 2-char prefix match
                                if (!Collections.disjoint(mTokens, ev.tokens())
                                        || matchesPrefix(s1.coreClubName(), ev.homeTeam())
                                        || matchesPrefix(s2.coreClubName(), ev.awayTeam())) {
                                    candidates.add(ev);
                                }
                            }
                        }
                    }
                }
            }

            // Check candidate similarities
            for (RawEventIndexed ev : candidates) {
                boolean direct = SimilarityUtils.areTeamsSimilar(m.team1(), ev.homeTeam(), m.league(), ev.tournament())
                        && SimilarityUtils.areTeamsSimilar(m.team2(), ev.awayTeam(), m.league(), ev.tournament());
                boolean reversed = SimilarityUtils.areTeamsSimilar(m.team1(), ev.awayTeam(), m.league(), ev.tournament())
                        && SimilarityUtils.areTeamsSimilar(m.team2(), ev.homeTeam(), m.league(), ev.tournament());

                if (direct || reversed) {
                    if (mWomen) womenCount++;
                    if (mYouth) youthCount++;
                    updates.add(new MatchUpdate(
                            m.id(),
                            ev.eventId(),
                            ev.isConfirmed(),
                            ev.lineupsJson(),
                            ev.h2hJson(),
                            ev.streaksJson(),
                            ev.homeScore(),
                            ev.awayScore(),
                            ev.status(),
                            direct
                    ));
                    break;
                }
            }
        }

        // 5. Batch update PostgreSQL
        if (!updates.isEmpty()) {
            String updateSql = """
                UPDATE match_enrichment
                SET reference_provider = 'SOFASCORE',
                    reference_fixture_id = ?,
                    is_reference_verified = true,
                    is_lineup_confirmed = CASE WHEN ? THEN true ELSE is_lineup_confirmed END,
                    lineups_json = COALESCE(?, lineups_json),
                    h2h_json = COALESCE(?, h2h_json),
                    storyline_json = COALESCE(?, storyline_json),
                    enrichment_captured_at = NOW(),
                    updated_at = NOW()
                WHERE match_id = ?
            """;

            jdbcTemplate.batchUpdate(updateSql, updates, 500, (ps, u) -> {
                ps.setString(1, u.eventId());
                ps.setBoolean(2, u.isConfirmed());
                ps.setString(3, u.lineupsJson());
                ps.setString(4, u.h2hJson());
                ps.setString(5, u.streaksJson());
                ps.setLong(6, u.matchId());
            });

            // Also promote SofaScore link to Ground Truth MDM
            for (MatchUpdate u : updates) {
                try {
                    String promoteUrl = mdmUrl + "/api/v1/mdm/matches/" + u.matchId() + "/ground-truth?sofascoreId=" + u.eventId() + "&sourceOrigin=SOFASCORE&reason=RECONCILIATION";
                    restTemplate.postForObject(promoteUrl, null, Object.class);
                } catch (Exception ex) {
                    log.debug("[SofaReconciliation] MDM Ground Truth promotion skipped for match #{}: {}", u.matchId(), ex.getMessage());
                }
            }

            // Propagate verified scores and statuses to MDM Live Score Consensus
            for (MatchUpdate u : updates) {
                if (u.status() != null && (u.homeScore() != null || u.awayScore() != null)) {
                    try {
                        MatchStatus st = SofaTaskProcessor.mapSofaStatus(u.status());
                        Integer s1 = u.direct() ? u.homeScore() : u.awayScore();
                        Integer s2 = u.direct() ? u.awayScore() : u.homeScore();
                        CanonicalLiveScoreDto liveDto = CanonicalLiveScoreDto.builder()
                                .matchId(u.matchId())
                                .sourceCode("SOFASCORE")
                                .status(st)
                                .score1(s1)
                                .score2(s2)
                                .displayTimer(st == MatchStatus.FINISHED ? "FT" : "")
                                .timestamp(System.currentTimeMillis())
                                .build();
                        restTemplate.postForObject(mdmUrl + "/api/v1/mdm/matches/" + u.matchId() + "/live-score", liveDto, CanonicalLiveScoreDto.class);
                    } catch (Exception ex) {
                        log.debug("[SofaReconciliation] MDM live score propagation skipped for match #{}: {}", u.matchId(), ex.getMessage());
                    }
                }
            }

            log.info("[SofaReconciliation] Successfully updated {} matches (women={}, youth={}) in DB",
                    updates.size(), womenCount, youthCount);
        }

        long duration = System.currentTimeMillis() - t0;
        log.info("[SofaReconciliation] Finished window reconciliation in {} ms. Matched: {}", duration, updates.size());

        return ReconciliationStats.builder()
                .totalProcessed(candidateMatches.size())
                .newlyMatched(updates.size())
                .womenMatches(womenCount)
                .youthMatches(youthCount)
                .durationMs(duration)
                .build();
    }

    /**
     * Checks all historical dates up to pastDays and backfills any with missing events.
     */
    public void backfillMissingHistoricalDates(int pastDays) {
        if (!backfillRunning.compareAndSet(false, true)) {
            log.info("[SofaBackfill] Backfill already in progress, skipping.");
            return;
        }

        try {
            LocalDate today = LocalDate.now();
            log.info("[SofaBackfill] Starting autonomous check for past {} days...", pastDays);

            for (int i = 1; i <= pastDays; i++) {
                LocalDate targetDate = today.minusDays(i);
                long dayStartMs = targetDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
                long dayEndMs = dayStartMs + 24 * 3600 * 1000L - 1L;

                Integer count = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM sofascore_raw_events WHERE match_date BETWEEN ? AND ? AND sport = 'football'",
                        Integer.class,
                        dayStartMs, dayEndMs
                );

                if (count == null || count < 100) {
                    log.info("[SofaBackfill] Date {} has only {} football events in DB. Backfilling all sports...", targetDate, count);
                    for (String sport : PRIMARY_SPORTS) {
                        try {
                            fixtureProvider.fetchScheduledFixtures(sport, targetDate);
                        } catch (Exception e) {
                            log.debug("[SofaBackfill] Error fetching {} on {}: {}", sport, targetDate, e.getMessage());
                        }
                    }
                    // Reconcile this day
                    reconcileWindow(Instant.ofEpochMilli(dayStartMs), Instant.ofEpochMilli(dayEndMs), false);
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            log.info("[SofaBackfill] Autonomous backfill check completed.");
        } finally {
            backfillRunning.set(false);
        }
    }

    private static Set<String> extractTokens(String name) {
        if (name == null || name.isBlank()) return Collections.emptySet();
        String norm = SimilarityUtils.normalizeToLatin(name);
        String[] words = norm.split("[^a-z0-9]+");
        Set<String> set = new HashSet<>();
        for (String w : words) {
            if (w.length() >= 3 && !w.equals("the") && !w.equals("club") && !w.equals("team")) {
                set.add(w);
            }
        }
        return set;
    }

    private static boolean matchesPrefix(String s1, String s2) {
        if (s1 == null || s2 == null || s1.length() < 3 || s2.length() < 3) return false;
        String n1 = SimilarityUtils.normalizeToLatin(s1);
        String n2 = SimilarityUtils.normalizeToLatin(s2);
        return n1.regionMatches(true, 0, n2, 0, 3);
    }

    private static class SportIndex {
        Map<String, List<RawEventIndexed>> byToken = new HashMap<>();
        Map<HourBucket, List<RawEventIndexed>> byHour = new HashMap<>();
    }

    private record HourBucket(long hour, boolean isWomen, boolean isYouth) {}

    private record RawEventIndexed(
            String eventId,
            String sport,
            long matchDate,
            String homeTeam,
            String awayTeam,
            String tournament,
            ParsedSignature shome,
            ParsedSignature saway,
            boolean isWomen,
            boolean isYouth,
            Set<String> tokens,
            String lineupsJson,
            String h2hJson,
            String streaksJson,
            boolean isConfirmed
    ,
            Integer homeScore,
            Integer awayScore,
            String status) {}

    private record CandidateDbMatch(
            Long id,
            long startTime,
            String sportName,
            String team1,
            String team2,
            String league
    ) {}

    private record MatchUpdate(
            Long matchId,
            String eventId,
            boolean isConfirmed,
            String lineupsJson,
            String h2hJson,
            String streaksJson
    ,
            Integer homeScore,
            Integer awayScore,
            String status,
            boolean direct) {}
}
