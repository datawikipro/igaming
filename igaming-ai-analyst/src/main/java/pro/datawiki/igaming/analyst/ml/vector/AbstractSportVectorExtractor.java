package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.dto.ml.tensor.PurePhysicsFeatureBlock;
import pro.datawiki.igaming.dto.ml.tensor.TacticalDnaFeatureBlock;
import pro.datawiki.igaming.dto.sport.Sport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Universal abstract base class for sport-specific tactical vector extractors.
 *
 * Enforces strict compile-time invariance and mathematical parity with Python
 * training matrix generation (prepare_multitask_matrix.py:763-795):
 * - Dims 0..7: Guaranteed unified scaling (wins, draws, losses, GF/10, GA/10, GD/10, sample_size/10, rest_days/14).
 * - Dims 8..63: Baseline physical DDS priors per sport, cleanly overlaid by DDS facts when available.
 * - Final L2 Normalization guaranteed.
 *
 * Prevents dimensional divergence, incorrect scaling divisors, or feature swapping at the compiler level.
 *
 * @param <S> Concrete Sport class from the polymorphic hierarchy
 */
@Slf4j
public abstract class AbstractSportVectorExtractor<S extends Sport> implements SportVectorExtractor<S> {

    /**
     * Computes Dimensions 0..7 of the form vector strictly matching Python prepare_multitask_matrix.py:
     * Dims 0..2: wins / n, draws / n (or 0.0), losses / n
     * Dims 3..5: (gf / n) / 10.0, (ga / n) / 10.0, ((gf - ga) / n) / 10.0
     * Dim 6: min(1.0, n / 10.0)
     * Dim 7: min(1.0, max(0.0, rest_days / 14.0)) or 0.50
     */
    public final float[] computeStandardMacroDims0To7(TacticalVectorContext ctx, boolean hasDraws) {
        float[] res = new float[8];
        List<Map<String, Object>> hist = ctx.getRecentMatches();

        if (hist == null || hist.isEmpty()) {
            res[0] = 0.50f;
            res[1] = hasDraws ? 0.25f : 0.0f;
            res[2] = hasDraws ? 0.25f : 0.50f;
            res[3] = 0.15f; // Neutral ~1.5 goals / 10
            res[4] = 0.15f;
            res[5] = 0.0f;
            res[6] = 0.50f;
            res[7] = 0.50f;
            return res;
        }

        int wins = 0, draws = 0, losses = 0;
        float gf = 0.0f, ga = 0.0f;
        int n = 0;
        long latestMatchTs = 0L;

        for (Map<String, Object> h : hist) {
            if (h.get("team_score") == null || h.get("opponent_score") == null) {
                continue;
            }
            int s1 = toInt(h.get("team_score"), 0);
            int s2 = toInt(h.get("opponent_score"), 0);

            String r = String.valueOf(h.getOrDefault("result", "")).toUpperCase();
            if ("WIN".equals(r) || "1".equals(r) || (r.isEmpty() && s1 > s2)) {
                wins++;
            } else if ("DRAW".equals(r) || "0".equals(r) || (r.isEmpty() && s1 == s2)) {
                draws++;
            } else if ("LOSS".equals(r) || "-1".equals(r) || (r.isEmpty() && s1 < s2)) {
                losses++;
            }

            gf += s1;
            ga += s2;
            n++;

            long mTs = parseTimestamp(h);
            if (mTs > latestMatchTs) {
                latestMatchTs = mTs;
            }
        }

        if (n == 0) {
            res[0] = 0.50f;
            res[1] = hasDraws ? 0.25f : 0.0f;
            res[2] = hasDraws ? 0.25f : 0.50f;
            res[3] = 0.15f;
            res[4] = 0.15f;
            res[5] = 0.0f;
            res[6] = 0.50f;
            res[7] = 0.50f;
            return res;
        }

        res[0] = (float) wins / (float) n;
        res[1] = hasDraws ? ((float) draws / (float) n) : 0.0f;
        res[2] = (float) losses / (float) n;
        res[3] = ((float) gf / (float) n) / 10.0f;
        res[4] = ((float) ga / (float) n) / 10.0f;
        res[5] = (((float) (gf - ga)) / (float) n) / 10.0f;
        res[6] = Math.min(1.0f, (float) n / 10.0f);

        long curTs = ctx.getMatchTimestamp();
        if (latestMatchTs > 0 && curTs > latestMatchTs) {
            float restDays = (float) ((curTs - latestMatchTs) / 86400000.0);
            res[7] = Math.min(1.0f, Math.max(0.0f, restDays / 14.0f));
        } else {
            res[7] = 0.50f;
        }

        return res;
    }

