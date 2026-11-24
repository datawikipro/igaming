package pro.datawiki.igaming.analyst.ml.vector;

import pro.datawiki.igaming.analyst.rating.TournamentTierClassifier;
import pro.datawiki.igaming.dto.ml.tensor.TacticalDnaFeatureBlock;
import pro.datawiki.igaming.dto.sport.Sport;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates the complete match-level contextual state required for polymorphic
 * micro feature extraction and pure physics tensor construction.
 * Eliminates procedural if/switch branching in TacticalTensorBuilder (Constitution Rule 8).
 */
public record TacticalMatchContext(
        Sport sport,
        TacticalVectorContext ctx1,
        TacticalVectorContext ctx2,
        float[] v1,
        float[] v2,
        List<Map<String, Object>> starters1,
        List<Map<String, Object>> starters2,
        List<Map<String, Object>> bench1,
        List<Map<String, Object>> bench2,
        PlayerLineAggressionEvaluator.LineAggressionProfile p1Agg,
        PlayerLineAggressionEvaluator.LineAggressionProfile p2Agg,
        PlayerLineAggressionEvaluator.MatchupFrictionDynamics friction,
        SquadChemistryEvaluator.SquadChemistryAndFatigue chem1,
        SquadChemistryEvaluator.SquadChemistryAndFatigue chem2,
        TacticalDnaFeatureBlock dnaBlock,
        float compositeCardBias,
        boolean isDerby,
        double t1Leverage,
        double t2Leverage,
        TournamentTierClassifier tierClassifier
) {
}
