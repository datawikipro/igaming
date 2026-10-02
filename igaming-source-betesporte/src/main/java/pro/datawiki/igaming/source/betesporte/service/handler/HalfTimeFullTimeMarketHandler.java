package pro.datawiki.igaming.source.betesporte.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;

/**
 * Handler for Half Time / Full Time (Intervalo / Final) markets.
 */
@Component
@Order(70)
public class HalfTimeFullTimeMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CARD") || mName.contains("CART")) return false;

        return mName.contains("HALF TIME / FULL TIME") ||
               mName.contains("HALF TIME/FULL TIME") ||
               mName.contains("HALF-TIME / FULL-TIME") ||
               mName.contains("HT/FT") ||
               mName.contains("HT / FT") ||
               mName.contains("INTERVALO / FINAL") ||
               mName.contains("INTERVALO/FINAL") ||
               mName.contains("INTERVALO - FINAL");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String group = "half_time_full_time";

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            HalfTimeFullTimeBet.Outcome htFtOutcome = resolveHtFtOutcome(oName, event);
            if (htFtOutcome != null) {
                BetType betType = new HalfTimeFullTimeBet(htFtOutcome);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private HalfTimeFullTimeBet.Outcome resolveHtFtOutcome(String outcomeName, BetesporteEventDto event) {
        if (outcomeName == null || outcomeName.isBlank()) return null;
        String normalized = outcomeName.replace(" - ", "/").replace(" / ", "/").replace("-", "/").trim().toUpperCase();
        String[] parts = normalized.split("/");
        if (parts.length != 2) return null;

        String p1 = resolvePart(parts[0].trim(), event);
        String p2 = resolvePart(parts[1].trim(), event);
        if (p1 == null || p2 == null) return null;

        String key = p1 + "_" + p2;
        return switch (key) {
            case "1_1" -> HalfTimeFullTimeBet.Outcome.W1_W1;
            case "1_X" -> HalfTimeFullTimeBet.Outcome.W1_X;
            case "1_2" -> HalfTimeFullTimeBet.Outcome.W1_W2;
            case "X_1" -> HalfTimeFullTimeBet.Outcome.X_W1;
            case "X_X" -> HalfTimeFullTimeBet.Outcome.X_X;
            case "X_2" -> HalfTimeFullTimeBet.Outcome.X_W2;
            case "2_1" -> HalfTimeFullTimeBet.Outcome.W2_W1;
            case "2_X" -> HalfTimeFullTimeBet.Outcome.W2_X;
            case "2_2" -> HalfTimeFullTimeBet.Outcome.W2_W2;
            default -> null;
        };
    }

    private String resolvePart(String part, BetesporteEventDto event) {
        if ("1".equals(part) || "HOME".equals(part) || "CASA".equals(part)) return "1";
        if ("2".equals(part) || "AWAY".equals(part) || "FORA".equals(part)) return "2";
        if ("X".equals(part) || "DRAW".equals(part) || "EMPATE".equals(part) || "TIE".equals(part)) return "X";

        if (event != null && event.getHomeTeam() != null && !event.getHomeTeam().isBlank()) {
            String home = event.getHomeTeam().trim().toUpperCase();
            if (part.equals(home) || part.contains(home) || home.contains(part)) return "1";
        }
        if (event != null && event.getAwayTeam() != null && !event.getAwayTeam().isBlank()) {
            String away = event.getAwayTeam().trim().toUpperCase();
            if (part.equals(away) || part.contains(away) || away.contains(part)) return "2";
        }
        return null;
    }
}
