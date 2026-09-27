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
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.smarkets.dto.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class SmarketsOddsMapper extends AbstractBetTypeMapper {

    private static final Pattern PARAM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");

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

        // Parse teams from "Team1 vs Team2" or "Team1 - Team2"
        String team1 = "Home Team";
        String team2 = "Away Team";
        if (event.getName() != null) {
            String name = event.getName();
            if (name.contains(" vs ")) {
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

        for (SmarketsMarket market : markets) {
            List<SmarketsContract> contracts = contractsByMarketId.get(market.getId());
            if (contracts == null || contracts.isEmpty()) {
                continue;
            }

            String marketName = market.getName() != null ? market.getName().toLowerCase() : "";
            String marketTypeName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                    market.getMarketType().getName().toLowerCase() : "";

            // 1. Full-time result / Winner (1X2 or 12)
            if (marketTypeName.contains("winner_3_way") || marketName.contains("full-time result") || marketName.equals("winner")) {
                mapWinnerMarket(contracts, quotesByContractId, team1, team2, true, oddItems);
            } else if (marketTypeName.contains("winner_2_way") || marketName.contains("match winner") || marketName.contains("moneyline")) {
                mapWinnerMarket(contracts, quotesByContractId, team1, team2, false, oddItems);
            }
            // 2. Over/Under Total
            else if (marketTypeName.contains("over_under") || marketName.contains("over/under") || marketName.contains("total")) {
                Double totalParam = extractParam(market.getMarketType() != null ? market.getMarketType().getParam() : null, market.getName());
                mapTotalMarket(contracts, quotesByContractId, totalParam, oddItems);
            }
            // 3. Both Teams to Score
            else if (marketName.contains("both teams to score") || marketTypeName.contains("btts")) {
                mapBttsMarket(contracts, quotesByContractId, oddItems);
            }
        }

        request.setOdds(oddItems);
        return request;
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

            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;

            if (cName.contains("draw") || cName.equals("x") || cName.equals("tie")) {
                if (allowsDraw) {
                    betType = map1X2Record("X", BetScope.FULL_MATCH, StatType.MATCH);
                }
            } else if (isMatch(cName, team1) || cName.equals("1") || cName.contains("home")) {
                betType = map1X2Record("1", BetScope.FULL_MATCH, StatType.MATCH);
            } else if (isMatch(cName, team2) || cName.equals("2") || cName.contains("away")) {
                betType = map1X2Record("2", BetScope.FULL_MATCH, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(oddItems, "moneyline", contract.getId(), contract.getName(), backOdds, betType);
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

            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;
            if (cName.contains("over") || cName.startsWith("o ")) {
                betType = mapTotalRecord("OVER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, param);
            } else if (cName.contains("under") || cName.startsWith("u ")) {
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

            String cName = contract.getName() != null ? contract.getName().toLowerCase() : "";
            BetType betType = null;
            if (cName.equals("yes") || cName.contains("btts yes")) {
                betType = new pro.datawiki.igaming.dto.market.BinaryMarketBet(
                        BetScope.FULL_MATCH,
                        BetSubject.MATCH,
                        pro.datawiki.igaming.dto.market.BinaryMarketBet.MarketType.BTTS,
                        pro.datawiki.igaming.dto.market.BinaryMarketBet.Outcome.YES,
                        StatType.MATCH
                );
            } else if (cName.equals("no") || cName.contains("btts no")) {
                betType = new pro.datawiki.igaming.dto.market.BinaryMarketBet(
                        BetScope.FULL_MATCH,
                        BetSubject.MATCH,
                        pro.datawiki.igaming.dto.market.BinaryMarketBet.MarketType.BTTS,
                        pro.datawiki.igaming.dto.market.BinaryMarketBet.Outcome.NO,
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
        if (text != null) {
            Matcher m = PARAM_PATTERN.matcher(text);
            if (m.find()) {
                try {
                    return Double.parseDouble(m.group(1));
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
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
        if (t.contains("football")) return SportType.FOOTBALL;
        return SportType.FOOTBALL;
    }
}
