package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.List;

@Component
@Order(10)
public class SbobetStatsHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String k = marketKey.toLowerCase();
        return k.contains("corner") || k.contains("card") || k.contains("yellow") || k.contains("booking");
    }

    @Override
    public boolean supports(String marketKey, SportType sportType) {
        return supports(marketKey);
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        // Fallback if sportType not passed
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

        String keyStr = marketKey != null ? marketKey.toLowerCase() : "";
        if (keyStr.isEmpty() && marketNode.has("_key")) {
            keyStr = marketNode.path("_key").asText().toLowerCase();
        }

        StatType statType = keyStr.contains("corner") ? StatType.CORNERS :
                (keyStr.contains("yellow") ? StatType.YELLOW_CARDS : StatType.CARDS);

        BetScope scope = resolveScope(keyStr, marketNode);
        String statPrefix = statType == StatType.CORNERS ? "corners" : "cards";
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());

        boolean isTotal = keyStr.contains("total") || keyStr.contains("over") || marketNode.has("limit")
                || (marketNode.isArray() && marketNode.size() > 0 && marketNode.get(0).has("limit"));
        boolean isHandicap = keyStr.contains("handicap") || keyStr.contains("hdp")
                || (marketNode.isArray() && marketNode.size() > 0 && marketNode.get(0).has("hdp"));

        if (isTotal) {
            String groupName = statPrefix + "_total" + scopeSuffix;
            if (marketNode.isArray()) {
                for (JsonNode tNode : marketNode) {
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
            } else if (marketNode.isObject() && marketNode.has("limit")) {
                double limit = marketNode.path("limit").asDouble();
                if (marketNode.has("over")) {
                    addOddItem(items, groupName, "OVER (" + limit + ")", marketNode.path("over").asDouble(),
                            new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, false, statType));
                }
                if (marketNode.has("under")) {
                    addOddItem(items, groupName, "UNDER (" + limit + ")", marketNode.path("under").asDouble(),
                            new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, false, statType));
                }
            }
        } else if (isHandicap) {
            String groupName = statPrefix + "_handicap" + scopeSuffix;
            if (marketNode.isArray()) {
                for (JsonNode hNode : marketNode) {
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
            } else if (marketNode.isObject() && marketNode.has("hdp")) {
                double hdp = marketNode.path("hdp").asDouble();
                if (marketNode.has("home")) {
                    addOddItem(items, groupName, "HOME (" + hdp + ")", marketNode.path("home").asDouble(),
                            new HandicapBet(scope, HandicapBet.Outcome.TEAM1, hdp, false, statType));
                }
                if (marketNode.has("away")) {
                    addOddItem(items, groupName, "AWAY (" + (-hdp) + ")", marketNode.path("away").asDouble(),
                            new HandicapBet(scope, HandicapBet.Outcome.TEAM2, -hdp, false, statType));
                }
            }
        } else {
            // 1X2 / Moneyline for stats
            String groupName = statPrefix + "_1x2" + scopeSuffix;
            boolean hasDraw = marketNode.has("draw");
            if (marketNode.has("home")) {
                addOddItem(items, groupName, "HOME", marketNode.path("home").asDouble(),
                        hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, statType)
                                : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, statType));
            }
            if (marketNode.has("away")) {
                addOddItem(items, groupName, "AWAY", marketNode.path("away").asDouble(),
                        hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, statType)
                                : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, statType));
            }
            if (hasDraw) {
                addOddItem(items, groupName, "DRAW", marketNode.path("draw").asDouble(),
                        new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, statType));
            }
        }
    }
}
