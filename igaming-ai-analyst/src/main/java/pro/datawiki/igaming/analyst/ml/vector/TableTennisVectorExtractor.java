package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.racket.TableTennisSport;

import java.util.List;
import java.util.Map;

/**
 * Tactical vector extractor for Table Tennis.
 * Queries table_tennis_match_fact directly from ClickHouse DDS (Zero Owls, Zero Sines).
 * Models set-by-set distribution, Elo rating differences, same-day match density,
 * and high-pressure clutch momentum.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TableTennisVectorExtractor extends AbstractSportVectorExtractor<TableTennisSport> {

    private final ClickHouseDdsClient clickHouse;
    private final TableTennisSport sport = new TableTennisSport();

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
        populateStandardBaselinePriors(vec, sport);

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

        if (pid != null && pid > 0) {
            String sql = "SELECT count() as matches, " +
                    "avg(if(player1_id = " + pid + ", player1_live_elo, player2_live_elo)) as avg_elo, " +
                    "avg(if(player1_id = " + pid + ", player1_winstreak, player2_winstreak)) as streak, " +
                    "avg(if(player1_id = " + pid + ", points_margin, -points_margin)) as margin, " +
                    "avg(if(player1_id = " + pid + ", score1 > score2, score2 > score1)) as wr, " +
                    "countIf((player1_id = " + pid + " AND score1 = 3 AND score2 = 0) OR (player2_id = " + pid + " AND score2 = 3 AND score1 = 0)) / greatest(1, count()) as sweep_rate, " +
                    "countIf(total_sets = 5) / greatest(1, count()) as dec_rate, " +
                    "avg(same_day_h2h_matches_today) as daily_m, " +
                    "avg(if(player1_id = " + pid + ", same_day_h2h_p1_wins_today, same_day_h2h_matches_today - same_day_h2h_p1_wins_today)) as daily_wins " +
                    "FROM table_tennis_match_fact WHERE player1_id = " + pid + " OR player2_id = " + pid;

            List<Map<String, Object>> rows = clickHouse.queryJsonCached("tt:" + pid, sql);
            if (!rows.isEmpty() && toInt(rows.get(0).get("matches"), 0) > 0) {
                Map<String, Object> r = rows.get(0);
                double elo = toDouble(r.get("avg_elo"), 1500.0);
                double streak = toDouble(r.get("streak"), 0.0);
                double rawMargin = toDouble(r.get("margin"), 0.0);

                if (playerMmrOpt.isEmpty()) {
                    vec[30] = (float) Math.max(0.0, Math.min(1.0, (elo - 1000.0) / 1200.0));
                    vec[31] = (float) Math.max(0.0, Math.min(1.0, (streak + 5.0) / 10.0));
                }
                vec[32] = (float) (1.0 / (1.0 + Math.exp(-0.25 * rawMargin)));
                vec[33] = (float) Math.max(0.10, Math.min(0.90, 0.50 + Math.tanh(streak / 5.0) * 0.25));

                vec[14] = (float) toDouble(r.get("sweep_rate"), 0.30);
                vec[10] = (float) toDouble(r.get("dec_rate"), 0.22);

                double dm = toDouble(r.get("daily_m"), 0.0);
                double dw = toDouble(r.get("daily_wins"), 0.0);
                if (dm > 0) {
                    vec[34] = (float) (dw / dm);
                }
            }
        }

        return l2Normalize(vec);
    }
}
