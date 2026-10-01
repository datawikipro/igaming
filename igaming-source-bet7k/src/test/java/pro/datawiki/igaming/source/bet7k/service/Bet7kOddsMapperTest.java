package pro.datawiki.igaming.source.bet7k.service;

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
import pro.datawiki.igaming.source.bet7k.dto.Bet7kEventDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kMarketDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Bet7kOddsMapperTest {

    private Bet7kOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new Bet7kOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("bet7k", SportType.FOOTBALL));
        assertTrue(mapper.supports("BET7K", SportType.ESPORTS));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-100")
                .sportName("Futebol")
                .leagueName("Brasileirão Série A")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .isLive(false)
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Resultado Final")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo").decimal(2.10).build(),
                                        Bet7kOutcomeDto.builder().name("Empate").decimal(3.30).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras").decimal(3.40).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("bet7k", request.getBookmaker());
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
    void testDoubleChance() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-101")
                .sportName("Soccer")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Dupla Chance")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("1X").decimal(1.35).build(),
                                        Bet7kOutcomeDto.builder().name("12").decimal(1.30).build(),
                                        Bet7kOutcomeDto.builder().name("X2").decimal(1.65).build()
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
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-102")
                .sportName("Futebol")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Total de Gols Mais/Menos")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 2.5").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 2.5").decimal(1.95).build()
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
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-103")
                .sportName("Soccer")
                .homeTeam("Liverpool")
                .awayTeam("Manchester City")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Total Casa - Mais/Menos")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais 1.5").decimal(1.70).build(),
                                        Bet7kOutcomeDto.builder().name("Menos 1.5").decimal(2.10).build()
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
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-104")
                .sportName("Basquete")
                .homeTeam("LA Lakers")
                .awayTeam("Golden State")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Handicap Asiatico")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("LA Lakers (-4.5)").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Golden State (+4.5)").decimal(1.90).build()
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
    void testScopeResolutionFirstHalf() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-105")
                .sportName("Futebol")
                .homeTeam("Santos")
                .awayTeam("Corinthians")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("1° Tempo - Resultado")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Santos").decimal(2.50).build(),
                                        Bet7kOutcomeDto.builder().name("Empate").decimal(2.00).build(),
                                        Bet7kOutcomeDto.builder().name("Corinthians").decimal(3.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem w1 = request.getOdds().stream().filter(o -> "Santos".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        MatchResultBet mrb = (MatchResultBet) w1.getBetType();
        assertEquals(BetScope.HALF_1, mrb.scope());
    }

    @Test
    void testFallbackTitleParsing() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-106")
                .name("Inter Milan vs AC Milan")
                .sportName("Futebol")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("1X2")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Inter Milan").decimal(2.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("Inter Milan", request.getTeam1());
        assertEquals("AC Milan", request.getTeam2());
    }

    @Test
    void testNullAndEmptyInputs() {
        assertNull(mapper.mapToOddsUpdateRequest(null));

        Bet7kEventDto empty = new Bet7kEventDto();
        assertNull(mapper.mapToOddsUpdateRequest(empty));

        Bet7kEventDto onlyId = Bet7kEventDto.builder().id("123").build();
        assertNull(mapper.mapToOddsUpdateRequest(onlyId));
    }
}
