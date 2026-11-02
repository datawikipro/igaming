package pro.datawiki.igaming.analyst.ml.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MlSportTargetsEvaluatorTest {

    private final MlSportTargetsEvaluator evaluator = new MlSportTargetsEvaluator(new ObjectMapper());

    @Test
    void testBasketballTargets() {
        String json = """
        {
          "event": {
            "homeScore": {"current": 93, "period1": 18, "period2": 23, "period3": 21, "period4": 31},
            "awayScore": {"current": 97, "period1": 28, "period2": 20, "period3": 18, "period4": 31}
          }
        }
        """;

        MlSportTargetsEvaluator.TargetEvaluationResult res = evaluator.evaluateTargets("basketball", json, 93, 97, 160.5);
        assertNotNull(res);
        assertEquals(14, res.numTargets());

        float[] targets = res.targets();
        // 0: winner_p1 (away won 97 > 93) -> 0.0
        assertEquals(0.0f, targets[0]);
        // 1: spread_minus_5_5 (-4 < 6) -> 0.0
        assertEquals(0.0f, targets[1]);
        // 2: spread_plus_5_5 (-4 >= -5) -> 1.0
        assertEquals(1.0f, targets[2]);
        // 4: total > 160.5 (93 + 97 = 190 > 160.5) -> 1.0
        assertEquals(1.0f, targets[4]);
        // 11: clutch <= 5 (abs(93-97) = 4 <= 5) -> 1.0
        assertEquals(1.0f, targets[11]);
    }

    @Test
    void testBasketballRejectUnder70Points() {
        MlSportTargetsEvaluator.TargetEvaluationResult res = evaluator.evaluateTargets("basketball", "{}", 20, 15, 160.5);
        assertNull(res, "Scores under 70 points in basketball must be rejected as invalid");
    }

    @Test
    void testIceHockeyTargets() {
        String json = """
        {
          "event": {
            "homeScore": {"current": 4, "normaltime": 3, "period1": 1},
            "awayScore": {"current": 3, "normaltime": 3, "period1": 0}
          }
        }
        """;

        MlSportTargetsEvaluator.TargetEvaluationResult res = evaluator.evaluateTargets("ice-hockey", json, 4, 3, 5.5);
        assertNotNull(res);
        assertEquals(14, res.numTargets());

        float[] targets = res.targets();
        // 0: reg win p1 (3 == 3) -> 0.0
        assertEquals(0.0f, targets[0]);
        // 1: reg draw px (3 == 3) -> 1.0
        assertEquals(1.0f, targets[1]);
        // 3: ot p1 (4 > 3) -> 1.0
        assertEquals(1.0f, targets[3]);
        // 6: total > 5.5 (4 + 3 = 7 > 5.5) -> 1.0
        assertEquals(1.0f, targets[6]);
    }
}
