package pro.datawiki.igaming.analyst.service.tier;

/**
 * Declares the competition class / tier scope targeted by a strategy.
 * Strictly adheres to Rule 8 (Zero Discriminator Branching).
 */
public enum TournamentTierScope {
    PRO_SENIOR,
    YOUTH_AMATEUR,
    ALL_INCLUSIVE;

    public static TournamentTierScope fromString(String val) {
        if (val == null || val.isBlank()) {
            return PRO_SENIOR;
        }
        try {
            return TournamentTierScope.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return PRO_SENIOR;
        }
    }
}
