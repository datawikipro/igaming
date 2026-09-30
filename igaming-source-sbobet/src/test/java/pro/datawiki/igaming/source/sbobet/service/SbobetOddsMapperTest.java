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
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
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
import pro.datawiki.igaming.source.sbobet.service.handler.SbobetMarketHandler;

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

    @Test
    public void testCornersStatisticsHandler() throws Exception {
        String eventJson = """
            {
                "id": "sb_corners_1",
                "home": "Arsenal",
                "away": "Tottenham",
                "corners_total": [
                    { "limit": 9.5, "over": 1.85, "under": 1.95 }
                ],
                "corners_half1_total": {
                    "limit": 4.5,
                    "over": 1.90,
                    "under": 1.90
                },
                "corners_handicap": [
                    { "hdp": -1.5, "home": 1.95, "away": 1.85 }
                ],
                "corners_half1_handicap": {
                    "hdp": -0.5,
                    "home": 2.00,
                    "away": 1.80
                },
                "corners_1x2": {
                    "home": 2.10,
                    "draw": 7.50,
                    "away": 1.80
                },
                "corners_half1_1x2": [
                    { "name": "HOME", "odds": 2.20 },
                    { "name": "DRAW", "odds": 4.00 },
                    { "name": "AWAY", "odds": 2.50 }
                ]
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Premier League");

        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);

        // Check Corners Total Full Match
        OddItem cOver = odds.stream().filter(o -> "corners_total".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(cOver);
        assertEquals(1.85, cOver.getValue());
        assertTrue(cOver.getBetType() instanceof TotalBet);
        TotalBet betCOver = (TotalBet) cOver.getBetType();
        assertEquals(BetScope.FULL_MATCH, betCOver.scope());
        assertEquals(9.5, betCOver.param());
        assertEquals(StatType.CORNERS, betCOver.statType());

        // Check Corners Total Half 1
        OddItem cH1Over = odds.stream().filter(o -> "corners_total_half_1".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(cH1Over);
        assertEquals(1.90, cH1Over.getValue());
        TotalBet betCH1Over = (TotalBet) cH1Over.getBetType();
        assertEquals(BetScope.HALF_1, betCH1Over.scope());
        assertEquals(4.5, betCH1Over.param());
        assertEquals(StatType.CORNERS, betCH1Over.statType());

        // Check Corners Handicap Full Match
        OddItem cHdpHome = odds.stream().filter(o -> "corners_handicap".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElse(null);
        assertNotNull(cHdpHome);
        assertEquals(1.95, cHdpHome.getValue());
        assertTrue(cHdpHome.getBetType() instanceof HandicapBet);
        HandicapBet betCHdp = (HandicapBet) cHdpHome.getBetType();
        assertEquals(BetScope.FULL_MATCH, betCHdp.scope());
        assertEquals(-1.5, betCHdp.param());
        assertEquals(StatType.CORNERS, betCHdp.statType());

        // Check Corners Handicap Half 1
        OddItem cH1HdpHome = odds.stream().filter(o -> "corners_handicap_half_1".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElse(null);
        assertNotNull(cH1HdpHome);
        assertEquals(2.00, cH1HdpHome.getValue());
        HandicapBet betCH1Hdp = (HandicapBet) cH1HdpHome.getBetType();
        assertEquals(BetScope.HALF_1, betCH1Hdp.scope());
        assertEquals(-0.5, betCH1Hdp.param());
        assertEquals(StatType.CORNERS, betCH1Hdp.statType());

        // Check Corners 1X2 Full Match
        OddItem c1x2Draw = odds.stream().filter(o -> "corners_1x2".equals(o.getGroupName()) && "DRAW".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(c1x2Draw);
        assertEquals(7.50, c1x2Draw.getValue());
        assertTrue(c1x2Draw.getBetType() instanceof MatchResultBet);
        MatchResultBet betC1x2Draw = (MatchResultBet) c1x2Draw.getBetType();
        assertEquals(BetScope.FULL_MATCH, betC1x2Draw.scope());
        assertEquals(MatchResultBet.Outcome.DRAW, betC1x2Draw.outcome());
        assertEquals(StatType.CORNERS, betC1x2Draw.statType());

        // Check Corners 1X2 Half 1
        OddItem c1x2H1Home = odds.stream().filter(o -> "corners_1x2_half_1".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(c1x2H1Home);
        assertEquals(2.20, c1x2H1Home.getValue());
        MatchResultBet betC1x2H1Home = (MatchResultBet) c1x2H1Home.getBetType();
        assertEquals(BetScope.HALF_1, betC1x2H1Home.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, betC1x2H1Home.outcome());
        assertEquals(StatType.CORNERS, betC1x2H1Home.statType());
    }

    @Test
    public void testYellowCardsAndBookingsStatisticsHandler() throws Exception {
        String eventJson = """
            {
                "id": "sb_cards_1",
                "home": "Atletico Madrid",
                "away": "Sevilla",
                "yellow_cards_total": {
                    "limit": 3.5,
                    "over": 1.75,
                    "under": 2.05
                },
                "yellow_cards_half1_total": [
                    { "limit": 1.5, "over": 1.80, "under": 1.95, "isHalf1": true }
                ],
                "yellow_cards_handicap": {
                    "hdp": 0.0,
                    "home": 1.85,
                    "away": 1.95
                },
                "yellow_cards_1x2": {
                    "home": 2.20,
                    "draw": 4.50,
                    "away": 2.40
                },
                "cards_total": [
                    { "limit": 4.5, "over": 1.90, "under": 1.90 }
                ],
                "cards_1x2": {
                    "home": 2.00,
                    "away": 1.80
                }
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "La Liga");

        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);

        // Yellow cards total
        OddItem yTot = odds.stream().filter(o -> "yellow_cards_total".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(yTot);
        assertEquals(1.75, yTot.getValue());
        TotalBet betYTot = (TotalBet) yTot.getBetType();
        assertEquals(StatType.YELLOW_CARDS, betYTot.statType());
        assertEquals(3.5, betYTot.param());

        // Yellow cards half 1 total
        OddItem yH1Tot = odds.stream().filter(o -> "yellow_cards_total_half_1".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(yH1Tot);
        assertEquals(1.80, yH1Tot.getValue());
        TotalBet betYH1Tot = (TotalBet) yH1Tot.getBetType();
        assertEquals(BetScope.HALF_1, betYH1Tot.scope());
        assertEquals(StatType.YELLOW_CARDS, betYH1Tot.statType());
        assertEquals(1.5, betYH1Tot.param());

        // Yellow cards handicap
        OddItem yHdp = odds.stream().filter(o -> "yellow_cards_handicap".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElse(null);
        assertNotNull(yHdp);
        assertEquals(1.85, yHdp.getValue());
        HandicapBet betYHdp = (HandicapBet) yHdp.getBetType();
        assertEquals(StatType.YELLOW_CARDS, betYHdp.statType());
        assertEquals(0.0, betYHdp.param());

        // Yellow cards 1x2
        OddItem y1x2 = odds.stream().filter(o -> "yellow_cards_1x2".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(y1x2);
        assertEquals(2.20, y1x2.getValue());
        MatchResultBet betY1x2 = (MatchResultBet) y1x2.getBetType();
        assertEquals(StatType.YELLOW_CARDS, betY1x2.statType());
        assertEquals(MatchResultBet.Outcome.WIN1, betY1x2.outcome());

        // Cards total (CARDS)
        OddItem cTot = odds.stream().filter(o -> "cards_total".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(cTot);
        assertEquals(1.90, cTot.getValue());
        TotalBet betCTot = (TotalBet) cTot.getBetType();
        assertEquals(StatType.CARDS, betCTot.statType());
        assertEquals(4.5, betCTot.param());

        // Cards 1x2 (2-way WIN1_2WAY)
        OddItem c1x2_2way = odds.stream().filter(o -> "cards_1x2".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(c1x2_2way);
        assertEquals(2.00, c1x2_2way.getValue());
        MatchResultBet betC2Way = (MatchResultBet) c1x2_2way.getBetType();
        assertEquals(StatType.CARDS, betC2Way.statType());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, betC2Way.outcome());
    }

    @Test
    public void testNestedCornersContainer() throws Exception {
        String eventJson = """
            {
                "id": "sb_nested_corners_1",
                "home": "Inter",
                "away": "Milan",
                "corners": {
                    "totals": [
                        { "limit": 10.5, "over": 1.92, "under": 1.88 }
                    ],
                    "handicap": {
                        "hdp": -2.0,
                        "home": 1.98,
                        "away": 1.82
                    },
                    "1x2": {
                        "home": 1.65,
                        "draw": 8.00,
                        "away": 2.50
                    }
                }
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Football", SportType.FOOTBALL, "Serie A");

        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);
        assertEquals(7, odds.size());

        OddItem over = odds.stream().filter(o -> o.getName().contains("OVER (10.5)")).findFirst().orElse(null);
        assertNotNull(over);
        assertEquals(1.92, over.getValue());
        assertEquals(StatType.CORNERS, ((TotalBet) over.getBetType()).statType());

        OddItem hdpHome = odds.stream().filter(o -> o.getName().contains("HOME (-2.0)")).findFirst().orElse(null);
        assertNotNull(hdpHome);
        assertEquals(1.98, hdpHome.getValue());
        assertEquals(StatType.CORNERS, ((HandicapBet) hdpHome.getBetType()).statType());

        OddItem win1 = odds.stream().filter(o -> "corners_1x2".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(win1);
        assertEquals(1.65, win1.getValue());
        assertEquals(StatType.CORNERS, ((MatchResultBet) win1.getBetType()).statType());
    }

    @Test
    public void testHandlerChainOrderingAndContextContainer() throws Exception {
        SportNormalizationService normalizationService = Mockito.mock(SportNormalizationService.class);
        Mockito.when(normalizationService.normalize("Football")).thenReturn(SportType.FOOTBALL);
        Mockito.when(normalizationService.normalize("Esports")).thenReturn(SportType.ESPORTS);

        // Pass handlers in completely reversed / mixed order to verify AnnotationAwareOrderComparator sorts them
        List<SbobetMarketHandler> unorderedHandlers = List.of(
                new SbobetMoneylineHandler(),
                new SbobetHandicapHandler(),
                new SbobetTotalHandler(),
                new SbobetCorrectScoreHandler(),
                new SbobetBttsHandler(),
                new SbobetDrawNoBetHandler(),
                new SbobetDoubleChanceHandler(),
                new SbobetEsportsHandler(),
                new SbobetStatsHandler()
        );
        SbobetOddsMapper testMapper = new SbobetOddsMapper(normalizationService, unorderedHandlers);

        // Verify chain ordering
        List<SbobetMarketHandler> ordered = testMapper.getMarketHandlers();
        assertEquals(9, ordered.size());
        assertTrue(ordered.get(0) instanceof SbobetStatsHandler, "First handler should be StatsHandler (@Order 10)");
        assertTrue(ordered.get(1) instanceof SbobetEsportsHandler, "Second handler should be EsportsHandler (@Order 20)");
        assertTrue(ordered.get(2) instanceof SbobetDoubleChanceHandler, "Third handler should be DoubleChanceHandler (@Order 30)");
        assertTrue(ordered.get(3) instanceof SbobetDrawNoBetHandler, "Fourth handler should be DrawNoBetHandler (@Order 40)");
        assertTrue(ordered.get(4) instanceof SbobetBttsHandler, "Fifth handler should be BttsHandler (@Order 50)");
        assertTrue(ordered.get(5) instanceof SbobetCorrectScoreHandler, "Sixth handler should be CorrectScoreHandler (@Order 60)");
        assertTrue(ordered.get(6) instanceof SbobetTotalHandler, "Seventh handler should be TotalHandler (@Order 70)");
        assertTrue(ordered.get(7) instanceof SbobetHandicapHandler, "Eighth handler should be HandicapHandler (@Order 80)");
        assertTrue(ordered.get(8) instanceof SbobetMoneylineHandler, "Ninth handler should be MoneylineHandler (@Order 90)");

        // Test event with "markets" container object and null SportType (should use normalizationService)
        String eventWithMarketsJson = """
            {
                "id": "sb_ctx_1",
                "home": "Natus Vincere",
                "away": "FaZe Clan",
                "startTime": 1785000000000,
                "isLive": false,
                "markets": {
                    "moneyline": {
                        "home": 1.75,
                        "away": 2.10
                    },
                    "maps_total": [
                        { "limit": 2.5, "over": 1.95, "under": 1.85 }
                    ]
                }
            }
            """;

        JsonNode event = objectMapper.readTree(eventWithMarketsJson);
        OddsUpdateRequest req = testMapper.mapToOddsUpdateRequest(event, "Esports", null, "BLAST Premier");

        assertNotNull(req);
        assertEquals(SportType.ESPORTS, req.getSportType());
        assertEquals("https://www.sbobet.com/euro/esports/match/sb_ctx_1", req.getEventUrl());
        assertEquals(4, req.getOdds().size());

        // Test event with "odds" container array
        String eventWithOddsArrayJson = """
            {
                "id": "sb_ctx_2",
                "home": "Arsenal",
                "away": "Chelsea",
                "startTime": 1785000000000,
                "isLive": false,
                "odds": [
                    {
                        "name": "double_chance",
                        "1x": 1.40,
                        "12": 1.30,
                        "x2": 1.60
                    },
                    {
                        "name": "btts",
                        "yes": 1.70,
                        "no": 2.10
                    }
                ]
            }
            """;

        JsonNode event2 = objectMapper.readTree(eventWithOddsArrayJson);
        OddsUpdateRequest req2 = testMapper.mapToOddsUpdateRequest(event2, "Football", SportType.FOOTBALL, "EPL");

        assertNotNull(req2);
        assertEquals(5, req2.getOdds().size());
        assertTrue(req2.getOdds().stream().anyMatch(o -> "double_chance".equals(o.getGroupName())));
        assertTrue(req2.getOdds().stream().anyMatch(o -> "btts".equals(o.getGroupName())));
    }

    @Test
    public void testEsportsMapWinnerAndMoneyline() throws Exception {
        String eventJson = """
            {
                "id": "sb_esports_1",
                "home": "Natus Vincere",
                "away": "FaZe Clan",
                "moneyline": {
                    "home": 1.65,
                    "away": 2.25
                },
                "map1_winner": {
                    "home": 1.70,
                    "away": 2.15
                },
                "map2_winner": [
                    { "name": "1", "odds": 1.80 },
                    { "name": "2", "odds": 2.00 }
                ],
                "map3_winner": [
                    { "name": "HOME", "odds": 1.90 },
                    { "name": "AWAY", "odds": 1.90 }
                ]
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "CS2", SportType.CS2, "PGL Major");

        assertNotNull(req);
        assertEquals("Natus Vincere", req.getTeam1());
        assertEquals("FaZe Clan", req.getTeam2());
        assertEquals(SportType.CS2, req.getSportType());

        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);
        assertEquals(8, odds.size());

        // Match Winner (2-Way Moneyline)
        OddItem mlHome = odds.stream().filter(o -> "moneyline".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(mlHome);
        assertEquals(1.65, mlHome.getValue());
        MatchResultBet betMlHome = (MatchResultBet) mlHome.getBetType();
        assertEquals(BetScope.FULL_MATCH, betMlHome.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, betMlHome.outcome());

        OddItem mlAway = odds.stream().filter(o -> "moneyline".equals(o.getGroupName()) && "AWAY".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(mlAway);
        assertEquals(2.25, mlAway.getValue());
        MatchResultBet betMlAway = (MatchResultBet) mlAway.getBetType();
        assertEquals(BetScope.FULL_MATCH, betMlAway.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, betMlAway.outcome());

        // Map 1 Winner
        OddItem m1Home = odds.stream().filter(o -> "map_winner_map_1".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(m1Home);
        assertEquals(1.70, m1Home.getValue());
        MatchResultBet betM1Home = (MatchResultBet) m1Home.getBetType();
        assertEquals(BetScope.MAP_1, betM1Home.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, betM1Home.outcome());

        // Map 2 Winner (from array "1" / "2")
        OddItem m2Away = odds.stream().filter(o -> "map_winner_map_2".equals(o.getGroupName()) && "AWAY".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(m2Away);
        assertEquals(2.00, m2Away.getValue());
        MatchResultBet betM2Away = (MatchResultBet) m2Away.getBetType();
        assertEquals(BetScope.MAP_2, betM2Away.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, betM2Away.outcome());

        // Map 3 Winner (from array "HOME" / "AWAY")
        OddItem m3Home = odds.stream().filter(o -> "map_winner_map_3".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(m3Home);
        assertEquals(1.90, m3Home.getValue());
        MatchResultBet betM3Home = (MatchResultBet) m3Home.getBetType();
        assertEquals(BetScope.MAP_3, betM3Home.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, betM3Home.outcome());
    }

    @Test
    public void testEsportsMapsTotalAndHandicap() throws Exception {
        String eventJson = """
            {
                "id": "sb_esports_maps",
                "home": "Team Spirit",
                "away": "G2 Esports",
                "maps_total": [
                    { "limit": 2.5, "over": 1.85, "under": 1.95 }
                ],
                "maps_handicap": {
                    "hdp": -1.5,
                    "home": 2.40,
                    "away": 1.55
                }
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Dota 2", SportType.DOTA2, "The International");

        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);
        assertEquals(4, odds.size());

        // Maps Total Over
        OddItem mapsOver = odds.stream().filter(o -> "maps_total".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(mapsOver);
        assertEquals(1.85, mapsOver.getValue());
        assertTrue(mapsOver.getBetType() instanceof TotalBet);
        TotalBet betMapsOver = (TotalBet) mapsOver.getBetType();
        assertEquals(BetScope.FULL_MATCH, betMapsOver.scope());
        assertEquals(2.5, betMapsOver.param());
        assertEquals(TotalBet.Direction.OVER, betMapsOver.direction());
        assertEquals(StatType.MAPS, betMapsOver.statType());

        // Maps Total Under
        OddItem mapsUnder = odds.stream().filter(o -> "maps_total".equals(o.getGroupName()) && o.getName().contains("UNDER")).findFirst().orElse(null);
        assertNotNull(mapsUnder);
        assertEquals(1.95, mapsUnder.getValue());
        TotalBet betMapsUnder = (TotalBet) mapsUnder.getBetType();
        assertEquals(BetScope.FULL_MATCH, betMapsUnder.scope());
        assertEquals(2.5, betMapsUnder.param());
        assertEquals(TotalBet.Direction.UNDER, betMapsUnder.direction());
        assertEquals(StatType.MAPS, betMapsUnder.statType());

        // Maps Handicap Home
        OddItem hdpHome = odds.stream().filter(o -> "maps_handicap".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElse(null);
        assertNotNull(hdpHome);
        assertEquals(2.40, hdpHome.getValue());
        assertTrue(hdpHome.getBetType() instanceof HandicapBet);
        HandicapBet betHdpHome = (HandicapBet) hdpHome.getBetType();
        assertEquals(BetScope.FULL_MATCH, betHdpHome.scope());
        assertEquals(-1.5, betHdpHome.param());
        assertEquals(HandicapBet.Outcome.TEAM1, betHdpHome.outcome());
        assertEquals(StatType.MAPS, betHdpHome.statType());

        // Maps Handicap Away
        OddItem hdpAway = odds.stream().filter(o -> "maps_handicap".equals(o.getGroupName()) && o.getName().contains("AWAY")).findFirst().orElse(null);
        assertNotNull(hdpAway);
        assertEquals(1.55, hdpAway.getValue());
        HandicapBet betHdpAway = (HandicapBet) hdpAway.getBetType();
        assertEquals(BetScope.FULL_MATCH, betHdpAway.scope());
        assertEquals(1.5, betHdpAway.param());
        assertEquals(HandicapBet.Outcome.TEAM2, betHdpAway.outcome());
        assertEquals(StatType.MAPS, betHdpAway.statType());
    }

    @Test
    public void testEsportsRoundsTotalAndHandicap() throws Exception {
        String eventJson = """
            {
                "id": "sb_esports_rounds",
                "home": "Vitality",
                "away": "MOUZ",
                "map1_rounds_total": [
                    { "limit": 22.5, "over": 1.90, "under": 1.90 }
                ],
                "map1_rounds_handicap": {
                    "hdp": -2.5,
                    "home": 1.85,
                    "away": 1.95
                },
                "rounds_total": [
                    { "map": 2, "limit": 21.5, "over": 1.75, "under": 2.05 }
                ],
                "rounds_handicap": [
                    { "map": 2, "hdp": 3.5, "home": 1.92, "away": 1.88 }
                ]
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "CS2", SportType.CS2, "ESL Pro League");

        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);
        assertEquals(8, odds.size());

        // Map 1 Rounds Total Over
        OddItem r1TotOver = odds.stream().filter(o -> "rounds_total_map_1".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(r1TotOver);
        assertEquals(1.90, r1TotOver.getValue());
        assertTrue(r1TotOver.getBetType() instanceof TotalBet);
        TotalBet betR1Over = (TotalBet) r1TotOver.getBetType();
        assertEquals(BetScope.MAP_1, betR1Over.scope());
        assertEquals(22.5, betR1Over.param());
        assertEquals(StatType.ROUNDS, betR1Over.statType());

        // Map 1 Rounds Handicap Home
        OddItem r1HdpHome = odds.stream().filter(o -> "rounds_handicap_map_1".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElse(null);
        assertNotNull(r1HdpHome);
        assertEquals(1.85, r1HdpHome.getValue());
        assertTrue(r1HdpHome.getBetType() instanceof HandicapBet);
        HandicapBet betR1HdpHome = (HandicapBet) r1HdpHome.getBetType();
        assertEquals(BetScope.MAP_1, betR1HdpHome.scope());
        assertEquals(-2.5, betR1HdpHome.param());
        assertEquals(StatType.ROUNDS, betR1HdpHome.statType());

        // Map 2 Rounds Total Over (resolved via item-level "map": 2)
        OddItem r2TotOver = odds.stream().filter(o -> "rounds_total_map_2".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(r2TotOver);
        assertEquals(1.75, r2TotOver.getValue());
        TotalBet betR2Over = (TotalBet) r2TotOver.getBetType();
        assertEquals(BetScope.MAP_2, betR2Over.scope());
        assertEquals(21.5, betR2Over.param());
        assertEquals(StatType.ROUNDS, betR2Over.statType());

        // Map 2 Rounds Handicap (resolved via item-level "map": 2)
        OddItem r2HdpHome = odds.stream().filter(o -> "rounds_handicap_map_2".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElse(null);
        assertNotNull(r2HdpHome);
        assertEquals(1.92, r2HdpHome.getValue());
        HandicapBet betR2HdpHome = (HandicapBet) r2HdpHome.getBetType();
        assertEquals(BetScope.MAP_2, betR2HdpHome.scope());
        assertEquals(3.5, betR2HdpHome.param());
        assertEquals(StatType.ROUNDS, betR2HdpHome.statType());
    }

    @Test
    public void testEsportsKillsTotalAndHandicap() throws Exception {
        String eventJson = """
            {
                "id": "sb_esports_kills",
                "home": "T1",
                "away": "Gen.G",
                "map1_kills_total": [
                    { "limit": 46.5, "over": 1.80, "under": 2.00 }
                ],
                "map1_kills_handicap": {
                    "hdp": -5.5,
                    "home": 1.85,
                    "away": 1.95
                },
                "kills_total": [
                    { "map": 2, "limit": 50.5, "over": 1.92, "under": 1.88 }
                ],
                "kills_handicap": [
                    { "map": 2, "hdp": -3.5, "home": 1.90, "away": 1.90 }
                ]
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "League of Legends", SportType.LEAGUE_OF_LEGENDS, "LCK");

        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);
        assertEquals(8, odds.size());

        // Map 1 Kills Total
        OddItem k1TotOver = odds.stream().filter(o -> "kills_total_map_1".equals(o.getGroupName()) && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(k1TotOver);
        assertEquals(1.80, k1TotOver.getValue());
        assertTrue(k1TotOver.getBetType() instanceof TotalBet);
        TotalBet betK1Over = (TotalBet) k1TotOver.getBetType();
        assertEquals(BetScope.MAP_1, betK1Over.scope());
        assertEquals(46.5, betK1Over.param());
        assertEquals(StatType.KILLS, betK1Over.statType());

        // Map 1 Kills Handicap
        OddItem k1HdpAway = odds.stream().filter(o -> "kills_handicap_map_1".equals(o.getGroupName()) && o.getName().contains("AWAY")).findFirst().orElse(null);
        assertNotNull(k1HdpAway);
        assertEquals(1.95, k1HdpAway.getValue());
        assertTrue(k1HdpAway.getBetType() instanceof HandicapBet);
        HandicapBet betK1HdpAway = (HandicapBet) k1HdpAway.getBetType();
        assertEquals(BetScope.MAP_1, betK1HdpAway.scope());
        assertEquals(5.5, betK1HdpAway.param());
        assertEquals(StatType.KILLS, betK1HdpAway.statType());

        // Map 2 Kills Total (item-level map index)
        OddItem k2TotUnder = odds.stream().filter(o -> "kills_total_map_2".equals(o.getGroupName()) && o.getName().contains("UNDER")).findFirst().orElse(null);
        assertNotNull(k2TotUnder);
        assertEquals(1.88, k2TotUnder.getValue());
        TotalBet betK2Under = (TotalBet) k2TotUnder.getBetType();
        assertEquals(BetScope.MAP_2, betK2Under.scope());
        assertEquals(50.5, betK2Under.param());
        assertEquals(StatType.KILLS, betK2Under.statType());

        // Map 2 Kills Handicap (item-level map index)
        OddItem k2HdpHome = odds.stream().filter(o -> "kills_handicap_map_2".equals(o.getGroupName()) && o.getName().contains("HOME")).findFirst().orElse(null);
        assertNotNull(k2HdpHome);
        assertEquals(1.90, k2HdpHome.getValue());
        HandicapBet betK2HdpHome = (HandicapBet) k2HdpHome.getBetType();
        assertEquals(BetScope.MAP_2, betK2HdpHome.scope());
        assertEquals(-3.5, betK2HdpHome.param());
        assertEquals(StatType.KILLS, betK2HdpHome.statType());
    }

    @Test
    public void testEsportsFirstBlood() throws Exception {
        String eventJson = """
            {
                "id": "sb_esports_fb",
                "home": "OG",
                "away": "Team Secret",
                "map1_first_blood": {
                    "home": 1.72,
                    "away": 2.05
                },
                "map2_first_blood": [
                    { "name": "HOME", "odds": 1.85 },
                    { "name": "AWAY", "odds": 1.95 }
                ],
                "first_blood": {
                    "home": 1.80,
                    "away": 1.95
                }
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Dota 2", SportType.DOTA2, "ESL One");

        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);
        assertEquals(6, odds.size());

        // Map 1 First Blood HOME
        OddItem fb1Home = odds.stream().filter(o -> "first_blood_map_1".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(fb1Home);
        assertEquals(1.72, fb1Home.getValue());
        assertTrue(fb1Home.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet betFb1Home = (BinaryMarketBet) fb1Home.getBetType();
        assertEquals(BetScope.MAP_1, betFb1Home.scope());
        assertEquals(BetSubject.TEAM1, betFb1Home.subject());
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, betFb1Home.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, betFb1Home.outcome());
        assertEquals(StatType.FIRST_BLOOD, betFb1Home.statType());

        // Map 1 First Blood AWAY
        OddItem fb1Away = odds.stream().filter(o -> "first_blood_map_1".equals(o.getGroupName()) && "AWAY".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(fb1Away);
        assertEquals(2.05, fb1Away.getValue());
        BinaryMarketBet betFb1Away = (BinaryMarketBet) fb1Away.getBetType();
        assertEquals(BetScope.MAP_1, betFb1Away.scope());
        assertEquals(BetSubject.TEAM2, betFb1Away.subject());
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, betFb1Away.marketType());
        assertEquals(StatType.FIRST_BLOOD, betFb1Away.statType());

        // Map 2 First Blood (from array)
        OddItem fb2Home = odds.stream().filter(o -> "first_blood_map_2".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(fb2Home);
        assertEquals(1.85, fb2Home.getValue());
        BinaryMarketBet betFb2Home = (BinaryMarketBet) fb2Home.getBetType();
        assertEquals(BetScope.MAP_2, betFb2Home.scope());
        assertEquals(BetSubject.TEAM1, betFb2Home.subject());

        // Full match First Blood
        OddItem fbMatchHome = odds.stream().filter(o -> "first_blood".equals(o.getGroupName()) && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(fbMatchHome);
        assertEquals(1.80, fbMatchHome.getValue());
        BinaryMarketBet betFbMatchHome = (BinaryMarketBet) fbMatchHome.getBetType();
        assertEquals(BetScope.FULL_MATCH, betFbMatchHome.scope());
        assertEquals(BetSubject.TEAM1, betFbMatchHome.subject());
    }

    @Test
    public void testEsportsNestedContainerAndDisciplines() throws Exception {
        String eventJson = """
            {
                "id": "sb_esports_nested",
                "home": "Sentinels",
                "away": "Fnatic",
                "esports": {
                    "map_1": {
                        "winner": {
                            "home": 1.60,
                            "away": 2.30
                        },
                        "rounds_total": [
                            { "limit": 21.5, "over": 1.95, "under": 1.85 }
                        ]
                    },
                    "first_blood": {
                        "home": 1.85,
                        "away": 1.90
                    }
                }
            }
            """;

        JsonNode event = objectMapper.readTree(eventJson);
        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event, "Valorant", SportType.VALORANT, "VCT Champions");

        List<OddItem> odds = req.getOdds();
        assertNotNull(odds);
        assertEquals(6, odds.size());

        // Map 1 Winner from nested container
        OddItem m1Home = odds.stream().filter(o -> o.getGroupName().contains("map_winner_map_1") && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(m1Home);
        assertEquals(1.60, m1Home.getValue());
        assertEquals(BetScope.MAP_1, ((MatchResultBet) m1Home.getBetType()).scope());

        // Map 1 Rounds Total from nested container
        OddItem r1Tot = odds.stream().filter(o -> o.getGroupName().contains("rounds_total_map_1") && o.getName().contains("OVER")).findFirst().orElse(null);
        assertNotNull(r1Tot);
        assertEquals(1.95, r1Tot.getValue());
        assertEquals(StatType.ROUNDS, ((TotalBet) r1Tot.getBetType()).statType());
        assertEquals(BetScope.MAP_1, ((TotalBet) r1Tot.getBetType()).scope());

        // First Blood
        OddItem fbHome = odds.stream().filter(o -> o.getGroupName().contains("first_blood") && "HOME".equals(o.getName())).findFirst().orElse(null);
        assertNotNull(fbHome);
        assertEquals(1.85, fbHome.getValue());
        assertEquals(StatType.FIRST_BLOOD, ((BinaryMarketBet) fbHome.getBetType()).statType());
    }
}
