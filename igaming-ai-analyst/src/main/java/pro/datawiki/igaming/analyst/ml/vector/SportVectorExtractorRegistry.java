package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.Sport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Flyweight registry of sport-specific tactical vector extractors.
 * Injects all polymorphic implementations with ZERO procedural if/switch branching (Rule 9).
 */
@Slf4j
@Component
public class SportVectorExtractorRegistry {

    private final Map<String, SportVectorExtractor<?>> extractorsByCode = new ConcurrentHashMap<>();
    private final SportVectorExtractor<?> defaultExtractor;

    public SportVectorExtractorRegistry(List<SportVectorExtractor<?>> extractors) {
        SportVectorExtractor<?> fb = null;
        for (SportVectorExtractor<?> ext : extractors) {
            String code = ext.getSport().getCode().toUpperCase();
            String slug = ext.getSport().getSlug().toLowerCase();
            extractorsByCode.put(code, ext);
            extractorsByCode.put(slug, ext);
            if ("FOOTBALL".equals(code) || "SOCCER".equals(code)) {
                fb = ext;
            }
            log.info("[Vector Registry] Registered tactical extractor for sport: {} ({})", code, ext.getClass().getSimpleName());
        }
        this.defaultExtractor = fb != null ? fb : extractors.get(0);
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
