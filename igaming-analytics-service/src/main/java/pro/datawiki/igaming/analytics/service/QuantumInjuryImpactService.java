package pro.datawiki.igaming.analytics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analytics.dto.InjuryProbabilityAdjustmentRequest;
import pro.datawiki.igaming.analytics.dto.InjuryProbabilityAdjustmentResponse;

/**
 * Quantum and pre-match analytical engine that recalculates match win probabilities
 * and fair Asian handicap spreads based on confirmed squad leader absences 24-48h before kickoff.
 */
@Service
@Slf4j
public class QuantumInjuryImpactService {

    /**
     * Recalculates win/draw/win probabilities and handicap line considering leader absences.
     */
    public InjuryProbabilityAdjustmentResponse adjustProbabilities(InjuryProbabilityAdjustmentRequest req) {
        if (req == null) return null;

        double homeImpact = req.getHomeKeyAbsenceImpact();
        double awayImpact = req.getAwayKeyAbsenceImpact();
        double netAdvantage = round3(awayImpact - homeImpact);

        // Mathematical scaling factor: each 1.0 of key player absence corresponds to ~12.5% fair probability shift
        double shift = netAdvantage * 0.125;

        double origHome = req.getBaseHomeProb();
        double origDraw = req.getBaseDrawProb();
        double origAway = req.getBaseAwayProb();

        // Normalize base probabilities if needed
        double totalBase = origHome + origDraw + origAway;
        if (totalBase <= 0.0) totalBase = 1.0;
        origHome /= totalBase;
        origDraw /= totalBase;
        origAway /= totalBase;

        // Apply shift
        double adjHome = Math.max(0.05, Math.min(0.90, origHome + shift));
        double adjAway = Math.max(0.05, Math.min(0.90, origAway - shift));
        double remaining = Math.max(0.02, 1.0 - adjHome - adjAway);
        double adjDraw = remaining;

        // Re-normalize so sum is 1.0
        double totalAdj = adjHome + adjDraw + adjAway;
        adjHome = round3(adjHome / totalAdj);
        adjDraw = round3(adjDraw / totalAdj);
        adjAway = round3(adjAway / totalAdj);

        // Handicap adjustment: net advantage shifts fair goal line by ~0.35 goals per 1.0 impact
        double handicapShift = -netAdvantage * 0.35;
        double adjustedHandicap = round2(req.getBaseHandicapHome() + handicapShift);

        String rationale = String.format(
                "Leader absence analysis: Home penalty=%.2f, Away penalty=%.2f, Net advantage=%.2f. Win probability Home %s from %.1f%% to %.1f%%; Handicap adjusted from %.2f to %.2f.",
                homeImpact, awayImpact, netAdvantage,
                (shift >= 0 ? "increased" : "decreased"),
                origHome * 100, adjHome * 100,
                req.getBaseHandicapHome(), adjustedHandicap
        );

        log.info("[Quantum Injury Engine] Calculated adjustments for {} vs {}: NetAdvantage={}, AdjHomeProb={}, AdjHandicap={}",
                req.getHomeTeam(), req.getAwayTeam(), netAdvantage, adjHome, adjustedHandicap);

        return InjuryProbabilityAdjustmentResponse.builder()
                .sport(req.getSport() != null ? req.getSport() : "football")
                .homeTeam(req.getHomeTeam())
                .awayTeam(req.getAwayTeam())
                .originalHomeProb(round3(origHome))
                .originalDrawProb(round3(origDraw))
                .originalAwayProb(round3(origAway))
                .adjustedHomeProb(adjHome)
                .adjustedDrawProb(adjDraw)
                .adjustedAwayProb(adjAway)
                .originalHandicapHome(req.getBaseHandicapHome())
                .adjustedHandicapHome(adjustedHandicap)
                .netInjuryAdvantage(netAdvantage)
                .summaryRationale(rationale)
                .build();
    }

    private double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
