package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;
import pro.datawiki.igaming.analyst.ml.vector.DerbyRivalryDetector;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;
import pro.datawiki.igaming.dto.MatchBroadcastDto;
import pro.datawiki.igaming.dto.mood.MatchMood;
import pro.datawiki.igaming.dto.mood.MatchMoodCardDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service orchestrating match mood discovery and excitement ranking.
 * Backed by in-memory 60s caching for sub-50ms query latency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchMoodDiscoveryService {

    private final MatchAiRecommendationRepository recommendationRepository;
    private final MatchMoodEvaluator moodEvaluator;
    private final pro.datawiki.igaming.analyst.ml.vector.ClickHouseDdsClient clickHouseDdsClient;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    // Fast in-memory cache with 60-second TTL
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60_000L;

    private static class CacheEntry {
        final List<MatchMoodCardDto> cards;
        final long timestamp;

        CacheEntry(List<MatchMoodCardDto> cards) {
            this.cards = cards;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid() {
            return (System.currentTimeMillis() - timestamp) < CACHE_TTL_MS;
        }
    }

    public List<MatchMoodCardDto> discoverMatches(
            String moodStr,
            Boolean hasBroadcast,
            String window,
            int limit
    ) {
        return discoverMatches(moodStr, "FOOTBALL", hasBroadcast, window, limit);
    }

    /**
     * Discovers and ranks matches matching the specified mood preset and filters.
     */
    public List<MatchMoodCardDto> discoverMatches(
            String moodStr,
            String sport,
            Boolean hasBroadcast,
            String window,
            int limit
    ) {
        MatchMood mood = (moodStr != null && !moodStr.isBlank() && !"ALL".equalsIgnoreCase(moodStr))
                ? MatchMood.fromString(moodStr)
                : null;

        String cacheKey = String.format("mood:%s:sp:%s:bc:%s:win:%s:lim:%d",
                mood != null ? mood.name() : "ANY",
                sport != null ? sport.toUpperCase() : "ALL",
                hasBroadcast != null ? hasBroadcast : "any",
                window != null ? window.toUpperCase() : "ALL",
                limit
        );

        CacheEntry entry = cache.get(cacheKey);
        if (entry != null && entry.isValid()) {
            return entry.cards;
        }

        String searchSport = (sport != null && !sport.isBlank() && !"ALL".equalsIgnoreCase(sport))
                ? sport.trim().toUpperCase()
                : null;

        List<MatchAiRecommendation> recommendations;
        if (searchSport != null) {
            recommendations = recommendationRepository
                    .findTop200BySportNameIgnoreCaseAndEvaluationStageNotInOrderByIdDesc(searchSport, List.of("SETTLED", "CANCELLED", "SKIPPED"));
        } else {
            recommendations = recommendationRepository
                    .findTop200ByEvaluationStageNotInOrderByIdDesc(List.of("SETTLED", "CANCELLED", "SKIPPED"));
        }

        if (recommendations.isEmpty()) {
            recommendations = recommendationRepository.findTop500ByOrderByIdDesc();
        }

        // Fetch broadcast and enrichment metadata in a single bulk query (safe fallback)
        Map<Long, Map<String, Object>> enrichmentMap = fetchEnrichmentsBulk(recommendations);

        LocalDateTime now = LocalDateTime.now();
        List<MatchMoodCardDto> matchedCards = new ArrayList<>();

        for (MatchAiRecommendation rec : recommendations) {
            if (rec == null) continue;

            // 1. Time Window Filter
            if ("LIVE_NOW".equalsIgnoreCase(window)) {
                boolean isLive = "LIVE".equalsIgnoreCase(rec.getEvaluationStage()) || "IN_PLAY".equalsIgnoreCase(rec.getEvaluationStage());
                if (!isLive && rec.getMatchStartTime() != null) {
                    if (rec.getMatchStartTime().isAfter(now.plusMinutes(20)) || rec.getMatchStartTime().isBefore(now.minusHours(3))) {
                        continue;
                    }
                }
            } else if ("TONIGHT".equalsIgnoreCase(window)) {
                if (rec.getMatchStartTime() != null) {
                    if (rec.getMatchStartTime().isBefore(now.minusHours(2)) ||
                        rec.getMatchStartTime().isAfter(now.plusHours(24))) {
                        continue;
                    }
                }
            }

            // 2. Broadcast Enrichment
            Map<String, Object> enrich = enrichmentMap.getOrDefault(rec.getMatchId(), Collections.emptyMap());
            boolean matchHasBroadcast = Boolean.TRUE.equals(enrich.get("has_broadcast"));
            List<MatchBroadcastDto> broadcasts = parseBroadcasts(enrich.get("broadcasts_json"));

            if (Boolean.TRUE.equals(hasBroadcast) && !matchHasBroadcast && broadcasts.isEmpty()) {
                continue;
            }

            // 3. Build Evaluation Context
            MatchMoodEvaluator.MatchContext ctx = buildMatchContext(rec, enrich, matchHasBroadcast, broadcasts);

            // 4. Evaluate against Mood
            Optional<MatchMoodCardDto> cardOpt = (mood != null)
                    ? moodEvaluator.evaluateForMood(ctx, mood)
                    : moodEvaluator.evaluateBestMood(ctx);

            cardOpt.ifPresent(matchedCards::add);
        }

        // Sort by excitement score descending
        matchedCards.sort((a, b) -> Double.compare(b.getExcitementScore(), a.getExcitementScore()));

        int cappedLimit = Math.max(1, Math.min(50, limit));
        List<MatchMoodCardDto> result = matchedCards.stream()
                .limit(cappedLimit)
                .collect(Collectors.toList());

        cache.put(cacheKey, new CacheEntry(result));
        return result;
    }

    private MatchMoodEvaluator.MatchContext buildMatchContext(
            MatchAiRecommendation rec,
            Map<String, Object> enrich,
            boolean hasBroadcast,
            List<MatchBroadcastDto> broadcasts
    ) {
        Double pOver = rec.getFairOver25() != null ? rec.getFairOver25().doubleValue() : null;
        Double xg = parseDoubleSafe(rec.getExpectedXg(), null);
        if (xg == null) {
            if (pOver != null) {
                if (pOver > 0.80) xg = 3.45;
                else if (pOver > 0.65) xg = 3.10;
                else if (pOver > 0.50) xg = 2.70;
                else xg = 2.25;
            } else {
                xg = 2.65;
            }
        }

        // Convert probability (0..1) to decimal odds
        Double rawP1 = rec.getFairP1() != null ? rec.getFairP1().doubleValue() : null;
        Double rawPx = rec.getFairPx() != null ? rec.getFairPx().doubleValue() : null;
        Double rawP2 = rec.getFairP2() != null ? rec.getFairP2().doubleValue() : null;

        Double p1 = toOdds(rawP1);
        Double px = toOdds(rawPx);
        Double p2 = toOdds(rawP2);
        Double o25 = toOdds(pOver);

        // Approximate leverage / motivation
        double lev1 = rawP1 != null ? rawP1 : 0.50;
        double lev2 = rawP2 != null ? rawP2 : 0.50;
        if (enrich.containsKey("motivation_score1")) {
            lev1 = ((Number) enrich.get("motivation_score1")).doubleValue();
        }
        if (enrich.containsKey("motivation_score2")) {
            lev2 = ((Number) enrich.get("motivation_score2")).doubleValue();
        }

        boolean isDerby = DerbyRivalryDetector.isDerbyMatch(
                rec.getTeam1Name(),
                rec.getTeam2Name(),
                rec.getLeagueName(),
                rec.getTournamentSlug()
        );

        String primaryStreamUrl = null;
        String primaryStreamProvider = null;
        if (!broadcasts.isEmpty()) {
            MatchBroadcastDto b0 = broadcasts.get(0);
            primaryStreamUrl = b0.getEmbedUrl();
            primaryStreamProvider = b0.getProvider();
        }

        // Query ClickHouse Point-in-Time standings & motivation gradients
        Boolean isGod = null;
        Double entropy = null;
        Double blowoutGrad = null;
        Boolean isKo = null;
        Double paceProxy = null;

        if (clickHouseDdsClient != null && rec.getMatchId() != null) {
            try {
                Map<String, Object> pit = clickHouseDdsClient.getTournamentPointInTime(rec.getMatchId());
                if (pit != null && !pit.isEmpty()) {
                    if (pit.containsKey("is_group_of_death")) {
                        isGod = pit.get("is_group_of_death") instanceof Number n && n.intValue() == 1;
                    }
                    if (pit.get("group_entropy") instanceof Number n) {
                        entropy = n.doubleValue();
                    }
                    if (pit.get("home_blowout_gradient") instanceof Number n) {
                        blowoutGrad = n.doubleValue();
                    }
                    if (pit.containsKey("is_knockout_stage")) {
                        isKo = pit.get("is_knockout_stage") instanceof Number n && n.intValue() == 1;
                    }
                    if (pit.get("home_win_leverage") instanceof Number n) {
                        lev1 = n.doubleValue();
                    }
                    if (pit.get("away_win_leverage") instanceof Number n) {
                        lev2 = n.doubleValue();
                    }
                }
            } catch (Exception e) {
                log.debug("Could not fetch ClickHouse tournament PIT: {}", e.getMessage());
            }
        }

        String sportUpper = rec.getSportName() != null ? rec.getSportName().toUpperCase() : "";
        if (sportUpper.contains("BASKET")) {
            paceProxy = 0.72;
        } else if (sportUpper.contains("HOCKEY")) {
            paceProxy = 0.65;
        }

        return MatchMoodEvaluator.MatchContext.builder()
                .matchId(rec.getMatchId())
                .matchSlug(rec.getMatchSlug())
                .sportName(rec.getSportName())
                .sportSlug(rec.getSportSlug())
                .leagueName(rec.getLeagueName())
                .team1Name(rec.getTeam1Name())
                .team2Name(rec.getTeam2Name())
                .matchStartTime(rec.getMatchStartTime())
                .matchStatus(rec.getEvaluationStage())
                .currentScore(rec.getFinalScore())
                .expectedTotalXg(xg)
                .oddsP1(p1)
                .oddsPx(px)
                .oddsP2(p2)
                .oddsOver25(o25)
                .winLeverageHome(lev1)
                .winLeverageAway(lev2)
                .isDerby(isDerby)
                .derbyFrictionIndex(isDerby ? 0.90 : 0.25)
                .compositeCardBias(isDerby ? 0.85 : 0.45)
                .refereeAvgYellowCards(isDerby ? 5.5 : 4.0)
                .highEventRate(xg >= 3.0 ? 0.45 : 0.25)
                .lateGoalsRatio(0.26)
                .benchAttackDepth(0.70)
                .isGroupOfDeath(isGod)
                .groupEntropy(entropy)
                .blowoutGradient(blowoutGrad)
                .isKnockout(isKo)
                .paceProxy(paceProxy)
                .hasBroadcast(hasBroadcast)
                .broadcasts(broadcasts)
                .primaryStreamUrl(primaryStreamUrl)
                .primaryStreamProvider(primaryStreamProvider)
                .build();
    }

    private Map<Long, Map<String, Object>> fetchEnrichmentsBulk(List<MatchAiRecommendation> recommendations) {
        if (recommendations.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> matchIds = recommendations.stream()
                .map(MatchAiRecommendation::getMatchId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (matchIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String inClause = matchIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String sql = "SELECT match_id, has_broadcast, broadcasts_json, motivation_score1, motivation_score2 " +
                     "FROM match_enrichment WHERE match_id IN (" + inClause + ")";

        Map<Long, Map<String, Object>> map = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            for (Map<String, Object> r : rows) {
                Long mId = ((Number) r.get("match_id")).longValue();
                map.put(mId, r);
            }
        } catch (Exception e) {
            log.debug("Enrichment bulk query skipped (cross-database): {}", e.getMessage());
        }
        return map;
    }

    private List<MatchBroadcastDto> parseBroadcasts(Object rawJson) {
        if (rawJson == null) return Collections.emptyList();
        String jsonStr = rawJson.toString().trim();
        if (jsonStr.isEmpty() || "[]".equals(jsonStr)) return Collections.emptyList();

        try {
            return objectMapper.readValue(jsonStr, new TypeReference<List<MatchBroadcastDto>>() {});
        } catch (Exception e) {
            log.debug("Failed to parse broadcasts JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Double toOdds(Double prob) {
        if (prob == null) {
            return null;
        }
        if (prob > 0 && prob <= 1.0) {
            return 1.0 / prob;
        }
        return prob;
    }

    private Double parseDoubleSafe(String val, Double defaultVal) {
        if (val == null || val.isBlank()) return defaultVal;
        try {
            return Double.parseDouble(val.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
