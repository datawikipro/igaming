package pro.datawiki.igaming.source.apuestatotal.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApuestatotalTotalAndHandicapHandlerTest {

    private ApuestatotalTotalHandler totalHandler;
    private ApuestatotalHandicapHandler handicapHandler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        totalHandler = new ApuestatotalTotalHandler();
        handicapHandler = new ApuestatotalHandicapHandler();

        match = new MatchCache();
        match.setId(100L);
        match.setTeam1("Alianza Lima");
        match.setTeam2("Universitario");
        match.setSportName("Football");
    }

    @Test
    void testTotalFullMatchOverUnder() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(3L)
                .nameEn("Total Goals")
                .nameRu("Тотал матча")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(101L).nameEn("Over").nameRu("Больше").argument(2.5).factor(1.90).build(),
                        ApuestatotalStakeData.builder().id(102L).nameEn("Under").nameRu("Меньше").argument(2.5).factor(1.95).build()
                ))
                .build();

        assertTrue(totalHandler.supports(group, SportType.FOOTBALL));
        assertFalse(handicapHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        totalHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        OddItem overItem = items.get(0);
        assertEquals("101", overItem.getFactorId());
        assertEquals(1.90, overItem.getValue());
        assertTrue(overItem.getBetType() instanceof TotalBet);
        TotalBet overBet = (TotalBet) overItem.getBetType();
        assertEquals(BetScope.FULL_MATCH, overBet.scope());
        assertEquals(BetSubject.MATCH, overBet.subject());
        assertEquals(TotalBet.Direction.OVER, overBet.direction());
        assertEquals(2.5, overBet.param());
        assertFalse(overBet.isAsian());
        assertEquals(StatType.MATCH, overBet.statType());

        OddItem underItem = items.get(1);
        TotalBet underBet = (TotalBet) underItem.getBetType();
        assertEquals(TotalBet.Direction.UNDER, underBet.direction());
        assertEquals(2.5, underBet.param());
    }

    @Test
    void testTotalSpanishMasMenos() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Más/Menos 2.5 goles")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(103L).nameEn("Más de 2.5").factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(104L).nameEn("Menos de 2.5").factor(1.95).build()
                ))
                .build();

        assertTrue(totalHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        totalHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) items.get(0).getBetType()).direction());
        assertEquals(2.5, ((TotalBet) items.get(0).getBetType()).param());
        assertEquals(TotalBet.Direction.UNDER, ((TotalBet) items.get(1).getBetType()).direction());
        assertEquals(2.5, ((TotalBet) items.get(1).getBetType()).param());
    }

    @Test
    void testTotalHalf1AndHalf2() {
        ApuestatotalStakeGroupData groupH1 = ApuestatotalStakeGroupData.builder()
                .id(6L)
                .nameEn("1st Half - Total")
                .nameRu("1-й тайм тотал")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(201L).nameEn("Over").argument(1.5).factor(2.10).build(),
                        ApuestatotalStakeData.builder().id(202L).nameEn("Under").argument(1.5).factor(1.75).build()
                ))
                .build();

        assertTrue(totalHandler.supports(groupH1, SportType.FOOTBALL));

        List<OddItem> itemsH1 = new ArrayList<>();
        totalHandler.handle(groupH1, match, SportType.FOOTBALL, itemsH1);

        assertEquals(2, itemsH1.size());
        TotalBet betH1 = (TotalBet) itemsH1.get(0).getBetType();
        assertEquals(BetScope.HALF_1, betH1.scope());
        assertEquals(BetSubject.MATCH, betH1.subject());
        assertEquals(TotalBet.Direction.OVER, betH1.direction());
        assertEquals(1.5, betH1.param());

        ApuestatotalStakeGroupData groupH2 = ApuestatotalStakeGroupData.builder()
                .id(9L)
                .nameEn("2nd Half - Total")
                .nameRu("2-й тайм тотал")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(203L).nameEn("Over").argument(1.5).factor(1.95).build(),
                        ApuestatotalStakeData.builder().id(204L).nameEn("Under").argument(1.5).factor(1.85).build()
                ))
                .build();

        assertTrue(totalHandler.supports(groupH2, SportType.FOOTBALL));

        List<OddItem> itemsH2 = new ArrayList<>();
        totalHandler.handle(groupH2, match, SportType.FOOTBALL, itemsH2);

        assertEquals(2, itemsH2.size());
        TotalBet betH2 = (TotalBet) itemsH2.get(0).getBetType();
        assertEquals(BetScope.HALF_2, betH2.scope());
    }

    @Test
    void testIndividualTotals() {
        ApuestatotalStakeGroupData groupT1 = ApuestatotalStakeGroupData.builder()
                .nameEn("Total de goles de Alianza Lima")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(301L).nameEn("Más de 1.5").argument(1.5).factor(1.80).build(),
                        ApuestatotalStakeData.builder().id(302L).nameEn("Menos de 1.5").argument(1.5).factor(2.00).build()
                ))
                .build();

        List<OddItem> itemsT1 = new ArrayList<>();
        totalHandler.handle(groupT1, match, SportType.FOOTBALL, itemsT1);

        assertEquals(2, itemsT1.size());
        TotalBet betT1 = (TotalBet) itemsT1.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, betT1.scope());
        assertEquals(BetSubject.TEAM1, betT1.subject());
        assertEquals(TotalBet.Direction.OVER, betT1.direction());

        ApuestatotalStakeGroupData groupT2 = ApuestatotalStakeGroupData.builder()
                .nameEn("Universitario Total")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(303L).nameEn("Over").argument(1.0).factor(1.65).build()
                ))
                .build();

        List<OddItem> itemsT2 = new ArrayList<>();
        totalHandler.handle(groupT2, match, SportType.FOOTBALL, itemsT2);

        assertEquals(1, itemsT2.size());
        TotalBet betT2 = (TotalBet) itemsT2.get(0).getBetType();
        assertEquals(BetSubject.TEAM2, betT2.subject());
    }

    @Test
    void testAsianTotalQuarterLine() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(3L)
                .nameEn("Asian Total")
                .nameRu("Азиатский тотал")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(401L).nameEn("Over").argument(2.25).factor(1.92).build(),
                        ApuestatotalStakeData.builder().id(402L).nameEn("Under").argument(2.75).factor(1.88).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        totalHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet bet1 = (TotalBet) items.get(0).getBetType();
        assertTrue(bet1.isAsian());
        assertEquals(2.25, bet1.param());

        TotalBet bet2 = (TotalBet) items.get(1).getBetType();
        assertTrue(bet2.isAsian());
        assertEquals(2.75, bet2.param());
    }

    @Test
    void testTotalArgumentParsedFromNameWhenNull() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(3L)
                .nameEn("Total")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(501L).nameEn("Over (3.5)").factor(2.40).build(),
                        ApuestatotalStakeData.builder().id(502L).nameRu("Меньше 3.5").factor(1.58).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        totalHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertEquals(3.5, ((TotalBet) items.get(0).getBetType()).param());
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) items.get(0).getBetType()).direction());
        assertEquals(3.5, ((TotalBet) items.get(1).getBetType()).param());
        assertEquals(TotalBet.Direction.UNDER, ((TotalBet) items.get(1).getBetType()).direction());
    }

    @Test
    void testHandicapFullMatch2Way() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(2L)
                .nameEn("Handicap")
                .nameRu("Фора")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(601L).nameEn("Handicap 1").nameRu("Ф1 (-1.5)").argument(-1.5).factor(2.05).build(),
                        ApuestatotalStakeData.builder().id(602L).nameEn("Handicap 2").nameRu("Ф2 (+1.5)").argument(1.5).factor(1.80).build()
                ))
                .build();

        assertTrue(handicapHandler.supports(group, SportType.FOOTBALL));
        assertFalse(totalHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handicapHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        OddItem h1Item = items.get(0);
        assertEquals("601", h1Item.getFactorId());
        assertEquals(2.05, h1Item.getValue());
        assertTrue(h1Item.getBetType() instanceof HandicapBet);
        HandicapBet h1Bet = (HandicapBet) h1Item.getBetType();
        assertEquals(BetScope.FULL_MATCH, h1Bet.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1Bet.outcome());
        assertEquals(-1.5, h1Bet.param());
        assertFalse(h1Bet.isAsian());
        assertEquals(StatType.MATCH, h1Bet.statType());

        OddItem h2Item = items.get(1);
        HandicapBet h2Bet = (HandicapBet) h2Item.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2Bet.outcome());
        assertEquals(1.5, h2Bet.param());
    }

    @Test
    void testHandicapSpanishDesventajaHandicap() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Hándicap asiático")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(603L).nameEn("Local (-0.75)").argument(-0.75).factor(1.95).build(),
                        ApuestatotalStakeData.builder().id(604L).nameEn("Visitante (+0.75)").argument(0.75).factor(1.85).build()
                ))
                .build();

        assertTrue(handicapHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handicapHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-0.75, h1.param());
        assertTrue(h1.isAsian());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(0.75, h2.param());
        assertTrue(h2.isAsian());
    }

    @Test
    void testHandicapHalf1AndHalf2() {
        ApuestatotalStakeGroupData groupH1 = ApuestatotalStakeGroupData.builder()
                .id(5L)
                .nameEn("1st Half - Handicap")
                .nameRu("1-й тайм фора")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(701L).nameEn("H1").argument(-0.5).factor(2.50).build(),
                        ApuestatotalStakeData.builder().id(702L).nameEn("H2").argument(0.5).factor(1.55).build()
                ))
                .build();

        assertTrue(handicapHandler.supports(groupH1, SportType.FOOTBALL));

        List<OddItem> itemsH1 = new ArrayList<>();
        handicapHandler.handle(groupH1, match, SportType.FOOTBALL, itemsH1);

        assertEquals(2, itemsH1.size());
        assertEquals(BetScope.HALF_1, ((HandicapBet) itemsH1.get(0).getBetType()).scope());
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) itemsH1.get(0).getBetType()).outcome());

        ApuestatotalStakeGroupData groupH2 = ApuestatotalStakeGroupData.builder()
                .id(8L)
                .nameEn("2nd Half - Handicap")
                .nameRu("2-й тайм фора")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(703L).nameEn("H1").argument(0.0).factor(1.80).build(),
                        ApuestatotalStakeData.builder().id(704L).nameEn("H2").argument(0.0).factor(2.00).build()
                ))
                .build();

        assertTrue(handicapHandler.supports(groupH2, SportType.FOOTBALL));

        List<OddItem> itemsH2 = new ArrayList<>();
        handicapHandler.handle(groupH2, match, SportType.FOOTBALL, itemsH2);

        assertEquals(2, itemsH2.size());
        assertEquals(BetScope.HALF_2, ((HandicapBet) itemsH2.get(0).getBetType()).scope());
    }

    @Test
    void testHandicap3WayEuropeanWithDraw() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(2L)
                .nameEn("European Handicap (-1)")
                .nameRu("Европейский гандикап (-1)")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(801L).nameEn("Handicap 1").argument(-1.0).factor(2.60).build(),
                        ApuestatotalStakeData.builder().id(802L).nameEn("Empate").argument(-1.0).factor(3.40).build(),
                        ApuestatotalStakeData.builder().id(803L).nameEn("Handicap 2").argument(1.0).factor(2.30).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handicapHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) items.get(0).getBetType()).outcome());
        assertEquals(HandicapBet.Outcome.DRAW, ((HandicapBet) items.get(1).getBetType()).outcome());
        assertEquals(HandicapBet.Outcome.TEAM2, ((HandicapBet) items.get(2).getBetType()).outcome());
    }

    @Test
    void testHandicapTeamNameMatchingAndNegativeSignCorrection() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(2L)
                .nameEn("Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(901L).nameEn("Alianza Lima (-1.5)").argument(1.5).factor(2.15).build(),
                        ApuestatotalStakeData.builder().id(902L).nameEn("Universitario (+1.5)").argument(1.5).factor(1.72).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handicapHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-1.5, h1.param());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param());
    }

    @Test
    void testAsianHandicapQuarterLine() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(2L)
                .nameEn("Asian Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(1001L).nameEn("H1").argument(-0.25).factor(1.95).build(),
                        ApuestatotalStakeData.builder().id(1002L).nameEn("H2").argument(0.25).factor(1.90).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handicapHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertTrue(((HandicapBet) items.get(0).getBetType()).isAsian());
        assertEquals(-0.25, ((HandicapBet) items.get(0).getBetType()).param());
        assertTrue(((HandicapBet) items.get(1).getBetType()).isAsian());
        assertEquals(0.25, ((HandicapBet) items.get(1).getBetType()).param());
    }

    @Test
    void testExclusionsAndInvalidOdds() {
        ApuestatotalStakeGroupData cornerGroup = ApuestatotalStakeGroupData.builder()
                .id(166L)
                .nameEn("Corners Total")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("Over").argument(9.5).factor(1.85).build()))
                .build();

        assertFalse(totalHandler.supports(cornerGroup, SportType.FOOTBALL));
        assertFalse(handicapHandler.supports(cornerGroup, SportType.FOOTBALL));

        ApuestatotalStakeGroupData cardGroup = ApuestatotalStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Handicap")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("H1").argument(-0.5).factor(1.95).build()))
                .build();

        assertFalse(totalHandler.supports(cardGroup, SportType.FOOTBALL));
        assertFalse(handicapHandler.supports(cardGroup, SportType.FOOTBALL));

        ApuestatotalStakeGroupData invalidTotalGroup = ApuestatotalStakeGroupData.builder()
                .id(3L)
                .nameEn("Total")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().nameEn("Over").argument(2.5).factor(1.0).build(),
                        ApuestatotalStakeData.builder().nameEn("Under").argument(2.5).factor(null).build(),
                        ApuestatotalStakeData.builder().nameEn("Over").argument(null).factor(1.8).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        totalHandler.handle(invalidTotalGroup, match, SportType.FOOTBALL, items);
        assertTrue(items.isEmpty());
    }
}
