package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

@Component
public class SbobetDrawNoBetHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String lower = marketKey.toLowerCase();
        return lower.equals("draw_no_bet") || lower.equals("dnb") || lower.equals("draw_no_bet_half1");
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null) return;
        boolean isHalf1 = marketNode.has("_isHalf1") || marketNode.path("isHalf1").asBoolean(false)
                || marketNode.path("_key").asText().contains("half1");
        BetScope scope = isHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
        String groupName = isHalf1 ? "draw_no_bet_half_1" : "draw_no_bet";

        if (marketNode.has("home") || marketNode.has("1")) {
            double val = marketNode.has("home") ? marketNode.path("home").asDouble() : marketNode.path("1").asDouble();
            addHandicap(items, groupName, "HOME (0.0)", val, scope, HandicapBet.Outcome.TEAM1, 0.0, true, StatType.MATCH);
        }
        if (marketNode.has("away") || marketNode.has("2")) {
            double val = marketNode.has("away") ? marketNode.path("away").asDouble() : marketNode.path("2").asDouble();
            addHandicap(items, groupName, "AWAY (0.0)", val, scope, HandicapBet.Outcome.TEAM2, 0.0, true, StatType.MATCH);
        }
    }
}
