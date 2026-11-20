package pro.datawiki.igaming.analyst.quantitative;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.dto.MlPredictionDto;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BetTypeRegistry;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.dto.ml.ContinuousProbabilityMatrixDto;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.TacticalProbabilityMath;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Resolves objective mathematical fair probabilities for candidate betting markets.
 * Fully OOP-driven: parses market codes into strongly-typed {@link BetType} records
 * and dispatches calculation polymorphically without string startsWith/contains heuristics.
 */
@Slf4j
@Component
public class MarketFairProbabilityResolver {

    public double resolveFairProbability(MlPredictionDto ml, String market, Sport sport) {
        return resolveFairProbability(ml, market, sport, 3, null);
    }

    public double resolveFairProbability(MlPredictionDto ml, String market, Sport sport, int bestOf, Double expTotal) {
        if (market == null || market.isBlank()) {
            return 60.0;
        }

        BetType betType = BetTypeRegistry.fromCode(market);
        return resolveFairProbability(ml, betType, sport, bestOf, expTotal);
    }

    public double resolveFairProbability(MlPredictionDto ml, BetType betType, Sport sport, int bestOf, Double expTotal) {
        if (betType == null) {
            return 60.0;
        }

        ContinuousProbabilityMatrixDto matrix = (ml != null) ? ml.getProbabilityMatrix() : null;

        if (betType instanceof TotalBet totalBet) {
            return resolveTotalProbability(ml, matrix, totalBet, expTotal);
        }

        if (betType instanceof HandicapBet handicapBet) {
            return resolveHandicapProbability(ml, matrix, handicapBet, sport, bestOf, expTotal);
        }

        if (betType instanceof MatchResultBet matchResultBet) {
            return resolveMatchResultProbability(ml, matchResultBet);
        }

        if (betType instanceof BinaryMarketBet binaryBet) {
            return resolveBinaryMarketProbability(matrix, binaryBet, expTotal);
        }

        // Generic fallback to model recommended prob or 65%
        return (ml != null && ml.getRecommendedProb() != null)
                ? ml.getRecommendedProb().doubleValue() * 100.0
                : 65.0;
    }

    private double resolveTotalProbability(
            MlPredictionDto ml,
            ContinuousProbabilityMatrixDto matrix,
            TotalBet totalBet,
            Double expTotal
    ) {
        Double line = totalBet.param();
        boolean isOver = totalBet.direction() == TotalBet.Direction.OVER;

        // 1. Try continuous probability surface first with monotonic interpolation
        if (matrix != null && line != null) {
            Map<Double, Double> surface = isOver ? matrix.getTotalOverProbabilities() : matrix.getTotalUnderProbabilities();
            if (surface != null && !surface.isEmpty()) {
                return interpolateSurface(surface, line, isOver);
            }
        }

        // 2. Check effective cushion matches
        if (ml != null && line != null) {
            if (isOver) {
                Double cLow = ml.getEffectiveCushionLow();
                if (cLow != null && Math.abs(line - cLow) < 0.1 && ml.getEffectiveProbCushionLow() != null) {
                    return toPct(BigDecimal.valueOf(ml.getEffectiveProbCushionLow()), 78.0);
                }
            } else {
                Double cHigh = ml.getEffectiveCushionHigh();
                if (cHigh != null && Math.abs(line - cHigh) < 0.1 && ml.getEffectiveProbCushionHigh() != null) {
                    return toPct(BigDecimal.valueOf(ml.getEffectiveProbCushionHigh()), 78.0);
                }
            }
        }

        // 3. ML scalar probability fallback
        if (ml != null) {
            BigDecimal scalar = isOver ? ml.getProbOver() : ml.getProbUnder();
            if (scalar != null && scalar.doubleValue() > 0) {
                return toPct(scalar, isOver ? 50.0 : 55.0);
            }
        }

        return isOver ? 50.0 : 55.0;
    }

