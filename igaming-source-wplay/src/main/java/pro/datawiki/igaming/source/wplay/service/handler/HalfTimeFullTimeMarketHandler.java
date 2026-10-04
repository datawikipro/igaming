package pro.datawiki.igaming.source.wplay.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for Half Time / Full Time (HT/FT) markets:
 * - "Half Time / Full Time" / "HT/FT" (English)
 * - "Descanso / Final" / "Medio Tiempo / Tiempo Completo" (Spanish)
 * Maps composite outcomes like "1/1", "1/X", "X/2" to HalfTimeFullTimeBet.Outcome (W1_W1, W1_X, W2_W2, etc.)
 */
@Component
@Order(60)
public class HalfTimeFullTimeMarketHandler extends AbstractWplayMarketHandler {

    /**
     * Matches composite HT/FT outcome tokens separated by '/', '-', or ' ':
     * e.g. "1/1", "X/2", "2/X", "HOME/DRAW", "1-X", "Local/Local"
     */
    private static final Pattern HTFT_SLASH_PATTERN = Pattern.compile(
            "([12X]|HOME|AWAY|DRAW|LOCAL|VISITANTE|EMPATE)\\s*[/\\-]\\s*([12X]|HOME|AWAY|DRAW|LOCAL|VISITANTE|EMPATE)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("HALF TIME / FULL TIME") ||
               mName.contains("HALF TIME/FULL TIME") ||
               mName.contains("HALF-TIME / FULL-TIME") ||
               mName.contains("HALFTIME/FULLTIME") ||
               mName.contains("HT/FT") ||
               mName.contains("HT / FT") ||
               mName.contains("DESCANSO / FINAL") ||
               mName.contains("DESCANSO/FINAL") ||
               mName.contains("MEDIO TIEMPO / TIEMPO COMPLETO") ||
               mName.contains("MEDIO TIEMPO/TIEMPO COMPLETO");
    }

    @Override
    public void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items) {
        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            BetType betType = resolveHtFt(oName, event);
            if (betType != null) {
                addOddItem(items, "ht_ft", oName, odds, betType);
            }
        }
    }

    /**
     * Resolves the HT/FT BetType from outcome name like "1/1", "X/2", "HOME/DRAW".
     * Maps to HalfTimeFullTimeBet.Outcome enum values: W1_W1, W1_X, W1_W2, X_W1, X_X, X_W2, W2_W1, W2_X, W2_W2.
     */
    private BetType resolveHtFt(String oName, WplayEventDto event) {
        Matcher m = HTFT_SLASH_PATTERN.matcher(oName.toUpperCase());
        if (!m.find()) return null;

        String ht = normalizeToken(m.group(1));
        String ft = normalizeToken(m.group(2));
        if (ht == null || ft == null) return null;

        HalfTimeFullTimeBet.Outcome htFtOutcome = toHtFtOutcome(ht, ft);
        if (htFtOutcome == null) return null;
        return new HalfTimeFullTimeBet(htFtOutcome);
    }

    /**
     * Normalizes a token to "W1", "W2", or "X".
     */
    private String normalizeToken(String token) {
        String t = token.trim().toUpperCase();
        if ("1".equals(t) || "HOME".equals(t) || "LOCAL".equals(t) || "CASA".equals(t)) return "W1";
        if ("2".equals(t) || "AWAY".equals(t) || "VISITANTE".equals(t) || "FORA".equals(t)) return "W2";
        if ("X".equals(t) || "DRAW".equals(t) || "EMPATE".equals(t) || "TIE".equals(t)) return "X";
        return null;
    }

    /**
     * Converts normalized HT/FT tokens to HalfTimeFullTimeBet.Outcome.
     */
    private HalfTimeFullTimeBet.Outcome toHtFtOutcome(String ht, String ft) {
        return switch (ht + "_" + ft) {
            case "W1_W1" -> HalfTimeFullTimeBet.Outcome.W1_W1;
            case "W1_X"  -> HalfTimeFullTimeBet.Outcome.W1_X;
            case "W1_W2" -> HalfTimeFullTimeBet.Outcome.W1_W2;
            case "X_W1"  -> HalfTimeFullTimeBet.Outcome.X_W1;
            case "X_X"   -> HalfTimeFullTimeBet.Outcome.X_X;
            case "X_W2"  -> HalfTimeFullTimeBet.Outcome.X_W2;
            case "W2_W1" -> HalfTimeFullTimeBet.Outcome.W2_W1;
            case "W2_X"  -> HalfTimeFullTimeBet.Outcome.W2_X;
            case "W2_W2" -> HalfTimeFullTimeBet.Outcome.W2_W2;
            default -> null;
        };
    }
}
