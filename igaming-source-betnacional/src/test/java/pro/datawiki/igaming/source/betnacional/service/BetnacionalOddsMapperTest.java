package pro.datawiki.igaming.source.betnacional.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalEventDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalMarketDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetnacionalOddsMapperTest {

    private BetnacionalOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BetnacionalOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("betnacional", SportType.FOOTBALL));
        assertTrue(mapper.supports("BETNACIONAL", SportType.ESPORTS));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-100")
                .sportName("Futebol")
                .leagueName("Brasileirão Série A")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .isLive(false)
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Resultado Final")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Flamengo").decimal(2.10).build(),
                                        BetnacionalOutcomeDto.builder().name("Empate").decimal(3.30).build(),
                                        BetnacionalOutcomeDto.builder().name("Palmeiras").decimal(3.40).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("betnacional", request.getBookmaker());
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
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-101")
                .sportName("Soccer")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Dupla Chance")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("1X").decimal(1.30).build(),
                                        BetnacionalOutcomeDto.builder().name("12").decimal(1.35).build(),
                                        BetnacionalOutcomeDto.builder().name("X2").decimal(1.65).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream().filter(o -> o.getName().equals("1X")).findFirst().orElseThrow();
        assertEquals(1.30, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1X.getBetType()).outcome());

        OddItem dc12 = request.getOdds().stream().filter(o -> o.getName().equals("12")).findFirst().orElseThrow();
        assertEquals(1.35, dc12.getValue());
        assertTrue(dc12.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) dc12.getBetType()).outcome());

        OddItem dcX2 = request.getOdds().stream().filter(o -> o.getName().equals("X2")).findFirst().orElseThrow();
        assertEquals(1.65, dcX2.getValue());
        assertTrue(dcX2.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) dcX2.getBetType()).outcome());
    }

    @Test
    void testTotalOverUnder() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-102")
                .sportName("Futebol")
                .homeTeam("Liverpool")
                .awayTeam("Man City")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Total de Gols - Mais/Menos")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 2.5").decimal(1.80).handicap(2.5).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 2.5").decimal(2.00).handicap(2.5).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().stream().filter(o -> o.getName().contains("Mais")).findFirst().orElseThrow();
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet overBet = (TotalBet) over.getBetType();
        assertEquals(TotalBet.Direction.OVER, overBet.direction());
        assertEquals(2.5, overBet.param());

        OddItem under = request.getOdds().stream().filter(o -> o.getName().contains("Menos")).findFirst().orElseThrow();
        assertTrue(under.getBetType() instanceof TotalBet);
        TotalBet underBet = (TotalBet) under.getBetType();
        assertEquals(TotalBet.Direction.UNDER, underBet.direction());
        assertEquals(2.5, underBet.param());
    }

    @Test
    void testHandicap() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-103")
                .sportName("Futebol")
                .homeTeam("Inter")
                .awayTeam("Milan")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Handicap Asiatico")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Inter (-0.5)").decimal(1.95).handicap(-0.5).build(),
                                        BetnacionalOutcomeDto.builder().name("Milan (+0.5)").decimal(1.85).handicap(0.5).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().stream().filter(o -> o.getName().contains("Inter")).findFirst().orElseThrow();
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet h1Bet = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, h1Bet.outcome());
        assertEquals(-0.5, h1Bet.param());

        OddItem h2 = request.getOdds().stream().filter(o -> o.getName().contains("Milan")).findFirst().orElseThrow();
        assertTrue(h2.getBetType() instanceof HandicapBet);
        HandicapBet h2Bet = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2Bet.outcome());
        assertEquals(0.5, h2Bet.param());
    }
}
