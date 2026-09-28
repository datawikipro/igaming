package pro.datawiki.igaming.source.pinnacle.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

@Component
public class PinnacleDoubleChanceHandler extends AbstractPinnacleMarketHandler {

    @Override
    public boolean supports(String marketType) {
        return "double_chance".equalsIgnoreCase(marketType);
    }

    @Override
    public void handle(JsonNode market, BetScope scope, String scopeSuffix, List<OddItem> items) {
        JsonNode prices = market.path("prices");
        if (!prices.isArray()) return;

        String groupName = "double_chance" + scopeSuffix;
        for (JsonNode p : prices) {
            String designation = p.path("designation").asText().toLowerCase();
            double rawPrice = p.path("price").asDouble();
            double decimalValue = americanToDecimal(rawPrice);

            switch (designation) {
                case "home_draw", "1x" -> addOddItem(items, groupName, "1X", decimalValue,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH));
                case "home_away", "12" -> addOddItem(items, groupName, "12", decimalValue,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, StatType.MATCH));
                case "away_draw", "draw_away", "x2" -> addOddItem(items, groupName, "X2", decimalValue,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH));
            }
        }
    }
}
