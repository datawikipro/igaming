package pro.datawiki.igaming.analyst.forensic;

import pro.datawiki.igaming.analyst.forensic.dto.ForensicAuditReport;
import pro.datawiki.igaming.analyst.forensic.dto.ForensicMatchContext;
import pro.datawiki.igaming.dto.sport.Sport;

/**
 * Sacred contract for sport-specific forensic audit and post-mortem evaluation.
 * Injected polymorphically by Sport type with zero procedural switch/if branching (Rule 9).
 *
 * @param <S> Concrete Sport class from the polymorphic hierarchy
 */
public interface SportForensicAuditor<S extends Sport> {

    /**
     * The Sport instance this auditor handles.
     */
    S getSport();

    /**
     * Generates a rich, institutional forensic audit prompt for the given match context.
     * Incorporates full pre-match vector expectations and post-match ground-truth protocols.
     */
    String buildForensicPrompt(ForensicMatchContext ctx);

    /**
     * Parses the LLM's forensic post-mortem JSON response into a standardized ForensicAuditReport.
     */
    ForensicAuditReport parseAuditReport(String jsonResponse);
}
