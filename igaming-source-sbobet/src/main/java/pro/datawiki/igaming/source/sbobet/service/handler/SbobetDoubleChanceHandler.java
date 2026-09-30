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
        return (k.contains("double_chance") || k.contains("doublechance") || k.contains("double chance")
                || k.equals("dc") || k.startsWith("dc_") || k.endsWith("_dc")) && !isStats(k);
    }

    @Override
    public boolean supports(String marketKey, SportType sportType) {
        return supports(marketKey);
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        handleMarket(null, marketNode, items);
    }

    @Override
    public void handle(JsonNode marketNode, SportType sportType, List<OddItem> items) {
        handleMarket(null, marketNode, items);
    }

    @Override
    public void handle(String marketKey, JsonNode marketNode, SportType sportType, List<OddItem> items) {
        handleMarket(marketKey, marketNode, items);
    }

    private void handleMarket(String marketKey, JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null) return;
        String keyStr = marketKey != null ? marketKey : marketNode.path("_key").asText("");
        BetScope scope = resolveScope(keyStr, marketNode);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
        String groupName = "double_chance" + scopeSuffix;

        if (marketNode.isObject()) {
            marketNode.fields().forEachRemaining(entry -> {
                String k = entry.getKey().toLowerCase().replace("-", "_").replace(" ", "_");
                double val = entry.getValue().asDouble(0.0);
                if (k.equals("1x") || k.contains("home_draw") || k.contains("homedraw") || k.equals("1_x") || k.contains("home_or_draw")) {
                    addOddItem(items, groupName, "1X", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH));
                } else if (k.equals("12") || k.contains("home_away") || k.contains("homeaway") || k.equals("1_2") || k.contains("home_or_away")) {
                    addOddItem(items, groupName, "12", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, StatType.MATCH));
                } else if (k.equals("x2") || k.contains("draw_away") || k.contains("drawaway") || k.equals("x_2") || k.contains("draw_or_away")) {
                    addOddItem(items, groupName, "X2", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH));
                }
            });
        } else if (marketNode.isArray()) {
            for (JsonNode itemNode : marketNode) {
                String name = itemNode.path("name").asText().toUpperCase();
                double val = itemNode.path("odds").asDouble(itemNode.path("value").asDouble(0.0));
                if (name.contains("1X") || name.contains("HOME/DRAW") || name.contains("HOME OR DRAW") || name.contains("1 OR X")) {
                    addOddItem(items, groupName, "1X", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH));
                } else if (name.contains("12") || name.contains("HOME/AWAY") || name.contains("HOME OR AWAY") || name.contains("1 OR 2")) {
                    addOddItem(items, groupName, "12", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, StatType.MATCH));
                } else if (name.contains("X2") || name.contains("DRAW/AWAY") || name.contains("DRAW OR AWAY") || name.contains("X OR 2")) {
                    addOddItem(items, groupName, "X2", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH));
                }
            }
        }
    }
}
