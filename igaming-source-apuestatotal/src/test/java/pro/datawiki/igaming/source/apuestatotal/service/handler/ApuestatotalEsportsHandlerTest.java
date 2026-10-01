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

class ApuestatotalEsportsHandlerTest {

    private ApuestatotalEsportsHandler handler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        handler = new ApuestatotalEsportsHandler();

        match = new MatchCache();
        match.setId(300L);
        match.setTeam1("Natus Vincere");
        match.setTeam2("FaZe Clan");
        match.setSportName("CS2");
    }

    // ==================== 1. MAP WINNERS (BetScope.MAP_1..MAP_5, StatType.MATCH) ====================

    @Test
    void testMap1WinnerById703() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .nameRu("Победитель 1 карты")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7001L).nameEn("Natus Vincere").factor(1.75).build(),
                        ApuestatotalStakeData.builder().id(7002L).nameEn("FaZe Clan").factor(2.05).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());

        OddItem item1 = items.get(0);
        assertEquals("7001", item1.getFactorId());
        assertEquals(1.75, item1.getValue());
        assertTrue(item1.getBetType() instanceof MatchResultBet);
        MatchResultBet bet1 = (MatchResultBet) item1.getBetType();
        assertEquals(BetScope.MAP_1, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());
        assertEquals(StatType.MATCH, bet1.statType());

        OddItem item2 = items.get(1);
        MatchResultBet bet2 = (MatchResultBet) item2.getBetType();
        assertEquals(BetScope.MAP_1, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
        assertEquals(StatType.MATCH, bet2.statType());
    }

    @Test
    void testMap2WinnerById704() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(704L)
                .nameEn("Map 2 Winner")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7003L).nameEn("1").factor(1.90).build(),
                        ApuestatotalStakeData.builder().id(7004L).nameEn("2").factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.DOTA2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.DOTA2, items);

        assertEquals(2, items.size());
        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_2, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());

        MatchResultBet bet2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_2, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
    }

    @Test
    void testMap3WinnerById705() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(705L)
                .nameEn("Map 3 Winner")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7005L).nameEn("Home").factor(1.60).build(),
                        ApuestatotalStakeData.builder().id(7006L).nameEn("Away").factor(2.30).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.LEAGUE_OF_LEGENDS));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.LEAGUE_OF_LEGENDS, items);

        assertEquals(2, items.size());
        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_3, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());
    }

    @Test
    void testMap4AndMap5ByTextPatterns() {
        ApuestatotalStakeGroupData map4Group = ApuestatotalStakeGroupData.builder()
                .nameEn("Map 4 Winner")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7007L).nameEn("1").factor(2.10).build(),
                        ApuestatotalStakeData.builder().id(7008L).nameEn("2").factor(1.72).build()
                ))
                .build();

        assertTrue(handler.supports(map4Group, SportType.VALORANT));

        List<OddItem> items4 = new ArrayList<>();
        handler.handle(map4Group, match, SportType.VALORANT, items4);

        assertEquals(2, items4.size());
        assertEquals(BetScope.MAP_4, ((MatchResultBet) items4.get(0).getBetType()).scope());

        ApuestatotalStakeGroupData map5Group = ApuestatotalStakeGroupData.builder()
                .nameEn("5th Map Winner")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7009L).nameEn("1").factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(7010L).nameEn("2").factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(map5Group, SportType.VALORANT));

        List<OddItem> items5 = new ArrayList<>();
        handler.handle(map5Group, match, SportType.VALORANT, items5);

        assertEquals(2, items5.size());
        assertEquals(BetScope.MAP_5, ((MatchResultBet) items5.get(0).getBetType()).scope());
    }

    @Test
    void testMapWinnerSpanishAndRussianPatterns() {
        ApuestatotalStakeGroupData spanishGroup = ApuestatotalStakeGroupData.builder()
                .nameEn("Ganador del mapa 1")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7011L).nameEn("Local").factor(1.65).build(),
                        ApuestatotalStakeData.builder().id(7012L).nameEn("Visitante").factor(2.20).build()
                ))
                .build();

        assertTrue(handler.supports(spanishGroup, SportType.CS2));
        List<OddItem> itemsEs = new ArrayList<>();
        handler.handle(spanishGroup, match, SportType.CS2, itemsEs);

        assertEquals(2, itemsEs.size());
        assertEquals(BetScope.MAP_1, ((MatchResultBet) itemsEs.get(0).getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, ((MatchResultBet) itemsEs.get(0).getBetType()).outcome());

        ApuestatotalStakeGroupData russianGroup = ApuestatotalStakeGroupData.builder()
                .nameRu("2-я карта - Победитель")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7013L).nameRu("П1").factor(1.80).build(),
                        ApuestatotalStakeData.builder().id(7014L).nameRu("П2").factor(2.00).build()
                ))
                .build();

        assertTrue(handler.supports(russianGroup, SportType.CS2));
        List<OddItem> itemsRu = new ArrayList<>();
        handler.handle(russianGroup, match, SportType.CS2, itemsRu);

        assertEquals(2, itemsRu.size());
        assertEquals(BetScope.MAP_2, ((MatchResultBet) itemsRu.get(0).getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, ((MatchResultBet) itemsRu.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, ((MatchResultBet) itemsRu.get(1).getBetType()).outcome());
    }

    @Test
    void testMapWinner3WayWithDraw() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Map 1 - 1X2")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7015L).nameEn("1").factor(2.20).build(),
                        ApuestatotalStakeData.builder().id(7016L).nameEn("X").factor(9.00).build(),
                        ApuestatotalStakeData.builder().id(7017L).nameEn("2").factor(1.70).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(3, items.size());
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) items.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) items.get(1).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) items.get(2).getBetType()).outcome());
        assertEquals(BetScope.MAP_1, ((MatchResultBet) items.get(0).getBetType()).scope());
    }

    // ==================== 2. MAP TOTALS & MAP HANDICAPS (StatType.MAPS) ====================

    @Test
    void testMapTotalById742() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(742L)
                .nameEn("Total Maps")
                .nameRu("Тотал карт")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7101L).nameEn("Over").argument(2.5).factor(1.95).build(),
                        ApuestatotalStakeData.builder().id(7102L).nameEn("Under").argument(2.5).factor(1.85).build()
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
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(2.5, under.param());
        assertEquals(StatType.MAPS, under.statType());
    }

    @Test
    void testMapHandicapById740And741() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(740L)
                .nameEn("Map Handicap")
                .nameRu("Фора по картам")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7201L).nameEn("H1 (-1.5)").argument(-1.5).factor(2.50).build(),
                        ApuestatotalStakeData.builder().id(7202L).nameEn("H2 (+1.5)").argument(1.5).factor(1.50).build()
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
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param());
        assertEquals(StatType.MAPS, h2.statType());
    }

    @Test
    void testMapHandicapSpanishDesventajaAndAsianQuarterLine() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Hándicap de mapas")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7203L).nameEn("Natus Vincere (-1.25)").argument(-1.25).factor(2.20).build(),
                        ApuestatotalStakeData.builder().id(7204L).nameEn("FaZe Clan (+1.25)").argument(1.25).factor(1.65).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertTrue(h1.isAsian());
        assertEquals(-1.25, h1.param());
        assertEquals(StatType.MAPS, h1.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
    }

    // ==================== 3. ROUND TOTALS & ROUND HANDICAPS (StatType.ROUNDS) ====================

    @Test
    void testMap1RoundTotalOverUnder() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Map 1 - Total Rounds")
                .nameRu("Карта 1 тотал раундов")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7301L).nameEn("Over").argument(22.5).factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(7302L).nameEn("Under").argument(22.5).factor(1.95).build()
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
    void testMap2RoundHandicap() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Map 2 - Round Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7303L).nameEn("H1 (-3.5)").argument(-3.5).factor(1.90).build(),
                        ApuestatotalStakeData.builder().id(7304L).nameEn("H2 (+3.5)").argument(3.5).factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());

        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_2, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-3.5, h1.param());
        assertFalse(h1.isAsian());
        assertEquals(StatType.ROUNDS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_2, h2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(3.5, h2.param());
        assertEquals(StatType.ROUNDS, h2.statType());
    }

    @Test
    void testSpanishRoundTotalAndHandicap() {
        ApuestatotalStakeGroupData totalGroup = ApuestatotalStakeGroupData.builder()
                .nameEn("Total de rondas - Mapa 1")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7305L).nameEn("Más de 21.5").argument(21.5).factor(1.80).build(),
                        ApuestatotalStakeData.builder().id(7306L).nameEn("Menos de 21.5").argument(21.5).factor(2.00).build()
                ))
                .build();

        assertTrue(handler.supports(totalGroup, SportType.CS2));
        List<OddItem> totalItems = new ArrayList<>();
        handler.handle(totalGroup, match, SportType.CS2, totalItems);

        assertEquals(2, totalItems.size());
        assertEquals(BetScope.MAP_1, ((TotalBet) totalItems.get(0).getBetType()).scope());
        assertEquals(StatType.ROUNDS, ((TotalBet) totalItems.get(0).getBetType()).statType());

        ApuestatotalStakeGroupData handicapGroup = ApuestatotalStakeGroupData.builder()
                .nameEn("Hándicap de rondas - Mapa 3")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7307L).nameEn("Natus Vincere (-2.5)").argument(-2.5).factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(7308L).nameEn("FaZe Clan (+2.5)").argument(2.5).factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(handicapGroup, SportType.CS2));
        List<OddItem> hdcItems = new ArrayList<>();
        handler.handle(handicapGroup, match, SportType.CS2, hdcItems);

        assertEquals(2, hdcItems.size());
        assertEquals(BetScope.MAP_3, ((HandicapBet) hdcItems.get(0).getBetType()).scope());
        assertEquals(StatType.ROUNDS, ((HandicapBet) hdcItems.get(0).getBetType()).statType());
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) hdcItems.get(0).getBetType()).outcome());
    }

    // ==================== 4. GENERAL ESPORTS MATCH MARKETS ====================

    @Test
    void testEsportsGeneralMatchWinner() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Winner")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7401L).nameEn("1").factor(1.55).build(),
                        ApuestatotalStakeData.builder().id(7402L).nameEn("2").factor(2.45).build()
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
    }

    @Test
    void testEsportsGeneralHandicapMappedToMaps() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(2L)
                .nameEn("Handicap")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7403L).nameEn("1 (-1.5)").argument(-1.5).factor(2.60).build(),
                        ApuestatotalStakeData.builder().id(7404L).nameEn("2 (+1.5)").argument(1.5).factor(1.48).build()
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
        assertEquals(StatType.MAPS, h1.statType());
    }

    @Test
    void testEsportsGeneralTotalMappedToMaps() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(3L)
                .nameEn("Total")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(7405L).nameEn("Over").argument(2.5).factor(2.05).build(),
                        ApuestatotalStakeData.builder().id(7406L).nameEn("Under").argument(2.5).factor(1.75).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.DOTA2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.DOTA2, items);

        assertEquals(2, items.size());
        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, over.scope());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(2.5, over.param());
        assertEquals(StatType.MAPS, over.statType());
    }

    // ==================== 5. SPORT FILTERING & EXCLUSIONS ====================

    @Test
    void testNonEsportsSportFiltering() {
        ApuestatotalStakeGroupData mapGroup = ApuestatotalStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("1").factor(1.8).build()))
                .build();

        // Non-esports sports should be rejected
        assertFalse(handler.supports(mapGroup, SportType.FOOTBALL));
        assertFalse(handler.supports(mapGroup, SportType.TENNIS));
        assertFalse(handler.supports(mapGroup, SportType.BASKETBALL));
        assertFalse(handler.supports(mapGroup, SportType.HOCKEY));
        assertFalse(handler.supports(mapGroup, SportType.BOXING));
        assertFalse(handler.supports(mapGroup, SportType.MMA));

        // Esports sports should be accepted
        assertTrue(handler.supports(mapGroup, SportType.ESPORTS));
        assertTrue(handler.supports(mapGroup, SportType.CS2));
        assertTrue(handler.supports(mapGroup, SportType.DOTA2));
        assertTrue(handler.supports(mapGroup, SportType.LEAGUE_OF_LEGENDS));
        assertTrue(handler.supports(mapGroup, SportType.VALORANT));
        assertTrue(handler.supports(mapGroup, SportType.STARCRAFT));
        assertTrue(handler.supports(mapGroup, SportType.RAINBOW_SIX));
    }

    @Test
    void testFootballStatsExclusions() {
        ApuestatotalStakeGroupData cornersGroup = ApuestatotalStakeGroupData.builder()
                .nameEn("Total Corners")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("Over").argument(9.5).factor(1.9).build()))
                .build();
        assertFalse(handler.supports(cornersGroup, SportType.CS2));

        ApuestatotalStakeGroupData cardsGroup = ApuestatotalStakeGroupData.builder()
                .nameEn("Yellow Cards Total")
                .stakes(List.of(ApuestatotalStakeData.builder().nameEn("Over").argument(3.5).factor(1.8).build()))
                .build();
        assertFalse(handler.supports(cardsGroup, SportType.CS2));
    }

    // ==================== 6. EDGE CASES & INVALID INPUTS ====================

    @Test
    void testNullAndEmptyInputs() {
        assertFalse(handler.supports((ApuestatotalStakeGroupData) null, SportType.CS2));

        ApuestatotalStakeGroupData emptyGroup = ApuestatotalStakeGroupData.builder()
                .stakes(List.of())
                .build();
        assertFalse(handler.supports(emptyGroup, SportType.CS2));

        ApuestatotalStakeGroupData invalidOddsGroup = ApuestatotalStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().nameEn("1").factor(1.0).build(),
                        ApuestatotalStakeData.builder().nameEn("2").factor(null).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(invalidOddsGroup, match, SportType.CS2, items);
        assertTrue(items.isEmpty());
    }
}
