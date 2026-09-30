package pro.datawiki.igaming.source.fanduel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.fanduel.dto.FanDuelEventGroupResponse;
import pro.datawiki.igaming.source.fanduel.service.mapper.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class FanDuelOddsMapperTest {

    private FanDuelOddsMapper oddsMapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;
    private FanDuelScopeResolver scopeResolver;
    private FanDuelStatTypeResolver statTypeResolver;

    @BeforeEach
    void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        Mockito.when(sportNormalizationService.normalize(anyString())).thenAnswer(invocation -> {
            String sport = invocation.getArgument(0);
            if (sport == null) return SportType.FOOTBALL;
            String lower = sport.toLowerCase();
            if (lower.contains("basket")) return SportType.BASKETBALL;
            if (lower.contains("hockey")) return SportType.HOCKEY;
            if (lower.contains("tennis")) return SportType.TENNIS;
            if (lower.contains("esports") || lower.contains("cs2") || lower.contains("dota")) return SportType.ESPORTS;
            return SportType.FOOTBALL;
        });

        scopeResolver = new FanDuelScopeResolver();
        statTypeResolver = new FanDuelStatTypeResolver();

        List<FanDuelMarketHandler> handlers = List.of(
                new FanDuelStatsMarketHandler(),
                new FanDuelEsportsMarketHandler(),
                new FanDuelResultMarketHandler(),
                new FanDuelTotalMarketHandler(),
                new FanDuelHandicapMarketHandler(),
                new FanDuelPropsMarketHandler()
        );

        oddsMapper = new FanDuelOddsMapper(
                unmappedBetService,
                sportNormalizationService,
                betTypeResolver,
                scopeResolver,
                statTypeResolver,
                handlers
        );
    }

    @Test
    @DisplayName("Verify supports() handles fanduel bookmaker correctly")
    void testSupports() {
        assertTrue(oddsMapper.supports("fanduel", SportType.FOOTBALL));
        assertTrue(oddsMapper.supports("FANDUEL", SportType.BASKETBALL));
        assertFalse(oddsMapper.supports("pinnacle", SportType.FOOTBALL));
    }

    @Test
    @DisplayName("Verify American to Decimal odds conversion")
    void testAmericanToDecimalConversion() {
        assertEquals(2.50, FanDuelOddsMapper.americanToDecimal(150), 0.001);
        assertEquals(1.909, FanDuelOddsMapper.americanToDecimal(-110), 0.001);
        assertEquals(2.00, FanDuelOddsMapper.americanToDecimal(100), 0.001);
        assertEquals(1.50, FanDuelOddsMapper.americanToDecimal(-200), 0.001);
        assertEquals(0.0, FanDuelOddsMapper.americanToDecimal(0), 0.001);
        assertEquals(2.50, FanDuelOddsMapper.americanToDecimal("+150"), 0.001);
        assertEquals(1.909, FanDuelOddsMapper.americanToDecimal("-110"), 0.001);
        assertEquals(0.0, FanDuelOddsMapper.americanToDecimal(""), 0.001);
        assertEquals(0.0, FanDuelOddsMapper.americanToDecimal((String) null), 0.001);
    }

    @Test
    @DisplayName("Verify 1X2, Moneyline, Double Chance and Draw No Bet mapping")
    void testResultMarkets() {
        FanDuelEventGroupResponse.FanDuelEvent event = createEvent(1001L, "Arsenal vs Chelsea");
        FanDuelEventGroupResponse response = createResponseWithMarkets(event, List.of(
                createMarketWithRunners("m1", 1001L, "Match Result", List.of(
                        createRunner(1L, "Arsenal", 2.10, null),
                        createRunner(2L, "Draw", 3.40, null),
                        createRunner(3L, "Chelsea", 3.80, null)
                )),
                createMarketWithRunners("m2", 1001L, "Double Chance", List.of(
                        createRunner(4L, "1X", 1.30, null),
                        createRunner(5L, "12", 1.35, null),
                        createRunner(6L, "X2", 1.70, null)
                )),
                createMarketWithRunners("m3", 1001L, "Draw No Bet", List.of(
                        createRunner(7L, "Arsenal", 1.50, null),
                        createRunner(8L, "Chelsea", 2.60, null)
                )),
                createMarketWithRunners("m4", 1001L, "Moneyline", List.of(
                        createRunner(9L, "Arsenal", 2.05, null),
                        createRunner(10L, "Chelsea", 1.85, null)
                ))
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Soccer", "Premier League");

        assertNotNull(request);
        assertEquals("fanduel", request.getBookmaker());
        assertEquals("1001", request.getExternalEventId());
        assertEquals(List.of(BookmakerRegion.US, BookmakerRegion.GLOBAL), request.getRegions());
        assertEquals("Arsenal", request.getTeam1());
        assertEquals("Chelsea", request.getTeam2());

        List<OddItem> odds = request.getOdds();
        assertNotNull(odds);
        assertEquals(10, odds.size());

        // Check 1X2
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.WIN1 && m.scope() == BetScope.FULL_MATCH));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.DRAW));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.WIN2));

        // Check Double Chance
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.DC_1X));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.DC_12));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.DC_X2));

        // Check Draw No Bet
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.WIN1_2WAY));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.WIN2_2WAY));
    }

    @Test
    @DisplayName("Verify Totals mapping (European, Asian, Team Totals)")
    void testTotalMarkets() {
        FanDuelEventGroupResponse.FanDuelEvent event = createEvent(1002L, "Lakers @ Celtics");
        FanDuelEventGroupResponse response = createResponseWithMarkets(event, List.of(
                createMarketWithRunners("m1", 1002L, "Total Points", List.of(
                        createRunner(1L, "Over 215.5", 1.90, 215.5),
                        createRunner(2L, "Under 215.5", 1.90, 215.5)
                )),
                createMarketWithRunners("m2", 1002L, "Over/Under", List.of(
                        createRunner(3L, "Over 2.25", 2.05, 2.25),
                        createRunner(4L, "Under 2.25", 1.80, 2.25)
                )),
                createMarketWithRunners("m3", 1002L, "Lakers Total Points", List.of(
                        createRunner(5L, "Over 108.5", 1.95, 108.5),
                        createRunner(6L, "Under 108.5", 1.85, 108.5)
                )),
                createMarketWithRunners("m4", 1002L, "Celtics Total Points", List.of(
                        createRunner(7L, "Over 110.5", 1.92, 110.5),
                        createRunner(8L, "Under 110.5", 1.88, 110.5)
                ))
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Basketball", "NBA");
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();
        assertEquals(8, odds.size());

        // Full match totals
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.direction() == TotalBet.Direction.OVER
                && t.param() == 215.5
                && !t.isAsian()
                && t.subject() == BetSubject.MATCH));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.direction() == TotalBet.Direction.UNDER
                && t.param() == 215.5));

        // Asian totals (2.25)
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.isAsian()
                && t.param() == 2.25));

        // Team totals
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.subject() == BetSubject.TEAM1
                && t.param() == 108.5));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.subject() == BetSubject.TEAM2
                && t.param() == 110.5));
    }

    @Test
    @DisplayName("Verify Spreads and Handicaps (European, Asian, Scopes)")
    void testHandicapMarkets() {
        FanDuelEventGroupResponse.FanDuelEvent event = createEvent(1003L, "Real Madrid vs Barcelona");
        FanDuelEventGroupResponse response = createResponseWithMarkets(event, List.of(
                createMarketWithRunners("m1", 1003L, "Point Spread", List.of(
                        createRunner(1L, "Real Madrid -1.5", 2.20, -1.5),
                        createRunner(2L, "Barcelona +1.5", 1.70, 1.5)
                )),
                createMarketWithRunners("m2", 1003L, "1st Half Spread", List.of(
                        createRunner(3L, "Real Madrid -0.25", 1.95, -0.25),
                        createRunner(4L, "Barcelona +0.25", 1.85, 0.25)
                ))
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Soccer", "La Liga");
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();
        assertEquals(4, odds.size());

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof HandicapBet h
                && h.outcome() == HandicapBet.Outcome.TEAM1
                && h.param() == -1.5
                && !h.isAsian()
                && h.scope() == BetScope.FULL_MATCH));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof HandicapBet h
                && h.outcome() == HandicapBet.Outcome.TEAM2
                && h.param() == 1.5));

        // Asian quarter line in 1st Half
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof HandicapBet h
                && h.scope() == BetScope.FIRST_HALF
                && h.isAsian()
                && h.param() == -0.25));
    }

    @Test
    @DisplayName("Verify Football Statistics: Corners, Yellow Cards, Cards, Fouls, Offsides, Shots on Target")
    void testStatsMarkets() {
        FanDuelEventGroupResponse.FanDuelEvent event = createEvent(1004L, "Bayern vs Dortmund");
        FanDuelEventGroupResponse response = createResponseWithMarkets(event, List.of(
                createMarketWithRunners("m1", 1004L, "Total Corners", List.of(
                        createRunner(1L, "Over 9.5", 1.85, 9.5),
                        createRunner(2L, "Under 9.5", 1.95, 9.5)
                )),
                createMarketWithRunners("m2", 1004L, "Corner Handicap", List.of(
                        createRunner(3L, "Bayern -2.5", 1.90, -2.5),
                        createRunner(4L, "Dortmund +2.5", 1.90, 2.5)
                )),
                createMarketWithRunners("m3", 1004L, "Total Yellow Cards", List.of(
                        createRunner(5L, "Over 3.5", 1.75, 3.5),
                        createRunner(6L, "Under 3.5", 2.05, 3.5)
                )),
                createMarketWithRunners("m4", 1004L, "Total Cards", List.of(
                        createRunner(7L, "Over 4.5", 1.90, 4.5),
                        createRunner(8L, "Under 4.5", 1.90, 4.5)
                )),
                createMarketWithRunners("m5", 1004L, "Total Fouls", List.of(
                        createRunner(9L, "Over 22.5", 1.80, 22.5),
                        createRunner(10L, "Under 22.5", 2.00, 22.5)
                )),
                createMarketWithRunners("m6", 1004L, "Total Offsides", List.of(
                        createRunner(11L, "Over 3.5", 1.85, 3.5),
                        createRunner(12L, "Under 3.5", 1.95, 3.5)
                )),
                createMarketWithRunners("m7", 1004L, "Total Shots on Target", List.of(
                        createRunner(13L, "Over 8.5", 1.88, 8.5),
                        createRunner(14L, "Under 8.5", 1.92, 8.5)
                ))
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Soccer", "Bundesliga");
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();
        assertEquals(14, odds.size());

        // Corners
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.CORNERS
                && t.param() == 9.5));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof HandicapBet h
                && h.statType() == StatType.CORNERS
                && h.param() == -2.5));

        // Yellow Cards
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.YELLOW_CARDS
                && t.param() == 3.5));

        // Cards
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.CARDS
                && t.param() == 4.5));

        // Fouls
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.FOULS
                && t.param() == 22.5));

        // Offsides
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.OFFSIDES
                && t.param() == 3.5));

        // Shots on Target
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.SHOTS_ON_TARGET
                && t.param() == 8.5));
    }

    @Test
    @DisplayName("Verify Esports Markets: Map Winners, Total Maps, Rounds, Kills, First Blood, Towers, Roshan")
    void testEsportsMarkets() {
        FanDuelEventGroupResponse.FanDuelEvent event = createEvent(1005L, "NaVi vs FaZe");
        FanDuelEventGroupResponse response = createResponseWithMarkets(event, List.of(
                createMarketWithRunners("m1", 1005L, "Map 1 Winner", List.of(
                        createRunner(1L, "NaVi", 1.70, null),
                        createRunner(2L, "FaZe", 2.15, null)
                )),
                createMarketWithRunners("m2", 1005L, "Total Maps", List.of(
                        createRunner(3L, "Over 2.5", 1.95, 2.5),
                        createRunner(4L, "Under 2.5", 1.85, 2.5)
                )),
                createMarketWithRunners("m3", 1005L, "Map 1 Total Rounds", List.of(
                        createRunner(5L, "Over 21.5", 1.80, 21.5),
                        createRunner(6L, "Under 21.5", 2.00, 21.5)
                )),
                createMarketWithRunners("m4", 1005L, "Map 1 First Blood", List.of(
                        createRunner(7L, "NaVi", 1.85, null),
                        createRunner(8L, "FaZe", 1.95, null)
                )),
                createMarketWithRunners("m5", 1005L, "Total Kills", List.of(
                        createRunner(9L, "Over 45.5", 1.90, 45.5),
                        createRunner(10L, "Under 45.5", 1.90, 45.5)
                )),
                createMarketWithRunners("m6", 1005L, "Map 1 Total Towers", List.of(
                        createRunner(11L, "Over 11.5", 1.85, 11.5),
                        createRunner(12L, "Under 11.5", 1.95, 11.5)
                )),
                createMarketWithRunners("m7", 1005L, "Map 1 Total Roshan", List.of(
                        createRunner(13L, "Over 1.5", 1.65, 1.5),
                        createRunner(14L, "Under 1.5", 2.20, 1.5)
                ))
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "CS2", "ESL Pro League");
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();
        assertEquals(14, odds.size());

        // Map 1 Winner
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.scope() == BetScope.MAP_1
                && m.outcome() == MatchResultBet.Outcome.WIN1_2WAY));

        // Total Maps
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.MAPS
                && t.param() == 2.5));

        // Rounds
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.ROUNDS
                && t.scope() == BetScope.MAP_1
                && t.param() == 21.5));

        // First Blood
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet b
                && b.marketType() == BinaryMarketBet.MarketType.FIRST_BLOOD
                && b.outcome() == BinaryMarketBet.Outcome.TEAM1));

        // Kills
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.KILLS
                && t.param() == 45.5));

        // Towers
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.TOWERS
                && t.param() == 11.5));

        // Roshan
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.ROSHAN
                && t.param() == 1.5));
    }

    @Test
    @DisplayName("Verify Props Markets: BTTS, Odd/Even, Correct Score")
    void testPropsMarkets() {
        FanDuelEventGroupResponse.FanDuelEvent event = createEvent(1006L, "PSG vs Marseille");
        FanDuelEventGroupResponse response = createResponseWithMarkets(event, List.of(
                createMarketWithRunners("m1", 1006L, "Both Teams to Score", List.of(
                        createRunner(1L, "Yes", 1.75, null),
                        createRunner(2L, "No", 2.10, null)
                )),
                createMarketWithRunners("m2", 1006L, "Total Points - Odd/Even", List.of(
                        createRunner(3L, "Odd", 1.95, null),
                        createRunner(4L, "Even", 1.85, null)
                )),
                createMarketWithRunners("m3", 1006L, "Correct Score", List.of(
                        createRunner(5L, "2-1", 8.50, null),
                        createRunner(6L, "Any Other", 15.0, null)
                ))
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Soccer", "Ligue 1");
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();
        assertEquals(6, odds.size());

        // BTTS
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet b
                && b.marketType() == BinaryMarketBet.MarketType.BTTS
                && b.outcome() == BinaryMarketBet.Outcome.YES));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet b
                && b.marketType() == BinaryMarketBet.MarketType.BTTS
                && b.outcome() == BinaryMarketBet.Outcome.NO));

        // Odd/Even
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet b
                && b.marketType() == BinaryMarketBet.MarketType.ODD_EVEN
                && b.outcome() == BinaryMarketBet.Outcome.ODD));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet b
                && b.marketType() == BinaryMarketBet.MarketType.ODD_EVEN
                && b.outcome() == BinaryMarketBet.Outcome.EVEN));

        // Correct Score
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof CorrectScoreBet c
                && c.score1() == 2 && c.score2() == 1 && !c.isAnyOtherScore()));

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof CorrectScoreBet c
                && c.isAnyOtherScore()));
    }

    @Test
    @DisplayName("Verify Selections attachment processing and American odds conversion")
    void testSelectionsAttachmentProcessing() {
        FanDuelEventGroupResponse.FanDuelEvent event = createEvent(1007L, "Liverpool vs Man City");
        FanDuelEventGroupResponse response = new FanDuelEventGroupResponse();
        FanDuelEventGroupResponse.Attachments att = new FanDuelEventGroupResponse.Attachments();
        response.setAttachments(att);

        // Market with no runners, selections in att.selections
        FanDuelEventGroupResponse.FanDuelMarket market = new FanDuelEventGroupResponse.FanDuelMarket();
        market.setMarketId("sel_m1");
        market.setEventId(1007L);
        market.setMarketName("Moneyline");
        att.setMarkets(Map.of("sel_m1", market));

        FanDuelEventGroupResponse.FanDuelSelection s1 = new FanDuelEventGroupResponse.FanDuelSelection();
        s1.setSelectionId("s1");
        s1.setMarketId("sel_m1");
        s1.setName("Liverpool");
        FanDuelEventGroupResponse.Price p1 = new FanDuelEventGroupResponse.Price();
        p1.setAmerican(150); // +150 -> 2.50
        s1.setPrice(p1);

        FanDuelEventGroupResponse.FanDuelSelection s2 = new FanDuelEventGroupResponse.FanDuelSelection();
        s2.setSelectionId("s2");
        s2.setMarketId("sel_m1");
        s2.setName("Man City");
        FanDuelEventGroupResponse.Price p2 = new FanDuelEventGroupResponse.Price();
        p2.setDecimal(1.95);
        s2.setPrice(p2);

        att.setSelections(Map.of("s1", s1, "s2", s2));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Soccer", "Premier League");
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();
        assertEquals(2, odds.size());
        assertTrue(odds.stream().anyMatch(o -> "Liverpool".equals(o.getName()) && Math.abs(o.getValue() - 2.50) < 0.001));
        assertTrue(odds.stream().anyMatch(o -> "Man City".equals(o.getName()) && Math.abs(o.getValue() - 1.95) < 0.001));
    }

    @Test
    @DisplayName("Verify Scopes: Halves, Quarters, Periods, Innings, Sets, Maps")
    void testScopes() {
        assertEquals(BetScope.FIRST_HALF, scopeResolver.resolve("1st Half Moneyline"));
        assertEquals(BetScope.SECOND_HALF, scopeResolver.resolve("2nd Half Total Goals"));
        assertEquals(BetScope.QUARTER_1, scopeResolver.resolve("1st Quarter Spread"));
        assertEquals(BetScope.PERIOD_1, scopeResolver.resolve("1st Period Moneyline"));
        assertEquals(BetScope.SET_1, scopeResolver.resolve("Set 1 Winner"));
        assertEquals(BetScope.MAP_2, scopeResolver.resolve("Map 2 Winner"));
        assertEquals(BetScope.FIRST_5_INNINGS, scopeResolver.resolve("First 5 Innings Moneyline"));
        assertEquals(BetScope.FULL_MATCH, scopeResolver.resolve("Moneyline"));
    }

    // Helper methods for building test objects
    private FanDuelEventGroupResponse.FanDuelEvent createEvent(Long id, String name) {
        FanDuelEventGroupResponse.FanDuelEvent event = new FanDuelEventGroupResponse.FanDuelEvent();
        event.setEventId(id);
        event.setName(name);
        event.setOpenDate("2026-10-01T18:00:00Z");
        event.setInPlay(false);
        return event;
    }

    private FanDuelEventGroupResponse createResponseWithMarkets(FanDuelEventGroupResponse.FanDuelEvent event,
                                                                List<FanDuelEventGroupResponse.FanDuelMarket> markets) {
        FanDuelEventGroupResponse response = new FanDuelEventGroupResponse();
        FanDuelEventGroupResponse.Attachments att = new FanDuelEventGroupResponse.Attachments();
        Map<String, FanDuelEventGroupResponse.FanDuelMarket> marketMap = new HashMap<>();
        for (FanDuelEventGroupResponse.FanDuelMarket m : markets) {
            marketMap.put(m.getMarketId(), m);
        }
        att.setMarkets(marketMap);
        response.setAttachments(att);
        return response;
    }

    private FanDuelEventGroupResponse.FanDuelMarket createMarketWithRunners(String marketId,
                                                                           Long eventId,
                                                                           String marketName,
                                                                           List<FanDuelEventGroupResponse.FanDuelRunner> runners) {
        FanDuelEventGroupResponse.FanDuelMarket market = new FanDuelEventGroupResponse.FanDuelMarket();
        market.setMarketId(marketId);
        market.setEventId(eventId);
        market.setMarketName(marketName);
        market.setRunners(runners);
        return market;
    }

    private FanDuelEventGroupResponse.FanDuelRunner createRunner(Long selectionId,
                                                                String runnerName,
                                                                Double decimalOdds,
                                                                Double handicap) {
        FanDuelEventGroupResponse.FanDuelRunner runner = new FanDuelEventGroupResponse.FanDuelRunner();
        runner.setSelectionId(selectionId);
        runner.setRunnerName(runnerName);
        runner.setHandicap(handicap);
        runner.setRunnerStatus("ACTIVE");

        FanDuelEventGroupResponse.WinRunnerOdds wro = new FanDuelEventGroupResponse.WinRunnerOdds();
        FanDuelEventGroupResponse.TrueOdds tro = new FanDuelEventGroupResponse.TrueOdds();
        FanDuelEventGroupResponse.DecimalOdds deco = new FanDuelEventGroupResponse.DecimalOdds();
        deco.setDecimalOdds(decimalOdds);
        tro.setDecimalOdds(deco);
        wro.setTrueOdds(tro);
        runner.setWinRunnerOdds(wro);

        return runner;
    }
}
