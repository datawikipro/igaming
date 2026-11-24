package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.repository.clickhouse.TableTennisAnalyticsRepository;
import pro.datawiki.igaming.dto.ml.tensor.PurePhysicsFeatureBlock;
import pro.datawiki.igaming.dto.sport.racket.TableTennisSport;

/**
 * Tactical vector extractor for Table Tennis.
 * Queries table_tennis_match_fact directly from ClickHouse DDS (Zero Owls, Zero Sines).
 * Models set-by-set distribution, Elo rating differences, same-day match density,
 * and high-pressure clutch momentum.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
public class TableTennisVectorExtractor extends AbstractSportVectorExtractor<TableTennisSport> {

    private final ClickHouseDdsClient clickHouse;
    private final TableTennisAnalyticsRepository ttRepository;
    private final TableTennisSport sport = new TableTennisSport();

    @Autowired
    public TableTennisVectorExtractor(ClickHouseDdsClient clickHouse, TableTennisAnalyticsRepository ttRepository) {
        this.clickHouse = clickHouse;
        this.ttRepository = ttRepository;
    }

    public TableTennisVectorExtractor(ClickHouseDdsClient clickHouse) {
        this(clickHouse, null);
    }

    @Override
    public TableTennisSport getSport() {
        return sport;
    }

    @Override
    public float[] extractFormVector(TacticalVectorContext ctx) {
        float[] vec = new float[64];

        // ── Dims 0..7: Guaranteed Macro Performance Vector (Parity with Python) ──
        float[] macro0to7 = computeStandardMacroDims0To7(ctx, sport.hasDrawOutcome());
        System.arraycopy(macro0to7, 0, vec, 0, 8);

        // ── Dims 8..63: Baseline Physical & Biological Priors ────────────────────
        populateBaselinePriors(vec);

        // ── Real ClickHouse DDS Facts from table_tennis_match_fact ────────────
        Long pid = (ctx.getTeamSofascoreId() != null && ctx.getTeamSofascoreId() > 0) ? ctx.getTeamSofascoreId() : ctx.getTeamId();
        Long oppPid = (ctx.getOpponentSofascoreId() != null && ctx.getOpponentSofascoreId() > 0) ? ctx.getOpponentSofascoreId() : ctx.getOpponentId();

        // ── Dynamic Universal Sports MMR (Multi-Tier Glicko-2) Resolution ─────
        java.util.Optional<ClickHouseDdsClient.TennisMmr> playerMmrOpt = clickHouse.getSportMmr(pid, "table-tennis", "ALL");
        java.util.Optional<ClickHouseDdsClient.TennisMmr> oppMmrOpt = clickHouse.getSportMmr(oppPid, "table-tennis", "ALL");

        if (playerMmrOpt.isPresent()) {
            ClickHouseDdsClient.TennisMmr pMmr = playerMmrOpt.get();
            double mmr = pMmr.mmr();
            vec[30] = (float) Math.max(0.0, Math.min(1.0, (mmr - 800.0) / 1400.0));
            vec[31] = (float) Math.max(0.0, Math.min(1.0, (pMmr.streak() + 5.0) / 10.0));

            if (oppMmrOpt.isPresent()) {
                ClickHouseDdsClient.TennisMmr oMmr = oppMmrOpt.get();
                double oppMmr = oMmr.mmr();
                double mmrDiff = mmr - oppMmr;
                double phiOpp = oMmr.rd() / 173.7178;
                double gPhiOpp = 1.0 / Math.sqrt(1.0 + (3.0 * phiOpp * phiOpp) / (Math.PI * Math.PI));
                double expectedWinProb = 1.0 / (1.0 + Math.exp(-gPhiOpp * (mmrDiff / 173.7178)));
                double pairConfidence = 1.0 / (1.0 + (pMmr.rd() + oMmr.rd()) / 300.0);

                vec[16] = (float) Math.tanh(mmrDiff / 400.0);
                vec[17] = (float) expectedWinProb;
                vec[18] = (float) pairConfidence;
                vec[19] = (float) ((mmr - 1500.0) / 500.0);
            }
        }

        if (pid != null && pid > 0 && ttRepository != null) {
            ttRepository.findStatsByPlayerId(pid).ifPresent(fact -> {
                if (fact.matches() > 0) {
                    double elo = fact.avgElo();
                    double streak = fact.streak();
                    double rawMargin = fact.margin();

                    if (playerMmrOpt.isEmpty()) {
                        vec[30] = (float) Math.max(0.0, Math.min(1.0, (elo - 1000.0) / 1200.0));
                        vec[31] = (float) Math.max(0.0, Math.min(1.0, (streak + 5.0) / 10.0));
                    }
                    vec[32] = (float) (1.0 / (1.0 + Math.exp(-0.25 * rawMargin)));
                    vec[33] = (float) Math.max(0.10, Math.min(0.90, 0.50 + Math.tanh(streak / 5.0) * 0.25));

                    vec[14] = (float) fact.sweepRate();
                    vec[10] = (float) fact.decRate();
                }
            });
        }

        // ── Dims 34..38: Real Same-Day Match Density & Physical Fatigue Vector ──
        int matchesToday = 0;
        int setsToday = 0;
        int winsToday = 0;
        double fatigueIndex = 0.0;
        double minRestHours = 24.0;

        if (ctx.getRecentMatches() != null && !ctx.getRecentMatches().isEmpty()) {
            long curTs = ctx.getMatchTimestamp();
            java.time.LocalDate curDate = java.time.Instant.ofEpochMilli(curTs)
                    .atZone(java.time.ZoneOffset.UTC).toLocalDate();

            for (java.util.Map<String, Object> m : ctx.getRecentMatches()) {
                Object tsObj = m.get("match_timestamp") != null ? m.get("match_timestamp") : m.get("timestamp");
                if (tsObj instanceof Number num) {
                    long mTs = num.longValue();
                    if (mTs < curTs) {
                        java.time.LocalDate mDate = java.time.Instant.ofEpochMilli(mTs)
                                .atZone(java.time.ZoneOffset.UTC).toLocalDate();
                        if (curDate.equals(mDate)) {
                            matchesToday++;
                            int s1 = m.get("score1") instanceof Number n ? n.intValue() : 0;
                            int s2 = m.get("score2") instanceof Number n ? n.intValue() : 0;
                            int matchSets = s1 + s2;
                            setsToday += matchSets;
                            if (s1 > s2) winsToday++;
                            double deltaHours = Math.max(0.0, (curTs - mTs) / 3600000.0);
                            fatigueIndex += matchSets * Math.exp(-0.15 * deltaHours);
                            if (deltaHours < minRestHours) minRestHours = deltaHours;
                        }
                    }
                }
            }
        }

        // Fallback to repository fact if recent matches list was empty in context
        if (matchesToday == 0 && ttRepository != null && pid != null && pid > 0) {
            java.util.Optional<pro.datawiki.igaming.analyst.repository.clickhouse.dto.TableTennisFactDto> factOpt =
                    ttRepository.findStatsByPlayerId(pid);
            if (factOpt.isPresent()) {
                matchesToday = (int) Math.round(factOpt.get().dailyM());
                winsToday = (int) Math.round(factOpt.get().dailyWins());
                setsToday = matchesToday * 4;
                fatigueIndex = matchesToday * 3.5;
            }
        }

        vec[34] = (float) Math.min(1.0, matchesToday / 8.0);
        vec[35] = (float) Math.min(1.0, fatigueIndex / 10.0);
        vec[36] = (float) (1.0 / (1.0 + minRestHours));
        vec[37] = matchesToday > 0 ? (float) ((double) winsToday / matchesToday) : 0.5f;
        vec[38] = (float) Math.min(1.0, setsToday / 30.0);

        // ── Dims 45..63: Table Tennis Dynamic Physical & Class Features (Zero Constants!) ──
        vec[45] = vec[30]; // Peak Elo/Class index
        vec[46] = vec[30]; // Current Elo/Class index
        vec[47] = (float) Math.max(0.0, 0.50 - vec[33]); // Deflation / form dip
        vec[48] = vec[34]; // Matches today density
        vec[49] = vec[35]; // Cumulative fatigue index
        vec[50] = vec[36]; // Min rest hours proxy
        vec[51] = vec[37]; // Same-day win rate

        vec[52] = ctx.getEliminationPressureIndex();
        vec[53] = 0.0f; // No draws in table tennis
        vec[54] = ctx.getRegionalCardBias();
        vec[55] = vec[38]; // Sets played today
        vec[56] = (float) (1.0 - Math.exp(-0.25 * matchesToday));
        vec[57] = vec[35] > 0.5f ? (vec[35] - 0.5f) : 0.0f; // Late day fatigue penalty
        vec[58] = (float) Math.max(0.20, 1.0 - vec[57]); // Physical stamina

        vec[59] = vec[10]; // Deciding set rate / clutch
        vec[60] = vec[14]; // Clean sweep rate
        vec[61] = vec[33]; // Momentum streak
        vec[62] = vec[17]; // Expected win prob
        vec[63] = macro0to7[0]; // Macro form win rate

        return l2Normalize(vec);
    }

    @Override
    public float[] extractMacroContext(float[] v1, float[] v2) {
        // Table Tennis duel: Home advantage does NOT exist.
        // Dimension is completely removed (Clean-Cut Policy / Rule 1.4).
        return new float[]{
                v1.length > 0 && v2.length > 0 ? (v1[0] - v2[0]) : 0.0f,
                v1.length > 5 && v2.length > 5 ? (v1[5] - v2[5]) : 0.0f
        };
    }

    @Override
    public float[] extractMicroFeatures(TacticalMatchContext matchCtx) {
        float[] lf = new float[64];
        float[] v1 = matchCtx.v1();
        float[] v2 = matchCtx.v2();

        lf[0] = 1.0f;
        lf[1] = v1.length > 45 ? v1[45] : 0.50f;
        lf[2] = v1.length > 58 ? v1[58] : 0.50f;
        lf[3] = v1.length > 33 ? v1[33] : 0.50f;
        lf[4] = v1.length > 35 ? v1[35] : 0.0f;
        lf[5] = v1.length > 34 ? v1[34] : 0.0f;
        lf[6] = v1.length > 36 ? v1[36] : 0.50f;
        lf[7] = v1.length > 10 ? v1[10] : 0.22f;
        lf[8] = v1.length > 14 ? v1[14] : 0.30f;
        lf[9] = v1.length > 37 ? v1[37] : 0.50f;
        lf[10] = v1.length > 38 ? v1[38] : 0.0f;
        lf[11] = v1.length > 31 ? v1[31] : 0.50f;

        lf[12] = 1.0f;
        lf[13] = v2.length > 45 ? v2[45] : 0.50f;
        lf[14] = v2.length > 58 ? v2[58] : 0.50f;
        lf[15] = v2.length > 33 ? v2[33] : 0.50f;
        lf[16] = v2.length > 35 ? v2[35] : 0.0f;
        lf[17] = v2.length > 34 ? v2[34] : 0.0f;
        lf[18] = v2.length > 36 ? v2[36] : 0.50f;
        lf[19] = v2.length > 10 ? v2[10] : 0.22f;
        lf[20] = v2.length > 14 ? v2[14] : 0.30f;
        lf[21] = v2.length > 37 ? v2[37] : 0.50f;
        lf[22] = v2.length > 38 ? v2[38] : 0.0f;
        lf[23] = v2.length > 31 ? v2[31] : 0.50f;

        lf[24] = lf[1] - lf[13];
        lf[25] = lf[3] - lf[15];
        lf[26] = lf[4] - lf[16];
        lf[27] = lf[2] - lf[14];
        lf[28] = lf[6] - lf[18];
        lf[29] = lf[8] - lf[20];
        lf[30] = lf[7] - lf[19];
        lf[31] = lf[9] - lf[21];

        for (int i = 0; i < 32; i++) {
            lf[32 + i] = (i < 8) ? (lf[i] - lf[12 + i]) : 0.0f;
        }

        return lf;
    }

    @Override
    public float[] extractPurePhysicsVector(TacticalMatchContext matchCtx, float[] lineup) {
        float[] p64 = new float[PurePhysicsFeatureBlock.EXPECTED_DIMENSION];
        float[] v1 = matchCtx.v1();
        float[] v2 = matchCtx.v2();

        p64[0]  = v1.length > 19 ? v1[19] : 0.0f;
        p64[1]  = v1.length > 18 ? v1[18] : 0.50f;
        p64[2]  = v1.length > 45 ? v1[45] : 0.50f;
        p64[3]  = v1.length > 31 ? v1[31] : 0.50f;
        p64[4]  = v1.length > 32 ? v1[32] : 0.50f;
        p64[5]  = v1.length > 63 ? v1[63] : 0.50f;
        p64[6]  = v1.length > 14 ? v1[14] : 0.30f;
        p64[7]  = v1.length > 10 ? v1[10] : 0.22f;
        p64[8]  = v1.length > 33 ? v1[33] : 0.50f;
        p64[9]  = v1.length > 34 ? v1[34] : 0.0f;
        p64[10] = v1.length > 35 ? v1[35] : 0.0f;
        p64[11] = v1.length > 36 ? v1[36] : 0.50f;
        p64[12] = v1.length > 37 ? v1[37] : 0.50f;
        p64[13] = v1.length > 38 ? v1[38] : 0.0f;
        p64[14] = v1.length > 58 ? v1[58] : 0.80f;
        p64[15] = v1.length > 57 ? v1[57] : 0.0f;
        p64[16] = v1.length > 16 ? v1[16] : 0.0f;
        p64[17] = v1.length > 17 ? v1[17] : 0.50f;
        p64[18] = v1.length > 0 ? v1[0] : 0.50f;
        p64[19] = v1.length > 1 ? v1[1] : 0.0f;
        p64[20] = v1.length > 2 ? v1[2] : 0.50f;
        p64[21] = v1.length > 3 ? v1[3] : 0.15f;
        p64[22] = v1.length > 4 ? v1[4] : 0.15f;
        p64[23] = v1.length > 5 ? v1[5] : 0.0f;

        p64[24] = v2.length > 19 ? v2[19] : 0.0f;
        p64[25] = v2.length > 18 ? v2[18] : 0.50f;
        p64[26] = v2.length > 45 ? v2[45] : 0.50f;
        p64[27] = v2.length > 31 ? v2[31] : 0.50f;
        p64[28] = v2.length > 32 ? v2[32] : 0.50f;
        p64[29] = v2.length > 63 ? v2[63] : 0.50f;
        p64[30] = v2.length > 14 ? v2[14] : 0.30f;
        p64[31] = v2.length > 10 ? v2[10] : 0.22f;
        p64[32] = v2.length > 33 ? v2[33] : 0.50f;
        p64[33] = v2.length > 34 ? v2[34] : 0.0f;
        p64[34] = v2.length > 35 ? v2[35] : 0.0f;
        p64[35] = v2.length > 36 ? v2[36] : 0.50f;
        p64[36] = v2.length > 37 ? v2[37] : 0.50f;
        p64[37] = v2.length > 38 ? v2[38] : 0.0f;
        p64[38] = v2.length > 58 ? v2[58] : 0.80f;
        p64[39] = v2.length > 57 ? v2[57] : 0.0f;
        p64[40] = v2.length > 16 ? v2[16] : 0.0f;
        p64[41] = v2.length > 17 ? v2[17] : 0.50f;
        p64[42] = v2.length > 0 ? v2[0] : 0.50f;
        p64[43] = v2.length > 1 ? v2[1] : 0.0f;
        p64[44] = v2.length > 2 ? v2[2] : 0.50f;
        p64[45] = v2.length > 3 ? v2[3] : 0.15f;
        p64[46] = v2.length > 4 ? v2[4] : 0.15f;
        p64[47] = v2.length > 5 ? v2[5] : 0.0f;

        p64[48] = p64[0] - p64[24];
        p64[49] = 1.0f / (1.0f + (float) Math.exp(-4.0 * (p64[0] - p64[24])));
        p64[50] = (p64[1] + p64[25]) * 0.5f;
        p64[51] = p64[2] - p64[26];
        p64[52] = p64[3] - p64[27];
        p64[53] = p64[4] - p64[28];
        p64[54] = p64[6] - p64[30];
        p64[55] = p64[7] - p64[31];
        p64[56] = p64[9] - p64[33];
        p64[57] = p64[10] - p64[34];
        p64[58] = p64[11] - p64[35];
        p64[59] = p64[12] - p64[36];
        p64[60] = p64[14] - p64[38];
        p64[61] = p64[5] - p64[29];
        p64[62] = (matchCtx.ctx1().getPaceMultiplier() != null) ? matchCtx.ctx1().getPaceMultiplier().floatValue() : 1.0f;
        p64[63] = (float) (matchCtx.t1Leverage() - matchCtx.t2Leverage());

        return p64;
    }

    @Override
    protected void populateBaselinePriors(float[] vec) {
        // Table Tennis Game State & Dynamics
        vec[8] = 0.50f; vec[9] = 0.38f; vec[10] = 0.22f; vec[11] = 0.22f;
        vec[12] = 0.22f; vec[13] = 0.22f; vec[14] = 0.30f; vec[15] = 0.30f;
        vec[16] = 0.0f; vec[17] = 0.50f;
        float[] bins = {0.25f, 0.25f, 0.25f, 0.25f, 0.0f, 0.0f};
        for (int i = 0; i < 6; i++) {
            vec[18 + i] = bins[i];
            vec[24 + i] = bins[i];
        }
        // Baseline Table Tennis Dynamics (Zero Football Goalkeeper/Defender Artifacts)
        vec[30] = 0.50f; vec[31] = 0.50f; vec[32] = 0.50f; vec[33] = 0.50f;
        vec[34] = 0.00f; vec[35] = 0.00f; vec[36] = 0.50f; vec[37] = 0.50f;
        vec[38] = 0.00f; vec[39] = 0.50f; vec[40] = 0.70f; vec[41] = 0.50f;
        vec[42] = 0.50f; vec[43] = 0.50f; vec[44] = 0.50f; vec[45] = 0.50f;
        vec[46] = 0.50f; vec[47] = 0.00f;
        vec[48] = 0.00f; vec[49] = 0.00f; vec[50] = 0.50f; vec[51] = 0.50f;
        vec[52] = 0.00f; vec[53] = 0.00f; vec[54] = 0.50f; vec[55] = 0.00f;
        vec[56] = 0.00f; vec[57] = 0.00f; vec[58] = 0.80f; vec[59] = 0.22f;
        vec[60] = 0.30f; vec[61] = 0.50f; vec[62] = 0.50f; vec[63] = 0.50f;
    }
}
