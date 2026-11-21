package pro.datawiki.igaming.analyst.repository;

import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.dto.bookmaker.BookmakerCode;

import java.util.*;

/**
 * Domain query resolver for bookmaker filtering.
 * Encapsulates multi-lingual alias resolution and canonical slug mapping
 * without procedural if-else ladders in database specifications.
 */
@Slf4j
public final class BookmakerQueryFilter {

    private static final Map<String, List<String>> ALIAS_REGISTRY = new HashMap<>();

    static {
        // Automatically register all canonical bookmakers from BookmakerCode enum
        for (BookmakerCode bc : BookmakerCode.values()) {
            register(bc.getSlug(), bc.getDisplayName().toLowerCase(Locale.ROOT));
        }

        // Multi-lingual transliterations and legacy brand aliases
        register("melbet", "мелбет", "melbet-com", "melbet.com", "melbet-ru", "melbet.ru", "1xbet", "1x-bet");
        register("fonbet", "fon", "фонбет", "fon-bet", "fon-bet-ru", "fon-bet-by", "fon-bet-kz");
        register("winline", "винлайн");
        register("pari", "пари", "paribet");
        register("leon", "леон");
        register("betboom", "бетбум", "bingoboom");
        register("ligastavok", "liga", "лигаставок", "лига");
        register("betcity", "бетсити");
        register("tennisi", "тенниси");
        register("olimpbet", "olimp", "олимп", "олимпбет");
        register("baltbet", "балтбет");
        register("marathon", "marathonbet", "марафон");
        register("zenit", "зенит");
        register("1xbet", "1хbet", "1хставка", "1xstavka");
        register("pinnacle", "пиннакл", "пинакл");
        register("betano", "бетано");
        register("sportbet", "спортбет");
        register("betera", "бетера");
    }

    private BookmakerQueryFilter() {
        // Utility domain class
    }

    private static void register(String canonical, String... aliases) {
        Set<String> tokenSet = new LinkedHashSet<>();
        tokenSet.add(canonical.toLowerCase());
        for (String a : aliases) {
            tokenSet.add(a.toLowerCase());
        }
        List<String> tokenList = Collections.unmodifiableList(new ArrayList<>(tokenSet));

        ALIAS_REGISTRY.put(canonical.toLowerCase(), tokenList);
        ALIAS_REGISTRY.put(cleanKey(canonical), tokenList);
        for (String a : aliases) {
            ALIAS_REGISTRY.put(a.toLowerCase(), tokenList);
            ALIAS_REGISTRY.put(cleanKey(a), tokenList);
        }
    }

    private static String cleanKey(String key) {
        if (key == null) return "";
        return key.toLowerCase().replace("-", "").replace("_", "").trim();
    }

    /**
     * Resolves search tokens for a given bookmaker query.
     * If query is in the known alias registry, returns all matching multi-lingual forms.
     * If unknown, returns the cleaned query string itself for dynamic matching.
     */
    public static List<String> resolveSearchTerms(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Collections.emptyList();
        }
        String normalized = rawQuery.trim().toLowerCase();
        List<String> registered = ALIAS_REGISTRY.get(normalized);
        if (registered != null && !registered.isEmpty()) {
            return registered;
        }

        String cleaned = cleanKey(normalized);
        registered = ALIAS_REGISTRY.get(cleaned);
        if (registered != null && !registered.isEmpty()) {
            return registered;
        }

        Optional<BookmakerCode> bCode = BookmakerCode.fromSlug(normalized);
        if (bCode.isPresent()) {
            BookmakerCode bc = bCode.get();
            List<String> registeredBc = ALIAS_REGISTRY.get(bc.getSlug());
            if (registeredBc != null && !registeredBc.isEmpty()) {
                return registeredBc;
            }
        }

        // Dynamic fallback: cleanly match the queried string without hardcoded rejection
        log.debug("Bookmaker '{}' not found in alias registry; using dynamic token matching", rawQuery);
        return List.of(normalized);
    }
}