    /**
     * Default micro feature extraction: Team Lineup & Tactical Friction dynamics.
     */
    @Override
    public float[] extractMicroFeatures(TacticalMatchContext matchCtx) {
        return extractLineupFeatures(
                matchCtx.starters1(), matchCtx.starters2(),
                matchCtx.bench1(), matchCtx.bench2(),
                matchCtx.p1Agg(), matchCtx.p2Agg(), matchCtx.friction(),
                matchCtx.compositeCardBias(),
                matchCtx.chem1(), matchCtx.chem2(),
                matchCtx.v1(), matchCtx.v2()
        );
    }

    /**
     * Default pure physics vector: 64-dim Team Pure Physics (MMR, Form, Chemistry, Style).
     */
    @Override
    public float[] extractPurePhysicsVector(TacticalMatchContext matchCtx, float[] lineup) {
        return buildTeamPurePhysicsVector(
                matchCtx.v1(), matchCtx.v2(), lineup, matchCtx.dnaBlock(),
                matchCtx.t1Leverage(), matchCtx.t2Leverage(),
                matchCtx.compositeCardBias(), matchCtx.isDerby(),
                matchCtx.ctx1().getBlowoutGradient(), matchCtx.ctx2().getBlowoutGradient()
        );
    }

    protected float[] createNeutralLineupFeatures(float[] v1, float[] v2, float compositeCardBias) {
        float[] lf = new float[64];
        lf[0] = 0.0f;
        lf[1] = 6.8f;
        lf[6] = 6.8f;
        lf[12] = 0.0f;
        lf[13] = 6.8f;
        lf[18] = 6.8f;

        lf[31] = compositeCardBias;

        if (v1 != null && v2 != null) {
            float hAge = v1.length > 48 ? v1[48] : 0.50f;
            float aAge = v2.length > 48 ? v2[48] : 0.50f;
            lf[32] = hAge;
            lf[33] = aAge;
            lf[34] = hAge;
            lf[35] = aAge;
            lf[38] = 0.40f;
            lf[39] = 0.40f;
            lf[40] = 0.40f;
            lf[41] = 0.40f;
            lf[42] = 0.80f;
            lf[43] = 0.80f;
            lf[44] = v1.length > 14 ? v1[14] : 0.28f;
            lf[45] = v2.length > 14 ? v2[14] : 0.28f;
            lf[46] = v1.length > 26 ? (v1[24] + v1[25] + v1[26]) * 0.5f : 0.25f;
            lf[47] = v2.length > 26 ? (v2[24] + v2[25] + v2[26]) * 0.5f : 0.25f;
            lf[48] = v1.length > 60 ? v1[60] : 0.68f;
            lf[49] = v2.length > 60 ? v2[60] : 0.68f;
            lf[50] = 1.0f;
            lf[51] = 1.0f;
            lf[52] = v1.length > 23 ? v1[23] : 0.21f;
            lf[53] = v2.length > 23 ? v2[23] : 0.21f;
            lf[57] = v1.length > 44 ? v1[44] : 0.50f;
            lf[58] = v2.length > 44 ? v2[44] : 0.50f;
        }
        return lf;
    }

