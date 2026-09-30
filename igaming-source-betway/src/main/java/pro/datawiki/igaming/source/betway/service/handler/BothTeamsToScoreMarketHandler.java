package pro.datawiki.igaming.source.betway.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;
import pro.datawiki.igaming.source.betway.dto.BetwayOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Both Teams To Score (BTTS) markets.
 */
@Component
public class BothTeamsToScoreMarketHandler extends AbstractBetwayMarketHandler {

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        return mName.contains("BOTH TEAMS TO SCORE") ||
               mName.contains("BTTS") ||
               mName.contains("BOTH TEAMS SCORE");
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = "btts" + (scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase()));

        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bttsOutcome = null;
            if ("YES".equals(upper) || upper.startsWith("YES")) {
                bttsOutcome = BinaryMarketBet.Outcome.YES;
            } else if ("NO".equals(upper) || upper.startsWith("NO")) {
                bttsOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bttsOutcome != null) {
                BinaryMarketBet betType = new BinaryMarketBet(scope, BetSubject.MATCH,
                        BinaryMarketBet.MarketType.BTTS, bttsOutcome, StatType.MATCH);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
