package pro.datawiki.igaming.source.vaidebet.service.handler;

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
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeData;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeGroupData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VaidebetStatsCardsHandlerTest {

    private VaidebetStatsCardsHandler handler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        handler = new VaidebetStatsCardsHandler();

        match = new MatchCache();
        match.setId(301L);
        match.setTeam1("Flamengo");
        match.setTeam2("Palmeiras");
        match.setSportName("Football");
    }

    @Test
    void testCards1X2ThreeWayById() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .id(187L)
                .nameEn("Yellow Cards 1X2")
                .nameRu("Желтые карточки 1X2")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(1871L).nameEn("1").factor(2.10).build(),
                        VaidebetStakeData.builder().id(1872L).nameEn("X").factor(4.20).build(),
                        VaidebetStakeData.builder().id(1873L).nameEn("2").factor(2.60).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        OddItem item1 = items.get(0);
        assertEquals("1871", item1.getFactorId());
        assertEquals("Yellow Cards 1X2", item1.getGroupName());
        assertEquals("1", item1.getName());
        assertEquals(2.10, item1.getValue());
        assertTrue(item1.getBetType() instanceof MatchResultBet);
        MatchResultBet bet1 = (MatchResultBet) item1.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, bet1.outcome());
        assertEquals(StatType.YELLOW_CARDS, bet1.statType());

        OddItem itemX = items.get(1);
        assertEquals("1872", itemX.getFactorId());
        MatchResultBet betX = (MatchResultBet) itemX.getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, betX.outcome());
        assertEquals(StatType.YELLOW_CARDS, betX.statType());

        OddItem item2 = items.get(2);
        assertEquals("1873", item2.getFactorId());
        MatchResultBet bet2 = (MatchResultBet) item2.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2, bet2.outcome());
        assertEquals(StatType.YELLOW_CARDS, bet2.statType());
    }

    @Test
    void testCardsWinnerTwoWayRussian() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameRu("Победа по желтым карточкам")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2001L).nameRu("П1").factor(1.75).build(),
                        VaidebetStakeData.builder().id(2002L).nameRu("П2").factor(2.10).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());
        assertEquals(StatType.YELLOW_CARDS, bet1.statType());

        MatchResultBet bet2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
        assertEquals(StatType.YELLOW_CARDS, bet2.statType());
    }

    @Test
    void testCardsWinnerPortuguesePtBr() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Vencedor dos Cartões Amarelos")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2011L).nameEn("Mandante").factor(1.95).build(),
                        VaidebetStakeData.builder().id(2012L).nameEn("Empate").factor(4.50).build(),
                        VaidebetStakeData.builder().id(2013L).nameEn("Visitante").factor(2.80).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        MatchResultBet b1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, b1.outcome());
        assertEquals(StatType.YELLOW_CARDS, b1.statType());

        MatchResultBet bX = (MatchResultBet) items.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, bX.outcome());
        assertEquals(StatType.YELLOW_CARDS, bX.statType());

        MatchResultBet b2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2, b2.outcome());
        assertEquals(StatType.YELLOW_CARDS, b2.statType());
    }

    @Test
    void testCardsDoubleChance() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Yellow Cards Double Chance")
                .nameRu("ЖК - Двойной шанс")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2101L).nameEn("1X").factor(1.35).build(),
                        VaidebetStakeData.builder().id(2102L).nameEn("12").factor(1.22).build(),
                        VaidebetStakeData.builder().id(2103L).nameEn("X2").factor(1.55).build()
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
    void testCardsDoubleChancePtBr() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Dupla Chance de Cartões")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2111L).nameEn("Mandante ou Empate").factor(1.40).build(),
                        VaidebetStakeData.builder().id(2112L).nameEn("Mandante ou Visitante").factor(1.20).build(),
                        VaidebetStakeData.builder().id(2113L).nameEn("Empate ou Visitante").factor(1.50).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) items.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) items.get(1).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) items.get(2).getBetType()).outcome());
        assertEquals(StatType.YELLOW_CARDS, ((MatchResultBet) items.get(0).getBetType()).statType());
    }

    @Test
    void testCardsTotalByIdAndNameOverUnder() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .nameRu("Тотал желтых карточек")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(1881L).nameEn("Over").argument(4.5).factor(1.90).build(),
                        VaidebetStakeData.builder().id(1882L).nameEn("Under").argument(4.5).factor(1.90).build()
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
    void testCardsTotalPortuguesePtBr() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Total de Cartões Mais/Menos")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2121L).nameEn("Acima de 3.5").argument(3.5).factor(1.75).build(),
                        VaidebetStakeData.builder().id(2122L).nameEn("Abaixo de 3.5").argument(3.5).factor(2.05).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) items.get(0).getBetType()).direction());
        assertEquals(TotalBet.Direction.UNDER, ((TotalBet) items.get(1).getBetType()).direction());
        assertEquals(StatType.YELLOW_CARDS, ((TotalBet) items.get(0).getBetType()).statType());
    }

    @Test
    void testCardsAsianTotal() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Yellow Cards Total (4.25)")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2201L).nameEn("Over").argument(4.25).factor(1.85).build(),
                        VaidebetStakeData.builder().id(2202L).nameEn("Under").argument(4.25).factor(1.95).build()
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
    void testIndividualCardsTotal() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Yellow Cards Team 1 Total")
                .nameRu("Индивидуальный тотал ЖК 1-й команды")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2301L).nameRu("Больше 2.5").argument(2.5).factor(1.80).build(),
                        VaidebetStakeData.builder().id(2302L).nameRu("Меньше 2.5").argument(2.5).factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetSubject.TEAM1, over.subject());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(2.5, over.param());
        assertEquals(StatType.YELLOW_CARDS, over.statType());

        // By club name in group
        VaidebetStakeGroupData groupT2 = VaidebetStakeGroupData.builder()
                .nameEn("Palmeiras Total de Cartões")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2303L).nameEn("Mais de 1.5").argument(1.5).factor(1.70).build()
                ))
                .build();

        List<OddItem> itemsT2 = new ArrayList<>();
        handler.handle(groupT2, match, SportType.FOOTBALL, itemsT2);
        assertEquals(1, itemsT2.size());
        assertEquals(BetSubject.TEAM2, ((TotalBet) itemsT2.get(0).getBetType()).subject());
    }

    @Test
    void testCardsHandicapByIdAndName() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .id(189L)
                .nameEn("Yellow Cards Handicap")
                .nameRu("Фора по желтым карточкам")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(1891L).nameEn("Handicap 1 (-1.5)").argument(1.5).factor(2.05).build(),
                        VaidebetStakeData.builder().id(1892L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.75).build()
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
        assertEquals(StatType.YELLOW_CARDS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param());
        assertFalse(h2.isAsian());
        assertEquals(StatType.YELLOW_CARDS, h2.statType());
    }

    @Test
    void testCardsHandicapPortuguesePtBr() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Handicap de Cartões Amarelos")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2401L).nameEn("Mandante (-0.5)").argument(-0.5).factor(1.90).build(),
                        VaidebetStakeData.builder().id(2402L).nameEn("Visitante (+0.5)").argument(0.5).factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) items.get(0).getBetType()).outcome());
        assertEquals(-0.5, ((HandicapBet) items.get(0).getBetType()).param());
        assertEquals(HandicapBet.Outcome.TEAM2, ((HandicapBet) items.get(1).getBetType()).outcome());
        assertEquals(0.5, ((HandicapBet) items.get(1).getBetType()).param());
        assertEquals(StatType.YELLOW_CARDS, ((HandicapBet) items.get(0).getBetType()).statType());
    }

    @Test
    void testCardsHalf1AndHalf2() {
        VaidebetStakeGroupData half1Group = VaidebetStakeGroupData.builder()
                .nameEn("1st Half Yellow Cards Total")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2501L).nameEn("Over").argument(1.5).factor(1.80).build(),
                        VaidebetStakeData.builder().id(2502L).nameEn("Under").argument(1.5).factor(2.00).build()
                ))
                .build();

        assertTrue(handler.supports(half1Group, SportType.FOOTBALL));

        List<OddItem> items1 = new ArrayList<>();
        handler.handle(half1Group, match, SportType.FOOTBALL, items1);

        assertEquals(2, items1.size());
        assertEquals(BetScope.HALF_1, ((TotalBet) items1.get(0).getBetType()).scope());
        assertEquals(StatType.YELLOW_CARDS, ((TotalBet) items1.get(0).getBetType()).statType());

        VaidebetStakeGroupData half2Group = VaidebetStakeGroupData.builder()
                .nameRu("2-й тайм ЖК Фора")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2503L).nameRu("Ф1 (0)").argument(0.0).factor(1.85).build(),
                        VaidebetStakeData.builder().id(2504L).nameRu("Ф2 (0)").argument(0.0).factor(1.95).build()
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
    void testRussianBookingsTerms() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameRu("Тотал предупреждений")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2601L).nameRu("Больше 3.5").argument(3.5).factor(1.85).build(),
                        VaidebetStakeData.builder().id(2602L).nameRu("Меньше 3.5").argument(3.5).factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) items.get(0).getBetType()).direction());
        assertEquals(3.5, ((TotalBet) items.get(0).getBetType()).param());
        assertEquals(StatType.YELLOW_CARDS, ((TotalBet) items.get(0).getBetType()).statType());
    }

    @Test
    void testSupportsExclusions() {
        // Corners
        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .nameEn("Corners Total")
                .stakes(List.of(VaidebetStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Red card / Expulsão
        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .nameEn("Red Cards Total")
                .stakes(List.of(VaidebetStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .nameEn("Cartão Vermelho")
                .stakes(List.of(VaidebetStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Esports Map
        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .nameEn("Map 1 Total")
                .stakes(List.of(VaidebetStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Pure Match Result
        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .nameEn("Match Result")
                .stakes(List.of(VaidebetStakeData.builder().factor(1.8).build())).build(), SportType.FOOTBALL));

        // Empty stakes
        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .stakes(List.of()).build(), SportType.FOOTBALL));

        // Null group
        assertFalse(handler.supports((VaidebetStakeGroupData) null, SportType.FOOTBALL));

        // Non-football sport type
        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .stakes(List.of(VaidebetStakeData.builder().factor(1.8).build())).build(), SportType.BASKETBALL));
    }

    @Test
    void testInvalidOddsFiltering() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(2701L).nameEn("Over").argument(4.5).factor(1.0).build(), // <= 1.0
                        VaidebetStakeData.builder().id(2702L).nameEn("Under").argument(4.5).factor(null).build(), // null factor
                        VaidebetStakeData.builder().id(2703L).nameEn("Over").argument(4.5).factor(1.90).build() // valid
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(1, items.size());
        assertEquals("2703", items.get(0).getFactorId());
        assertEquals(1.90, items.get(0).getValue());
    }
}
