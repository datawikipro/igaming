package pro.datawiki.igaming.analyst.rating;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.racket.TableTennisSport;

/**
 * Specialized Multi-Tier Glicko-2 Rating Engine for Table Tennis.
 * Implements:
 * 1. Progressive Asymmetric Win Scaling (diminishing return when beating weaker players in closed pools, 0% gain at delta >= 150)
 * 2. Progressive Upset Penalty (multiplies rating loss up to 1.75x when a favorite loses to an underdog)
 * 3. H2H Rematch Decay (1 / sqrt(N_h2h) to prevent endless MMR farming among the same opponents)
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
        double diff12 = p1.getMmr() - p2.getMmr(); // > 0 means P1 was favorite

        if (score1 > score2) { // P1 won
            if (diff12 > 0.0) { // P1 was favorite
                double scaleWin;
                if (diff12 <= 50.0) {
                    scaleWin = 1.0;
                } else if (diff12 <= 100.0) {
                    scaleWin = 1.0 - 0.25 * ((diff12 - 50.0) / 50.0);
                } else if (diff12 < 150.0) {
                    scaleWin = 0.75 - 0.75 * ((diff12 - 100.0) / 50.0);
                } else {
                    scaleWin = 0.0; // Delta >= 150: 0% bonus!
                }
                d1 *= scaleWin;
            } else if (diff12 < 0.0) { // P1 was underdog, P2 suffered upset
                double absDiff = Math.abs(diff12);
                double penalty;
                if (absDiff <= 50.0) {
                    penalty = 1.0;
                } else if (absDiff < 150.0) {
                    penalty = 1.0 + 0.75 * ((absDiff - 50.0) / 100.0);
                } else {
                    penalty = 1.75;
                }
                d2 *= penalty;
            }
        } else if (score2 > score1) { // P2 won
            if (diff12 < 0.0) { // P2 was favorite
                double absDiff = Math.abs(diff12);
                double scaleWin;
                if (absDiff <= 50.0) {
                    scaleWin = 1.0;
                } else if (absDiff <= 100.0) {
                    scaleWin = 1.0 - 0.25 * ((absDiff - 50.0) / 50.0);
                } else if (absDiff < 150.0) {
                    scaleWin = 0.75 - 0.75 * ((absDiff - 100.0) / 50.0);
                } else {
                    scaleWin = 0.0; // Delta >= 150: 0% bonus!
                }
                d2 *= scaleWin;
            } else if (diff12 > 0.0) { // P2 was underdog, P1 suffered upset
                double penalty;
                if (diff12 <= 50.0) {
                    penalty = 1.0;
                } else if (diff12 < 150.0) {
                    penalty = 1.0 + 0.75 * ((diff12 - 50.0) / 100.0);
                } else {
                    penalty = 1.75;
                }
                d1 *= penalty;
            }
        }

        if (h2hCount > 1) {
            double h2hDecay = 1.0 / Math.sqrt(h2hCount);
            d1 *= h2hDecay;
            d2 *= h2hDecay;
        }

        return new Deltas(d1, d2);
    }
}
