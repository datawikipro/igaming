package pro.datawiki.igaming.source.sbobet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetBttsHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetCorrectScoreHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetDoubleChanceHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetDrawNoBetHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetEsportsHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetHandicapHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetMoneylineHandler;
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetStatsHandler;
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
                        new SbobetStatsHandler(),
                        new SbobetEsportsHandler(),
                        new SbobetDoubleChanceHandler(),
                        new SbobetDrawNoBetHandler(),
                        new SbobetBttsHandler(),
                        new SbobetCorrectScoreHandler(),
                        new SbobetTotalHandler(),
                        new SbobetHandicapHandler(),
                        new SbobetMoneylineHandler()
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
    public void testDoubleChanceHandler() throws Exception {
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
                "double_chance_half1": [
                    { "name": "1X", "odds": 1.20 },
                    { "name": "12", "odds": 1.45 },
                    { "name": "X2", "odds": 1.50 }
                ]
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "La Liga");

        List<OddItem> odds = req.getOdds();
        assertEquals(6, odds.size());

        // Full match DC
        OddItem dc1X = odds.stream().filter(o -> "double_chance".equals(o.getGroupName()) && "1X".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(dc1X);
        assertEquals(1.35, dc1X.getValue());
        MatchResultBet bet1X = (MatchResultBet) dc1X.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1X.scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, bet1X.outcome());

        OddItem dc12 = odds.stream().filter(o -> "double_chance".equals(o.getGroupName()) && "12".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(dc12);
        assertEquals(1.28, dc12.getValue());
        MatchResultBet bet12 = (MatchResultBet) dc12.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet12.scope());
        assertEquals(MatchResultBet.Outcome.DC_12, bet12.outcome());

        OddItem dcX2 = odds.stream().filter(o -> "double_chance".equals(o.getGroupName()) && "X2".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(dcX2);
        assertEquals(1.65, dcX2.getValue());
        MatchResultBet betX2 = (MatchResultBet) dcX2.getBetType();
        assertEquals(BetScope.FULL_MATCH, betX2.scope());
        assertEquals(MatchResultBet.Outcome.DC_X2, betX2.outcome());

        // Half 1 DC
        OddItem h1Dc1X = odds.stream().filter(o -> "double_chance_half_1".equals(o.getGroupName()) && "1X".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(h1Dc1X);
        assertEquals(1.20, h1Dc1X.getValue());
        MatchResultBet h1Bet1X = (MatchResultBet) h1Dc1X.getBetType();
        assertEquals(BetScope.HALF_1, h1Bet1X.scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, h1Bet1X.outcome());

        OddItem h1DcX2 = odds.stream().filter(o -> "double_chance_half_1".equals(o.getGroupName()) && "X2".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(h1DcX2);
        assertEquals(1.50, h1DcX2.getValue());
        MatchResultBet h1BetX2 = (MatchResultBet) h1DcX2.getBetType();
        assertEquals(BetScope.HALF_1, h1BetX2.scope());
        assertEquals(MatchResultBet.Outcome.DC_X2, h1BetX2.outcome());
    }

    @Test
    public void testDrawNoBetHandler() throws Exception {
        String eventJson = """
            {
                "id": "sb_dnb_1",
                "home": "Arsenal",
                "away": "Chelsea",
                "draw_no_bet": {
                    "home": 1.55,
                    "away": 2.45
                },
                "draw_no_bet_half1": [
                    { "name": "HOME", "odds": 1.60 },
                    { "name": "AWAY", "odds": 2.30 }
                ]
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Premier League");

        List<OddItem> odds = req.getOdds();
        assertEquals(4, odds.size());

        // Full match DNB
        OddItem dnbHome = odds.stream().filter(o -> "draw_no_bet".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(dnbHome);
        assertEquals(1.55, dnbHome.getValue());
        MatchResultBet betHome = (MatchResultBet) dnbHome.getBetType();
        assertEquals(BetScope.FULL_MATCH, betHome.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, betHome.outcome());

        OddItem dnbAway = odds.stream().filter(o -> "draw_no_bet".equals(o.getGroupName()) && "AWAY".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(dnbAway);
        assertEquals(2.45, dnbAway.getValue());
        MatchResultBet betAway = (MatchResultBet) dnbAway.getBetType();
        assertEquals(BetScope.FULL_MATCH, betAway.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, betAway.outcome());

        // Half 1 DNB
        OddItem h1DnbHome = odds.stream().filter(o -> "draw_no_bet_half_1".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(h1DnbHome);
        assertEquals(1.60, h1DnbHome.getValue());
        MatchResultBet h1BetHome = (MatchResultBet) h1DnbHome.getBetType();
        assertEquals(BetScope.HALF_1, h1BetHome.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, h1BetHome.outcome());
    }

    @Test
    public void testBttsHandler() throws Exception {
        String eventJson = """
            {
                "id": "sb_btts_1",
                "home": "Bayern",
                "away": "Dortmund",
                "btts": {
                    "yes": 1.52,
                    "no": 2.40
                },
                "btts_half1": [
                    { "name": "YES", "value": 3.80 },
                    { "name": "NO", "value": 1.25 }
                ]
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Bundesliga");

        List<OddItem> odds = req.getOdds();
        assertEquals(4, odds.size());

        // Full match BTTS
        OddItem bttsYes = odds.stream().filter(o -> "btts".equals(o.getGroupName()) && "YES".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(bttsYes);
        assertEquals(1.52, bttsYes.getValue());
        BinaryMarketBet betYes = (BinaryMarketBet) bttsYes.getBetType();
        assertEquals(BetScope.FULL_MATCH, betYes.scope());
        assertEquals(BinaryMarketBet.MarketType.BTTS, betYes.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, betYes.outcome());

        OddItem bttsNo = odds.stream().filter(o -> "btts".equals(o.getGroupName()) && "NO".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(bttsNo);
        assertEquals(2.40, bttsNo.getValue());
        BinaryMarketBet betNo = (BinaryMarketBet) bttsNo.getBetType();
        assertEquals(BetScope.FULL_MATCH, betNo.scope());
        assertEquals(BinaryMarketBet.Outcome.NO, betNo.outcome());

        // Half 1 BTTS
        OddItem h1BttsYes = odds.stream().filter(o -> "btts_half_1".equals(o.getGroupName()) && "YES".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(h1BttsYes);
        assertEquals(3.80, h1BttsYes.getValue());
        BinaryMarketBet h1BetYes = (BinaryMarketBet) h1BttsYes.getBetType();
        assertEquals(BetScope.HALF_1, h1BetYes.scope());

        OddItem h1BttsNo = odds.stream().filter(o -> "btts_half_1".equals(o.getGroupName()) && "NO".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(h1BttsNo);
        assertEquals(1.25, h1BttsNo.getValue());
        BinaryMarketBet h1BetNo = (BinaryMarketBet) h1BttsNo.getBetType();
        assertEquals(BetScope.HALF_1, h1BetNo.scope());
    }

    @Test
    public void testCorrectScoreHandler() throws Exception {
        String eventJson = """
            {
                "id": "sb_cs_1",
                "home": "PSG",
                "away": "Marseille",
                "correct_score": {
                    "1-0": 7.50,
                    "2-1": 8.00,
                    "any other score": 4.50
                },
                "correct_score_half1": [
                    { "score": "0-0", "odds": 2.80 },
                    { "name": "1-0", "odds": 3.50 },
                    { "name": "OTHER", "odds": 6.00 }
                ]
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Ligue 1");

        List<OddItem> odds = req.getOdds();
        assertEquals(6, odds.size());

        // Full match 1-0
        OddItem cs10 = odds.stream().filter(o -> "correct_score".equals(o.getGroupName()) && "1-0".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(cs10);
        assertEquals(7.50, cs10.getValue());
        CorrectScoreBet bet10 = (CorrectScoreBet) cs10.getBetType();
        assertEquals(BetScope.FULL_MATCH, bet10.scope());
        assertEquals(1, bet10.score1());
        assertEquals(0, bet10.score2());
        assertFalse(bet10.isAnyOtherScore());

        // Full match Any Other
        OddItem csOther = odds.stream().filter(o -> "correct_score".equals(o.getGroupName()) && "ANY_OTHER".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(csOther);
        assertEquals(4.50, csOther.getValue());
        CorrectScoreBet betOther = (CorrectScoreBet) csOther.getBetType();
        assertEquals(BetScope.FULL_MATCH, betOther.scope());
        assertTrue(betOther.isAnyOtherScore());

        // Half 1 0-0
        OddItem h1Cs00 = odds.stream().filter(o -> "correct_score_half_1".equals(o.getGroupName()) && "0-0".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(h1Cs00);
        assertEquals(2.80, h1Cs00.getValue());
        CorrectScoreBet h1Bet00 = (CorrectScoreBet) h1Cs00.getBetType();
        assertEquals(BetScope.HALF_1, h1Bet00.scope());
        assertEquals(0, h1Bet00.score1());
        assertEquals(0, h1Bet00.score2());

        // Half 1 Other
        OddItem h1CsOther = odds.stream().filter(o -> "correct_score_half_1".equals(o.getGroupName()) && "ANY_OTHER".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(h1CsOther);
        assertEquals(6.00, h1CsOther.getValue());
        CorrectScoreBet h1BetOther = (CorrectScoreBet) h1CsOther.getBetType();
        assertEquals(BetScope.HALF_1, h1BetOther.scope());
        assertTrue(h1BetOther.isAnyOtherScore());
    }
}
