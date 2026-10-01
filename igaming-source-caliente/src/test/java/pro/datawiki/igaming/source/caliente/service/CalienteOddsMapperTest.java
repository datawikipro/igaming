package pro.datawiki.igaming.source.caliente.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.caliente.dto.CalienteEventDto;
import pro.datawiki.igaming.source.caliente.dto.CalienteMarketDto;
import pro.datawiki.igaming.source.caliente.dto.CalienteOutcomeDto;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalienteOddsMapperTest {

    private CalienteOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CalienteOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("caliente", SportType.FOOTBALL));
        assertTrue(mapper.supports("CALIENTE", SportType.ESPORTS));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        CalienteEventDto event = CalienteEventDto.builder()
                .id("ev-mx-100")
                .sportName("Fútbol")
                .leagueName("Liga MX")
                .homeTeam("América")
                .awayTeam("Guadalajara")
                .isLive(false)
                .markets(List.of(
                        CalienteMarketDto.builder()
                                .name("Resultado del Partido")
                                .outcomes(List.of(
                                        CalienteOutcomeDto.builder().name("América").decimal(1.95).build(),
                                        CalienteOutcomeDto.builder().name("Empate").decimal(3.40).build(),
                                        CalienteOutcomeDto.builder().name("Guadalajara").decimal(3.80).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("caliente", request.getBookmaker());
        assertEquals("América", request.getTeam1());
        assertEquals("Guadalajara", request.getTeam2());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertTrue(request.getRegions().contains(BookmakerRegion.LATAM));

        List<OddItem> odds = request.getOdds();
        assertEquals(3, odds.size());

        OddItem w1 = odds.stream().filter(o -> "América".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.95, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) w1.getBetType()).outcome());

        OddItem draw = odds.stream().filter(o -> "Empate".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(3.40, draw.getValue());
        assertTrue(draw.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) draw.getBetType()).outcome());

        OddItem w2 = odds.stream().filter(o -> "Guadalajara".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(3.80, w2.getValue());
        assertTrue(w2.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) w2.getBetType()).outcome());
    }

    @Test
    void testDoubleChance() {
        CalienteEventDto event = CalienteEventDto.builder()
                .id("ev-mx-101")
                .sportName("Soccer")
                .homeTeam("Cruz Azul")
                .awayTeam("Pumas UNAM")
                .markets(List.of(
                        CalienteMarketDto.builder()
                                .name("Doble Oportunidad")
                                .outcomes(List.of(
                                        CalienteOutcomeDto.builder().name("1X").decimal(1.25).build(),
                                        CalienteOutcomeDto.builder().name("12").decimal(1.30).build(),
                                        CalienteOutcomeDto.builder().name("X2").decimal(1.70).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream().filter(o -> o.getName().equals("1X")).findFirst().orElseThrow();
        assertEquals(1.25, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1X.getBetType()).outcome());

        OddItem dc12 = request.getOdds().stream().filter(o -> o.getName().equals("12")).findFirst().orElseThrow();
        assertEquals(1.30, dc12.getValue());
        assertTrue(dc12.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) dc12.getBetType()).outcome());

        OddItem dcX2 = request.getOdds().stream().filter(o -> o.getName().equals("X2")).findFirst().orElseThrow();
        assertEquals(1.70, dcX2.getValue());
        assertTrue(dcX2.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) dcX2.getBetType()).outcome());
    }

    @Test
    void testTotalOverUnder() {
        CalienteEventDto event = CalienteEventDto.builder()
                .id("ev-mx-102")
                .sportName("Fútbol")
                .homeTeam("Tigres UANL")
                .awayTeam("Monterrey")
                .markets(List.of(
                        CalienteMarketDto.builder()
                                .name("Total de Goles - Altas/Bajas")
                                .outcomes(List.of(
                                        CalienteOutcomeDto.builder().name("Altas 2.5").decimal(1.85).handicap(2.5).build(),
                                        CalienteOutcomeDto.builder().name("Bajas 2.5").decimal(1.95).handicap(2.5).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().stream().filter(o -> o.getName().contains("Altas")).findFirst().orElseThrow();
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet overBet = (TotalBet) over.getBetType();
        assertEquals(TotalBet.Direction.OVER, overBet.direction());
        assertEquals(2.5, overBet.param());

        OddItem under = request.getOdds().stream().filter(o -> o.getName().contains("Bajas")).findFirst().orElseThrow();
        assertTrue(under.getBetType() instanceof TotalBet);
        TotalBet underBet = (TotalBet) under.getBetType();
        assertEquals(TotalBet.Direction.UNDER, underBet.direction());
        assertEquals(2.5, underBet.param());
    }

    @Test
    void testHandicap() {
        CalienteEventDto event = CalienteEventDto.builder()
                .id("ev-mx-103")
                .sportName("Fútbol")
                .homeTeam("Toluca")
                .awayTeam("Pachuca")
                .markets(List.of(
                        CalienteMarketDto.builder()
                                .name("Hándicap Asiático")
                                .outcomes(List.of(
                                        CalienteOutcomeDto.builder().name("Toluca (-0.5)").decimal(1.90).handicap(-0.5).build(),
                                        CalienteOutcomeDto.builder().name("Pachuca (+0.5)").decimal(1.90).handicap(0.5).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().stream().filter(o -> o.getName().contains("Toluca")).findFirst().orElseThrow();
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet h1Bet = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, h1Bet.outcome());
        assertEquals(-0.5, h1Bet.param());

        OddItem h2 = request.getOdds().stream().filter(o -> o.getName().contains("Pachuca")).findFirst().orElseThrow();
        assertTrue(h2.getBetType() instanceof HandicapBet);
        HandicapBet h2Bet = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2Bet.outcome());
        assertEquals(0.5, h2Bet.param());
    }

    @Test
    void testMapHtmlToOddsUpdateRequest() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("12345");
        cache.setSportName("Soccer");
        cache.setLeagueName("Liga MX");
        cache.setTeam1("Atlas");
        cache.setTeam2("Santos Laguna");
        cache.setIsLive(false);

        String html = "<button class=\"price dec\"><span class=\"seln-name\">Atlas</span><span class=\"price dec\">2.15</span></button>" +
                      "<button class=\"price dec\"><span class=\"seln-name\">Empate</span><span class=\"price dec\">3.25</span></button>" +
                      "<button class=\"price dec\"><span class=\"seln-name\">Santos Laguna</span><span class=\"price dec\">3.30</span></button>";

        OddsUpdateRequest request = mapper.mapHtmlToOddsUpdateRequest(cache, html);
        assertNotNull(request);
        assertEquals("caliente", request.getBookmaker());
        assertEquals(3, request.getOdds().size());

        OddItem w1 = request.getOdds().stream().filter(o -> o.getName().equals("Atlas")).findFirst().orElseThrow();
        assertEquals(2.15, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) w1.getBetType()).outcome());
    }
}
