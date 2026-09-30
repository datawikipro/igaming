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
        return mName.contains("HALF TIME / FULL TIME") ||
               mName.contains("HALF TIME/FULL TIME") ||
               mName.contains("HALF-TIME / FULL-TIME") ||
               mName.contains("HT/FT") ||
               mName.contains("HT / FT") ||
               mName.contains("INTERVALO / FINAL DO JOGO");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String group = "ht_ft";
        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase().replace(" ", "");

            HalfTimeFullTimeBet.Outcome htFtOutcome = resolveHtFt(upper, event);
            if (htFtOutcome != null) {
                BetType bet = new HalfTimeFullTimeBet(htFtOutcome);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private HalfTimeFullTimeBet.Outcome resolveHtFt(String text, BetanoEventDto event) {
        if (text.contains("1/1") || text.contains("HOME/HOME")) return HalfTimeFullTimeBet.Outcome.W1_W1;
        if (text.contains("1/X") || text.contains("1/DRAW") || text.contains("HOME/DRAW")) return HalfTimeFullTimeBet.Outcome.W1_X;
        if (text.contains("1/2") || text.contains("HOME/AWAY")) return HalfTimeFullTimeBet.Outcome.W1_W2;
        if (text.contains("X/1") || text.contains("DRAW/1") || text.contains("DRAW/HOME")) return HalfTimeFullTimeBet.Outcome.X_W1;
        if (text.contains("X/X") || text.contains("DRAW/X") || text.contains("DRAW/DRAW")) return HalfTimeFullTimeBet.Outcome.X_X;
        if (text.contains("X/2") || text.contains("DRAW/2") || text.contains("DRAW/AWAY")) return HalfTimeFullTimeBet.Outcome.X_W2;
        if (text.contains("2/1") || text.contains("AWAY/HOME")) return HalfTimeFullTimeBet.Outcome.W2_W1;
        if (text.contains("2/X") || text.contains("2/DRAW") || text.contains("AWAY/DRAW")) return HalfTimeFullTimeBet.Outcome.W2_X;
        if (text.contains("2/2") || text.contains("AWAY/AWAY")) return HalfTimeFullTimeBet.Outcome.W2_W2;
        return null;
    }
}
