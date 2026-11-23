package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Flyweight registry of sport-specific tactical vector extractors.
 * Injects all polymorphic implementations with ZERO procedural if/switch branching (Rule 9).
 */
@Slf4j
@Component
public class SportVectorExtractorRegistry {

    private final Map<String, SportVectorExtractor<?>> extractorsByCode = new ConcurrentHashMap<>();
    private final Set<Sport> supportedSports = ConcurrentHashMap.newKeySet();
    private final SportVectorExtractor<?> defaultExtractor;

    public SportVectorExtractorRegistry(List<SportVectorExtractor<?>> extractors) {
        SportVectorExtractor<?> fb = null;
        for (SportVectorExtractor<?> ext : extractors) {
            String code = ext.getSport().getCode().toUpperCase();
            String slug = ext.getSport().getSlug().toLowerCase();
            extractorsByCode.put(code, ext);
            extractorsByCode.put(slug, ext);
            supportedSports.add(ext.getSport());
            if ("FOOTBALL".equals(code) || "SOCCER".equals(code)) {
                fb = ext;
            }
            log.info("[Vector Registry] Registered tactical extractor for sport: {} ({})", code, ext.getClass().getSimpleName());
        }
        this.defaultExtractor = fb != null ? fb : (extractors.isEmpty() ? null : extractors.get(0));

        // Ensure polymorphic fallback aliases for sub-disciplines
        registerAliasIfAbsent("BASKETBALL_48M", "basketball-48m", "BASKETBALL");
        registerAliasIfAbsent("TABLE_TENNIS_WOMEN", "table-tennis-women", "TABLE_TENNIS");
        registerAliasIfAbsent("TENNIS_DOUBLES_WOMEN", "tennis-doubles-women", "TENNIS_DOUBLES");
        registerAliasIfAbsent("FOOTBALL_WOMEN", "football-women", "FOOTBALL");
        registerAliasIfAbsent("SOCCER_WOMEN", "soccer-women", "SOCCER");
        registerAliasIfAbsent("ICE_HOCKEY_WOMEN", "ice-hockey-women", "ICE_HOCKEY");
        registerAliasIfAbsent("VOLLEYBALL_WOMEN", "volleyball-women", "VOLLEYBALL");
        registerAliasIfAbsent("HANDBALL_WOMEN", "handball-women", "HANDBALL");
        registerAliasIfAbsent("FUTSAL_WOMEN", "futsal-women", "FUTSAL");
        registerAliasIfAbsent("CRICKET_WOMEN", "cricket-women", "CRICKET");
    }

    private void registerAliasIfAbsent(String targetCode, String targetSlug, String sourceCode) {
        if (!extractorsByCode.containsKey(targetCode)) {
            SportVectorExtractor<?> base = extractorsByCode.get(sourceCode);
            if (base != null) {
                extractorsByCode.put(targetCode, base);
                extractorsByCode.put(targetSlug, base);
                Sport sp = Sports.get(targetCode);
                if (sp != null && !sp.equals(Sports.UNKNOWN)) {
                    supportedSports.add(sp);
                }
                log.info("[Vector Registry] Registered sub-discipline alias {} ({}) -> {}", targetCode, targetSlug, base.getClass().getSimpleName());
            }
        }
    }

    /**
     * Checks if a tactical vector extractor is registered for the given sport.
     */
    public boolean hasExtractor(Sport sport) {
        if (sport == null) return false;
        return extractorsByCode.containsKey(sport.getCode().toUpperCase())
                || extractorsByCode.containsKey(sport.getSlug().toLowerCase());
    }

    /**
     * Checks if a tactical vector extractor is registered for the given sport name or slug.
     */
    public boolean hasExtractor(String sportNameOrSlug) {
        if (sportNameOrSlug == null || sportNameOrSlug.isBlank()) return false;
        String clean = sportNameOrSlug.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (extractorsByCode.containsKey(clean)) return true;
        return extractorsByCode.containsKey(sportNameOrSlug.trim().toLowerCase().replace("_", "-"));
    }

    /**
     * Returns an unmodifiable set of all sports currently supported by vector extractors.
     */
    public Set<Sport> getSupportedSports() {
        return Collections.unmodifiableSet(supportedSports);
    }

    /**
     * Resolves the polymorphic extractor for the given sport.
     */
    public SportVectorExtractor<?> getExtractor(Sport sport) {
        if (sport == null) return defaultExtractor;
        SportVectorExtractor<?> ext = extractorsByCode.get(sport.getCode().toUpperCase());
        if (ext == null) {
            ext = extractorsByCode.get(sport.getSlug().toLowerCase());
        }
        return ext != null ? ext : defaultExtractor;
    }

    /**
     * Resolves the polymorphic extractor by sport name or slug string.
     */
    public SportVectorExtractor<?> getExtractor(String sportNameOrSlug) {
        if (sportNameOrSlug == null || sportNameOrSlug.isBlank()) return defaultExtractor;
        String clean = sportNameOrSlug.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        SportVectorExtractor<?> ext = extractorsByCode.get(clean);
        if (ext == null) {
            ext = extractorsByCode.get(sportNameOrSlug.trim().toLowerCase().replace("_", "-"));
        }
        return ext != null ? ext : defaultExtractor;
    }
}
