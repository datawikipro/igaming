package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
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
        return lower.equals("draw_no_bet") || lower.equals("dnb")
                || lower.equals("draw_no_bet_half1") || lower.equals("dnb_half1")
                || lower.equals("draw_no_bet_half_1") || lower.equals("dnb_half_1")
                || lower.equals("tie_break_no_bet") || lower.equals("tie_no_bet")
                || lower.contains("draw_no_bet") || lower.startsWith("dnb_");
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
        String groupName = half1 ? "draw_no_bet_half_1" : "draw_no_bet";

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

        double valHome = extractDouble(node, "home", "1", "h", "team1", "dnb_home", "dnb_1", "home_team");
        if (valHome > 1.0) {
            addHandicap(items, groupName, "HOME (0.0)", valHome,
                    scope, HandicapBet.Outcome.TEAM1, 0.0, true, StatType.MATCH);
        }

        double valAway = extractDouble(node, "away", "2", "a", "team2", "dnb_away", "dnb_2", "away_team");
        if (valAway > 1.0) {
            addHandicap(items, groupName, "AWAY (0.0)", valAway,
                    scope, HandicapBet.Outcome.TEAM2, 0.0, true, StatType.MATCH);
        }
    }

    private void mapPriceOutcome(List<OddItem> items, String groupName, BetScope scope, String designation, double price) {
        if (price <= 1.0) return;
        switch (designation) {
            case "home", "1", "h", "team1" ->
                addHandicap(items, groupName, "HOME (0.0)", price,
                        scope, HandicapBet.Outcome.TEAM1, 0.0, true, StatType.MATCH);
            case "away", "2", "a", "team2" ->
                addHandicap(items, groupName, "AWAY (0.0)", price,
                        scope, HandicapBet.Outcome.TEAM2, 0.0, true, StatType.MATCH);
        }
    }
}
