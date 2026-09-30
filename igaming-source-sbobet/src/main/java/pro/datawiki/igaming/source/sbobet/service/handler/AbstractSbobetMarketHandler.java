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
        return resolveScope(marketKey, node, (SportType) null);
    }

    protected BetScope resolveScope(String marketKey, JsonNode node, SbobetMarketContext context) {
        return resolveScope(marketKey, node, context != null ? context.getSportType() : null);
    }

    protected BetScope resolveScope(String marketKey, JsonNode node, SportType sportType) {
        if (marketKey != null) {
            String k = marketKey.toLowerCase();
            // Esports maps
            if (k.contains("map1") || k.contains("map_1") || k.contains("map 1")
                    || k.contains("1st_map") || k.contains("1st map") || k.contains("first_map") || k.contains("first map")) return BetScope.MAP_1;
            if (k.contains("map2") || k.contains("map_2") || k.contains("map 2")
                    || k.contains("2nd_map") || k.contains("2nd map") || k.contains("second_map") || k.contains("second map")) return BetScope.MAP_2;
            if (k.contains("map3") || k.contains("map_3") || k.contains("map 3")
                    || k.contains("3rd_map") || k.contains("3rd map") || k.contains("third_map") || k.contains("third map")) return BetScope.MAP_3;
            if (k.contains("map4") || k.contains("map_4") || k.contains("map 4")
                    || k.contains("4th_map") || k.contains("4th map") || k.contains("fourth_map") || k.contains("fourth map")) return BetScope.MAP_4;
            if (k.contains("map5") || k.contains("map_5") || k.contains("map 5")
                    || k.contains("5th_map") || k.contains("5th map") || k.contains("fifth_map") || k.contains("fifth map")) return BetScope.MAP_5;
            if (k.contains("map6") || k.contains("map_6") || k.contains("map 6")) return BetScope.MAP_6;
            if (k.contains("map7") || k.contains("map_7") || k.contains("map 7")) return BetScope.MAP_7;

            // Rounds
            if (k.contains("round1") || k.contains("round_1") || k.contains("round 1")) return BetScope.ROUND_1;
            if (k.contains("round2") || k.contains("round_2") || k.contains("round 2")) return BetScope.ROUND_2;
            if (k.contains("round3") || k.contains("round_3") || k.contains("round 3")) return BetScope.ROUND_3;
            if (k.contains("round4") || k.contains("round_4") || k.contains("round 4")) return BetScope.ROUND_4;
            if (k.contains("round5") || k.contains("round_5") || k.contains("round 5")) return BetScope.ROUND_5;

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
            int map = node.path("map").asInt(0);
            if (map == 0) map = node.path("mapIndex").asInt(0);
            if (map == 0 && node.has("map_index")) map = node.path("map_index").asInt(0);
            if (map == 1) return BetScope.MAP_1;
            if (map == 2) return BetScope.MAP_2;
            if (map == 3) return BetScope.MAP_3;
            if (map == 4) return BetScope.MAP_4;
            if (map == 5) return BetScope.MAP_5;
            if (map == 6) return BetScope.MAP_6;
            if (map == 7) return BetScope.MAP_7;

            int round = node.path("round").asInt(0);
            if (round == 0) round = node.path("roundIndex").asInt(0);
            if (round == 1) return BetScope.ROUND_1;
            if (round == 2) return BetScope.ROUND_2;
            if (round == 3) return BetScope.ROUND_3;
            if (round == 4) return BetScope.ROUND_4;
            if (round == 5) return BetScope.ROUND_5;

            boolean isEsport = isEsports(sportType);

            int period = node.path("period").asInt(0);
            if (period > 0) {
                if (isEsport) {
                    return switch (period) {
                        case 1 -> BetScope.MAP_1;
                        case 2 -> BetScope.MAP_2;
                        case 3 -> BetScope.MAP_3;
                        case 4 -> BetScope.MAP_4;
                        case 5 -> BetScope.MAP_5;
                        case 6 -> BetScope.MAP_6;
                        case 7 -> BetScope.MAP_7;
                        default -> BetScope.PERIOD_1;
                    };
                } else {
                    if (period == 1) return BetScope.HALF_1;
                    if (period == 2) return BetScope.HALF_2;
                }
            }

            if (node.has("_isHalf1") || node.path("isHalf1").asBoolean(false)
                    || node.path("half").asInt(0) == 1
                    || "1st half".equalsIgnoreCase(node.path("period").asText(""))
                    || "first half".equalsIgnoreCase(node.path("period").asText(""))) {
                return BetScope.HALF_1;
            }
            if (node.has("_isHalf2") || node.path("isHalf2").asBoolean(false)
                    || node.path("half").asInt(0) == 2
                    || "2nd half".equalsIgnoreCase(node.path("period").asText(""))
                    || "second half".equalsIgnoreCase(node.path("period").asText(""))) {
                return BetScope.HALF_2;
            }
        }
        return BetScope.FULL_MATCH;
    }

    private static final java.util.regex.Pattern LIMIT_PATTERN = java.util.regex.Pattern.compile("([0-9]+(?:\\.[0-9]+)?)");

    protected double parseLimitFromName(String name) {
        if (name == null) return 0.0;
        java.util.regex.Matcher matcher = LIMIT_PATTERN.matcher(name);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }

    protected double extractDouble(JsonNode node, String... fieldNames) {
        for (String field : fieldNames) {
            if (node.has(field)) {
                return node.path(field).asDouble(0.0);
            }
        }
        return 0.0;
    }

    protected boolean hasDirectOutcomeFields(JsonNode node) {
        return node.has("home") || node.has("away") || node.has("draw")
                || node.has("limit") || node.has("hdp") || node.has("line")
                || node.has("over") || node.has("under")
                || node.has("1") || node.has("2") || node.has("x")
                || node.has("odds") || node.has("value") || node.has("price");
    }

    public static boolean isNonMarketKey(String key) {
        if (key == null) return true;
        String k = key.toLowerCase();
        return k.equals("id") || k.equals("home") || k.equals("away") || k.equals("team1") || k.equals("team2")
                || k.equals("starttime") || k.equals("start_time") || k.equals("islive") || k.equals("is_live")
                || k.equals("sportname") || k.equals("sport_name") || k.equals("leaguename") || k.equals("league_name")
                || k.equals("status") || k.equals("eventurl") || k.equals("event_url") || k.startsWith("_");
    }
}
