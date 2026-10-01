package pro.datawiki.igaming.source.esportesdasorte.service.handler;

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
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeData;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeGroupData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EsportesdasorteStatsHandlerTest {

    private EsportesdasorteStatsCornersHandler cornersHandler;
    private EsportesdasorteStatsCardsHandler cardsHandler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        cornersHandler = new EsportesdasorteStatsCornersHandler();
        cardsHandler = new EsportesdasorteStatsCardsHandler();

        match = new MatchCache();
        match.setId(200L);
        match.setTeam1("Flamengo");
        match.setTeam2("Palmeiras");
        match.setSportName("Football");
    }

    // =========================================================================
    // CORNERS TESTS (EsportesdasorteStatsCornersHandler)
    // =========================================================================

    @Test
    void testCorners1X2FullMatch() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(168L)
                .nameEn("Corners - 1X2")
                .nameRu("Угловые - 1Х2")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(1001L).nameEn("1").factor(1.75).build(),
                        EsportesdasorteStakeData.builder().id(1002L).nameEn("X").factor(7.50).build(),
                        EsportesdasorteStakeData.builder().id(1003L).nameEn("2").factor(2.40).build()
                ))
                .build();

        assertTrue(cornersHandler.supports(group, SportType.FOOTBALL));
        assertFalse(cardsHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        OddItem w1 = items.get(0);
        assertEquals("1001", w1.getFactorId());
        assertEquals(1.75, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        MatchResultBet b1 = (MatchResultBet) w1.getBetType();
        assertEquals(BetScope.FULL_MATCH, b1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, b1.outcome());
        assertEquals(StatType.CORNERS, b1.statType());

        OddItem x = items.get(1);
        MatchResultBet bx = (MatchResultBet) x.getBetType();
        assertEquals(BetScope.FULL_MATCH, bx.scope());
        assertEquals(MatchResultBet.Outcome.DRAW, bx.outcome());
        assertEquals(StatType.CORNERS, bx.statType());

        OddItem w2 = items.get(2);
        MatchResultBet b2 = (MatchResultBet) w2.getBetType();
        assertEquals(BetScope.FULL_MATCH, b2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2, b2.outcome());
        assertEquals(StatType.CORNERS, b2.statType());
    }

    @Test
    void testCorners1X2FirstHalfPtBr() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("1º Tempo - Escanteios 1X2")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(1011L).nameEn("Mandante").factor(1.90).build(),
                        EsportesdasorteStakeData.builder().id(1012L).nameEn("Empate").factor(4.20).build(),
                        EsportesdasorteStakeData.builder().id(1013L).nameEn("Visitante").factor(2.80).build()
                ))
                .build();

        assertTrue(cornersHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        MatchResultBet b1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, b1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, b1.outcome());
        assertEquals(StatType.CORNERS, b1.statType());

        MatchResultBet bx = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.HALF_1, bx.scope());
        assertEquals(MatchResultBet.Outcome.DRAW, bx.outcome());

        MatchResultBet b2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(BetScope.HALF_1, b2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2, b2.outcome());
    }

    @Test
    void testCornersTotalOverUnderFullMatch() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(166L)
                .nameEn("Corners Total")
                .nameRu("Тотал угловых")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(1021L).nameEn("Over").nameRu("Больше").argument(9.5).factor(1.85).build(),
                        EsportesdasorteStakeData.builder().id(1022L).nameEn("Under").nameRu("Меньше").argument(9.5).factor(1.95).build()
                ))
                .build();

        assertTrue(cornersHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        OddItem over = items.get(0);
        assertEquals("1021", over.getFactorId());
        assertEquals(1.85, over.getValue());
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet overBet = (TotalBet) over.getBetType();
        assertEquals(BetScope.FULL_MATCH, overBet.scope());
        assertEquals(BetSubject.MATCH, overBet.subject());
        assertEquals(TotalBet.Direction.OVER, overBet.direction());
        assertEquals(9.5, overBet.param());
        assertFalse(overBet.isAsian());
        assertEquals(StatType.CORNERS, overBet.statType());

        OddItem under = items.get(1);
        TotalBet underBet = (TotalBet) under.getBetType();
        assertEquals(TotalBet.Direction.UNDER, underBet.direction());
        assertEquals(9.5, underBet.param());
        assertEquals(StatType.CORNERS, underBet.statType());
    }

    @Test
    void testCornersTotalPortuguesePtBrAcimaAbaixo() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Total de Escanteios Mais/Menos")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(1031L).nameEn("Acima de 10.5").factor(2.05).build(),
                        EsportesdasorteStakeData.builder().id(1032L).nameEn("Abaixo de 10.5").factor(1.75).build()
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
    }

    @Test
    void testCornersAsianQuarterTotal() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Total Asiático de Escanteios")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(1041L).nameEn("Mais").argument(9.75).factor(1.92).build(),
                        EsportesdasorteStakeData.builder().id(1042L).nameEn("Menos").argument(9.75).factor(1.88).build()
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
    void testCornersHandicapFullMatch() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(167L)
                .nameEn("Corner Handicap")
                .nameRu("Фора по угловым")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(1051L).nameEn("Handicap 1 (-1.5)").argument(-1.5).factor(2.10).build(),
                        EsportesdasorteStakeData.builder().id(1052L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.72).build()
                ))
                .build();

        assertTrue(cornersHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        OddItem h1 = items.get(0);
        assertEquals("1051", h1.getFactorId());
        assertEquals(2.10, h1.getValue());
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet h1Bet = (HandicapBet) h1.getBetType();
        assertEquals(BetScope.FULL_MATCH, h1Bet.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1Bet.outcome());
        assertEquals(-1.5, h1Bet.param());
        assertFalse(h1Bet.isAsian());
        assertEquals(StatType.CORNERS, h1Bet.statType());

        OddItem h2 = items.get(1);
        HandicapBet h2Bet = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2Bet.outcome());
        assertEquals(1.5, h2Bet.param());
        assertEquals(StatType.CORNERS, h2Bet.statType());
    }

    @Test
    void testCornersIndividualTeamTotals() {
        EsportesdasorteStakeGroupData groupT1 = EsportesdasorteStakeGroupData.builder()
                .nameEn("Total de Escanteios do Mandante")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(1061L).nameEn("Mais de 5.5").argument(5.5).factor(1.80).build(),
                        EsportesdasorteStakeData.builder().id(1062L).nameEn("Menos de 5.5").argument(5.5).factor(1.95).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(groupT1, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet bet = (TotalBet) items.get(0).getBetType();
        assertEquals(BetSubject.TEAM1, bet.subject());
        assertEquals(5.5, bet.param());
        assertEquals(StatType.CORNERS, bet.statType());
    }

    @Test
    void testCornersHalfTimeScopeAndParamParsingFromGroup() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("1st Half Total Corners 4.5")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(1071L).nameEn("Over").factor(1.90).build(),
                        EsportesdasorteStakeData.builder().id(1072L).nameEn("Under").factor(1.85).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet bet = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, bet.scope());
        assertEquals(4.5, bet.param());
        assertEquals(StatType.CORNERS, bet.statType());
    }

    // =========================================================================
    // YELLOW CARDS TESTS (EsportesdasorteStatsCardsHandler)
    // =========================================================================

    @Test
    void testCards1X2FullMatch() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(187L)
                .nameEn("Yellow Cards - 1X2")
                .nameRu("Желтые карточки - 1Х2")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(2001L).nameEn("1").factor(2.10).build(),
                        EsportesdasorteStakeData.builder().id(2002L).nameEn("X").factor(4.50).build(),
                        EsportesdasorteStakeData.builder().id(2003L).nameEn("2").factor(2.60).build()
                ))
                .build();

        assertTrue(cardsHandler.supports(group, SportType.FOOTBALL));
        assertFalse(cornersHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        OddItem w1 = items.get(0);
        assertEquals("2001", w1.getFactorId());
        assertEquals(2.10, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        MatchResultBet b1 = (MatchResultBet) w1.getBetType();
        assertEquals(BetScope.FULL_MATCH, b1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, b1.outcome());
        assertEquals(StatType.YELLOW_CARDS, b1.statType());

        OddItem x = items.get(1);
        MatchResultBet bx = (MatchResultBet) x.getBetType();
        assertEquals(BetScope.FULL_MATCH, bx.scope());
        assertEquals(MatchResultBet.Outcome.DRAW, bx.outcome());
        assertEquals(StatType.YELLOW_CARDS, bx.statType());

        OddItem w2 = items.get(2);
        MatchResultBet b2 = (MatchResultBet) w2.getBetType();
        assertEquals(BetScope.FULL_MATCH, b2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2, b2.outcome());
        assertEquals(StatType.YELLOW_CARDS, b2.statType());
    }

    @Test
    void testCardsTotalOverUnderFullMatch() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(188L)
                .nameEn("Yellow Cards Total")
                .nameRu("Тотал желтых карточек")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(2011L).nameEn("Over").nameRu("Больше").argument(4.5).factor(1.78).build(),
                        EsportesdasorteStakeData.builder().id(2012L).nameEn("Under").nameRu("Меньше").argument(4.5).factor(2.02).build()
                ))
                .build();

        assertTrue(cardsHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        OddItem over = items.get(0);
        assertEquals("2011", over.getFactorId());
        assertEquals(1.78, over.getValue());
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet overBet = (TotalBet) over.getBetType();
        assertEquals(BetScope.FULL_MATCH, overBet.scope());
        assertEquals(BetSubject.MATCH, overBet.subject());
        assertEquals(TotalBet.Direction.OVER, overBet.direction());
        assertEquals(4.5, overBet.param());
        assertFalse(overBet.isAsian());
        assertEquals(StatType.YELLOW_CARDS, overBet.statType());

        OddItem under = items.get(1);
        TotalBet underBet = (TotalBet) under.getBetType();
        assertEquals(TotalBet.Direction.UNDER, underBet.direction());
        assertEquals(4.5, underBet.param());
        assertEquals(StatType.YELLOW_CARDS, underBet.statType());
    }

    @Test
    void testCardsTotalPtBrCartoesAmarelos() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Total de Cartões Amarelos Mais/Menos")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(2021L).nameEn("Mais de 3.5").argument(3.5).factor(1.65).build(),
                        EsportesdasorteStakeData.builder().id(2022L).nameEn("Menos de 3.5").argument(3.5).factor(2.15).build()
                ))
                .build();

        assertTrue(cardsHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) items.get(0).getBetType()).direction());
        assertEquals(StatType.YELLOW_CARDS, ((TotalBet) items.get(0).getBetType()).statType());
        assertEquals(TotalBet.Direction.UNDER, ((TotalBet) items.get(1).getBetType()).direction());
        assertEquals(StatType.YELLOW_CARDS, ((TotalBet) items.get(1).getBetType()).statType());
    }

    @Test
    void testCardsHandicapFullMatch() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(189L)
                .nameEn("Yellow Cards Handicap")
                .nameRu("Фора по желтым карточкам")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(2031L).nameEn("H1").argument(-0.5).factor(1.95).build(),
                        EsportesdasorteStakeData.builder().id(2032L).nameEn("H2").argument(0.5).factor(1.85).build()
                ))
                .build();

        assertTrue(cardsHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        OddItem h1 = items.get(0);
        assertEquals("2031", h1.getFactorId());
        assertEquals(1.95, h1.getValue());
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet h1Bet = (HandicapBet) h1.getBetType();
        assertEquals(BetScope.FULL_MATCH, h1Bet.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1Bet.outcome());
        assertEquals(-0.5, h1Bet.param());
        assertEquals(StatType.YELLOW_CARDS, h1Bet.statType());

        OddItem h2 = items.get(1);
        HandicapBet h2Bet = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2Bet.outcome());
        assertEquals(0.5, h2Bet.param());
        assertEquals(StatType.YELLOW_CARDS, h2Bet.statType());
    }

    @Test
    void testCardsIndividualTeamTotals() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Total de Cartões da Equipe 2")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(2041L).nameEn("Mais de 1.5").argument(1.5).factor(1.70).build(),
                        EsportesdasorteStakeData.builder().id(2042L).nameEn("Menos de 1.5").argument(1.5).factor(2.05).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet bet = (TotalBet) items.get(0).getBetType();
        assertEquals(BetSubject.TEAM2, bet.subject());
        assertEquals(1.5, bet.param());
        assertEquals(StatType.YELLOW_CARDS, bet.statType());
    }

    @Test
    void testCardsFirstHalfScope() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("1º Tempo - Cartões Amarelos Total (1.5)")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(2051L).nameEn("Over").argument(1.5).factor(1.85).build(),
                        EsportesdasorteStakeData.builder().id(2052L).nameEn("Under").argument(1.5).factor(1.90).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        cardsHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        TotalBet bet = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, bet.scope());
        assertEquals(1.5, bet.param());
        assertEquals(StatType.YELLOW_CARDS, bet.statType());
    }

    @Test
    void testExclusionsAndNonFootballSports() {
        // Red card market should be excluded
        EsportesdasorteStakeGroupData redCardGroup = EsportesdasorteStakeGroupData.builder()
                .nameEn("Cartão Vermelho - Sim/Não")
                .stakes(List.of(EsportesdasorteStakeData.builder().nameEn("Sim").factor(4.5).build()))
                .build();
        assertFalse(cardsHandler.supports(redCardGroup, SportType.FOOTBALL));

        // Basketball should not be supported
        EsportesdasorteStakeGroupData basketballCards = EsportesdasorteStakeGroupData.builder()
                .nameEn("Cards")
                .stakes(List.of(EsportesdasorteStakeData.builder().nameEn("Over").argument(2.5).factor(1.8).build()))
                .build();
        assertFalse(cardsHandler.supports(basketballCards, SportType.BASKETBALL));
        assertFalse(cornersHandler.supports(basketballCards, SportType.BASKETBALL));

        // Corner market not supported by cards handler
        EsportesdasorteStakeGroupData cornerGroup = EsportesdasorteStakeGroupData.builder()
                .id(166L)
                .nameEn("Corners Total")
                .stakes(List.of(EsportesdasorteStakeData.builder().nameEn("Over").argument(9.5).factor(1.85).build()))
                .build();
        assertFalse(cardsHandler.supports(cornerGroup, SportType.FOOTBALL));

        // Invalid factors ignored
        EsportesdasorteStakeGroupData invalidOddsGroup = EsportesdasorteStakeGroupData.builder()
                .id(166L)
                .nameEn("Corners Total")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Over").argument(9.5).factor(1.0).build(),
                        EsportesdasorteStakeData.builder().nameEn("Under").argument(9.5).factor(null).build()
                ))
                .build();
        List<OddItem> items = new ArrayList<>();
        cornersHandler.handle(invalidOddsGroup, match, SportType.FOOTBALL, items);
        assertTrue(items.isEmpty());
    }
}
