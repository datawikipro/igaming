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
@Order(100)
public class SmarketsHandicapHandler extends AbstractSmarketsMarketHandler {

    @Override
    public boolean supports(SmarketsMarketContext context) {
        if (context.getMarket() == null) return false;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";

        if (mName.contains("corner") || mName.contains("card") || mName.contains("booking")) {
            return false;
        }

        if (isEsports(context.getSportType())) {
            return false;
        }

        return mtName.contains("handicap") ||
               mName.contains("handicap") ||
               mName.contains("asian handicap") ||
               mName.contains("spread");
    }

    @Override
    public void handle(SmarketsMarketContext context, List<OddItem> items) {
        if (context.getContracts() == null) return;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String marketParam = market.getMarketType() != null ? market.getMarketType().getParam() : null;
        Double defaultParam = extractParam(marketParam, market.getName());

        BetScope scope = resolveScope(mName);
        String team1 = context.getTeam1();
        String team2 = context.getTeam2();

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            Double param = extractParam(null, contract.getName());
            if (param == null) {
                param = defaultParam;
            }
            if (param == null) continue;

            boolean isAsian = (param % 0.5 != 0);
            BetType betType = null;

            if (isMatch(cName, team1) || cName.contains("home") || cName.startsWith("1")) {
                betType = mapHandicapRecord("1", scope, StatType.MATCH, isAsian, param);
            } else if (isMatch(cName, team2) || cName.contains("away") || cName.startsWith("2")) {
                betType = mapHandicapRecord("2", scope, StatType.MATCH, isAsian, param);
            }

            if (betType != null) {
                String groupName = scope == BetScope.FULL_MATCH ? "handicap" : ("handicap_" + scope.name().toLowerCase());
                String label = contract.getName() + " (" + param + ")";
                addOddItem(items, groupName, contract.getId(), label, backOdds, betType);
            }
        }
    }
}
