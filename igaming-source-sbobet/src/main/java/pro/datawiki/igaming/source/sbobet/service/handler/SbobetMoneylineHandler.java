package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

@Component
public class SbobetMoneylineHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        return "moneyline".equalsIgnoreCase(marketKey) || "moneyline_half1".equalsIgnoreCase(marketKey);
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null) return;
        boolean isHalf1 = marketNode.has("_isHalf1") || "moneyline_half1".equals(marketNode.path("_key").asText());
        BetScope scope = isHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
        String groupName = isHalf1 ? "moneyline_half_1" : "moneyline";

        if (marketNode.has("home")) {
            addOddItem(items, groupName, "HOME", marketNode.path("home").asDouble(),
                    new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, StatType.MATCH));
        }
        if (marketNode.has("away")) {
            addOddItem(items, groupName, "AWAY", marketNode.path("away").asDouble(),
                    new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, StatType.MATCH));
        }
        if (marketNode.has("draw")) {
            addOddItem(items, groupName, "DRAW", marketNode.path("draw").asDouble(),
                    new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH));
        }
    }
}
