package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;

import java.util.List;

public abstract class AbstractSbobetMarketHandler implements SbobetMarketHandler {

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

    public static boolean isEsports(SportType sportType) {
        if (sportType == null) return false;
        return switch (sportType) {
            case ESPORTS, CS2, DOTA2, LEAGUE_OF_LEGENDS, VALORANT,
                 RAINBOW_SIX, ROCKET_LEAGUE, CALL_OF_DUTY, OVERWATCH,
                 PUBG, MOBILE_LEGENDS, CROSSFIRE, STARCRAFT,
                 CYBER_FOOTBALL, CYBER_BASKETBALL, CYBER_HOCKEY -> true;
            default -> false;
        };
    }

    public static boolean isStats(String marketKey) {
        if (marketKey == null) return false;
        String k = marketKey.toLowerCase();
        return k.contains("corner") || k.contains("card") || k.contains("booking") || k.contains("yellow") || k.contains("foul");
    }

    protected BetScope resolveScope(String marketKey, JsonNode node) {
        if (marketKey != null) {
            String k = marketKey.toLowerCase();
            // Esports maps
            if (k.contains("map1") || k.contains("map_1") || k.contains("map 1")) return BetScope.MAP_1;
            if (k.contains("map2") || k.contains("map_2") || k.contains("map 2")) return BetScope.MAP_2;
            if (k.contains("map3") || k.contains("map_3") || k.contains("map 3")) return BetScope.MAP_3;
            if (k.contains("map4") || k.contains("map_4") || k.contains("map 4")) return BetScope.MAP_4;
            if (k.contains("map5") || k.contains("map_5") || k.contains("map 5")) return BetScope.MAP_5;

            // Halves
            if (k.contains("half1") || k.contains("1st_half") || k.contains("half_1") || k.contains("1st half")
                    || k.contains("first_half") || k.contains("first half") || k.contains("1sthalf") || k.contains("ht1")) {
                return BetScope.HALF_1;
            }
            if (k.contains("half2") || k.contains("2nd_half") || k.contains("half_2") || k.contains("2nd half")
                    || k.contains("second_half") || k.contains("second half") || k.contains("2ndhalf") || k.contains("ht2")) {
                return BetScope.HALF_2;
            }
        }
        if (node != null) {
            if (node.has("_isHalf1") || node.path("isHalf1").asBoolean(false)
                    || node.path("half").asInt(0) == 1
                    || node.path("period").asInt(0) == 1
                    || "1st half".equalsIgnoreCase(node.path("period").asText(""))
                    || "first half".equalsIgnoreCase(node.path("period").asText(""))) {
                return BetScope.HALF_1;
            }
            if (node.has("_isHalf2") || node.path("isHalf2").asBoolean(false)
                    || node.path("half").asInt(0) == 2
                    || node.path("period").asInt(0) == 2
                    || "2nd half".equalsIgnoreCase(node.path("period").asText(""))
                    || "second half".equalsIgnoreCase(node.path("period").asText(""))) {
                return BetScope.HALF_2;
            }
        }
        return BetScope.FULL_MATCH;
    }
}
