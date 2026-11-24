package pro.datawiki.igaming.analyst.ml.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.repository.clickhouse.FootballAnalyticsRepository;
import pro.datawiki.igaming.dto.sport.football.SoccerSport;
import pro.datawiki.igaming.dto.util.SimilarityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tactical vector extractor for Football (Soccer).
 * Queries football_match_fact, football_lineup_player_fact,
 * match_gamestate_resilience_fact, and match_goal_timeline_fact.
 * Captures transition chaos, compactness, conversion fragility, and blunder rates.
 * Strictly guarantees mathematical parity with Python prepare_multitask_matrix.py.
 */
@Slf4j
@Component
public class FootballVectorExtractor extends AbstractSportVectorExtractor<SoccerSport> {

    private final ClickHouseDdsClient clickHouse;
    private final FootballAnalyticsRepository footballRepository;
    private final SoccerSport sport = new SoccerSport();

    @Autowired
    public FootballVectorExtractor(ClickHouseDdsClient clickHouse, FootballAnalyticsRepository footballRepository) {
        this.clickHouse = clickHouse;
        this.footballRepository = footballRepository;
    }

    public FootballVectorExtractor(ClickHouseDdsClient clickHouse) {
        this(clickHouse, null);
    }

    @Override
    public SoccerSport getSport() {
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

        // Evaluate Starters from Context if present (Dims 60..63)
        evaluateFootballStarters(ctx, vec);

        // ── Real ClickHouse DDS Facts (ID-Only Querying) ───────────────────────
        Long tid = ctx.getTeamId();
        Long sofaId = ctx.getTeamSofascoreId();
        Long effectiveSofaId = (sofaId != null && sofaId > 0) ? sofaId : tid;

        // ── Dynamic Universal Sports MMR (ID-Only Querying from ClickHouse) ──
        if (effectiveSofaId != null && effectiveSofaId > 0) {
            clickHouse.getSportMmr(effectiveSofaId, "football", "ALL").ifPresent(mmr -> {
                vec[14] = (float) Math.max(-1.0, Math.min(1.0, (mmr.mmr() - 1500.0) / 1000.0));
                vec[15] = (float) Math.max(0.0, Math.min(1.0, 1.0 - (mmr.rd() / 350.0)));
                vec[52] = (float) Math.max(0.0, Math.min(1.5, (mmr.peakMmr() - 1500.0) / 1000.0));
                vec[53] = (float) Math.max(-1.0, Math.min(1.0, mmr.streak() / 10.0));
            });
        }

        List<Long> targetTeamIds = new ArrayList<>();
        if (tid != null && tid > 0) targetTeamIds.add(tid);
        if (sofaId != null && sofaId > 0 && !sofaId.equals(tid)) targetTeamIds.add(sofaId);

        if (!targetTeamIds.isEmpty()) {
            if (footballRepository != null) {
                // 1. Resilience (Dims 8..17)
                footballRepository.findResilienceByTeamIds(targetTeamIds).ifPresent(r -> {
                    if (r.matches() > 0) {
                        vec[8]  = (float) r.cf();
                        vec[9]  = (float) r.eq();
                        vec[10] = (float) r.comebackW();
                        vec[11] = (float) Math.min(1.0, r.ppgLos() / 3.0);
                        vec[12] = (float) r.col();
                        vec[13] = (float) Math.min(1.0, r.blownPts() / 3.0);
                        vec[16] = (float) Math.min(1.0, r.avgDeficit() / 3.0);
                        double lf = r.lateF();
                        double la = r.lateA();
                        vec[17] = (float) (lf / Math.max(0.1, lf + la));
                    }
                });

                // 2. 15-minute Goal Distribution (Dims 18..29)
                footballRepository.findGoalDistributionByTeamIds(targetTeamIds).ifPresent(g -> {
                    vec[18] = (float) g.b1();
                    vec[19] = (float) g.b2();
                    vec[20] = (float) g.b3();
                    vec[21] = (float) g.b4();
                    vec[22] = (float) g.b5();
                    vec[23] = (float) g.b6();
                });
            }

            // 3. Tactical & Transition Metrics from football_team_advanced_v (Dims 30..47)
            if (effectiveSofaId != null && effectiveSofaId > 0) {
                String tacSql = "SELECT total_matches, avg_xg, avg_xga, avg_xg_open, avg_xg_set_piece, avg_pass_acc, " +
                        "high_event_rate, low_event_rate, avg_fouls, avg_cards, pressing_intensity_sigmoid, " +
                        "avg_def_line, avg_possession, avg_aerial_won " +
                        "FROM igaming_dds.football_team_advanced_v WHERE team_id = ?";

                List<Map<String, Object>> tacRows = clickHouse.queryJsonCached("tac_adv:" + effectiveSofaId, tacSql, effectiveSofaId);
                if (!tacRows.isEmpty() && toInt(tacRows.get(0).get("total_matches"), 0) > 0) {
                    Map<String, Object> t = tacRows.get(0);
                    double xg = toDouble(t.get("avg_xg"), 1.35);
                    double xga = toDouble(t.get("avg_xga"), 1.35);
                    double xgOpen = toDouble(t.get("avg_xg_open"), 0.90);
                    double xgSetPiece = toDouble(t.get("avg_xg_set_piece"), 0.35);
                    double passAcc = toDouble(t.get("avg_pass_acc"), 80.0);
                    double highEvRate = toDouble(t.get("high_event_rate"), 0.25);
                    double lowEvRate = toDouble(t.get("low_event_rate"), 0.30);
                    double fouls = toDouble(t.get("avg_fouls"), 11.0);
                    double cards = toDouble(t.get("avg_cards"), 2.0);

                    vec[30] = (float) Math.min(1.0, xg / 3.5);
                    vec[31] = (float) Math.min(1.0, xga / 3.5);
                    vec[32] = (float) Math.max(0.0, Math.min(1.0, (xg - xga) / 3.5 + 0.5));

                    // Dims 33..34: Open-play vs Set-piece xG distribution (Pure Observables)
                    vec[33] = (float) Math.min(1.0, xgOpen / Math.max(0.1, xg));
                    vec[34] = (float) Math.min(1.0, xgSetPiece / Math.max(0.1, xg));

                    // Dims 35..40: Direct physical match dynamics from ClickHouse DDS
                    vec[35] = (float) toDouble(t.get("pressing_intensity_sigmoid"), 0.30);
                    vec[36] = (float) (toDouble(t.get("avg_def_line"), 52.0) / 100.0);
                    vec[37] = (float) (toDouble(t.get("avg_possession"), 50.0) / 100.0);
                    vec[38] = (float) Math.min(1.0, passAcc / 100.0);
                    vec[39] = (float) Math.max(0.0, 1.0 - (passAcc / 100.0));
                    vec[40] = (float) (toDouble(t.get("avg_aerial_won"), 50.0) / 100.0);

                    // Dims 41..44: Discipline and event tempo
                    vec[41] = (float) (fouls / 25.0);
                    vec[42] = (float) (cards / 6.0);
                    vec[43] = (float) highEvRate;
                    vec[44] = (float) lowEvRate;
                }

                // 4. Squad Biological Age from football_lineup_player_fact (Dims 48..51)
                String ageSql = "SELECT avg(age_at_match) as avg_age, " +
                        "countIf(age_at_match <= 22) / greatest(1, count()) as young_pct, " +
                        "countIf(age_at_match >= 32) / greatest(1, count()) as veteran_pct, " +
                        "stddevPop(age_at_match) as age_std " +
                        "FROM football_lineup_player_fact WHERE mdm_team_id = ? AND age_at_match > 0";
                List<Map<String, Object>> ageRows = clickHouse.queryJsonCached("age:" + tid, ageSql, tid);
                if (!ageRows.isEmpty() && toDouble(ageRows.get(0).get("avg_age"), 0) > 0) {
                    Map<String, Object> a = ageRows.get(0);
                    double avgAge = toDouble(a.get("avg_age"), 26.5);
                    vec[48] = (float) Math.exp(-Math.pow(avgAge - 26.8, 2) / (2.0 * 12.25));
                    vec[49] = (float) toDouble(a.get("young_pct"), 0.25);
                    vec[50] = (float) toDouble(a.get("veteran_pct"), 0.15);
                    vec[51] = (float) (toDouble(a.get("age_std"), 3.5) / 10.0);
                }
            }
        }

        // Point-in-Time Tournament Motivation & Dynamic Stakes (Dims 52..58)
        if (ctx.getEliminationPressureIndex() > 0 && vec[52] == 0.50f) {
            vec[52] = ctx.getEliminationPressureIndex();
        }
        if (ctx.getDrawViabilityFlag() > 0 && vec[53] == 0.36f) {
            vec[53] = ctx.getDrawViabilityFlag();
        }
        if (ctx.getRegionalCardBias() > 0) {
            vec[54] = ctx.getRegionalCardBias();
        }
        vec[55] = ctx.getBlowoutGradient();
        vec[56] = ctx.getWinLeverage();
        vec[57] = ctx.getEliminationHazard();
        vec[58] = ctx.getPrizeWageRatio();

        // Dims 59: Observable national team fatigue signal (0.0 .. 1.0)
        SquadChemistryEvaluator.SquadChemistryAndFatigue chem = ctx.getSquadChemistry();
        if (chem != null) {
            vec[59] = (float) Math.min(1.0, chem.nationalFatiguedCount() / 5.0f);
        }

        return l2Normalize(vec);
    }