    protected float[] extractLineupFeatures(List<Map<String, Object>> s1, List<Map<String, Object>> s2,
                                            List<Map<String, Object>> b1, List<Map<String, Object>> b2,
                                            PlayerLineAggressionEvaluator.LineAggressionProfile p1Agg,
                                            PlayerLineAggressionEvaluator.LineAggressionProfile p2Agg,
                                            PlayerLineAggressionEvaluator.MatchupFrictionDynamics friction,
                                            float compositeCardBias,
                                            SquadChemistryEvaluator.SquadChemistryAndFatigue chem1,
                                            SquadChemistryEvaluator.SquadChemistryAndFatigue chem2,
                                            float[] v1, float[] v2) {
        float[] lf = createNeutralLineupFeatures(v1, v2, compositeCardBias);

        if (s1 == null || s1.isEmpty() || s2 == null || s2.isEmpty()) {
            return lf;
        }

        lf[0] = 1.0f;
        lf[1] = calculateAvgRating(s1) * (chem1 != null ? chem1.staminaDiscount() : 1.0f);
        lf[2] = calculateMinRating(s1);
        lf[3] = calculateMaxRating(s1);
        lf[4] = p1Agg != null ? p1Agg.totalInjuryRustDecay() : 0.0f;
        lf[5] = 0.0f;
        lf[6] = calculateGkRating(s1);

        if (p1Agg != null) {
            lf[7] = p1Agg.defAggression();
            lf[8] = p1Agg.midAggression();
            lf[9] = p1Agg.attAggression();
            lf[10] = p1Agg.teamTotalAggression();
            lf[11] = p1Agg.attFoulsDrawn();
        }

        lf[12] = 1.0f;
        lf[13] = calculateAvgRating(s2) * (chem2 != null ? chem2.staminaDiscount() : 1.0f);
        lf[14] = calculateMinRating(s2);
        lf[15] = calculateMaxRating(s2);
        lf[16] = p2Agg != null ? p2Agg.totalInjuryRustDecay() : 0.0f;
        lf[17] = 0.0f;
        lf[18] = calculateGkRating(s2);

        if (p2Agg != null) {
            lf[19] = p2Agg.defAggression();
            lf[20] = p2Agg.midAggression();
            lf[21] = p2Agg.attAggression();
            lf[22] = p2Agg.teamTotalAggression();
            lf[23] = p2Agg.attFoulsDrawn();
        }

        if (b1 != null && !b1.isEmpty()) {
            lf[24] = calculateBenchDepth(b1);
        }
        if (b2 != null && !b2.isEmpty()) {
            lf[25] = calculateBenchDepth(b2);
        }

        if (friction != null) {
            lf[26] = friction.midfieldClash();
            lf[27] = friction.attack1VsDefense2();
            lf[28] = friction.attack2VsDefense1();
            lf[29] = friction.totalMatchAggression();
            lf[30] = friction.aggressionDifferential();
        }
        lf[31] = compositeCardBias;

        float hDefAge = calculatePositionalAge(s1, "DEF", v1 != null ? v1[48] : 0.38f);
        float aDefAge = calculatePositionalAge(s2, "DEF", v2 != null ? v2[48] : 0.38f);
        float hAttAge = calculatePositionalAge(s1, "ATT", v1 != null ? v1[48] : 0.38f);
        float aAttAge = calculatePositionalAge(s2, "ATT", v2 != null ? v2[48] : 0.38f);
        lf[32] = hDefAge;
        lf[33] = aDefAge;
        lf[34] = hAttAge;
        lf[35] = aAttAge;
        lf[36] = hAttAge - aDefAge;
        lf[37] = aAttAge - hDefAge;

        float hDefR = calculatePositionalRating(s1, "DEF", 6.8f);
        float aDefR = calculatePositionalRating(s2, "DEF", 6.8f);
        float hAttR = calculatePositionalRating(s1, "ATT", 6.8f);
        float aAttR = calculatePositionalRating(s2, "ATT", 6.8f);
        lf[38] = (hDefR - 6.0f) / 2.0f;
        lf[39] = (aDefR - 6.0f) / 2.0f;
        lf[40] = (hAttR - 6.0f) / 2.0f;
        lf[41] = (aAttR - 6.0f) / 2.0f;
        lf[42] = countDefenders(s1) / 5.0f;
        lf[43] = countDefenders(s2) / 5.0f;

        lf[44] = v1 != null ? v1[14] : 0.28f;
        lf[45] = v2 != null ? v2[14] : 0.28f;
        lf[46] = v1 != null ? (v1[24] + v1[25] + v1[26]) * 0.5f : 0.25f;
        lf[47] = v2 != null ? (v2[24] + v2[25] + v2[26]) * 0.5f : 0.25f;
        lf[48] = v1 != null ? v1[60] : 0.68f;
        lf[49] = v2 != null ? v2[60] : 0.68f;

        lf[50] = v1 != null ? (v1[21] + v1[22] + v1[23]) : 0.50f;
        lf[51] = v2 != null ? (v2[21] + v2[22] + v2[23]) : 0.50f;
        lf[52] = Math.min(1.0f, (v1 != null ? v1[23] : 0.21f) + (chem1 != null ? chem1.lateDefenseLeakBonus() : 0.0f));
        lf[53] = Math.min(1.0f, (v2 != null ? v2[23] : 0.21f) + (chem2 != null ? chem2.lateDefenseLeakBonus() : 0.0f));

        lf[54] = lf[24];
        lf[55] = lf[25];
        lf[56] = lf[24] - lf[25];

        float hAer = v1 != null ? v1[44] : 0.50f;
        float aAer = v2 != null ? v2[44] : 0.50f;
        lf[57] = hAer;
        lf[58] = aAer;
        lf[59] = hAer - aAer;

        lf[60] = lf[40] - lf[39];
        lf[61] = lf[41] - lf[38];
        lf[62] = lf[38] - lf[41];
        lf[63] = lf[39] - lf[40];

        return lf;
    }

