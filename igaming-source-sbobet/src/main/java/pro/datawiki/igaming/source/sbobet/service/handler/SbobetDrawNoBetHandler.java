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
        return (k.contains("draw_no_bet") || k.contains("drawnobet") || k.equals("dnb") || k.startsWith("dnb_")) && !isStats(k);
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
        String groupName = isHalf1 ? "draw_no_bet_half_1" : "draw_no_bet";

        if (marketNode.isObject()) {
            if (marketNode.has("home")) {
                addOddItem(items, groupName, "HOME", marketNode.path("home").asDouble(),
                        new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
            } else if (marketNode.has("1")) {
                addOddItem(items, groupName, "HOME", marketNode.path("1").asDouble(),
                        new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
            }
            if (marketNode.has("away")) {
                addOddItem(items, groupName, "AWAY", marketNode.path("away").asDouble(),
                        new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
            } else if (marketNode.has("2")) {
                addOddItem(items, groupName, "AWAY", marketNode.path("2").asDouble(),
                        new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
            }
        } else if (marketNode.isArray()) {
            for (JsonNode itemNode : marketNode) {
                String name = itemNode.path("name").asText().toUpperCase();
                double val = itemNode.path("odds").asDouble(itemNode.path("value").asDouble(0.0));
                if (name.contains("HOME") || name.equals("1")) {
                    addOddItem(items, groupName, "HOME", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
                } else if (name.contains("AWAY") || name.equals("2")) {
                    addOddItem(items, groupName, "AWAY", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
                }
            }
        }
    }
}
