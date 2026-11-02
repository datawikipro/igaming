package pro.datawiki.igaming.analyst.ml.vector;

import java.util.Locale;
import java.util.Set;

/**
 * Domain-specific Derby & High-Rivalry Detector.
 * Identifies high-friction derbies and heated regional rivalries that trigger emotional card surges.
 */
public final class DerbyRivalryDetector {

    private DerbyRivalryDetector() {}

    private static final Set<String> DERBY_KEYWORDS = Set.of(
            "derby", "дерби", "clasico", "clásico", "класико", "superclásico", "superclasico",
            "rivalry", "rivalen", "дублинское", "брабантское", "ютландское", "классико"
    );

    private static final String[] KNOWN_DERBY_PAIRS = {
            // Cluster 2 verified forensic cases
            "helmond", "eindhoven",
            "patrick", "bohemian",
            "trinidense", "rubio",
            "san carlos", "escorpiones",
            "skive", "aarhus",
            "miedz", "warta",
            "miedź", "warta",

            // Global Top Tier Derbies
            "celtic", "rangers",
            "roma", "lazio",
            "milan", "inter",
            "juventus", "torino",
            "real madrid", "atletico",
            "barcelona", "espanyol",
            "sevilla", "betis",
            "boca", "river",
            "flamengo", "fluminense",
            "corinthians", "palmeiras",
            "gremio", "internacional",
            "fenerbahce", "galatasaray",
            "fenerbahce", "besiktas",
            "galatasaray", "besiktas",
            "olympiacos", "panathinaikos",
            "olympiakos", "panathinaikos",
            "red star", "partizan",
            "crvena zvezda", "partizan",
            "dinamo zagreb", "hajduk",
            "benfica", "sporting",
            "benfica", "porto",
            "arsenal", "tottenham",
            "liverpool", "everton",
            "manchester united", "manchester city",
            "spartak", "cska",
            "spartak", "zenit",
            "spartak", "dynamo"
    };

    private static final String[] METROPOLITAN_CITIES = {
            "manchester", "madrid", "milano", "milan", "prague", "praha", "sofia", "athens",
            "dublin", "rotterdam", "krakow", "kraków", "lodz", "łódź", "belgrade", "istanbul",
            "sevilla", "valencia", "lisbon", "lisboa", "porto", "montevideo", "rosario",
            "avellaneda", "medellin", "cali", "guayaquil", "quito", "asuncion", "asunción",
            "san jose", "san josé", "bogota", "bogotá", "santiago", "eindhoven", "aarhus"
    };

    /**
     * Determines whether the given matchup constitutes a local derby or heated rivalry.
     */
    public static boolean isDerbyMatch(String team1, String team2, String tournament, String country) {
        String t1 = team1 != null ? team1.toLowerCase(Locale.ROOT) : "";
        String t2 = team2 != null ? team2.toLowerCase(Locale.ROOT) : "";
        String tour = tournament != null ? tournament.toLowerCase(Locale.ROOT) : "";
        String c = country != null ? country.toLowerCase(Locale.ROOT) : "";

        String combinedContext = (t1 + " " + t2 + " " + tour + " " + c);

        // 1. Explicit keyword check in tournament / match names
        for (String kw : DERBY_KEYWORDS) {
            if (combinedContext.contains(kw)) {
                return true;
            }
        }

        // 2. Specific pairwise rivalry lookup
        for (int i = 0; i < KNOWN_DERBY_PAIRS.length; i += 2) {
            String p1 = KNOWN_DERBY_PAIRS[i];
            String p2 = KNOWN_DERBY_PAIRS[i + 1];
            if ((t1.contains(p1) && t2.contains(p2)) || (t1.contains(p2) && t2.contains(p1))) {
                return true;
            }
        }

        // 3. Same Metropolitan City Rivalry
        for (String city : METROPOLITAN_CITIES) {
            if (t1.contains(city) && t2.contains(city)) {
                return true;
            }
        }

        return false;
    }
}
