package pro.datawiki.igaming.source.betvictor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betvictor.dto.BetvictorEvent;
import pro.datawiki.igaming.source.betvictor.dto.BetvictorMarket;
import pro.datawiki.igaming.source.betvictor.dto.BetvictorOutcome;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pro.datawiki.igaming.source.core.mapper.rule.LineParamParser;

@Component
@Slf4j
@RequiredArgsConstructor
public class BetvictorOddsMapper {

    private final SportNormalizationService sportNormalizationService;

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, BetvictorEvent event) {
        if (cached == null || event == null) {
            return null;
        }

        List<BetvictorMarket> markets = event.getMarkets();
        if (markets == null || markets.isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("betvictor");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));
        request.setExternalEventId(String.valueOf(event.getResolvedId()));
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : (event.getSportName() != null ? event.getSportName() : "Soccer"));

        SportType sportType = sportNormalizationService != null ? sportNormalizationService.normalize(request.getSportName()) : SportType.FOOTBALL;
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : event.getResolvedLeague());
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(event.getResolvedIsLive() != null ? event.getResolvedIsLive() : Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://www.betvictor.com/en-gb/sports/events/" + event.getResolvedId());

        List<OddItem> oddItems = new ArrayList<>();

        for (BetvictorMarket market : markets) {
            if (market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
                continue;
            }

            String marketName = market.getResolvedName().toLowerCase();
            String marketType = market.getResolvedType().toUpperCase();

            if (is1X2Market(marketName, marketType)) {
                map1X2(market, oddItems, cached);
            } else if (isTotalMarket(marketName, marketType)) {
                mapTotals(market, oddItems);
            } else if (isHandicapMarket(marketName, marketType)) {
                mapHandicap(market, oddItems, cached);
            } else if (isBttsMarket(marketName, marketType)) {
                mapBtts(market, oddItems);
            } else if (isDoubleChanceMarket(marketName, marketType)) {
                mapDoubleChance(market, oddItems);
            }
        }

        if (oddItems.isEmpty()) {
            return null;
        }

        request.setOdds(oddItems);
        return request;
    }

    private boolean is1X2Market(String name, String type) {
        return type.contains("1X2") || type.contains("MATCH_BETTING") || type.contains("WINNER")
                || name.contains("match betting") || name.contains("match result")
                || name.contains("full time result") || name.contains("1x2") || name.equals("winner");
    }

    private void map1X2(BetvictorMarket market, List<OddItem> oddItems, MatchCache cached) {
        boolean hasDraw = market.getOutcomes().stream().anyMatch(o -> {
            String n = o.getResolvedName().toLowerCase();
            return n.equals("draw") || n.equals("tie") || n.equals("x");
        });

        for (BetvictorOutcome outcome : market.getOutcomes()) {
            Double price = outcome.getResolvedPrice();
            if (price == null || price <= 1.0) continue;

            String name = outcome.getResolvedName().trim();
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.equals("1") || name.equalsIgnoreCase("1") || (cached.getTeam1() != null && name.equalsIgnoreCase(cached.getTeam1())) || name.equalsIgnoreCase("Home")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null)));
            } else if (code.equals("X") || name.equalsIgnoreCase("X") || name.equalsIgnoreCase("Draw") || name.equalsIgnoreCase("Tie")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null)));
            } else if (code.equals("2") || name.equalsIgnoreCase("2") || (cached.getTeam2() != null && name.equalsIgnoreCase(cached.getTeam2())) || name.equalsIgnoreCase("Away")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null)));
            }
        }
    }

    private boolean isTotalMarket(String name, String type) {
        return type.contains("TOTAL") || type.contains("OVER_UNDER") || name.contains("total goals")
                || name.contains("over/under") || name.contains("total points") || name.contains("match goals");
    }

    private void mapTotals(BetvictorMarket market, List<OddItem> oddItems) {
        Double marketTotal = parseTotalValue(market.getResolvedName());

        for (BetvictorOutcome outcome : market.getOutcomes()) {
            Double price = outcome.getResolvedPrice();
            if (price == null || price <= 1.0) continue;

            Double total = outcome.getHandicap();
            if (total == null) {
                total = parseTotalValue(outcome.getResolvedName());
            }
            if (total == null) {
                total = marketTotal;
            }
            if (total == null) continue;

            String name = outcome.getResolvedName().toLowerCase();
            if (name.contains("over") || name.startsWith("o ") || name.equals("o")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, total, false, null)));
            } else if (name.contains("under") || name.startsWith("u ") || name.equals("u")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, total, false, null)));
            }
        }
    }

    private boolean isHandicapMarket(String name, String type) {
        return type.contains("HANDICAP") || type.contains("SPREAD")
                || name.contains("handicap") || name.contains("spread");
    }

    private void mapHandicap(BetvictorMarket market, List<OddItem> oddItems, MatchCache cached) {
        for (BetvictorOutcome outcome : market.getOutcomes()) {
            Double price = outcome.getResolvedPrice();
            if (price == null || price <= 1.0) continue;

            Double hcap = outcome.getHandicap();
            if (hcap == null) {
                hcap = LineParamParser.parseHandicapParam(outcome.getResolvedName(), market.getResolvedName());
            }
            if (hcap == null) continue;

            String name = outcome.getResolvedName().trim();
            if (name.equalsIgnoreCase("1") || (cached.getTeam1() != null && name.toLowerCase().contains(cached.getTeam1().toLowerCase())) || name.toLowerCase().startsWith("home")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, hcap, false, null)));
            } else if (name.equalsIgnoreCase("2") || (cached.getTeam2() != null && name.toLowerCase().contains(cached.getTeam2().toLowerCase())) || name.toLowerCase().startsWith("away")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, hcap, false, null)));
            }
        }
    }

    private boolean isBttsMarket(String name, String type) {
        return type.contains("BTTS") || name.contains("both teams to score") || name.contains("btts");
    }

    private void mapBtts(BetvictorMarket market, List<OddItem> oddItems) {
        for (BetvictorOutcome outcome : market.getOutcomes()) {
            Double price = outcome.getResolvedPrice();
            if (price == null || price <= 1.0) continue;

            String name = outcome.getResolvedName().toLowerCase().trim();
            if (name.equals("yes") || name.equals("y") || name.contains("da")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null)));
            } else if (name.equals("no") || name.equals("n") || name.contains("nu")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null)));
            }
        }
    }

    private boolean isDoubleChanceMarket(String name, String type) {
        return type.contains("DOUBLE_CHANCE") || name.contains("double chance");
    }

    private void mapDoubleChance(BetvictorMarket market, List<OddItem> oddItems) {
        for (BetvictorOutcome outcome : market.getOutcomes()) {
            Double price = outcome.getResolvedPrice();
            if (price == null || price <= 1.0) continue;

            String name = outcome.getResolvedName().trim().toUpperCase();
            if (name.equals("1X") || name.contains("HOME OR DRAW") || name.contains("1 OR X")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null)));
            } else if (name.equals("12") || name.contains("HOME OR AWAY") || name.contains("1 OR 2")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null)));
            } else if (name.equals("X2") || name.contains("DRAW OR AWAY") || name.contains("X OR 2")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null)));
            }
        }
    }

    private OddItem createOddItem(BetvictorOutcome outcome, String groupName, pro.datawiki.igaming.dto.BetType betType) {
        OddItem item = new OddItem();
        item.setFactorId(outcome.getId() != null ? String.valueOf(outcome.getId()) : null);
        item.setGroupName(groupName);
        item.setName(outcome.getResolvedName());
        item.setValue(outcome.getResolvedPrice());
        item.setBetType(betType);
        return item;
    }

    private Double parseTotalValue(String text) {
        return LineParamParser.parseTotalParam(text);
    }
}
