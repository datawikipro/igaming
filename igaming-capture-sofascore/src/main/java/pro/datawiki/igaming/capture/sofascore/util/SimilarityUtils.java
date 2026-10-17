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
        norm = norm.replaceAll("^(фк|хк|бк|вк|мфк|fk|hk|khk|bk|vk|club|fc|hc|bc|vc)\\s+", "");
        norm = norm.replaceAll("\\s+(фк|хк|бк|вк|мфк|fk|hk|khk|bk|vk|club|fc|hc|bc|vc)$", "");
        
        // Normalize accents
        norm = Normalizer.normalize(norm, Normalizer.Form.NFD);
        norm = norm.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        
        // Remove non-alphanumeric characters
        norm = NON_ALPHA_CYRILLIC.matcher(norm).replaceAll("");
        
        return norm;
    }

    /**
     * Normalizes a team name to Latin characters for cross-language matching:
     * - Strips sports prefixes/suffixes and cleans diacritics
     * - Transliterates Cyrillic to Latin phonetically
     * - Applies common sports club and city aliases
     */
    public static String normalizeToLatin(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String norm = normalizeTeamName(name);
        String translit = transliterateCyrillic(norm);
        return normalizeTeamName(translit);
    }

    /**
     * Fast deterministic Cyrillic -> Latin transliteration for sports team and club names.
     */
    public static String transliterateCyrillic(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String s = input.toLowerCase()
                .replace('ё', 'е')
                .replace('э', 'е')
                .replace("ъ", "")
                .replace("ь", "")
                .replace("тс", "ц")
                .replace("дж", "j")
                .replace("th", "t")
                .replace("ph", "f")
                .replace("цска", "cska")
                .replace("ска", "ska")
                .replace("псж", "psg")
                .replace("москва", "moscow")
                .replace("санкт-петербург", "stpetersburg")
                .replace("санктпетербург", "stpetersburg");

        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 'а' -> sb.append('a');
                case 'б' -> sb.append('b');
                case 'в' -> sb.append('v');
                case 'г' -> sb.append('g');
                case 'д' -> sb.append('d');
                case 'е' -> sb.append('e');
                case 'ж' -> sb.append("zh");
                case 'з' -> sb.append('z');
                case 'и' -> sb.append('i');
                case 'й' -> sb.append('y');
                case 'к' -> sb.append('k');
                case 'л' -> sb.append('l');
                case 'м' -> sb.append('m');
                case 'н' -> sb.append('n');
                case 'о' -> sb.append('o');
                case 'п' -> sb.append('p');
                case 'р' -> sb.append('r');
                case 'с' -> sb.append('s');
                case 'т' -> sb.append('t');
                case 'у' -> sb.append('u');
                case 'ф' -> sb.append('f');
                case 'х' -> sb.append("kh");
                case 'ц' -> sb.append("ts");
                case 'ч' -> sb.append("ch");
                case 'ш' -> sb.append("sh");
                case 'щ' -> sb.append("shch");
                case 'ы' -> sb.append('y');
                case 'ю' -> sb.append("yu");
                case 'я' -> sb.append("ya");
                default -> sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Checks whether two team names are similar using Jaro-Winkler similarity
     * with cross-language (Cyrillic <-> Latin) transliteration support.
     */
    public static boolean areTeamsSimilar(String name1, String name2) {
        String norm1 = normalizeTeamName(name1);
        String norm2 = normalizeTeamName(name2);

        if (norm1.isEmpty() || norm2.isEmpty()) {
            return false;
        }

        // 1. Direct matching (both Cyrillic or both Latin)
        if (norm1.equals(norm2)) {
            return true;
        }

        // Substring matching for obvious containment (e.g. "Реал Мадрид" vs "Реал")
        if ((norm1.length() > 3 && norm2.contains(norm1)) || (norm2.length() > 3 && norm1.contains(norm2))) {
            return true;
        }

        double score = jaroWinklerSimilarity(norm1, norm2);
        if (score >= JARO_WINKLER_THRESHOLD) {
            log.debug("Fuzzy matched: '{}' and '{}' with score: {}", name1, name2, score);
            return true;
        }

        // 2. Cross-language transliterated matching (e.g. "Динамо Махачкала" vs "Dinamo Makhachkala")
        String lat1 = normalizeToLatin(name1);
        String lat2 = normalizeToLatin(name2);

        if (!lat1.isEmpty() && !lat2.isEmpty()) {
            if (lat1.equals(lat2)) {
                log.debug("Cross-language exact matched: '{}' ({}) and '{}' ({})", name1, lat1, name2, lat2);
                return true;
            }

            if ((lat1.length() > 3 && lat2.contains(lat1)) || (lat2.length() > 3 && lat1.contains(lat2))) {
                log.debug("Cross-language substring matched: '{}' ({}) and '{}' ({})", name1, lat1, name2, lat2);
                return true;
            }

            double latScore = jaroWinklerSimilarity(lat1, lat2);
            if (latScore >= JARO_WINKLER_THRESHOLD) {
                log.debug("Cross-language fuzzy matched: '{}' ({}) and '{}' ({}) with score: {}", name1, lat1, name2, lat2, latScore);
                return true;
            }
        }

        return false;
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
