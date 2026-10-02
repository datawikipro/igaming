package pro.datawiki.igaming.capture.sofascore.util;

import java.util.Set;

/**
 * Utility class for detecting individual (one-on-one) sports where match participants are
 * individual players (not teams). Used to determine whether to use player face avatar URLs
 * (e.g., https://api.sofascore.app/api/v1/player/{id}/image) vs team logo URLs.
 *
 * <p>Sports covered: Tennis, Table Tennis, Badminton, Squash, Padel, Boxing, MMA,
 * Snooker, Darts, Golf, Billiards, Sumo.</p>
 */
public final class IndividualSportDetector {

    /** Sports where participants in SofaScore events are individual players, not teams. */
    private static final Set<String> INDIVIDUAL_SPORTS = Set.of(
            "TENNIS",
            "TABLE_TENNIS",
            "BADMINTON",
            "SQUASH",
            "PADEL",
            "BOXING",
            "MMA",
            "SNOOKER",
            "DARTS",
            "GOLF",
            "BILLIARDS",
            "SUMO"
    );

    private IndividualSportDetector() {}

    /**
     * Returns {@code true} when the given sport name corresponds to an individual (solo)
     * discipline where face/portrait avatars should be fetched instead of team logos.
     *
     * @param sportName sport name as used by the aggregator (case-insensitive, e.g. "TENNIS")
     * @return {@code true} if the sport is individual
     */
    public static boolean isIndividualSport(String sportName) {
        if (sportName == null || sportName.isBlank()) return false;
        return INDIVIDUAL_SPORTS.contains(sportName.toUpperCase());
    }

    /**
     * Builds the SofaScore player face avatar URL for the given player ID.
     *
     * @param playerId external SofaScore player ID
     * @return fully-qualified avatar image URL
     */
    public static String buildPlayerAvatarUrl(String playerId) {
        return "https://api.sofascore.app/api/v1/player/" + playerId + "/image";
    }

    /**
     * Builds the SofaScore team logo URL for the given team ID.
     *
     * @param teamId external SofaScore team ID
     * @return fully-qualified team logo URL
     */
    public static String buildTeamLogoUrl(String teamId) {
        return "https://api.sofascore.app/api/v1/team/" + teamId + "/image";
    }
}
