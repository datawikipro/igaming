package pro.datawiki.igaming.source.mozzartbet.service;

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
import pro.datawiki.igaming.source.mozzartbet.dto.MozzartEvent;
import pro.datawiki.igaming.source.mozzartbet.dto.MozzartMarket;
import pro.datawiki.igaming.source.mozzartbet.dto.MozzartOutcome;

import java.util.ArrayList;
import java.util.List;

import pro.datawiki.igaming.source.core.mapper.rule.LineParamParser;

@Component
@Slf4j
@RequiredArgsConstructor
public class MozzartOddsMapper {

    private final SportNormalizationService sportNormalizationService;

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, MozzartEvent event) {
        if (cached == null || event == null) {
            return null;
        }

        List<MozzartMarket> markets = event.getResolvedMarkets();
        if (markets == null || markets.isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("mozzartbet");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));
        request.setExternalEventId(String.valueOf(event.getResolvedId()));
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : (event.getSportName() != null ? event.getSportName() : "Football"));

        SportType sportType = sportNormalizationService != null ? sportNormalizationService.normalize(request.getSportName()) : SportType.FOOTBALL;
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : (event.getCompetitionName() != null ? event.getCompetitionName() : "General"));
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(event.getLive() != null ? event.getLive() : Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://www.mozzartbet.com/sr#/match/" + event.getResolvedId());

        List<OddItem> oddItems = new ArrayList<>();

        for (MozzartMarket market : markets) {
            List<MozzartOutcome> outcomes = market.getResolvedOutcomes();
            if (outcomes == null || outcomes.isEmpty()) {
                continue;
            }

            String marketName = market.getName() != null ? market.getName().toLowerCase() : "";
            String marketCode = market.getCode() != null ? market.getCode().toUpperCase() : "";

            if (is1X2Market(marketName, marketCode)) {
                map1X2(market, outcomes, oddItems, cached);
            } else if (isTotalMarket(marketName, marketCode)) {
                mapTotals(market, outcomes, oddItems);
            } else if (isHandicapMarket(marketName, marketCode)) {
                mapHandicap(market, outcomes, oddItems, cached);
            } else if (isBttsMarket(marketName, marketCode)) {
                mapBtts(market, outcomes, oddItems);
            } else if (isDoubleChanceMarket(marketName, marketCode)) {
                mapDoubleChance(market, outcomes, oddItems);
            }
        }

        if (oddItems.isEmpty()) {
            return null;
        }

        request.setOdds(oddItems);
        return request;
    }

    private boolean is1X2Market(String name, String code) {
        return code.equals("1X2") || code.equals("KI") || code.equals("MATCH_RESULT") || code.equals("WINNER")
                || name.contains("konačan ishod") || name.contains("konacan ishod") || name.contains("ishod")
                || name.contains("pobednik") || name.equals("1x2") || name.equals("winner");
    }

    private void map1X2(MozzartMarket market, List<MozzartOutcome> outcomes, List<OddItem> oddItems, MatchCache cached) {
        boolean hasDraw = outcomes.stream().anyMatch(o ->
                "X".equalsIgnoreCase(o.getCode()) || "0".equalsIgnoreCase(o.getCode())
                        || "X".equalsIgnoreCase(o.getName()) || (o.getName() != null && o.getName().toLowerCase().contains("nerešeno")));

        for (MozzartOutcome outcome : outcomes) {
            Double price = outcome.getResolvedOdds();
            if (price == null || price <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim() : "";
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.equals("1") || name.equalsIgnoreCase("1") || (cached.getTeam1() != null && name.equalsIgnoreCase(cached.getTeam1()))) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null)));
            } else if (code.equals("X") || code.equals("0") || name.equalsIgnoreCase("X") || name.equalsIgnoreCase("Nerešeno") || name.equalsIgnoreCase("Nereseno")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null)));
            } else if (code.equals("2") || name.equalsIgnoreCase("2") || (cached.getTeam2() != null && name.equalsIgnoreCase(cached.getTeam2()))) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null)));
            }
        }
    }

    private boolean isTotalMarket(String name, String code) {
        return code.contains("TOTAL") || code.contains("UG") || code.contains("OU") || name.contains("ukupno golova")
                || name.contains("ukupno poena") || name.contains("over/under") || name.contains("više/manje") || name.contains("total");
    }

    private void mapTotals(MozzartMarket market, List<MozzartOutcome> outcomes, List<OddItem> oddItems) {
        Double line = market.getParam();
        if (line == null) {
            line = LineParamParser.parseTotalParam(market.getName());
        }

        for (MozzartOutcome outcome : outcomes) {
            Double price = outcome.getResolvedOdds();
            if (price == null || price <= 1.0) continue;
            Double outcomeLine = outcome.getParam() != null ? outcome.getParam() : line;
            if (outcomeLine == null) {
                outcomeLine = LineParamParser.parseTotalParam(outcome.getName());
            }
            if (outcomeLine == null) continue;

            String name = outcome.getName() != null ? outcome.getName().trim().toLowerCase() : "";
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.contains("OVER") || code.contains("VISE") || name.contains("više") || name.contains("vise") || name.contains("over") || name.startsWith(">") || name.startsWith("+")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, outcomeLine, false, null)));
            } else if (code.contains("UNDER") || code.contains("MANJE") || name.contains("manje") || name.contains("under") || name.startsWith("<") || name.startsWith("-")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, outcomeLine, false, null)));
            }
        }
    }

    private boolean isHandicapMarket(String name, String code) {
        return code.contains("AH") || code.contains("HANDICAP") || code.contains("HEN") || name.contains("hendikep") || name.contains("handicap");
    }

    private void mapHandicap(MozzartMarket market, List<MozzartOutcome> outcomes, List<OddItem> oddItems, MatchCache cached) {
        Double line = market.getParam();
        if (line == null) {
            line = LineParamParser.parseHandicapParam(market.getName());
        }

        for (MozzartOutcome outcome : outcomes) {
            Double price = outcome.getResolvedOdds();
            if (price == null || price <= 1.0) continue;
            Double outcomeLine = outcome.getParam() != null ? outcome.getParam() : line;
            if (outcomeLine == null) {
                outcomeLine = LineParamParser.parseHandicapParam(outcome.getName());
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

    private boolean isBttsMarket(String name, String code) {
        return code.equals("GG") || code.equals("BTTS") || name.contains("oba tima daju gol")
                || name.contains("both teams to score") || name.contains("gg/ng");
    }

    private void mapBtts(MozzartMarket market, List<MozzartOutcome> outcomes, List<OddItem> oddItems) {
        for (MozzartOutcome outcome : outcomes) {
            Double price = outcome.getResolvedOdds();
            if (price == null || price <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toLowerCase() : "";
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.equals("GG") || code.equals("YES") || name.equals("da") || name.equals("yes") || name.equals("gg")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null)));
            } else if (code.equals("NG") || code.equals("NO") || name.equals("ne") || name.equals("no") || name.equals("ng")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null)));
            }
        }
    }

    private boolean isDoubleChanceMarket(String name, String code) {
        return code.equals("DS") || code.equals("DC") || name.contains("dupla šansa") || name.contains("dupla sansa") || name.contains("double chance");
    }

    private void mapDoubleChance(MozzartMarket market, List<MozzartOutcome> outcomes, List<OddItem> oddItems) {
        for (MozzartOutcome outcome : outcomes) {
            Double price = outcome.getResolvedOdds();
            if (price == null || price <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toUpperCase() : "";
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.equals("1X") || name.contains("1X") || name.equals("1 ILI X")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null)));
            } else if (code.equals("12") || name.contains("12") || name.equals("1 ILI 2")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null)));
            } else if (code.equals("X2") || name.contains("X2") || name.equals("X ILI 2")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null)));
            }
        }
    }

    private Double extractParameter(String text) {
        return LineParamParser.parseGeneralParam(text);
    }

    private OddItem createOddItem(MozzartOutcome outcome, String groupName, pro.datawiki.igaming.dto.BetType betType) {
        OddItem item = new OddItem();
        item.setFactorId(outcome.getId() != null ? String.valueOf(outcome.getId()) : null);
        item.setGroupName(groupName);
        item.setName(outcome.getName());
        item.setValue(outcome.getResolvedOdds());
        item.setBetType(betType);
        return item;
    }
}
