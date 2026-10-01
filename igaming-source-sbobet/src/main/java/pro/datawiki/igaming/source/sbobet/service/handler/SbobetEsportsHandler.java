package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SbobetEsportsHandler extends AbstractSbobetMarketHandler {

    private static final Pattern MAP_INDEX_PATTERN = Pattern.compile("map[_-]?([1-5])(?![0-9])");

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String lower = marketKey.toLowerCase();
        return lower.startsWith("esport")
                || lower.startsWith("map")
                || lower.startsWith("round")
                || lower.contains("map_winner")
                || lower.contains("maps_total")
                || lower.contains("maps_handicap")
                || lower.contains("map_total")
                || lower.contains("map_handicap")
                || lower.contains("rounds_total")
                || lower.contains("rounds_handicap");
    }

    @Override
    public boolean supports(String marketKey, SportType sportType) {
        if (marketKey == null) return false;
        if (supports(marketKey)) {
            return true;
        }
        if (isEsports(sportType)) {
            String lower = marketKey.toLowerCase();
            return lower.equals("winner") || lower.equals("match_winner") || lower.equals("moneyline")
                    || lower.equals("totals") || lower.equals("handicaps");
        }
        return false;
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        handle(null, marketNode, SportType.ESPORTS, items);
    }

    @Override
    public void handle(JsonNode marketNode, SportType sportType, List<OddItem> items) {
        handle(null, marketNode, sportType, items);
    }

    public static boolean isEsports(SportType sportType) {
        if (sportType == null) return false;
        return sportType == SportType.ESPORTS
                || sportType == SportType.CS2
                || sportType == SportType.DOTA2
                || sportType == SportType.LEAGUE_OF_LEGENDS
                || sportType == SportType.VALORANT
                || sportType == SportType.STARCRAFT
                || sportType == SportType.RAINBOW_SIX
                || sportType == SportType.OVERWATCH
                || sportType == SportType.CALL_OF_DUTY;
    }

    @Override
    public void handle(String marketKey, JsonNode marketNode, SportType sportType, List<OddItem> items) {
        if (marketNode == null) return;

        if (marketKey == null || marketKey.toLowerCase().startsWith("esport")) {
            processContainerNode(marketNode, sportType, items);
            return;
        }

        String lowerKey = marketKey.toLowerCase();

        // 1. Direct Maps Total
        if (lowerKey.equals("maps_total") || lowerKey.equals("map_totals") || lowerKey.equals("maps_totals")
                || (isEsports(sportType) && lowerKey.equals("totals"))) {
            processMapsTotal(marketNode, items);
            return;
        }

        // 2. Direct Maps Handicap
        if (lowerKey.equals("maps_handicap") || lowerKey.equals("map_handicap") || lowerKey.equals("maps_handicaps")
                || (isEsports(sportType) && lowerKey.equals("handicaps"))) {
            processMapsHandicap(marketNode, items);
            return;
        }

        // 3. Match Winner for esports
        if (lowerKey.equals("match_winner") || (isEsports(sportType) && (lowerKey.equals("winner") || lowerKey.equals("moneyline")))) {
            processMatchWinner(marketNode, items);
            return;
        }

        // 4. Map-specific markets
        int mapIdx = extractMapIndex(lowerKey);
        if (mapIdx >= 1 && mapIdx <= 5) {
            BetScope mapScope = resolveMapScope(mapIdx);
            String mapPrefix = "map" + mapIdx;

            if (lowerKey.contains("total")) {
                processRoundsTotal(marketNode, mapPrefix, mapScope, items);
            } else if (lowerKey.contains("handicap") || lowerKey.contains("spread") || lowerKey.contains("hdp")) {
                processRoundsHandicap(marketNode, mapPrefix, mapScope, items);
            } else if (lowerKey.contains("winner")) {
                processMapWinner(marketNode, mapPrefix, mapScope, items);
            } else {
                processMapNode(marketNode, mapPrefix, mapScope, items);
            }
            return;
        }

        // 5. General rounds total or handicap
        if (lowerKey.contains("round") && lowerKey.contains("total")) {
            BetScope scope = resolveMapScopeFromNode(marketNode, BetScope.FULL_MATCH);
            String prefix = scope != BetScope.FULL_MATCH ? "map" + mapIndexFromScope(scope) : "rounds";
            processRoundsTotal(marketNode, prefix, scope, items);
            return;
        }

        if (lowerKey.contains("round") && (lowerKey.contains("handicap") || lowerKey.contains("hdp") || lowerKey.contains("spread"))) {
            BetScope scope = resolveMapScopeFromNode(marketNode, BetScope.FULL_MATCH);
            String prefix = scope != BetScope.FULL_MATCH ? "map" + mapIndexFromScope(scope) : "rounds";
            processRoundsHandicap(marketNode, prefix, scope, items);
        }
    }

    private int extractMapIndex(String key) {
        if (key == null) return 0;
        String lower = key.toLowerCase();
        Matcher m = MAP_INDEX_PATTERN.matcher(lower);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    public BetScope resolveMapScope(int mapIdx) {
        return switch (mapIdx) {
            case 1 -> BetScope.MAP_1;
            case 2 -> BetScope.MAP_2;
            case 3 -> BetScope.MAP_3;
            case 4 -> BetScope.MAP_4;
            case 5 -> BetScope.MAP_5;
            default -> null;
        };
    }

    private int mapIndexFromScope(BetScope scope) {
        if (scope == BetScope.MAP_1) return 1;
        if (scope == BetScope.MAP_2) return 2;
        if (scope == BetScope.MAP_3) return 3;
        if (scope == BetScope.MAP_4) return 4;
        if (scope == BetScope.MAP_5) return 5;
        return 0;
    }

    private BetScope resolveMapScopeFromNode(JsonNode node, BetScope defaultScope) {
        if (node != null) {
            int mapNum = node.path("map").asInt(0);
            if (mapNum == 0) mapNum = node.path("mapNumber").asInt(0);
            if (mapNum == 0) mapNum = node.path("map_number").asInt(0);
            if (mapNum >= 1 && mapNum <= 5) {
                return resolveMapScope(mapNum);
            }
        }
        return defaultScope;
    }

    private void processContainerNode(JsonNode marketNode, SportType sportType, List<OddItem> items) {
        if (marketNode == null) return;

        // 1. Process map1..5 inside container
        for (int mapIdx = 1; mapIdx <= 5; mapIdx++) {
            String mapKey = "map" + mapIdx;
            BetScope mapScope = resolveMapScope(mapIdx);
            JsonNode mapNode = marketNode.has(mapKey) ? marketNode.get(mapKey)
                    : (marketNode.has("map_" + mapIdx) ? marketNode.get("map_" + mapIdx) : null);
            if (mapNode != null) {
                processMapNode(mapNode, mapKey, mapScope, items);
            }
        }

        // 2. Maps Total inside container
        JsonNode mapsTotalNode = marketNode.has("maps_total") ? marketNode.get("maps_total")
                : (marketNode.has("map_totals") ? marketNode.get("map_totals") : null);
        if (mapsTotalNode != null) {
            processMapsTotal(mapsTotalNode, items);
        }

        // 3. Maps Handicap inside container
        JsonNode mapsHdpNode = marketNode.has("maps_handicap") ? marketNode.get("maps_handicap")
                : (marketNode.has("map_handicaps") ? marketNode.get("map_handicaps") : null);
        if (mapsHdpNode != null) {
            processMapsHandicap(mapsHdpNode, items);
        }

        // 4. Match Winner inside container
        JsonNode winnerNode = marketNode.has("winner") ? marketNode.get("winner")
                : (marketNode.has("match_winner") ? marketNode.get("match_winner") : null);
        if (winnerNode != null) {
            processMatchWinner(winnerNode, items);
        }
    }

    private void processMapNode(JsonNode mapNode, String mapPrefix, BetScope mapScope, List<OddItem> items) {
        if (mapNode == null) return;

        if (mapNode.isArray()) {
            for (JsonNode child : mapNode) {
                processMapNode(child, mapPrefix, mapScope, items);
            }
            return;
        }

        // 1. Winner
        JsonNode winnerNode = mapNode.has("winner") ? mapNode.get("winner") : mapNode;
        if (winnerNode.has("home") || winnerNode.has("away") || winnerNode.has("1") || winnerNode.has("2") || winnerNode.has("prices")) {
            processMapWinner(winnerNode, mapPrefix, mapScope, items);
        }

        // 2. Totals (Rounds)
        if (mapNode.has("totals")) {
            processRoundsTotal(mapNode.get("totals"), mapPrefix, mapScope, items);
        } else if (mapNode.has("rounds_total")) {
            processRoundsTotal(mapNode.get("rounds_total"), mapPrefix, mapScope, items);
        }

        // 3. Handicaps (Rounds)
        if (mapNode.has("handicaps")) {
            processRoundsHandicap(mapNode.get("handicaps"), mapPrefix, mapScope, items);
        } else if (mapNode.has("rounds_handicap")) {
            processRoundsHandicap(mapNode.get("rounds_handicap"), mapPrefix, mapScope, items);
        }
    }

    private void processMapWinner(JsonNode node, String mapPrefix, BetScope mapScope, List<OddItem> items) {
        if (node == null) return;
        if (node.has("winner")) {
            node = node.get("winner");
        }
        String groupName = mapPrefix + "_winner";

        if (node.has("prices") && node.get("prices").isArray()) {
            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("home") || des.equals("1") || des.equals("h")) {
                    addMatchResult(items, groupName, "HOME", price, mapScope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH);
                } else if (des.equals("away") || des.equals("2") || des.equals("a")) {
                    addMatchResult(items, groupName, "AWAY", price, mapScope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH);
                } else if (des.equals("draw") || des.equals("x") || des.equals("d")) {
                    addMatchResult(items, groupName, "DRAW", price, mapScope, MatchResultBet.Outcome.DRAW, StatType.MATCH);
                }
            }
            return;
        }

        double home = extractDouble(node, "home", "1", "win1", "team1", "h");
        double away = extractDouble(node, "away", "2", "win2", "team2", "a");
        double draw = extractDouble(node, "draw", "x", "tie", "d");

        if (home > 1.0) {
            MatchResultBet.Outcome outcome = (draw > 1.0) ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            addMatchResult(items, groupName, "HOME", home, mapScope, outcome, StatType.MATCH);
        }
        if (away > 1.0) {
            MatchResultBet.Outcome outcome = (draw > 1.0) ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            addMatchResult(items, groupName, "AWAY", away, mapScope, outcome, StatType.MATCH);
        }
        if (draw > 1.0) {
            addMatchResult(items, groupName, "DRAW", draw, mapScope, MatchResultBet.Outcome.DRAW, StatType.MATCH);
        }
    }

    private void processMatchWinner(JsonNode node, List<OddItem> items) {
        if (node == null) return;
        if (node.has("winner")) {
            node = node.get("winner");
        } else if (node.has("match_winner")) {
            node = node.get("match_winner");
        }
        String groupName = "match_winner";

        if (node.has("prices") && node.get("prices").isArray()) {
            boolean hasDraw = false;
            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                if (des.equals("draw") || des.equals("x") || des.equals("d")) {
                    hasDraw = true;
                    break;
                }
            }
            MatchResultBet.Outcome homeOutcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            MatchResultBet.Outcome awayOutcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;

            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("home") || des.equals("1") || des.equals("h")) {
                    addMatchResult(items, groupName, "HOME", price, BetScope.FULL_MATCH, homeOutcome, StatType.MATCH);
                } else if (des.equals("away") || des.equals("2") || des.equals("a")) {
                    addMatchResult(items, groupName, "AWAY", price, BetScope.FULL_MATCH, awayOutcome, StatType.MATCH);
                } else if (des.equals("draw") || des.equals("x") || des.equals("d")) {
                    addMatchResult(items, groupName, "DRAW", price, BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, StatType.MATCH);
                }
            }
            return;
        }

        double home = extractDouble(node, "home", "1", "win1", "team1", "h");
        double away = extractDouble(node, "away", "2", "win2", "team2", "a");
        double draw = extractDouble(node, "draw", "x", "tie", "d");

        MatchResultBet.Outcome homeOutcome = (draw > 1.0) ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
        MatchResultBet.Outcome awayOutcome = (draw > 1.0) ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;

        if (home > 1.0) {
            addMatchResult(items, groupName, "HOME", home, BetScope.FULL_MATCH, homeOutcome, StatType.MATCH);
        }
        if (away > 1.0) {
            addMatchResult(items, groupName, "AWAY", away, BetScope.FULL_MATCH, awayOutcome, StatType.MATCH);
        }
        if (draw > 1.0) {
            addMatchResult(items, groupName, "DRAW", draw, BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, StatType.MATCH);
        }
    }

    private void processMapsTotal(JsonNode node, List<OddItem> items) {
        if (node == null) return;
        if (node.has("maps_total")) {
            node = node.get("maps_total");
        } else if (node.has("map_totals")) {
            node = node.get("map_totals");
        } else if (node.has("totals")) {
            node = node.get("totals");
        }
        if (node.isArray()) {
            for (JsonNode tNode : node) {
                processSingleMapsTotal(tNode, items);
            }
        } else if (node.isObject()) {
            processSingleMapsTotal(node, items);
        }
    }

    private void processSingleMapsTotal(JsonNode node, List<OddItem> items) {
        if (node == null || !node.isObject()) return;
        double limit = extractDouble(node, "limit", "point", "line", "total");
        String groupName = "maps_total";

        if (node.has("prices") && node.get("prices").isArray()) {
            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("over") || des.equals("o")) {
                    addTotal(items, groupName, "OVER (" + limit + ")", price,
                            BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, limit, true, StatType.MAPS);
                } else if (des.equals("under") || des.equals("u")) {
                    addTotal(items, groupName, "UNDER (" + limit + ")", price,
                            BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, true, StatType.MAPS);
                }
            }
            return;
        }

        double over = extractDouble(node, "over", "o");
        double under = extractDouble(node, "under", "u");

        if (over > 1.0) {
            addTotal(items, groupName, "OVER (" + limit + ")", over,
                    BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, limit, true, StatType.MAPS);
        }
        if (under > 1.0) {
            addTotal(items, groupName, "UNDER (" + limit + ")", under,
                    BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, true, StatType.MAPS);
        }
    }

    private void processMapsHandicap(JsonNode node, List<OddItem> items) {
        if (node == null) return;
        if (node.has("maps_handicap")) {
            node = node.get("maps_handicap");
        } else if (node.has("map_handicaps")) {
            node = node.get("map_handicaps");
        } else if (node.has("handicaps")) {
            node = node.get("handicaps");
        }
        if (node.isArray()) {
            for (JsonNode hNode : node) {
                processSingleMapsHandicap(hNode, items);
            }
        } else if (node.isObject()) {
            processSingleMapsHandicap(node, items);
        }
    }

    private void processSingleMapsHandicap(JsonNode node, List<OddItem> items) {
        if (node == null || !node.isObject()) return;
        double hdp = extractDouble(node, "hdp", "handicap", "spread", "line");
        String groupName = "maps_handicap";

        if (node.has("prices") && node.get("prices").isArray()) {
            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("home") || des.equals("1") || des.equals("h")) {
                    addHandicap(items, groupName, "HOME (" + hdp + ")", price,
                            BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, hdp, true, StatType.MAPS);
                } else if (des.equals("away") || des.equals("2") || des.equals("a")) {
                    addHandicap(items, groupName, "AWAY (" + (-hdp) + ")", price,
                            BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, -hdp, true, StatType.MAPS);
                }
            }
            return;
        }

        double home = extractDouble(node, "home", "1", "h", "team1");
        double away = extractDouble(node, "away", "2", "a", "team2");

        if (home > 1.0) {
            addHandicap(items, groupName, "HOME (" + hdp + ")", home,
                    BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, hdp, true, StatType.MAPS);
        }
        if (away > 1.0) {
            addHandicap(items, groupName, "AWAY (" + (-hdp) + ")", away,
                    BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, -hdp, true, StatType.MAPS);
        }
    }

    private void processRoundsTotal(JsonNode node, String prefix, BetScope scope, List<OddItem> items) {
        if (node == null) return;
        if (node.has("totals")) {
            node = node.get("totals");
        } else if (node.has("rounds_total")) {
            node = node.get("rounds_total");
        }
        if (node.isArray()) {
            for (JsonNode tNode : node) {
                processSingleRoundsTotal(tNode, prefix, scope, items);
            }
        } else if (node.isObject()) {
            processSingleRoundsTotal(node, prefix, scope, items);
        }
    }

    private void processSingleRoundsTotal(JsonNode node, String prefix, BetScope scope, List<OddItem> items) {
        if (node == null || !node.isObject()) return;
        double limit = extractDouble(node, "limit", "point", "line", "total");
        String groupName = prefix + "_rounds_total";

        if (node.has("prices") && node.get("prices").isArray()) {
            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("over") || des.equals("o")) {
                    addTotal(items, groupName, "OVER (" + limit + ")", price,
                            scope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, true, StatType.ROUNDS);
                } else if (des.equals("under") || des.equals("u")) {
                    addTotal(items, groupName, "UNDER (" + limit + ")", price,
                            scope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, true, StatType.ROUNDS);
                }
            }
            return;
        }

        double over = extractDouble(node, "over", "o");
        double under = extractDouble(node, "under", "u");

        if (over > 1.0) {
            addTotal(items, groupName, "OVER (" + limit + ")", over,
                    scope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, true, StatType.ROUNDS);
        }
        if (under > 1.0) {
            addTotal(items, groupName, "UNDER (" + limit + ")", under,
                    scope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, true, StatType.ROUNDS);
        }
    }

    private void processRoundsHandicap(JsonNode node, String prefix, BetScope scope, List<OddItem> items) {
        if (node == null) return;
        if (node.has("handicaps")) {
            node = node.get("handicaps");
        } else if (node.has("rounds_handicap")) {
            node = node.get("rounds_handicap");
        }
        if (node.isArray()) {
            for (JsonNode hNode : node) {
                processSingleRoundsHandicap(hNode, prefix, scope, items);
            }
        } else if (node.isObject()) {
            processSingleRoundsHandicap(node, prefix, scope, items);
        }
    }

    private void processSingleRoundsHandicap(JsonNode node, String prefix, BetScope scope, List<OddItem> items) {
        if (node == null || !node.isObject()) return;
        double hdp = extractDouble(node, "hdp", "handicap", "spread", "line");
        String groupName = prefix + "_rounds_handicap";

        if (node.has("prices") && node.get("prices").isArray()) {
            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("home") || des.equals("1") || des.equals("h")) {
                    addHandicap(items, groupName, "HOME (" + hdp + ")", price,
                            scope, HandicapBet.Outcome.TEAM1, hdp, true, StatType.ROUNDS);
                } else if (des.equals("away") || des.equals("2") || des.equals("a")) {
                    addHandicap(items, groupName, "AWAY (" + (-hdp) + ")", price,
                            scope, HandicapBet.Outcome.TEAM2, -hdp, true, StatType.ROUNDS);
                }
            }
            return;
        }

        double home = extractDouble(node, "home", "1", "h", "team1");
        double away = extractDouble(node, "away", "2", "a", "team2");

        if (home > 1.0) {
            addHandicap(items, groupName, "HOME (" + hdp + ")", home,
                    scope, HandicapBet.Outcome.TEAM1, hdp, true, StatType.ROUNDS);
        }
        if (away > 1.0) {
            addHandicap(items, groupName, "AWAY (" + (-hdp) + ")", away,
                    scope, HandicapBet.Outcome.TEAM2, -hdp, true, StatType.ROUNDS);
        }
    }
}
