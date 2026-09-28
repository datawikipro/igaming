package pro.datawiki.igaming.source.pinnacle.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.List;

@Component
public class PinnacleTeamTotalHandler extends AbstractPinnacleMarketHandler {

    @Override
    public boolean supports(String marketType) {
        return "team_total".equalsIgnoreCase(marketType);
    }

    @Override
    public void handle(JsonNode market, BetScope scope, String scopeSuffix, List<OddItem> items) {
        JsonNode prices = market.path("prices");
        if (!prices.isArray()) return;

        String side = market.path("side").asText().toLowerCase();
        BetSubject subject = "home".equals(side) ? BetSubject.TEAM1 : BetSubject.TEAM2;
        String groupName = "team_total" + scopeSuffix + "_" + side;

        for (JsonNode p : prices) {
            String designation = p.path("designation").asText().toLowerCase();
            double points = p.path("points").asDouble();
            double rawPrice = p.path("price").asDouble();
            double decimalValue = americanToDecimal(rawPrice);

            if ("over".equals(designation)) {
                addOddItem(items, groupName, "OVER (" + points + ")", decimalValue,
                        new TotalBet(scope, subject, TotalBet.Direction.OVER, points, false, StatType.MATCH));
            } else if ("under".equals(designation)) {
                addOddItem(items, groupName, "UNDER (" + points + ")", decimalValue,
                        new TotalBet(scope, subject, TotalBet.Direction.UNDER, points, false, StatType.MATCH));
            }
        }
    }
}
