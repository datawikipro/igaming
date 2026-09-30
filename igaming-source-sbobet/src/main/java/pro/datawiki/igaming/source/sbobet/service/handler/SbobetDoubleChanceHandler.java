package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

@Component
@Order(30)
public class SbobetDoubleChanceHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String k = marketKey.toLowerCase();
        return (k.contains("double_chance") || k.contains("doublechance") || k.equals("dc") || k.startsWith("dc_")) && !isStats(k);
    }

    @Override
    public boolean supports(String marketKey, SportType sportType) {
        return supports(marketKey);
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null) return;
        boolean isHalf1 = marketNode.has("_isHalf1") || marketNode.path("isHalf1").asBoolean(false);
        BetScope scope = isHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
        String groupName = isHalf1 ? "double_chance_half_1" : "double_chance";

        if (marketNode.isObject()) {
            marketNode.fields().forEachRemaining(entry -> {
                String k = entry.getKey().toLowerCase().replace("-", "_").replace(" ", "_");
                double val = entry.getValue().asDouble(0.0);
                if (k.equals("1x") || k.contains("home_draw") || k.contains("homedraw")) {
                    addOddItem(items, groupName, "1X", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH));
                } else if (k.equals("12") || k.contains("home_away") || k.contains("homeaway")) {
                    addOddItem(items, groupName, "12", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, StatType.MATCH));
                } else if (k.equals("x2") || k.contains("draw_away") || k.contains("drawaway")) {
                    addOddItem(items, groupName, "X2", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH));
                }
            });
        } else if (marketNode.isArray()) {
            for (JsonNode itemNode : marketNode) {
                String name = itemNode.path("name").asText().toUpperCase();
                double val = itemNode.path("odds").asDouble(itemNode.path("value").asDouble(0.0));
                if (name.contains("1X") || name.contains("HOME/DRAW")) {
                    addOddItem(items, groupName, "1X", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH));
                } else if (name.contains("12") || name.contains("HOME/AWAY")) {
                    addOddItem(items, groupName, "12", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, StatType.MATCH));
                } else if (name.contains("X2") || name.contains("DRAW/AWAY")) {
                    addOddItem(items, groupName, "X2", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH));
                }
            }
        }
    }
}
