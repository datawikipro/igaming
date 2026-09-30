package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.List;

public abstract class AbstractSbobetStatsHandler extends AbstractSbobetMarketHandler {

    protected abstract StatType getStatType();
    protected abstract String getStatPrefix();

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null) return;
        StatType statType = getStatType();
        String prefix = getStatPrefix();

        boolean isHalf1 = marketNode.has("_isHalf1") || marketNode.path("isHalf1").asBoolean(false);
        BetScope scope = isHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
        String scopeSuffix = isHalf1 ? "_half_1" : "";

        // 1X2 / Moneyline
        if (marketNode.has("home")) {
            addMatchResult(items, prefix + "_moneyline" + scopeSuffix, "HOME",
                    marketNode.path("home").asDouble(), scope, MatchResultBet.Outcome.WIN1, statType);
        }
        if (marketNode.has("draw")) {
            addMatchResult(items, prefix + "_moneyline" + scopeSuffix, "DRAW",
                    marketNode.path("draw").asDouble(), scope, MatchResultBet.Outcome.DRAW, statType);
        }
        if (marketNode.has("away")) {
            addMatchResult(items, prefix + "_moneyline" + scopeSuffix, "AWAY",
                    marketNode.path("away").asDouble(), scope, MatchResultBet.Outcome.WIN2, statType);
        }

        // Totals
        JsonNode totalsNode = marketNode.has("totals") ? marketNode.get("totals") : (marketNode.isArray() ? marketNode : null);
        if (totalsNode != null && totalsNode.isArray()) {
            for (JsonNode tNode : totalsNode) {
                boolean tHalf1 = isHalf1 || tNode.path("isHalf1").asBoolean(false);
                BetScope tScope = tHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
                String tGroupName = prefix + "_total" + (tHalf1 ? "_half_1" : "");
                double limit = tNode.path("limit").asDouble();

                if (tNode.has("over")) {
                    addTotal(items, tGroupName, "OVER (" + limit + ")", tNode.path("over").asDouble(),
                            tScope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, true, statType);
                }
                if (tNode.has("under")) {
                    addTotal(items, tGroupName, "UNDER (" + limit + ")", tNode.path("under").asDouble(),
                            tScope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, true, statType);
                }
            }
        }

        // Handicaps
        JsonNode handicapsNode = marketNode.has("handicaps") ? marketNode.get("handicaps") : null;
        if (handicapsNode != null && handicapsNode.isArray()) {
            for (JsonNode hNode : handicapsNode) {
                boolean hHalf1 = isHalf1 || hNode.path("isHalf1").asBoolean(false);
                BetScope hScope = hHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
                String hGroupName = prefix + "_handicap" + (hHalf1 ? "_half_1" : "");
                double hdp = hNode.path("hdp").asDouble();

                if (hNode.has("home")) {
                    addHandicap(items, hGroupName, "HOME (" + hdp + ")", hNode.path("home").asDouble(),
                            hScope, HandicapBet.Outcome.TEAM1, hdp, true, statType);
                }
                if (hNode.has("away")) {
                    addHandicap(items, hGroupName, "AWAY (" + (-hdp) + ")", hNode.path("away").asDouble(),
                            hScope, HandicapBet.Outcome.TEAM2, -hdp, true, statType);
                }
            }
        }
    }
}
