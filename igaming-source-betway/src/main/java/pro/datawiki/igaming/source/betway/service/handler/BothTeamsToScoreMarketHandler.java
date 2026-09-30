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
               mName.contains("BOTH TEAMS SCORE") ||
               mName.contains("GOAL / GOAL") ||
               mName.contains("GOAL/GOAL");
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        boolean isBothHalves = mName.contains("BOTH HALVES") || mName.contains("IN BOTH HALVES");
        BetScope scope = isBothHalves ? BetScope.FULL_MATCH : resolveScope(mName);
        String group = isBothHalves ? "btts_both_halves" : formatGroupName("btts", scope);
        BinaryMarketBet.MarketType marketType = isBothHalves ? BinaryMarketBet.MarketType.BOTH_HALVES_BTTS : BinaryMarketBet.MarketType.BTTS;

        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bttsOutcome = null;
            if ("YES".equals(upper) || upper.startsWith("YES") || "GG".equals(upper) || "GOAL/GOAL".equals(upper) || "GOAL / GOAL".equals(upper)) {
                bttsOutcome = BinaryMarketBet.Outcome.YES;
            } else if ("NO".equals(upper) || upper.startsWith("NO") || "NG".equals(upper) || "NO GOAL".equals(upper) || "NO/NO".equals(upper)) {
                bttsOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bttsOutcome != null) {
                BinaryMarketBet betType = new BinaryMarketBet(scope, BetSubject.MATCH,
                        marketType, bttsOutcome, StatType.MATCH);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
