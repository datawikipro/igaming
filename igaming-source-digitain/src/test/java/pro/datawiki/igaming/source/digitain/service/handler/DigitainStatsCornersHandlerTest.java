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

class DigitainStatsCornersHandlerTest {

    private DigitainStatsCornersHandler handler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        handler = new DigitainStatsCornersHandler();

        match = new MatchCache();
        match.setId(201L);
        match.setTeam1("Arsenal");
        match.setTeam2("Chelsea");
        match.setSportName("Football");
    }

    @Test
    void testCorners1X2ThreeWayById() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(168L)
                .nameEn("Corners 1X2")
                .nameRu("Угловые 1X2")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1681L).nameEn("1").factor(1.80).build(),
                        DigitainStakeData.builder().id(1682L).nameEn("X").factor(7.50).build(),
                        DigitainStakeData.builder().id(1683L).nameEn("2").factor(2.30).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        OddItem item1 = items.get(0);
        assertEquals("1681", item1.getFactorId());
        assertEquals("Corners 1X2", item1.getGroupName());
        assertEquals("1", item1.getName());
        assertEquals(1.80, item1.getValue());
        assertTrue(item1.getBetType() instanceof MatchResultBet);
        MatchResultBet bet1 = (MatchResultBet) item1.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, bet1.outcome());
        assertEquals(StatType.CORNERS, bet1.statType());

        OddItem itemX = items.get(1);
        assertEquals("1682", itemX.getFactorId());
        MatchResultBet betX = (MatchResultBet) itemX.getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, betX.outcome());
        assertEquals(StatType.CORNERS, betX.statType());

        OddItem item2 = items.get(2);
        assertEquals("1683", item2.getFactorId());
        MatchResultBet bet2 = (MatchResultBet) item2.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2, bet2.outcome());
        assertEquals(StatType.CORNERS, bet2.statType());
    }

    @Test
    void testCornersWinnerTwoWayRussian() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameRu("Победа по угловым")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1001L).nameRu("П1").factor(1.65).build(),
                        DigitainStakeData.builder().id(1002L).nameRu("П2").factor(2.20).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());
        assertEquals(StatType.CORNERS, bet1.statType());

        MatchResultBet bet2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
        assertEquals(StatType.CORNERS, bet2.statType());
    }

    @Test
    void testCornersDoubleChance() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameEn("Corners Double Chance")
                .nameRu("Угловые - Двойной шанс")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1101L).nameEn("1X").factor(1.25).build(),
                        DigitainStakeData.builder().id(1102L).nameEn("12").factor(1.15).build(),
                        DigitainStakeData.builder().id(1103L).nameEn("X2").factor(1.70).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        MatchResultBet dc1X = (MatchResultBet) items.get(0).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, dc1X.outcome());
        assertEquals(StatType.CORNERS, dc1X.statType());

        MatchResultBet dc12 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_12, dc12.outcome());
        assertEquals(StatType.CORNERS, dc12.statType());

        MatchResultBet dcX2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_X2, dcX2.outcome());
        assertEquals(StatType.CORNERS, dcX2.statType());
    }

    @Test
    void testCornersTotalByIdAndNameOverUnder() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(166L)
                .nameEn("Corners Total")
                .nameRu("Тотал угловых")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1661L).nameEn("Over").argument(9.5).factor(1.85).build(),
                        DigitainStakeData.builder().id(1662L).nameEn("Under").argument(9.5).factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, over.scope());
        assertEquals(BetSubject.MATCH, over.subject());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(9.5, over.param());
        assertFalse(over.isAsian());
        assertEquals(StatType.CORNERS, over.statType());

        TotalBet under = (TotalBet) items.get(1).getBetType();
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(9.5, under.param());
        assertFalse(under.isAsian());
        assertEquals(StatType.CORNERS, under.statType());
    }

    @Test
    void testCornersAsianTotal() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameEn("Corners Total (9.75)")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1201L).nameEn("Over").argument(9.75).factor(1.90).build(),
                        DigitainStakeData.builder().id(1202L).nameEn("Under").argument(9.75).factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertTrue(over.isAsian());
        assertEquals(9.75, over.param());
        assertEquals(StatType.CORNERS, over.statType());
    }

    @Test
    void testIndividualCornersTotal() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameEn("Corners Team 1 Total")
                .nameRu("Индивидуальный тотал угловых 1-й команды")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1301L).nameRu("Больше 5.5").argument(5.5).factor(1.75).build(),
                        DigitainStakeData.builder().id(1302L).nameRu("Меньше 5.5").argument(5.5).factor(2.05).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetSubject.TEAM1, over.subject());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(5.5, over.param());
        assertEquals(StatType.CORNERS, over.statType());
    }

    @Test
    void testCornersHandicapByIdAndName() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(167L)
                .nameEn("Corners Handicap")
                .nameRu("Фора угловых")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1671L).nameEn("Handicap 1 (-1.5)").argument(1.5).factor(1.92).build(),
                        DigitainStakeData.builder().id(1672L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.88).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-1.5, h1.param());
        assertFalse(h1.isAsian());
        assertEquals(StatType.CORNERS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param());
        assertFalse(h2.isAsian());
        assertEquals(StatType.CORNERS, h2.statType());
    }

    @Test
    void testCornersHalf1AndHalf2() {
        DigitainStakeGroupData half1Group = DigitainStakeGroupData.builder()
                .nameEn("1st Half Corners Total")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1401L).nameEn("Over").argument(4.5).factor(1.80).build(),
                        DigitainStakeData.builder().id(1402L).nameEn("Under").argument(4.5).factor(2.00).build()
                ))
                .build();

        assertTrue(handler.supports(half1Group, SportType.FOOTBALL));

        List<OddItem> items1 = new ArrayList<>();
        handler.handle(half1Group, match, SportType.FOOTBALL, items1);

        assertEquals(2, items1.size());
        assertEquals(BetScope.HALF_1, ((TotalBet) items1.get(0).getBetType()).scope());
        assertEquals(StatType.CORNERS, ((TotalBet) items1.get(0).getBetType()).statType());

        DigitainStakeGroupData half2Group = DigitainStakeGroupData.builder()
                .nameRu("2-й тайм Угловые Фора")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1403L).nameRu("Ф1 (0)").argument(0.0).factor(1.70).build(),
                        DigitainStakeData.builder().id(1404L).nameRu("Ф2 (0)").argument(0.0).factor(2.10).build()
                ))
                .build();

        assertTrue(handler.supports(half2Group, SportType.FOOTBALL));

        List<OddItem> items2 = new ArrayList<>();
        handler.handle(half2Group, match, SportType.FOOTBALL, items2);

        assertEquals(2, items2.size());
        assertEquals(BetScope.HALF_2, ((HandicapBet) items2.get(0).getBetType()).scope());
        assertEquals(StatType.CORNERS, ((HandicapBet) items2.get(0).getBetType()).statType());
    }

    @Test
    void testSupportsExclusions() {
        // Cards
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameEn("Yellow Cards Total")
                .stakes(List.of(DigitainStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Esports Map
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameEn("Map 1 Corners")
                .stakes(List.of(DigitainStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Pure Match Result
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameEn("Match Result")
                .stakes(List.of(DigitainStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Empty stakes
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .id(166L)
                .nameEn("Corners Total")
                .stakes(List.of()).build(), SportType.FOOTBALL));

        // Null group
        assertFalse(handler.supports((DigitainStakeGroupData) null, SportType.FOOTBALL));
    }

    @Test
    void testInvalidOddsFiltering() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(166L)
                .nameEn("Corners Total")
                .stakes(List.of(
                        DigitainStakeData.builder().id(1501L).nameEn("Over").argument(9.5).factor(1.0).build(), // <= 1.0
                        DigitainStakeData.builder().id(1502L).nameEn("Under").argument(9.5).factor(null).build(), // null factor
                        DigitainStakeData.builder().id(1503L).nameEn("Over").argument(9.5).factor(1.90).build() // valid
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(1, items.size());
        assertEquals("1503", items.get(0).getFactorId());
        assertEquals(1.90, items.get(0).getValue());
    }
}
