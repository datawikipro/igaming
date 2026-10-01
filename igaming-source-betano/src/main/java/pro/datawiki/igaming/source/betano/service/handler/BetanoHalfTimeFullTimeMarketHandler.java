package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Handler for Half Time / Full Time (HT/FT) markets for Betano.
 */
@Component
public class BetanoHalfTimeFullTimeMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        return mName.contains("HALF TIME / FULL TIME") ||
               mName.contains("HALF TIME/FULL TIME") ||
               mName.contains("HALF-TIME / FULL-TIME") ||
               mName.contains("HALF TIME/ FULL TIME") ||
               mName.contains("HALFTIME/FULLTIME") ||
               mName.contains("HT/FT") ||
               mName.contains("HT / FT") ||
               mName.contains("DOUBLE RESULT") ||
               mName.contains("INTERVALO / FINAL DO JOGO") ||
               mName.contains("INTERVALO/FINAL");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String group = "half_time_full_time";
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            HalfTimeFullTimeBet.Outcome htFtOutcome = parseOutcome(oName, event);
            if (htFtOutcome != null) {
                BetType bet = new HalfTimeFullTimeBet(htFtOutcome);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private HalfTimeFullTimeBet.Outcome parseOutcome(String outcomeName, BetanoEventDto event) {
        String upper = outcomeName.toUpperCase().trim();

        String delimiter = null;
        if (upper.contains("/")) {
            delimiter = "/";
        } else if (upper.contains(" - ")) {
            delimiter = " - ";
        } else if (upper.contains("-")) {
            delimiter = "-";
        }

        if (delimiter != null) {
            String[] parts = upper.split(java.util.regex.Pattern.quote(delimiter), 2);
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

    private int parseHalfOutcome(String part, BetanoEventDto event) {
        if ("1".equals(part) || "HOME".equals(part) || part.startsWith("HOME") || "TEAM 1".equals(part) || "TEAM1".equals(part) || "CASA".equals(part)) {
            return 1;
        }
        if ("X".equals(part) || "DRAW".equals(part) || "TIE".equals(part) || "EMPATE".equals(part)) {
            return 0;
        }
        if ("2".equals(part) || "AWAY".equals(part) || part.startsWith("AWAY") || "TEAM 2".equals(part) || "TEAM2".equals(part) || "FORA".equals(part)) {
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
