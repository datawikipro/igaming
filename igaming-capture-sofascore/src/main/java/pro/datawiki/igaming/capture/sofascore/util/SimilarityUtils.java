package pro.datawiki.igaming.capture.sofascore.util;

import lombok.extern.slf4j.Slf4j;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Slf4j
public class SimilarityUtils {

    private static final Pattern NON_ALPHA_CYRILLIC = Pattern.compile("[^a-zа-я0-9]");
    private static final double JARO_WINKLER_THRESHOLD = 0.85;

    /**
     * Normalizes a team name for fuzzy similarity matching:
     * - Converts to lowercase and trims
     * - Replaces "ё" with "е"
     * - Normalizes accents and strips diacritics
     * - Removes common sports prefixes like "фк ", "хк ", "бк ", "вк " (both Russian and English)
     * - Removes non-alphanumeric characters
     */
    public static String normalizeTeamName(String name) {
        if (name == null) {
            return "";
        }
        
        String norm = name.toLowerCase().trim();
        
        // Map ё to е for Russian names consistency
        norm = norm.replace('ё', 'е');
        
        // Remove common sport prefixes/suffixes
        norm = norm.replaceAll("^(фк|хк|бк|вк|мфк|fk|hk|bk|vk|club|fc|hc|bc|vc)\\s+", "");
        norm = norm.replaceAll("\\s+(фк|хк|бк|вк|мфк|fk|hk|bk|vk|club|fc|hc|bc|vc)$", "");
        
        // Normalize accents
        norm = Normalizer.normalize(norm, Normalizer.Form.NFD);
        norm = norm.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        
        // Remove non-alphanumeric characters
        norm = NON_ALPHA_CYRILLIC.matcher(norm).replaceAll("");
        
        return norm;
    }

    /**
     * Checks whether two team names are similar using Jaro-Winkler similarity.
     */
    public static boolean areTeamsSimilar(String name1, String name2) {
        String norm1 = normalizeTeamName(name1);
        String norm2 = normalizeTeamName(name2);

        if (norm1.isEmpty() || norm2.isEmpty()) {
            return false;
        }

        // Exact match after normalization
        if (norm1.equals(norm2)) {
            return true;
        }

        // Substring matching for obvious containment (e.g. "Реал Мадрид" vs "Реал")
        if ((norm1.length() > 3 && norm2.contains(norm1)) || (norm2.length() > 3 && norm1.contains(norm2))) {
            return true;
        }

        double score = jaroWinklerSimilarity(norm1, norm2);
        boolean isSimilar = score >= JARO_WINKLER_THRESHOLD;
        
        if (isSimilar) {
            log.debug("Fuzzy matched: '{}' and '{}' with score: {}", name1, name2, score);
        }
        
        return isSimilar;
    }

    /**
     * Computes the Jaro-Winkler similarity between two strings.
     * Returns a score between 0.0 (no similarity) and 1.0 (exact match).
     */
    public static double jaroWinklerSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        if (s1.equals(s2)) {
            return 1.0;
        }

        int s1Len = s1.length();
        int s2Len = s2.length();

        // Match range: max(len(s1), len(s2)) / 2 - 1
        int matchRange = Math.max(s1Len, s2Len) / 2 - 1;
        if (matchRange < 0) {
            matchRange = 0;
        }

        boolean[] s1Matches = new boolean[s1Len];
        boolean[] s2Matches = new boolean[s2Len];

        int matches = 0;
        for (int i = 0; i < s1Len; i++) {
            int start = Math.max(0, i - matchRange);
            int end = Math.min(s2Len - 1, i + matchRange);

            for (int j = start; j <= end; j++) {
                if (s2Matches[j]) {
                    continue;
                }
                if (s1.charAt(i) == s2.charAt(j)) {
                    s1Matches[i] = true;
                    s2Matches[j] = true;
                    matches++;
                    break;
                }
            }
        }

        if (matches == 0) {
            return 0.0;
        }

        // Count transpositions
        int transpositions = 0;
        int k = 0;
        for (int i = 0; i < s1Len; i++) {
            if (!s1Matches[i]) {
                continue;
            }
            while (!s2Matches[k]) {
                k++;
            }
            if (s1.charAt(i) != s2.charAt(k)) {
                transpositions++;
            }
            k++;
        }

        double jaro = (((double) matches / s1Len) +
                       ((double) matches / s2Len) +
                       (((double) matches - (transpositions / 2.0)) / matches)) / 3.0;

        // Winkler modification for common prefix up to 4 chars
        int prefixLen = 0;
        for (int i = 0; i < Math.min(4, Math.min(s1Len, s2Len)); i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                prefixLen++;
            } else {
                break;
            }
        }

        double p = 0.1; // scaling factor
        return jaro + prefixLen * p * (1.0 - jaro);
    }
}
