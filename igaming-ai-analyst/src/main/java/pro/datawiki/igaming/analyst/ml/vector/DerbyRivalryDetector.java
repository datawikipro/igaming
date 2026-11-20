package pro.datawiki.igaming.analyst.ml.vector;

import java.util.Locale;

/**
 * Domain-specific Derby Detector.
 * Determines whether a matchup represents an intense local derby or rivalry
 * using explicit tournament and match metadata (zero hardcoded club string heuristics).
 */
public final class DerbyRivalryDetector {

    private DerbyRivalryDetector() {}

    private static final String[] DERBY_KEYWORDS = {
            "DERBY", "ДЕРБИ", "CLASICO", "CLÁSICO", "КЛАСИКО", "SUPERCLASICO", "RIVALRY"
    };

    /**
     * Determines whether the given matchup constitutes a derby from tournament and match context.
     */
    public static boolean isDerbyMatch(String tournamentName, String matchTitle) {
        String ctx = ((tournamentName != null ? tournamentName : "") + " " +
                      (matchTitle != null ? matchTitle : "")).toUpperCase(Locale.ROOT);

        for (String kw : DERBY_KEYWORDS) {
            if (ctx.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compatibility helper matching four context parameters.
     */
    public static boolean isDerbyMatch(String team1, String team2, String tournament, String country) {
        String combined = (team1 != null ? team1 : "") + " " +
                          (team2 != null ? team2 : "") + " " +
                          (tournament != null ? tournament : "") + " " +
                          (country != null ? country : "");
        return isDerbyMatch(tournament, combined);
    }
}
