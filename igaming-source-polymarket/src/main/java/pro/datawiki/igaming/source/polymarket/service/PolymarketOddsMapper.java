package pro.datawiki.igaming.source.polymarket.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.polymarket.dto.PolymarketEventDto;
import pro.datawiki.igaming.source.polymarket.dto.PolymarketMarketDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class PolymarketOddsMapper extends AbstractBetTypeMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;
    private final ObjectMapper objectMapper;

    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d*\\.?\\d+");

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "polymarket".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cache, PolymarketEventDto event) {
        if (cache == null || event == null || event.getMarkets() == null || event.getMarkets().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("polymarket");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.US));
        request.setExternalEventId(event.getId() != null ? event.getId() : cache.getExternalId());

        String sportName = cache.getSportName() != null ? cache.getSportName() : "General";
        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);
        request.setSportName(sportName);

        request.setLeagueName(cache.getLeagueName() != null ? cache.getLeagueName() : "Prediction Market");
        request.setTeam1(cache.getTeam1());
        request.setTeam2(cache.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cache.getIsLive()));
        request.setStartTime(cache.getStartTime());

        List<OddItem> items = new ArrayList<>();

        for (PolymarketMarketDto market : event.getMarkets()) {
            if (market == null || Boolean.TRUE.equals(market.getClosed())) {
                continue;
            }
            mapMarket(items, market, cache.getTeam1(), cache.getTeam2());
        }

        request.setOdds(items);
        return request;
    }

    private void mapMarket(List<OddItem> items, PolymarketMarketDto market, String team1, String team2) {
        List<String> outcomes = parseStringList(market.getOutcomes());
        List<Double> prices = parseDoubleList(market.getOutcomePrices());

        if (outcomes.isEmpty() || prices.isEmpty() || outcomes.size() != prices.size()) {
            return;
        }

        String question = market.getQuestion() != null ? market.getQuestion().toLowerCase() : "";

        // Check 3-way match odds: Team1, Draw, Team2
        if (outcomes.size() == 3 && outcomes.stream().anyMatch(o -> "draw".equalsIgnoreCase(o) || "tie".equalsIgnoreCase(o))) {
            for (int i = 0; i < outcomes.size(); i++) {
                String name = outcomes.get(i).trim();
                Double prob = prices.get(i);
                Double decimalOdd = calculateDecimalOdds(prob);
                if (decimalOdd == null) continue;

                BetType betType = null;
                if (name.equalsIgnoreCase(team1) || "1".equals(name) || name.toLowerCase().contains("home")) {
                    betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, null);
                } else if ("draw".equalsIgnoreCase(name) || "tie".equalsIgnoreCase(name) || "x".equalsIgnoreCase(name)) {
                    betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
                } else if (name.equalsIgnoreCase(team2) || "2".equals(name) || name.toLowerCase().contains("away")) {
                    betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, null);
                }

                if (betType != null) {
                    items.add(createOddItem(market.getId() + "_" + i, market.getQuestion(), name, decimalOdd, betType));
                }
            }
            return;
        }

        // Check 2-way match odds: Team1 vs Team2
        if (outcomes.size() == 2 && !outcomes.get(0).equalsIgnoreCase("yes") && !outcomes.get(0).equalsIgnoreCase("no")) {
            for (int i = 0; i < outcomes.size(); i++) {
                String name = outcomes.get(i).trim();
                Double prob = prices.get(i);
                Double decimalOdd = calculateDecimalOdds(prob);
                if (decimalOdd == null) continue;

                BetType betType = null;
                if (name.equalsIgnoreCase(team1) || (team1 != null && name.toLowerCase().contains(team1.toLowerCase()))) {
                    betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1_2WAY, null);
                } else if (name.equalsIgnoreCase(team2) || (team2 != null && name.toLowerCase().contains(team2.toLowerCase()))) {
                    betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2_2WAY, null);
                }

                if (betType != null) {
                    items.add(createOddItem(market.getId() + "_" + i, market.getQuestion(), name, decimalOdd, betType));
                }
            }
            return;
        }

        // Check binary YES / NO markets
        if (outcomes.size() == 2 && (outcomes.get(0).equalsIgnoreCase("yes") || outcomes.get(1).equalsIgnoreCase("yes"))) {
            int yesIdx = outcomes.get(0).equalsIgnoreCase("yes") ? 0 : 1;
            int noIdx = yesIdx == 0 ? 1 : 0;

            Double yesProb = prices.get(yesIdx);
            Double noProb = prices.get(noIdx);

            Double yesOdd = calculateDecimalOdds(yesProb);
            Double noOdd = calculateDecimalOdds(noProb);

            if (question.contains("both teams to score") || question.contains("btts")) {
                if (yesOdd != null) {
                    items.add(createOddItem(market.getId() + "_yes", market.getQuestion(), "Yes", yesOdd,
                            new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null)));
                }
                if (noOdd != null) {
                    items.add(createOddItem(market.getId() + "_no", market.getQuestion(), "No", noOdd,
                            new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null)));
                }
            } else if (question.contains("over") || question.contains("under") || question.contains("total")) {
                Double param = extractParam(question);
                if (param == null) param = 2.5;

                if (question.contains("over")) {
                    if (yesOdd != null) {
                        items.add(createOddItem(market.getId() + "_over", market.getQuestion(), "Over " + param, yesOdd,
                                new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, null)));
                    }
                    if (noOdd != null) {
                        items.add(createOddItem(market.getId() + "_under", market.getQuestion(), "Under " + param, noOdd,
                                new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, null)));
                    }
                }
            } else if (team1 != null && (question.contains("will " + team1.toLowerCase() + " win") || question.contains(team1.toLowerCase()))) {
                if (yesOdd != null) {
                    items.add(createOddItem(market.getId() + "_yes", market.getQuestion(), team1, yesOdd,
                            new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1_2WAY, null)));
                }
                if (noOdd != null) {
                    items.add(createOddItem(market.getId() + "_no", market.getQuestion(), team2 != null ? team2 : "Not " + team1, noOdd,
                            new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2_2WAY, null)));
                }
            }
        }
    }

    public Double calculateDecimalOdds(Double impliedProbability) {
        if (impliedProbability == null || impliedProbability <= 0.001 || impliedProbability >= 0.999) {
            return null;
        }
        try {
            double odd = 1.0 / impliedProbability;
            return BigDecimal.valueOf(odd).setScale(3, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseStringList(Object obj) {
        if (obj == null) return Collections.emptyList();
        if (obj instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        if (obj instanceof String str) {
            try {
                return objectMapper.readValue(str, new TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }
        return Collections.emptyList();
    }

    private List<Double> parseDoubleList(Object obj) {
        if (obj == null) return Collections.emptyList();
        if (obj instanceof List<?> list) {
            List<Double> doubles = new ArrayList<>();
            for (Object o : list) {
                try {
                    doubles.add(Double.parseDouble(o.toString()));
                } catch (Exception ignored) {}
            }
            return doubles;
        }
        if (obj instanceof String str) {
            try {
                List<String> strList = objectMapper.readValue(str, new TypeReference<List<String>>() {});
                return strList.stream().map(s -> {
                    try {
                        return Double.parseDouble(s);
                    } catch (Exception e) {
                        return 0.0;
                    }
                }).toList();
            } catch (Exception ignored) {}
        }
        return Collections.emptyList();
    }

    private Double extractParam(String str) {
        try {
            Matcher m = NUMBER_PATTERN.matcher(str);
            if (m.find()) {
                return Double.parseDouble(m.group());
            }
        } catch (Exception ignored) {}
        return null;
    }

    private OddItem createOddItem(String id, String groupName, String name, Double price, BetType betType) {
        OddItem item = new OddItem();
        item.setFactorId(id);
        item.setGroupName(groupName);
        item.setName(name);
        item.setValue(price);
        item.setBetType(betType);
        return item;
    }
}