    private double resolveHandicapProbability(
            MlPredictionDto ml,
            ContinuousProbabilityMatrixDto matrix,
            HandicapBet handicapBet,
            Sport sport,
            int bestOf,
            Double expTotal
    ) {
        Double line = handicapBet.param();
        boolean isHome = handicapBet.outcome() == HandicapBet.Outcome.TEAM1;
        double p1 = ml != null ? toPct(ml.getProbHome(), 50.0) : 50.0;
        double p2 = ml != null ? toPct(ml.getProbAway(), 50.0) : 50.0;

        // 1. Try continuous probability surface first
        if (matrix != null && line != null) {
            Map<Double, Double> hdcSurface = isHome ? matrix.getHandicapHomeProbabilities() : matrix.getHandicapAwayProbabilities();
            if (hdcSurface != null && !hdcSurface.isEmpty()) {
                return interpolateSurface(hdcSurface, line, isHome);
            }
        }

        // 2. Draw-No-Bet (DNB / Handicap 0.0)
        if (line != null && Math.abs(line) < 0.05) {
            double sum = p1 + p2;
            return sum > 0 ? ((isHome ? p1 : p2) / sum) * 100.0 : 65.0;
        }

        // 3. Set Handicaps (+1.5) for solo / racket sports (Tennis, Table Tennis)
        if (sport != null && !sport.isTeamSport() && line != null && line > 0) {
            double baseP = (isHome ? p1 : p2) / 100.0;
            return TacticalProbabilityMath.tennisSetHandicapPlus15(baseP, bestOf) * 100.0;
        }

        // 4. Calibrated Sigmoidal Margin Model (Zero Uncalibrated Poisson)
        if (line != null) {
            double diff = (isHome ? (p1 - p2) : (p2 - p1)) / 100.0;
            double baseline = (sport != null) ? sport.getDefaultTotalBaseline() : 2.5;
            double scale = Math.max(1.0, baseline * 0.5);
            double z = (diff * 2.5) + (line / scale);
            double p = 1.0 / (1.0 + Math.exp(-z));
            return Math.max(1.0, Math.min(99.0, p * 100.0));
        }

        return isHome ? p1 : p2;
    }

    private double resolveMatchResultProbability(MlPredictionDto ml, MatchResultBet matchResultBet) {
        double p1 = ml != null ? toPct(ml.getProbHome(), 50.0) : 50.0;
        double px = ml != null && ml.getProbDraw() != null ? toPct(ml.getProbDraw(), 25.0) : 25.0;
        double p2 = ml != null ? toPct(ml.getProbAway(), 50.0) : 50.0;

        return switch (matchResultBet.outcome()) {
            case WIN1, WIN1_2WAY -> p1;
            case WIN2, WIN2_2WAY -> p2;
            case DRAW -> px;
            case DC_1X -> Math.min(95.0, p1 + px);
            case DC_X2 -> Math.min(95.0, p2 + px);
            case DC_12 -> Math.min(95.0, p1 + p2);
        };
    }

    private double resolveBinaryMarketProbability(
            ContinuousProbabilityMatrixDto matrix,
            BinaryMarketBet binaryBet,
            Double expTotal
    ) {
        if (binaryBet.marketType() == BinaryMarketBet.MarketType.BTTS) {
            boolean isYes = binaryBet.outcome() == BinaryMarketBet.Outcome.YES;
            if (matrix != null && matrix.getBttsYesProb() > 0) {
                return (isYes ? matrix.getBttsYesProb() : matrix.getBttsNoProb()) * 100.0;
            }
            return isYes ? 52.0 : 48.0;
        }
        return 50.0;
    }

    private double interpolateSurface(java.util.Map<Double, Double> surface, double targetLine, boolean isOver) {
        if (surface == null || surface.isEmpty()) {
            return 50.0;
        }
        if (surface.containsKey(targetLine)) {
            return surface.get(targetLine) * 100.0;
        }

        Double lowerLine = null;
        Double upperLine = null;
        for (Double k : surface.keySet()) {
            if (k < targetLine) {
                if (lowerLine == null || k > lowerLine) {
                    lowerLine = k;
                }
            } else if (k > targetLine) {
                if (upperLine == null || k < upperLine) {
                    upperLine = k;
                }
            }
        }

        if (lowerLine != null && upperLine != null) {
            double pLow = surface.get(lowerLine);
            double pHigh = surface.get(upperLine);
            double fraction = (targetLine - lowerLine) / (upperLine - lowerLine);
            double interpolated = pLow + fraction * (pHigh - pLow);
            return Math.max(1.0, Math.min(99.0, interpolated * 100.0));
        }

        if (lowerLine != null) {
            double pBase = surface.get(lowerLine);
            double delta = targetLine - lowerLine;
            double p = isOver ? pBase * Math.exp(-delta * 0.5) : 1.0 - (1.0 - pBase) * Math.exp(-delta * 0.5);
            return Math.max(1.0, Math.min(99.0, p * 100.0));
        }

        if (upperLine != null) {
            double pBase = surface.get(upperLine);
            double delta = upperLine - targetLine;
            double p = isOver ? 1.0 - (1.0 - pBase) * Math.exp(-delta * 0.5) : pBase * Math.exp(-delta * 0.5);
            return Math.max(1.0, Math.min(99.0, p * 100.0));
        }

        return 50.0;
    }

    private double toPct(BigDecimal val, double fallback) {
        if (val == null) return fallback;
        double d = val.doubleValue();
        return (d > 0.0 && d <= 1.0) ? d * 100.0 : d;
    }
}
