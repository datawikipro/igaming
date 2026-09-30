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

class DigitainStatsCardsHandlerTest {

    private DigitainStatsCardsHandler handler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        handler = new DigitainStatsCardsHandler();

        match = new MatchCache();
        match.setId(301L);
        match.setTeam1("Real Madrid");
        match.setTeam2("Barcelona");
        match.setSportName("Football");
    }

    @Test
    void testYellowCards1X2ThreeWay() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameEn("Yellow Cards 1X2")
                .nameRu("Желтые карточки 1X2")
                .stakes(List.of(
                        DigitainStakeData.builder().id(2001L).nameEn("1").factor(2.10).build(),
                        DigitainStakeData.builder().id(2002L).nameEn("X").factor(4.50).build(),
                        DigitainStakeData.builder().id(2003L).nameEn("2").factor(2.40).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        OddItem item1 = items.get(0);
        assertEquals("2001", item1.getFactorId());
        assertEquals("Yellow Cards 1X2", item1.getGroupName());
        assertEquals("1", item1.getName());
        assertEquals(2.10, item1.getValue());
        assertTrue(item1.getBetType() instanceof MatchResultBet);
        MatchResultBet bet1 = (MatchResultBet) item1.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, bet1.outcome());
        assertEquals(StatType.YELLOW_CARDS, bet1.statType());

        OddItem itemX = items.get(1);
        MatchResultBet betX = (MatchResultBet) itemX.getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, betX.outcome());
        assertEquals(StatType.YELLOW_CARDS, betX.statType());

        OddItem item2 = items.get(2);
        MatchResultBet bet2 = (MatchResultBet) item2.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2, bet2.outcome());
        assertEquals(StatType.YELLOW_CARDS, bet2.statType());
    }

    @Test
    void testYellowCardsDoubleChance() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameEn("Yellow Cards Double Chance")
                .nameRu("ЖК - Двойной шанс")
                .stakes(List.of(
                        DigitainStakeData.builder().id(2101L).nameEn("1X").factor(1.40).build(),
                        DigitainStakeData.builder().id(2102L).nameEn("12").factor(1.22).build(),
                        DigitainStakeData.builder().id(2103L).nameEn("X2").factor(1.50).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        MatchResultBet dc1X = (MatchResultBet) items.get(0).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, dc1X.outcome());
        assertEquals(StatType.YELLOW_CARDS, dc1X.statType());

        MatchResultBet dc12 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_12, dc12.outcome());
        assertEquals(StatType.YELLOW_CARDS, dc12.statType());

        MatchResultBet dcX2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_X2, dcX2.outcome());
        assertEquals(StatType.YELLOW_CARDS, dcX2.statType());
    }

    @Test
    void testYellowCardsTotalById188() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .nameRu("Тотал желтых карточек")
                .stakes(List.of(
                        DigitainStakeData.builder().id(2201L).nameEn("Over").argument(4.5).factor(1.85).build(),
                        DigitainStakeData.builder().id(2202L).nameEn("Under").argument(4.5).factor(1.95).build()
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
        assertEquals(4.5, over.param());
        assertFalse(over.isAsian());
        assertEquals(StatType.YELLOW_CARDS, over.statType());

        TotalBet under = (TotalBet) items.get(1).getBetType();
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(4.5, under.param());
        assertFalse(under.isAsian());
        assertEquals(StatType.YELLOW_CARDS, under.statType());
    }

    @Test
    void testYellowCardsAsianTotal() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameEn("Bookings Total (4.25)")
                .nameRu("Тотал ЖК (4.25)")
                .stakes(List.of(
                        DigitainStakeData.builder().id(2301L).nameEn("Over").argument(4.25).factor(1.90).build(),
                        DigitainStakeData.builder().id(2302L).nameEn("Under").argument(4.25).factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertTrue(over.isAsian());
        assertEquals(4.25, over.param());
        assertEquals(StatType.YELLOW_CARDS, over.statType());
    }

    @Test
    void testIndividualYellowCardsTotal() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .nameEn("Yellow Cards Team 2 Total")
                .nameRu("Индивидуальный тотал ЖК 2-й команды")
                .stakes(List.of(
                        DigitainStakeData.builder().id(2401L).nameRu("Больше 2.5").argument(2.5).factor(2.15).build(),
                        DigitainStakeData.builder().id(2402L).nameRu("Меньше 2.5").argument(2.5).factor(1.68).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetSubject.TEAM2, over.subject());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(2.5, over.param());
        assertEquals(StatType.YELLOW_CARDS, over.statType());
    }

    @Test
    void testYellowCardsHandicapById189() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(189L)
                .nameEn("Yellow Cards Handicap")
                .nameRu("Фора по ЖК")
                .stakes(List.of(
                        DigitainStakeData.builder().id(2501L).nameEn("H1 (-0.5)").argument(0.5).factor(1.95).build(),
                        DigitainStakeData.builder().id(2502L).nameEn("H2 (+0.5)").argument(0.5).factor(1.85).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-0.5, h1.param());
        assertFalse(h1.isAsian());
        assertEquals(StatType.YELLOW_CARDS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(0.5, h2.param());
        assertFalse(h2.isAsian());
        assertEquals(StatType.YELLOW_CARDS, h2.statType());
    }

    @Test
    void testYellowCardsHalves() {
        DigitainStakeGroupData half1Group = DigitainStakeGroupData.builder()
                .nameEn("1st Half Yellow Cards Total")
                .stakes(List.of(
                        DigitainStakeData.builder().id(2601L).nameEn("Over").argument(1.5).factor(1.75).build(),
                        DigitainStakeData.builder().id(2602L).nameEn("Under").argument(1.5).factor(2.05).build()
                ))
                .build();

        assertTrue(handler.supports(half1Group, SportType.FOOTBALL));

        List<OddItem> items1 = new ArrayList<>();
        handler.handle(half1Group, match, SportType.FOOTBALL, items1);

        assertEquals(2, items1.size());
        assertEquals(BetScope.HALF_1, ((TotalBet) items1.get(0).getBetType()).scope());
        assertEquals(StatType.YELLOW_CARDS, ((TotalBet) items1.get(0).getBetType()).statType());

        DigitainStakeGroupData half2Group = DigitainStakeGroupData.builder()
                .nameRu("2-й тайм ЖК Фора")
                .stakes(List.of(
                        DigitainStakeData.builder().id(2603L).nameRu("Ф1 (0)").argument(0.0).factor(1.80).build(),
                        DigitainStakeData.builder().id(2604L).nameRu("Ф2 (0)").argument(0.0).factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(half2Group, SportType.FOOTBALL));

        List<OddItem> items2 = new ArrayList<>();
        handler.handle(half2Group, match, SportType.FOOTBALL, items2);

        assertEquals(2, items2.size());
        assertEquals(BetScope.HALF_2, ((HandicapBet) items2.get(0).getBetType()).scope());
        assertEquals(StatType.YELLOW_CARDS, ((HandicapBet) items2.get(0).getBetType()).statType());
    }

    @Test
    void testSupportsExclusions() {
        // Corners
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameEn("Corners Total")
                .stakes(List.of(DigitainStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Red card / Sending off
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameEn("Red Card Shown (Yes/No)")
                .stakes(List.of(DigitainStakeData.builder().factor(3.5).build())).build(), SportType.FOOTBALL));

        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameRu("Красная карточка: Да/Нет")
                .stakes(List.of(DigitainStakeData.builder().factor(3.5).build())).build(), SportType.FOOTBALL));

        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameRu("Удаление игрока")
                .stakes(List.of(DigitainStakeData.builder().factor(3.5).build())).build(), SportType.FOOTBALL));

        // Esports Map
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .nameEn("Map 1 Cards")
                .stakes(List.of(DigitainStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Empty stakes
        assertFalse(handler.supports(DigitainStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .stakes(List.of()).build(), SportType.FOOTBALL));

        // Null group
        assertFalse(handler.supports((DigitainStakeGroupData) null, SportType.FOOTBALL));
    }

    @Test
    void testInvalidOddsFiltering() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .stakes(List.of(
                        DigitainStakeData.builder().id(2701L).nameEn("Over").argument(3.5).factor(1.0).build(), // <= 1.0
                        DigitainStakeData.builder().id(2702L).nameEn("Under").argument(3.5).factor(null).build(), // null factor
                        DigitainStakeData.builder().id(2703L).nameEn("Over").argument(3.5).factor(1.82).build() // valid
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(1, items.size());
        assertEquals("2703", items.get(0).getFactorId());
        assertEquals(1.82, items.get(0).getValue());
    }
}
