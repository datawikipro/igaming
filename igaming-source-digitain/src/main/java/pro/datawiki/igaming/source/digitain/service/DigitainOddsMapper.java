package pro.datawiki.igaming.source.digitain.service;

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
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.digitain.dto.DigitainMatchOddsData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DigitainOddsMapper {

    private final SportNormalizationService sportNormalizationService;

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, DigitainMatchOddsData oddsData) {
        if (cached == null || oddsData == null || oddsData.getGroups() == null || oddsData.getGroups().isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("digitain");
        request.setRegions(List.of(BookmakerRegion.RU));
        request.setExternalEventId(String.valueOf(oddsData.getMatchId()));
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : "Football");

        SportType sportType = sportNormalizationService.normalize(request.getSportName());
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : "General");
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://melbet.ru/line/sport/event/" + oddsData.getMatchId());

        List<OddItem> oddItems = new ArrayList<>();

        for (DigitainStakeGroupData group : oddsData.getGroups()) {
            if (group.getStakes() == null || group.getStakes().isEmpty()) {
                continue;
            }
            mapStakeGroup(group, oddItems, cached);
        }

        if (oddItems.isEmpty()) {
            return null;
        }

        request.setOdds(oddItems);
        return request;
    }

    private void mapStakeGroup(DigitainStakeGroupData group, List<OddItem> oddItems, MatchCache cached) {
        Long groupId = group.getId();
        String groupName = (group.getNameEn() != null ? group.getNameEn() : group.getNameRu() != null ? group.getNameRu() : "").toLowerCase();

        if (groupId != null && (groupId == 1L || groupId == 702L) || groupName.contains("match result") || groupName.contains("1x2") || groupName.contains("исход")) {
            map1X2(group, oddItems, cached);
        } else if (groupId != null && groupId == 3L || groupName.contains("total") || groupName.contains("тотал")) {
            mapTotal(group, oddItems);
        } else if (groupId != null && groupId == 2L || groupName.contains("handicap") || groupName.contains("фора")) {
            mapHandicap(group, oddItems);
        } else if (groupId != null && groupId == 46L || groupName.contains("both teams to score") || groupName.contains("обе забьют")) {
            mapBothToScore(group, oddItems);
        } else if (groupId != null && groupId == 992L || groupName.contains("double chance") || groupName.contains("двойной шанс")) {
            mapDoubleChance(group, oddItems);
        }
    }

    private void map1X2(DigitainStakeGroupData group, List<OddItem> oddItems, MatchCache cached) {
        boolean hasDraw = group.getStakes().stream()
                .anyMatch(s -> "X".equalsIgnoreCase(s.getNameEn()) || "X".equalsIgnoreCase(s.getNameRu())
                        || (s.getNameEn() != null && s.getNameEn().toLowerCase().contains("draw"))
                        || (s.getNameRu() != null && s.getNameRu().toLowerCase().contains("ничья")));

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;
            String nameEn = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
            String nameRu = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
            BetType betType = null;

            if ("1".equalsIgnoreCase(nameEn) || "Win1".equalsIgnoreCase(nameEn) || "П1".equalsIgnoreCase(nameRu)
                    || (cached.getTeam1() != null && (nameEn.equalsIgnoreCase(cached.getTeam1()) || nameRu.equalsIgnoreCase(cached.getTeam1())))) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if ("X".equalsIgnoreCase(nameEn) || "X".equalsIgnoreCase(nameRu) || "Draw".equalsIgnoreCase(nameEn) || "Ничья".equalsIgnoreCase(nameRu)) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            } else if ("2".equalsIgnoreCase(nameEn) || "Win2".equalsIgnoreCase(nameEn) || "П2".equalsIgnoreCase(nameRu)
                    || (cached.getTeam2() != null && (nameEn.equalsIgnoreCase(cached.getTeam2()) || nameRu.equalsIgnoreCase(cached.getTeam2())))) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(stake, group.getNameEn(), betType, null));
            }
        }
    }

    private void mapTotal(DigitainStakeGroupData group, List<OddItem> oddItems) {
        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;
            Double arg = stake.getArgument();
            if (arg == null) continue;

            String nameEn = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
            String nameRu = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
            BetType betType = null;

            if ("Over".equalsIgnoreCase(nameEn) || "Больше".equalsIgnoreCase(nameRu) || "Б".equalsIgnoreCase(nameRu) || "O".equalsIgnoreCase(nameEn)) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, arg, false, null);
            } else if ("Under".equalsIgnoreCase(nameEn) || "Меньше".equalsIgnoreCase(nameRu) || "М".equalsIgnoreCase(nameRu) || "U".equalsIgnoreCase(nameEn)) {
                betType = new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, arg, false, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(stake, group.getNameEn(), betType, arg));
            }
        }
    }

    private void mapHandicap(DigitainStakeGroupData group, List<OddItem> oddItems) {
        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;
            Double arg = stake.getArgument();
            if (arg == null) continue;

            String nameEn = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
            String nameRu = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
            BetType betType = null;

            if ("Handicap 1".equalsIgnoreCase(nameEn) || "Фора1".equalsIgnoreCase(nameRu) || "H1".equalsIgnoreCase(nameEn) || "Ф1".equalsIgnoreCase(nameRu)) {
                betType = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, arg, false, null);
            } else if ("Handicap 2".equalsIgnoreCase(nameEn) || "Фора2".equalsIgnoreCase(nameRu) || "H2".equalsIgnoreCase(nameEn) || "Ф2".equalsIgnoreCase(nameRu)) {
                betType = new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, arg, false, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(stake, group.getNameEn(), betType, arg));
            }
        }
    }

    private void mapBothToScore(DigitainStakeGroupData group, List<OddItem> oddItems) {
        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;
            String nameEn = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
            String nameRu = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
            BetType betType = null;

            if ("Yes".equalsIgnoreCase(nameEn) || "Да".equalsIgnoreCase(nameRu)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
            } else if ("No".equalsIgnoreCase(nameEn) || "Нет".equalsIgnoreCase(nameRu)) {
                betType = new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(stake, group.getNameEn(), betType, null));
            }
        }
    }

    private void mapDoubleChance(DigitainStakeGroupData group, List<OddItem> oddItems) {
        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;
            String nameEn = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
            String nameRu = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
            BetType betType = null;

            String combined = (nameEn + " " + nameRu).toUpperCase();
            if (combined.contains("1X") || combined.contains("1-X") || combined.contains("1Х")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null);
            } else if (combined.contains("12") || combined.contains("1-2")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null);
            } else if (combined.contains("X2") || combined.contains("X-2") || combined.contains("2X") || combined.contains("Х2")) {
                betType = new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null);
            }

            if (betType != null) {
                oddItems.add(createOddItem(stake, group.getNameEn(), betType, null));
            }
        }
    }

    private OddItem createOddItem(DigitainStakeData stake, String marketName, BetType betType, Double param) {
        OddItem item = new OddItem();
        item.setFactorId(stake.getId() != null ? String.valueOf(stake.getId()) : "0");
        item.setGroupName(marketName);
        item.setName(stake.getNameEn() != null ? stake.getNameEn() : stake.getNameRu());
        item.setValue(stake.getFactor());
        item.setBetType(betType);
        return item;
    }
}
