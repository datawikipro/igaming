package pro.datawiki.igaming.source.esportesdasorte.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

class EsportesdasorteEsportsHandlerTest {

    private EsportesdasorteEsportsHandler handler;
    private MatchCache cs2Match;
    private MatchCache dotaMatch;

    @BeforeEach
    void setUp() {
        handler = new EsportesdasorteEsportsHandler();

        cs2Match = new MatchCache();
        cs2Match.setId(101L);
        cs2Match.setTeam1("Natus Vincere");
        cs2Match.setTeam2("FaZe Clan");
        cs2Match.setSportName("CS2");

        dotaMatch = new MatchCache();
        dotaMatch.setId(102L);
        dotaMatch.setTeam1("Team Spirit");
        dotaMatch.setTeam2("Gaimin Gladiators");
        dotaMatch.setSportName("Dota 2");
    }

    // =========================================================================
    // 2.3.1 Победители карт (BetScope.MAP_1..MAP_5, StatType.MATCH)
    // =========================================================================

    @Test
    @DisplayName("2.3.1 ID 703L - Map 1 Winner (2-way)")
    void testMap1WinnerById() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(7031L).nameEn("Natus Vincere").factor(1.65).build(),
                        EsportesdasorteStakeData.builder().id(7032L).nameEn("FaZe Clan").factor(2.25).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, cs2Match, SportType.CS2, items);

        assertEquals(2, items.size());

        OddItem item1 = items.get(0);
        assertEquals("7031", item1.getFactorId());
        assertEquals(1.65, item1.getValue());
        assertTrue(item1.getBetType() instanceof MatchResultBet);
        MatchResultBet bet1 = (MatchResultBet) item1.getBetType();
        assertEquals(BetScope.MAP_1, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());
        assertEquals(StatType.MATCH, bet1.statType());

        OddItem item2 = items.get(1);
        assertEquals("7032", item2.getFactorId());
        assertEquals(2.25, item2.getValue());
        MatchResultBet bet2 = (MatchResultBet) item2.getBetType();
        assertEquals(BetScope.MAP_1, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
        assertEquals(StatType.MATCH, bet2.statType());
    }

    @Test
    @DisplayName("2.3.1 ID 704L, 705L, 706L, 707L - Maps 2 to 5 Winners")
    void testMaps2To5WinnersById() {
        long[] ids = {704L, 705L, 706L, 707L};
        BetScope[] scopes = {BetScope.MAP_2, BetScope.MAP_3, BetScope.MAP_4, BetScope.MAP_5};

        for (int i = 0; i < ids.length; i++) {
            EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                    .id(ids[i])
                    .nameEn("Map " + (i + 2) + " Winner")
                    .stakes(List.of(
                            EsportesdasorteStakeData.builder().id(ids[i] * 10 + 1).nameEn("1").factor(1.80).build(),
                            EsportesdasorteStakeData.builder().id(ids[i] * 10 + 2).nameEn("2").factor(2.00).build()
                    ))
                    .build();

            assertTrue(handler.supports(group, SportType.CS2));

            List<OddItem> items = new ArrayList<>();
            handler.handle(group, cs2Match, SportType.CS2, items);

            assertEquals(2, items.size());
            MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
            assertEquals(scopes[i], bet1.scope());
            assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());
            assertEquals(StatType.MATCH, bet1.statType());

            MatchResultBet bet2 = (MatchResultBet) items.get(1).getBetType();
            assertEquals(scopes[i], bet2.scope());
            assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
            assertEquals(StatType.MATCH, bet2.statType());
        }
    }

    @Test
    @DisplayName("2.3.1 Map Winner by Name (English, Russian, Portuguese PT-BR)")
    void testMapWinnerByNameVariants() {
        // PT-BR
        EsportesdasorteStakeGroupData ptBrGroup = EsportesdasorteStakeGroupData.builder()
                .nameEn("Vencedor do Mapa 1")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Mandante").factor(1.90).build(),
                        EsportesdasorteStakeData.builder().nameEn("Visitante").factor(1.90).build()
                ))
                .build();
        assertTrue(handler.supports(ptBrGroup, SportType.CS2));
        List<OddItem> ptItems = new ArrayList<>();
        handler.handle(ptBrGroup, cs2Match, SportType.CS2, ptItems);
        assertEquals(2, ptItems.size());
        assertEquals(BetScope.MAP_1, ((MatchResultBet) ptItems.get(0).getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, ((MatchResultBet) ptItems.get(0).getBetType()).outcome());

        // Ordinal PT-BR: "1º Mapa - Vencedor"
        EsportesdasorteStakeGroupData ptOrdinalGroup = EsportesdasorteStakeGroupData.builder()
                .nameEn("1º Mapa - Vencedor")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Team 1").factor(1.50).build(),
                        EsportesdasorteStakeData.builder().nameEn("Team 2").factor(2.60).build()
                ))
                .build();
        assertTrue(handler.supports(ptOrdinalGroup, SportType.CS2));
        List<OddItem> ptOrdItems = new ArrayList<>();
        handler.handle(ptOrdinalGroup, cs2Match, SportType.CS2, ptOrdItems);
        assertEquals(BetScope.MAP_1, ((MatchResultBet) ptOrdItems.get(0).getBetType()).scope());

        // Russian: "Победитель 2-й карты"
        EsportesdasorteStakeGroupData ruGroup = EsportesdasorteStakeGroupData.builder()
                .nameRu("Победитель 2-й карты")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameRu("П1").factor(2.10).build(),
                        EsportesdasorteStakeData.builder().nameRu("П2").factor(1.72).build()
                ))
                .build();
        assertTrue(handler.supports(ruGroup, SportType.DOTA2));
        List<OddItem> ruItems = new ArrayList<>();
        handler.handle(ruGroup, dotaMatch, SportType.DOTA2, ruItems);
        assertEquals(2, ruItems.size());
        assertEquals(BetScope.MAP_2, ((MatchResultBet) ruItems.get(0).getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, ((MatchResultBet) ruItems.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, ((MatchResultBet) ruItems.get(1).getBetType()).outcome());
    }

    @Test
    @DisplayName("2.3.1 Map Winner 3-way with Draw")
    void testMapWinner3WayWithDraw() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Map 1 - Result")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("1").factor(2.30).build(),
                        EsportesdasorteStakeData.builder().nameEn("X").factor(9.00).build(),
                        EsportesdasorteStakeData.builder().nameEn("2").factor(1.70).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, cs2Match, SportType.CS2, items);

        assertEquals(3, items.size());
        MatchResultBet b1 = (MatchResultBet) items.get(0).getBetType();
        MatchResultBet bx = (MatchResultBet) items.get(1).getBetType();
        MatchResultBet b2 = (MatchResultBet) items.get(2).getBetType();

        assertEquals(BetScope.MAP_1, b1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, b1.outcome());

        assertEquals(BetScope.MAP_1, bx.scope());
        assertEquals(MatchResultBet.Outcome.DRAW, bx.outcome());

        assertEquals(BetScope.MAP_1, b2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2, b2.outcome());
    }

    @Test
    @DisplayName("2.3.1 General Esports Match Winner (BetScope.FULL_MATCH, StatType.MATCH)")
    void testEsportsMatchWinner() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(702L)
                .nameEn("Match Winner")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Natus Vincere").factor(1.85).build(),
                        EsportesdasorteStakeData.builder().nameEn("FaZe Clan").factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, cs2Match, SportType.CS2, items);

        assertEquals(2, items.size());
        MatchResultBet b1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, b1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, b1.outcome());
        assertEquals(StatType.MATCH, b1.statType());

        MatchResultBet b2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, b2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, b2.outcome());
        assertEquals(StatType.MATCH, b2.statType());
    }

    // =========================================================================
    // 2.3.2 Тоталы и форы по картам (StatType.MAPS)
    // =========================================================================

    @Test
    @DisplayName("2.3.2 ID 742L - Map Total (StatType.MAPS, BetScope.FULL_MATCH)")
    void testMapTotalById() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(742L)
                .nameEn("Total Maps")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(7421L).nameEn("Over 2.5").argument(2.5).factor(1.95).build(),
                        EsportesdasorteStakeData.builder().id(7422L).nameEn("Under 2.5").argument(2.5).factor(1.85).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, cs2Match, SportType.CS2, items);

        assertEquals(2, items.size());

        OddItem overItem = items.get(0);
        assertTrue(overItem.getBetType() instanceof TotalBet);
        TotalBet over = (TotalBet) overItem.getBetType();
        assertEquals(BetScope.FULL_MATCH, over.scope());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(2.5, over.param());
        assertEquals(StatType.MAPS, over.statType());
        assertFalse(over.isAsian());

        OddItem underItem = items.get(1);
        TotalBet under = (TotalBet) underItem.getBetType();
        assertEquals(BetScope.FULL_MATCH, under.scope());
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(2.5, under.param());
        assertEquals(StatType.MAPS, under.statType());
    }

    @Test
    @DisplayName("2.3.2 ID 740L/741L - Map Handicap (StatType.MAPS, BetScope.FULL_MATCH)")
    void testMapHandicapById() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(740L)
                .nameEn("Map Handicap")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(7401L).nameEn("1 (-1.5)").argument(1.5).factor(2.70).build(),
                        EsportesdasorteStakeData.builder().id(7402L).nameEn("2 (+1.5)").argument(1.5).factor(1.45).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, cs2Match, SportType.CS2, items);

        assertEquals(2, items.size());

        OddItem h1Item = items.get(0);
        assertTrue(h1Item.getBetType() instanceof HandicapBet);
        HandicapBet h1 = (HandicapBet) h1Item.getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-1.5, h1.param(), 0.001);
        assertEquals(StatType.MAPS, h1.statType());
        assertFalse(h1.isAsian());

        OddItem h2Item = items.get(1);
        HandicapBet h2 = (HandicapBet) h2Item.getBetType();
        assertEquals(BetScope.FULL_MATCH, h2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param(), 0.001);
        assertEquals(StatType.MAPS, h2.statType());
    }

    @Test
    @DisplayName("2.3.2 PT-BR Map Total & Handicap: Total de Mapas, Desvantagem de Mapas")
    void testPtBrMapTotalAndHandicap() {
        // PT-BR Total de Mapas
        EsportesdasorteStakeGroupData totalGroup = EsportesdasorteStakeGroupData.builder()
                .nameEn("Total de Mapas")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Acima de 2.5").factor(2.05).build(),
                        EsportesdasorteStakeData.builder().nameEn("Abaixo de 2.5").factor(1.75).build()
                ))
                .build();

        assertTrue(handler.supports(totalGroup, SportType.CS2));
        List<OddItem> totalItems = new ArrayList<>();
        handler.handle(totalGroup, cs2Match, SportType.CS2, totalItems);

        assertEquals(2, totalItems.size());
        TotalBet over = (TotalBet) totalItems.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, over.scope());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(2.5, over.param());
        assertEquals(StatType.MAPS, over.statType());

        // PT-BR Desvantagem de Mapas
        EsportesdasorteStakeGroupData hdcGroup = EsportesdasorteStakeGroupData.builder()
                .nameEn("Desvantagem de Mapas")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Desvantagem 1 (-1.5)").factor(2.65).build(),
                        EsportesdasorteStakeData.builder().nameEn("Desvantagem 2 (+1.5)").factor(1.48).build()
                ))
                .build();

        assertTrue(handler.supports(hdcGroup, SportType.CS2));
        List<OddItem> hdcItems = new ArrayList<>();
        handler.handle(hdcGroup, cs2Match, SportType.CS2, hdcItems);

        assertEquals(2, hdcItems.size());
        HandicapBet h1 = (HandicapBet) hdcItems.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-1.5, h1.param(), 0.001);
        assertEquals(StatType.MAPS, h1.statType());
    }

    // =========================================================================
    // 2.3.2 Тоталы и форы по раундам (StatType.ROUNDS)
    // =========================================================================

    @Test
    @DisplayName("2.3.2 Map 1 Total Rounds (BetScope.MAP_1, StatType.ROUNDS)")
    void testMap1TotalRounds() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Map 1 - Total Rounds")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Over").argument(21.5).factor(1.90).build(),
                        EsportesdasorteStakeData.builder().nameEn("Under").argument(21.5).factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, cs2Match, SportType.CS2, items);

        assertEquals(2, items.size());

        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, over.scope());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(21.5, over.param());
        assertEquals(StatType.ROUNDS, over.statType());

        TotalBet under = (TotalBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_1, under.scope());
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(21.5, under.param());
        assertEquals(StatType.ROUNDS, under.statType());
    }

    @Test
    @DisplayName("2.3.2 Map 1 Round Handicap (BetScope.MAP_1, StatType.ROUNDS)")
    void testMap1RoundHandicap() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Map 1 - Round Handicap")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Natus Vincere (-2.5)").argument(2.5).factor(1.85).build(),
                        EsportesdasorteStakeData.builder().nameEn("FaZe Clan (+2.5)").argument(2.5).factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, cs2Match, SportType.CS2, items);

        assertEquals(2, items.size());

        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, h1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-2.5, h1.param(), 0.001);
        assertEquals(StatType.ROUNDS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_1, h2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(2.5, h2.param(), 0.001);
        assertEquals(StatType.ROUNDS, h2.statType());
    }

    @Test
    @DisplayName("2.3.2 PT-BR: Total de Rodadas - Mapa 1 (BetScope.MAP_1, StatType.ROUNDS)")
    void testPtBrTotalRodadasMapa1() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Total de Rodadas - Mapa 1")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Mais de 20.5").factor(1.82).build(),
                        EsportesdasorteStakeData.builder().nameEn("Menos de 20.5").factor(1.98).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, cs2Match, SportType.CS2, items);

        assertEquals(2, items.size());

        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, over.scope());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(20.5, over.param());
        assertEquals(StatType.ROUNDS, over.statType());

        TotalBet under = (TotalBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_1, under.scope());
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(20.5, under.param());
        assertEquals(StatType.ROUNDS, under.statType());
    }

    @Test
    @DisplayName("2.3.2 Full Match Total Rounds in CS2 (BetScope.FULL_MATCH, StatType.ROUNDS)")
    void testFullMatchTotalRounds() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Total Rounds")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Over 54.5").argument(54.5).factor(1.90).build(),
                        EsportesdasorteStakeData.builder().nameEn("Under 54.5").argument(54.5).factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, cs2Match, SportType.CS2, items);

        assertEquals(2, items.size());
        TotalBet over = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, over.scope());
        assertEquals(TotalBet.Direction.OVER, over.direction());
        assertEquals(54.5, over.param());
        assertEquals(StatType.ROUNDS, over.statType());
    }

    // =========================================================================
    // Edge cases and exclusions
    // =========================================================================

    @Test
    @DisplayName("Exclusions: Boxing and MMA excluded from esports handler")
    void testBoxingMmaExclusion() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Round 1 Winner")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("1").factor(2.50).build()
                ))
                .build();

        assertFalse(handler.supports(group, SportType.BOXING));
        assertFalse(handler.supports(group, SportType.MMA));
    }

    @Test
    @DisplayName("Exclusions: Football stats (corners, cards) excluded")
    void testFootballStatsExclusion() {
        EsportesdasorteStakeGroupData cornerGroup = EsportesdasorteStakeGroupData.builder()
                .nameEn("Corners Total")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Over 9.5").argument(9.5).factor(1.90).build()
                ))
                .build();

        assertFalse(handler.supports(cornerGroup, SportType.FOOTBALL));

        EsportesdasorteStakeGroupData cardGroup = EsportesdasorteStakeGroupData.builder()
                .nameEn("Total Yellow Cards")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("Over 4.5").argument(4.5).factor(1.90).build()
                ))
                .build();

        assertFalse(handler.supports(cardGroup, SportType.FOOTBALL));
    }

    @Test
    @DisplayName("Edge cases: null and empty handling")
    void testNullAndEmptyHandling() {
        assertFalse(handler.supports((EsportesdasorteStakeGroupData) null, SportType.CS2));
        assertFalse(handler.supports(EsportesdasorteStakeGroupData.builder().build(), SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(null, cs2Match, SportType.CS2, items);
        assertTrue(items.isEmpty());

        handler.handle(EsportesdasorteStakeGroupData.builder().stakes(List.of()).build(), cs2Match, SportType.CS2, items);
        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Edge cases: invalid odd factors (<= 1.0) are skipped")
    void testInvalidOddFactors() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().nameEn("1").factor(1.00).build(),
                        EsportesdasorteStakeData.builder().nameEn("2").factor(-0.5).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, cs2Match, SportType.CS2, items);
        assertTrue(items.isEmpty());
    }
}
