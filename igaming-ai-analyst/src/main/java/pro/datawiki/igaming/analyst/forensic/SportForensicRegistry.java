package pro.datawiki.igaming.analyst.forensic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.Sport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Flyweight polymorphic registry of sport-specific forensic auditors.
 * Follows Rule 9: Zero procedural switch/if branching across sports.
 */
@Slf4j
@Component
public class SportForensicRegistry {

    private final Map<String, SportForensicAuditor<?>> auditorsByCode = new ConcurrentHashMap<>();
    private final SportForensicAuditor<?> defaultAuditor;

    public SportForensicRegistry(List<SportForensicAuditor<?>> auditors, DefaultForensicAuditor fallback) {
        this.defaultAuditor = fallback;
        for (SportForensicAuditor<?> auditor : auditors) {
            String code = auditor.getSport().getCode().toUpperCase();
            String slug = auditor.getSport().getSlug().toLowerCase();
            auditorsByCode.put(code, auditor);
            auditorsByCode.put(slug, auditor);
            log.info("[Forensic Registry] Registered sport forensic auditor for: {} ({})", code, auditor.getClass().getSimpleName());
        }
    }

    /**
     * Resolves the polymorphic forensic auditor for the given sport.
     */
    public SportForensicAuditor<?> getAuditor(Sport sport) {
        if (sport == null) return defaultAuditor;
        SportForensicAuditor<?> a = auditorsByCode.get(sport.getCode().toUpperCase());
        if (a == null) {
            a = auditorsByCode.get(sport.getSlug().toLowerCase());
        }
        return a != null ? a : defaultAuditor;
    }

    /**
     * Resolves the polymorphic forensic auditor by sport name or slug string.
     */
    public SportForensicAuditor<?> getAuditor(String sportNameOrSlug) {
        if (sportNameOrSlug == null || sportNameOrSlug.isBlank()) return defaultAuditor;
        String clean = sportNameOrSlug.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        SportForensicAuditor<?> a = auditorsByCode.get(clean);
        if (a == null) {
            a = auditorsByCode.get(sportNameOrSlug.trim().toLowerCase().replace("_", "-"));
        }
        return a != null ? a : defaultAuditor;
    }
}
