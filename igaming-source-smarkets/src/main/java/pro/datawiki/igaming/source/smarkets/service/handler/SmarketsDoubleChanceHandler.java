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
@Order(70)
public class SmarketsDoubleChanceHandler extends AbstractSmarketsMarketHandler {

    @Override
    public boolean supports(SmarketsMarketContext context) {
        if (context.getMarket() == null) return false;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        return mName.contains("double chance") || mtName.contains("double_chance");
    }

    @Override
    public void handle(SmarketsMarketContext context, List<OddItem> items) {
        if (context.getContracts() == null) return;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        BetScope scope = resolveScope(mName);

        String team1 = context.getTeam1() != null ? context.getTeam1().toLowerCase() : "";
        String team2 = context.getTeam2() != null ? context.getTeam2().toLowerCase() : "";

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;

            if (cName.equals("1x") || cName.contains("home or draw") ||
                (cName.contains("draw") && (isMatch(cName, team1) || cName.contains("1")))) {
                betType = map1X2DCRecord("1X", scope, StatType.MATCH);
            } else if (cName.equals("12") || cName.contains("home or away") ||
                       (isMatch(cName, team1) && isMatch(cName, team2))) {
                betType = map1X2DCRecord("12", scope, StatType.MATCH);
            } else if (cName.equals("x2") || cName.contains("draw or away") ||
                       (cName.contains("draw") && (isMatch(cName, team2) || cName.contains("2")))) {
                betType = map1X2DCRecord("X2", scope, StatType.MATCH);
            }

            if (betType != null) {
                String groupName = scope == BetScope.FULL_MATCH ? "double_chance" : ("double_chance_" + scope.name().toLowerCase());
                addOddItem(items, groupName, contract.getId(), contract.getName(), backOdds, betType);
            }
        }
    }
}
