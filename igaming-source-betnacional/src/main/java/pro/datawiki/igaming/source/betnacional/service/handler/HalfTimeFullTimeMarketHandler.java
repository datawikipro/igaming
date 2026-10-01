package pro.datawiki.igaming.source.betnacional.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalEventDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalMarketDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalOutcomeDto;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Dedicated handler for Half Time / Full Time (HT/FT) double result markets:
 * - Intervalo / Final (Portuguese)
 * - Half Time / Full Time / Double Result (English)
 */
@Component
public class HalfTimeFullTimeMarketHandler extends AbstractBetnacionalMarketHandler {

    @Override
    public boolean supports(BetnacionalMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CANTO") ||
            mName.contains("CARD") || mName.contains("CART") || mName.contains("AMAREL")) {
            return false;
        }

        return mName.contains("HALF TIME / FULL TIME") ||
               mName.contains("HALFTIME/FULLTIME") ||
               mName.contains("HALF-TIME / FULL-TIME") ||
               mName.contains("HALF TIME/ FULL TIME") ||
               mName.contains("HT / FT") ||
               mName.contains("HT/FT") ||
               mName.contains("DOUBLE RESULT") ||
               mName.contains("INTERVALO / FINAL") ||
               mName.contains("INTERVALO/FINAL") ||
               mName.contains("INTERVALO / FIM DO JOGO") ||
               mName.contains("INTERVALO/FIM DO JOGO") ||
               mName.contains("INTERVALO E FINAL") ||
               mName.contains("1º TEMPO / FINAL") ||
               mName.contains("1° TEMPO / FINAL") ||
               mName.contains("1º TEMPO/FINAL") ||
               mName.contains("1T / FINAL");
    }

    @Override
    public void handle(BetnacionalMarketDto market, BetnacionalEventDto event, SportType sportType, List<OddItem> items) {
        for (BetnacionalOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            HalfTimeFullTimeBet.Outcome htftOutcome = parseOutcome(oName, event);

            if (htftOutcome != null) {
                HalfTimeFullTimeBet betType = new HalfTimeFullTimeBet(htftOutcome);
                addOddItem(items, "half_time_full_time", oName, odds, betType);
            }
        }
    }

    private HalfTimeFullTimeBet.Outcome parseOutcome(String outcomeName, BetnacionalEventDto event) {
        String upper = outcomeName.toUpperCase().trim();

        String delimiter = null;
        if (upper.contains("/")) {
            delimiter = "/";
        } else if (upper.contains(" - ")) {
            delimiter = " - ";
        } else if (upper.contains("-")) {
            delimiter = "-";
        } else if (upper.contains(" E ")) {
            delimiter = " E ";
        }

        if (delimiter != null) {
            String[] parts = upper.split(Pattern.quote(delimiter), 2);
            int r1 = parseHalfOutcome(parts[0].trim(), event);
            int r2 = parseHalfOutcome(parts[1].trim(), event);
            if (r1 >= 0 && r2 >= 0) {
                if (r1 == 1 && r2 == 1) return HalfTimeFullTimeBet.Outcome.W1_W1;
                if (r1 == 1 && r2 == 0) return HalfTimeFullTimeBet.Outcome.W1_X;
                if (r1 == 1 && r2 == 2) return HalfTimeFullTimeBet.Outcome.W1_W2;
                if (r1 == 0 && r2 == 1) return HalfTimeFullTimeBet.Outcome.X_W1;
                if (r1 == 0 && r2 == 0) return HalfTimeFullTimeBet.Outcome.X_X;
                if (r1 == 0 && r2 == 2) return HalfTimeFullTimeBet.Outcome.X_W2;
                if (r1 == 2 && r2 == 1) return HalfTimeFullTimeBet.Outcome.W2_W1;
                if (r1 == 2 && r2 == 0) return HalfTimeFullTimeBet.Outcome.W2_X;
                if (r1 == 2 && r2 == 2) return HalfTimeFullTimeBet.Outcome.W2_W2;
            }
        }

        return null;
    }

    private int parseHalfOutcome(String part, BetnacionalEventDto event) {
        if ("1".equals(part) || "HOME".equals(part) || part.startsWith("HOME") ||
            "CASA".equals(part) || part.startsWith("CASA") ||
            "TEAM 1".equals(part) || "TEAM1".equals(part) || "TIME 1".equals(part) || "EQUIPE 1".equals(part)) {
            return 1;
        }
        if ("X".equals(part) || "DRAW".equals(part) || "TIE".equals(part) ||
            "EMPATE".equals(part) || "IGUALDADE".equals(part)) {
            return 0;
        }
        if ("2".equals(part) || "AWAY".equals(part) || part.startsWith("AWAY") ||
            "FORA".equals(part) || part.startsWith("FORA") ||
            "TEAM 2".equals(part) || "TEAM2".equals(part) || "TIME 2".equals(part) || "EQUIPE 2".equals(part)) {
            return 2;
        }
        if (event != null) {
            if (event.getHomeTeam() != null) {
                String home = event.getHomeTeam().toUpperCase();
                if (part.contains(home) || (home.length() >= 3 && home.contains(part))) {
                    return 1;
                }
            }
            if (event.getAwayTeam() != null) {
                String away = event.getAwayTeam().toUpperCase();
                if (part.contains(away) || (away.length() >= 3 && away.contains(part))) {
                    return 2;
                }
            }
        }
        return -1;
    }
}