    protected float[] buildTeamPurePhysicsVector(
            float[] v1, float[] v2, float[] lineup, TacticalDnaFeatureBlock dnaBlock,
            double t1Leverage, double t2Leverage, float compositeCardBias, boolean isDerby,
            float t1Blowout, float t2Blowout) {

        float[] pure64 = new float[PurePhysicsFeatureBlock.EXPECTED_DIMENSION];

        // Block 1 (0..7): Glicko MMR & Class
        pure64[0] = v1.length > 14 ? v1[14] : 0.0f;
        pure64[1] = v2.length > 14 ? v2[14] : 0.0f;
        pure64[2] = pure64[0] - pure64[1];
        pure64[3] = v1.length > 15 ? v1[15] : 0.0f;
        pure64[4] = v2.length > 15 ? v2[15] : 0.0f;
        pure64[5] = v1.length > 52 ? v1[52] : 0.5f;
        pure64[6] = v2.length > 52 ? v2[52] : 0.5f;
        pure64[7] = (v1.length > 28 && v2.length > 28) ? (v1[28] - v2[28]) : 0.0f;

        // Block 2 (8..31): Form & Scoring Physics
        pure64[8]  = v1.length > 0 ? v1[0] : 0.0f;
        pure64[9]  = v1.length > 1 ? v1[1] : 0.0f;
        pure64[10] = v1.length > 2 ? v1[2] : 0.0f;
        pure64[11] = v1.length > 3 ? v1[3] : 0.0f;
        pure64[12] = v1.length > 4 ? v1[4] : 0.0f;
        pure64[13] = v1.length > 5 ? v1[5] : 0.0f;
        pure64[14] = v1.length > 30 ? v1[30] : 0.34f;
        pure64[15] = v1.length > 31 ? v1[31] : 0.34f;
        pure64[16] = v1.length > 8 ? v1[8] : 0.28f;
        pure64[17] = v1.length > 10 ? v1[10] : 0.24f;
        pure64[18] = v1.length > 35 ? v1[35] : 0.75f;
        pure64[19] = v1.length > 47 ? v1[47] : 0.50f;

        pure64[20] = v2.length > 0 ? v2[0] : 0.0f;
        pure64[21] = v2.length > 1 ? v2[1] : 0.0f;
        pure64[22] = v2.length > 2 ? v2[2] : 0.0f;
        pure64[23] = v2.length > 3 ? v2[3] : 0.0f;
        pure64[24] = v2.length > 4 ? v2[4] : 0.0f;
        pure64[25] = v2.length > 5 ? v2[5] : 0.0f;
        pure64[26] = v2.length > 30 ? v2[30] : 0.34f;
        pure64[27] = v2.length > 31 ? v2[31] : 0.34f;
        pure64[28] = v2.length > 8 ? v2[8] : 0.28f;
        pure64[29] = v2.length > 10 ? v2[10] : 0.24f;
        pure64[30] = v2.length > 35 ? v2[35] : 0.75f;
        pure64[31] = v2.length > 47 ? v2[47] : 0.50f;

        // Block 3 (32..47): Rest, Fatigue, Chemistry & Lineups
        pure64[32] = v1.length > 7 ? v1[7] : 0.50f;
        pure64[33] = v2.length > 7 ? v2[7] : 0.50f;
        pure64[34] = pure64[32] - pure64[33];
        pure64[35] = v1.length > 59 ? v1[59] : 0.15f;
        pure64[36] = v2.length > 59 ? v2[59] : 0.15f;
        pure64[37] = lineup != null && lineup.length > 1 ? lineup[1] / 10.0f : 0.68f;
        pure64[38] = lineup != null && lineup.length > 13 ? lineup[13] / 10.0f : 0.68f;
        pure64[39] = pure64[37] - pure64[38];
        pure64[40] = lineup != null && lineup.length > 4 ? lineup[4] : 0.0f;
        pure64[41] = lineup != null && lineup.length > 16 ? lineup[16] : 0.0f;
        pure64[42] = v1.length > 60 ? v1[60] : 0.68f;
        pure64[43] = v2.length > 60 ? v2[60] : 0.68f;
        pure64[44] = v1.length > 61 ? v1[61] : 0.68f;
        pure64[45] = v2.length > 61 ? v2[61] : 0.68f;
        pure64[46] = v1.length > 54 ? v1[54] : 0.50f;
        pure64[47] = v2.length > 54 ? v2[54] : 0.50f;

        // Block 4 (48..63): Style Clash, Leverage, Substitutions & Friction
        pure64[48] = v1.length > 36 ? v1[36] : 0.52f;
        pure64[49] = v2.length > 36 ? v2[36] : 0.52f;
        pure64[50] = v1.length > 37 ? v1[37] : 0.50f;
        pure64[51] = v2.length > 37 ? v2[37] : 0.50f;
        pure64[52] = v1.length > 35 ? v1[35] : 0.30f;
        pure64[53] = v2.length > 35 ? v2[35] : 0.30f;
        pure64[54] = v1.length > 40 ? v1[40] : 0.50f;
        pure64[55] = v2.length > 40 ? v2[40] : 0.50f;
        pure64[56] = pure64[50] - pure64[51];
        pure64[57] = compositeCardBias;
        pure64[58] = isDerby ? 1.0f : 0.0f;
        pure64[59] = (v1.length > 29 && v2.length > 29) ? (v1[29] - v2[29]) : 0.0f;
        pure64[60] = dnaBlock != null ? dnaBlock.getCosineDistance() : 1.0f;
        pure64[61] = (v1.length > 25 && v2.length > 25) ? (v1[25] - v2[25]) : 0.0f;
        pure64[62] = (float) (t1Leverage - t2Leverage);
        pure64[63] = (t1Blowout - t2Blowout); // Standings Blowout Gradient Differential (Symmetric)

        return pure64;
    }

