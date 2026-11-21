package pro.datawiki.igaming.analyst.dto;

import lombok.Getter;
import pro.datawiki.igaming.dto.classification.ClassificationErrorPublisher;

/**
 * Rich Enum representing AI Analyst conviction and risk tiers.
 * Encapsulates stake sizing, Kelly criterion multipliers, and institutional limits.
 */
@Getter
public enum ConvictionTier {

    ML_90("ML_90", 5.0, 0.35, 5.0, 1000L),
    ML_80("ML_80", 4.0, 0.25, 4.0, 500L),
    ML_70("ML_70", 3.0, 0.20, 3.0, 250L),
    ML_60("ML_60", 2.0, 0.15, 2.0, 100L),
    PASS("PASS", 0.0, 0.0, 0.0, 0L),
    UNKNOWN("UNKNOWN", 0.0, 0.0, 0.0, 0L);

    private final String code;
    private final double radarStakePct;
    private final double kellyFraction;
    private final double maxKellyStakePct;
    private final long suggestedStakeRub;

    ConvictionTier(String code, double radarStakePct, double kellyFraction, double maxKellyStakePct, long suggestedStakeRub) {
        this.code = code;
        this.radarStakePct = radarStakePct;
        this.kellyFraction = kellyFraction;
        this.maxKellyStakePct = maxKellyStakePct;
        this.suggestedStakeRub = suggestedStakeRub;
    }

    public boolean isHighConviction() {
        return this == ML_90 || this == ML_80;
    }

    public boolean isTopConviction() {
        return this == ML_90;
    }

    public static ConvictionTier fromCode(String code) {
        if (code == null || code.isBlank()) {
            ClassificationErrorPublisher.report("ConvictionTier", "ConvictionTier", code, "blank code");
            return UNKNOWN;
        }
        for (ConvictionTier t : values()) {
            if (t.code.equalsIgnoreCase(code) || t.name().equalsIgnoreCase(code)) {
                return t;
            }
        }
        ClassificationErrorPublisher.report("ConvictionTier", "ConvictionTier", code, "unrecognized conviction code");
        return UNKNOWN;
    }

    /**
     * Determines tier from fair probability and market type.
     */
    public static ConvictionTier evaluateFromProb(double fairProb, boolean isDraw) {
        if (isDraw) {
            if (fairProb >= 45.0) return ML_90;
            if (fairProb >= 39.0) return ML_80;
            if (fairProb >= 34.0) return ML_70;
            if (fairProb >= 28.0) return ML_60;
            return PASS;
        } else {
            if (fairProb >= 88.0) return ML_90;
            if (fairProb >= 78.0) return ML_80;
            if (fairProb >= 68.0) return ML_70;
            if (fairProb >= 58.0) return ML_60;
            return PASS;
        }
    }
}
