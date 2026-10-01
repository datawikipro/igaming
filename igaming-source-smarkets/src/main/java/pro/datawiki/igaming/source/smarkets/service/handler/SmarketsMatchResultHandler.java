package pro.datawiki.igaming.source.smarkets.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContract;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsMarket;

import java.util.List;

@Component
@Order(60)
public class SmarketsMatchResultHandler extends AbstractSmarketsMarketHandler {

    @Override
    public boolean supports(SmarketsMarketContext context) {
        if (context.getMarket() == null) return false;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        if (mName.contains("corner") || mName.contains("card") || mName.contains("booking") ||
            mName.contains("handicap") || mName.contains("double chance") || mName.contains("draw no bet")) {
            return false;
        }

        return mtName.contains("winner_3_way") ||
               mtName.contains("winner_2_way") ||
               mName.contains("full-time result") ||
               mName.contains("match winner") ||
               mName.contains("moneyline") ||
               mName.equals("winner");
    }

    @Override
    public void handle(SmarketsMarketContext context, List<OddItem> items) {
        if (context.getContracts() == null) return;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        boolean allowsDraw = mtName.contains("winner_3_way") || mName.contains("full-time result");
        BetScope scope = resolveScope(mName);

        String team1 = context.getTeam1();
        String team2 = context.getTeam2();

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;

            if (cName.contains("draw") || cName.equals("x") || cName.equals("tie")) {
                if (allowsDraw) {
                    betType = map1X2Record("X", scope, StatType.MATCH);
                }
            } else if (isMatch(cName, team1) || cName.equals("1") || cName.contains("home")) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isMatch(cName, team2) || cName.equals("2") || cName.contains("away")) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            }

            if (betType != null) {
                String groupName = scope == BetScope.FULL_MATCH ? "moneyline" : ("moneyline_" + scope.name().toLowerCase());
                addOddItem(items, groupName, contract.getId(), contract.getName(), backOdds, betType);
            }
        }
    }
}
