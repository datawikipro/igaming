package pro.datawiki.igaming.source.fortuna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.fortuna.dto.FortunaEvent;
import pro.datawiki.igaming.source.fortuna.dto.FortunaMarket;
import pro.datawiki.igaming.source.fortuna.dto.FortunaOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class FortunaOddsMapper {

    private final SportNormalizationService sportNormalizationService;
    private static final Pattern PARAM_PATTERN = Pattern.compile("[-+]?\\d*\\.?\\d+");

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, FortunaEvent event) {
        if (cached == null || event == null) {
            return null;
        }

        List<FortunaMarket> markets = event.getMarkets();
        if (markets == null || markets.isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("fortuna");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));
        request.setExternalEventId(String.valueOf(event.getResolvedId()));
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : (event.getSportName() != null ? event.getSportName() : "Football"));

        SportType sportType = sportNormalizationService != null ? sportNormalizationService.normalize(request.getSportName()) : SportType.FOOTBALL;
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : (event.getTournamentName() != null ? event.getTournamentName() : "General"));
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(event.getLive() != null ? event.getLive() : Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://www.ifortuna.cz/sazeni/detail/" + event.getResolvedId());

        List<OddItem> oddItems = new ArrayList<>();

        for (FortunaMarket market : markets) {
            if (market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
                continue;
            }

            String marketName = market.getName() != null ? market.getName().toLowerCase() : "";
            String marketType = market.getMarketType() != null ? market.getMarketType().toUpperCase() : "";

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
        return type.equals("1X2") || type.equals("MATCH_RESULT") || type.equals("WINNER")
                || name.contains("zápas") || name.contains("vítěz") || name.contains("výsledek")
                || name.contains("kdo vyhraje") || name.equals("1x2") || name.equals("winner");
    }

    private void map1X2(FortunaMarket market, List<OddItem> oddItems, MatchCache cached) {
        boolean hasDraw = market.getOutcomes().stream().anyMatch(o ->
                "0".equalsIgnoreCase(o.getCode()) || "X".equalsIgnoreCase(o.getCode())
                        || "0".equalsIgnoreCase(o.getName()) || "X".equalsIgnoreCase(o.getName())
                        || (o.getName() != null && o.getName().toLowerCase().contains("remíza")));

        for (FortunaOutcome outcome : market.getOutcomes()) {
            Double price = outcome.getResolvedOdds();
            if (price == null || price <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim() : "";
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.equals("1") || name.equalsIgnoreCase("1") || (cached.getTeam1() != null && name.equalsIgnoreCase(cached.getTeam1()))) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null)));
            } else if (code.equals("0") || code.equals("X") || name.equalsIgnoreCase("X") || name.equalsIgnoreCase("Remíza")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null)));
            } else if (code.equals("2") || name.equalsIgnoreCase("2") || (cached.getTeam2() != null && name.equalsIgnoreCase(cached.getTeam2()))) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null)));
            }
        }
    }

    private boolean isTotalMarket(String name, String type) {
        return type.contains("TOTAL") || type.contains("OU") || name.contains("počet gólů")
                || name.contains("počet bodů") || name.contains("méně/více") || name.contains("over/under") || name.contains("total");
    }

    private void mapTotals(FortunaMarket market, List<OddItem> oddItems) {
        Double line = market.getParam();
        if (line == null) {
            line = extractParameter(market.getName());
        }

        for (FortunaOutcome outcome : market.getOutcomes()) {
            Double price = outcome.getResolvedOdds();
            if (price == null || price <= 1.0) continue;
            Double outcomeLine = outcome.getParam() != null ? outcome.getParam() : line;
            if (outcomeLine == null) {
                outcomeLine = extractParameter(outcome.getName());
            }
            if (outcomeLine == null) continue;

            String name = outcome.getName() != null ? outcome.getName().trim().toLowerCase() : "";
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.contains("OVER") || name.contains("více") || name.contains("over") || name.startsWith(">") || name.startsWith("+")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, outcomeLine, false, null)));
            } else if (code.contains("UNDER") || name.contains("méně") || name.contains("under") || name.startsWith("<") || name.startsWith("-")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, outcomeLine, false, null)));
            }
        }
    }

    private boolean isHandicapMarket(String name, String type) {
        return type.contains("AH") || type.contains("HANDICAP") || name.contains("handicap");
    }

    private void mapHandicap(FortunaMarket market, List<OddItem> oddItems, MatchCache cached) {
        Double line = market.getParam();
        if (line == null) {
            line = extractParameter(market.getName());
        }

        for (FortunaOutcome outcome : market.getOutcomes()) {
            Double price = outcome.getResolvedOdds();
            if (price == null || price <= 1.0) continue;
            Double outcomeLine = outcome.getParam() != null ? outcome.getParam() : line;
            if (outcomeLine == null) {
                outcomeLine = extractParameter(outcome.getName());
            }
            if (outcomeLine == null) outcomeLine = 0.0;

            String name = outcome.getName() != null ? outcome.getName().trim() : "";
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.equals("H1") || code.equals("1") || (cached.getTeam1() != null && name.equalsIgnoreCase(cached.getTeam1()))) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, outcomeLine, false, null)));
            } else if (code.equals("H2") || code.equals("2") || (cached.getTeam2() != null && name.equalsIgnoreCase(cached.getTeam2()))) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, outcomeLine, false, null)));
            }
        }
    }

    private boolean isBttsMarket(String name, String type) {
        return type.equals("BTTS") || name.contains("oba týmy dají gól")
                || name.contains("both teams to score") || name.contains("btts");
    }

    private void mapBtts(FortunaMarket market, List<OddItem> oddItems) {
        for (FortunaOutcome outcome : market.getOutcomes()) {
            Double price = outcome.getResolvedOdds();
            if (price == null || price <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toLowerCase() : "";
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.equals("YES") || name.equals("ano") || name.equals("yes")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null)));
            } else if (code.equals("NO") || name.equals("ne") || name.equals("no")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null)));
            }
        }
    }

    private boolean isDoubleChanceMarket(String name, String type) {
        return type.equals("DC") || name.contains("dvojitá šance") || name.contains("neprohra") || name.contains("double chance");
    }

    private void mapDoubleChance(FortunaMarket market, List<OddItem> oddItems) {
        for (FortunaOutcome outcome : market.getOutcomes()) {
            Double price = outcome.getResolvedOdds();
            if (price == null || price <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toUpperCase() : "";
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.equals("10") || code.equals("1X") || name.contains("1X") || name.equals("1 NEBO 0") || name.equals("1X")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null)));
            } else if (code.equals("12") || name.contains("12") || name.equals("1 NEBO 2") || name.equals("12")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null)));
            } else if (code.equals("02") || code.equals("X2") || name.contains("X2") || name.equals("0 NEBO 2") || name.equals("X2")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null)));
            }
        }
    }

    private Double extractParameter(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher matcher = PARAM_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private OddItem createOddItem(FortunaOutcome outcome, String groupName, pro.datawiki.igaming.dto.BetType betType) {
        OddItem item = new OddItem();
        item.setFactorId(outcome.getId() != null ? String.valueOf(outcome.getId()) : null);
        item.setGroupName(groupName);
        item.setName(outcome.getName());
        item.setValue(outcome.getResolvedOdds());
        item.setBetType(betType);
        return item;
    }
}
