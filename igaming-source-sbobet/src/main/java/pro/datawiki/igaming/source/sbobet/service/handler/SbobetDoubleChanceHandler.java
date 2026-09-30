package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

@Component
public class SbobetDoubleChanceHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String lower = marketKey.toLowerCase();
        return lower.equals("double_chance") || lower.equals("double_chance_half1")
                || lower.equals("doublechance") || lower.equals("dc") || lower.equals("dc_half1");
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null) return;
        boolean isHalf1 = marketNode.has("_isHalf1") || marketNode.path("isHalf1").asBoolean(false)
                || marketNode.path("_key").asText().contains("half1");
        BetScope scope = isHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
        String groupName = isHalf1 ? "double_chance_half_1" : "double_chance";

        if (marketNode.has("1x")) {
            addMatchResult(items, groupName, "1X", marketNode.path("1x").asDouble(),
                    scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH);
        } else if (marketNode.has("home_draw")) {
            addMatchResult(items, groupName, "1X", marketNode.path("home_draw").asDouble(),
                    scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH);
        }

        if (marketNode.has("12")) {
            addMatchResult(items, groupName, "12", marketNode.path("12").asDouble(),
                    scope, MatchResultBet.Outcome.DC_12, StatType.MATCH);
        } else if (marketNode.has("home_away")) {
            addMatchResult(items, groupName, "12", marketNode.path("home_away").asDouble(),
                    scope, MatchResultBet.Outcome.DC_12, StatType.MATCH);
        }

        if (marketNode.has("x2")) {
            addMatchResult(items, groupName, "X2", marketNode.path("x2").asDouble(),
                    scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH);
        } else if (marketNode.has("draw_away")) {
            addMatchResult(items, groupName, "X2", marketNode.path("draw_away").asDouble(),
                    scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH);
        }
    }
}
