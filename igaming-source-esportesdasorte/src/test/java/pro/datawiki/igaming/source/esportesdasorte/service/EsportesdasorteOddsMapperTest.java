package pro.datawiki.igaming.source.esportesdasorte.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteMatchOddsData;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeData;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeGroupData;
import pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteEsportsHandler;
import pro.datawiki.igaming.source.esportesdasorte.service.handler.EsportesdasorteMarketHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EsportesdasorteOddsMapperTest {

    private EsportesdasorteOddsMapper mapper;
    private MatchCache cs2Match;

    @BeforeEach
    void setUp() {
        mapper = new EsportesdasorteOddsMapper();

        cs2Match = new MatchCache();
        cs2Match.setId(500L);
        cs2Match.setTeam1("Natus Vincere");
        cs2Match.setTeam2("FaZe Clan");
        cs2Match.setLeagueName("IEM Cologne");
        cs2Match.setSportName("Counter-Strike 2");
    }

    @Test
    @DisplayName("2.4.1 Handlers order and fallback initialization")
    void testHandlersInitializationAndOrder() {
        List<EsportesdasorteMarketHandler> handlers = mapper.getHandlers();
        assertNotNull(handlers);
        assertFalse(handlers.isEmpty());

        // EsportsHandler should be @Order(10), first among the handlers
        assertTrue(handlers.get(0) instanceof EsportesdasorteEsportsHandler);
    }

    @Test
    @DisplayName("2.4.3 Metadata: BR/LATAM regions, eventUrl, bookmaker name")
    void testMapperMetadataAndRegions() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(1L).nameEn("1").factor(1.85).build(),
                        EsportesdasorteStakeData.builder().id(2L).nameEn("2").factor(1.95).build()
                ))
                .build();

        EsportesdasorteMatchOddsData oddsData = EsportesdasorteMatchOddsData.builder()
                .matchId(12345L)
                .groups(List.of(group))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cs2Match, oddsData);

        assertNotNull(request);
        assertEquals("esportesdasorte", request.getBookmaker());
        assertEquals("12345", request.getExternalEventId());
        assertEquals("https://esportesdasorte.com/line/sport/event/12345", request.getEventUrl());
        assertEquals(List.of(BookmakerRegion.BR, BookmakerRegion.LATAM), request.getRegions());
        assertEquals("Natus Vincere", request.getTeam1());
        assertEquals("FaZe Clan", request.getTeam2());
        assertEquals("IEM Cologne", request.getLeagueName());
        assertEquals(SportType.CS2, request.getSportType());
    }

    @Test
    @DisplayName("2.5.3 Esports full mapping with CS2 (Map winners, totals, handicaps)")
    void testEsportsFullMappingCs2() {
        EsportesdasorteStakeGroupData mapWinnerGroup = EsportesdasorteStakeGroupData.builder()
                .id(703L)
                .nameEn("Map 1 Winner")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(11L).nameEn("1").factor(1.70).build(),
                        EsportesdasorteStakeData.builder().id(12L).nameEn("2").factor(2.10).build()
                ))
                .build();

        EsportesdasorteStakeGroupData mapTotalGroup = EsportesdasorteStakeGroupData.builder()
                .id(742L)
                .nameEn("Total Maps")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(21L).nameEn("Over 2.5").argument(2.5).factor(1.90).build(),
                        EsportesdasorteStakeData.builder().id(22L).nameEn("Under 2.5").argument(2.5).factor(1.90).build()
                ))
                .build();

        EsportesdasorteStakeGroupData roundTotalGroup = EsportesdasorteStakeGroupData.builder()
                .nameEn("Map 1 - Total Rounds")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(31L).nameEn("Mais de 21.5").argument(21.5).factor(1.80).build(),
                        EsportesdasorteStakeData.builder().id(32L).nameEn("Menos de 21.5").argument(21.5).factor(2.00).build()
                ))
                .build();

        EsportesdasorteMatchOddsData oddsData = EsportesdasorteMatchOddsData.builder()
                .matchId(999L)
                .groups(List.of(mapWinnerGroup, mapTotalGroup, roundTotalGroup))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cs2Match, oddsData);

        assertNotNull(request);
        assertEquals(6, request.getOdds().size());

        // Check map 1 winner
        OddItem w1 = request.getOdds().get(0);
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        MatchResultBet b1 = (MatchResultBet) w1.getBetType();
        assertEquals(BetScope.MAP_1, b1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, b1.outcome());
        assertEquals(StatType.MATCH, b1.statType());

        // Check total maps
        OddItem tmOver = request.getOdds().get(2);
        assertTrue(tmOver.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) tmOver.getBetType();
        assertEquals(BetScope.FULL_MATCH, tb.scope());
        assertEquals(StatType.MAPS, tb.statType());
        assertEquals(2.5, tb.param());

        // Check map 1 rounds
        OddItem rOver = request.getOdds().get(4);
        assertTrue(rOver.getBetType() instanceof TotalBet);
        TotalBet rb = (TotalBet) rOver.getBetType();
        assertEquals(BetScope.MAP_1, rb.scope());
        assertEquals(StatType.ROUNDS, rb.statType());
        assertEquals(21.5, rb.param());
    }

    @Test
    @DisplayName("2.5.3 Dota 2 sport type resolution and kill markets")
    void testDota2Mapping() {
        MatchCache dotaMatch = new MatchCache();
        dotaMatch.setId(600L);
        dotaMatch.setTeam1("Team Spirit");
        dotaMatch.setTeam2("Gaimin Gladiators");
        dotaMatch.setSportName("Dota 2");

        EsportesdasorteStakeGroupData killHandicapGroup = EsportesdasorteStakeGroupData.builder()
                .nameEn("Map 1 - Kill Handicap")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(41L).nameEn("Team Spirit (-5.5)").argument(5.5).factor(1.85).build(),
                        EsportesdasorteStakeData.builder().id(42L).nameEn("Gaimin Gladiators (+5.5)").argument(5.5).factor(1.95).build()
                ))
                .build();

        EsportesdasorteMatchOddsData oddsData = EsportesdasorteMatchOddsData.builder()
                .matchId(888L)
                .groups(List.of(killHandicapGroup))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(dotaMatch, oddsData);

        assertNotNull(request);
        assertEquals(SportType.DOTA2, request.getSportType());
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().get(0);
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet hb = (HandicapBet) h1.getBetType();
        assertEquals(BetScope.MAP_1, hb.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());
        assertEquals(-5.5, hb.param(), 0.001);
        assertEquals(StatType.KILLS, hb.statType());
    }

    @Test
    @DisplayName("2.5.4 Edge cases: null payload, empty groups, unsupported bookmaker")
    void testEdgeCases() {
        assertNull(mapper.mapToOddsUpdateRequest(null, null));
        assertNull(mapper.mapToOddsUpdateRequest(cs2Match, null));
        assertNull(mapper.mapToOddsUpdateRequest(null, EsportesdasorteMatchOddsData.builder().build()));
        assertNull(mapper.mapToOddsUpdateRequest(cs2Match, EsportesdasorteMatchOddsData.builder().groups(List.of()).build()));

        assertTrue(mapper.supports("esportesdasorte", SportType.FOOTBALL));
        assertTrue(mapper.supports("ESPORTESDASORTE", SportType.CS2));
        assertFalse(mapper.supports("winline", SportType.FOOTBALL));
    }
}
