package pro.datawiki.igaming.analyst.rating;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Universal Multi-Tier Glicko-2 Rating Engine.
 * Features:
 * 1. Mathematically rigorous Glicko-2 with Illinois root-finding for volatility.
 * 2. Margin of Victory (MOV) weighting on games/sets dominance.
 * 3. Asymptotic Soft Cap (Polish Junior Guard) preventing closed-pool runaway inflation.
 */
@Slf4j
@Component
public class MultiTierGlickoEngine {

    public static final double TAU = 0.5; // Volatility constraint parameter
    public static final double EPSILON = 0.000001;
    public static final double GLICKO2_SCALE = GlickoPlayer.GLICKO2_SCALE;

    public record MatchUpdateResult(double deltaP1, double deltaP2) {}

    /**
     * Executes a 1v1 match rating update.
     */
    public MatchUpdateResult updateMatch(
            GlickoPlayer p1,
            GlickoPlayer p2,
            int score1,
            int score2,
            TournamentTierClassifier.TierInfo tier,
            double movMultiplier
    ) {
        double s1 = (score1 == score2) ? 0.5 : (score1 > score2 ? 1.0 : 0.0);
        double s2 = 1.0 - s1;

        double mu1 = p1.getMu();
        double phi1 = p1.getPhi();
        double vol1 = p1.getVolatility();

        double mu2 = p2.getMu();
        double phi2 = p2.getPhi();
        double vol2 = p2.getVolatility();

        double g1 = g(phi1);
        double g2 = g(phi2);
        double e1 = expectedScore(mu1, mu2, phi2);
        double e2 = expectedScore(mu2, mu1, phi1);

        double effWeight = Math.max(0.75, Math.min(1.30, movMultiplier));

        // 1. Variance and Delta for P1
        double v1 = 1.0 / (g2 * g2 * e1 * (1.0 - e1) * effWeight + EPSILON);
        double delta1 = v1 * g2 * (s1 - e1) * effWeight;

        // Volatility for P1
        double newVol1 = solveNewVolatility(vol1, phi1, v1, delta1);
        double phiStar1 = Math.sqrt(phi1 * phi1 + newVol1 * newVol1);
        double newPhi1 = 1.0 / Math.sqrt(1.0 / (phiStar1 * phiStar1) + 1.0 / v1);
        double newMu1 = mu1 + newPhi1 * newPhi1 * g2 * (s1 - e1) * effWeight;

        // 2. Variance and Delta for P2
        double v2 = 1.0 / (g1 * g1 * e2 * (1.0 - e2) * effWeight + EPSILON);
        double delta2 = v2 * g1 * (s2 - e2) * effWeight;

        double newVol2 = solveNewVolatility(vol2, phi2, v2, delta2);
        double phiStar2 = Math.sqrt(phi2 * phi2 + newVol2 * newVol2);
        double newPhi2 = 1.0 / Math.sqrt(1.0 / (phiStar2 * phiStar2) + 1.0 / v2);
        double newMu2 = mu2 + newPhi2 * newPhi2 * g1 * (s2 - e2) * effWeight;

        // Raw MMR deltas
        double targetMmr1 = (newMu1 * GLICKO2_SCALE) + GlickoPlayer.DEFAULT_MMR;
        double targetMmr2 = (newMu2 * GLICKO2_SCALE) + GlickoPlayer.DEFAULT_MMR;
        double rawDelta1 = targetMmr1 - p1.getMmr();
        double rawDelta2 = targetMmr2 - p2.getMmr();

        // 3. Asymptotic Soft Cap Dampening (Polish Junior Guard)
        double finalDelta1 = rawDelta1;
        double finalDelta2 = rawDelta2;
        int softCap = tier.softCapMmr();

        if (s1 > s2 && rawDelta1 > 0) {
            finalDelta1 = applySoftCapDamping(p1.getMmr(), softCap, rawDelta1);
        }
        if (s2 > s1 && rawDelta2 > 0) {
            finalDelta2 = applySoftCapDamping(p2.getMmr(), softCap, rawDelta2);
        }

        // Apply to P1
        p1.setMmr(p1.getMmr() + finalDelta1);
        p1.setRd(Math.min(350.0, Math.max(30.0, newPhi1 * GLICKO2_SCALE)));
        p1.setVolatility(newVol1);
        p1.setMatchesPlayed(p1.getMatchesPlayed() + 1);
        if (s1 > s2) {
            p1.setWins(p1.getWins() + 1);
            p1.setStreak(p1.getStreak() > 0 ? p1.getStreak() + 1 : 1);
        } else {
            p1.setLosses(p1.getLosses() + 1);
            p1.setStreak(p1.getStreak() < 0 ? p1.getStreak() - 1 : -1);
        }
        if (p1.getMmr() > p1.getPeakMmr()) {
            p1.setPeakMmr(p1.getMmr());
        }

        // Apply to P2
        p2.setMmr(p2.getMmr() + finalDelta2);
        p2.setRd(Math.min(350.0, Math.max(30.0, newPhi2 * GLICKO2_SCALE)));
        p2.setVolatility(newVol2);
        p2.setMatchesPlayed(p2.getMatchesPlayed() + 1);
        if (s2 > s1) {
            p2.setWins(p2.getWins() + 1);
            p2.setStreak(p2.getStreak() > 0 ? p2.getStreak() + 1 : 1);
        } else {
            p2.setLosses(p2.getLosses() + 1);
            p2.setStreak(p2.getStreak() < 0 ? p2.getStreak() - 1 : -1);
        }
        if (p2.getMmr() > p2.getPeakMmr()) {
            p2.setPeakMmr(p2.getMmr());
        }

        return new MatchUpdateResult(finalDelta1, finalDelta2);
    }

