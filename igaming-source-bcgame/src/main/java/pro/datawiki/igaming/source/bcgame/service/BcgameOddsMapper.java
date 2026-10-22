package pro.datawiki.igaming.source.bcgame.service;

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
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pro.datawiki.igaming.source.core.mapper.rule.LineParamParser;

@Component
@Slf4j
@RequiredArgsConstructor
public class BcgameOddsMapper extends AbstractBetTypeMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "bcgame".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cache, BcgameEventDto event) {
        if (cache == null || event == null || event.getMarkets() == null || event.getMarkets().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("bcgame");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));
        request.setExternalEventId(event.getId() != null ? event.getId() : cache.getExternalId());

        String sportName = cache.getSportName() != null ? cache.getSportName() : (event.getSportName() != null ? event.getSportName() : "General");
        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);
        request.setSportName(sportName);

        request.setLeagueName(cache.getLeagueName() != null ? cache.getLeagueName() : (event.getTournamentName() != null ? event.getTournamentName() : "General"));
        request.setTeam1(cache.getTeam1());
        request.setTeam2(cache.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(event.getIsLive()) || Boolean.TRUE.equals(cache.getIsLive()));
        request.setStartTime(cache.getStartTime());

        List<OddItem> items = new ArrayList<>();

        for (BcgameMarketDto market : event.getMarkets()) {
            if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
                continue;
            }
            mapMarket(items, market, cache.getTeam1(), cache.getTeam2());
        }

        request.setOdds(items);
        return request;
    }

    private void mapMarket(List<OddItem> items, BcgameMarketDto market, String team1, String team2) {
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";

        if (mName.contains("1x2") || mName.contains("winner") || mName.contains("match result") || mName.contains("moneyline")) {
            map1X2(items, market, team1, team2);
        } else if (mName.contains("total") || mName.contains("over/under")) {
            mapTotal(items, market);
        } else if (mName.contains("handicap") || mName.contains("spread")) {
            mapHandicap(items, market, team1, team2);
        } else if (mName.contains("both teams to score") || mName.contains("btts")) {
            mapBtts(items, market);
        } else if (mName.contains("double chance")) {
            mapDoubleChance(items, market);
        }
    }

    private void map1X2(List<OddItem> items, BcgameMarketDto market, String team1, String team2) {
        boolean hasDraw = market.getOutcomes().stream().anyMatch(o -> "x".equalsIgnoreCase(o.getName()) || "draw".equalsIgnoreCase(o.getName()));

        for (BcgameOutcomeDto outcome : market.getOutcomes()) {
            Double price = outcome.getEffectiveOdds();
            if (price == null || price <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim() : "";
            BetType betType = null;

            if ("1".equalsIgnoreCase(name) || (team1 != null && name.equalsIgnoreCase(team1)) || "home".equalsIgnoreCase(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if ("X".equalsIgnoreCase(name) || "Draw".equalsIgnoreCase(name) || "Tie".equalsIgnoreCase(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            } else if ("2".equalsIgnoreCase(name) || (team2 != null && name.equalsIgnoreCase(team2)) || "away".equalsIgnoreCase(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null);
            }

            if (betType != null) {
                items.add(createOddItem(outcome, market.getName(), betType, price));
            }
        }
    }

    private void mapTotal(List<OddItem> items, BcgameMarketDto market) {
        for (BcgameOutcomeDto outcome : market.getOutcomes()) {
            Double price = outcome.getEffectiveOdds();
            if (price == null || price <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toLowerCase() : "";
            Double param = outcome.getEffectiveParam();
            if (param == null) {
                param = LineParamParser.parseTotalParam(name, market.getName());
            }
            if (param == null) continue;

            BetType betType = null;
            if (name.startsWith("over") || name.startsWith("o ") || name.contains("over")) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, null);
            } else if (name.startsWith("under") || name.startsWith("u ") || name.contains("under")) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, null);
            }

            if (betType != null) {
                items.add(createOddItem(outcome, market.getName(), betType, price));
            }
        }
    }

    private void mapHandicap(List<OddItem> items, BcgameMarketDto market, String team1, String team2) {
        for (BcgameOutcomeDto outcome : market.getOutcomes()) {
            Double price = outcome.getEffectiveOdds();
            if (price == null || price <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim() : "";
            Double param = outcome.getEffectiveParam();
            if (param == null) {
                param = LineParamParser.parseHandicapParam(name, market.getName());
            }
            if (param == null) param = 0.0;

            BetType betType = null;
            if (name.startsWith("1") || (team1 != null && name.toLowerCase().contains(team1.toLowerCase())) || name.toLowerCase().contains("home")) {
                betType = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, param, false, null);
            } else if (name.startsWith("2") || (team2 != null && name.toLowerCase().contains(team2.toLowerCase())) || name.toLowerCase().contains("away")) {
                betType = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, param, false, null);
            }

            if (betType != null) {
                items.add(createOddItem(outcome, market.getName(), betType, price));
            }
        }
    }

    private void mapBtts(List<OddItem> items, BcgameMarketDto market) {
        for (BcgameOutcomeDto outcome : market.getOutcomes()) {
            Double price = outcome.getEffectiveOdds();
            if (price == null || price <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toLowerCase() : "";
            BetType betType = null;

            if ("yes".equalsIgnoreCase(name)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
            } else if ("no".equalsIgnoreCase(name)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
            }

            if (betType != null) {
                items.add(createOddItem(outcome, market.getName(), betType, price));
            }
        }
    }

    private void mapDoubleChance(List<OddItem> items, BcgameMarketDto market) {
        for (BcgameOutcomeDto outcome : market.getOutcomes()) {
            Double price = outcome.getEffectiveOdds();
            if (price == null || price <= 1.0) continue;
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
                items.add(createOddItem(outcome, market.getName(), betType, price));
            }
        }
    }

    private Double extractParam(String str) {
        return LineParamParser.parseGeneralParam(str);
    }

    private OddItem createOddItem(BcgameOutcomeDto outcome, String groupName, BetType betType, Double price) {
        OddItem item = new OddItem();
        item.setFactorId(outcome.getId());
        item.setGroupName(groupName);
        item.setName(outcome.getName());
        item.setValue(price);
        item.setBetType(betType);
        return item;
    }
}
