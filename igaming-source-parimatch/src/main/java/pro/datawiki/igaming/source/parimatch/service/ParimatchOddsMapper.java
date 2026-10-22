package pro.datawiki.igaming.source.parimatch.service;

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
import pro.datawiki.igaming.source.core.mapper.rule.LineParamParser;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchEvent;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchMarket;
import pro.datawiki.igaming.source.parimatch.dto.ParimatchOutcome;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParimatchOddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, ParimatchEvent event) {
        if (cached == null || event == null || event.getMarkets() == null || event.getMarkets().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("parimatch");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.CIS, BookmakerRegion.EU));
        request.setExternalEventId(event.getId());
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : (event.getSportName() != null ? event.getSportName() : "General"));

        SportType sportType = sportNormalizationService.normalize(request.getSportName());
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : (event.getTournamentName() != null ? event.getTournamentName() : "League"));
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(event.getIsLive() != null ? event.getIsLive() : Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://parimatch.com/en/events/" + event.getId());

        List<OddItem> oddItems = new ArrayList<>();

        for (ParimatchMarket market : event.getMarkets()) {
            if (market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
                continue;
            }
            mapMarket(market, oddItems, cached, request);
        }

        request.setOdds(oddItems);
        return request;
    }

    private void mapMarket(ParimatchMarket market, List<OddItem> oddItems, MatchCache cached, OddsUpdateRequest request) {
        String marketName = market.getName() != null ? market.getName().toLowerCase() : "";

        if (marketName.contains("result") || marketName.contains("1x2") || marketName.contains("winner") || marketName.contains("исход")) {
            map1X2(market, oddItems, cached);
        } else if (marketName.contains("total") || marketName.contains("тотал")) {
            mapTotal(market, oddItems);
        } else if (marketName.contains("handicap") || marketName.contains("фора") || marketName.contains("spread")) {
            mapHandicap(market, oddItems, cached);
        } else if (marketName.contains("both teams to score") || marketName.contains("both to score") || marketName.contains("обе забьют")) {
            mapBothToScore(market, oddItems);
        } else if (marketName.contains("double chance") || marketName.contains("двойной исход") || marketName.contains("двойной шанс")) {
            mapDoubleChance(market, oddItems);
        }
    }

    private void map1X2(ParimatchMarket market, List<OddItem> oddItems, MatchCache cached) {
        boolean hasDraw = market.getOutcomes().stream()
                .anyMatch(o -> "X".equalsIgnoreCase(o.getName()) || (o.getName() != null && o.getName().toLowerCase().contains("draw")));

        for (ParimatchOutcome outcome : market.getOutcomes()) {
            Double odd = outcome.getEffectiveOdd();
            if (odd == null || odd <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim() : "";
            BetType betType = null;

            if ("1".equalsIgnoreCase(name) || "Team 1".equalsIgnoreCase(name) || (cached.getTeam1() != null && name.equalsIgnoreCase(cached.getTeam1()))) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if ("X".equalsIgnoreCase(name) || "Draw".equalsIgnoreCase(name) || "Ничья".equalsIgnoreCase(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            } else if ("2".equalsIgnoreCase(name) || "Team 2".equalsIgnoreCase(name) || (cached.getTeam2() != null && name.equalsIgnoreCase(cached.getTeam2()))) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(outcome, market.getName(), betType, odd));
            }
        }
    }

    private void mapTotal(ParimatchMarket market, List<OddItem> oddItems) {
        for (ParimatchOutcome outcome : market.getOutcomes()) {
            Double odd = outcome.getEffectiveOdd();
            if (odd == null || odd <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toLowerCase() : "";
            Double param = outcome.getParam();

            if (param == null) {
                param = LineParamParser.parseTotalParam(name, market.getName());
            }
            if (param == null) continue;

            BetType betType = null;
            if (name.startsWith("over") || name.startsWith("больше") || name.startsWith("б")) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, null);
            } else if (name.startsWith("under") || name.startsWith("меньше") || name.startsWith("м")) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(outcome, market.getName(), betType, odd));
            }
        }
    }

    private void mapHandicap(ParimatchMarket market, List<OddItem> oddItems, MatchCache cached) {
        for (ParimatchOutcome outcome : market.getOutcomes()) {
            Double odd = outcome.getEffectiveOdd();
            if (odd == null || odd <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim() : "";
            Double param = outcome.getParam();

            if (param == null) {
                param = LineParamParser.parseHandicapParam(name, market.getName());
            }
            if (param == null) param = 0.0;

            BetType betType = null;
            if (name.startsWith("1") || (cached.getTeam1() != null && name.toLowerCase().contains(cached.getTeam1().toLowerCase()))) {
                betType = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, param, false, null);
            } else if (name.startsWith("2") || (cached.getTeam2() != null && name.toLowerCase().contains(cached.getTeam2().toLowerCase()))) {
                betType = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, param, false, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(outcome, market.getName(), betType, odd));
            }
        }
    }

    private void mapBothToScore(ParimatchMarket market, List<OddItem> oddItems) {
        for (ParimatchOutcome outcome : market.getOutcomes()) {
            Double odd = outcome.getEffectiveOdd();
            if (odd == null || odd <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toLowerCase() : "";
            BetType betType = null;

            if ("yes".equals(name) || "да".equals(name)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
            } else if ("no".equals(name) || "нет".equals(name)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(outcome, market.getName(), betType, odd));
            }
        }
    }

    private void mapDoubleChance(ParimatchMarket market, List<OddItem> oddItems) {
        for (ParimatchOutcome outcome : market.getOutcomes()) {
            Double odd = outcome.getEffectiveOdd();
            if (odd == null || odd <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toUpperCase() : "";
            BetType betType = null;

            if ("1X".equals(name) || "1/X".equals(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null);
            } else if ("12".equals(name) || "1/2".equals(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null);
            } else if ("X2".equals(name) || "X/2".equals(name) || "2X".equals(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(outcome, market.getName(), betType, odd));
            }
        }
    }

    private Double extractParam(String str) {
        return LineParamParser.parseGeneralParam(str);
    }

    private OddItem createOddItem(ParimatchOutcome outcome, String groupName, BetType betType, Double odd) {
        OddItem item = new OddItem();
        item.setFactorId(outcome.getId());
        item.setGroupName(groupName);
        item.setName(outcome.getName());
        item.setValue(odd);
        item.setBetType(betType);
        return item;
    }
}