    public static double applySoftCapDamping(double currentMmr, int softCap, double rawDelta) {
        double buffer = 150.0;
        double threshold = softCap - buffer;
        if (currentMmr >= softCap) {
            return 0.0; // Absolute ceiling inside lower closed leagues
        } else if (currentMmr > threshold) {
            double ratio = (currentMmr - threshold) / buffer;
            double gamma = Math.cos(ratio * (Math.PI / 2.0));
            return rawDelta * gamma;
        } else {
            return rawDelta;
        }
    }

    public static double g(double phi) {
        return 1.0 / Math.sqrt(1.0 + (3.0 * phi * phi) / (Math.PI * Math.PI));
    }

    public static double expectedScore(double mu, double muOpp, double phiOpp) {
        return 1.0 / (1.0 + Math.exp(-g(phiOpp) * (mu - muOpp)));
    }

    private static double solveNewVolatility(double vol, double phi, double v, double delta) {
        double a = Math.log(vol * vol);
        double deltaSq = delta * delta;
        double phiSq = phi * phi;

        java.util.function.DoubleUnaryOperator f = x -> {
            double ex = Math.exp(x);
            double denom = 2.0 * Math.pow(phiSq + v + ex, 2);
            double part1 = (ex * (deltaSq - phiSq - v - ex)) / denom;
            double part2 = (x - a) / (TAU * TAU);
            return part1 - part2;
        };

        double A = a;
        double B;
        if (deltaSq > phiSq + v) {
            B = Math.log(deltaSq - phiSq - v);
        } else {
            int k = 1;
            while (f.applyAsDouble(a - k * TAU) < 0) {
                k++;
            }
            B = a - k * TAU;
        }

        double fa = f.applyAsDouble(A);
        double fb = f.applyAsDouble(B);

        for (int iter = 0; iter < 50; iter++) {
            if (Math.abs(B - A) <= EPSILON) break;
            double C = A + (A - B) * fa / (fb - fa);
            double fc = f.applyAsDouble(C);
            if (fc * fb < 0) {
                A = B;
                fa = fb;
            } else {
                fa = fa / 2.0;
            }
            B = C;
            fb = fc;
        }

        return Math.exp(A / 2.0);
    }
}