    private void evaluateFootballStarters(TacticalVectorContext ctx, float[] vec) {
        List<Map<String, Object>> starters = ctx.getStarters();
        if (starters == null || starters.isEmpty()) {
            return;
        }

        double gkRating = 0.0;
        int gkCount = 0;
        double cbRating = 0.0;
        int cbCount = 0;
        double midRating = 0.0;
        int midCount = 0;
        double attRating = 0.0;
        int attCount = 0;

        // Composite Team DNA & Missing Star Potency Analysis
        double topAttRating = 0.0;
        Long topAttId = null;
        double topDefRating = 0.0;
        Long topDefId = null;

        List<Map<String, Object>> allSquad = new ArrayList<>(starters);
        if (ctx.getBench() != null) allSquad.addAll(ctx.getBench());

        for (Map<String, Object> p : allSquad) {
            double r = parsePlayerRating(p);
            String pos = String.valueOf(p.getOrDefault("position", "")).toUpperCase();
            if ("F".equals(pos) || "A".equals(pos) || "FW".equals(pos) || "ST".equals(pos) || "LW".equals(pos) || "RW".equals(pos)) {
                if (r > topAttRating) {
                    topAttRating = r;
                    topAttId = toLong(p.get("sofascore_id"));
                    if (topAttId == null) topAttId = toLong(p.get("id"));
                }
            } else if ("D".equals(pos) || "CB".equals(pos) || "G".equals(pos) || "GK".equals(pos)) {
                if (r > topDefRating) {
                    topDefRating = r;
                    topDefId = toLong(p.get("sofascore_id"));
                    if (topDefId == null) topDefId = toLong(p.get("id"));
                }
            }
        }

        boolean topAttInStarters = false;
        boolean topDefInStarters = false;
        double starterAttSum = 0.0;
        int starterAttCount = 0;
        double starterDefSum = 0.0;
        int starterDefCount = 0;

        for (Map<String, Object> p : starters) {
            double r = parsePlayerRating(p);
            Long pid = toLong(p.get("sofascore_id"));
            if (pid == null) pid = toLong(p.get("id"));
            if (pid != null && pid > 0) {
                var pMmrOpt = clickHouse.getSportMmr(pid, "football-player", "ALL");
                if (pMmrOpt.isPresent()) {
                    double pMmr = pMmrOpt.get().mmr();
                    double mmrWeight = Math.max(0.70, Math.min(1.40, 1.0 + (pMmr - 1500.0) / 2800.0));
                    r = r * mmrWeight;
                }
            }
            if (pid != null && pid.equals(topAttId)) topAttInStarters = true;
            if (pid != null && pid.equals(topDefId)) topDefInStarters = true;

            String pos = String.valueOf(p.getOrDefault("position", "")).toUpperCase();

            if ("G".equals(pos) || "GK".equals(pos)) {
                gkRating += r;
                gkCount++;
                starterDefSum += r;
                starterDefCount++;
            } else if ("D".equals(pos) || "CB".equals(pos) || "LB".equals(pos) || "RB".equals(pos)) {
                cbRating += r;
                cbCount++;
                starterDefSum += r;
                starterDefCount++;
            } else if ("M".equals(pos) || "CM".equals(pos) || "DM".equals(pos) || "AM".equals(pos)) {
                midRating += r;
                midCount++;
                starterAttSum += r;
                starterAttCount++;
            } else {
                attRating += r;
                attCount++;
                starterAttSum += r;
                starterAttCount++;
            }
        }

        // Bench firepower reserve evaluation
        double benchFirepower = 0.0;
        int benchAttCount = 0;
        int benchJokers = 0;
        if (ctx.getBench() != null) {
            for (Map<String, Object> b : ctx.getBench()) {
                double r = parsePlayerRating(b);
                Long pid = toLong(b.get("sofascore_id"));
                if (pid == null) pid = toLong(b.get("id"));
                if (pid != null && pid > 0) {
                    var pMmrOpt = clickHouse.getSportMmr(pid, "football-player", "ALL");
                    if (pMmrOpt.isPresent()) {
                        double pMmr = pMmrOpt.get().mmr();
                        double mmrWeight = Math.max(0.70, Math.min(1.40, 1.0 + (pMmr - 1500.0) / 2800.0));
                        r = r * mmrWeight;
                    }
                }
                String pos = String.valueOf(b.getOrDefault("position", "")).toUpperCase();
                if ("F".equals(pos) || "A".equals(pos) || "FW".equals(pos) || "ST".equals(pos) || "LW".equals(pos) || "RW".equals(pos) || "M".equals(pos)) {
                    benchFirepower += r;
                    benchAttCount++;
                    if (r >= 7.0) benchJokers++;
                }
            }
        }

        // ── Team DNA Composite Metrics (Dims 24..29) ───────────────────────────
        // Dims 24: Starter Attacking Potency (0.0 .. 1.0)
        if (starterAttCount > 0) vec[24] = (float) Math.min(1.0, (starterAttSum / starterAttCount) / 10.0);

        // Dims 25: Missing Alpha Attacker Deficit (quantifies lost offensive punch without Haaland/star scorer)
        if (topAttRating > 7.0 && !topAttInStarters) {
            vec[25] = (float) Math.min(1.0, (topAttRating - 6.5) / 2.5);
        } else {
            vec[25] = 0.0f;
        }

        // Dims 26: Starter Defensive Solidity (0.0 .. 1.0)
        if (starterDefCount > 0) vec[26] = (float) Math.min(1.0, (starterDefSum / starterDefCount) / 10.0);

        // Dims 27: Missing Anchor Defender Deficit (quantifies backline fragility without key center-back/GK)
        if (topDefRating > 7.0 && !topDefInStarters) {
            vec[27] = (float) Math.min(1.0, (topDefRating - 6.5) / 2.5);
        } else {
            vec[27] = 0.0f;
        }

        // Dims 28: Lineup Quality Retention (Actual XI strength / Squad best XI potential)
        double totalStarterRating = (starterAttSum + starterDefSum);
        int totalStarters = (starterAttCount + starterDefCount);
        if (totalStarters > 0) {
            vec[28] = (float) Math.min(1.0, (totalStarterRating / totalStarters) / 7.5);
        }

        // Dims 29: Bench Firepower Reserve (Attacking depth for 2nd half turnaround)
        if (benchAttCount > 0) {
            float avgAtt = (float) ((benchFirepower / benchAttCount) / 10.0);
            float jokerBonus = (float) Math.min(0.20, benchJokers * 0.07);
            vec[29] = (float) Math.min(1.0, avgAtt + jokerBonus);
        }

        // ── Positional Line Ratings (Dims 60..63) ──────────────────────────────
        if (gkCount > 0) vec[60] = (float) Math.min(1.0, (gkRating / gkCount) / 10.0);
        if (cbCount > 0) vec[61] = (float) Math.min(1.0, (cbRating / cbCount) / 10.0);
        if (midCount > 0) vec[62] = (float) Math.min(1.0, (midRating / midCount) / 10.0);
        if (attCount > 0) vec[63] = (float) Math.min(1.0, (attRating / attCount) / 10.0);
    }

