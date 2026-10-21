package pro.datawiki.igaming.source.superbet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.mapper.rule.LineParamParser;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.superbet.dto.SuperbetEvent;
import pro.datawiki.igaming.source.superbet.dto.SuperbetMarket;
import pro.datawiki.igaming.source.superbet.dto.SuperbetOutcome;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SuperbetOddsMapper {

    private final SportNormalizationService sportNormalizationService;

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, SuperbetEvent event) {
        if (cached == null || event == null) {
            return null;
        }

        List<SuperbetMarket> markets = event.getMarkets();
        if (markets == null || markets.isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("superbet");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));
        request.setExternalEventId(String.valueOf(event.getResolvedId()));
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : event.getResolvedSportName());

        SportType sportType = sportNormalizationService != null ? sportNormalizationService.normalize(request.getSportName()) : SportType.FOOTBALL;
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : (event.getTournamentName() != null ? event.getTournamentName() : "General"));
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(event.isMatchLive() || Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://superbet.pl/zaklady-bukmacherskie/eveniment/" + event.getResolvedId());

        List<OddItem> oddItems = new ArrayList<>();

        for (SuperbetMarket market : markets) {
            List<SuperbetOutcome> outcomes = market.getResolvedOutcomes();
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
        return code.equals("1X2") || code.equals("FT_1X2") || code.equals("WINNER")
                || name.equals("mecz") || name.contains("rezultat final") || name.contains("final result")
                || name.contains("câştigător meci") || name.equals("1x2") || name.equals("winner");
    }

    private void map1X2(SuperbetMarket market, List<SuperbetOutcome> outcomes, List<OddItem> oddItems, MatchCache cached) {
        boolean hasDraw = outcomes.stream().anyMatch(o -> {
            String c = o.getResolvedCode();
            String n = o.getResolvedName();
            return "X".equalsIgnoreCase(c) || "0".equalsIgnoreCase(c) || "X".equalsIgnoreCase(n)
                    || (n != null && (n.toLowerCase().contains("egal") || n.toLowerCase().contains("remis")));
        });

        for (SuperbetOutcome outcome : outcomes) {
            if (outcome.getPrice() == null || outcome.getPrice() <= 1.0) continue;
            String name = outcome.getResolvedName() != null ? outcome.getResolvedName().trim() : "";
            String code = outcome.getResolvedCode() != null ? outcome.getResolvedCode().trim().toUpperCase() : "";

            if (code.equals("1") || name.equalsIgnoreCase("1") || (cached.getTeam1() != null && name.equalsIgnoreCase(cached.getTeam1()))) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null)));
            } else if (code.equals("X") || code.equals("0") || name.equalsIgnoreCase("X") || name.equalsIgnoreCase("Egal") || name.equalsIgnoreCase("Remis")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null)));
            } else if (code.equals("2") || name.equalsIgnoreCase("2") || (cached.getTeam2() != null && name.equalsIgnoreCase(cached.getTeam2()))) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null)));
            }
        }
    }

    private boolean isTotalMarket(String name, String code) {
        return code.contains("TOTAL") || code.contains("OU") || name.contains("total")
                || name.contains("liczba goli") || name.contains("peste/sub") || name.contains("over/under");
    }

    private void mapTotals(SuperbetMarket market, List<SuperbetOutcome> outcomes, List<OddItem> oddItems) {
        Double line = extractParameter(market.getSpecifiers() != null ? market.getSpecifiers().toString() : null);
        if (line == null) {
            line = LineParamParser.parseTotalParam(market.getName());
        }

        for (SuperbetOutcome outcome : outcomes) {
            if (outcome.getPrice() == null || outcome.getPrice() <= 1.0) continue;
            Double outcomeLine = line != null ? line : outcome.getValue();
            if (outcomeLine == null && outcome.getSpecialBetValue() != null) {
                try {
                    outcomeLine = Double.parseDouble(outcome.getSpecialBetValue());
                } catch (Exception ignored) {
                }
            }
            if (outcomeLine == null) {
                outcomeLine = LineParamParser.parseTotalParam(outcome.getResolvedName(), market.getName());
            }
            if (outcomeLine == null) continue;

            String name = outcome.getResolvedName() != null ? outcome.getResolvedName().trim().toLowerCase() : "";
            String code = outcome.getResolvedCode() != null ? outcome.getResolvedCode().trim().toUpperCase() : "";

            if (code.contains("OVER") || code.equals("0") || code.equals("1") || name.contains("powyżej") || name.contains("peste") || name.contains("over") || name.startsWith(">")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, outcomeLine, false, null)));
            } else if (code.contains("UNDER") || code.equals("-0") || code.equals("-1") || name.contains("poniżej") || name.contains("sub") || name.contains("under") || name.startsWith("<")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, outcomeLine, false, null)));
            }
        }
    }

    private boolean isHandicapMarket(String name, String code) {
        return code.contains("AH") || code.contains("HANDICAP") || name.contains("handicap");
    }

    private void mapHandicap(SuperbetMarket market, List<SuperbetOutcome> outcomes, List<OddItem> oddItems, MatchCache cached) {
        Double line = extractParameter(market.getSpecifiers() != null ? market.getSpecifiers().toString() : null);
        if (line == null) {
            line = LineParamParser.parseHandicapParam(market.getName());
        }

        for (SuperbetOutcome outcome : outcomes) {
            if (outcome.getPrice() == null || outcome.getPrice() <= 1.0) continue;
            Double outcomeLine = outcome.getValue() != null ? outcome.getValue() : line;
            if (outcomeLine == null && outcome.getSpecialBetValue() != null) {
                try {
                    outcomeLine = Double.parseDouble(outcome.getSpecialBetValue());
                } catch (Exception ignored) {
                }
            }
            if (outcomeLine == null) {
                outcomeLine = LineParamParser.parseHandicapParam(outcome.getResolvedName(), market.getName());
            }
            if (outcomeLine == null) outcomeLine = 0.0;

            String name = outcome.getResolvedName() != null ? outcome.getResolvedName().trim() : "";
            String code = outcome.getResolvedCode() != null ? outcome.getResolvedCode().trim().toUpperCase() : "";

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
        return code.equals("GG") || code.equals("BTTS") || name.contains("obie drużyny") || name.contains("ambele echipe")
                || name.contains("both teams to score") || name.contains("gg/ng");
    }

    private void mapBtts(SuperbetMarket market, List<SuperbetOutcome> outcomes, List<OddItem> oddItems) {
        for (SuperbetOutcome outcome : outcomes) {
            if (outcome.getPrice() == null || outcome.getPrice() <= 1.0) continue;
            String name = outcome.getResolvedName() != null ? outcome.getResolvedName().trim().toLowerCase() : "";
            String code = outcome.getResolvedCode() != null ? outcome.getResolvedCode().trim().toUpperCase() : "";

            if (code.equals("GG") || code.equals("YES") || name.equals("tak") || name.equals("da") || name.equals("yes") || name.equals("gg")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null)));
            } else if (code.equals("NG") || code.equals("NO") || name.equals("nie") || name.equals("nu") || name.equals("no") || name.equals("ng")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null)));
            }
        }
    }

    private boolean isDoubleChanceMarket(String name, String code) {
        return code.equals("DC") || name.contains("podwójna szansa") || name.contains("şansă dublă") || name.contains("sansa dubla") || name.contains("double chance");
    }

    private void mapDoubleChance(SuperbetMarket market, List<SuperbetOutcome> outcomes, List<OddItem> oddItems) {
        for (SuperbetOutcome outcome : outcomes) {
            if (outcome.getPrice() == null || outcome.getPrice() <= 1.0) continue;
            String name = outcome.getResolvedName() != null ? outcome.getResolvedName().trim().toUpperCase() : "";
            String code = outcome.getResolvedCode() != null ? outcome.getResolvedCode().trim().toUpperCase() : "";

            if (code.equals("1X") || name.contains("1X") || name.equals("1 OR X")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null)));
            } else if (code.equals("12") || name.contains("12") || name.equals("1 OR 2")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null)));
            } else if (code.equals("X2") || name.contains("X2") || name.equals("X OR 2")) {
                oddItems.add(createOddItem(outcome, market.getName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null)));
            }
        }
    }

    private Double extractParameter(String text) {
        if (text == null) return null;
        return LineParamParser.parseGeneralParam(text);
    }

    private OddItem createOddItem(SuperbetOutcome outcome, String groupName, pro.datawiki.igaming.dto.BetType betType) {
        OddItem item = new OddItem();
        item.setFactorId(outcome.getId() != null ? String.valueOf(outcome.getId()) : (outcome.getUuid() != null ? outcome.getUuid() : null));
        item.setGroupName(groupName);
        item.setName(outcome.getResolvedName());
        item.setValue(outcome.getPrice());
        item.setBetType(betType);
        return item;
    }
}
