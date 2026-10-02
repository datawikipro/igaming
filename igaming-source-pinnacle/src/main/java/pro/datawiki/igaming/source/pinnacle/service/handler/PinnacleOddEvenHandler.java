package pro.datawiki.igaming.source.pinnacle.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

/**
 * Handles Odd/Even (Чёт/Нечёт) market type for Pinnacle (Arcadia API).
 * Supported market types: "odd_even", "total_odd_even", "goals_odd_even",
 * "corners_odd_even", "cards_odd_even".
 *
 * Outcome codes ODD / EVEN are recognised by the aggregator SurebetRuleEvaluator
 * (SUREBET_COMBINATIONS entry {ODD, EVEN}) so villas formed across bookmakers
 * (e.g. Pinnacle ODD vs 1xBet EVEN) are detected automatically.
 */
@Component
public class PinnacleOddEvenHandler extends AbstractPinnacleMarketHandler {

    @Override
    public boolean supports(String marketType) {
        if (marketType == null) return false;
        String lower = marketType.toLowerCase();
        return lower.equals("odd_even")
                || lower.equals("total_odd_even")
                || lower.equals("oddeven")
                || lower.equals("goals_odd_even")
                || lower.equals("corners_odd_even")
                || lower.equals("cards_odd_even");
    }

    @Override
    public void handle(JsonNode market, BetScope scope, String scopeSuffix, List<OddItem> items) {
        JsonNode prices = market.path("prices");
        if (!prices.isArray()) return;

        String mType = market.path("type").asText("").toLowerCase();
        StatType statType = resolveStatType(mType);
        String groupName = buildGroupName(mType, scopeSuffix);

        for (JsonNode p : prices) {
            String designation = p.path("designation").asText("").toLowerCase();
            double rawPrice = p.path("price").asDouble();
            double decimalValue = americanToDecimal(rawPrice);

            BinaryMarketBet.Outcome outcome = resolveOutcome(designation);
            if (outcome != null) {
                addOddItem(items, groupName, outcome.name(), decimalValue,
                        new BinaryMarketBet(scope, BetSubject.MATCH,
                                BinaryMarketBet.MarketType.ODD_EVEN, outcome, statType));
            }
        }
    }

    private StatType resolveStatType(String mType) {
        if (mType.contains("corner")) return StatType.CORNERS;
        if (mType.contains("card"))   return StatType.CARDS;
        return StatType.MATCH;
    }

    private String buildGroupName(String mType, String scopeSuffix) {
        if (mType.contains("corner")) return "corners_odd_even" + scopeSuffix;
        if (mType.contains("card"))   return "cards_odd_even" + scopeSuffix;
        return "odd_even" + scopeSuffix;
    }

    private BinaryMarketBet.Outcome resolveOutcome(String designation) {
        if (designation == null) return null;
        return switch (designation) {
            case "odd",  "o" -> BinaryMarketBet.Outcome.ODD;
            case "even", "e" -> BinaryMarketBet.Outcome.EVEN;
            default          -> null;
        };
    }
}