    private double parsePlayerRating(Map<String, Object> p) {
        if (p == null) return 6.8;
        Object rObj = p.get("effective_rating");
        if (rObj == null) rObj = p.get("rating");
        if (rObj == null) rObj = p.get("match_rating");
        double r = toDouble(rObj, 6.8);
        return (r >= 2.0 && r <= 10.0) ? r : 6.8;
    }

    @Override
    protected void populateBaselinePriors(float[] vec) {
        vec[8] = 0.48f; vec[9] = 0.32f; vec[10] = 0.16f; vec[11] = 0.22f;
        vec[12] = 0.18f; vec[13] = 0.25f; vec[14] = 0.28f; vec[15] = 0.24f;
        vec[16] = 0.45f; vec[17] = 0.21f;
        float[] bins = {0.138f, 0.142f, 0.150f, 0.188f, 0.163f, 0.217f};
        for (int i = 0; i < 6; i++) {
            vec[18 + i] = bins[i];
            vec[24 + i] = bins[i];
        }
        vec[30] = 0.34f; vec[31] = 0.34f; vec[32] = 0.50f; vec[33] = 0.75f;
        vec[34] = 0.25f; vec[35] = 0.30f; vec[36] = 0.52f; vec[37] = 0.50f;
        vec[38] = 0.80f; vec[39] = 0.20f; vec[40] = 0.50f; vec[41] = 0.44f;
        vec[42] = 0.33f; vec[43] = 0.25f; vec[44] = 0.30f; vec[45] = 0.44f;
        vec[46] = 0.28f; vec[47] = 0.24f;
        vec[48] = 0.38f; vec[49] = 0.25f; vec[50] = 0.15f; vec[51] = 0.35f;
        vec[52] = 0.50f; vec[53] = 0.36f; vec[54] = 0.65f; vec[55] = 0.0f;
        vec[56] = 0.0f; vec[57] = 0.0f; vec[58] = 0.0f; vec[59] = 0.18f;
        vec[60] = 0.68f; vec[61] = 0.68f; vec[62] = 0.68f; vec[63] = 0.68f;
    }
}
