package pro.datawiki.igaming.source.smarkets.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.smarkets.dto.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pro.datawiki.igaming.source.core.mapper.rule.LineParamParser;

@Component
@Slf4j
@RequiredArgsConstructor
public class SmarketsOddsMapper extends AbstractBetTypeMapper {

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "smarkets".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(SmarketsEvent event,
                                                    List<SmarketsMarket> markets,
                                                    Map<String, List<SmarketsContract>> contractsByMarketId,
                                                    Map<String, SmarketsContractQuotes> quotesByContractId) {
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("smarkets");
        request.setRegions(List.of(BookmakerRegion.EU, BookmakerRegion.GLOBAL));
        request.setExternalEventId(event.getId());

        SportType sportType = resolveSportType(event.getType());
        request.setSportType(sportType);
        request.setSportName(sportType.name());
        request.setLeagueName(event.getSlug() != null ? event.getSlug() : "Smarkets");

        // Parse teams from "Team1 vs Team2", "Team1 vs. Team2", "Team1 v Team2", or "Team1 - Team2"
        String team1 = "Home Team";
        String team2 = "Away Team";
        if (event.getName() != null) {
            String name = event.getName();
            if (name.contains(" vs. ")) {
                String[] parts = name.split(" vs\\. ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (name.contains(" vs ")) {
                String[] parts = name.split(" vs ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (name.contains(" - ")) {
                String[] parts = name.split(" - ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (name.contains(" @ ")) {
                String[] parts = name.split(" @ ", 2);
                team2 = parts[0].trim();
                team1 = parts[1].trim();
            } else if (name.contains(" v ")) {
                String[] parts = name.split(" v ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive("live".equalsIgnoreCase(event.getState()));
        if (event.getStartDatetime() != null) {
            try {
                request.setStartTime(Instant.parse(event.getStartDatetime()).toEpochMilli());
            } catch (Exception e) {
                request.setStartTime(Instant.now().toEpochMilli());
            }
        } else {
            request.setStartTime(Instant.now().toEpochMilli());
        }

        List<OddItem> oddItems = new ArrayList<>();
        boolean winnerMapped = false;
        boolean doubleChanceMapped = false;
        boolean bttsMapped = false;

        for (SmarketsMarket market : markets) {
            List<SmarketsContract> contracts = contractsByMarketId.get(market.getId());
            if (contracts == null || contracts.isEmpty()) {
                continue;
            }

            String marketName = market.getName() != null ? market.getName().toLowerCase().trim() : "";
            String marketTypeName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                    market.getMarketType().getName().toLowerCase().trim() : "";

            // 1. Full-time result / Winner (1X2)
            if (!winnerMapped && isFullTimeWinner3Way(marketTypeName, marketName)) {
                mapWinnerMarket(contracts, quotesByContractId, team1, team2, true, oddItems);
                winnerMapped = true;
            }
            // 2. Full-time 2-way Match Winner / Moneyline (12)
            else if (!winnerMapped && isFullTimeWinner2Way(marketTypeName, marketName)) {
                mapWinnerMarket(contracts, quotesByContractId, team1, team2, false, oddItems);
                winnerMapped = true;
            }
            // 3. Double Chance (1X, 12, X2)
            else if (!doubleChanceMapped && isDoubleChance(marketTypeName, marketName)) {
                mapDoubleChanceMarket(contracts, quotesByContractId, oddItems);
                doubleChanceMapped = true;
            }
            // 4. Over/Under Total
            else if (isFullTimeTotal(marketTypeName, marketName)) {
                Double totalParam = extractParam(market.getMarketType() != null ? market.getMarketType().getParam() : null, market.getName());
                mapTotalMarket(contracts, quotesByContractId, totalParam, oddItems);
            }
            // 5. Both Teams to Score (BTTS)
            else if (!bttsMapped && isBtts(marketTypeName, marketName)) {
                mapBttsMarket(contracts, quotesByContractId, oddItems);
                bttsMapped = true;
            }
        }

        request.setOdds(oddItems);
        return request;
    }

    private boolean isFullTimeWinner3Way(String type, String name) {
        if (name.contains("half") || name.contains("bracket") || name.contains("corner") || name.contains("card")
                || name.contains("period") || name.contains("minute")
                || type.contains("half") || type.contains("bracket") || type.contains("corner") || type.contains("card")) {
            return false;
        }
        if ("winner_3_way".equals(type)) return true;
        return "full-time result".equals(name) || "full time result".equals(name) || "match result".equals(name);
    }

    private boolean isFullTimeWinner2Way(String type, String name) {
        if (name.contains("half") || name.contains("bracket") || name.contains("corner") || name.contains("card")
                || name.contains("period") || name.contains("minute")
                || type.contains("half") || type.contains("bracket") || type.contains("corner") || type.contains("card") || type.contains("dnb")) {
            return false;
        }
        if ("winner_2_way".equals(type) || "moneyline".equals(type)) return true;
        return "match winner".equals(name) || "moneyline".equals(name) || "winner".equals(name);
    }

    private boolean isDoubleChance(String type, String name) {
        if (name.contains("half") || type.contains("half") || name.contains("bracket") || type.contains("bracket")) {
            return false;
        }
        if ("double_chance".equals(type)) return true;
        return "double chance".equals(name);
    }

    private boolean isFullTimeTotal(String type, String name) {
        if (name.contains("corner") || name.contains("card") || name.contains("half")
                || name.contains("team") || name.contains("period") || name.contains("quarter")
                || name.contains("minute") || name.contains("booking") || name.contains("shot")
                || type.contains("half") || type.contains("corner") || type.contains("card")
                || type.contains("bracket") || type.contains("team")) {
            return false;
        }
        if ("over_under".equals(type)) return true;
        return name.startsWith("over/under ") || name.startsWith("total ");
    }

    private boolean isBtts(String type, String name) {
        if (name.contains("half") || type.contains("half") || name.contains("winner") || type.contains("winner")) {
            return false;
        }
        if ("both_teams_to_score".equals(type)) return true;
        return "both teams to score".equals(name) || "btts".equals(name);
    }

    private void mapWinnerMarket(List<SmarketsContract> contracts,
                                 Map<String, SmarketsContractQuotes> quotesByContractId,
                                 String team1,
                                 String team2,
                                 boolean allowsDraw,
                                 List<OddItem> oddItems) {
        for (SmarketsContract contract : contracts) {
            SmarketsContractQuotes quotes = quotesByContractId.get(contract.getId());
            if (quotes == null) continue;

            Double backOdds = quotes.getBestBackOdds();
            if (backOdds == null || backOdds <= 1.0) continue;

            String slug = contract.getSlug() != null ? contract.getSlug().toLowerCase() : "";
            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;

            if (slug.equals("draw") || cName.contains("draw") || cName.equals("x") || cName.equals("tie")) {
                if (allowsDraw) {
                    betType = map1X2Record("X", BetScope.FULL_MATCH, StatType.MATCH);
                }
            } else if (slug.equals("home") || isMatch(cName, team1) || cName.equals("1") || cName.contains("home")) {
                betType = map1X2Record("1", BetScope.FULL_MATCH, StatType.MATCH);
            } else if (slug.equals("away") || isMatch(cName, team2) || cName.equals("2") || cName.contains("away")) {
                betType = map1X2Record("2", BetScope.FULL_MATCH, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(oddItems, "moneyline", contract.getId(), contract.getName(), backOdds, betType);
            }
        }
    }

    private void mapDoubleChanceMarket(List<SmarketsContract> contracts,
                                       Map<String, SmarketsContractQuotes> quotesByContractId,
                                       List<OddItem> oddItems) {
        for (SmarketsContract contract : contracts) {
            SmarketsContractQuotes quotes = quotesByContractId.get(contract.getId());
            if (quotes == null) continue;

            Double backOdds = quotes.getBestBackOdds();
            if (backOdds == null || backOdds <= 1.0) continue;

            String slug = contract.getSlug() != null ? contract.getSlug().toLowerCase() : "";
            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;

            if (slug.equals("home-draw") || cName.endsWith("or draw") || (cName.contains("home") && cName.contains("draw")) || cName.equals("1x")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null);
            } else if (slug.equals("draw-away") || cName.startsWith("draw or") || (cName.contains("draw") && cName.contains("away")) || cName.equals("x2")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null);
            } else if (slug.equals("home-away") || (cName.contains("or") && !cName.contains("draw")) || (cName.contains("home") && cName.contains("away")) || cName.equals("12")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null);
            }

            if (betType != null) {
                addOddItem(oddItems, "double_chance", contract.getId(), contract.getName(), backOdds, betType);
            }
        }
    }

    private void mapTotalMarket(List<SmarketsContract> contracts,
                                Map<String, SmarketsContractQuotes> quotesByContractId,
                                Double param,
                                List<OddItem> oddItems) {
        if (param == null) return;

        for (SmarketsContract contract : contracts) {
            SmarketsContractQuotes quotes = quotesByContractId.get(contract.getId());
            if (quotes == null) continue;

            Double backOdds = quotes.getBestBackOdds();
            if (backOdds == null || backOdds <= 1.0) continue;

            String slug = contract.getSlug() != null ? contract.getSlug().toLowerCase() : "";
            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;
            if (slug.equals("over") || cName.contains("over") || cName.startsWith("o ")) {
                betType = mapTotalRecord("OVER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, param);
            } else if (slug.equals("under") || cName.contains("under") || cName.startsWith("u ")) {
                betType = mapTotalRecord("UNDER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, param);
            }

            if (betType != null) {
                String label = contract.getName() + " (" + param + ")";
                addOddItem(oddItems, "total", contract.getId(), label, backOdds, betType);
            }
        }
    }

    private void mapBttsMarket(List<SmarketsContract> contracts,
                               Map<String, SmarketsContractQuotes> quotesByContractId,
                               List<OddItem> oddItems) {
        for (SmarketsContract contract : contracts) {
            SmarketsContractQuotes quotes = quotesByContractId.get(contract.getId());
            if (quotes == null) continue;

            Double backOdds = quotes.getBestBackOdds();
            if (backOdds == null || backOdds <= 1.0) continue;

            String slug = contract.getSlug() != null ? contract.getSlug().toLowerCase() : "";
            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;
            if (slug.equals("yes") || cName.equals("yes") || cName.contains("btts yes")) {
                betType = new BinaryMarketBet(
                        BetScope.FULL_MATCH,
                        BetSubject.MATCH,
                        BinaryMarketBet.MarketType.BTTS,
                        BinaryMarketBet.Outcome.YES,
                        StatType.MATCH
                );
            } else if (slug.equals("no") || cName.equals("no") || cName.contains("btts no")) {
                betType = new BinaryMarketBet(
                        BetScope.FULL_MATCH,
                        BetSubject.MATCH,
                        BinaryMarketBet.MarketType.BTTS,
                        BinaryMarketBet.Outcome.NO,
                        StatType.MATCH
                );
            }

            if (betType != null) {
                addOddItem(oddItems, "btts", contract.getId(), contract.getName(), backOdds, betType);
            }
        }
    }

    private void addOddItem(List<OddItem> items, String groupName, String outcomeId, String rawName, double value, BetType betType) {
        // Golden Rule #9: NO YIELD CAP! No silent drop of odds!
        if (value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }

        // Deduplicate: avoid duplicate betType in a single match update
        for (OddItem existing : items) {
            if (existing.getBetType() != null && existing.getBetType().code().equals(betType.code())) {
                log.debug("Duplicate betType {} ignored for outcomeId {}", betType.code(), outcomeId);
                return;
            }
        }

        OddItem item = new OddItem();
        item.setFactorId(outcomeId != null ? outcomeId : (groupName + "_" + rawName.replaceAll("[^a-zA-Z0-9_+.-]", "_")));
        item.setGroupName(groupName);
        item.setName(rawName);
        item.setValue(Math.round(value * 1000.0) / 1000.0);
        item.setBetType(betType);
        items.add(item);
    }

    private boolean isMatch(String name, String team) {
        if (name == null || team == null) return false;
        String n = name.trim().toLowerCase();
        String t = team.trim().toLowerCase();
        return n.contains(t) || t.contains(n);
    }

    private Double extractParam(String paramStr, String text) {
        if (paramStr != null) {
            try {
                return Double.parseDouble(paramStr);
            } catch (NumberFormatException ignored) {}
        }
        return LineParamParser.parseTotalParam(text);
    }

    public SportType resolveSportType(String type) {
        if (type == null) return SportType.FOOTBALL;
        String t = type.toLowerCase();
        if (t.contains("american")) return SportType.AMERICAN_FOOTBALL;
        if (t.contains("table_tennis")) return SportType.TABLE_TENNIS;
        if (t.contains("tennis")) return SportType.TENNIS;
        if (t.contains("basketball")) return SportType.BASKETBALL;
        if (t.contains("baseball")) return SportType.BASEBALL;
        if (t.contains("hockey")) return SportType.HOCKEY;
        if (t.contains("cricket")) return SportType.CRICKET;
        if (t.contains("mma") || t.contains("ufc")) return SportType.MMA;
        if (t.contains("boxing")) return SportType.BOXING;
        if (t.contains("darts")) return SportType.DARTS;
        if (t.contains("snooker")) return SportType.SNOOKER;
        if (t.contains("rugby_league")) return SportType.RUGBY_LEAGUE;
        if (t.contains("rugby")) return SportType.RUGBY_UNION;
        if (t.contains("handball")) return SportType.HANDBALL;
        if (t.contains("volleyball")) return SportType.VOLLEYBALL;
        if (t.contains("csgo") || t.contains("cs2") || t.contains("dota") || t.contains("league_of_legends") || t.contains("call_of_duty") || t.contains("esports")) return SportType.ESPORTS;
        if (t.contains("golf")) return SportType.GOLF;
        if (t.contains("football")) return SportType.FOOTBALL;
        return SportType.FOOTBALL;
    }
}
