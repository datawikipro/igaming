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
        oddsMapper = new PinnacleOddsMapper(normalizationService);
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
        assertEquals(HandicapBet.Team.TEAM1, hb.team());
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
}
