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
    private final Float eliminationPressureIndex;
    private final Float drawViabilityFlag;
    private final Float regionalCardBias;

    // Point-in-Time Tournament Motivation & Stakes (Dims 55..58)
    private final Float blowoutGradient;
    private final Float winLeverage;
    private final Float eliminationHazard;
    private final Float prizeWageRatio;
    private final Float groupEntropy;
    private final Boolean isGroupOfDeath;

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
}
