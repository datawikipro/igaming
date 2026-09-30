package pro.datawiki.igaming.source.sbobet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetBttsHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetDoubleChanceHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetDrawNoBetHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetHandicapHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetMoneylineHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetStatsCardsHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetStatsCornersHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetTotalHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SbobetOddsMapperTest {

    private SbobetOddsMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        SportNormalizationService normalizationService = Mockito.mock(SportNormalizationService.class);
        mapper = new SbobetOddsMapper(
                normalizationService,
                List.of(
                        new SbobetMoneylineHandler(),
                        new SbobetHandicapHandler(),
                        new SbobetTotalHandler(),
                        new SbobetDoubleChanceHandler(),
                        new SbobetBttsHandler(),
                        new SbobetDrawNoBetHandler(),
                        new SbobetStatsCornersHandler(),
                        new SbobetStatsCardsHandler()
                )
        );
    }

    @Test
    public void testMapToOddsUpdateRequestWithHalfTime() throws Exception {
        String eventJson = """
            {
                "id": "sb12345",
                "home": "Liverpool",
                "away": "Manchester City",
                "startTime": 1780000000000,
                "isLive": false,
                "moneyline": {
                    "home": 2.10,
                    "draw": 3.40,
                    "away": 3.20
                },
                "moneyline_half1": {
                    "home": 2.70,
                    "draw": 2.10,
                    "away": 3.60,
                    "_isHalf1": true
                },
                "handicaps": [
                    { "hdp": -0.5, "home": 1.95, "away": 1.90, "isHalf1": false }
                ],
                "handicaps_half1": [
                    { "hdp": -0.25, "home": 2.05, "away": 1.82, "isHalf1": true }
                ],
                "totals": [
                    { "limit": 2.5, "over": 1.85, "under": 1.98, "isHalf1": false }
                ],
                "totals_half1": [
                    { "limit": 1.0, "over": 1.90, "under": 1.92, "isHalf1": true }
                ]
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Premier League");

        assertNotNull(req);
        assertEquals("sbobet", req.getBookmaker());
        assertEquals("Liverpool", req.getTeam1());
        assertEquals("Manchester City", req.getTeam2());

        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);
        // 3 ML + 3 ML_1H + 2 HDP + 2 HDP_1H + 2 TOT + 2 TOT_1H = 14 odds
        assertEquals(14, odds.size());

        // Check 1st Half Moneyline Home
        OddItem h1Home = odds.stream().filter(o -> "moneyline_half_1".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(h1Home);
        assertEquals(2.70, h1Home.getValue());
        assertTrue(h1Home.getBetType() instanceof MatchResultBet);
        assertEquals(BetScope.HALF_1, ((MatchResultBet) h1Home.getBetType()).scope());

        // Check 1st Half Handicap Home
        OddItem h1HdpHome = odds.stream().filter(o -> "handicap_half_1".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElse(null);
        assertNotNull(h1HdpHome);
        assertEquals(2.05, h1HdpHome.getValue());
        assertTrue(h1HdpHome.getBetType() instanceof HandicapBet);
        assertEquals(BetScope.HALF_1, ((HandicapBet) h1HdpHome.getBetType()).scope());
        assertEquals(-0.25, ((HandicapBet) h1HdpHome.getBetType()).param());

        // Check 1st Half Total Over
        OddItem h1TotOver = odds.stream().filter(o -> "total_half_1".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(h1TotOver);
        assertEquals(1.90, h1TotOver.getValue());
        assertTrue(h1TotOver.getBetType() instanceof TotalBet);
        assertEquals(BetScope.HALF_1, ((TotalBet) h1TotOver.getBetType()).scope());
        assertEquals(1.0, ((TotalBet) h1TotOver.getBetType()).param());
    }

    @Test
    public void testDoubleChanceFullMatchAndHalf1() throws Exception {
        String eventJson = """
            {
                "id": "sb_dc_1",
                "home": "Real Madrid",
                "away": "Barcelona",
                "double_chance": {
                    "1x": 1.35,
                    "12": 1.28,
                    "x2": 1.65
                },
                "double_chance_half1": {
                    "1x": 1.22,
                    "12": 1.45,
                    "x2": 1.55
                }
            }
            """;
        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "La Liga");

        assertNotNull(req);
        List<OddItem> odds = req.getOdds();
        assertEquals(6, odds.size());

        OddItem dc1x = odds.stream().filter(o -> "double_chance".equals(o.getGroupName()) && "1X".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.35, dc1x.getValue());
        assertTrue(dc1x.getBetType() instanceof MatchResultBet);
        MatchResultBet mrb1x = (MatchResultBet) dc1x.getBetType();
        assertEquals(BetScope.FULL_MATCH, mrb1x.scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, mrb1x.outcome());

        OddItem dc12 = odds.stream().filter(o -> "double_chance".equals(o.getGroupName()) && "12".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.28, dc12.getValue());
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) dc12.getBetType()).outcome());

        OddItem dcX2 = odds.stream().filter(o -> "double_chance".equals(o.getGroupName()) && "X2".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.65, dcX2.getValue());
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) dcX2.getBetType()).outcome());

        OddItem h1Dc1x = odds.stream().filter(o -> "double_chance_half_1".equals(o.getGroupName()) && "1X".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.22, h1Dc1x.getValue());
        assertEquals(BetScope.HALF_1, ((MatchResultBet) h1Dc1x.getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) h1Dc1x.getBetType()).outcome());
    }

    @Test
    public void testBttsFullMatchAndHalf1() throws Exception {
        String eventJson = """
            {
                "id": "sb_btts_1",
                "home": "Arsenal",
                "away": "Chelsea",
                "btts": {
                    "yes": 1.75,
                    "no": 2.05
                },
                "both_teams_to_score_half1": {
                    "yes": 4.10,
                    "no": 1.20
                }
            }
            """;
        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Premier League");

        assertNotNull(req);
        List<OddItem> odds = req.getOdds();
        assertEquals(4, odds.size());

        OddItem bttsYes = odds.stream().filter(o -> "btts".equals(o.getGroupName()) && "YES".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.75, bttsYes.getValue());
        assertTrue(bttsYes.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbYes = (BinaryMarketBet) bttsYes.getBetType();
        assertEquals(BetScope.FULL_MATCH, bmbYes.scope());
        assertEquals(BinaryMarketBet.MarketType.BTTS, bmbYes.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bmbYes.outcome());

        OddItem bttsNo = odds.stream().filter(o -> "btts".equals(o.getGroupName()) && "NO".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(2.05, bttsNo.getValue());
        assertEquals(BinaryMarketBet.Outcome.NO, ((BinaryMarketBet) bttsNo.getBetType()).outcome());

        OddItem h1BttsYes = odds.stream().filter(o -> "btts_half_1".equals(o.getGroupName()) && "YES".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(4.10, h1BttsYes.getValue());
        assertEquals(BetScope.HALF_1, ((BinaryMarketBet) h1BttsYes.getBetType()).scope());
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) h1BttsYes.getBetType()).outcome());
    }

    @Test
    public void testDrawNoBetFullMatchAndHalf1() throws Exception {
        String eventJson = """
            {
                "id": "sb_dnb_1",
                "home": "Bayern Munich",
                "away": "Borussia Dortmund",
                "dnb": {
                    "home": 1.40,
                    "away": 2.85
                },
                "draw_no_bet_half1": {
                    "1": 1.55,
                    "2": 2.45
                }
            }
            """;
        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Bundesliga");

        assertNotNull(req);
        List<OddItem> odds = req.getOdds();
        assertEquals(4, odds.size());

        OddItem dnbHome = odds.stream().filter(o -> "draw_no_bet".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElseThrow();
        assertEquals(1.40, dnbHome.getValue());
        assertTrue(dnbHome.getBetType() instanceof HandicapBet);
        HandicapBet hbHome = (HandicapBet) dnbHome.getBetType();
        assertEquals(BetScope.FULL_MATCH, hbHome.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, hbHome.outcome());
        assertEquals(0.0, hbHome.param());

        OddItem dnbAway = odds.stream().filter(o -> "draw_no_bet".equals(o.getGroupName()) && o.getName().contains("AWAY")).findFirst().orElseThrow();
        assertEquals(2.85, dnbAway.getValue());
        assertEquals(HandicapBet.Outcome.TEAM2, ((HandicapBet) dnbAway.getBetType()).outcome());

        OddItem h1DnbHome = odds.stream().filter(o -> "draw_no_bet_half_1".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElseThrow();
        assertEquals(1.55, h1DnbHome.getValue());
        assertEquals(BetScope.HALF_1, ((HandicapBet) h1DnbHome.getBetType()).scope());
        assertEquals(0.0, ((HandicapBet) h1DnbHome.getBetType()).param());
    }

    @Test
    public void testAbstractBetTypeMapperMethodsForExpandedOutcomes() {
        // Test map() method in SbobetOddsMapper
        BetType btDc1x = mapper.map("double_chance", "1X", null);
        assertNotNull(btDc1x);
        assertTrue(btDc1x instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) btDc1x).outcome());

        BetType btBttsYes = mapper.map("btts", "YES", null);
        assertNotNull(btBttsYes);
        assertTrue(btBttsYes instanceof BinaryMarketBet);
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) btBttsYes).outcome());

        BetType btDnb1 = mapper.map("draw_no_bet", "1", null);
        assertNotNull(btDnb1);
        assertTrue(btDnb1 instanceof HandicapBet);
        assertEquals(HandicapBet.Outcome.TEAM1, ((HandicapBet) btDnb1).outcome());
        assertEquals(0.0, ((HandicapBet) btDnb1).param());

        // Test stats mapping in SbobetOddsMapper.map()
        BetType btCornersTotal = mapper.map("corners_total", "OVER", 9.5);
        assertNotNull(btCornersTotal);
        assertTrue(btCornersTotal instanceof TotalBet);
        TotalBet tbCorners = (TotalBet) btCornersTotal;
        assertEquals(StatType.CORNERS, tbCorners.statType());
        assertEquals(TotalBet.Direction.OVER, tbCorners.direction());
        assertEquals(9.5, tbCorners.param());

        BetType btCardsHandicap = mapper.map("cards_handicap", "HOME", -0.5);
        assertNotNull(btCardsHandicap);
        assertTrue(btCardsHandicap instanceof HandicapBet);
        HandicapBet hbCards = (HandicapBet) btCardsHandicap;
        assertEquals(StatType.YELLOW_CARDS, hbCards.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, hbCards.outcome());
        assertEquals(-0.5, hbCards.param());

        BetType btCornersHalf1 = mapper.map("corners_1x2_half1", "1", null);
        assertNotNull(btCornersHalf1);
        assertTrue(btCornersHalf1 instanceof MatchResultBet);
        MatchResultBet mrbCornersH1 = (MatchResultBet) btCornersHalf1;
        assertEquals(StatType.CORNERS, mrbCornersH1.statType());
        assertEquals(BetScope.HALF_1, mrbCornersH1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mrbCornersH1.outcome());
    }

    @Test
    public void testStatsCornersFullMatchAndHalf1() throws Exception {
        String eventJson = """
            {
                "id": "sb_corners_1",
                "home": "Barcelona",
                "away": "Real Madrid",
                "corners": {
                    "home": 1.80,
                    "draw": 7.50,
                    "away": 2.20,
                    "totals": [
                        { "limit": 9.5, "over": 1.85, "under": 1.95 }
                    ],
                    "handicaps": [
                        { "hdp": -1.5, "home": 2.05, "away": 1.80 }
                    ]
                },
                "corners_half1": {
                    "home": 1.95,
                    "draw": 4.50,
                    "away": 2.40,
                    "totals": [
                        { "limit": 4.5, "over": 1.90, "under": 1.90 }
                    ],
                    "handicaps": [
                        { "hdp": -0.5, "home": 2.10, "away": 1.75 }
                    ]
                }
            }
            """;
        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "La Liga");

        assertNotNull(req);
        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);
        // Full match: 3 1X2 + 2 Totals + 2 Handicaps = 7
        // Half 1: 3 1X2 + 2 Totals + 2 Handicaps = 7
        // Total = 14
        assertEquals(14, odds.size());

        // Check Full Match 1X2 Corners
        OddItem cHome = odds.stream().filter(o -> "corners_moneyline".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.80, cHome.getValue());
        assertTrue(cHome.getBetType() instanceof MatchResultBet);
        MatchResultBet mrbHome = (MatchResultBet) cHome.getBetType();
        assertEquals(BetScope.FULL_MATCH, mrbHome.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mrbHome.outcome());
        assertEquals(StatType.CORNERS, mrbHome.statType());

        // Check Full Match Corners Total
        OddItem cOver = odds.stream().filter(o -> "corners_total".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElseThrow();
        assertEquals(1.85, cOver.getValue());
        assertTrue(cOver.getBetType() instanceof TotalBet);
        TotalBet tbOver = (TotalBet) cOver.getBetType();
        assertEquals(BetScope.FULL_MATCH, tbOver.scope());
        assertEquals(TotalBet.Direction.OVER, tbOver.direction());
        assertEquals(9.5, tbOver.param());
        assertEquals(StatType.CORNERS, tbOver.statType());

        // Check Full Match Corners Handicap
        OddItem cHdp = odds.stream().filter(o -> "corners_handicap".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElseThrow();
        assertEquals(2.05, cHdp.getValue());
        assertTrue(cHdp.getBetType() instanceof HandicapBet);
        HandicapBet hbHome = (HandicapBet) cHdp.getBetType();
        assertEquals(BetScope.FULL_MATCH, hbHome.scope());
        assertEquals(-1.5, hbHome.param());
        assertEquals(StatType.CORNERS, hbHome.statType());

        // Check Half 1 Corners 1X2
        OddItem h1Home = odds.stream().filter(o -> "corners_moneyline_half_1".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.95, h1Home.getValue());
        assertEquals(BetScope.HALF_1, ((MatchResultBet) h1Home.getBetType()).scope());
        assertEquals(StatType.CORNERS, ((MatchResultBet) h1Home.getBetType()).statType());

        // Check Half 1 Corners Total
        OddItem h1Over = odds.stream().filter(o -> "corners_total_half_1".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElseThrow();
        assertEquals(1.90, h1Over.getValue());
        assertEquals(BetScope.HALF_1, ((TotalBet) h1Over.getBetType()).scope());
        assertEquals(4.5, ((TotalBet) h1Over.getBetType()).param());
        assertEquals(StatType.CORNERS, ((TotalBet) h1Over.getBetType()).statType());
    }

    @Test
    public void testStatsCornersDirectAndPrices() throws Exception {
        String eventJson = """
            {
                "id": "sb_corners_direct",
                "home": "Milan",
                "away": "Inter",
                "corners_total": {
                    "limit": 10.5,
                    "prices": [
                        { "designation": "over", "price": 2.10 },
                        { "designation": "under", "price": 1.75 }
                    ]
                },
                "corners_handicap": {
                    "hdp": -0.5,
                    "prices": [
                        { "designation": "home", "price": 1.95 },
                        { "designation": "away", "price": 1.90 }
                    ]
                }
            }
            """;
        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Serie A");

        assertNotNull(req);
        List<OddItem> odds = req.getOdds();
        assertEquals(4, odds.size());

        OddItem totOver = odds.stream().filter(o -> "corners_total".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElseThrow();
        assertEquals(2.10, totOver.getValue());
        assertEquals(StatType.CORNERS, ((TotalBet) totOver.getBetType()).statType());
        assertEquals(10.5, ((TotalBet) totOver.getBetType()).param());

        OddItem hdpHome = odds.stream().filter(o -> "corners_handicap".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElseThrow();
        assertEquals(1.95, hdpHome.getValue());
        assertEquals(StatType.CORNERS, ((HandicapBet) hdpHome.getBetType()).statType());
        assertEquals(-0.5, ((HandicapBet) hdpHome.getBetType()).param());
    }

    @Test
    public void testStatsCornersDoubleChanceAndTeamTotals() throws Exception {
        String eventJson = """
            {
                "id": "sb_corners_dc_tt",
                "home": "Chelsea",
                "away": "Arsenal",
                "corners_double_chance": {
                    "1x": 1.40,
                    "12": 1.25,
                    "x2": 1.60
                },
                "corners_home_totals": [
                    { "limit": 5.5, "over": 1.85, "under": 1.95 }
                ]
            }
            """;
        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Premier League");

        assertNotNull(req);
        List<OddItem> odds = req.getOdds();
        assertEquals(5, odds.size());

        OddItem dc1x = odds.stream().filter(o -> "corners_double_chance".equals(o.getGroupName()) && "1X".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.40, dc1x.getValue());
        assertEquals(StatType.CORNERS, ((MatchResultBet) dc1x.getBetType()).statType());
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1x.getBetType()).outcome());

        OddItem teamTot = odds.stream().filter(o -> o.getGroupName().contains("corners_team1_total") && o.getName().contains("OVER")).findFirst().orElseThrow();
        assertEquals(1.85, teamTot.getValue());
        TotalBet tb = (TotalBet) teamTot.getBetType();
        assertEquals(BetSubject.TEAM1, tb.subject());
        assertEquals(StatType.CORNERS, tb.statType());
        assertEquals(5.5, tb.param());
    }

    @Test
    public void testStatsCardsFullMatchAndHalf1() throws Exception {
        String eventJson = """
            {
                "id": "sb_cards_1",
                "home": "Atletico Madrid",
                "away": "Sevilla",
                "cards": {
                    "home": 2.20,
                    "draw": 4.00,
                    "away": 2.60,
                    "totals": [
                        { "limit": 4.5, "over": 1.75, "under": 2.10 }
                    ],
                    "handicaps": [
                        { "hdp": 0.0, "home": 1.85, "away": 1.95 }
                    ]
                },
                "cards_half1": {
                    "totals": [
                        { "limit": 1.5, "over": 2.00, "under": 1.80 }
                    ]
                }
            }
            """;
        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "La Liga");

        assertNotNull(req);
        List<OddItem> odds = req.getOdds();
        // 3 1X2 + 2 Totals + 2 Handicaps + 2 Totals Half1 = 9 odds
        assertEquals(9, odds.size());

        OddItem cardsHome = odds.stream().filter(o -> "cards_moneyline".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(2.20, cardsHome.getValue());
        assertEquals(StatType.YELLOW_CARDS, ((MatchResultBet) cardsHome.getBetType()).statType());

        OddItem cardsOver = odds.stream().filter(o -> "cards_total".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElseThrow();
        assertEquals(1.75, cardsOver.getValue());
        assertEquals(StatType.YELLOW_CARDS, ((TotalBet) cardsOver.getBetType()).statType());
        assertEquals(4.5, ((TotalBet) cardsOver.getBetType()).param());

        OddItem h1CardsOver = odds.stream().filter(o -> "cards_total_half_1".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElseThrow();
        assertEquals(2.00, h1CardsOver.getValue());
        assertEquals(BetScope.HALF_1, ((TotalBet) h1CardsOver.getBetType()).scope());
        assertEquals(StatType.YELLOW_CARDS, ((TotalBet) h1CardsOver.getBetType()).statType());
        assertEquals(1.5, ((TotalBet) h1CardsOver.getBetType()).param());
    }

    @Test
    public void testStatsCardsDirectAndBookings() throws Exception {
        String eventJson = """
            {
                "id": "sb_bookings_1",
                "home": "Juventus",
                "away": "Roma",
                "bookings_total": {
                    "limit": 3.5,
                    "over": 1.80,
                    "under": 2.00
                },
                "yellow_cards_handicap": {
                    "hdp": -0.5,
                    "home": 2.15,
                    "away": 1.70
                }
            }
            """;
        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Serie A");

        assertNotNull(req);
        List<OddItem> odds = req.getOdds();
        assertEquals(4, odds.size());

        OddItem bookTot = odds.stream().filter(o -> "cards_total".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElseThrow();
        assertEquals(1.80, bookTot.getValue());
        assertEquals(StatType.YELLOW_CARDS, ((TotalBet) bookTot.getBetType()).statType());
        assertEquals(3.5, ((TotalBet) bookTot.getBetType()).param());

        OddItem cardHdp = odds.stream().filter(o -> "cards_handicap".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElseThrow();
        assertEquals(2.15, cardHdp.getValue());
        assertEquals(StatType.YELLOW_CARDS, ((HandicapBet) cardHdp.getBetType()).statType());
        assertEquals(-0.5, ((HandicapBet) cardHdp.getBetType()).param());
    }
}
