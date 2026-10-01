package pro.datawiki.igaming.source.betesporte.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetesporteOddsMapperTest {

    private BetesporteOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BetesporteOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("betesporte", SportType.FOOTBALL));
        assertTrue(mapper.supports("BETESPORTE", SportType.ESPORTS));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-100")
                .sportName("Futebol")
                .leagueName("Brasileirão Série A")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .isLive(false)
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Resultado Final")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Flamengo").decimal(2.10).build(),
                                        BetesporteOutcomeDto.builder().name("Empate").decimal(3.30).build(),
                                        BetesporteOutcomeDto.builder().name("Palmeiras").decimal(3.40).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("betesporte", request.getBookmaker());
        assertEquals("Flamengo", request.getTeam1());
        assertEquals("Palmeiras", request.getTeam2());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertTrue(request.getRegions().contains(BookmakerRegion.LATAM));

        List<OddItem> odds = request.getOdds();
        assertEquals(3, odds.size());

        OddItem w1 = odds.stream().filter(o -> "Flamengo".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(2.10, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) w1.getBetType()).outcome());

        OddItem draw = odds.stream().filter(o -> "Empate".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(3.30, draw.getValue());
        assertTrue(draw.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) draw.getBetType()).outcome());

        OddItem w2 = odds.stream().filter(o -> "Palmeiras".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(3.40, w2.getValue());
        assertTrue(w2.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) w2.getBetType()).outcome());
    }

    @Test
    void testMatchResultPeriod() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-1002")
                .homeTeam("Corinthians")
                .awayTeam("Santos")
                .sportName("Futebol")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("1° Tempo - Resultado")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("1").decimal(2.80).build(),
                                        BetesporteOutcomeDto.builder().name("X").decimal(2.05).build(),
                                        BetesporteOutcomeDto.builder().name("2").decimal(3.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest update = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(update);
        List<OddItem> odds = update.getOdds();
        assertEquals(3, odds.size());

        OddItem item1T = odds.get(0);
        MatchResultBet bet1T = (MatchResultBet) item1T.getBetType();
        assertEquals(BetScope.HALF_1, bet1T.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, bet1T.outcome());
    }

    @Test
    void testDoubleChance() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-101")
                .sportName("Soccer")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Dupla Chance")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("1X").decimal(1.35).build(),
                                        BetesporteOutcomeDto.builder().name("12").decimal(1.30).build(),
                                        BetesporteOutcomeDto.builder().name("X2").decimal(1.65).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream().filter(o -> "1X".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.35, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1X.getBetType()).outcome());
    }

    @Test
    void testTotals() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-102")
                .sportName("Futebol")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Total de Gols Mais/Menos")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais de 2.5").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("Menos de 2.5").decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().stream().filter(o -> o.getName().contains("Mais")).findFirst().orElseThrow();
        assertEquals(1.85, over.getValue());
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet tbOver = (TotalBet) over.getBetType();
        assertEquals(2.5, tbOver.param());
        assertEquals(TotalBet.Direction.OVER, tbOver.direction());
        assertEquals(BetSubject.MATCH, tbOver.subject());

        OddItem under = request.getOdds().stream().filter(o -> o.getName().contains("Menos")).findFirst().orElseThrow();
        assertEquals(1.95, under.getValue());
        assertTrue(under.getBetType() instanceof TotalBet);
        TotalBet tbUnder = (TotalBet) under.getBetType();
        assertEquals(2.5, tbUnder.param());
        assertEquals(TotalBet.Direction.UNDER, tbUnder.direction());
    }

    @Test
    void testTeamTotals() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-103")
                .sportName("Soccer")
                .homeTeam("Liverpool")
                .awayTeam("Manchester City")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Total Casa - Mais/Menos")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 1.5").decimal(1.70).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 1.5").decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().stream().filter(o -> o.getName().contains("Mais")).findFirst().orElseThrow();
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) over.getBetType();
        assertEquals(BetSubject.TEAM1, tb.subject());
        assertEquals(1.5, tb.param());
    }

    @Test
    void testHandicap() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-104")
                .sportName("Basquete")
                .homeTeam("LA Lakers")
                .awayTeam("Golden State")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Handicap Asiatico")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("LA Lakers (-4.5)").decimal(1.90).build(),
                                        BetesporteOutcomeDto.builder().name("Golden State (+4.5)").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().stream().filter(o -> o.getName().contains("Lakers")).findFirst().orElseThrow();
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-4.5, hb1.param());
        assertTrue(hb1.isAsian());
    }

    @Test
    void testInvalidOrEmptyEvent() {
        assertNull(mapper.mapToOddsUpdateRequest(null));
        assertNull(mapper.mapToOddsUpdateRequest(BetesporteEventDto.builder().id("1").build()));
    }
}
