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
@Order(80)
public class SmarketsDrawNoBetHandler extends AbstractSmarketsMarketHandler {

    @Override
    public boolean supports(SmarketsMarketContext context) {
        if (context.getMarket() == null) return false;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        return mName.contains("draw no bet") || mtName.contains("draw_no_bet") || mName.contains("dnb");
    }

    @Override
    public void handle(SmarketsMarketContext context, List<OddItem> items) {
        if (context.getContracts() == null) return;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        BetScope scope = resolveScope(mName);

        String team1 = context.getTeam1();
        String team2 = context.getTeam2();

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;

            if (isMatch(cName, team1) || cName.equals("1") || cName.contains("home")) {
                betType = mapHandicapRecord("1", scope, StatType.MATCH, false, 0.0);
            } else if (isMatch(cName, team2) || cName.equals("2") || cName.contains("away")) {
                betType = mapHandicapRecord("2", scope, StatType.MATCH, false, 0.0);
            }

            if (betType != null) {
                String groupName = scope == BetScope.FULL_MATCH ? "draw_no_bet" : ("draw_no_bet_" + scope.name().toLowerCase());
                addOddItem(items, groupName, contract.getId(), contract.getName(), backOdds, betType);
            }
        }
    }
}
