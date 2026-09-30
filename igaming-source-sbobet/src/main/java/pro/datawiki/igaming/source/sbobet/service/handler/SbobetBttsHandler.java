package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

@Component
public class SbobetBttsHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String lower = marketKey.toLowerCase();
        return lower.equals("btts") || lower.equals("both_teams_to_score")
                || lower.equals("btts_half1") || lower.equals("both_teams_to_score_half1");
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null) return;
        boolean isHalf1 = marketNode.has("_isHalf1") || marketNode.path("isHalf1").asBoolean(false)
                || marketNode.path("_key").asText().contains("half1");
        BetScope scope = isHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
        String groupName = isHalf1 ? "btts_half_1" : "btts";

        if (marketNode.has("yes")) {
            addBinary(items, groupName, "YES", marketNode.path("yes").asDouble(),
                    scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS,
                    BinaryMarketBet.Outcome.YES, StatType.MATCH);
        }
        if (marketNode.has("no")) {
            addBinary(items, groupName, "NO", marketNode.path("no").asDouble(),
                    scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS,
                    BinaryMarketBet.Outcome.NO, StatType.MATCH);
        }
    }
}
