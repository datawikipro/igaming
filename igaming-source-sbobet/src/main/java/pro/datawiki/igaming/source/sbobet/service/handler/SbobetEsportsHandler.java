package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.List;

@Component
@Order(20)
public class SbobetEsportsHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String k = marketKey.toLowerCase();
        return k.contains("map") || k.contains("round") || k.contains("kill") || k.contains("first_blood")
                || k.contains("1st_blood") || k.startsWith("esports_");
    }

    @Override
    public boolean supports(String marketKey, SportType sportType) {
        if (isEsports(sportType)) return true;
        return supports(marketKey);
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        handleMarket(null, SportType.ESPORTS, marketNode, items);
    }

    @Override
    public void handle(JsonNode marketNode, SportType sportType, List<OddItem> items) {
        handleMarket(null, sportType, marketNode, items);
    }

    @Override
    public void handle(String marketKey, JsonNode marketNode, SportType sportType, List<OddItem> items) {
        handleMarket(marketKey, sportType, marketNode, items);
    }

    private void handleMarket(String marketKey, SportType sportType, JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null) return;

        String keyStr = marketKey != null ? marketKey.toLowerCase() : "";
        if (keyStr.isEmpty() && marketNode.has("_key")) {
            keyStr = marketNode.path("_key").asText().toLowerCase();
        }

        BetScope scope = resolveScope(keyStr, marketNode);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());

        // 1. Total Maps (Match Scope)
        if (keyStr.contains("map") && (keyStr.contains("total") || keyStr.contains("over")) && !keyStr.contains("round") && scope == BetScope.FULL_MATCH) {
            handleTotals(marketNode, items, "maps_total", scope, StatType.MAPS);
            return;
        }

        // 2. Map Handicap (Match Scope)
        if (keyStr.contains("map") && (keyStr.contains("handicap") || keyStr.contains("hdp")) && !keyStr.contains("round") && scope == BetScope.FULL_MATCH) {
            handleHandicaps(marketNode, items, "maps_handicap", scope, StatType.MAPS);
            return;
        }

        // 3. Total Rounds (Map Scope or Match Scope)
        if (keyStr.contains("round") && (keyStr.contains("total") || keyStr.contains("over"))) {
            handleTotals(marketNode, items, "rounds_total" + scopeSuffix, scope, StatType.ROUNDS);
            return;
        }

        // 4. Round Handicap (Map Scope or Match Scope)
        if (keyStr.contains("round") && (keyStr.contains("handicap") || keyStr.contains("hdp"))) {
            handleHandicaps(marketNode, items, "rounds_handicap" + scopeSuffix, scope, StatType.ROUNDS);
            return;
        }

        // 5. Total Kills
        if (keyStr.contains("kill") && (keyStr.contains("total") || keyStr.contains("over"))) {
            handleTotals(marketNode, items, "kills_total" + scopeSuffix, scope, StatType.KILLS);
            return;
        }

        // 6. First Blood
        if (keyStr.contains("first_blood") || keyStr.contains("1st_blood")) {
            handleFirstBlood(marketNode, items, "first_blood" + scopeSuffix, scope);
            return;
        }

        // 7. Map Winner / Esports Moneyline
        if (keyStr.contains("winner") || keyStr.contains("moneyline") || keyStr.contains("1x2") || keyStr.contains("map")) {
            handleWinner(marketNode, items, (scope == BetScope.FULL_MATCH ? "moneyline" : "map_winner" + scopeSuffix), scope);
            return;
        }

        // 8. General fallback for esports totals
        if (keyStr.contains("total") || keyStr.contains("over")) {
            handleTotals(marketNode, items, "total" + scopeSuffix, scope, StatType.MATCH);
            return;
        }

        // 9. General fallback for esports handicaps
        if (keyStr.contains("handicap") || keyStr.contains("hdp")) {
            handleHandicaps(marketNode, items, "handicap" + scopeSuffix, scope, StatType.MATCH);
        }
    }

    private void handleTotals(JsonNode node, List<OddItem> items, String groupName, BetScope scope, StatType statType) {
        if (node.isArray()) {
            for (JsonNode tNode : node) {
                double limit = tNode.path("limit").asDouble();
                if (tNode.has("over")) {
                    addOddItem(items, groupName, "OVER (" + limit + ")", tNode.path("over").asDouble(),
                            new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, false, statType));
                }
                if (tNode.has("under")) {
                    addOddItem(items, groupName, "UNDER (" + limit + ")", tNode.path("under").asDouble(),
                            new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, false, statType));
                }
            }
        } else if (node.isObject() && node.has("limit")) {
            double limit = node.path("limit").asDouble();
            if (node.has("over")) {
                addOddItem(items, groupName, "OVER (" + limit + ")", node.path("over").asDouble(),
                        new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, false, statType));
            }
            if (node.has("under")) {
                addOddItem(items, groupName, "UNDER (" + limit + ")", node.path("under").asDouble(),
                        new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, false, statType));
            }
        }
    }

    private void handleHandicaps(JsonNode node, List<OddItem> items, String groupName, BetScope scope, StatType statType) {
        if (node.isArray()) {
            for (JsonNode hNode : node) {
                double hdp = hNode.path("hdp").asDouble();
                if (hNode.has("home")) {
                    addOddItem(items, groupName, "HOME (" + hdp + ")", hNode.path("home").asDouble(),
                            new HandicapBet(scope, HandicapBet.Outcome.TEAM1, hdp, false, statType));
                }
                if (hNode.has("away")) {
                    addOddItem(items, groupName, "AWAY (" + (-hdp) + ")", hNode.path("away").asDouble(),
                            new HandicapBet(scope, HandicapBet.Outcome.TEAM2, -hdp, false, statType));
                }
            }
        } else if (node.isObject() && node.has("hdp")) {
            double hdp = node.path("hdp").asDouble();
            if (node.has("home")) {
                addOddItem(items, groupName, "HOME (" + hdp + ")", node.path("home").asDouble(),
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM1, hdp, false, statType));
            }
            if (node.has("away")) {
                addOddItem(items, groupName, "AWAY (" + (-hdp) + ")", node.path("away").asDouble(),
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM2, -hdp, false, statType));
            }
        }
    }

    private void handleWinner(JsonNode node, List<OddItem> items, String groupName, BetScope scope) {
        boolean hasDraw = node.has("draw");
        if (node.has("home")) {
            addOddItem(items, groupName, "HOME", node.path("home").asDouble(),
                    hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, StatType.MATCH)
                            : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
        }
        if (node.has("away")) {
            addOddItem(items, groupName, "AWAY", node.path("away").asDouble(),
                    hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, StatType.MATCH)
                            : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
        }
        if (hasDraw) {
            addOddItem(items, groupName, "DRAW", node.path("draw").asDouble(),
                    new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH));
        }
    }

    private void handleFirstBlood(JsonNode node, List<OddItem> items, String groupName, BetScope scope) {
        if (node.has("home") || node.has("1")) {
            double val = node.has("home") ? node.path("home").asDouble() : node.path("1").asDouble();
            addOddItem(items, groupName, "HOME", val,
                    new BinaryMarketBet(scope, BetSubject.TEAM1, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD));
        }
        if (node.has("away") || node.has("2")) {
            double val = node.has("away") ? node.path("away").asDouble() : node.path("2").asDouble();
            addOddItem(items, groupName, "AWAY", val,
                    new BinaryMarketBet(scope, BetSubject.TEAM2, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD));
        }
    }
}
