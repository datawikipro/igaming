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

class VaidebetEsportsHandlerTest {

    private VaidebetEsportsHandler handler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        handler = new VaidebetEsportsHandler();

        match = new MatchCache();
        match.setId(501L);
        match.setTeam1("Natus Vincere");
        match.setTeam2("FaZe Clan");
        match.setSportName("CS2");
    }

    @Test
    void testMap1WinnerById703TwoWay() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(7031L).nameEn("1").factor(1.72).build(),
                        VaidebetStakeData.builder().id(7032L).nameEn("2").factor(2.10).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());

        OddItem item1 = items.get(0);
        assertEquals("7031", item1.getFactorId());
        assertEquals("Map 1 Winner", item1.getGroupName());
        assertEquals(1.72, item1.getValue());
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
    void testMap2WinnerById704ThreeWayWithDraw() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .id(704L)
                .nameEn("Map 2 Result")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(7041L).nameEn("1").factor(1.90).build(),
                        VaidebetStakeData.builder().id(7042L).nameEn("X").factor(8.00).build(),
                        VaidebetStakeData.builder().id(7043L).nameEn("2").factor(1.90).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(3, items.size());

        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_2, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, bet1.outcome());
        assertEquals(StatType.MATCH, bet1.statType());

        MatchResultBet betX = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_2, betX.scope());
        assertEquals(MatchResultBet.Outcome.DRAW, betX.outcome());

        MatchResultBet bet2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(BetScope.MAP_2, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2, bet2.outcome());
    }

    @Test
    void testMapScopesByIds705To707() {
        // Map 3
        VaidebetStakeGroupData g3 = VaidebetStakeGroupData.builder()
                .id(705L)
                .nameEn("Map 3 Winner")
                .stakes(List.of(VaidebetStakeData.builder().id(7051L).nameEn("1").factor(1.85).build()))
                .build();
        assertTrue(handler.supports(g3, SportType.DOTA2));
        List<OddItem> items3 = new ArrayList<>();
        handler.handle(g3, match, SportType.DOTA2, items3);
        assertEquals(1, items3.size());
        assertEquals(BetScope.MAP_3, ((MatchResultBet) items3.get(0).getBetType()).scope());

        // Map 4
        VaidebetStakeGroupData g4 = VaidebetStakeGroupData.builder()
                .id(706L)
                .nameEn("Map 4 Winner")
                .stakes(List.of(VaidebetStakeData.builder().id(7061L).nameEn("1").factor(1.85).build()))
                .build();
        assertTrue(handler.supports(g4, SportType.DOTA2));
        List<OddItem> items4 = new ArrayList<>();
        handler.handle(g4, match, SportType.DOTA2, items4);
        assertEquals(1, items4.size());
        assertEquals(BetScope.MAP_4, ((MatchResultBet) items4.get(0).getBetType()).scope());

        // Map 5
        VaidebetStakeGroupData g5 = VaidebetStakeGroupData.builder()
                .id(707L)
                .nameEn("Map 5 Winner")
                .stakes(List.of(VaidebetStakeData.builder().id(7071L).nameEn("1").factor(1.85).build()))
                .build();
        assertTrue(handler.supports(g5, SportType.DOTA2));
        List<OddItem> items5 = new ArrayList<>();
        handler.handle(g5, match, SportType.DOTA2, items5);
        assertEquals(1, items5.size());
        assertEquals(BetScope.MAP_5, ((MatchResultBet) items5.get(0).getBetType()).scope());
    }

    @Test
    void testMapWinnerByNamePatternsRussianAndPortuguese() {
        // Russian Map 1
        VaidebetStakeGroupData gRu = VaidebetStakeGroupData.builder()
                .nameRu("Победитель 1 карты")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(11L).nameRu("П1").factor(1.65).build(),
                        VaidebetStakeData.builder().id(12L).nameRu("П2").factor(2.20).build()
                ))
                .build();
        assertTrue(handler.supports(gRu, SportType.CS2));
        List<OddItem> itemsRu = new ArrayList<>();
        handler.handle(gRu, match, SportType.CS2, itemsRu);
        assertEquals(2, itemsRu.size());
        assertEquals(BetScope.MAP_1, ((MatchResultBet) itemsRu.get(0).getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, ((MatchResultBet) itemsRu.get(0).getBetType()).outcome());

        // Portuguese Map 2
        VaidebetStakeGroupData gPt = VaidebetStakeGroupData.builder()
                .nameEn("Mapa 2 Vencedor")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(21L).nameEn("Mandante").factor(1.75).build(),
                        VaidebetStakeData.builder().id(22L).nameEn("Visitante").factor(2.05).build()
                ))
                .build();
        assertTrue(handler.supports(gPt, SportType.LEAGUE_OF_LEGENDS));
        List<OddItem> itemsPt = new ArrayList<>();
        handler.handle(gPt, match, SportType.LEAGUE_OF_LEGENDS, itemsPt);
        assertEquals(2, itemsPt.size());
        assertEquals(BetScope.MAP_2, ((MatchResultBet) itemsPt.get(0).getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, ((MatchResultBet) itemsPt.get(0).getBetType()).outcome());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, ((MatchResultBet) itemsPt.get(1).getBetType()).outcome());

        // Portuguese ordinal words: Terceiro Mapa
        VaidebetStakeGroupData gPt3 = VaidebetStakeGroupData.builder()
                .nameEn("Terceiro Mapa Vencedor")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(31L).nameEn("Time 1").factor(1.80).build(),
                        VaidebetStakeData.builder().id(32L).nameEn("Time 2").factor(1.95).build()
                ))
                .build();
        assertTrue(handler.supports(gPt3, SportType.VALORANT));
        List<OddItem> itemsPt3 = new ArrayList<>();
        handler.handle(gPt3, match, SportType.VALORANT, itemsPt3);
        assertEquals(2, itemsPt3.size());
        assertEquals(BetScope.MAP_3, ((MatchResultBet) itemsPt3.get(0).getBetType()).scope());
    }

    @Test
    void testMapHandicapByIdAndName() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .id(740L)
                .nameEn("Map Handicap")
                .nameRu("Фора по картам")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(7401L).nameEn("Handicap 1 (-1.5)").argument(1.5).factor(2.15).build(),
                        VaidebetStakeData.builder().id(7402L).nameEn("Handicap 2 (+1.5)").argument(1.5).factor(1.68).build()
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
        assertFalse(h1.isAsian());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param());
        assertEquals(StatType.MAPS, h2.statType());
        assertFalse(h2.isAsian());
    }

    @Test
    void testMapHandicapPortuguesePtBr() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Handicap de Mapas")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(7411L).nameEn("Mandante (-1.5)").argument(-1.5).factor(2.20).build(),
                        VaidebetStakeData.builder().id(7412L).nameEn("Visitante (+1.5)").argument(1.5).factor(1.65).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());
        HandicapBet h1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, h1.outcome());
        assertEquals(-1.5, h1.param());
        assertEquals(StatType.MAPS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(1.5, h2.param());
        assertEquals(StatType.MAPS, h2.statType());
    }

    @Test
    void testMapTotalById742AndName() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .id(742L)
                .nameEn("Map Total")
                .nameRu("Тотал карт")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(7421L).nameEn("Over").argument(2.5).factor(1.95).build(),
                        VaidebetStakeData.builder().id(7422L).nameEn("Under").argument(2.5).factor(1.85).build()
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
        assertEquals(StatType.MAPS, over.statType());

        TotalBet under = (TotalBet) items.get(1).getBetType();
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(2.5, under.param());
        assertEquals(StatType.MAPS, under.statType());
    }

    @Test
    void testMapTotalPortuguesePtBr() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Total de Mapas Mais/Menos")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(7431L).nameEn("Acima de 2.5").argument(2.5).factor(2.00).build(),
                        VaidebetStakeData.builder().id(7432L).nameEn("Abaixo de 2.5").argument(2.5).factor(1.80).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.DOTA2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.DOTA2, items);

        assertEquals(2, items.size());
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) items.get(0).getBetType()).direction());
        assertEquals(TotalBet.Direction.UNDER, ((TotalBet) items.get(1).getBetType()).direction());
        assertEquals(StatType.MAPS, ((TotalBet) items.get(0).getBetType()).statType());
    }

    @Test
    void testRoundTotalWithinMap() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Map 1 Total Rounds")
                .nameRu("Карта 1 - Тотал раундов")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(801L).nameEn("Over").argument(21.5).factor(1.85).build(),
                        VaidebetStakeData.builder().id(802L).nameEn("Under").argument(21.5).factor(1.95).build()
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
        assertEquals(21.5, over.param());
        assertEquals(StatType.ROUNDS, over.statType());

        TotalBet under = (TotalBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_1, under.scope());
        assertEquals(TotalBet.Direction.UNDER, under.direction());
        assertEquals(21.5, under.param());
        assertEquals(StatType.ROUNDS, under.statType());
    }

    @Test
    void testRoundHandicapWithinMap() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .nameEn("Map 2 Round Handicap")
                .nameRu("Карта 2 - Фора по раундам")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(811L).nameEn("Handicap 1 (-3.5)").argument(3.5).factor(1.90).build(),
                        VaidebetStakeData.builder().id(812L).nameEn("Handicap 2 (+3.5)").argument(3.5).factor(1.90).build()
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
        assertEquals(StatType.ROUNDS, h1.statType());

        HandicapBet h2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_2, h2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, h2.outcome());
        assertEquals(3.5, h2.param());
        assertEquals(StatType.ROUNDS, h2.statType());
    }

    @Test
    void testRoundHandicapAndTotalPortuguesePtBr() {
        VaidebetStakeGroupData groupTotal = VaidebetStakeGroupData.builder()
                .nameEn("Total de Rounds Mapa 1")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(821L).nameEn("Mais de 20.5").argument(20.5).factor(1.80).build(),
                        VaidebetStakeData.builder().id(822L).nameEn("Menos de 20.5").argument(20.5).factor(2.00).build()
                ))
                .build();

        assertTrue(handler.supports(groupTotal, SportType.CS2));
        List<OddItem> itemsTotal = new ArrayList<>();
        handler.handle(groupTotal, match, SportType.CS2, itemsTotal);
        assertEquals(2, itemsTotal.size());
        assertEquals(BetScope.MAP_1, ((TotalBet) itemsTotal.get(0).getBetType()).scope());
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) itemsTotal.get(0).getBetType()).direction());
        assertEquals(StatType.ROUNDS, ((TotalBet) itemsTotal.get(0).getBetType()).statType());

        VaidebetStakeGroupData groupHandicap = VaidebetStakeGroupData.builder()
                .nameEn("Handicap de Rounds Mapa 2")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(831L).nameEn("Mandante (-2.5)").argument(-2.5).factor(1.95).build(),
                        VaidebetStakeData.builder().id(832L).nameEn("Visitante (+2.5)").argument(2.5).factor(1.85).build()
                ))
                .build();

        assertTrue(handler.supports(groupHandicap, SportType.CS2));
        List<OddItem> itemsHandicap = new ArrayList<>();
        handler.handle(groupHandicap, match, SportType.CS2, itemsHandicap);
        assertEquals(2, itemsHandicap.size());
        assertEquals(BetScope.MAP_2, ((HandicapBet) itemsHandicap.get(0).getBetType()).scope());
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) itemsHandicap.get(0).getBetType()).outcome());
        assertEquals(StatType.ROUNDS, ((HandicapBet) itemsHandicap.get(0).getBetType()).statType());
    }

    @Test
    void testGenericEsportsMatchWinnerWhenEsportsSport() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Winner")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(101L).nameEn("1").factor(1.50).build(),
                        VaidebetStakeData.builder().id(102L).nameEn("2").factor(2.50).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.CS2));
        assertTrue(handler.supports(group, SportType.DOTA2));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(2, items.size());
        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());
        assertEquals(StatType.MATCH, bet1.statType());
    }

    @Test
    void testSupportsExclusions() {
        // Boxing / MMA excluded
        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .nameEn("Round 1 Total")
                .stakes(List.of(VaidebetStakeData.builder().factor(1.8).build()))
                .build(), SportType.BOXING));

        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .nameEn("Round 1 Total")
                .stakes(List.of(VaidebetStakeData.builder().factor(1.8).build()))
                .build(), SportType.MMA));

        // Corner / Card excluded
        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .nameEn("Corners Total")
                .stakes(List.of(VaidebetStakeData.builder().factor(1.8).build()))
                .build(), SportType.FOOTBALL));

        // Empty stakes
        assertFalse(handler.supports(VaidebetStakeGroupData.builder()
                .id(703L)
                .stakes(List.of())
                .build(), SportType.CS2));

        // Null group
        assertFalse(handler.supports((VaidebetStakeGroupData) null, SportType.CS2));
    }

    @Test
    void testInvalidOddsFiltering() {
        VaidebetStakeGroupData group = VaidebetStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        VaidebetStakeData.builder().id(7031L).nameEn("1").factor(1.0).build(), // <= 1.0 filtered
                        VaidebetStakeData.builder().id(7032L).nameEn("2").factor(null).build(), // null filtered
                        VaidebetStakeData.builder().id(7033L).nameEn("1").factor(1.95).build() // valid
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.CS2, items);

        assertEquals(1, items.size());
        assertEquals("7033", items.get(0).getFactorId());
        assertEquals(1.95, items.get(0).getValue());
    }
}
