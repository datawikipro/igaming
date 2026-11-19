package pro.datawiki.igaming.analyst.quantitative;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BetTypeRegistry;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.Map;

/**
 * Resolves fallback bookmaker consensus odds from dossier metadata.
 * Uses typed {@link BetType} records rather than string manipulation.
 */
@Slf4j
@Component
public class DossierOddsFallbackResolver {

    public Double fallbackOddFromDossier(Map<String, Object> dossierMeta, String targetMarket) {
        if (dossierMeta == null || targetMarket == null || targetMarket.isBlank()) {
            return null;
        }

        BetType betType = BetTypeRegistry.fromCode(targetMarket);
        return fallbackOddFromDossier(dossierMeta, betType);
    }

    public Double fallbackOddFromDossier(Map<String, Object> dossierMeta, BetType betType) {
        if (dossierMeta == null || betType == null) {
            return null;
        }

        if (betType instanceof MatchResultBet mrb) {
            return switch (mrb.outcome()) {
                case WIN1, WIN1_2WAY -> getNumber(dossierMeta, "avg_win1", "best_win1");
                case WIN2, WIN2_2WAY -> getNumber(dossierMeta, "avg_win2", "best_win2");
                case DRAW -> getNumber(dossierMeta, "avg_draw", "best_draw");
                case DC_1X -> deriveSyntheticDoubleChance(dossierMeta, "avg_win1", "best_win1", "avg_draw", "best_draw");
                case DC_X2 -> deriveSyntheticDoubleChance(dossierMeta, "avg_win2", "best_win2", "avg_draw", "best_draw");
                default -> null;
            };
        }

        if (betType instanceof HandicapBet) {
            // Strictly prohibit synthetic handicap odd extrapolation (e.g. 1 + (winOdd - 1) * 0.45).
            // Only real bookmaker quotes from Redis consensus are valid for handicap markets.
            return null;
        }

        if (betType instanceof TotalBet tb) {
            if (tb.param() != null && Math.abs(tb.param() - 2.5) < 0.05) {
                return (tb.direction() == TotalBet.Direction.OVER)
                        ? getNumber(dossierMeta, "best_to25")
                        : getNumber(dossierMeta, "best_tu25");
            }
            return null;
        }

        return null;
    }

    private Double getNumber(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val instanceof Number n && n.doubleValue() > 1.01) {
                return n.doubleValue();
            }
        }
        return null;
    }

    private Double deriveSyntheticDoubleChance(
            Map<String, Object> map,
            String winKey1, String winKey2,
            String drawKey1, String drawKey2
    ) {
        Double w = getNumber(map, winKey1, winKey2);
        Double x = getNumber(map, drawKey1, drawKey2);
        if (w != null && x != null && w > 1.0 && x > 1.0) {
            double synthetic = (1.0 / (1.0 / w + 1.0 / x)) * 0.95;
            return Math.round(synthetic * 100.0) / 100.0;
        }
        return null;
    }
}