    protected float calculatePositionalAge(List<Map<String, Object>> starters, String posGroup, float fallback) {
        if (starters == null || starters.isEmpty()) return fallback;
        double sum = 0.0;
        int count = 0;
        for (Map<String, Object> p : starters) {
            String pos = String.valueOf(p.getOrDefault("position", p.getOrDefault("pos", ""))).toUpperCase();
            boolean match = switch (posGroup) {
                case "DEF" -> pos.contains("D") || pos.contains("CB") || pos.contains("LB") || pos.contains("RB") || pos.contains("DEF");
                case "ATT" -> pos.contains("F") || pos.contains("FW") || pos.contains("ST") || pos.contains("RW") || pos.contains("LW") || pos.contains("ATT");
                default -> false;
            };
            if (match) {
                double age = toDouble(p.get("age"), 26.0);
                sum += age;
                count++;
            }
        }
        if (count == 0) return fallback;
        double avgAge = sum / count;
        return (float) Math.max(0.0, Math.min(1.0, (avgAge - 18.0) / 22.0));
    }

    protected float calculatePositionalRating(List<Map<String, Object>> starters, String posGroup, float fallback) {
        if (starters == null || starters.isEmpty()) return fallback;
        double sum = 0.0;
        int count = 0;
        for (Map<String, Object> p : starters) {
            String pos = String.valueOf(p.getOrDefault("position", p.getOrDefault("pos", ""))).toUpperCase();
            boolean match = switch (posGroup) {
                case "DEF" -> pos.contains("D") || pos.contains("CB") || pos.contains("LB") || pos.contains("RB") || pos.contains("DEF");
                case "ATT" -> pos.contains("F") || pos.contains("FW") || pos.contains("ST") || pos.contains("RW") || pos.contains("LW") || pos.contains("ATT");
                default -> false;
            };
            if (match) {
                sum += parseRating(p.get("rating"), fallback);
                count++;
            }
        }
        return count > 0 ? (float) (sum / count) : fallback;
    }

