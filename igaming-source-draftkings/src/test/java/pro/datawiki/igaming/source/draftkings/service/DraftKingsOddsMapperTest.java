package pro.datawiki.igaming.source.draftkings.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.draftkings.dto.DraftKingsEventGroupResponse;
import pro.datawiki.igaming.source.draftkings.service.mapper.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class DraftKingsOddsMapperTest {

    private DraftKingsOddsMapper oddsMapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;
    private DraftKingsScopeResolver scopeResolver;
    private DraftKingsStatTypeResolver statTypeResolver;

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

        scopeResolver = new DraftKingsScopeResolver();
        statTypeResolver = new DraftKingsStatTypeResolver();

        List<DraftKingsMarketHandler> handlers = List.of(
                new DraftKingsStatsMarketHandler(),
                new DraftKingsEsportsMarketHandler(),
                new DraftKingsResultMarketHandler(),
                new DraftKingsTotalMarketHandler(),
                new DraftKingsHandicapMarketHandler(),
                new DraftKingsPropsMarketHandler()
        );

        oddsMapper = new DraftKingsOddsMapper(
                unmappedBetService,
                sportNormalizationService,
                betTypeResolver,
                scopeResolver,
                statTypeResolver,
                handlers
        );
    }

    @Test
    @DisplayName("Verify supports() handles draftkings bookmaker correctly")
    void testSupports() {
        assertTrue(oddsMapper.supports("draftkings", SportType.FOOTBALL));
        assertTrue(oddsMapper.supports("DRAFTKINGS", SportType.BASKETBALL));
        assertFalse(oddsMapper.supports("pinnacle", SportType.FOOTBALL));
    }

    @Test
    @DisplayName("Verify American to Decimal odds conversion")
    void testAmericanToDecimalConversion() {
        assertEquals(2.50, DraftKingsOddsMapper.americanToDecimal("+150"), 0.001);
        assertEquals(1.909, DraftKingsOddsMapper.americanToDecimal("-110"), 0.001);
        assertEquals(2.00, DraftKingsOddsMapper.americanToDecimal("+100"), 0.001);
        assertEquals(1.50, DraftKingsOddsMapper.americanToDecimal("-200"), 0.001);
        assertEquals(0.0, DraftKingsOddsMapper.americanToDecimal(""), 0.001);
        assertEquals(0.0, DraftKingsOddsMapper.americanToDecimal(null), 0.001);
    }

    @Test
    @DisplayName("Verify 1X2, Moneyline, Double Chance and Draw No Bet mapping")
    void testResultMarkets() {
        DraftKingsEventGroupResponse.DraftKingsEvent event = createEvent("1001", "Arsenal vs Chelsea", "Arsenal", "Chelsea");
        DraftKingsEventGroupResponse response = new DraftKingsEventGroupResponse();
        DraftKingsEventGroupResponse.DraftKingsEventGroup eg = new DraftKingsEventGroupResponse.DraftKingsEventGroup();
        response.setEventGroup(eg);

        List<DraftKingsEventGroupResponse.DraftKingsOfferCategory> categories = new ArrayList<>();
        DraftKingsEventGroupResponse.DraftKingsOfferCategory cat = new DraftKingsEventGroupResponse.DraftKingsOfferCategory();
        cat.setName("Main Markets");
        categories.add(cat);
        eg.setOfferCategories(categories);

        List<DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor> descriptors = new ArrayList<>();
        cat.setOfferSubcategoryDescriptors(descriptors);

        // 1. 3-Way Match Result (1X2)
        addMarket(descriptors, "1001", "off-1", "Match Result (3-Way)", List.of(
                createOutcome("Arsenal", "+120", 2.20, null, null),
                createOutcome("Draw", "+240", 3.40, null, null),
                createOutcome("Chelsea", "+210", 3.10, null, null)
        ));

        // 2. Double Chance
        addMarket(descriptors, "1001", "off-2", "Double Chance", List.of(
                createOutcome("Arsenal or Draw", "-250", 1.40, null, null),
                createOutcome("Arsenal or Chelsea", "-300", 1.33, null, null),
                createOutcome("Draw or Chelsea", "-175", 1.57, null, null)
        ));

        // 3. Draw No Bet
        addMarket(descriptors, "1001", "off-3", "Draw No Bet", List.of(
                createOutcome("Arsenal", "-160", 1.625, null, null),
                createOutcome("Chelsea", "+130", 2.30, null, null)
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Soccer", "Premier League");
        assertNotNull(request);
        assertEquals("draftkings", request.getBookmaker());
        assertEquals("Arsenal", request.getTeam1());
        assertEquals("Chelsea", request.getTeam2());

        List<OddItem> odds = request.getOdds();
        assertEquals(8, odds.size());

        // Assert 1X2
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.WIN1));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.DRAW));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.WIN2));

        // Assert Double Chance
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.DC_1X));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.DC_12));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.DC_X2));

        // Assert Draw No Bet
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.WIN1_2WAY));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.outcome() == MatchResultBet.Outcome.WIN2_2WAY));
    }

    @Test
    @DisplayName("Verify Totals and Team Totals with European and Asian quarter lines")
    void testTotals() {
        DraftKingsEventGroupResponse.DraftKingsEvent event = createEvent("1002", "Boston Celtics @ LA Lakers", "LA Lakers", "Boston Celtics");
        DraftKingsEventGroupResponse response = new DraftKingsEventGroupResponse();
        DraftKingsEventGroupResponse.DraftKingsEventGroup eg = new DraftKingsEventGroupResponse.DraftKingsEventGroup();
        response.setEventGroup(eg);

        List<DraftKingsEventGroupResponse.DraftKingsOfferCategory> categories = new ArrayList<>();
        DraftKingsEventGroupResponse.DraftKingsOfferCategory cat = new DraftKingsEventGroupResponse.DraftKingsOfferCategory();
        cat.setName("Totals");
        categories.add(cat);
        eg.setOfferCategories(categories);

        List<DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor> descriptors = new ArrayList<>();
        cat.setOfferSubcategoryDescriptors(descriptors);

        // Match Total (Half line 215.5)
        addMarket(descriptors, "1002", "off-total", "Total Points", List.of(
                createOutcome(null, "-110", 1.909, "Over", "215.5"),
                createOutcome(null, "-110", 1.909, "Under", "215.5")
        ));

        // Asian quarter line Total (2.25)
        addMarket(descriptors, "1002", "off-asian", "Match Total Goals", List.of(
                createOutcome(null, "-105", 1.95, "Over", "2.25"),
                createOutcome(null, "-115", 1.87, "Under", "2.25")
        ));

        // Team 1 Total
        addMarket(descriptors, "1002", "off-team1", "LA Lakers Team Total", List.of(
                createOutcome(null, "-115", 1.87, "Over", "110.5"),
                createOutcome(null, "-105", 1.95, "Under", "110.5")
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Basketball", "NBA");
        assertNotNull(request);

        List<OddItem> odds = request.getOdds();
        assertEquals(6, odds.size());

        // Verify standard total
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.direction() == TotalBet.Direction.OVER
                && t.param() == 215.5
                && !t.isAsian()
                && t.subject() == BetSubject.MATCH));

        // Verify Asian quarter total
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.direction() == TotalBet.Direction.OVER
                && t.param() == 2.25
                && t.isAsian()
                && t.subject() == BetSubject.MATCH));

        // Verify Team 1 total
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.direction() == TotalBet.Direction.OVER
                && t.param() == 110.5
                && t.subject() == BetSubject.TEAM1));
    }

    @Test
    @DisplayName("Verify Handicaps: Spreads, Puck Line, Run Line")
    void testHandicaps() {
        DraftKingsEventGroupResponse.DraftKingsEvent event = createEvent("1003", "New York Rangers vs Boston Bruins", "New York Rangers", "Boston Bruins");
        DraftKingsEventGroupResponse response = new DraftKingsEventGroupResponse();
        DraftKingsEventGroupResponse.DraftKingsEventGroup eg = new DraftKingsEventGroupResponse.DraftKingsEventGroup();
        response.setEventGroup(eg);

        List<DraftKingsEventGroupResponse.DraftKingsOfferCategory> categories = new ArrayList<>();
        DraftKingsEventGroupResponse.DraftKingsOfferCategory cat = new DraftKingsEventGroupResponse.DraftKingsOfferCategory();
        cat.setName("Spreads");
        categories.add(cat);
        eg.setOfferCategories(categories);

        List<DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor> descriptors = new ArrayList<>();
        cat.setOfferSubcategoryDescriptors(descriptors);

        // Puck Line
        addMarket(descriptors, "1003", "off-puck", "Puck Line Handicap", List.of(
                createOutcome("New York Rangers", "-110", 1.909, null, "-1.5"),
                createOutcome("Boston Bruins", "-110", 1.909, null, "+1.5")
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Ice Hockey", "NHL");
        assertNotNull(request);

        List<OddItem> odds = request.getOdds();
        assertEquals(2, odds.size());

        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof HandicapBet h
                && h.outcome() == HandicapBet.Outcome.TEAM1
                && h.param() == -1.5));
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof HandicapBet h
                && h.outcome() == HandicapBet.Outcome.TEAM2
                && h.param() == 1.5));
    }

    @Test
    @DisplayName("Verify Statistical Markers: CORNERS, YELLOW_CARDS, CARDS, OFFSIDES, FOULS, SHOTS_ON_TARGET")
    void testStatsMarkets() {
        DraftKingsEventGroupResponse.DraftKingsEvent event = createEvent("1004", "Liverpool vs Manchester City", "Liverpool", "Manchester City");
        DraftKingsEventGroupResponse response = new DraftKingsEventGroupResponse();
        DraftKingsEventGroupResponse.DraftKingsEventGroup eg = new DraftKingsEventGroupResponse.DraftKingsEventGroup();
        response.setEventGroup(eg);

        List<DraftKingsEventGroupResponse.DraftKingsOfferCategory> categories = new ArrayList<>();
        DraftKingsEventGroupResponse.DraftKingsOfferCategory cat = new DraftKingsEventGroupResponse.DraftKingsOfferCategory();
        cat.setName("Corners & Cards");
        categories.add(cat);
        eg.setOfferCategories(categories);

        List<DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor> descriptors = new ArrayList<>();
        cat.setOfferSubcategoryDescriptors(descriptors);

        // Corners Total
        addMarket(descriptors, "1004", "off-c1", "Total Corners", List.of(
                createOutcome(null, "-120", 1.833, "Over", "9.5"),
                createOutcome(null, "-110", 1.909, "Under", "9.5")
        ));

        // Corners 1X2
        addMarket(descriptors, "1004", "off-c2", "Corners Match Result", List.of(
                createOutcome("Liverpool", "+110", 2.10, null, null),
                createOutcome("Draw", "+400", 5.00, null, null),
                createOutcome("Manchester City", "+130", 2.30, null, null)
        ));

        // Yellow Cards Total
        addMarket(descriptors, "1004", "off-yc", "Total Yellow Cards", List.of(
                createOutcome(null, "-105", 1.95, "Over", "3.5"),
                createOutcome(null, "-125", 1.80, "Under", "3.5")
        ));

        // Cards Total
        addMarket(descriptors, "1004", "off-cards", "Total Booking Points / Cards", List.of(
                createOutcome(null, "-110", 1.909, "Over", "4.5"),
                createOutcome(null, "-110", 1.909, "Under", "4.5")
        ));

        // Fouls Total
        addMarket(descriptors, "1004", "off-fouls", "Total Fouls", List.of(
                createOutcome(null, "-115", 1.87, "Over", "22.5"),
                createOutcome(null, "-115", 1.87, "Under", "22.5")
        ));

        // Offsides Total
        addMarket(descriptors, "1004", "off-off", "Total Offsides", List.of(
                createOutcome(null, "-110", 1.909, "Over", "3.5"),
                createOutcome(null, "-120", 1.833, "Under", "3.5")
        ));

        // Shots on Target
        addMarket(descriptors, "1004", "off-sot", "Total Shots On Target", List.of(
                createOutcome(null, "-110", 1.909, "Over", "8.5"),
                createOutcome(null, "-110", 1.909, "Under", "8.5")
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Soccer", "EPL");
        assertNotNull(request);

        List<OddItem> odds = request.getOdds();
        assertEquals(15, odds.size());

        // Verify Corners Total
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.CORNERS
                && t.param() == 9.5));

        // Verify Corners 1X2
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.statType() == StatType.CORNERS
                && m.outcome() == MatchResultBet.Outcome.WIN1));

        // Verify Yellow Cards
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.YELLOW_CARDS
                && t.param() == 3.5));

        // Verify Cards
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.CARDS
                && t.param() == 4.5));

        // Verify Fouls
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.FOULS
                && t.param() == 22.5));

        // Verify Offsides
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.OFFSIDES
                && t.param() == 3.5));

        // Verify Shots On Target
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.SHOTS_ON_TARGET
                && t.param() == 8.5));
    }

    @Test
    @DisplayName("Verify Esports: MAPS, ROUNDS, KILLS, FIRST_BLOOD, TOWERS, ROSHAN")
    void testEsportsMarkets() {
        DraftKingsEventGroupResponse.DraftKingsEvent event = createEvent("1005", "Natus Vincere vs FaZe Clan", "Natus Vincere", "FaZe Clan");
        DraftKingsEventGroupResponse response = new DraftKingsEventGroupResponse();
        DraftKingsEventGroupResponse.DraftKingsEventGroup eg = new DraftKingsEventGroupResponse.DraftKingsEventGroup();
        response.setEventGroup(eg);

        List<DraftKingsEventGroupResponse.DraftKingsOfferCategory> categories = new ArrayList<>();
        DraftKingsEventGroupResponse.DraftKingsOfferCategory cat = new DraftKingsEventGroupResponse.DraftKingsOfferCategory();
        cat.setName("Esports CS2");
        categories.add(cat);
        eg.setOfferCategories(categories);

        List<DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor> descriptors = new ArrayList<>();
        cat.setOfferSubcategoryDescriptors(descriptors);

        // 1. Map 1 Winner
        addMarket(descriptors, "1005", "off-m1w", "Map 1 Winner 2-Way", List.of(
                createOutcome("Natus Vincere", "-150", 1.667, null, null),
                createOutcome("FaZe Clan", "+120", 2.20, null, null)
        ));

        // 2. Total Maps
        addMarket(descriptors, "1005", "off-tm", "Total Maps", List.of(
                createOutcome(null, "-110", 1.909, "Over", "2.5"),
                createOutcome(null, "-110", 1.909, "Under", "2.5")
        ));

        // 3. Map Handicap
        addMarket(descriptors, "1005", "off-mh", "Map Handicap", List.of(
                createOutcome("Natus Vincere", "-120", 1.833, null, "-1.5"),
                createOutcome("FaZe Clan", "+100", 2.00, null, "+1.5")
        ));

        // 4. Total Rounds
        addMarket(descriptors, "1005", "off-tr", "Total Rounds Map 1", List.of(
                createOutcome(null, "-115", 1.87, "Over", "21.5"),
                createOutcome(null, "-115", 1.87, "Under", "21.5")
        ));

        // 5. Total Kills
        addMarket(descriptors, "1005", "off-tk", "Total Kills Map 1", List.of(
                createOutcome(null, "-110", 1.909, "Over", "48.5"),
                createOutcome(null, "-110", 1.909, "Under", "48.5")
        ));

        // 6. First Blood
        addMarket(descriptors, "1005", "off-fb", "First Blood Map 1", List.of(
                createOutcome("Natus Vincere", "-110", 1.909, null, null),
                createOutcome("FaZe Clan", "-110", 1.909, null, null)
        ));

        // 7. Towers (Dota2 / LoL)
        addMarket(descriptors, "1005", "off-tow", "Total Towers Map 1", List.of(
                createOutcome(null, "-110", 1.909, "Over", "12.5"),
                createOutcome(null, "-110", 1.909, "Under", "12.5")
        ));

        // 8. Roshan
        addMarket(descriptors, "1005", "off-rosh", "First Roshan Slay", List.of(
                createOutcome("Natus Vincere", "-130", 1.769, null, null),
                createOutcome("FaZe Clan", "+100", 2.00, null, null)
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Esports", "CS2 Major");
        assertNotNull(request);

        List<OddItem> odds = request.getOdds();
        assertEquals(16, odds.size());

        // Assert Map 1 Winner
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.scope() == BetScope.MAP_1
                && m.outcome() == MatchResultBet.Outcome.WIN1_2WAY));

        // Assert Total Maps
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.MAPS
                && t.param() == 2.5));

        // Assert Map Handicap
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof HandicapBet h
                && h.statType() == StatType.MAPS
                && h.param() == -1.5));

        // Assert Total Rounds
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.ROUNDS
                && t.param() == 21.5));

        // Assert Total Kills
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.KILLS
                && t.param() == 48.5));

        // Assert First Blood
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet b
                && b.marketType() == BinaryMarketBet.MarketType.FIRST_BLOOD
                && b.outcome() == BinaryMarketBet.Outcome.TEAM1));

        // Assert Towers
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof TotalBet t
                && t.statType() == StatType.TOWERS
                && t.param() == 12.5));

        // Assert Roshan
        assertTrue(odds.stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet m
                && m.statType() == StatType.ROSHAN
                && m.outcome() == MatchResultBet.Outcome.WIN1));
    }

    @Test
    @DisplayName("Verify Props: BTTS, Odd/Even, Correct Score")
    void testPropsMarkets() {
        DraftKingsEventGroupResponse.DraftKingsEvent event = createEvent("1006", "Real Madrid vs Barcelona", "Real Madrid", "Barcelona");
        DraftKingsEventGroupResponse response = new DraftKingsEventGroupResponse();
        DraftKingsEventGroupResponse.DraftKingsEventGroup eg = new DraftKingsEventGroupResponse.DraftKingsEventGroup();
        response.setEventGroup(eg);

        List<DraftKingsEventGroupResponse.DraftKingsOfferCategory> categories = new ArrayList<>();
        DraftKingsEventGroupResponse.DraftKingsOfferCategory cat = new DraftKingsEventGroupResponse.DraftKingsOfferCategory();
        cat.setName("Game Props");
        categories.add(cat);
        eg.setOfferCategories(categories);

        List<DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor> descriptors = new ArrayList<>();
        cat.setOfferSubcategoryDescriptors(descriptors);

        // BTTS
        addMarket(descriptors, "1006", "off-btts", "Both Teams to Score", List.of(
                createOutcome("Yes", "-140", 1.714, null, null),
                createOutcome("No", "+110", 2.10, null, null)
        ));

        // Odd/Even
        addMarket(descriptors, "1006", "off-oe", "Total Goals Odd/Even", List.of(
                createOutcome("Odd", "-105", 1.95, null, null),
                createOutcome("Even", "-115", 1.87, null, null)
        ));

        // Correct Score
        addMarket(descriptors, "1006", "off-cs", "Correct Score", List.of(
                createOutcome("2 - 1", "+750", 8.50, null, null),
                createOutcome("1 - 1", "+600", 7.00, null, null),
                createOutcome("Any Other Score", "+1500", 16.00, null, null)
        ));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, "Soccer", "La Liga");
        assertNotNull(request);

        List<OddItem> odds = request.getOdds();
        assertEquals(7, odds.size());

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
    @DisplayName("Verify Scopes: Halves, Quarters, Periods, Innings, Sets")
    void testScopes() {
        assertEquals(BetScope.FIRST_HALF, scopeResolver.resolve("1st Half", "Moneyline"));
        assertEquals(BetScope.SECOND_HALF, scopeResolver.resolve("Main", "2nd Half Total Goals"));
        assertEquals(BetScope.QUARTER_1, scopeResolver.resolve("Quarters", "1st Quarter Spread"));
        assertEquals(BetScope.PERIOD_1, scopeResolver.resolve("Periods", "1st Period Moneyline"));
        assertEquals(BetScope.SET_1, scopeResolver.resolve("Sets", "Set 1 Winner"));
        assertEquals(BetScope.MAP_2, scopeResolver.resolve("Maps", "Map 2 Winner"));
        assertEquals(BetScope.FIRST_5_INNINGS, scopeResolver.resolve("Innings", "First 5 Innings Moneyline"));
        assertEquals(BetScope.FULL_MATCH, scopeResolver.resolve("Main", "Moneyline"));
    }

    // Helper methods
    private DraftKingsEventGroupResponse.DraftKingsEvent createEvent(String eventId, String name, String team1, String team2) {
        DraftKingsEventGroupResponse.DraftKingsEvent event = new DraftKingsEventGroupResponse.DraftKingsEvent();
        event.setEventId(eventId);
        event.setName(name);
        event.setTeamName1(team1);
        event.setTeamName2(team2);
        event.setStartDate("2026-10-01T18:00:00Z");
        event.setEventStatus("Pre-Event");
        return event;
    }

    private DraftKingsEventGroupResponse.DraftKingsOutcome createOutcome(String participant, String american, double decimal, String label, String line) {
        DraftKingsEventGroupResponse.DraftKingsOutcome outcome = new DraftKingsEventGroupResponse.DraftKingsOutcome();
        outcome.setParticipant(participant);
        outcome.setOddsAmerican(american);
        outcome.setOddsDecimal(decimal);
        outcome.setLabel(label);
        outcome.setLine(line);
        return outcome;
    }

    private void addMarket(List<DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor> descriptors,
                           String eventId,
                           String offerId,
                           String marketName,
                           List<DraftKingsEventGroupResponse.DraftKingsOutcome> outcomes) {
        DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor desc = new DraftKingsEventGroupResponse.DraftKingsOfferSubcategoryDescriptor();
        desc.setName(marketName);

        DraftKingsEventGroupResponse.DraftKingsOfferSubcategory subcat = new DraftKingsEventGroupResponse.DraftKingsOfferSubcategory();
        DraftKingsEventGroupResponse.DraftKingsOffer offer = new DraftKingsEventGroupResponse.DraftKingsOffer();
        offer.setEventId(eventId);
        offer.setOfferId(offerId);
        offer.setOutcomes(outcomes);

        subcat.setOffers(List.of(List.of(offer)));
        desc.setOfferSubcategory(subcat);
        descriptors.add(desc);
    }
}
