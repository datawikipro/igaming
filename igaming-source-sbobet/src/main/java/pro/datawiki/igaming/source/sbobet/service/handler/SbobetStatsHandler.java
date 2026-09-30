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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(10)
public class SbobetStatsHandler extends AbstractSbobetMarketHandler {

    private static final Pattern LIMIT_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)");

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String k = marketKey.toLowerCase();
        return k.contains("corner") || k.contains("card") || k.contains("yellow") || k.contains("booking")
                || k.contains("foul") || k.contains("offside") || k.contains("shot");
    }

    @Override
    public boolean supports(String marketKey, SportType sportType) {
        return supports(marketKey);
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
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

        // If marketNode is a container object without direct outcome fields, process child fields
        if (marketNode.isObject() && !hasDirectOutcomeFields(marketNode)) {
            final String baseKey = keyStr;
            marketNode.fields().forEachRemaining(entry -> {
                String subKey = baseKey.isEmpty() ? entry.getKey() : baseKey + "_" + entry.getKey();
                handleMarket(subKey, entry.getValue(), items);
            });
            return;
        }

        StatType statType = resolveStatType(keyStr);

        boolean isTotal = keyStr.contains("total") || keyStr.contains("over") || keyStr.contains("under")
                || marketNode.has("limit") || hasAnyChildWithField(marketNode, "limit") || hasAnyChildWithField(marketNode, "over");
        boolean isHandicap = keyStr.contains("handicap") || keyStr.contains("hdp") || keyStr.contains("spread")
                || marketNode.has("hdp") || hasAnyChildWithField(marketNode, "hdp");
        boolean isDoubleChance = keyStr.contains("double_chance") || keyStr.contains("doublechance")
                || keyStr.contains("dc") || keyStr.endsWith("_dc");
        boolean isDrawNoBet = keyStr.contains("draw_no_bet") || keyStr.contains("drawnobet")
                || keyStr.contains("dnb") || keyStr.endsWith("_dnb");

        if (isTotal) {
            handleTotals(keyStr, marketNode, items, statType);
        } else if (isHandicap) {
            handleHandicaps(keyStr, marketNode, items, statType);
        } else if (isDoubleChance) {
            handleDoubleChance(keyStr, marketNode, items, statType);
        } else if (isDrawNoBet) {
            handleDrawNoBet(keyStr, marketNode, items, statType);
        } else {
            handle1X2(keyStr, marketNode, items, statType);
        }
    }

    private void handleTotals(String keyStr, JsonNode marketNode, List<OddItem> items, StatType statType) {
        String statPrefix = getStatPrefix(statType);

        if (marketNode.isArray()) {
            for (JsonNode tNode : marketNode) {
                BetScope scope = resolveItemScope(keyStr, marketNode, tNode);
                String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
                String groupName = statPrefix + "_total" + scopeSuffix;

                double limit = extractDouble(tNode, "limit", "line", "total");

                if (tNode.has("over")) {
                    addOddItem(items, groupName, "OVER (" + limit + ")", tNode.path("over").asDouble(),
                            new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, false, statType));
                }
                if (tNode.has("under")) {
                    addOddItem(items, groupName, "UNDER (" + limit + ")", tNode.path("under").asDouble(),
                            new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, false, statType));
                }

                // If element is a single outcome (e.g. {"name": "Over 9.5", "odds": 1.85})
                if (tNode.has("name") || tNode.has("type")) {
                    String name = tNode.path("name").asText(tNode.path("type").asText("")).toUpperCase();
                    double odds = extractDouble(tNode, "odds", "value", "price");
                    if (limit == 0.0) {
                        limit = parseLimitFromName(name);
                    }
                    if (name.contains("OVER")) {
                        addOddItem(items, groupName, "OVER (" + limit + ")", odds,
                                new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, false, statType));
                    } else if (name.contains("UNDER")) {
                        addOddItem(items, groupName, "UNDER (" + limit + ")", odds,
                                new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, false, statType));
                    }
                }
            }
        } else if (marketNode.isObject()) {
            BetScope scope = resolveItemScope(keyStr, marketNode, null);
            String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
            String groupName = statPrefix + "_total" + scopeSuffix;
            double limit = extractDouble(marketNode, "limit", "line", "total");

            if (marketNode.has("over")) {
                addOddItem(items, groupName, "OVER (" + limit + ")", marketNode.path("over").asDouble(),
                        new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.OVER, limit, false, statType));
            }
            if (marketNode.has("under")) {
                addOddItem(items, groupName, "UNDER (" + limit + ")", marketNode.path("under").asDouble(),
                        new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.UNDER, limit, false, statType));
            }

            // Also check individual fields if object has over_X / under_X
            marketNode.fields().forEachRemaining(entry -> {
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

    private void handleHandicaps(String keyStr, JsonNode marketNode, List<OddItem> items, StatType statType) {
        String statPrefix = getStatPrefix(statType);

        if (marketNode.isArray()) {
            for (JsonNode hNode : marketNode) {
                BetScope scope = resolveItemScope(keyStr, marketNode, hNode);
                String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
                String groupName = statPrefix + "_handicap" + scopeSuffix;

                double hdp = extractDouble(hNode, "hdp", "line", "handicap");

                if (hNode.has("home") || hNode.has("1")) {
                    double homeOdds = hNode.has("home") ? hNode.path("home").asDouble() : hNode.path("1").asDouble();
                    addOddItem(items, groupName, "HOME (" + hdp + ")", homeOdds,
                            new HandicapBet(scope, HandicapBet.Outcome.TEAM1, hdp, false, statType));
                }
                if (hNode.has("away") || hNode.has("2")) {
                    double awayOdds = hNode.has("away") ? hNode.path("away").asDouble() : hNode.path("2").asDouble();
                    addOddItem(items, groupName, "AWAY (" + (-hdp) + ")", awayOdds,
                            new HandicapBet(scope, HandicapBet.Outcome.TEAM2, -hdp, false, statType));
                }

                // If element is a single outcome
                if (hNode.has("name") || hNode.has("type")) {
                    String name = hNode.path("name").asText(hNode.path("type").asText("")).toUpperCase();
                    double odds = extractDouble(hNode, "odds", "value", "price");
                    if (name.contains("HOME") || name.startsWith("1")) {
                        addOddItem(items, groupName, "HOME (" + hdp + ")", odds,
                                new HandicapBet(scope, HandicapBet.Outcome.TEAM1, hdp, false, statType));
                    } else if (name.contains("AWAY") || name.startsWith("2")) {
                        addOddItem(items, groupName, "AWAY (" + (-hdp) + ")", odds,
                                new HandicapBet(scope, HandicapBet.Outcome.TEAM2, -hdp, false, statType));
                    }
                }
            }
        } else if (marketNode.isObject()) {
            BetScope scope = resolveItemScope(keyStr, marketNode, null);
            String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
            String groupName = statPrefix + "_handicap" + scopeSuffix;

            double hdp = extractDouble(marketNode, "hdp", "line", "handicap");
            if (marketNode.has("home") || marketNode.has("1")) {
                double homeOdds = marketNode.has("home") ? marketNode.path("home").asDouble() : marketNode.path("1").asDouble();
                addOddItem(items, groupName, "HOME (" + hdp + ")", homeOdds,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM1, hdp, false, statType));
            }
            if (marketNode.has("away") || marketNode.has("2")) {
                double awayOdds = marketNode.has("away") ? marketNode.path("away").asDouble() : marketNode.path("2").asDouble();
                addOddItem(items, groupName, "AWAY (" + (-hdp) + ")", awayOdds,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM2, -hdp, false, statType));
            }
        }
    }

    private void handle1X2(String keyStr, JsonNode marketNode, List<OddItem> items, StatType statType) {
        String statPrefix = getStatPrefix(statType);

        if (marketNode.isArray()) {
            boolean hasDraw = false;
            for (JsonNode child : marketNode) {
                String n = child.path("name").asText(child.path("type").asText("")).toUpperCase();
                if (n.contains("DRAW") || n.equals("X") || n.contains("TIE")) {
                    hasDraw = true;
                    break;
                }
            }

            for (JsonNode itemNode : marketNode) {
                BetScope scope = resolveItemScope(keyStr, marketNode, itemNode);
                String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
                String groupName = statPrefix + "_1x2" + scopeSuffix;

                String name = itemNode.path("name").asText(itemNode.path("type").asText("")).toUpperCase().trim();
                double odds = extractDouble(itemNode, "odds", "value", "price");

                if (name.contains("HOME") || name.equals("1") || name.startsWith("1 ") || name.endsWith(" 1")) {
                    addOddItem(items, groupName, "HOME", odds,
                            hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, statType)
                                    : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, statType));
                } else if (name.contains("AWAY") || name.equals("2") || name.startsWith("2 ") || name.endsWith(" 2")) {
                    addOddItem(items, groupName, "AWAY", odds,
                            hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, statType)
                                    : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, statType));
                } else if (name.contains("DRAW") || name.equals("X") || name.contains("TIE")) {
                    addOddItem(items, groupName, "DRAW", odds,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, statType));
                }
            }
        } else if (marketNode.isObject()) {
            BetScope scope = resolveItemScope(keyStr, marketNode, null);
            String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
            String groupName = statPrefix + "_1x2" + scopeSuffix;

            boolean hasDraw = marketNode.has("draw") || marketNode.has("x") || marketNode.has("tie");

            double homeVal = extractDouble(marketNode, "home", "1", "team1", "h");
            if (homeVal > 1.0) {
                addOddItem(items, groupName, "HOME", homeVal,
                        hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, statType)
                                : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, statType));
            }

            double awayVal = extractDouble(marketNode, "away", "2", "team2", "a");
            if (awayVal > 1.0) {
                addOddItem(items, groupName, "AWAY", awayVal,
                        hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, statType)
                                : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, statType));
            }

            if (hasDraw) {
                double drawVal = extractDouble(marketNode, "draw", "x", "tie", "d");
                if (drawVal > 1.0) {
                    addOddItem(items, groupName, "DRAW", drawVal,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, statType));
                }
            }
        }
    }

    private void handleDoubleChance(String keyStr, JsonNode marketNode, List<OddItem> items, StatType statType) {
        String statPrefix = getStatPrefix(statType);

        if (marketNode.isObject()) {
            BetScope scope = resolveItemScope(keyStr, marketNode, null);
            String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
            String groupName = statPrefix + "_double_chance" + scopeSuffix;

            marketNode.fields().forEachRemaining(entry -> {
                String k = entry.getKey().toLowerCase().replace("-", "_").replace(" ", "_");
                double val = entry.getValue().asDouble(0.0);
                if (k.equals("1x") || k.contains("home_draw") || k.contains("1_x")) {
                    addOddItem(items, groupName, "1X", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, statType));
                } else if (k.equals("12") || k.contains("home_away") || k.contains("1_2")) {
                    addOddItem(items, groupName, "12", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, statType));
                } else if (k.equals("x2") || k.contains("draw_away") || k.contains("x_2")) {
                    addOddItem(items, groupName, "X2", val,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, statType));
                }
            });
        }
    }

    private void handleDrawNoBet(String keyStr, JsonNode marketNode, List<OddItem> items, StatType statType) {
        String statPrefix = getStatPrefix(statType);

        if (marketNode.isObject()) {
            BetScope scope = resolveItemScope(keyStr, marketNode, null);
            String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
            String groupName = statPrefix + "_draw_no_bet" + scopeSuffix;

            double homeVal = extractDouble(marketNode, "home", "1", "team1");
            if (homeVal > 1.0) {
                addOddItem(items, groupName, "HOME", homeVal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, statType));
            }
            double awayVal = extractDouble(marketNode, "away", "2", "team2");
            if (awayVal > 1.0) {
                addOddItem(items, groupName, "AWAY", awayVal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, statType));
            }
        }
    }

    private StatType resolveStatType(String keyStr) {
        if (keyStr.contains("corner")) {
            return StatType.CORNERS;
        } else if (keyStr.contains("yellow")) {
            return StatType.YELLOW_CARDS;
        } else if (keyStr.contains("card") || keyStr.contains("booking")) {
            return StatType.CARDS;
        } else if (keyStr.contains("foul")) {
            return StatType.FOULS;
        } else if (keyStr.contains("offside")) {
            return StatType.OFFSIDES;
        } else if (keyStr.contains("shot") || keyStr.contains("sot")) {
            return StatType.SHOTS_ON_TARGET;
        }
        return StatType.CORNERS;
    }

    private String getStatPrefix(StatType statType) {
        if (statType == StatType.CORNERS) return "corners";
        if (statType == StatType.YELLOW_CARDS) return "yellow_cards";
        if (statType == StatType.CARDS) return "cards";
        if (statType == StatType.FOULS) return "fouls";
        if (statType == StatType.OFFSIDES) return "offsides";
        if (statType == StatType.SHOTS_ON_TARGET) return "shots_on_target";
        return "stats";
    }

    private BetScope resolveItemScope(String keyStr, JsonNode marketNode, JsonNode itemNode) {
        if (itemNode != null) {
            BetScope itemScope = resolveScope(null, itemNode);
            if (itemScope != BetScope.FULL_MATCH) {
                return itemScope;
            }
        }
        return resolveScope(keyStr, marketNode);
    }

    private boolean hasAnyChildWithField(JsonNode node, String fieldName) {
        if (node == null || !node.isArray()) return false;
        for (JsonNode child : node) {
            if (child.has(fieldName)) return true;
        }
        return false;
    }
}
