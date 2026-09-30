package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

@Component
@Order(50)
public class SbobetBttsHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String k = marketKey.toLowerCase();
        return (k.contains("btts") || k.contains("both_teams_to_score") || k.contains("bothteamstoscore")
                || k.contains("both teams to score") || k.contains("gg_ng")) && !isStats(k);
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
        String groupName = "btts" + scopeSuffix;

        if (marketNode.isObject()) {
            marketNode.fields().forEachRemaining(entry -> {
                String k = entry.getKey().toLowerCase().trim();
                double val = entry.getValue().asDouble(0.0);
                if (k.equals("yes") || k.equals("y") || k.equals("gg") || k.contains("btts_yes")) {
                    addOddItem(items, groupName, "YES", val,
                            new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, StatType.MATCH));
                } else if (k.equals("no") || k.equals("n") || k.equals("ng") || k.contains("btts_no")) {
                    addOddItem(items, groupName, "NO", val,
                            new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, StatType.MATCH));
                }
            });
        } else if (marketNode.isArray()) {
            for (JsonNode itemNode : marketNode) {
                String name = itemNode.path("name").asText().toUpperCase().trim();
                double val = itemNode.path("odds").asDouble(itemNode.path("value").asDouble(0.0));
                if (name.contains("YES") || name.equals("Y") || name.equals("GG")) {
                    addOddItem(items, groupName, "YES", val,
                            new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, StatType.MATCH));
                } else if (name.contains("NO") || name.equals("N") || name.equals("NG")) {
                    addOddItem(items, groupName, "NO", val,
                            new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, StatType.MATCH));
                }
            }
        }
    }
}
