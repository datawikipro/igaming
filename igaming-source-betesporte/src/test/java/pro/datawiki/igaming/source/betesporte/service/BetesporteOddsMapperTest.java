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
                                        BetesporteOutcomeDto.builder().name("1X").odds(1.35).build(),
                                        BetesporteOutcomeDto.builder().name("12").odds(1.28).build(),
                                        BetesporteOutcomeDto.builder().name("X2").odds(1.55).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());
    }

    @Test
    void testTotalOverUnder() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-102")
                .sportName("Futebol")
                .homeTeam("Santos")
                .awayTeam("Corinthians")
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
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet tbOver = (TotalBet) over.getBetType();
        assertEquals(TotalBet.Direction.OVER, tbOver.direction());
        assertEquals(2.5, tbOver.param());

        OddItem under = request.getOdds().stream().filter(o -> o.getName().contains("Menos")).findFirst().orElseThrow();
        assertTrue(under.getBetType() instanceof TotalBet);
        TotalBet tbUnder = (TotalBet) under.getBetType();
        assertEquals(TotalBet.Direction.UNDER, tbUnder.direction());
        assertEquals(2.5, tbUnder.param());
    }

    @Test
    void testTeamTotal() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-103")
                .sportName("Soccer")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Total Casa - Mais/Menos")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 1.5").decimal(1.70).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 1.5").decimal(2.15).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem item = request.getOdds().get(0);
        assertTrue(item.getBetType() instanceof TotalBet);
        assertEquals(BetSubject.TEAM1, ((TotalBet) item.getBetType()).subject());
    }

    @Test
    void testHandicap() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-104")
                .sportName("Futebol")
                .homeTeam("Liverpool")
                .awayTeam("Everton")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Handicap Asiatico")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Liverpool (-1.5)").decimal(2.05).build(),
                                        BetesporteOutcomeDto.builder().name("Everton (+1.5)").decimal(1.80).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().stream().filter(o -> o.getName().contains("Liverpool")).findFirst().orElseThrow();
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-1.5, hb1.param());

        OddItem h2 = request.getOdds().stream().filter(o -> o.getName().contains("Everton")).findFirst().orElseThrow();
        assertTrue(h2.getBetType() instanceof HandicapBet);
        HandicapBet hb2 = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(1.5, hb2.param());
    }

    @Test
    void testHalvesScope() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-105")
                .sportName("Futebol")
                .homeTeam("PSG")
                .awayTeam("Marseille")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("1º Tempo - Resultado Final")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("PSG").decimal(2.20).build(),
                                        BetesporteOutcomeDto.builder().name("Empate").decimal(2.40).build(),
                                        BetesporteOutcomeDto.builder().name("Marseille").decimal(4.50).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());
        assertEquals(BetScope.HALF_1, ((MatchResultBet) request.getOdds().get(0).getBetType()).scope());
    }

    @Test
    void testInvalidEventOrMissingParticipants() {
        assertNull(mapper.mapToOddsUpdateRequest(null));

        BetesporteEventDto noTeams = BetesporteEventDto.builder().id("ev-invalid").build();
        assertNull(mapper.mapToOddsUpdateRequest(noTeams));
    }
}
