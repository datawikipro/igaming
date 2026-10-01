package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import pro.datawiki.igaming.dto.BetType;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractSbobetMarketHandler implements SbobetMarketHandler {

    private static final Pattern LIMIT_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)");

    /**
     * Check if the market key refers to a stats market (corners, cards, bookings, etc.).
     */
    protected boolean isStats(String marketKey) {
        if (marketKey == null) return false;
        String k = marketKey.toLowerCase();
        return k.contains("corner") || k.contains("card") || k.contains("yellow")
                || k.contains("booking") || k.contains("foul") || k.contains("offside")
                || k.contains("shot");
    }

    /**
     * Check if the sport type is an esports discipline.
     * Delegates to {@link SbobetEsportsHandler#isEsports(SportType)}.
     */
    public static boolean isEsports(SportType sportType) {
        return SbobetEsportsHandler.isEsports(sportType);
    }

    /**
     * Check if a JSON node has direct outcome fields (odds/price, over/under, home/away etc.)
     * as opposed to being a container of sub-markets.
     */
    protected boolean hasDirectOutcomeFields(JsonNode node) {
        if (node == null) return false;
        return node.has("odds") || node.has("price") || node.has("value")
                || node.has("over") || node.has("under")
                || node.has("home") || node.has("away")
                || node.has("1") || node.has("2")
                || node.has("hdp") || node.has("limit") || node.has("line")
                || node.has("name") || node.has("type");
    }

    /**
     * Parse a numeric limit value from a market name string (e.g. "Over 9.5" → 9.5).
     */
    protected double parseLimitFromName(String name) {
        if (name == null) return 0.0;
        Matcher m = LIMIT_PATTERN.matcher(name);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }


    protected boolean isHalf1(String marketKey, JsonNode node) {
        if (marketKey != null) {
            String lowerKey = marketKey.toLowerCase();
            if (lowerKey.contains("half1") || lowerKey.contains("half_1") || lowerKey.contains("1st_half") || lowerKey.contains("first_half")) {
                return true;
            }
        }
        if (node != null) {
            if (node.has("_isHalf1") && node.path("_isHalf1").asBoolean()) return true;
            if (node.path("isHalf1").asBoolean(false)) return true;
            String keyField = node.path("_key").asText("").toLowerCase();
            if (keyField.contains("half1") || keyField.contains("half_1")) return true;
        }
        return false;
    }

    protected boolean isHalf2(String marketKey, JsonNode node) {
        if (marketKey != null) {
            String lowerKey = marketKey.toLowerCase();
            if (lowerKey.contains("half2") || lowerKey.contains("half_2") || lowerKey.contains("2nd_half") || lowerKey.contains("second_half")) {
                return true;
            }
        }
        if (node != null) {
            if (node.has("_isHalf2") && node.path("_isHalf2").asBoolean()) return true;
            if (node.path("isHalf2").asBoolean(false)) return true;
            String keyField = node.path("_key").asText("").toLowerCase();
            if (keyField.contains("half2") || keyField.contains("half_2")) return true;
        }
        return false;
    }

    protected BetScope resolveScope(String marketKey, JsonNode node) {
        if (isHalf1(marketKey, node)) {
            return BetScope.HALF_1;
        }
        if (isHalf2(marketKey, node)) {
            return BetScope.HALF_2;
        }
        return BetScope.FULL_MATCH;
    }

    protected String getScopeSuffix(BetScope scope) {
        if (scope == BetScope.HALF_1) {
            return "_half_1";
        }
        if (scope == BetScope.HALF_2) {
            return "_half_2";
        }
        return "";
    }

    protected boolean isQuarterAsian(double param) {
        return Math.abs(param * 4 - Math.round(param * 4)) < 0.001
                && Math.abs(param * 2 - Math.round(param * 2)) > 0.001;
    }

    protected double extractDouble(JsonNode node, String... fieldNames) {
        if (node == null) return 0.0;
        for (String field : fieldNames) {
            if (node.has(field)) {
                return node.path(field).asDouble(0.0);
            }
        }
        return 0.0;
    }

    protected void addOddItem(List<OddItem> items, String groupName, String rawOutcomeName, double value, BetType betType) {
        if (value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        item.setFactorId(groupName + "_" + rawOutcomeName.replace(" ", "_"));
        item.setGroupName(groupName);
        item.setName(rawOutcomeName);
        item.setValue(value);
        item.setBetType(betType);
        items.add(item);
    }

    protected void addMatchResult(List<OddItem> items, String groupName, String name, double value,
                                  BetScope scope, MatchResultBet.Outcome outcome, StatType statType) {
        addOddItem(items, groupName, name, value, new MatchResultBet(scope, outcome, statType));
    }

    protected void addTotal(List<OddItem> items, String groupName, String name, double value,
                            BetScope scope, BetSubject subject, TotalBet.Direction direction,
                            double limit, boolean isAsian, StatType statType) {
        addOddItem(items, groupName, name, value, new TotalBet(scope, subject, direction, limit, isAsian, statType));
    }

    protected void addHandicap(List<OddItem> items, String groupName, String name, double value,
                               BetScope scope, HandicapBet.Outcome outcome, double hdp,
                               boolean isAsian, StatType statType) {
        addOddItem(items, groupName, name, value, new HandicapBet(scope, outcome, hdp, isAsian, statType));
    }

    protected void addBinary(List<OddItem> items, String groupName, String name, double value,
                             BetScope scope, BetSubject subject, BinaryMarketBet.MarketType marketType,
                             BinaryMarketBet.Outcome outcome, StatType statType) {
        addOddItem(items, groupName, name, value, new BinaryMarketBet(scope, subject, marketType, outcome, statType));
    }
}
