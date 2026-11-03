package pro.datawiki.igaming.playerfaces.harvester;

import pro.datawiki.igaming.playerfaces.domain.TennisSurface;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.TennisSurfaceStats;

import java.util.Map;
import java.util.Optional;

/**
 * Harvester interface for deep tennis statistical sources (Tennis Abstract, Tennis Explorer).
 */
public interface TennisStatsHarvester {

    /**
     * Data provider name identifier (e.g. "TENNIS_ABSTRACT", "TENNIS_EXPLORER").
     */
    String getProviderName();

    /**
     * Harvests surface-segregated point-by-point stats (Hard, Clay, Grass, Indoor) for a player.
     *
     * @param playerName raw or display player name
     * @param tour ATP or WTA
     * @return Optional map of surface to TennisSurfaceStats
     */
    Optional<Map<TennisSurface, TennisSurfaceStats>> harvestSurfaceStats(String playerName, TourType tour);

    /**
     * Priority order for harvester execution (lower values executed first).
     */
    default int getOrder() {
        return 100;
    }
}
