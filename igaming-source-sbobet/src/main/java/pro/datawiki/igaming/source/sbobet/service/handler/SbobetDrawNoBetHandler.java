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
@Order(40)
public class SbobetDrawNoBetHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String k = marketKey.toLowerCase();
        return (k.contains("draw_no_bet") || k.contains("drawnobet") || k.contains("draw no bet")
                || k.equals("dnb") || k.startsWith("dnb_") || k.endsWith("_dnb")) && !isStats(k);
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
        String groupName = "draw_no_bet" + scopeSuffix;

        if (marketNode.isObject()) {
            marketNode.fields().forEachRemaining(entry -> {
                String k = entry.getKey().toLowerCase().trim();
                double val = entry.getValue().asDouble(0.0);
                if (k.equals("home") || k.equals("1") || k.equals("team1") || k.equals("win1") || k.equals("h")) {
                    addOddItem(items, groupName, "HOME", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
                } else if (k.equals("away") || k.equals("2") || k.equals("team2") || k.equals("win2") || k.equals("a")) {
                    addOddItem(items, groupName, "AWAY", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
                }
            });
        } else if (marketNode.isArray()) {
            for (JsonNode itemNode : marketNode) {
                String name = itemNode.path("name").asText().toUpperCase().trim();
                double val = itemNode.path("odds").asDouble(itemNode.path("value").asDouble(0.0));
                if (name.contains("HOME") || name.equals("1") || name.startsWith("1 ") || name.endsWith(" 1")) {
                    addOddItem(items, groupName, "HOME", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
                } else if (name.contains("AWAY") || name.equals("2") || name.startsWith("2 ") || name.endsWith(" 2")) {
                    addOddItem(items, groupName, "AWAY", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
                }
            }
        }
    }
}