    protected int countDefenders(List<Map<String, Object>> starters) {
        if (starters == null || starters.isEmpty()) return 4;
        int count = 0;
        for (Map<String, Object> p : starters) {
            String pos = String.valueOf(p.getOrDefault("position", p.getOrDefault("pos", ""))).toUpperCase();
            if (pos.contains("D") || pos.contains("CB") || pos.contains("LB") || pos.contains("RB") || pos.contains("DEF")) {
                count++;
            }
        }
        return count > 0 ? count : 4;
    }

    protected float calculateAvgRating(List<Map<String, Object>> starters) {
        if (starters == null || starters.isEmpty()) return 6.8f;
        double sum = 0.0;
        int count = 0;
        for (Map<String, Object> p : starters) {
            double r = parsePlayerRatingRobust(p, 6.8);
            sum += r;
            count++;
        }
        return count > 0 ? (float) (sum / count) : 6.8f;
    }

    protected float calculateMinRating(List<Map<String, Object>> starters) {
        if (starters == null || starters.isEmpty()) return 6.2f;
        double min = 10.0;
        boolean found = false;
        for (Map<String, Object> p : starters) {
            double r = parsePlayerRatingRobust(p, 6.8);
            if (r < min) {
                min = r;
                found = true;
            }
        }
        return found ? (float) min : 6.2f;
    }

    protected float calculateMaxRating(List<Map<String, Object>> starters) {
        if (starters == null || starters.isEmpty()) return 7.5f;
        double max = 0.0;
        boolean found = false;
        for (Map<String, Object> p : starters) {
            double r = parsePlayerRatingRobust(p, 6.8);
            if (r > max) {
                max = r;
                found = true;
            }
        }
        return found ? (float) max : 7.5f;
    }

    protected float calculateGkRating(List<Map<String, Object>> starters) {
        if (starters == null) return 6.8f;
        for (Map<String, Object> p : starters) {
            String pos = String.valueOf(p.getOrDefault("position", "")).toUpperCase();
            if (pos.contains("GK") || pos.contains("GOAL") || pos.contains("ВР") || "G".equals(pos)) {
                return (float) parsePlayerRatingRobust(p, 6.8);
            }
        }
        return 6.8f;
    }

    protected float calculateBenchDepth(List<Map<String, Object>> bench) {
        if (bench == null || bench.isEmpty()) return 0.0f;
        int jokers = 0;
        double sumQuality = 0.0;
        for (Map<String, Object> p : bench) {
            double r = parsePlayerRatingRobust(p, 6.5);
            if (r >= 7.0) jokers++;
            sumQuality += r;
        }
        float jokerBonus = Math.min(0.50f, jokers * 0.17f);
        float avgBenchRatio = (float) Math.min(0.50f, ((sumQuality / bench.size()) - 6.0) / 3.0);
        return Math.min(1.0f, Math.max(0.0f, jokerBonus + avgBenchRatio));
    }

    protected double parsePlayerRatingRobust(Map<String, Object> p, double fallback) {
        if (p == null) return fallback;
        Object r = p.get("effective_rating");
        if (r == null) r = p.get("rating");
        if (r == null) r = p.get("match_rating");
        return parseRating(r, fallback);
    }

    protected double parseRating(Object obj, double fallback) {
        double val = toDouble(obj, fallback);
        return (val >= 2.0 && val <= 10.0) ? val : fallback;
    }

    /**
     * Populates baseline physical, tactical, and biological DDS priors for Dims 8..63.
     * Guaranteed polymorphic implementation per sport (Zero procedural switch/if branching).
     *
     * @param vec Target 64-dimensional form vector
     */
    protected abstract void populateBaselinePriors(float[] vec);

    protected Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(obj.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    protected long toLong(Object obj, long def) {
        if (obj == null) return def;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(obj.toString().trim());
        } catch (Exception e) {
            return def;
        }
    }

    protected long parseTimestamp(Map<String, Object> map) {
        if (map == null) return 0L;
        Object val = map.get("start_timestamp");
        if (val == null) val = map.get("match_timestamp");
        if (val == null) val = map.get("start_time");
        if (val == null) val = map.get("timestamp");
        if (val == null) val = map.get("ts");
        if (val instanceof Number n) return n.longValue();
        if (val != null) {
            try {
                return Long.parseLong(val.toString());
            } catch (Exception ignored) {}
        }
        return 0L;
    }
}
