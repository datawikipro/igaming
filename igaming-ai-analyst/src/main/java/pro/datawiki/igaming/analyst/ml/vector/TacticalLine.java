package pro.datawiki.igaming.analyst.ml.vector;

import lombok.Getter;
import pro.datawiki.igaming.dto.classification.ClassificationErrorPublisher;

/**
 * Encapsulates the tactical outfield / goalkeeping line for bottom-up player discipline
 * and aggression evaluation ("Ирландец в Осло").
 */
@Getter
public enum TacticalLine {

    DEFENSE("D", 0.42f, 0.18f),
    MIDFIELD("M", 0.47f, 0.25f),
    FORWARD("F", 0.37f, 0.28f),
    GOALKEEPER("G", 0.06f, 0.04f),
    UNKNOWN("U", 0.35f, 0.20f);

    private final String code;
    private final float defaultAggression;
    private final float defaultFoulsDrawn;

    TacticalLine(String code, float defaultAggression, float defaultFoulsDrawn) {
        this.code = code;
        this.defaultAggression = defaultAggression;
        this.defaultFoulsDrawn = defaultFoulsDrawn;
    }

    /**
     * Resolves tactical line from player position string.
     */
    public static TacticalLine fromPosition(String pos) {
        if (pos == null || pos.isBlank()) {
            ClassificationErrorPublisher.report("TacticalLine", "TacticalLine", pos, "blank position");
            return UNKNOWN;
        }
        String p = pos.toUpperCase();
        if (p.startsWith("D") || p.contains("CB") || p.contains("LB") || p.contains("RB") || p.contains("DEF") || p.contains("ЗАЩ")) {
            return DEFENSE;
        }
        if (p.startsWith("F") || p.contains("CF") || p.contains("ST") || p.contains("RW") || p.contains("LW") || p.contains("ATT") || p.contains("НАП")) {
            return FORWARD;
        }
        if (p.startsWith("G") || p.contains("GK") || p.contains("GOAL") || p.contains("ВР")) {
            return GOALKEEPER;
        }
        if (p.startsWith("M") || p.contains("CM") || p.contains("LM") || p.contains("RM") || p.contains("DM") || p.contains("AM") || p.contains("MID") || p.contains("ПЗ") || p.contains("ХАВ")) {
            return MIDFIELD;
        }
        ClassificationErrorPublisher.report("TacticalLine", "TacticalLine", pos, "unrecognized position token");
        return UNKNOWN;
    }
}
