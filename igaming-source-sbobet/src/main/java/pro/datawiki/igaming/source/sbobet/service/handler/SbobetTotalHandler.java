package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.List;

@Component
public class SbobetTotalHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        return "totals".equalsIgnoreCase(marketKey) || "totals_half1".equalsIgnoreCase(marketKey);
    }

    @Override
    public boolean supports(String marketKey, pro.datawiki.igaming.dto.SportType sportType) {
        if (SbobetEsportsHandler.isEsports(sportType)) {
            return false;
        }
        return supports(marketKey);
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null || !marketNode.isArray()) return;

        for (JsonNode totalNode : marketNode) {
            boolean isHalf1 = totalNode.path("isHalf1").asBoolean(false);
            BetScope scope = isHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
            String groupName = isHalf1 ? "total_half_1" : "total";
            double limit = totalNode.path("limit").asDouble();

            addOddItem(items, groupName, "OVER (" + limit + ")", totalNode.path("over").asDouble(),
                    new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, true, StatType.MATCH));
            addOddItem(items, groupName, "UNDER (" + limit + ")", totalNode.path("under").asDouble(),
                    new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, true, StatType.MATCH));
        }
    }
}
