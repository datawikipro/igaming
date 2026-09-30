package pro.datawiki.igaming.source.digitain.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DigitainEsportsHandlerTest {

    private DigitainEsportsHandler handler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        handler = new DigitainEsportsHandler();

        match = new MatchCache();
        match.setId(501L);
        match.setTeam1("Natus Vincere");
        match.setTeam2("FaZe Clan");
        match.setSportName("CS2");
    }

    @Test
    void testMap1WinnerByIdAndName() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .nameRu("Победитель 1 карты")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7031L).nameEn("1").factor(1.85).build(),
                        DigitainStakeData.builder().id(7032L).nameEn("2").factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());

        OddItem item1 = items.get(0);
        assertEquals("7031", item1.getFactorId());
        assertEquals("Map 1 Winner", item1.getGroupName());
        assertEquals("1", item1.getName());
        assertEquals(1.85, item1.getValue());
        assertTrue(item1.getBetType() instanceof MatchResultBet);
        MatchResultBet bet1 = (MatchResultBet) item1.getBetType();
        assertEquals(BetScope.MAP_1, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());
        assertEquals(StatType.MATCH, bet1.statType());

        OddItem item2 = items.get(1);
        assertEquals("7032", item2.getFactorId());
        MatchResultBet bet2 = (MatchResultBet) item2.getBetType();
        assertEquals(BetScope.MAP_1, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
        assertEquals(StatType.MATCH, bet2.statType());
    }

    @Test
    void testMap2AndMap3WinnerById() {
        DigitainStakeGroupData map2Group = DigitainStakeGroupData.builder()
                .id(704L)
                .nameEn("Map 2 Winner")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7041L).nameEn("1").factor(2.10).build(),
                        DigitainStakeData.builder().id(7042L).nameEn("2").factor(1.72).build()
                ))
                .build();

        assertTrue(handler.supports(map2Group, SportType.DOTA2));
        List<OddItem> items2 = new ArrayList<>();
        handler.handle(map2Group, match, SportType.DOTA2, items2);

        assertEquals(2, items2.size());
        assertEquals(BetScope.MAP_2, ((MatchResultBet) items2.get(0).getBetType()).scope());
        assertEquals(BetScope.MAP_2, ((MatchResultBet) items2.get(1).getBetType()).scope());

        DigitainStakeGroupData map3Group = DigitainStakeGroupData.builder()
                .id(705L)
                .nameEn("Map 3 Winner")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7051L).nameEn("1").factor(1.90).build(),
                        DigitainStakeData.builder().id(7052L).nameEn("2").factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(map3Group, SportType.DOTA2));
        List<OddItem> items3 = new ArrayList<>();
        handler.handle(map3Group, match, SportType.DOTA2, items3);

        assertEquals(2, items3.size());
        assertEquals(BetScope.MAP_3, ((MatchResultBet) items3.get(0).getBetType()).scope());
        assertEquals(BetScope.MAP_3, ((MatchResultBet) items3.get(1).getBetType()).scope());
    }

    @Test
    void testMapWinnerRussianNamesAnd3WayDraw() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameRu("2-я карта Исход")
                .stakes(List.of(
                        DigitainStakeData.builder().id(801L).nameRu("П1").factor(2.20).build(),
                        DigitainStakeData.builder().id(802L).nameRu("Ничья").factor(8.00).build(),
                        DigitainStakeData.builder().id(803L).nameRu("П2").factor(1.65).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(3, items.size());

        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_2, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, bet1.outcome());

        MatchResultBet betX = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_2, betX.scope());
        assertEquals(MatchResultBet.Outcome.DRAW, betX.outcome());

        MatchResultBet bet2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(BetScope.MAP_2, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2, bet2.outcome());
    }

    @Test
    void testMapHandicapByIdAndName() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(740L)
                .nameEn("Map Handicap")
                .nameRu("Фора по картам")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7401L).nameEn("Handicap 1 (-1.5)").argument(1.5).factor(2.35).build(),
                        DigitainStakeData.builder().id(7402L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.60).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());

        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-1.5, h1.param());
        assertFalse(h1.isAsian());
        assertEquals(StatType.MAPS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, h2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param());
        assertFalse(h2.isAsian());
        assertEquals(StatType.MAPS, h2.statType());
    }

    @Test
    void testMapTotalByIdAndName() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(742L)
                .nameEn("Map Total")
                .nameRu("Тотал карт")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7421L).nameEn("Over").argument(2.5).factor(1.95).build(),
                        DigitainStakeData.builder().id(7422L).nameEn("Under").argument(2.5).factor(1.85).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());

        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, over.scope());
        assertEquals(BetSubject.MATCH, over.subject());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(2.5, over.param());
        assertFalse(over.isAsian());
        assertEquals(StatType.MAPS, over.statType());

        TotalBet under = (TotalBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, under.scope());
        assertEquals(BetSubject.MATCH, under.subject());
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(2.5, under.param());
        assertFalse(under.isAsian());
        assertEquals(StatType.MAPS, under.statType());
    }

    @Test
    void testRoundTotalOnMap1() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameEn("Map 1 Total Rounds")
                .nameRu("1-я карта Тотал по раундам")
                .stakes(List.of(
                        DigitainStakeData.builder().id(901L).nameEn("Over").argument(22.5).factor(1.88).build(),
                        DigitainStakeData.builder().id(902L).nameEn("Under").argument(22.5).factor(1.92).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());

        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, over.scope());
        assertEquals(BetSubject.MATCH, over.subject());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(22.5, over.param());
        assertFalse(over.isAsian());
        assertEquals(StatType.ROUNDS, over.statType());

        TotalBet under = (TotalBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_1, under.scope());
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(22.5, under.param());
        assertEquals(StatType.ROUNDS, under.statType());
    }

    @Test
    void testRoundHandicapOnMap1() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameEn("Map 1 Round Handicap")
                .nameRu("1-я карта Фора по раундам")
                .stakes(List.of(
                        DigitainStakeData.builder().id(911L).nameRu("Ф1 (-3.5)").argument(3.5).factor(1.85).build(),
                        DigitainStakeData.builder().id(912L).nameRu("Ф2 (+3.5)").argument(3.5).factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());

        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-3.5, h1.param());
        assertEquals(StatType.ROUNDS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_1, h2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(3.5, h2.param());
        assertEquals(StatType.ROUNDS, h2.statType());
    }

    @Test
    void testFullMatchRoundTotalAndHandicap() {
        DigitainStakeGroupData totalGroup = DigitainStakeGroupData.builder()
                .nameEn("Total Rounds")
                .nameRu("Тотал раундов")
                .stakes(List.of(
                        DigitainStakeData.builder().id(921L).nameEn("Over").argument(54.5).factor(1.90).build(),
                        DigitainStakeData.builder().id(922L).nameEn("Under").argument(54.5).factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(totalGroup, SportType.CS2));

        List<OddItem> totalItems = new ArrayList<>();
        handler.handle(totalGroup, match, SportType.CS2, totalItems);

        assertEquals(2, totalItems.size());
        TotalBet over = (TotalBet) totalItems.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, over.scope());
        assertEquals(StatType.ROUNDS, over.statType());
        assertEquals(54.5, over.param());

        DigitainStakeGroupData hdpGroup = DigitainStakeGroupData.builder()
                .nameRu("Фора по раундам")
                .stakes(List.of(
                        DigitainStakeData.builder().id(923L).nameRu("Фора 1 (-5.5)").argument(5.5).factor(1.80).build(),
                        DigitainStakeData.builder().id(924L).nameRu("Фора 2 (+5.5)").argument(5.5).factor(2.00).build()
                ))
                .build();

        assertTrue(handler.supports(hdpGroup, SportType.CS2));

        List<OddItem> hdpItems = new ArrayList<>();
        handler.handle(hdpGroup, match, SportType.CS2, hdpItems);

        assertEquals(2, hdpItems.size());
        HandicapBet h1 = (HandicapBet) hdpItems.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(StatType.ROUNDS, h1.statType());
        assertEquals(-5.5, h1.param());
    }

    @Test
    void testEsportsMatchWinnerWithTeamNames() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(702L)
                .nameEn("Match Winner")
                .nameRu("Победитель матча")
                .stakes(List.of(
                        DigitainStakeData.builder().id(7021L).nameEn("Natus Vincere").factor(1.55).build(),
                        DigitainStakeData.builder().id(7022L).nameEn("FaZe Clan").factor(2.45).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());

        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());
        assertEquals(StatType.MATCH, bet1.statType());

        MatchResultBet bet2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
        assertEquals(StatType.MATCH, bet2.statType());
    }

    @Test
    void testAsianParamOnRounds() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameEn("Map 1 Total Rounds (22.25)")
                .stakes(List.of(
                        DigitainStakeData.builder().id(931L).nameEn("Over").argument(22.25).factor(1.90).build(),
                        DigitainStakeData.builder().id(932L).nameEn("Under").argument(22.25).factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());
        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertTrue(over.isAsian());
        assertEquals(22.25, over.param());
        assertEquals(StatType.ROUNDS, over.statType());
    }

    @Test
    void testSupportsExclusions() {
        // Football Corners
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameEn("Corners Total")
                .stakes(List.of(DigitainStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Football Cards
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameEn("Yellow Cards Total")
                .stakes(List.of(DigitainStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Boxing / MMA rounds
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameEn("Round 1 Winner")
                .stakes(List.of(DigitainStakeData.builder().factor(1.8).build())).build(), SportType.MMA));

        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameEn("Total Rounds")
                .stakes(List.of(DigitainStakeData.builder().factor(1.8).build())).build(), SportType.BOXING));

        // Null / empty
        assertFalse(handler.supports((DigitainStakeGroupData) null, SportType.CS2));
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of()).build(), SportType.CS2));
    }

    @Test
    void testInvalidOddsFiltering() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        DigitainStakeData.builder().id(941L).nameEn("1").factor(1.0).build(), // <= 1.0
                        DigitainStakeData.builder().id(942L).nameEn("2").factor(null).build(), // null
                        DigitainStakeData.builder().id(943L).nameEn("1").factor(1.85).build() // valid
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(1, items.size());
        assertEquals("943", items.get(0).getFactorId());
        assertEquals(1.85, items.get(0).getValue());
    }
}
