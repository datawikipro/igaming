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
        return mName.contains("HALF TIME / FULL TIME") ||
               mName.contains("HALFTIME/FULLTIME") ||
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
        String upper = outcomeName.toUpperCase().replace(" - ", "/").replace(" / ", "/").replace("-", "/");

        // Standard 1/X/2 combinations
        if (upper.equals("1/1") || upper.contains("HOME/HOME") || upper.contains("1 / 1")) return HalfTimeFullTimeBet.Outcome.W1_W1;
        if (upper.equals("1/X") || upper.contains("HOME/DRAW") || upper.contains("1 / X")) return HalfTimeFullTimeBet.Outcome.W1_X;
        if (upper.equals("1/2") || upper.contains("HOME/AWAY") || upper.contains("1 / 2")) return HalfTimeFullTimeBet.Outcome.W1_W2;
        if (upper.equals("X/1") || upper.contains("DRAW/HOME") || upper.contains("X / 1")) return HalfTimeFullTimeBet.Outcome.X_W1;
        if (upper.equals("X/X") || upper.contains("DRAW/DRAW") || upper.contains("X / X")) return HalfTimeFullTimeBet.Outcome.X_X;
        if (upper.equals("X/2") || upper.contains("DRAW/AWAY") || upper.contains("X / 2")) return HalfTimeFullTimeBet.Outcome.X_W2;
        if (upper.equals("2/1") || upper.contains("AWAY/HOME") || upper.contains("2 / 1")) return HalfTimeFullTimeBet.Outcome.W2_W1;
        if (upper.equals("2/X") || upper.contains("AWAY/DRAW") || upper.contains("2 / X")) return HalfTimeFullTimeBet.Outcome.W2_X;
        if (upper.equals("2/2") || upper.contains("AWAY/AWAY") || upper.contains("2 / 2")) return HalfTimeFullTimeBet.Outcome.W2_W2;

        if (event.getHomeTeam() != null && event.getAwayTeam() != null) {
            String home = event.getHomeTeam().toUpperCase();
            String away = event.getAwayTeam().toUpperCase();
            if (upper.startsWith(home) && upper.endsWith(home)) return HalfTimeFullTimeBet.Outcome.W1_W1;
            if (upper.startsWith(home) && upper.contains("DRAW")) return HalfTimeFullTimeBet.Outcome.W1_X;
            if (upper.startsWith(home) && upper.endsWith(away)) return HalfTimeFullTimeBet.Outcome.W1_W2;
            if (upper.startsWith("DRAW") && upper.endsWith(home)) return HalfTimeFullTimeBet.Outcome.X_W1;
            if (upper.startsWith("DRAW") && upper.endsWith("DRAW")) return HalfTimeFullTimeBet.Outcome.X_X;
            if (upper.startsWith("DRAW") && upper.endsWith(away)) return HalfTimeFullTimeBet.Outcome.X_W2;
            if (upper.startsWith(away) && upper.endsWith(home)) return HalfTimeFullTimeBet.Outcome.W2_W1;
            if (upper.startsWith(away) && upper.contains("DRAW")) return HalfTimeFullTimeBet.Outcome.W2_X;
            if (upper.startsWith(away) && upper.endsWith(away)) return HalfTimeFullTimeBet.Outcome.W2_W2;
        }

        return null;
    }
}
