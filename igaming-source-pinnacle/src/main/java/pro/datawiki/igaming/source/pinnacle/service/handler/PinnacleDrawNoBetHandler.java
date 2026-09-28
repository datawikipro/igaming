package pro.datawiki.igaming.source.pinnacle.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

@Component
public class PinnacleDrawNoBetHandler extends AbstractPinnacleMarketHandler {

    @Override
    public boolean supports(String marketType) {
        return "draw_no_bet".equalsIgnoreCase(marketType) || "dnb".equalsIgnoreCase(marketType);
    }

    @Override
    public void handle(JsonNode market, BetScope scope, String scopeSuffix, List<OddItem> items) {
        JsonNode prices = market.path("prices");
        if (!prices.isArray()) return;

        String groupName = "draw_no_bet" + scopeSuffix;
        for (JsonNode p : prices) {
            String designation = p.path("designation").asText().toLowerCase();
            double rawPrice = p.path("price").asDouble();
            double decimalValue = americanToDecimal(rawPrice);

            if ("home".equals(designation)) {
                addOddItem(items, groupName, "HOME", decimalValue,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM1, 0.0, false, StatType.MATCH));
            } else if ("away".equals(designation)) {
                addOddItem(items, groupName, "AWAY", decimalValue,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM2, 0.0, false, StatType.MATCH));
            }
        }
    }
}
