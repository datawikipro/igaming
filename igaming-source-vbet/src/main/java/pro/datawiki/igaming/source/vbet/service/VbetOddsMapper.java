package pro.datawiki.igaming.source.vbet.service;

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
import pro.datawiki.igaming.source.vbet.dto.VbetEvent;
import pro.datawiki.igaming.source.vbet.dto.VbetStake;
import pro.datawiki.igaming.source.vbet.dto.VbetStakeGroup;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class VbetOddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, VbetEvent event) {
        if (cached == null || event == null || event.getStakeTypes() == null || event.getStakeTypes().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("vbet");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));
        request.setExternalEventId(String.valueOf(event.getId()));
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : (event.getEnglishSportName() != null ? event.getEnglishSportName() : "General"));

        SportType sportType = sportNormalizationService.normalize(request.getSportName());
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : (event.getEnglishChampionshipName() != null ? event.getEnglishChampionshipName() : "League"));
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(event.getIsLive() != null ? event.getIsLive() : Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://sport.vbet.com/sportsbook/event/" + event.getId());

        List<OddItem> oddItems = new ArrayList<>();

        for (VbetStakeGroup group : event.getStakeTypes()) {
            if (group.getStakes() == null || group.getStakes().isEmpty()) {
                continue;
            }
            mapStakeGroup(group, oddItems, cached);
        }

        request.setOdds(oddItems);
        return request;
    }

    private void mapStakeGroup(VbetStakeGroup group, List<OddItem> oddItems, MatchCache cached) {
        Long groupId = group.getId();
        String groupName = group.getName() != null ? group.getName().toLowerCase() : "";

        if (groupId != null && groupId == 1L || groupName.contains("match result") || groupName.contains("1x2") || groupName.contains("winner")) {
            map1X2(group, oddItems, cached);
        } else if (groupId != null && groupId == 2L || groupName.contains("total")) {
            mapTotal(group, oddItems);
        } else if (groupId != null && groupId == 3L || groupName.contains("handicap")) {
            mapHandicap(group, oddItems, cached);
        } else if (groupId != null && (groupId == 702L || groupId == 46L) || groupName.contains("both teams to score") || groupName.contains("both to score")) {
            mapBothToScore(group, oddItems);
        } else if (groupId != null && groupId == 992L || groupName.contains("double chance")) {
            mapDoubleChance(group, oddItems);
        }
    }

    private void map1X2(VbetStakeGroup group, List<OddItem> oddItems, MatchCache cached) {
        boolean hasDraw = group.getStakes().stream()
                .anyMatch(s -> "X".equalsIgnoreCase(s.getName()) || (s.getName() != null && s.getName().toLowerCase().contains("draw")));

        for (VbetStake stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;
            String name = stake.getName() != null ? stake.getName().trim() : "";
            BetType betType = null;

            if ("1".equalsIgnoreCase(name) || "Team 1".equalsIgnoreCase(name) || (cached.getTeam1() != null && name.equalsIgnoreCase(cached.getTeam1()))) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if ("X".equalsIgnoreCase(name) || "Draw".equalsIgnoreCase(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            } else if ("2".equalsIgnoreCase(name) || "Team 2".equalsIgnoreCase(name) || (cached.getTeam2() != null && name.equalsIgnoreCase(cached.getTeam2()))) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(stake, group.getName(), betType));
            }
        }
    }

    private void mapTotal(VbetStakeGroup group, List<OddItem> oddItems) {
        for (VbetStake stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;
            String name = stake.getName() != null ? stake.getName().trim().toLowerCase() : "";
            Double param = stake.getArgument();

            if (param == null) {
                param = LineParamParser.parseTotalParam(name, group.getName());
            }
            if (param == null) continue;

            BetType betType = null;
            if (name.startsWith("over") || name.startsWith("больше") || name.startsWith("б")) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, null);
            } else if (name.startsWith("under") || name.startsWith("меньше") || name.startsWith("м")) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(stake, group.getName(), betType));
            }
        }
    }

    private void mapHandicap(VbetStakeGroup group, List<OddItem> oddItems, MatchCache cached) {
        for (VbetStake stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;
            String name = stake.getName() != null ? stake.getName().trim() : "";
            Double param = stake.getArgument();

            if (param == null) {
                param = LineParamParser.parseHandicapParam(name, group.getName());
            }
            if (param == null) param = 0.0;

            BetType betType = null;
            if (name.startsWith("1") || (cached.getTeam1() != null && name.toLowerCase().contains(cached.getTeam1().toLowerCase()))) {
                betType = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, param, false, null);
            } else if (name.startsWith("2") || (cached.getTeam2() != null && name.toLowerCase().contains(cached.getTeam2().toLowerCase()))) {
                betType = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, param, false, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(stake, group.getName(), betType));
            }
        }
    }

    private void mapBothToScore(VbetStakeGroup group, List<OddItem> oddItems) {
        for (VbetStake stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;
            String name = stake.getName() != null ? stake.getName().trim().toLowerCase() : "";
            BetType betType = null;

            if ("yes".equals(name) || "да".equals(name)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
            } else if ("no".equals(name) || "нет".equals(name)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(stake, group.getName(), betType));
            }
        }
    }

    private void mapDoubleChance(VbetStakeGroup group, List<OddItem> oddItems) {
        for (VbetStake stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;
            String name = stake.getName() != null ? stake.getName().trim().toUpperCase() : "";
            BetType betType = null;

            if ("1X".equals(name) || "1/X".equals(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null);
            } else if ("12".equals(name) || "1/2".equals(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null);
            } else if ("X2".equals(name) || "X/2".equals(name) || "2X".equals(name)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(stake, group.getName(), betType));
            }
        }
    }

    private Double extractParam(String str) {
        return LineParamParser.parseGeneralParam(str);
    }

    private OddItem createOddItem(VbetStake stake, String groupName, BetType betType) {
        OddItem item = new OddItem();
        item.setFactorId(String.valueOf(stake.getId()));
        item.setGroupName(groupName);
        item.setName(stake.getName());
        item.setValue(stake.getFactor());
        item.setBetType(betType);
        return item;
    }
}
