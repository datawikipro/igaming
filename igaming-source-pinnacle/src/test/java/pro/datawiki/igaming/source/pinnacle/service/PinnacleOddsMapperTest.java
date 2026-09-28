package pro.datawiki.igaming.source.pinnacle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

public class PinnacleOddsMapperTest {

    private PinnacleOddsMapper oddsMapper;
    private SportNormalizationService normalizationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        normalizationService = Mockito.mock(SportNormalizationService.class);
        Mockito.when(normalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);
        pro.datawiki.igaming.source.core.sport.SportRegistry sportRegistry = new pro.datawiki.igaming.source.core.sport.SportRegistry(
                List.of(
                        new pro.datawiki.igaming.source.core.sport.FootballSport(),
                        new pro.datawiki.igaming.source.core.sport.HockeySport(),
                        new pro.datawiki.igaming.source.core.sport.BasketballSport(),
                        new pro.datawiki.igaming.source.core.sport.TennisSport()
                )
        );
        List<pro.datawiki.igaming.source.pinnacle.service.handler.PinnacleMarketHandler> handlers = List.of(
                new pro.datawiki.igaming.source.pinnacle.service.handler.PinnacleMoneylineHandler(),
                new pro.datawiki.igaming.source.pinnacle.service.handler.PinnacleSpreadHandler(),
                new pro.datawiki.igaming.source.pinnacle.service.handler.PinnacleTotalHandler(),
                new pro.datawiki.igaming.source.pinnacle.service.handler.PinnacleTeamTotalHandler(),
                new pro.datawiki.igaming.source.pinnacle.service.handler.PinnacleDoubleChanceHandler(),
                new pro.datawiki.igaming.source.pinnacle.service.handler.PinnacleBttsHandler(),
                new pro.datawiki.igaming.source.pinnacle.service.handler.PinnacleDrawNoBetHandler()
        );
        oddsMapper = new PinnacleOddsMapper(normalizationService, sportRegistry, handlers);
    }

    @Test
    public void testMapToOddsUpdateRequest() throws Exception {
        // Construct mock Pinnacle fixture JSON node
        String fixtureJson = """
            {
                "id": "16182903",
                "home": "Real Madrid",
                "away": "Barcelona",
                "status": "O",
                "starts": "2026-06-01T20:00:00Z"
            }
            """;
        JsonNode fixture = objectMapper.readTree(fixtureJson);

        // Construct mock Pinnacle odds JSON node (including moneyline, spreads, totals)
        String oddsJson = """
            {
                "id": "16182903",
                "periods": [
                    {
                        "number": 0,
                        "lineId": 998822,
                        "moneyline": {
                            "home": 1.95,
                            "away": 3.60,
                            "draw": 3.40
                        },
                        "spreads": [
                            {
                                "hdp": -0.5,
                                "home": 1.95,
                                "away": 1.88
                            }
                        ],
                        "totals": [
                            {
                                "points": 2.5,
                                "over": 1.90,
                                "under": 1.92
                            }
                        ]
                    }
                ]
            }
            """;
        JsonNode odds = objectMapper.readTree(oddsJson);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(fixture, odds, "Soccer", SportType.FOOTBALL, "La Liga");

        assertNotNull(request);
        assertEquals("pinnacle", request.getBookmaker());
        assertEquals("16182903", request.getExternalEventId());
        assertEquals("Soccer", request.getSportName());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertEquals("La Liga", request.getLeagueName());
        assertEquals("Real Madrid", request.getTeam1());
        assertEquals("Barcelona", request.getTeam2());
        assertFalse(request.getIsLive());
        assertEquals(1780344000000L, request.getStartTime()); // 2026-06-01T20:00:00Z in epoch millis is 1780344000000

        List<OddItem> oddItems = request.getOdds();
        assertNotNull(oddItems);
        // Expecting: 3 moneyline, 2 spreads, 2 totals = 7 odds
        assertEquals(7, oddItems.size());

        // Validate Moneyline (HOME)
        OddItem homeMl = oddItems.stream()
                .filter(item -> "moneyline".equals(item.getGroupName()) && "HOME".equals(item.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(homeMl);
        assertEquals(1.95, homeMl.getValue());
        assertTrue(homeMl.getBetType() instanceof MatchResultBet);
        MatchResultBet mrb = (MatchResultBet) homeMl.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, mrb.outcome());

        // Validate Spread (HOME -0.5)
        OddItem homeHdp = oddItems.stream()
                .filter(item -> "spread".equals(item.getGroupName()) && item.getName().contains("HOME"))
                .findFirst()
                .orElse(null);
        assertNotNull(homeHdp);
        assertEquals(1.95, homeHdp.getValue());
        assertTrue(homeHdp.getBetType() instanceof HandicapBet);
        HandicapBet hb = (HandicapBet) homeHdp.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());
        assertEquals(-0.5, hb.param());

        // Validate Total (OVER 2.5)
        OddItem overTotal = oddItems.stream()
                .filter(item -> "total".equals(item.getGroupName()) && item.getName().contains("OVER"))
                .findFirst()
                .orElse(null);
        assertNotNull(overTotal);
        assertEquals(1.90, overTotal.getValue());
        assertTrue(overTotal.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) overTotal.getBetType();
        assertEquals(TotalBet.Direction.OVER, tb.direction());
        assertEquals(2.5, tb.param());
    }

    @Test
    public void testAmericanToDecimal() {
        assertEquals(2.0, PinnacleOddsMapper.americanToDecimal(100));
        assertEquals(2.33, PinnacleOddsMapper.americanToDecimal(133));
        assertEquals(3.74, PinnacleOddsMapper.americanToDecimal(274));
        assertEquals(1.741, PinnacleOddsMapper.americanToDecimal(-135));
        assertEquals(1.962, PinnacleOddsMapper.americanToDecimal(-104));
        assertEquals(1.893, PinnacleOddsMapper.americanToDecimal(-112));
    }

    @Test
    public void testMapArcadiaToOddsUpdateRequest() throws Exception {
        String matchupJson = """
            {
                "id": 1635228938,
                "type": "matchup",
                "isLive": false,
                "startTime": "2026-09-17T19:00:00Z",
                "league": {
                    "id": 2630,
                    "name": "UEFA - Europa League"
                },
                "participants": [
                    { "alignment": "home", "name": "Celtic" },
                    { "alignment": "away", "name": "Ferencvaros" }
                ]
            }
            """;
        JsonNode matchup = objectMapper.readTree(matchupJson);

        String marketsJson = """
            [
                {
                    "type": "moneyline",
                    "period": 0,
                    "prices": [
                        { "designation": "home", "price": -135 },
                        { "designation": "away", "price": 330 },
                        { "designation": "draw", "price": 303 }
                    ]
                },
                {
                    "type": "spread",
                    "period": 0,
                    "prices": [
                        { "designation": "home", "points": -0.75, "price": -104 },
                        { "designation": "away", "points": 0.75, "price": -112 }
                    ]
                },
                {
                    "type": "total",
                    "period": 0,
                    "prices": [
                        { "designation": "over", "points": 3.0, "price": -113 },
                        { "designation": "under", "points": 3.0, "price": -105 }
                    ]
                }
            ]
            """;
        JsonNode marketsNode = objectMapper.readTree(marketsJson);
        List<JsonNode> markets = new java.util.ArrayList<>();
        marketsNode.forEach(markets::add);

        OddsUpdateRequest request = oddsMapper.mapArcadiaToOddsUpdateRequest(matchup, markets, "Soccer", SportType.FOOTBALL);

        assertNotNull(request);
        assertEquals("pinnacle", request.getBookmaker());
        assertEquals("1635228938", request.getExternalEventId());
        assertEquals("Soccer", request.getSportName());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertEquals("UEFA - Europa League", request.getLeagueName());
        assertEquals("Celtic", request.getTeam1());
        assertEquals("Ferencvaros", request.getTeam2());
        assertFalse(request.getIsLive());
        assertTrue(request.getStartTime() > 0);

        List<OddItem> oddItems = request.getOdds();
        assertNotNull(oddItems);
        assertEquals(7, oddItems.size());

        // Check Celtic home moneyline (-135 -> 1.741)
        OddItem homeMl = oddItems.stream()
                .filter(item -> "moneyline".equals(item.getGroupName()) && "HOME".equals(item.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(homeMl);
        assertEquals(1.741, homeMl.getValue());
        assertTrue(homeMl.getBetType() instanceof MatchResultBet);

        // Check Away moneyline (+330 -> 4.3)
        OddItem awayMl = oddItems.stream()
                .filter(item -> "moneyline".equals(item.getGroupName()) && "AWAY".equals(item.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(awayMl);
        assertEquals(4.30, awayMl.getValue());

        // Check Total OVER 3.0 (-113 -> 1.885)
        OddItem overTotal = oddItems.stream()
                .filter(item -> "total".equals(item.getGroupName()) && item.getName().contains("OVER"))
                .findFirst()
                .orElse(null);
        assertNotNull(overTotal);
        assertEquals(1.885, overTotal.getValue());
        assertTrue(overTotal.getBetType() instanceof TotalBet);
    }

    @Test
    public void testMapArcadiaWithHalfTimeAndBtts() throws Exception {
        String matchupJson = """
            {
                "id": 998877,
                "type": "matchup",
                "isLive": false,
                "startTime": "2026-09-17T19:00:00Z",
                "league": { "id": 100, "name": "Premier League" },
                "participants": [
                    { "alignment": "home", "name": "Arsenal" },
                    { "alignment": "away", "name": "Chelsea" }
                ]
            }
            """;
        JsonNode matchup = objectMapper.readTree(matchupJson);

        String marketsJson = """
            [
                {
                    "type": "moneyline",
                    "period": 1,
                    "prices": [
                        { "designation": "home", "price": 150 },
                        { "designation": "away", "price": 280 },
                        { "designation": "draw", "price": 110 }
                    ]
                },
                {
                    "type": "both_teams_to_score",
                    "period": 0,
                    "prices": [
                        { "designation": "yes", "price": -110 },
                        { "designation": "no", "price": -110 }
                    ]
                },
                {
                    "type": "double_chance",
                    "period": 0,
                    "prices": [
                        { "designation": "home_draw", "price": -300 },
                        { "designation": "home_away", "price": -250 },
                        { "designation": "draw_away", "price": 180 }
                    ]
                }
            ]
            """;
        JsonNode marketsNode = objectMapper.readTree(marketsJson);
        List<JsonNode> markets = new java.util.ArrayList<>();
        marketsNode.forEach(markets::add);

        OddsUpdateRequest request = oddsMapper.mapArcadiaToOddsUpdateRequest(matchup, markets, "Soccer", SportType.FOOTBALL);
        assertNotNull(request);
        List<OddItem> odds = request.getOdds();
        assertEquals(8, odds.size());

        // Verify 1st Half Moneyline
        OddItem h1Home = odds.stream().filter(o -> "moneyline_half_1".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(h1Home);
        assertEquals(2.5, h1Home.getValue());
        assertTrue(h1Home.getBetType() instanceof MatchResultBet);
        MatchResultBet mrb = (MatchResultBet) h1Home.getBetType();
        assertEquals(pro.datawiki.igaming.dto.market.BetScope.HALF_1, mrb.scope());

        // Verify BTTS
        OddItem bttsYes = odds.stream().filter(o -> "btts".equals(o.getGroupName()) && "YES".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(bttsYes);
        assertTrue(bttsYes.getBetType() instanceof pro.datawiki.igaming.dto.market.BinaryMarketBet);
        pro.datawiki.igaming.dto.market.BinaryMarketBet bmb = (pro.datawiki.igaming.dto.market.BinaryMarketBet) bttsYes.getBetType();
        assertEquals(pro.datawiki.igaming.dto.market.BinaryMarketBet.MarketType.BTTS, bmb.marketType());
        assertEquals(pro.datawiki.igaming.dto.market.BinaryMarketBet.Outcome.YES, bmb.outcome());

        // Verify Double Chance
        OddItem dc1x = odds.stream().filter(o -> "double_chance".equals(o.getGroupName()) && "1X".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(dc1x);
        assertTrue(dc1x.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1x.getBetType()).outcome());
    }
}

