package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.repository.clickhouse.TableTennisAnalyticsRepository;
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

        return l2Normalize(vec);
    }
}
