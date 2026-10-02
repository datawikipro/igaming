package pro.datawiki.igaming.playerfaces.harvester;

import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.PlayerFaceDto;

import java.util.Optional;

/**
 * Strategy interface for harvesting player headshot portraits, metadata,
 * flags and ranking details from external sports and encyclopedic data sources.
 */
public interface PlayerHeadshotHarvester {

    /**
     * Source provider identifier.
     */
    AvatarSourceProvider getProvider();

    /**
     * Attempts to harvest player face profile and portrait URL by player name and sport.
     *
     * @param playerName raw or display player name
     * @param sport sport type (Tennis, MMA, Boxing, etc.)
     * @param tour tour organization (ATP, WTA, UFC, etc.)
     * @return Optional containing harvested PlayerFaceDto, or empty if not found
     */
    Optional<PlayerFaceDto> harvest(String playerName, SportType sport, TourType tour);

    /**
     * Priority order for cascade harvesting (lower values executed first).
     */
    default int getOrder() {
        return 100;
    }

    /**
     * Checks if this harvester source is currently available and enabled.
     */
    default boolean isAvailable() {
        return true;
    }
}
