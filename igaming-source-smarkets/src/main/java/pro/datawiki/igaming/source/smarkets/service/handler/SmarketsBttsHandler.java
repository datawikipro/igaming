package pro.datawiki.igaming.source.smarkets.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContract;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsMarket;

import java.util.List;

@Component
@Order(110)
public class SmarketsBttsHandler extends AbstractSmarketsMarketHandler {

    @Override
    public boolean supports(SmarketsMarketContext context) {
        if (context.getMarket() == null) return false;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        return mName.contains("both teams to score") ||
               mtName.contains("btts") ||
               mName.contains("btts");
    }

    @Override
    public void handle(SmarketsMarketContext context, List<OddItem> items) {
        if (context.getContracts() == null) return;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        BetScope scope = resolveScope(mName);

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;

            if (cName.equals("yes") || cName.contains("btts yes")) {
                betType = new BinaryMarketBet(
                        scope,
                        BetSubject.MATCH,
                        BinaryMarketBet.MarketType.BTTS,
                        BinaryMarketBet.Outcome.YES,
                        StatType.MATCH
                );
            } else if (cName.equals("no") || cName.contains("btts no")) {
                betType = new BinaryMarketBet(
                        scope,
                        BetSubject.MATCH,
                        BinaryMarketBet.MarketType.BTTS,
                        BinaryMarketBet.Outcome.NO,
                        StatType.MATCH
                );
            }

            if (betType != null) {
                String groupName = scope == BetScope.FULL_MATCH ? "btts" : ("btts_" + scope.name().toLowerCase());
                addOddItem(items, groupName, contract.getId(), contract.getName(), backOdds, betType);
            }
        }
    }
}
