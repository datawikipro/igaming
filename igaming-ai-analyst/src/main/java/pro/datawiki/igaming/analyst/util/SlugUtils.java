package pro.datawiki.igaming.analyst.util;

import pro.datawiki.igaming.dto.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Production-ready Slug & URL Generator strictly mirroring smartbet.guru frontend routing
 * (src/lib/slug.ts and src/app/[locale]/matches/[slug]/page.tsx).
 * Produces canonical SEO slugs and routes for publishing AI recommendations to web, SMM, and bots.
 */
public final class SlugUtils {

    private static final Map<Character, String> CYRILLIC_TO_LATIN = new HashMap<>();
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Pattern SURROUNDING_DASHES = Pattern.compile("^-+|-+$");

    static {
        CYRILLIC_TO_LATIN.put('а', "a");
        CYRILLIC_TO_LATIN.put('б', "b");
        CYRILLIC_TO_LATIN.put('в', "v");
        CYRILLIC_TO_LATIN.put('г', "g");
        CYRILLIC_TO_LATIN.put('д', "d");
        CYRILLIC_TO_LATIN.put('е', "e");
        CYRILLIC_TO_LATIN.put('ё', "yo");
        CYRILLIC_TO_LATIN.put('ж', "zh");
        CYRILLIC_TO_LATIN.put('з', "z");
        CYRILLIC_TO_LATIN.put('и', "i");
        CYRILLIC_TO_LATIN.put('й', "y");
        CYRILLIC_TO_LATIN.put('к', "k");
        CYRILLIC_TO_LATIN.put('л', "l");
        CYRILLIC_TO_LATIN.put('м', "m");
        CYRILLIC_TO_LATIN.put('н', "n");
        CYRILLIC_TO_LATIN.put('о', "o");
        CYRILLIC_TO_LATIN.put('п', "p");
        CYRILLIC_TO_LATIN.put('р', "r");
        CYRILLIC_TO_LATIN.put('с', "s");
        CYRILLIC_TO_LATIN.put('т', "t");
        CYRILLIC_TO_LATIN.put('у', "u");
        CYRILLIC_TO_LATIN.put('ф', "f");
        CYRILLIC_TO_LATIN.put('х', "kh");
        CYRILLIC_TO_LATIN.put('ц', "ts");
        CYRILLIC_TO_LATIN.put('ч', "ch");
        CYRILLIC_TO_LATIN.put('ш', "sh");
        CYRILLIC_TO_LATIN.put('щ', "shch");
        CYRILLIC_TO_LATIN.put('ъ', "");
        CYRILLIC_TO_LATIN.put('ы', "y");
        CYRILLIC_TO_LATIN.put('ь', "");
        CYRILLIC_TO_LATIN.put('э', "e");
        CYRILLIC_TO_LATIN.put('ю', "yu");
        CYRILLIC_TO_LATIN.put('я', "ya");
    }

    private SlugUtils() {
    }

    public static String transliterate(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length() * 2);
        for (char c : text.toLowerCase().toCharArray()) {
            String replacement = CYRILLIC_TO_LATIN.get(c);
            if (replacement != null) {
                sb.append(replacement);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String slugify(String text, String fallback) {
        if (text == null || text.isBlank()) {
            return fallback != null ? fallback : "";
        }
        String transliterated = transliterate(text);
        String cleaned = NON_ALPHANUMERIC.matcher(transliterated).replaceAll("-");
        String trimmed = SURROUNDING_DASHES.matcher(cleaned).replaceAll("");
        return trimmed.isBlank() ? (fallback != null ? fallback : "") : trimmed;
    }

    public static String buildMatchSlug(Long matchId, String team1, String team2) {
        String t1 = slugify(team1, "team1");
        String t2 = slugify(team2, "team2");
        return (matchId != null ? matchId : "0") + "-" + t1 + "-vs-" + t2;
    }

    public static String buildMatchUrl(Long matchId, String team1, String team2, Language lang) {
        Language l = (lang != null) ? lang : Language.DEFAULT;
        return "/" + l.getCode() + "/matches/" + buildMatchSlug(matchId, team1, team2);
    }

    public static String buildMatchUrl(Long matchId, String team1, String team2) {
        return buildMatchUrl(matchId, team1, team2, Language.DEFAULT);
    }

    public static String buildMatchUrl(Long matchId, String team1, String team2, String locale) {
        return buildMatchUrl(matchId, team1, team2, Language.fromCode(locale));
    }
}
