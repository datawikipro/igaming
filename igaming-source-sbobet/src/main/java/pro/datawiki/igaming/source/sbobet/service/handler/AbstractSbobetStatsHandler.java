package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
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
        handle(null, marketNode, null, items);
    }

    @Override
    public void handle(String marketKey, JsonNode marketNode, SportType sportType, List<OddItem> items) {
        if (marketNode == null) return;
        StatType statType = getStatType();
        String prefix = getStatPrefix();

        if (marketNode.isArray()) {
            for (JsonNode child : marketNode) {
                processSingleNode(marketKey, child, statType, prefix, items);
            }
        } else if (marketNode.isObject()) {
            processSingleNode(marketKey, marketNode, statType, prefix, items);
        }
    }

    protected void processSingleNode(String marketKey, JsonNode node, StatType statType, String prefix, List<OddItem> items) {
        if (node == null || !node.isObject()) return;

        BetScope scope = resolveScope(marketKey, node);
        String scopeSuffix = getScopeSuffix(scope);

        // Check if node contains sub-market containers
        boolean hasSubMarkets = node.has("totals") || node.has("handicaps") || node.has("double_chance")
                || node.has("dc") || node.has("moneyline") || node.has("1x2") || node.has("dnb")
                || node.has("draw_no_bet") || node.has("home_totals") || node.has("away_totals")
                || node.has("team1_totals") || node.has("team2_totals");

        if (hasSubMarkets) {
            if (node.has("totals")) {
                processTotalsNode(marketKey, node.get("totals"), scope, prefix, statType, BetSubject.MATCH, items);
            }
            if (node.has("home_totals")) {
                processTotalsNode(marketKey, node.get("home_totals"), scope, prefix + "_team1", statType, BetSubject.TEAM1, items);
            } else if (node.has("team1_totals")) {
                processTotalsNode(marketKey, node.get("team1_totals"), scope, prefix + "_team1", statType, BetSubject.TEAM1, items);
            }
            if (node.has("away_totals")) {
                processTotalsNode(marketKey, node.get("away_totals"), scope, prefix + "_team2", statType, BetSubject.TEAM2, items);
            } else if (node.has("team2_totals")) {
                processTotalsNode(marketKey, node.get("team2_totals"), scope, prefix + "_team2", statType, BetSubject.TEAM2, items);
            }

            if (node.has("handicaps")) {
                processHandicapsNode(marketKey, node.get("handicaps"), scope, prefix, statType, items);
            }
            if (node.has("double_chance")) {
                processDoubleChanceNode(marketKey, node.get("double_chance"), scope, prefix, statType, items);
            } else if (node.has("dc")) {
                processDoubleChanceNode(marketKey, node.get("dc"), scope, prefix, statType, items);
            }
            if (node.has("moneyline")) {
                processMoneylineNode(marketKey, node.get("moneyline"), scope, scopeSuffix, prefix, statType, items);
            } else if (node.has("1x2")) {
                processMoneylineNode(marketKey, node.get("1x2"), scope, scopeSuffix, prefix, statType, items);
            }
            if (node.has("dnb")) {
                processDnbNode(marketKey, node.get("dnb"), scope, prefix, statType, items);
            } else if (node.has("draw_no_bet")) {
                processDnbNode(marketKey, node.get("draw_no_bet"), scope, prefix, statType, items);
            }

            // Top-level 1X2 in a container node (e.g. {"home": 2.1, "draw": 7.0, "away": 1.9, "totals": [...]})
            if (!node.has("hdp") && !node.has("limit") && (node.has("home") || node.has("draw") || node.has("away"))) {
                processMoneylineNode(marketKey, node, scope, scopeSuffix, prefix, statType, items);
            }
            return;
        }

        // Direct node handling
        String lowerKey = marketKey != null ? marketKey.toLowerCase() : "";

        // 1. Direct Handicap
        if (node.has("hdp") || lowerKey.contains("handicap") || lowerKey.contains("spread")) {
            processSingleHandicap(marketKey, node, scope, prefix, statType, items);
            return;
        }

        // 2. Direct Total
        if (node.has("over") || node.has("under") || node.has("limit") || lowerKey.contains("total") || lowerKey.contains("over_under")) {
            BetSubject subject = resolveSubject(lowerKey);
            String subjPrefix = subject == BetSubject.TEAM1 ? "_team1" : (subject == BetSubject.TEAM2 ? "_team2" : "");
            processSingleTotal(marketKey, node, scope, prefix + subjPrefix, statType, subject, items);
            return;
        }

        // 3. Direct Double Chance
        if (node.has("1x") || node.has("x2") || lowerKey.contains("double_chance") || lowerKey.contains("dc")) {
            processDoubleChanceNode(marketKey, node, scope, prefix, statType, items);
            return;
        }

        // 4. Direct DNB
        if (lowerKey.contains("draw_no_bet") || lowerKey.contains("dnb")) {
            processDnbNode(marketKey, node, scope, prefix, statType, items);
            return;
        }

        // 5. Prices array
        if (node.has("prices") && node.get("prices").isArray()) {
            processPrices(marketKey, node, scope, scopeSuffix, prefix, statType, items);
            return;
        }

        // 6. Direct 1X2 / Moneyline
        if (node.has("home") || node.has("away") || node.has("draw") || node.has("1") || node.has("2")) {
            processMoneylineNode(marketKey, node, scope, scopeSuffix, prefix, statType, items);
        }
    }

    protected void processMoneylineNode(String marketKey, JsonNode node, BetScope baseScope, String defaultScopeSuffix,
                                        String prefix, StatType statType, List<OddItem> items) {
        if (node == null || !node.isObject()) return;
        BetScope scope = resolveScope(marketKey, node);
        if (scope == BetScope.FULL_MATCH && baseScope != BetScope.FULL_MATCH) {
            scope = baseScope;
        }
        String scopeSuffix = getScopeSuffix(scope);
        String groupName = prefix + "_moneyline" + scopeSuffix;

        if (node.has("prices") && node.get("prices").isArray()) {
            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("1") || des.equals("home") || des.equals("h")) {
                    addMatchResult(items, groupName, "HOME", price, scope, MatchResultBet.Outcome.WIN1, statType);
                } else if (des.equals("x") || des.equals("draw") || des.equals("d")) {
                    addMatchResult(items, groupName, "DRAW", price, scope, MatchResultBet.Outcome.DRAW, statType);
                } else if (des.equals("2") || des.equals("away") || des.equals("a")) {
                    addMatchResult(items, groupName, "AWAY", price, scope, MatchResultBet.Outcome.WIN2, statType);
                }
            }
            return;
        }

        double home = extractDouble(node, "home", "1", "win1", "h", "team1");
        double draw = extractDouble(node, "draw", "x", "tie", "d");
        double away = extractDouble(node, "away", "2", "win2", "a", "team2");

        if (home > 1.0) {
            MatchResultBet.Outcome outcome = (draw > 1.0) ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            addMatchResult(items, groupName, "HOME", home, scope, outcome, statType);
        }
        if (draw > 1.0) {
            addMatchResult(items, groupName, "DRAW", draw, scope, MatchResultBet.Outcome.DRAW, statType);
        }
        if (away > 1.0) {
            MatchResultBet.Outcome outcome = (draw > 1.0) ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            addMatchResult(items, groupName, "AWAY", away, scope, outcome, statType);
        }
    }

    protected void processTotalsNode(String marketKey, JsonNode totalsNode, BetScope baseScope, String prefix,
                                     StatType statType, BetSubject subject, List<OddItem> items) {
        if (totalsNode == null) return;
        if (totalsNode.isArray()) {
            for (JsonNode tNode : totalsNode) {
                processSingleTotal(marketKey, tNode, baseScope, prefix, statType, subject, items);
            }
        } else if (totalsNode.isObject()) {
            processSingleTotal(marketKey, totalsNode, baseScope, prefix, statType, subject, items);
        }
    }

    protected void processSingleTotal(String marketKey, JsonNode node, BetScope baseScope, String prefix,
                                      StatType statType, BetSubject subject, List<OddItem> items) {
        if (node == null || !node.isObject()) return;
        BetScope scope = resolveScope(marketKey, node);
        if (scope == BetScope.FULL_MATCH && baseScope != BetScope.FULL_MATCH) {
            scope = baseScope;
        }
        String scopeSuffix = getScopeSuffix(scope);
        String groupName = prefix + "_total" + scopeSuffix;
        double limit = extractDouble(node, "limit", "point", "line", "total");

        if (node.has("prices") && node.get("prices").isArray()) {
            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("over") || des.equals("o")) {
                    addTotal(items, groupName, "OVER (" + limit + ")", price, scope, subject, TotalBet.Direction.OVER, limit, true, statType);
                } else if (des.equals("under") || des.equals("u")) {
                    addTotal(items, groupName, "UNDER (" + limit + ")", price, scope, subject, TotalBet.Direction.UNDER, limit, true, statType);
                }
            }
            return;
        }

        double over = extractDouble(node, "over", "o");
        double under = extractDouble(node, "under", "u");

        if (over > 1.0) {
            addTotal(items, groupName, "OVER (" + limit + ")", over, scope, subject, TotalBet.Direction.OVER, limit, true, statType);
        }
        if (under > 1.0) {
            addTotal(items, groupName, "UNDER (" + limit + ")", under, scope, subject, TotalBet.Direction.UNDER, limit, true, statType);
        }
    }

    protected void processHandicapsNode(String marketKey, JsonNode handicapsNode, BetScope baseScope, String prefix,
                                        StatType statType, List<OddItem> items) {
        if (handicapsNode == null) return;
        if (handicapsNode.isArray()) {
            for (JsonNode hNode : handicapsNode) {
                processSingleHandicap(marketKey, hNode, baseScope, prefix, statType, items);
            }
        } else if (handicapsNode.isObject()) {
            processSingleHandicap(marketKey, handicapsNode, baseScope, prefix, statType, items);
        }
    }

    protected void processSingleHandicap(String marketKey, JsonNode node, BetScope baseScope, String prefix,
                                         StatType statType, List<OddItem> items) {
        if (node == null || !node.isObject()) return;
        BetScope scope = resolveScope(marketKey, node);
        if (scope == BetScope.FULL_MATCH && baseScope != BetScope.FULL_MATCH) {
            scope = baseScope;
        }
        String scopeSuffix = getScopeSuffix(scope);
        String groupName = prefix + "_handicap" + scopeSuffix;
        double hdp = extractDouble(node, "hdp", "handicap", "spread", "line");

        if (node.has("prices") && node.get("prices").isArray()) {
            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("home") || des.equals("1") || des.equals("h")) {
                    addHandicap(items, groupName, "HOME (" + hdp + ")", price, scope, HandicapBet.Outcome.TEAM1, hdp, true, statType);
                } else if (des.equals("away") || des.equals("2") || des.equals("a")) {
                    addHandicap(items, groupName, "AWAY (" + (-hdp) + ")", price, scope, HandicapBet.Outcome.TEAM2, -hdp, true, statType);
                }
            }
            return;
        }

        double home = extractDouble(node, "home", "1", "h", "team1");
        double away = extractDouble(node, "away", "2", "a", "team2");

        if (home > 1.0) {
            addHandicap(items, groupName, "HOME (" + hdp + ")", home, scope, HandicapBet.Outcome.TEAM1, hdp, true, statType);
        }
        if (away > 1.0) {
            addHandicap(items, groupName, "AWAY (" + (-hdp) + ")", away, scope, HandicapBet.Outcome.TEAM2, -hdp, true, statType);
        }
    }

    protected void processDoubleChanceNode(String marketKey, JsonNode node, BetScope baseScope, String prefix,
                                           StatType statType, List<OddItem> items) {
        if (node == null || !node.isObject()) return;
        BetScope scope = resolveScope(marketKey, node);
        if (scope == BetScope.FULL_MATCH && baseScope != BetScope.FULL_MATCH) {
            scope = baseScope;
        }
        String scopeSuffix = getScopeSuffix(scope);
        String groupName = prefix + "_double_chance" + scopeSuffix;

        if (node.has("prices") && node.get("prices").isArray()) {
            for (JsonNode p : node.get("prices")) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("1x") || des.equals("hd")) {
                    addMatchResult(items, groupName, "1X", price, scope, MatchResultBet.Outcome.DC_1X, statType);
                } else if (des.equals("12") || des.equals("ha")) {
                    addMatchResult(items, groupName, "12", price, scope, MatchResultBet.Outcome.DC_12, statType);
                } else if (des.equals("x2") || des.equals("da") || des.equals("2x")) {
                    addMatchResult(items, groupName, "X2", price, scope, MatchResultBet.Outcome.DC_X2, statType);
                }
            }
            return;
        }

        double val1x = extractDouble(node, "1x", "1X", "home_draw", "homeDraw", "dc_1x", "hd");
        double val12 = extractDouble(node, "12", "home_away", "homeAway", "dc_12", "ha");
        double valX2 = extractDouble(node, "x2", "X2", "draw_away", "drawAway", "2x", "dc_x2", "da");

        if (val1x > 1.0) {
            addMatchResult(items, groupName, "1X", val1x, scope, MatchResultBet.Outcome.DC_1X, statType);
        }
        if (val12 > 1.0) {
            addMatchResult(items, groupName, "12", val12, scope, MatchResultBet.Outcome.DC_12, statType);
        }
        if (valX2 > 1.0) {
            addMatchResult(items, groupName, "X2", valX2, scope, MatchResultBet.Outcome.DC_X2, statType);
        }
    }

    protected void processDnbNode(String marketKey, JsonNode node, BetScope baseScope, String prefix,
                                  StatType statType, List<OddItem> items) {
        if (node == null || !node.isObject()) return;
        BetScope scope = resolveScope(marketKey, node);
        if (scope == BetScope.FULL_MATCH && baseScope != BetScope.FULL_MATCH) {
            scope = baseScope;
        }
        String scopeSuffix = getScopeSuffix(scope);
        String groupName = prefix + "_draw_no_bet" + scopeSuffix;

        double home = extractDouble(node, "home", "1", "h", "team1");
        double away = extractDouble(node, "away", "2", "a", "team2");

        if (home > 1.0) {
            addHandicap(items, groupName, "HOME (0.0)", home, scope, HandicapBet.Outcome.TEAM1, 0.0, true, statType);
        }
        if (away > 1.0) {
            addHandicap(items, groupName, "AWAY (0.0)", away, scope, HandicapBet.Outcome.TEAM2, 0.0, true, statType);
        }
    }

    protected void processPrices(String marketKey, JsonNode node, BetScope scope, String scopeSuffix,
                                 String prefix, StatType statType, List<OddItem> items) {
        JsonNode prices = node.path("prices");
        if (!prices.isArray()) return;

        String lowerKey = marketKey != null ? marketKey.toLowerCase() : "";

        // Totals prices
        if (node.has("limit") || lowerKey.contains("total")) {
            double limit = extractDouble(node, "limit", "point", "line", "total");
            BetSubject subject = resolveSubject(lowerKey);
            String subjPrefix = subject == BetSubject.TEAM1 ? "_team1" : (subject == BetSubject.TEAM2 ? "_team2" : "");
            String group = prefix + subjPrefix + "_total" + scopeSuffix;
            for (JsonNode p : prices) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("over") || des.equals("o")) {
                    addTotal(items, group, "OVER (" + limit + ")", price, scope, subject, TotalBet.Direction.OVER, limit, true, statType);
                } else if (des.equals("under") || des.equals("u")) {
                    addTotal(items, group, "UNDER (" + limit + ")", price, scope, subject, TotalBet.Direction.UNDER, limit, true, statType);
                }
            }
            return;
        }

        // Handicap prices
        if (node.has("hdp") || lowerKey.contains("handicap") || lowerKey.contains("spread")) {
            double hdp = extractDouble(node, "hdp", "handicap", "spread", "line");
            String group = prefix + "_handicap" + scopeSuffix;
            for (JsonNode p : prices) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("home") || des.equals("1") || des.equals("h")) {
                    addHandicap(items, group, "HOME (" + hdp + ")", price, scope, HandicapBet.Outcome.TEAM1, hdp, true, statType);
                } else if (des.equals("away") || des.equals("2") || des.equals("a")) {
                    addHandicap(items, group, "AWAY (" + (-hdp) + ")", price, scope, HandicapBet.Outcome.TEAM2, -hdp, true, statType);
                }
            }
            return;
        }

        // Double chance prices
        if (lowerKey.contains("double_chance") || lowerKey.contains("dc")) {
            String group = prefix + "_double_chance" + scopeSuffix;
            for (JsonNode p : prices) {
                String des = p.path("designation").asText("").toLowerCase();
                double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
                if (price <= 1.0) continue;
                if (des.equals("1x") || des.equals("hd")) {
                    addMatchResult(items, group, "1X", price, scope, MatchResultBet.Outcome.DC_1X, statType);
                } else if (des.equals("12") || des.equals("ha")) {
                    addMatchResult(items, group, "12", price, scope, MatchResultBet.Outcome.DC_12, statType);
                } else if (des.equals("x2") || des.equals("da") || des.equals("2x")) {
                    addMatchResult(items, group, "X2", price, scope, MatchResultBet.Outcome.DC_X2, statType);
                }
            }
            return;
        }

        // 1X2 / Moneyline prices
        String group = prefix + "_moneyline" + scopeSuffix;
        for (JsonNode p : prices) {
            String des = p.path("designation").asText("").toLowerCase();
            double price = p.has("price") ? p.path("price").asDouble() : p.path("odds").asDouble();
            if (price <= 1.0) continue;
            if (des.equals("1") || des.equals("home") || des.equals("h")) {
                addMatchResult(items, group, "HOME", price, scope, MatchResultBet.Outcome.WIN1, statType);
            } else if (des.equals("x") || des.equals("draw") || des.equals("d")) {
                addMatchResult(items, group, "DRAW", price, scope, MatchResultBet.Outcome.DRAW, statType);
            } else if (des.equals("2") || des.equals("away") || des.equals("a")) {
                addMatchResult(items, group, "AWAY", price, scope, MatchResultBet.Outcome.WIN2, statType);
            }
        }
    }

    private BetSubject resolveSubject(String lowerKey) {
        if (lowerKey != null) {
            if (lowerKey.contains("home") || lowerKey.contains("team1") || lowerKey.contains("t1")) {
                return BetSubject.TEAM1;
            }
            if (lowerKey.contains("away") || lowerKey.contains("team2") || lowerKey.contains("t2")) {
                return BetSubject.TEAM2;
            }
        }
        return BetSubject.MATCH;
    }
}
