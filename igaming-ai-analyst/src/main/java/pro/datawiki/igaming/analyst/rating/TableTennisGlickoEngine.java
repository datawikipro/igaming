package pro.datawiki.igaming.analyst.rating;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.racket.TableTennisSport;

/**
 * Specialized Multi-Tier Glicko-2 Rating Engine for Table Tennis.
 * Implements:
 * 1. Symmetrical Exchange Damping for mismatched pairings in closed pools (Liga Pro, Setka Cup).
 *    Gradually damps exchange delta from 1.0 down to 0.20 when rating disparity is large (diff > 100).
 *    Both winner gain and loser loss are scaled by the exact same damping factor, strictly
 *    conserving rating mass and preventing deflationary vacuums.
 * 2. Symmetrical H2H Rematch Decay (1 / sqrt(N_h2h)) to prevent rating farming among the same opponents.
 */
@Slf4j
@Component
public class TableTennisGlickoEngine extends MultiTierGlickoEngine {

    private final TableTennisSport sport = new TableTennisSport();

    @Override
    public Sport getSport() {
        return sport;
    }

    @Override
    protected Deltas adjustRawDeltas(
            GlickoPlayer p1,
            GlickoPlayer p2,
            int score1,
            int score2,
            double rawDelta1,
            double rawDelta2,
            int h2hCount
    ) {
        double d1 = rawDelta1;
        double d2 = rawDelta2;
        double diff12 = p1.getMmr() - p2.getMmr();

        double absDiff = Math.abs(diff12);
        if (absDiff > 100.0) {
            // Symmetrical exchange damping for mismatched pairings in closed pools
            // Gradually damps delta from 1.0 down to 0.20 as disparity grows from 100 to 260
            double exchangeDamping = Math.max(0.20, 1.0 - 0.50 * ((absDiff - 100.0) / 100.0));
            d1 *= exchangeDamping;
            d2 *= exchangeDamping;
        }

        if (h2hCount > 1) {
            double h2hDecay = 1.0 / Math.sqrt(h2hCount);
            d1 *= h2hDecay;
            d2 *= h2hDecay;
        }

        return new Deltas(d1, d2);
    }
}
