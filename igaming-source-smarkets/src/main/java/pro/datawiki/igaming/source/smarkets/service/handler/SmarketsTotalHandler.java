package pro.datawiki.igaming.source.smarkets.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContract;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsMarket;

import java.util.List;

@Component
@Order(90)
public class SmarketsTotalHandler extends AbstractSmarketsMarketHandler {

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

        return mtName.contains("over_under") ||
               mName.contains("over/under") ||
               mName.contains("total");
    }

    @Override
    public void handle(SmarketsMarketContext context, List<OddItem> items) {
        if (context.getContracts() == null) return;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String marketParam = market.getMarketType() != null ? market.getMarketType().getParam() : null;
        Double defaultParam = extractParam(marketParam, market.getName());

        BetScope scope = resolveScope(mName);
        BetSubject subject = BetSubject.MATCH;
        if (mName.contains("home") || isMatch(mName, context.getTeam1())) {
            subject = BetSubject.TEAM1;
        } else if (mName.contains("away") || isMatch(mName, context.getTeam2())) {
            subject = BetSubject.TEAM2;
        }

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            Double param = defaultParam != null ? defaultParam : extractParam(null, contract.getName());
            if (param == null) continue;

            boolean isAsian = (param % 0.5 != 0);
            BetType betType = null;
            if (cName.contains("over") || cName.startsWith("o ") || cName.equals("over")) {
                betType = mapTotalRecord("OVER", scope, subject, StatType.MATCH, isAsian, param);
            } else if (cName.contains("under") || cName.startsWith("u ") || cName.equals("under")) {
                betType = mapTotalRecord("UNDER", scope, subject, StatType.MATCH, isAsian, param);
            }

            if (betType != null) {
                String groupName = "total";
                if (subject != BetSubject.MATCH) {
                    groupName += "_" + subject.name().toLowerCase();
                }
                if (scope != BetScope.FULL_MATCH) {
                    groupName += "_" + scope.name().toLowerCase();
                }
                String label = contract.getName() + " (" + param + ")";
                addOddItem(items, groupName, contract.getId(), label, backOdds, betType);
            }
        }
    }
}
