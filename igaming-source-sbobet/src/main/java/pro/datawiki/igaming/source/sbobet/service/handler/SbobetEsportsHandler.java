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
        if (marketKey == null) return false;
        String k = marketKey.toLowerCase();
        if (isEsports(sportType)) {
            if (isNonMarketKey(k)) return false;
            if (isStats(k)) return false;
            if (isDoubleChanceKey(k) || isDrawNoBetKey(k) || isBttsKey(k) || isCorrectScoreKey(k)) {
                return false;
            }
            return true;
        }
        return supports(marketKey);
    }

    private boolean isDoubleChanceKey(String k) {
        return k.contains("double_chance") || k.contains("doublechance") || k.equals("dc") || k.startsWith("dc_") || k.endsWith("_dc");
    }

    private boolean isDrawNoBetKey(String k) {
        return k.contains("draw_no_bet") || k.contains("drawnobet") || k.equals("dnb") || k.startsWith("dnb_") || k.endsWith("_dnb");
    }

    private boolean isBttsKey(String k) {
        return k.contains("btts") || k.contains("both_teams_to_score") || k.contains("bothteamstoscore") || k.contains("gg_ng");
    }

    private boolean isCorrectScoreKey(String k) {
        return k.contains("correct_score") || k.contains("correctscore") || k.contains("exact_score")
                || k.contains("exactscore") || k.equals("cs") || k.startsWith("cs_") || k.endsWith("_cs");
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
    public boolean supports(String marketKey, SbobetMarketContext context) {
        return supports(marketKey, context != null ? context.getSportType() : null);
    }

    @Override
    public void handle(String marketKey, JsonNode marketNode, SbobetMarketContext context, List<OddItem> items) {
        handleMarket(marketKey, context != null ? context.getSportType() : SportType.ESPORTS, marketNode, items);
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

        // If marketNode is a container object without direct outcome fields, process child fields
        if (marketNode.isObject() && !hasDirectOutcomeFields(marketNode)) {
            final String baseKey = keyStr;
            marketNode.fields().forEachRemaining(entry -> {
                String subKey = baseKey.isEmpty() ? entry.getKey() : baseKey + "_" + entry.getKey();
                handleMarket(subKey, sportType, entry.getValue(), items);
            });
            return;
        }

        BetScope scope = resolveScope(keyStr, marketNode, sportType);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());

        // 1. Total Maps (Match Scope)
        if (keyStr.contains("map") && (keyStr.contains("total") || keyStr.contains("over") || keyStr.contains("under"))
                && !keyStr.contains("round") && !keyStr.contains("kill") && scope == BetScope.FULL_MATCH) {
            handleTotals(marketNode, items, "maps_total", scope, StatType.MAPS, sportType);
            return;
        }

        // 2. Map Handicap (Match Scope)
        if (keyStr.contains("map") && (keyStr.contains("handicap") || keyStr.contains("hdp") || keyStr.contains("spread"))
                && !keyStr.contains("round") && !keyStr.contains("kill") && scope == BetScope.FULL_MATCH) {
            handleHandicaps(marketNode, items, "maps_handicap", scope, StatType.MAPS, sportType);
            return;
        }

        // 3. Total Rounds (Map Scope or Match Scope)
        if (keyStr.contains("round") && (keyStr.contains("total") || keyStr.contains("over") || keyStr.contains("under"))) {
            handleTotals(marketNode, items, "rounds_total" + scopeSuffix, scope, StatType.ROUNDS, sportType);
            return;
        }

        // 4. Round Handicap (Map Scope or Match Scope)
        if (keyStr.contains("round") && (keyStr.contains("handicap") || keyStr.contains("hdp") || keyStr.contains("spread"))) {
            handleHandicaps(marketNode, items, "rounds_handicap" + scopeSuffix, scope, StatType.ROUNDS, sportType);
            return;
        }

        // 5. Total Kills
        if (keyStr.contains("kill") && (keyStr.contains("total") || keyStr.contains("over") || keyStr.contains("under"))) {
            handleTotals(marketNode, items, "kills_total" + scopeSuffix, scope, StatType.KILLS, sportType);
            return;
        }

        // 5b. Kill Handicap
        if (keyStr.contains("kill") && (keyStr.contains("handicap") || keyStr.contains("hdp") || keyStr.contains("spread"))) {
            handleHandicaps(marketNode, items, "kills_handicap" + scopeSuffix, scope, StatType.KILLS, sportType);
            return;
        }

        // 6. First Blood
        if (keyStr.contains("first_blood") || keyStr.contains("1st_blood") || keyStr.contains("firstblood")) {
            handleFirstBlood(marketNode, items, "first_blood" + scopeSuffix, scope);
            return;
        }

        // 7. Map Winner / Esports Moneyline
        if (keyStr.contains("winner") || keyStr.contains("moneyline") || keyStr.contains("1x2") || keyStr.contains("map") || keyStr.contains("to_win")) {
            handleWinner(marketNode, items, (scope == BetScope.FULL_MATCH ? "moneyline" : "map_winner" + scopeSuffix), scope);
            return;
        }

        // 8. General fallback for esports totals
        if (keyStr.contains("total") || keyStr.contains("over") || keyStr.contains("under")) {
            handleTotals(marketNode, items, "total" + scopeSuffix, scope, StatType.MATCH, sportType);
            return;
        }

        // 9. General fallback for esports handicaps
        if (keyStr.contains("handicap") || keyStr.contains("hdp") || keyStr.contains("spread")) {
            handleHandicaps(marketNode, items, "handicap" + scopeSuffix, scope, StatType.MATCH, sportType);
        }
    }

    private void handleTotals(JsonNode node, List<OddItem> items, String groupName, BetScope scope, StatType statType, SportType sportType) {
        if (node.isArray()) {
            for (JsonNode tNode : node) {
                BetScope itemScope = resolveItemScope(groupName, tNode, scope, sportType);
                String curGroup = itemScope == scope ? groupName : (groupName.replaceAll("_map_\\d+", "") + "_" + itemScope.name().toLowerCase());
                double limit = extractDouble(tNode, "limit", "line", "total");

                if (tNode.has("over")) {
                    addOddItem(items, curGroup, "OVER (" + limit + ")", tNode.path("over").asDouble(),
                            new TotalBet(itemScope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, false, statType));
                }
                if (tNode.has("under")) {
                    addOddItem(items, curGroup, "UNDER (" + limit + ")", tNode.path("under").asDouble(),
                            new TotalBet(itemScope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, false, statType));
                }

                if (tNode.has("name") || tNode.has("type")) {
                    String name = tNode.path("name").asText(tNode.path("type").asText("")).toUpperCase();
                    double odds = extractDouble(tNode, "odds", "value", "price");
                    if (limit == 0.0) {
                        limit = parseLimitFromName(name);
                    }
                    if (name.contains("OVER") || name.startsWith("O ") || name.startsWith("O(")) {
                        addOddItem(items, curGroup, "OVER (" + limit + ")", odds,
                                new TotalBet(itemScope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, false, statType));
                    } else if (name.contains("UNDER") || name.startsWith("U ") || name.startsWith("U(")) {
                        addOddItem(items, curGroup, "UNDER (" + limit + ")", odds,
                                new TotalBet(itemScope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, false, statType));
                    }
                }
            }
        } else if (node.isObject()) {
            double limit = extractDouble(node, "limit", "line", "total");
            if (node.has("over")) {
                addOddItem(items, groupName, "OVER (" + limit + ")", node.path("over").asDouble(),
                        new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, false, statType));
            }
            if (node.has("under")) {
                addOddItem(items, groupName, "UNDER (" + limit + ")", node.path("under").asDouble(),
                        new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, false, statType));
            }

            node.fields().forEachRemaining(entry -> {
                String k = entry.getKey().toLowerCase();
                double val = entry.getValue().asDouble(0.0);
                if (k.startsWith("over_") || k.startsWith("o_")) {
                    double l = parseLimitFromName(k);
                    addOddItem(items, groupName, "OVER (" + l + ")", val,
                            new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.OVER, l, false, statType));
                } else if (k.startsWith("under_") || k.startsWith("u_")) {
                    double l = parseLimitFromName(k);
                    addOddItem(items, groupName, "UNDER (" + l + ")", val,
                            new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.UNDER, l, false, statType));
                }
            });
        }
    }

    private void handleHandicaps(JsonNode node, List<OddItem> items, String groupName, BetScope scope, StatType statType, SportType sportType) {
        if (node.isArray()) {
            for (JsonNode hNode : node) {
                BetScope itemScope = resolveItemScope(groupName, hNode, scope, sportType);
                String curGroup = itemScope == scope ? groupName : (groupName.replaceAll("_map_\\d+", "") + "_" + itemScope.name().toLowerCase());
                double hdp = extractDouble(hNode, "hdp", "line", "handicap");

                if (hNode.has("home") || hNode.has("1")) {
                    double homeOdds = hNode.has("home") ? hNode.path("home").asDouble() : hNode.path("1").asDouble();
                    addOddItem(items, curGroup, "HOME (" + hdp + ")", homeOdds,
                            new HandicapBet(itemScope, HandicapBet.Outcome.TEAM1, hdp, false, statType));
                }
                if (hNode.has("away") || hNode.has("2")) {
                    double awayOdds = hNode.has("away") ? hNode.path("away").asDouble() : hNode.path("2").asDouble();
                    addOddItem(items, curGroup, "AWAY (" + (-hdp) + ")", awayOdds,
                            new HandicapBet(itemScope, HandicapBet.Outcome.TEAM2, -hdp, false, statType));
                }

                if (hNode.has("name") || hNode.has("type")) {
                    String name = hNode.path("name").asText(hNode.path("type").asText("")).toUpperCase();
                    double odds = extractDouble(hNode, "odds", "value", "price");
                    if (name.contains("HOME") || name.startsWith("1")) {
                        addOddItem(items, curGroup, "HOME (" + hdp + ")", odds,
                                new HandicapBet(itemScope, HandicapBet.Outcome.TEAM1, hdp, false, statType));
                    } else if (name.contains("AWAY") || name.startsWith("2")) {
                        addOddItem(items, curGroup, "AWAY (" + (-hdp) + ")", odds,
                                new HandicapBet(itemScope, HandicapBet.Outcome.TEAM2, -hdp, false, statType));
                    }
                }
            }
        } else if (node.isObject()) {
            double hdp = extractDouble(node, "hdp", "line", "handicap");
            if (node.has("home") || node.has("1")) {
                double homeOdds = node.has("home") ? node.path("home").asDouble() : node.path("1").asDouble();
                addOddItem(items, groupName, "HOME (" + hdp + ")", homeOdds,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM1, hdp, false, statType));
            }
            if (node.has("away") || node.has("2")) {
                double awayOdds = node.has("away") ? node.path("away").asDouble() : node.path("2").asDouble();
                addOddItem(items, groupName, "AWAY (" + (-hdp) + ")", awayOdds,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM2, -hdp, false, statType));
            }
        }
    }

    private void handleWinner(JsonNode node, List<OddItem> items, String groupName, BetScope scope) {
        if (node.isArray()) {
            boolean hasDraw = false;
            for (JsonNode child : node) {
                String n = child.path("name").asText(child.path("type").asText("")).toUpperCase();
                if (n.contains("DRAW") || n.equals("X") || n.contains("TIE")) {
                    hasDraw = true;
                    break;
                }
            }
            for (JsonNode itemNode : node) {
                String name = itemNode.path("name").asText(itemNode.path("type").asText("")).toUpperCase().trim();
                double odds = extractDouble(itemNode, "odds", "value", "price");
                if (name.contains("HOME") || name.equals("1") || name.startsWith("1 ") || name.endsWith(" 1") || name.contains("TEAM 1") || name.contains("TEAM1")) {
                    addOddItem(items, groupName, "HOME", odds,
                            hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, StatType.MATCH)
                                    : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
                } else if (name.contains("AWAY") || name.equals("2") || name.startsWith("2 ") || name.endsWith(" 2") || name.contains("TEAM 2") || name.contains("TEAM2")) {
                    addOddItem(items, groupName, "AWAY", odds,
                            hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, StatType.MATCH)
                                    : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
                } else if (name.contains("DRAW") || name.equals("X") || name.contains("TIE")) {
                    addOddItem(items, groupName, "DRAW", odds,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH));
                }
            }
        } else if (node.isObject()) {
            boolean hasDraw = node.has("draw") || node.has("x") || node.has("tie");
            double homeVal = extractDouble(node, "home", "1", "team1", "h");
            if (homeVal > 1.0) {
                addOddItem(items, groupName, "HOME", homeVal,
                        hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, StatType.MATCH)
                                : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
            }
            double awayVal = extractDouble(node, "away", "2", "team2", "a");
            if (awayVal > 1.0) {
                addOddItem(items, groupName, "AWAY", awayVal,
                        hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, StatType.MATCH)
                                : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
            }
            if (hasDraw) {
                double drawVal = extractDouble(node, "draw", "x", "tie", "d");
                if (drawVal > 1.0) {
                    addOddItem(items, groupName, "DRAW", drawVal,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH));
                }
            }
        }
    }

    private void handleFirstBlood(JsonNode node, List<OddItem> items, String groupName, BetScope scope) {
        if (node.isArray()) {
            for (JsonNode itemNode : node) {
                String name = itemNode.path("name").asText(itemNode.path("type").asText("")).toUpperCase().trim();
                double odds = extractDouble(itemNode, "odds", "value", "price");
                if (name.contains("HOME") || name.equals("1") || name.contains("TEAM 1") || name.contains("TEAM1")) {
                    addOddItem(items, groupName, "HOME", odds,
                            new BinaryMarketBet(scope, BetSubject.TEAM1, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD));
                } else if (name.contains("AWAY") || name.equals("2") || name.contains("TEAM 2") || name.contains("TEAM2")) {
                    addOddItem(items, groupName, "AWAY", odds,
                            new BinaryMarketBet(scope, BetSubject.TEAM2, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD));
                }
            }
        } else if (node.isObject()) {
            double homeVal = extractDouble(node, "home", "1", "team1");
            if (homeVal > 1.0) {
                addOddItem(items, groupName, "HOME", homeVal,
                        new BinaryMarketBet(scope, BetSubject.TEAM1, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD));
            }
            double awayVal = extractDouble(node, "away", "2", "team2");
            if (awayVal > 1.0) {
                addOddItem(items, groupName, "AWAY", awayVal,
                        new BinaryMarketBet(scope, BetSubject.TEAM2, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD));
            }
        }
    }

    private BetScope resolveItemScope(String keyStr, JsonNode itemNode, BetScope defaultScope, SportType sportType) {
        if (itemNode != null) {
            BetScope itemScope = resolveScope(null, itemNode, sportType);
            if (itemScope != BetScope.FULL_MATCH) {
                return itemScope;
            }
        }
        return defaultScope;
    }
}
