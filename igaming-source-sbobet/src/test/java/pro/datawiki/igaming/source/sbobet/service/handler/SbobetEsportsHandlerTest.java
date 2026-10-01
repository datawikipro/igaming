package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SbobetEsportsHandlerTest {

    private SbobetEsportsHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        handler = new SbobetEsportsHandler();
    }

    @Test
    public void testSupports() {
        assertTrue(handler.supports("esports"));
        assertTrue(handler.supports("map1"));
        assertTrue(handler.supports("map_2"));
        assertTrue(handler.supports("map3_winner"));
        assertTrue(handler.supports("maps_total"));
        assertTrue(handler.supports("maps_handicap"));
        assertTrue(handler.supports("map1_rounds_total"));
        assertTrue(handler.supports("map2_rounds_handicap"));
        assertTrue(handler.supports("rounds_total"));
        assertTrue(handler.supports("rounds_handicap"));

        assertFalse(handler.supports("id"));
        assertFalse(handler.supports("home"));
        assertFalse(handler.supports("moneyline"));
        assertFalse(handler.supports((String) null));

        // With sportType
        assertTrue(handler.supports("winner", SportType.CS2));
        assertTrue(handler.supports("match_winner", SportType.DOTA2));
        assertFalse(handler.supports("id", SportType.CS2));
        assertFalse(handler.supports("home", SportType.CS2));
        assertFalse(handler.supports("winner", SportType.FOOTBALL));
    }

    @Test
    public void testMapWinnersDirectAndNested() throws Exception {
        String json = """
            {
                "home": 1.75,
                "away": 2.05
            }
            """;
        JsonNode node = objectMapper.readTree(json);
        List<OddItem> items = new ArrayList<>();
        handler.handle("map1", node, SportType.CS2, items);

        assertEquals(2, items.size());
        OddItem home = items.stream().filter(i -> i.getName().equals("HOME")).findFirst().orElseThrow();
        assertEquals(1.75, home.getValue());
        assertEquals("map1_winner", home.getGroupName());
        assertTrue(home.getBetType() instanceof MatchResultBet);
        MatchResultBet mrbHome = (MatchResultBet) home.getBetType();
        assertEquals(BetScope.MAP_1, mrbHome.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, mrbHome.outcome());
        assertEquals(StatType.MATCH, mrbHome.statType());

        OddItem away = items.stream().filter(i -> i.getName().equals("AWAY")).findFirst().orElseThrow();
        assertEquals(2.05, away.getValue());
        MatchResultBet mrbAway = (MatchResultBet) away.getBetType();
        assertEquals(BetScope.MAP_1, mrbAway.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, mrbAway.outcome());
    }

    @Test
    public void testMapWinnersWithPricesArray() throws Exception {
        String json = """
            {
                "winner": {
                    "prices": [
                        { "designation": "home", "price": 1.62 },
                        { "designation": "away", "price": 2.28 }
                    ]
                }
            }
            """;
        JsonNode node = objectMapper.readTree(json);
        List<OddItem> items = new ArrayList<>();
        handler.handle("map2_winner", node, SportType.DOTA2, items);

        assertEquals(2, items.size());
        OddItem home = items.stream().filter(i -> i.getName().equals("HOME")).findFirst().orElseThrow();
        assertEquals(1.62, home.getValue());
        assertEquals("map2_winner", home.getGroupName());
        assertEquals(BetScope.MAP_2, ((MatchResultBet) home.getBetType()).scope());

        OddItem away = items.stream().filter(i -> i.getName().equals("AWAY")).findFirst().orElseThrow();
        assertEquals(2.28, away.getValue());
        assertEquals(BetScope.MAP_2, ((MatchResultBet) away.getBetType()).scope());
    }

    @Test
    public void testMapsTotalAndHandicap() throws Exception {
        String totalJson = """
            [
                { "limit": 2.5, "over": 1.85, "under": 1.95 }
            ]
            """;
        JsonNode totalNode = objectMapper.readTree(totalJson);
        List<OddItem> items = new ArrayList<>();
        handler.handle("maps_total", totalNode, SportType.CS2, items);

        assertEquals(2, items.size());
        OddItem over = items.stream().filter(i -> i.getName().contains("OVER")).findFirst().orElseThrow();
        assertEquals(1.85, over.getValue());
        assertEquals("maps_total", over.getGroupName());
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) over.getBetType();
        assertEquals(BetScope.FULL_MATCH, tb.scope());
        assertEquals(StatType.MAPS, tb.statType());
        assertEquals(2.5, tb.param());
        assertEquals(TotalBet.Direction.OVER, tb.direction());

        OddItem under = items.stream().filter(i -> i.getName().contains("UNDER")).findFirst().orElseThrow();
        assertEquals(1.95, under.getValue());
        assertEquals(TotalBet.Direction.UNDER, ((TotalBet) under.getBetType()).direction());

        // Test Maps Handicap
        String hdpJson = """
            {
                "hdp": -1.5,
                "home": 2.25,
                "away": 1.60
            }
            """;
        JsonNode hdpNode = objectMapper.readTree(hdpJson);
        items.clear();
        handler.handle("maps_handicap", hdpNode, SportType.CS2, items);

        assertEquals(2, items.size());
        OddItem homeHdp = items.stream().filter(i -> i.getName().contains("HOME")).findFirst().orElseThrow();
        assertEquals(2.25, homeHdp.getValue());
        assertEquals("maps_handicap", homeHdp.getGroupName());
        assertTrue(homeHdp.getBetType() instanceof HandicapBet);
        HandicapBet hb = (HandicapBet) homeHdp.getBetType();
        assertEquals(BetScope.FULL_MATCH, hb.scope());
        assertEquals(StatType.MAPS, hb.statType());
        assertEquals(-1.5, hb.param());
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());

        OddItem awayHdp = items.stream().filter(i -> i.getName().contains("AWAY")).findFirst().orElseThrow();
        assertEquals(1.60, awayHdp.getValue());
        assertEquals(1.5, ((HandicapBet) awayHdp.getBetType()).param());
        assertEquals(HandicapBet.Outcome.TEAM2, ((HandicapBet) awayHdp.getBetType()).outcome());
    }

    @Test
    public void testMapRoundsTotalAndHandicap() throws Exception {
        String json = """
            {
                "totals": [
                    { "limit": 26.5, "over": 1.90, "under": 1.90 }
                ],
                "handicaps": [
                    { "hdp": -2.5, "home": 1.80, "away": 2.00 }
                ]
            }
            """;
        JsonNode node = objectMapper.readTree(json);
        List<OddItem> items = new ArrayList<>();
        handler.handle("map1", node, SportType.CS2, items);

        // 2 rounds totals + 2 rounds handicaps = 4 items
        assertEquals(4, items.size());

        OddItem rTotOver = items.stream().filter(i -> i.getGroupName().equals("map1_rounds_total") && i.getName().contains("OVER")).findFirst().orElseThrow();
        assertEquals(1.90, rTotOver.getValue());
        assertTrue(rTotOver.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) rTotOver.getBetType();
        assertEquals(BetScope.MAP_1, tb.scope());
        assertEquals(StatType.ROUNDS, tb.statType());
        assertEquals(26.5, tb.param());

        OddItem rHdpHome = items.stream().filter(i -> i.getGroupName().equals("map1_rounds_handicap") && i.getName().contains("HOME")).findFirst().orElseThrow();
        assertEquals(1.80, rHdpHome.getValue());
        assertTrue(rHdpHome.getBetType() instanceof HandicapBet);
        HandicapBet hb = (HandicapBet) rHdpHome.getBetType();
        assertEquals(BetScope.MAP_1, hb.scope());
        assertEquals(StatType.ROUNDS, hb.statType());
        assertEquals(-2.5, hb.param());
    }

    @Test
    public void testEsportsContainerNode() throws Exception {
        String json = """
            {
                "map1": {
                    "home": 1.55,
                    "away": 2.45
                },
                "map2": {
                    "home": 1.85,
                    "away": 1.95
                },
                "maps_total": [
                    { "limit": 2.5, "over": 1.90, "under": 1.90 }
                ],
                "maps_handicap": [
                    { "hdp": -1.5, "home": 2.40, "away": 1.55 }
                ]
            }
            """;
        JsonNode node = objectMapper.readTree(json);
        List<OddItem> items = new ArrayList<>();
        handler.handle("esports", node, SportType.CS2, items);

        // 2 (map1) + 2 (map2) + 2 (maps_total) + 2 (maps_handicap) = 8 items
        assertEquals(8, items.size());

        assertTrue(items.stream().anyMatch(i -> i.getGroupName().equals("map1_winner") && i.getName().equals("HOME")));
        assertTrue(items.stream().anyMatch(i -> i.getGroupName().equals("map2_winner") && i.getName().equals("AWAY")));
        assertTrue(items.stream().anyMatch(i -> i.getGroupName().equals("maps_total") && i.getName().contains("OVER")));
        assertTrue(items.stream().anyMatch(i -> i.getGroupName().equals("maps_handicap") && i.getName().contains("HOME")));
    }
}
