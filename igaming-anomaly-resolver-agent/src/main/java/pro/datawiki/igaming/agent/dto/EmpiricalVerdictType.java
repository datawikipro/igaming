package pro.datawiki.igaming.agent.dto;

public enum EmpiricalVerdictType {
    /**
     * Both records represent the exact same physical sporting event from different bookmakers.
     * Remediation: Consolidate lines into winner match, cancel loser with MERGED_DUPLICATE_MDM.
     */
    DUPLICATE_MDM_MATCH,

    /**
     * The record is not a sporting match, but a tournament outright / futures market ("Winner", "Победитель").
     * Remediation: Cancel with CANCELLED_OUTRIGHT_TRASH and purge from active line.
     */
    OUTRIGHT_OR_STATS_TRASH,

    /**
     * A team exists under multiple phonetic transliterations (e.g., Хапоэль vs Hapoel).
     * Remediation: Map raw alias to canonical SofaScore team in mdm_team_ingestion_map.
     */
    UNMAPPED_TEAM_ALIAS,

    /**
     * One record is a cyber/esport/fantasy duel (e.g. FIFA 8 min, E-League) colliding with a real match.
     * Remediation: Cancel synthetic match with CANCELLED_SYNTHETIC_COLLISION.
     */
    SYNTHETIC_DUEL,

    /**
     * Legitimate schedule collision where the entity is scheduled in two real-world matches.
     * Remediation: Flag SCHEDULE_CONFLICT_CONFIRMED and notify AI Analyst.
     */
    REAL_SCHEDULE_CONFLICT,

    /**
     * Model was unable to confidently classify the incident.
     */
    INCONCLUSIVE
}
