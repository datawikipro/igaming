package pro.datawiki.igaming.source.apuestatotal.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApuestatotalStatsCornersAndCardsHandlerTest {

    private ApuestatotalStatsCornersHandler cornersHandler;
    private ApuestatotalStatsCardsHandler cardsHandler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        cornersHandler = new ApuestatotalStatsCornersHandler();
        cardsHandler = new ApuestatotalStatsCardsHandler();

        match = new MatchCache();
        match.setId(200L);
        match.setTeam1("Sporting Cristal");
        match.setTeam2("Melgar");
        match.setSportName("Football");
    }

    // ==================== CORNERS: TOTALS ====================

    @Test
    void testCornersTotalOverUnder() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(166L)
                .nameEn("Total Corners")
                .nameRu("Тотал угловых")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1101L).nameEn("Over").argument(9.5).factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(1102L).nameEn("Under").argument(9.5).factor(1.95).build()
                ))
                .build();

        assertTrue(cornersHandler.supports(group, SportType.FOOTBALL));
        assertFalse(cardsHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        OddItem overItem = items.get(0);
        assertEquals("1101", overItem.getFactorId());
        assertEquals(1.85, overItem.getValue());
        assertTrue(overItem.getBetType() instanceof TotalBet);
        TotalBet overBet = (TotalBet) overItem.getBetType();
        assertEquals(BetScope.FULL_MATCH, overBet.scope());
        assertEquals(BetSubject.MATCH, overBet.subject());
        assertEquals(TotalBet.Direction.OVER, overBet.direction());
        assertEquals(9.5, overBet.param());
        assertFalse(overBet.isAsian());
        assertEquals(StatType.CORNERS, overBet.statType());

        OddItem underItem = items.get(1);
        TotalBet underBet = (TotalBet) underItem.getBetType();
        assertEquals(TotalBet.Direction.UNDER, underBet.direction());
        assertEquals(9.5, underBet.param());
        assertEquals(StatType.CORNERS, underBet.statType());
    }

    @Test
    void testCornersTotalSpanishMasMenosAndParsedParam() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Córners Más / Menos 10.5")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1103L).nameEn("Más de 10.5").factor(2.10).build(),
                        ApuestatotalStakeData.builder().id(1104L).nameEn("Menos de 10.5").factor(1.70).build()
                ))
                .build();

        assertTrue(cornersHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(10.5, over.param());
        assertEquals(StatType.CORNERS, over.statType());

        TotalBet under = (TotalBet) items.get(1).getBetType();
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(10.5, under.param());
        assertEquals(StatType.CORNERS, under.statType());
    }

    @Test
    void testCornersTotalAsianQuarterLine() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(166L)
                .nameEn("Asian Total Corners")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1105L).nameEn("Over").argument(9.75).factor(1.90).build(),
                        ApuestatotalStakeData.builder().id(1106L).nameEn("Under").argument(9.75).factor(1.90).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertTrue(over.isAsian());
        assertEquals(9.75, over.param());
        assertEquals(StatType.CORNERS, over.statType());
    }

    @Test
    void testCornersTotalFirstHalf() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("1st Half - Total Corners")
                .nameRu("1-й тайм тотал угловых")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1107L).nameEn("Over").argument(4.5).factor(1.92).build(),
                        ApuestatotalStakeData.builder().id(1108L).nameEn("Under").argument(4.5).factor(1.88).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, over.scope());
        assertEquals(4.5, over.param());
        assertEquals(StatType.CORNERS, over.statType());
    }

    @Test
    void testCornersIndividualTotals() {
        ApuestatotalStakeGroupData groupT1 = ApuestatotalStakeGroupData.builder()
                .nameEn("Sporting Cristal - Total córners")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1109L).nameEn("Más de 5.5").argument(5.5).factor(1.80).build(),
                        ApuestatotalStakeData.builder().id(1110L).nameEn("Menos de 5.5").argument(5.5).factor(1.95).build()
                ))
                .build();

        List<OddItem> itemsT1 = new ArrayList<>();
        cornersHandler.handle(groupT1, match, SportType.FOOTBALL, itemsT1);

        assertEquals(2, itemsT1.size());
        TotalBet betT1 = (TotalBet) itemsT1.get(0).getBetType();
        assertEquals(BetSubject.TEAM1, betT1.subject());
        assertEquals(5.5, betT1.param());
        assertEquals(StatType.CORNERS, betT1.statType());

        ApuestatotalStakeGroupData groupT2 = ApuestatotalStakeGroupData.builder()
                .nameEn("Melgar - Total córners")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1111L).nameEn("Más de 4.5").argument(4.5).factor(2.05).build()
                ))
                .build();

        List<OddItem> itemsT2 = new ArrayList<>();
        cornersHandler.handle(groupT2, match, SportType.FOOTBALL, itemsT2);

        assertEquals(1, itemsT2.size());
        TotalBet betT2 = (TotalBet) itemsT2.get(0).getBetType();
        assertEquals(BetSubject.TEAM2, betT2.subject());
        assertEquals(4.5, betT2.param());
        assertEquals(StatType.CORNERS, betT2.statType());
    }

    // ==================== CORNERS: HANDICAP ====================

    @Test
    void testCornersHandicap2WayFullMatch() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(167L)
                .nameEn("Corners Handicap")
                .nameRu("Фора по угловым")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1201L).nameEn("Handicap 1 (-1.5)").argument(-1.5).factor(1.90).build(),
                        ApuestatotalStakeData.builder().id(1202L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.90).build()
                ))
                .build();

        assertTrue(cornersHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        OddItem h1Item = items.get(0);
        assertTrue(h1Item.getBetType() instanceof HandicapBet);
        HandicapBet h1 = (HandicapBet) h1Item.getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-1.5, h1.param());
        assertFalse(h1.isAsian());
        assertEquals(StatType.CORNERS, h1.statType());

        OddItem h2Item = items.get(1);
        HandicapBet h2 = (HandicapBet) h2Item.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param());
        assertEquals(StatType.CORNERS, h2.statType());
    }

    @Test
    void testCornersHandicapSpanishDesventajaAndTeamNames() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Hándicap de córners")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1203L).nameEn("Sporting Cristal (-2.0)").argument(-2.0).factor(2.15).build(),
                        ApuestatotalStakeData.builder().id(1204L).nameEn("Melgar (+2.0)").argument(2.0).factor(1.68).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-2.0, h1.param());
        assertEquals(StatType.CORNERS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(2.0, h2.param());
        assertEquals(StatType.CORNERS, h2.statType());
    }

    @Test
    void testCornersHandicap3WayEuropeanWithDraw() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Hándicap europeo de córners (-2)")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1205L).nameEn("Handicap 1").argument(-2.0).factor(2.80).build(),
                        ApuestatotalStakeData.builder().id(1206L).nameEn("Empate").argument(-2.0).factor(4.00).build(),
                        ApuestatotalStakeData.builder().id(1207L).nameEn("Handicap 2").argument(2.0).factor(2.00).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) items.get(0).getBetType()).outcome());
        assertEquals(HandicapBet.Outcome.DRAW, ((HandicapBet) items.get(1).getBetType()).outcome());
        assertEquals(HandicapBet.Outcome.TEAM2, ((HandicapBet) items.get(2).getBetType()).outcome());
        assertEquals(StatType.CORNERS, ((HandicapBet) items.get(0).getBetType()).statType());
    }

    @Test
    void testCornersHandicapFirstHalf() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("1st Half - Corners Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1208L).nameEn("H1 (-0.5)").argument(-0.5).factor(2.00).build(),
                        ApuestatotalStakeData.builder().id(1209L).nameEn("H2 (+0.5)").argument(0.5).factor(1.80).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(StatType.CORNERS, h1.statType());
    }

    // ==================== CORNERS: 1X2 & DOUBLE CHANCE ====================

    @Test
    void testCornersResult3Way1X2() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(168L)
                .nameEn("Corners 1X2")
                .nameRu("Угловые 1X2")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1301L).nameEn("1").factor(1.65).build(),
                        ApuestatotalStakeData.builder().id(1302L).nameEn("X").factor(7.50).build(),
                        ApuestatotalStakeData.builder().id(1303L).nameEn("2").factor(2.80).build()
                ))
                .build();

        assertTrue(cornersHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        MatchResultBet w1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, w1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, w1.outcome());
        assertEquals(StatType.CORNERS, w1.statType());

        MatchResultBet x = (MatchResultBet) items.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, x.outcome());
        assertEquals(StatType.CORNERS, x.statType());

        MatchResultBet w2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2, w2.outcome());
        assertEquals(StatType.CORNERS, w2.statType());
    }

    @Test
    void testCornersResult2WayDnb() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Most Corners (Draw No Bet)")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1304L).nameEn("Sporting Cristal").factor(1.45).build(),
                        ApuestatotalStakeData.builder().id(1305L).nameEn("Melgar").factor(2.55).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        MatchResultBet w1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, w1.outcome());
        assertEquals(StatType.CORNERS, w1.statType());

        MatchResultBet w2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, w2.outcome());
        assertEquals(StatType.CORNERS, w2.statType());
    }

    @Test
    void testCornersDoubleChance() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Doble oportunidad de córners")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1306L).nameEn("1X").factor(1.25).build(),
                        ApuestatotalStakeData.builder().id(1307L).nameEn("12").factor(1.15).build(),
                        ApuestatotalStakeData.builder().id(1308L).nameEn("X2").factor(1.95).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) items.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) items.get(1).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) items.get(2).getBetType()).outcome());
        assertEquals(StatType.CORNERS, ((MatchResultBet) items.get(0).getBetType()).statType());
    }

    @Test
    void testCornersResultFirstHalf() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("1er tiempo - Córners 1X2")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1309L).nameEn("Local").factor(1.90).build(),
                        ApuestatotalStakeData.builder().id(1310L).nameEn("Empate").factor(3.80).build(),
                        ApuestatotalStakeData.builder().id(1311L).nameEn("Visitante").factor(3.40).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        MatchResultBet w1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, w1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, w1.outcome());
        assertEquals(StatType.CORNERS, w1.statType());
    }

    // ==================== YELLOW CARDS: TOTALS ====================

    @Test
    void testYellowCardsTotalOverUnder() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .nameRu("Тотал желтых карточек")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1401L).nameEn("Over").argument(4.5).factor(1.88).build(),
                        ApuestatotalStakeData.builder().id(1402L).nameEn("Under").argument(4.5).factor(1.92).build()
                ))
                .build();

        assertTrue(cardsHandler.supports(group, SportType.FOOTBALL));
        assertFalse(cornersHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        OddItem overItem = items.get(0);
        assertEquals("1401", overItem.getFactorId());
        assertEquals(1.88, overItem.getValue());
        assertTrue(overItem.getBetType() instanceof TotalBet);
        TotalBet overBet = (TotalBet) overItem.getBetType();
        assertEquals(BetScope.FULL_MATCH, overBet.scope());
        assertEquals(BetSubject.MATCH, overBet.subject());
        assertEquals(TotalBet.Direction.OVER, overBet.direction());
        assertEquals(4.5, overBet.param());
        assertFalse(overBet.isAsian());
        assertEquals(StatType.YELLOW_CARDS, overBet.statType());

        OddItem underItem = items.get(1);
        TotalBet underBet = (TotalBet) underItem.getBetType();
        assertEquals(TotalBet.Direction.UNDER, underBet.direction());
        assertEquals(4.5, underBet.param());
        assertEquals(StatType.YELLOW_CARDS, underBet.statType());
    }

    @Test
    void testYellowCardsTotalSpanishTarjetasAmarillas() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Tarjetas amarillas Más/Menos")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1403L).nameEn("Más de 3.5").argument(3.5).factor(1.65).build(),
                        ApuestatotalStakeData.builder().id(1404L).nameEn("Menos de 3.5").argument(3.5).factor(2.15).build()
                ))
                .build();

        assertTrue(cardsHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(3.5, over.param());
        assertEquals(StatType.YELLOW_CARDS, over.statType());
    }

    @Test
    void testYellowCardsTotalFirstHalf() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("1st Half - Yellow Cards Total")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1405L).nameEn("Over").argument(1.5).factor(1.95).build(),
                        ApuestatotalStakeData.builder().id(1406L).nameEn("Under").argument(1.5).factor(1.80).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, over.scope());
        assertEquals(1.5, over.param());
        assertEquals(StatType.YELLOW_CARDS, over.statType());
    }

    @Test
    void testYellowCardsIndividualTotal() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Melgar - Total tarjetas amarillas")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1407L).nameEn("Más de 2.5").argument(2.5).factor(1.85).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(1, items.size());
        TotalBet bet = (TotalBet) items.get(0).getBetType();
        assertEquals(BetSubject.TEAM2, bet.subject());
        assertEquals(2.5, bet.param());
        assertEquals(StatType.YELLOW_CARDS, bet.statType());
    }

    // ==================== YELLOW CARDS: HANDICAP ====================

    @Test
    void testYellowCardsHandicap2Way() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(189L)
                .nameEn("Yellow Cards Handicap")
                .nameRu("Фора по желтым карточкам")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1501L).nameEn("H1").argument(-0.5).factor(1.95).build(),
                        ApuestatotalStakeData.builder().id(1502L).nameEn("H2").argument(0.5).factor(1.85).build()
                ))
                .build();

        assertTrue(cardsHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-0.5, h1.param());
        assertEquals(StatType.YELLOW_CARDS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(0.5, h2.param());
        assertEquals(StatType.YELLOW_CARDS, h2.statType());
    }

    @Test
    void testYellowCardsHandicapFirstHalf() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("1er tiempo - Hándicap de tarjetas amarillas")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1503L).nameEn("H1").argument(0.0).factor(1.75).build(),
                        ApuestatotalStakeData.builder().id(1504L).nameEn("H2").argument(0.0).factor(2.05).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(StatType.YELLOW_CARDS, h1.statType());
    }

    // ==================== YELLOW CARDS: 1X2 & DOUBLE CHANCE ====================

    @Test
    void testYellowCardsResult3Way1X2() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(187L)
                .nameEn("Yellow Cards 1X2")
                .nameRu("ЖК 1X2")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1601L).nameEn("1").factor(2.10).build(),
                        ApuestatotalStakeData.builder().id(1602L).nameEn("X").factor(4.20).build(),
                        ApuestatotalStakeData.builder().id(1603L).nameEn("2").factor(2.60).build()
                ))
                .build();

        assertTrue(cardsHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) items.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) items.get(1).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) items.get(2).getBetType()).outcome());
        assertEquals(StatType.YELLOW_CARDS, ((MatchResultBet) items.get(0).getBetType()).statType());
    }

    @Test
    void testYellowCardsDoubleChance() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Tarjetas amarillas Doble Oportunidad")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1604L).nameEn("1X").factor(1.40).build(),
                        ApuestatotalStakeData.builder().id(1605L).nameEn("12").factor(1.22).build(),
                        ApuestatotalStakeData.builder().id(1606L).nameEn("X2").factor(1.65).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) items.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) items.get(1).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) items.get(2).getBetType()).outcome());
        assertEquals(StatType.YELLOW_CARDS, ((MatchResultBet) items.get(0).getBetType()).statType());
    }

    // ==================== EXCLUSIONS & EDGE CASES ====================

    @Test
    void testCornersExclusions() {
        // Red cards should not be supported
        ApuestatotalStakeGroupData redCards = ApuestatotalStakeGroupData.builder()
                .nameEn("Red card shown")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("Yes").factor(3.5).build()))
                .build();
        assertFalse(cornersHandler.supports(redCards, SportType.FOOTBALL));

        // Yellow cards should not be supported
        ApuestatotalStakeGroupData yellowCards = ApuestatotalStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("Over").argument(3.5).factor(1.8).build()))
                .build();
        assertFalse(cornersHandler.supports(yellowCards, SportType.FOOTBALL));

        // Esports map markets should not be supported
        ApuestatotalStakeGroupData esportsMap = ApuestatotalStakeGroupData.builder()
                .nameEn("Map 1 Total Rounds")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("Over").argument(22.5).factor(1.9).build()))
                .build();
        assertFalse(cornersHandler.supports(esportsMap, SportType.FOOTBALL));
    }

    @Test
    void testYellowCardsExclusions() {
        // Corners should not be supported
        ApuestatotalStakeGroupData corners = ApuestatotalStakeGroupData.builder()
                .id(166L)
                .nameEn("Corners Total")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("Over").argument(9.5).factor(1.9).build()))
                .build();
        assertFalse(cardsHandler.supports(corners, SportType.FOOTBALL));

        // Red cards should not be supported
        ApuestatotalStakeGroupData redCard = ApuestatotalStakeGroupData.builder()
                .nameEn("Tarjeta roja en el partido")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("Si").factor(3.2).build()))
                .build();
        assertFalse(cardsHandler.supports(redCard, SportType.FOOTBALL));
    }

    @Test
    void testSportTypeFilteringNonFootballRejected() {
        ApuestatotalStakeGroupData cornerGroup = ApuestatotalStakeGroupData.builder()
                .id(166L)
                .nameEn("Total Corners")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("Over").argument(9.5).factor(1.85).build()))
                .build();

        assertFalse(cornersHandler.supports(cornerGroup, SportType.TENNIS));
        assertFalse(cornersHandler.supports(cornerGroup, SportType.BASKETBALL));
        assertFalse(cornersHandler.supports(cornerGroup, SportType.CS2));
        assertTrue(cornersHandler.supports(cornerGroup, SportType.FOOTBALL));
        assertTrue(cornersHandler.supports(cornerGroup, null)); // null sport defaults to allowing name matching

        ApuestatotalStakeGroupData cardGroup = ApuestatotalStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("Over").argument(4.5).factor(1.88).build()))
                .build();

        assertFalse(cardsHandler.supports(cardGroup, SportType.TENNIS));
        assertFalse(cardsHandler.supports(cardGroup, SportType.BASKETBALL));
        assertFalse(cardsHandler.supports(cardGroup, SportType.DOTA2));
        assertTrue(cardsHandler.supports(cardGroup, SportType.FOOTBALL));
        assertTrue(cardsHandler.supports(cardGroup, null));
    }

    @Test
    void testNullAndInvalidInputs() {
        assertFalse(cornersHandler.supports((ApuestatotalStakeGroupData) null, SportType.FOOTBALL));
        assertFalse(cardsHandler.supports((ApuestatotalStakeGroupData) null, SportType.FOOTBALL));

        ApuestatotalStakeGroupData emptyGroup = ApuestatotalStakeGroupData.builder()
                .stakes(List.of())
                .build();
        assertFalse(cornersHandler.supports(emptyGroup, SportType.FOOTBALL));
        assertFalse(cardsHandler.supports(emptyGroup, SportType.FOOTBALL));

        // Invalid odds (<= 1.0 or null)
        ApuestatotalStakeGroupData invalidGroup = ApuestatotalStakeGroupData.builder()
                .id(166L)
                .nameEn("Total Corners")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().nameEn("Over").argument(9.5).factor(1.0).build(),
                        ApuestatotalStakeData.builder().nameEn("Under").argument(9.5).factor(null).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(invalidGroup, match, SportType.FOOTBALL, items);
        assertTrue(items.isEmpty());
    }
}
