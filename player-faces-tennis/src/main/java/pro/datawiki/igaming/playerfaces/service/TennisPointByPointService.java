package pro.datawiki.igaming.playerfaces.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.TennisSurface;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.TennisMatchCardDto;
import pro.datawiki.igaming.playerfaces.dto.TennisPointByPointEvent;
import pro.datawiki.igaming.playerfaces.dto.TennisSurfaceStats;
import pro.datawiki.igaming.playerfaces.dto.TiebreakStats;
import pro.datawiki.igaming.playerfaces.harvester.TennisStatsHarvester;
import pro.datawiki.igaming.playerfaces.sport.TennisSport;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing deep tennis point-by-point statistical harvesting,
 * MDM resolution, and Kafka streaming.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TennisPointByPointService {

    private final List<TennisStatsHarvester> harvesters;
    private final TennisMdmPlayerResolver mdmResolver;
    private final TennisPointByPointKafkaProducer kafkaProducer;
    private final TennisSport tennisSport;

    private final Map<String, TennisPointByPointEvent> cache = new ConcurrentHashMap<>();

    /**
     * Harvests deep point-by-point stats for a player, resolves MDM identity,
     * and streams the event to Kafka topic 'sports.tennis.point-by-point'.
     *
     * @param rawPlayerName raw or display player name
     * @param tour ATP, WTA, etc.
     * @return assembled TennisPointByPointEvent
     */
    public TennisPointByPointEvent harvestAndPublish(String rawPlayerName, TourType tour) {
        log.info("[Tennis PointByPoint Service] Harvesting point-by-point stats for '{}' ({})", rawPlayerName, tour);

        // 1. Resolve MDM player record
        PlayerFace player = mdmResolver.resolveOrCreatePlayer(rawPlayerName, tour);
        TourType effectiveTour = player.getTour() != null ? player.getTour() : tour;

        // 2. Query harvesters in priority order (Tennis Abstract first, then Tennis Explorer)
        List<TennisStatsHarvester> sortedHarvesters = harvesters.stream()
                .sorted(Comparator.comparingInt(TennisStatsHarvester::getOrder))
                .toList();

        Map<TennisSurface, TennisSurfaceStats> combinedStats = new EnumMap<>(TennisSurface.class);
        String selectedProvider = "AGGREGATED";

        for (TennisStatsHarvester harvester : sortedHarvesters) {
            Optional<Map<TennisSurface, TennisSurfaceStats>> res = harvester.harvestSurfaceStats(rawPlayerName, effectiveTour);
            if (res.isPresent() && !res.get().isEmpty()) {
                if (combinedStats.isEmpty()) {
                    selectedProvider = harvester.getProviderName();
                    combinedStats.putAll(res.get());
                } else {
                    // Merge secondary provider metrics if primary has gaps
                    res.get().forEach((surface, stats) -> combinedStats.putIfAbsent(surface, stats));
                }
            }
        }

        // 3. Assemble overall tiebreak metrics across surfaces
        TiebreakStats overallTb = aggregateTiebreakStats(combinedStats);

        // 4. Construct Kafka event
        List<String> aliases = player.getAliases() != null
                ? Arrays.stream(player.getAliases().split(";")).map(String::trim).toList()
                : List.of(player.getPlayerName());

        TennisPointByPointEvent event = TennisPointByPointEvent.builder()
                .mdmPlayerId(player.getId())
                .playerName(player.getPlayerName())
                .normalizedName(player.getNormalizedName())
                .aliases(aliases)
                .tour(effectiveTour)
                .countryCode(player.getCountryCode())
                .currentRanking(player.getRanking())
                .avatarUrl(player.getAvatarUrl())
                .sourceProvider(selectedProvider)
                .surfaceStats(combinedStats)
                .overallTiebreaks(overallTb)
                .timestamp(LocalDateTime.now())
                .build();

        // 5. Cache locally
        cache.put(player.getNormalizedName(), event);

        // 6. Stream to Kafka topic 'sports.tennis.point-by-point'
        kafkaProducer.publish(event);

        return event;
    }

    /**
     * Retrieves cached or fresh point-by-point stats for a player.
     */
    public TennisPointByPointEvent getStats(String playerName, TourType tour) {
        String normalized = PlayerFace.normalizeName(playerName);
        TennisPointByPointEvent cached = cache.get(normalized);
        if (cached != null) {
            return cached;
        }
        return harvestAndPublish(playerName, tour);
    }

    /**
     * Evaluates surface advantage and projected hold probabilities for a match card.
     */
    public Map<String, Object> evaluateMatchEdge(String player1, String player2, String rawSurface, TourType tour) {
        TennisSurface surface = TennisSurface.fromString(rawSurface);
        TennisPointByPointEvent p1Event = getStats(player1, tour);
        TennisPointByPointEvent p2Event = getStats(player2, tour);

        TennisSurfaceStats p1Surface = p1Event.getSurfaceStats() != null ? p1Event.getSurfaceStats().get(surface) : null;
        TennisSurfaceStats p2Surface = p2Event.getSurfaceStats() != null ? p2Event.getSurfaceStats().get(surface) : null;

        double edge = tennisSport.calculateSurfaceEdge(surface, p1Surface, p2Surface);
        double p1ServeP = tennisSport.calculateServePointWinProb(
                p1Surface != null ? p1Surface.getServeStats() : null,
                p2Surface != null ? p2Surface.getReturnStats() : null,
                surface);
        double p2ServeP = tennisSport.calculateServePointWinProb(
                p2Surface != null ? p2Surface.getServeStats() : null,
                p1Surface != null ? p1Surface.getReturnStats() : null,
                surface);

        double p1HoldProb = tennisSport.calculateGameHoldProb(p1ServeP);
        double p2HoldProb = tennisSport.calculateGameHoldProb(p2ServeP);

        return Map.of(
                "surface", surface.name(),
                "player1", p1Event.getPlayerName(),
                "player2", p2Event.getPlayerName(),
                "p1HoldProbability", Math.round(p1HoldProb * 1000.0) / 10.0,
                "p2HoldProbability", Math.round(p2HoldProb * 1000.0) / 10.0,
                "surfaceEdge", Math.round(edge * 1000.0) / 10.0,
                "favoredPlayer", edge >= 0 ? p1Event.getPlayerName() : p2Event.getPlayerName()
        );
    }

    private TiebreakStats aggregateTiebreakStats(Map<TennisSurface, TennisSurfaceStats> stats) {
        int played = 0;
        int won = 0;
        int decidingPlayed = 0;
        int decidingWon = 0;

        for (TennisSurfaceStats s : stats.values()) {
            if (s.getTiebreakStats() != null) {
                TiebreakStats tb = s.getTiebreakStats();
                if (tb.getTiebreaksPlayed() != null) played += tb.getTiebreaksPlayed();
                if (tb.getTiebreaksWon() != null) won += tb.getTiebreaksWon();
                if (tb.getDecidingSetTiebreaksPlayed() != null) decidingPlayed += tb.getDecidingSetTiebreaksPlayed();
                if (tb.getDecidingSetTiebreaksWon() != null) decidingWon += tb.getDecidingSetTiebreaksWon();
            }
        }

        double winPct = played > 0 ? (won * 100.0) / played : 50.0;
        return TiebreakStats.builder()
                .tiebreaksPlayed(played)
                .tiebreaksWon(won)
                .tiebreakWinPct(Math.round(winPct * 10.0) / 10.0)
                .decidingSetTiebreaksPlayed(decidingPlayed)
                .decidingSetTiebreaksWon(decidingWon)
                .build();
    }
}
