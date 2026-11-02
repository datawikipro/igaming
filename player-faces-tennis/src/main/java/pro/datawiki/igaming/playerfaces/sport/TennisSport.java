package pro.datawiki.igaming.playerfaces.sport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TennisSurface;
import pro.datawiki.igaming.playerfaces.dto.PointByPointReturnStats;
import pro.datawiki.igaming.playerfaces.dto.PointByPointServeStats;
import pro.datawiki.igaming.playerfaces.dto.TennisSurfaceStats;
import pro.datawiki.igaming.playerfaces.dto.TiebreakStats;

import java.util.Map;

/**
 * Polymorphic sport class for Tennis (`TennisSport`).
 * Evaluates court-specific surface dynamics, point-by-point hold/break probabilities,
 * break point conversion efficiency, and tiebreak clutch expectancy.
 */
@Slf4j
@Component
public class TennisSport {

    /**
     * Court Speed Index (CPI) baseline coefficients per surface.
     */
    private static final Map<TennisSurface, Double> SURFACE_CPI_MULTIPLIER = Map.of(
            TennisSurface.HARD, 1.00,
            TennisSurface.CLAY, 0.85,
            TennisSurface.GRASS, 1.15,
            TennisSurface.INDOOR, 1.08
    );

    public SportType getSportType() {
        return SportType.TENNIS;
    }

    public boolean supportsDraw() {
        return false;
    }

    /**
     * Estimates single-point serving win probability on a given court surface,
     * incorporating 1st serve %, 1st serve win %, 2nd serve win %, and returner quality.
     *
     * @param serverServeStats server point-by-point serve metrics
     * @param returnerReturnStats returner point-by-point return metrics
     * @param surface court surface
     * @return probability (0.0 to 1.0) of winning a point on serve
     */
    public double calculateServePointWinProb(
            PointByPointServeStats serverServeStats,
            PointByPointReturnStats returnerReturnStats,
            TennisSurface surface
    ) {
        if (serverServeStats == null) {
            return 0.62; // Tour average baseline
        }

        double firstIn = (serverServeStats.getFirstServeInPct() != null ? serverServeStats.getFirstServeInPct() : 62.0) / 100.0;
        double firstWon = (serverServeStats.getFirstServeWonPct() != null ? serverServeStats.getFirstServeWonPct() : 72.0) / 100.0;
        double secondWon = (serverServeStats.getSecondServeWonPct() != null ? serverServeStats.getSecondServeWonPct() : 51.0) / 100.0;

        // Baseline server point win rate: P = (1stIn * 1stWon) + ((1 - 1stIn) * 2ndWon)
        double baseServerP = (firstIn * firstWon) + ((1.0 - firstIn) * secondWon);

        // Adjust for returner quality if available
        if (returnerReturnStats != null) {
            double ret1stWon = (returnerReturnStats.getFirstServeReturnWonPct() != null ? returnerReturnStats.getFirstServeReturnWonPct() : 30.0) / 100.0;
            double ret2ndWon = (returnerReturnStats.getSecondServeReturnWonPct() != null ? returnerReturnStats.getSecondServeReturnWonPct() : 50.0) / 100.0;
            double oppReturnPower = (ret1stWon + ret2ndWon) / 2.0;

            // Shift baseline relative to tour average return power (~0.40)
            double returnAdjustment = (0.40 - oppReturnPower) * 0.45;
            baseServerP += returnAdjustment;
        }

        // Surface multiplier: Fast courts (Grass, Indoor) enhance serve edge, Slow courts (Clay) reduce it
        double cpiMult = SURFACE_CPI_MULTIPLIER.getOrDefault(surface, 1.0);
        double adjustedP = 0.50 + ((baseServerP - 0.50) * Math.sqrt(cpiMult));

        // Clamp to realistic bounds [0.35, 0.88]
        return Math.max(0.35, Math.min(0.88, adjustedP));
    }

