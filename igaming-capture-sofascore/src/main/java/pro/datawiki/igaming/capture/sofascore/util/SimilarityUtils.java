package pro.datawiki.igaming.capture.sofascore.util;

/**
 * Similarity utility adapter for SofaScore capture service.
 * Delegates directly to the canonical implementation in igaming-dto
 * ensuring unified signature normalization and gender/age parity.
 */
public class SimilarityUtils {

    public static String normalizeTeamName(String name) {
        return pro.datawiki.igaming.dto.util.SimilarityUtils.normalizeTeamName(name);
    }

    public static String normalizeToLatin(String name) {
        return pro.datawiki.igaming.dto.util.SimilarityUtils.normalizeToLatin(name);
    }

    public static String transliterateCyrillic(String input) {
        return pro.datawiki.igaming.dto.util.SimilarityUtils.transliterateCyrillic(input);
    }

    public static boolean areTeamsSimilar(String name1, String name2) {
        return pro.datawiki.igaming.dto.util.SimilarityUtils.areTeamsSimilar(name1, name2);
    }

    public static boolean areTeamsSimilar(String name1, String name2, String context2) {
        return pro.datawiki.igaming.dto.util.SimilarityUtils.areTeamsSimilar(name1, name2, context2);
    }

    public static boolean areTeamsSimilar(String name1, String name2, String context1, String context2) {
        return pro.datawiki.igaming.dto.util.SimilarityUtils.areTeamsSimilar(name1, name2, context1, context2);
    }

    public static double jaroWinklerSimilarity(String s1, String s2) {
        return pro.datawiki.igaming.dto.util.SimilarityUtils.jaroWinklerSimilarity(s1, s2);
    }
}
