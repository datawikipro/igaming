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
        return (k.contains("btts") || k.contains("both_teams_to_score") || k.contains("bothteamstoscore")) && !isStats(k);
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
        String groupName = isHalf1 ? "btts_half_1" : "btts";

        if (marketNode.isObject()) {
            if (marketNode.has("yes")) {
                addOddItem(items, groupName, "YES", marketNode.path("yes").asDouble(),
                        new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, StatType.MATCH));
            }
            if (marketNode.has("no")) {
                addOddItem(items, groupName, "NO", marketNode.path("no").asDouble(),
                        new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, StatType.MATCH));
            }
        } else if (marketNode.isArray()) {
            for (JsonNode itemNode : marketNode) {
                String name = itemNode.path("name").asText().toUpperCase();
                double val = itemNode.path("odds").asDouble(itemNode.path("value").asDouble(0.0));
                if (name.contains("YES")) {
                    addOddItem(items, groupName, "YES", val,
                            new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, StatType.MATCH));
                } else if (name.contains("NO")) {
                    addOddItem(items, groupName, "NO", val,
                            new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, StatType.MATCH));
                }
            }
        }
    }
}
