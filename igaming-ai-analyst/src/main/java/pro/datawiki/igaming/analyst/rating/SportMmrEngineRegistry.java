package pro.datawiki.igaming.analyst.rating;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Flyweight registry of sport-specific MMR rating engines.
 * Dispatches to specialized subclasses (e.g. TableTennisGlickoEngine) via pure polymorphism
 * with ZERO procedural if/switch branching (Constitutional Mandate Rule 1).
 */
@Slf4j
@Component
public class SportMmrEngineRegistry {

    private final Map<String, MultiTierGlickoEngine> enginesBySlug = new ConcurrentHashMap<>();
    private final MultiTierGlickoEngine defaultEngine;

    public SportMmrEngineRegistry(List<MultiTierGlickoEngine> engines) {
        MultiTierGlickoEngine def = null;
        for (MultiTierGlickoEngine engine : engines) {
            if (engine.getSport() == null) {
                def = engine;
            } else {
                String code = engine.getSport().getCode().toUpperCase(Locale.ROOT);
                String slug = engine.getSport().getSlug().toLowerCase(Locale.ROOT);
                enginesBySlug.put(code, engine);
                enginesBySlug.put(slug, engine);
                log.info("[MMR Registry] Registered polymorphic rating engine for sport: {} ({})", code, engine.getClass().getSimpleName());
            }
        }
        this.defaultEngine = def != null ? def : engines.get(0);

        // Sub-discipline aliases
        registerAliasIfAbsent("TABLE_TENNIS_WOMEN", "table-tennis-women", "TABLE_TENNIS");
    }

    private void registerAliasIfAbsent(String targetCode, String targetSlug, String sourceCode) {
        if (!enginesBySlug.containsKey(targetCode)) {
            MultiTierGlickoEngine base = enginesBySlug.get(sourceCode);
            if (base != null) {
                enginesBySlug.put(targetCode, base);
                enginesBySlug.put(targetSlug, base);
                log.info("[MMR Registry] Registered sub-discipline MMR alias {} ({}) -> {}", targetCode, targetSlug, base.getClass().getSimpleName());
            }
        }
    }

    /**
     * Resolves the specialized rating engine for the given sport.
     * Guaranteed polymorphic resolution with fallback to universal baseline engine.
     */
    public MultiTierGlickoEngine getEngine(String sportSlug) {
        if (sportSlug == null || sportSlug.isBlank()) {
            return defaultEngine;
        }
        String clean = sportSlug.toLowerCase(Locale.ROOT).replace('_', '-');
        return enginesBySlug.getOrDefault(clean, defaultEngine);
    }
}
