package pro.datawiki.igaming.source.betway.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;
import pro.datawiki.igaming.source.betway.dto.BetwayOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Half Time / Full Time (HT/FT) double result markets.
 */
@Component
public class HalfTimeFullTimeMarketHandler extends AbstractBetwayMarketHandler {

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        return mName.contains("HALF TIME / FULL TIME") ||
               mName.contains("HALFTIME/FULLTIME") ||
               mName.contains("HALF-TIME / FULL-TIME") ||
               mName.contains("HALF TIME/ FULL TIME") ||
               mName.contains("HT / FT") ||
               mName.contains("HT/FT") ||
               mName.contains("DOUBLE RESULT");
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
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

    private HalfTimeFullTimeBet.Outcome parseOutcome(String outcomeName, BetwayEventDto event) {
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

    private int parseHalfOutcome(String part, BetwayEventDto event) {
        if ("1".equals(part) || "HOME".equals(part) || part.startsWith("HOME") || "TEAM 1".equals(part) || "TEAM1".equals(part)) {
            return 1;
        }
        if ("X".equals(part) || "DRAW".equals(part) || "TIE".equals(part)) {
            return 0;
        }
        if ("2".equals(part) || "AWAY".equals(part) || part.startsWith("AWAY") || "TEAM 2".equals(part) || "TEAM2".equals(part)) {
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
