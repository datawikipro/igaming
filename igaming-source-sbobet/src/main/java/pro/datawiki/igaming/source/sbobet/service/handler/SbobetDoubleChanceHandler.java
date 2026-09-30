package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
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
                || lower.equals("double_chance_half_1") || lower.equals("doublechance")
                || lower.equals("dc") || lower.equals("dc_half1") || lower.equals("dc_half_1")
                || lower.contains("double_chance") || lower.startsWith("dc_");
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
        String groupName = half1 ? "double_chance_half_1" : "double_chance";

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

        double val1x = extractDouble(node, "1x", "1X", "home_draw", "homeDraw", "homedraw", "1_x", "dc_1x", "hd");
        if (val1x > 1.0) {
            addMatchResult(items, groupName, "1X", val1x, scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH);
        }

        double val12 = extractDouble(node, "12", "home_away", "homeAway", "homeaway", "1_2", "dc_12", "ha");
        if (val12 > 1.0) {
            addMatchResult(items, groupName, "12", val12, scope, MatchResultBet.Outcome.DC_12, StatType.MATCH);
        }

        double valX2 = extractDouble(node, "x2", "X2", "draw_away", "drawAway", "drawaway", "away_draw", "awayDraw", "x_2", "2x", "2X", "dc_x2", "da");
        if (valX2 > 1.0) {
            addMatchResult(items, groupName, "X2", valX2, scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH);
        }
    }

    private void mapPriceOutcome(List<OddItem> items, String groupName, BetScope scope, String designation, double price) {
        if (price <= 1.0) return;
        switch (designation) {
            case "1x", "home_draw", "homedraw", "hd" ->
                addMatchResult(items, groupName, "1X", price, scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH);
            case "12", "home_away", "homeaway", "ha" ->
                addMatchResult(items, groupName, "12", price, scope, MatchResultBet.Outcome.DC_12, StatType.MATCH);
            case "x2", "draw_away", "drawaway", "away_draw", "da", "2x" ->
                addMatchResult(items, groupName, "X2", price, scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH);
        }
    }
}
