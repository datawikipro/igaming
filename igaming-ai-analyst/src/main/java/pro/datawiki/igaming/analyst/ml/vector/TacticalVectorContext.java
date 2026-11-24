package pro.datawiki.igaming.analyst.ml.vector;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import pro.datawiki.igaming.dto.sport.racket.CourtSurface;

import java.util.List;
import java.util.Map;

/**
 * Tactical Context DTO passed to sport-specific vector extractors.
 */
@Getter
@Builder
@ToString
public class TacticalVectorContext {
    private final Long teamId;
    private final String teamName;
    private final Long teamSofascoreId;
    private final Long opponentId;
    private final String opponentName;
    private final Long opponentSofascoreId;
    private final long matchTimestamp;
    private final List<Map<String, Object>> recentMatches;
    private final List<Map<String, Object>> starters;
    private final List<Map<String, Object>> bench;
    private final String stage;
    private final Long tournamentId;
    private final String tournamentName;
    private final String countryName;
    @Builder.Default
    private final Float eliminationPressureIndex = 0.0f;
    @Builder.Default
    private final Float drawViabilityFlag = 1.0f;
    @Builder.Default
    private final Float regionalCardBias = 0.50f;

    // Point-in-Time Tournament Motivation & Stakes (Dims 55..58)
    @Builder.Default
    private final Float blowoutGradient = 0.0f;
    @Builder.Default
    private final Float winLeverage = 0.50f;
    @Builder.Default
    private final Float eliminationHazard = 0.50f;
    @Builder.Default
    private final Float prizeWageRatio = 0.05f;
    @Builder.Default
    private final Float groupEntropy = 0.50f;
    @Builder.Default
    private final Boolean isGroupOfDeath = false;

    // League / Tournament Calibration Parameters (from mdm_tournament DB)
    private final Integer regulationMinutes;
    private final Double paceMultiplier;
    private final Double expectedTotalBaseline;

    // Referee Strictness & Derby Disciplinary Physics (Cluster 2 Fix)
    private final Float refereeCardStrictness;
    private final Float refereeAvgYellowCards;
    private final String refereeStrictnessTier;
    private final Boolean isDerby;
    private final Float derbyFrictionIndex;

    // Court / Ground Surface (Tennis & Racket Sports: HARD, CLAY, GRASS, INDOORS)
    private final CourtSurface surface;

    // Squad Chemistry & National Team Travel Fatigue ("Вирус сборных")
    private final SquadChemistryEvaluator.SquadChemistryAndFatigue squadChemistry;

    public float getEliminationPressureIndex() {
        return eliminationPressureIndex != null ? eliminationPressureIndex : 0.0f;
    }

    public float getDrawViabilityFlag() {
        return drawViabilityFlag != null ? drawViabilityFlag : 1.0f;
    }

    public float getRegionalCardBias() {
        return regionalCardBias != null ? regionalCardBias : 0.50f;
    }

    public float getBlowoutGradient() {
        return blowoutGradient != null ? blowoutGradient : 0.0f;
    }

    public float getWinLeverage() {
        return winLeverage != null ? winLeverage : 0.50f;
    }

    public float getEliminationHazard() {
        return eliminationHazard != null ? eliminationHazard : 0.50f;
    }

    public float getPrizeWageRatio() {
        return prizeWageRatio != null ? prizeWageRatio : 0.05f;
    }

    public float getGroupEntropy() {
        return groupEntropy != null ? groupEntropy : 0.50f;
    }

    public boolean isGroupOfDeath() {
        return isGroupOfDeath != null && isGroupOfDeath;
    }
}
