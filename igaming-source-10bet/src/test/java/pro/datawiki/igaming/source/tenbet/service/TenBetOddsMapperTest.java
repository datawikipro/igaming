package pro.datawiki.igaming.source.tenbet.service;

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
import pro.datawiki.igaming.source.tenbet.dto.TenBetEventDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetMarketDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TenBetOddsMapperTest {

    private TenBetOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TenBetOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("10bet", SportType.FOOTBALL));
        assertTrue(mapper.supports("10BET", SportType.ESPORTS));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-100")
                .sportName("Football")
                .leagueName("Premier League")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .isLive(false)
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Match Winner 1X2")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Arsenal").decimal(1.85).build(),
                                        TenBetOutcomeDto.builder().name("Draw").decimal(3.60).build(),
                                        TenBetOutcomeDto.builder().name("Chelsea").decimal(4.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("10bet", request.getBookmaker());
        assertEquals("Arsenal", request.getTeam1());
        assertEquals("Chelsea", request.getTeam2());
        assertTrue(request.getRegions().contains(BookmakerRegion.EU));

        List<OddItem> odds = request.getOdds();
        assertEquals(3, odds.size());

        OddItem home = odds.stream().filter(o -> o.getName().equals("Arsenal")).findFirst().orElseThrow();
        assertEquals(1.85, home.getValue());
        assertTrue(home.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) home.getBetType()).outcome());

        OddItem draw = odds.stream().filter(o -> o.getName().equals("Draw")).findFirst().orElseThrow();
        assertEquals(3.60, draw.getValue());
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) draw.getBetType()).outcome());

        OddItem away = odds.stream().filter(o -> o.getName().equals("Chelsea")).findFirst().orElseThrow();
        assertEquals(4.20, away.getValue());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) away.getBetType()).outcome());
    }

    @Test
    void testDoubleChance() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-101")
                .sportName("Football")
                .homeTeam("Liverpool")
                .awayTeam("Man City")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Double Chance")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Liverpool or Draw").decimal(1.40).build(),
                                        TenBetOutcomeDto.builder().name("Liverpool or Man City").decimal(1.30).build(),
                                        TenBetOutcomeDto.builder().name("Draw or Man City").decimal(1.50).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream().filter(o -> o.getName().contains("Draw") && o.getName().contains("Liverpool")).findFirst().orElseThrow();
        assertEquals(1.40, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1X.getBetType()).outcome());

        OddItem dc12 = request.getOdds().stream().filter(o -> o.getName().contains("Liverpool or Man City")).findFirst().orElseThrow();
        assertEquals(1.30, dc12.getValue());
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) dc12.getBetType()).outcome());

        OddItem dcX2 = request.getOdds().stream().filter(o -> o.getName().contains("Draw or Man City")).findFirst().orElseThrow();
        assertEquals(1.50, dcX2.getValue());
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) dcX2.getBetType()).outcome());
    }

    @Test
    void testTotalOverUnder() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-102")
                .sportName("Football")
                .homeTeam("Barcelona")
                .awayTeam("Real Madrid")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Total Goals Over / Under")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 2.5").handicap(2.5).decimal(1.75).build(),
                                        TenBetOutcomeDto.builder().name("Under 2.5").handicap(2.5).decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().stream().filter(o -> o.getName().startsWith("Over")).findFirst().orElseThrow();
        assertEquals(1.75, over.getValue());
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet overBet = (TotalBet) over.getBetType();
        assertEquals(TotalBet.Direction.OVER, overBet.direction());
        assertEquals(2.5, overBet.param());
        assertEquals(BetSubject.MATCH, overBet.subject());

        OddItem under = request.getOdds().stream().filter(o -> o.getName().startsWith("Under")).findFirst().orElseThrow();
        assertEquals(2.10, under.getValue());
        TotalBet underBet = (TotalBet) under.getBetType();
        assertEquals(TotalBet.Direction.UNDER, underBet.direction());
        assertEquals(2.5, underBet.param());
    }

    @Test
    void testTeamTotals() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-103")
                .sportName("Football")
                .homeTeam("Bayern")
                .awayTeam("Dortmund")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Bayern Total Goals")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 1.5").handicap(1.5).decimal(1.60).build(),
                                        TenBetOutcomeDto.builder().name("Under 1.5").handicap(1.5).decimal(2.30).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().get(0);
        TotalBet tb = (TotalBet) over.getBetType();
        assertEquals(BetSubject.TEAM1, tb.subject());
        assertEquals(1.5, tb.param());
    }

    @Test
    void testHandicapSpread() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-104")
                .sportName("Basketball")
                .homeTeam("Lakers")
                .awayTeam("Celtics")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("Point Spread")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Lakers -4.5").handicap(-4.5).decimal(1.90).build(),
                                        TenBetOutcomeDto.builder().name("Celtics +4.5").handicap(4.5).decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().stream().filter(o -> o.getName().contains("Lakers")).findFirst().orElseThrow();
        HandicapBet hb1 = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-4.5, hb1.param());

        OddItem h2 = request.getOdds().stream().filter(o -> o.getName().contains("Celtics")).findFirst().orElseThrow();
        HandicapBet hb2 = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(4.5, hb2.param());
    }

    @Test
    void testFirstHalfScope() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-105")
                .sportName("Football")
                .homeTeam("PSG")
                .awayTeam("Marseille")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("1st Half Total Goals")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Over 1.5").handicap(1.5).decimal(2.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(1, request.getOdds().size());
        TotalBet tb = (TotalBet) request.getOdds().get(0).getBetType();
        assertEquals(BetScope.HALF_1, tb.scope());
    }

    @Test
    void testExtractTeamsFromTitle() {
        TenBetEventDto event = TenBetEventDto.builder()
                .id("ev-106")
                .name("Inter vs Milan")
                .sportName("Soccer")
                .markets(List.of(
                        TenBetMarketDto.builder()
                                .name("1X2")
                                .outcomes(List.of(
                                        TenBetOutcomeDto.builder().name("Inter").decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("Inter", request.getTeam1());
        assertEquals("Milan", request.getTeam2());
        assertEquals(SportType.FOOTBALL, request.getSportType());
    }

    @Test
    void testSportResolution() {
        assertEquals(SportType.FOOTBALL, mapper.resolveSportType("Soccer"));
        assertEquals(SportType.BASKETBALL, mapper.resolveSportType("Basketball"));
        assertEquals(SportType.TENNIS, mapper.resolveSportType("Tennis"));
        assertEquals(SportType.CS2, mapper.resolveSportType("CS2"));
        assertEquals(SportType.DOTA2, mapper.resolveSportType("Dota 2"));
        assertEquals(SportType.LEAGUE_OF_LEGENDS, mapper.resolveSportType("League of Legends"));
        assertEquals(SportType.VALORANT, mapper.resolveSportType("Valorant"));
        assertEquals(SportType.UNKNOWN, mapper.resolveSportType(""));
    }

    @Test
    void testNullAndEmptyHandling() {
        assertNull(mapper.mapToOddsUpdateRequest(null));

        TenBetEventDto emptyEvent = TenBetEventDto.builder().id("ev-null").build();
        assertNull(mapper.mapToOddsUpdateRequest(emptyEvent));

        TenBetEventDto noMarketsEvent = TenBetEventDto.builder()
                .id("ev-empty")
                .homeTeam("Team A")
                .awayTeam("Team B")
                .build();
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(noMarketsEvent);
        assertNotNull(req);
        assertTrue(req.getOdds().isEmpty());
    }
}
