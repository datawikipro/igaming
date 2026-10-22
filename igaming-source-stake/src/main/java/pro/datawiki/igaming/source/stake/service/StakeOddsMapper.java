package pro.datawiki.igaming.source.stake.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.mapper.rule.LineParamParser;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.stake.dto.StakeFixtureDto;
import pro.datawiki.igaming.source.stake.dto.StakeMarketDto;
import pro.datawiki.igaming.source.stake.dto.StakeOutcomeDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class StakeOddsMapper extends AbstractBetTypeMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "stake".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(StakeFixtureDto fixture) {
        if (fixture == null || fixture.getId() == null) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("stake");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));
        request.setExternalEventId(fixture.getId());

        String sportName = fixture.getSport() != null && fixture.getSport().getName() != null
                ? fixture.getSport().getName() : "General";
        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);
        request.setSportName(sportName);

        String league = fixture.getTournament() != null && fixture.getTournament().getName() != null
                ? fixture.getTournament().getName() : "Global";
        request.setLeagueName(league);

        // Resolve teams
        String team1 = "Home";
        String team2 = "Away";
        if (fixture.getCompetitors() != null && fixture.getCompetitors().size() >= 2) {
            for (StakeFixtureDto.CompetitorRef comp : fixture.getCompetitors()) {
                if ("home".equalsIgnoreCase(comp.getQualifier())) {
                    team1 = comp.getName();
                } else if ("away".equalsIgnoreCase(comp.getQualifier())) {
                    team2 = comp.getName();
                }
            }
            if ("Home".equals(team1) && "Away".equals(team2)) {
                team1 = fixture.getCompetitors().get(0).getName();
                team2 = fixture.getCompetitors().get(1).getName();
            }
        } else if (fixture.getName() != null) {
            String[] parts = fixture.getName().split(" vs | - | @ ");
            if (parts.length >= 2) {
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive("live".equalsIgnoreCase(fixture.getStatus()) || "active".equalsIgnoreCase(fixture.getStatus()));

        if (fixture.getStartTime() instanceof Number num) {
            request.setStartTime(num.longValue());
        } else if (fixture.getStartTime() instanceof String str) {
            try {
                request.setStartTime(Instant.parse(str).toEpochMilli());
            } catch (Exception e) {
                request.setStartTime(System.currentTimeMillis());
            }
        } else {
            request.setStartTime(System.currentTimeMillis());
        }

        List<OddItem> items = new ArrayList<>();

        if (fixture.getMarkets() != null) {
            for (StakeMarketDto market : fixture.getMarkets()) {
                if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
                    continue;
                }
                mapMarket(items, market, team1, team2);
            }
        }

        request.setOdds(items);
        return request;
    }

    private void mapMarket(List<OddItem> items, StakeMarketDto market, String team1, String team2) {
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";

        if (mName.contains("winner") || mName.contains("1x2") || mName.contains("match result") || mName.equals("moneyline")) {
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

    private void map1X2(List<OddItem> items, StakeMarketDto market, String team1, String team2) {
        boolean hasDraw = market.getOutcomes().stream().anyMatch(o -> "draw".equalsIgnoreCase(o.getName()) || "x".equalsIgnoreCase(o.getName()));

        for (StakeOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;
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
                items.add(createOddItem(outcome, market.getName(), betType, odds));
            }
        }
    }

    private void mapTotal(List<OddItem> items, StakeMarketDto market) {
        for (StakeOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;
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
                items.add(createOddItem(outcome, market.getName(), betType, odds));
            }
        }
    }

    private void mapHandicap(List<OddItem> items, StakeMarketDto market, String team1, String team2) {
        for (StakeOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;
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
                items.add(createOddItem(outcome, market.getName(), betType, odds));
            }
        }
    }

    private void mapBtts(List<OddItem> items, StakeMarketDto market) {
        for (StakeOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toLowerCase() : "";
            BetType betType = null;

            if ("yes".equalsIgnoreCase(name)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
            } else if ("no".equalsIgnoreCase(name)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
            }

            if (betType != null) {
                items.add(createOddItem(outcome, market.getName(), betType, odds));
            }
        }
    }

    private void mapDoubleChance(List<OddItem> items, StakeMarketDto market) {
        for (StakeOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;
            String name = outcome.getName() != null ? outcome.getName().trim().toUpperCase() : "";
            BetType betType = null;

            if ("1X".equals(name) || "1/X".equals(name) || name.contains("HOME/DRAW")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null);
            } else if ("12".equals(name) || "1/2".equals(name) || name.contains("HOME/AWAY")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null);
            } else if ("X2".equals(name) || "X/2".equals(name) || "2X".equals(name) || name.contains("DRAW/AWAY")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null);
            }

            if (betType != null) {
                items.add(createOddItem(outcome, market.getName(), betType, odds));
            }
        }
    }

    private Double extractParam(String str) {
        return LineParamParser.parseGeneralParam(str);
    }

    private OddItem createOddItem(StakeOutcomeDto outcome, String groupName, BetType betType, Double odds) {
        OddItem item = new OddItem();
        item.setFactorId(outcome.getId());
        item.setGroupName(groupName);
        item.setName(outcome.getName());
        item.setValue(odds);
        item.setBetType(betType);
        return item;
    }
}
