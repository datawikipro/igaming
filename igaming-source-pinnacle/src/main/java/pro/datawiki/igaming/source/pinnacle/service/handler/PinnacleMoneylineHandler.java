package pro.datawiki.igaming.source.pinnacle.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

@Component
public class PinnacleMoneylineHandler extends AbstractPinnacleMarketHandler {

    @Override
    public boolean supports(String marketType) {
        return "moneyline".equalsIgnoreCase(marketType);
    }

    @Override
    public void handle(JsonNode market, BetScope scope, String scopeSuffix, List<OddItem> items) {
        JsonNode prices = market.path("prices");
        if (!prices.isArray()) return;

        String groupName = "moneyline" + scopeSuffix;
        for (JsonNode p : prices) {
            String designation = p.path("designation").asText().toLowerCase();
            double rawPrice = p.path("price").asDouble();
            double decimalValue = americanToDecimal(rawPrice);

            switch (designation) {
                case "home" -> addOddItem(items, groupName, "HOME", decimalValue,
                        new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, StatType.MATCH));
                case "away" -> addOddItem(items, groupName, "AWAY", decimalValue,
                        new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, StatType.MATCH));
                case "draw" -> addOddItem(items, groupName, "DRAW", decimalValue,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH));
            }
        }
    }
}