    /**
     * Computes the exact Markov chain probability of holding service game in tennis:
     * P(Hold) = p^4 * (15 - 34p + 28p^2 - 8p^3) / (1 - 2p(1 - p))
     *
     * @param pointWinProb probability of winning an individual serve point
     * @return probability of holding service game (0.0 to 1.0)
     */
    public double calculateGameHoldProb(double pointWinProb) {
        double p = Math.max(0.01, Math.min(0.99, pointWinProb));
        double q = 1.0 - p;
        double denom = 1.0 - (2.0 * p * q);
        if (denom <= 0.0) {
            return p >= 0.5 ? 1.0 : 0.0;
        }
        double numerator = Math.pow(p, 4) * (15.0 - (34.0 * p) + (28.0 * Math.pow(p, 2)) - (8.0 * Math.pow(p, 3)));
        double holdProb = numerator / denom;
        return Math.max(0.0, Math.min(1.0, holdProb));
    }

    /**
     * Calculates the probability of breaking opponent's serve on surface:
     * P(Break) = 1.0 - P(Hold)
     */
    public double calculateGameBreakProb(double pointWinProb) {
        return 1.0 - calculateGameHoldProb(pointWinProb);
    }

    /**
     * Calculates head-to-head edge on a specific surface between two players.
     * Edge > 0 favors Player 1, Edge < 0 favors Player 2.
     *
     * @param surface court surface
     * @param p1Stats player 1 surface statistics
     * @param p2Stats player 2 surface statistics
     * @return numerical edge score between -1.0 and 1.0
     */
    public double calculateSurfaceEdge(
            TennisSurface surface,
            TennisSurfaceStats p1Stats,
            TennisSurfaceStats p2Stats
    ) {
        if (p1Stats == null || p2Stats == null) {
            return 0.0;
        }

        // 1. Serving point win probability in both matchups
        double p1ServeP = calculateServePointWinProb(p1Stats.getServeStats(), p2Stats.getReturnStats(), surface);
        double p2ServeP = calculateServePointWinProb(p2Stats.getServeStats(), p1Stats.getReturnStats(), surface);

        double p1Hold = calculateGameHoldProb(p1ServeP);
        double p2Hold = calculateGameHoldProb(p2ServeP);

        // Expected game difference per service rotation
        double holdDiff = p1Hold - p2Hold;

        // 2. Break point efficiency factor
        double p1BpEdge = getBreakPointEfficiencyEdge(p1Stats, p2Stats);

        // 3. Tiebreak clutch differential
        double tbEdge = getTiebreakClutchEdge(p1Stats.getTiebreakStats(), p2Stats.getTiebreakStats());

        // Composite surface advantage
        double compositeEdge = (holdDiff * 0.60) + (p1BpEdge * 0.25) + (tbEdge * 0.15);
        return Math.max(-1.0, Math.min(1.0, compositeEdge));
    }

    /**
     * Calculates break point efficiency edge between two players.
     */
    public double getBreakPointEfficiencyEdge(TennisSurfaceStats p1, TennisSurfaceStats p2) {
        double p1Conv = p1.getReturnStats() != null && p1.getReturnStats().getBreakPointConversionPct() != null
                ? p1.getReturnStats().getBreakPointConversionPct() / 100.0 : 0.40;
        double p2Conv = p2.getReturnStats() != null && p2.getReturnStats().getBreakPointConversionPct() != null
                ? p2.getReturnStats().getBreakPointConversionPct() / 100.0 : 0.40;

        double p1Save = p1.getBreakPointSavePct() != null ? p1.getBreakPointSavePct() / 100.0 : 0.60;
        double p2Save = p2.getBreakPointSavePct() != null ? p2.getBreakPointSavePct() / 100.0 : 0.60;

        double p1NetBp = p1Conv - (1.0 - p1Save);
        double p2NetBp = p2Conv - (1.0 - p2Save);

        return (p1NetBp - p2NetBp) / 2.0;
    }

    /**
     * Computes tiebreak clutch advantage between two players.
     */
    public double getTiebreakClutchEdge(TiebreakStats p1Tb, TiebreakStats p2Tb) {
        double p1TbWinPct = p1Tb != null ? p1Tb.getEffectiveWinPct() / 100.0 : 0.50;
        double p2TbWinPct = p2Tb != null ? p2Tb.getEffectiveWinPct() / 100.0 : 0.50;
        return (p1TbWinPct - p2TbWinPct);
    }
}
