package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
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
        if (lower.contains("corner") || lower.contains("card") || lower.contains("booking") || lower.contains("esport")) {
            return false;
        }
        return lower.equals("btts") || lower.equals("both_teams_to_score")
                || lower.equals("btts_half1") || lower.equals("both_teams_to_score_half1")
                || lower.equals("btts_half_1") || lower.equals("both_teams_to_score_half_1")
                || lower.equals("gg_ng") || lower.equals("ggng")
                || lower.contains("btts") || lower.contains("both_teams_to_score")
                || lower.contains("both_team_to_score");
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        handle(null, marketNode, null, items);
    }

    @Override
    public void handle(String marketKey, JsonNode marketNode, SportType sportType, List<OddItem> items) {
        if (marketNode == null) return;

        if (marketNode.isArray()) {
            for (JsonNode node : marketNode) {
                processSingleNode(marketKey, node, items);
            }
        } else {
            processSingleNode(marketKey, marketNode, items);
        }
    }

    private void processSingleNode(String marketKey, JsonNode node, List<OddItem> items) {
        if (node == null || !node.isObject()) return;

        boolean half1 = isHalf1(marketKey, node);
        BetScope scope = half1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
        String groupName = half1 ? "btts_half_1" : "btts";

        // Support prices array if present
        JsonNode prices = node.path("prices");
        if (prices.isArray()) {
            for (JsonNode p : prices) {
                String designation = p.path("designation").asText("").toLowerCase();
                if (designation.isEmpty()) designation = p.path("name").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                mapPriceOutcome(items, groupName, scope, designation, price);
            }
            return;
        }

        double valYes = extractDouble(node, "yes", "y", "true", "gg", "both_teams_score", "yes_both_teams_to_score");
        if (valYes > 1.0) {
            addBinary(items, groupName, "YES", valYes,
                    scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS,
                    BinaryMarketBet.Outcome.YES, StatType.MATCH);
        }

        double valNo = extractDouble(node, "no", "n", "false", "ng", "one_team_not_score", "no_both_teams_to_score");
        if (valNo > 1.0) {
            addBinary(items, groupName, "NO", valNo,
                    scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS,
                    BinaryMarketBet.Outcome.NO, StatType.MATCH);
        }
    }

    private void mapPriceOutcome(List<OddItem> items, String groupName, BetScope scope, String designation, double price) {
        if (price <= 1.0) return;
        switch (designation) {
            case "yes", "y", "true", "gg" ->
                addBinary(items, groupName, "YES", price,
                        scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS,
                        BinaryMarketBet.Outcome.YES, StatType.MATCH);
            case "no", "n", "false", "ng" ->
                addBinary(items, groupName, "NO", price,
                        scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS,
                        BinaryMarketBet.Outcome.NO, StatType.MATCH);
        }
    }
}
