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
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetBttsHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetDoubleChanceHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetDrawNoBetHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetHandicapHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetMoneylineHandler;
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
                        new SbobetDrawNoBetHandler()
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
    }
}
