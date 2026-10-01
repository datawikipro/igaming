package pro.datawiki.igaming.source.bcgame.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameOutcomeDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BcgameEsportsMarketHandlerTest {

    private BcgameEsportsMarketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BcgameEsportsMarketHandler();
    }

    private BcgameOutcomeDto createOutcome(String id, String name, Double odds) {
        return createOutcome(id, name, odds, null);
    }

    private BcgameOutcomeDto createOutcome(String id, String name, Double odds, Double param) {
        BcgameOutcomeDto outcome = new BcgameOutcomeDto();
        outcome.setId(id);
        outcome.setName(name);
        outcome.setOdds(odds);
        outcome.setParam(param);
        return outcome;
    }

    @Test
    @DisplayName("Should correctly identify supported and unsupported esports markets")
    void testSupports() {
        BcgameMarketContext cs2Context = BcgameMarketContext.builder()
                .sportType(SportType.CS2)
                .homeTeam("NaVi")
                .awayTeam("FaZe")
                .build();

        BcgameMarketContext footballContext = BcgameMarketContext.builder()
                .sportType(SportType.FOOTBALL)
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .build();

        // Supported Maps
        assertTrue(handler.supports("Map 1 Winner", cs2Context));
        assertTrue(handler.supports("Map 2 - 2-Way", cs2Context));
        assertTrue(handler.supports("1st Map Winner", cs2Context));
        assertTrue(handler.supports("1-я карта - Победитель", cs2Context));
        assertTrue(handler.supports("Total Maps", cs2Context));
        assertTrue(handler.supports("Тотал карт", cs2Context));
        assertTrue(handler.supports("Map Handicap", cs2Context));
        assertTrue(handler.supports("Map Spread", cs2Context));
        assertTrue(handler.supports("Фора по картам", cs2Context));

        // Supported Rounds
        assertTrue(handler.supports("Total Rounds", cs2Context));
        assertTrue(handler.supports("Map 1 Total Rounds", cs2Context));
        assertTrue(handler.supports("Тотал раундов", cs2Context));
        assertTrue(handler.supports("Round Handicap", cs2Context));
        assertTrue(handler.supports("Map 1 Round Handicap", cs2Context));
        assertTrue(handler.supports("Фора по раундам", cs2Context));

        // Supported Kills
        assertTrue(handler.supports("Total Kills", cs2Context));
        assertTrue(handler.supports("Map 1 Total Kills", cs2Context));
        assertTrue(handler.supports("Kill Handicap", cs2Context));
        assertTrue(handler.supports("Map 1 Kill Handicap", cs2Context));
        assertTrue(handler.supports("Тотал убийств", cs2Context));

        // Supported First Blood
        assertTrue(handler.supports("First Blood", cs2Context));
        assertTrue(handler.supports("Map 1 First Blood", cs2Context));
        assertTrue(handler.supports("1st Blood", cs2Context));
        assertTrue(handler.supports("Первая кровь", cs2Context));

        // Supported Towers, Roshan, Baron
        assertTrue(handler.supports("First Tower", cs2Context));
        assertTrue(handler.supports("Total Towers", cs2Context));
        assertTrue(handler.supports("Tower Handicap", cs2Context));
        assertTrue(handler.supports("First Roshan", cs2Context));
        assertTrue(handler.supports("Total Roshan", cs2Context));
        assertTrue(handler.supports("First Baron", cs2Context));
        assertTrue(handler.supports("Total Baron", cs2Context));

        // Unsupported: Stats (handled by BcgameStatsMarketHandler)
        assertFalse(handler.supports("Corners 1X2", cs2Context));
        assertFalse(handler.supports("Yellow Cards Total", cs2Context));
        assertFalse(handler.supports("Fouls 1X2", cs2Context));

        // Unsupported: Traditional football markets
        assertFalse(handler.supports("1X2", footballContext));
        assertFalse(handler.supports("Match Result", footballContext));
        assertFalse(handler.supports("Both Teams to Score", footballContext));
        assertFalse(handler.supports("Correct Score", footballContext));
        assertFalse(handler.supports("Total Goals", footballContext));
    }

    @Test
    @DisplayName("Should correctly handle Map 1 Winner (2-Way)")
    void testHandleMapWinner2Way() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.CS2)
                .homeTeam("Natus Vincere")
                .awayTeam("FaZe Clan")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_map1_w");
        market.setName("Map 1 Winner");
        market.setOutcomes(List.of(
                createOutcome("o1", "Natus Vincere", 1.75),
                createOutcome("o2", "FaZe Clan", 2.10)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        OddItem item1 = items.get(0);
        assertEquals("o1", item1.getFactorId());
        assertEquals("Map 1 Winner", item1.getGroupName());
        assertEquals(1.75, item1.getValue());
        assertInstanceOf(MatchResultBet.class, item1.getBetType());
        MatchResultBet bet1 = (MatchResultBet) item1.getBetType();
        assertEquals(BetScope.MAP_1, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());
        assertEquals(StatType.MATCH, bet1.statType());

        OddItem item2 = items.get(1);
        MatchResultBet bet2 = (MatchResultBet) item2.getBetType();
        assertEquals(BetScope.MAP_1, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
    }

    @Test
    @DisplayName("Should correctly handle Map 2 Winner in Russian (1-я / 2-я карта)")
    void testHandleMap2WinnerRussian() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.DOTA2)
                .homeTeam("Team Spirit")
                .awayTeam("BetBoom Team")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_map2_w");
        market.setName("2-я карта - Победитель");
        market.setOutcomes(List.of(
                createOutcome("o1", "Team Spirit", 1.85),
                createOutcome("o2", "BetBoom Team", 1.95)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());
        MatchResultBet bet1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_2, bet1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, bet1.outcome());

        MatchResultBet bet2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_2, bet2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, bet2.outcome());
    }

    @Test
    @DisplayName("Should correctly handle Total Maps market")
    void testHandleTotalMaps() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.CS2)
                .homeTeam("G2 Esports")
                .awayTeam("MOUZ")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_tot_maps");
        market.setName("Total Maps");
        market.setOutcomes(List.of(
                createOutcome("o_over", "Over 2.5", 1.90, 2.5),
                createOutcome("o_under", "Under 2.5", 1.90, 2.5)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        OddItem itemOver = items.get(0);
        TotalBet betOver = (TotalBet) itemOver.getBetType();
        assertEquals(BetScope.FULL_MATCH, betOver.scope());
        assertEquals(BetSubject.MATCH, betOver.subject());
        assertEquals(TotalBet.Direction.OVER, betOver.direction());
        assertEquals(2.5, betOver.param());
        assertEquals(StatType.MAPS, betOver.statType());

        OddItem itemUnder = items.get(1);
        TotalBet betUnder = (TotalBet) itemUnder.getBetType();
        assertEquals(BetScope.FULL_MATCH, betUnder.scope());
        assertEquals(TotalBet.Direction.UNDER, betUnder.direction());
        assertEquals(2.5, betUnder.param());
        assertEquals(StatType.MAPS, betUnder.statType());
    }

    @Test
    @DisplayName("Should correctly handle Map Handicap market")
    void testHandleMapHandicap() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.CS2)
                .homeTeam("NaVi")
                .awayTeam("FaZe")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_hdp_maps");
        market.setName("Map Handicap");
        market.setOutcomes(List.of(
                createOutcome("o1", "NaVi (-1.5)", 2.40, -1.5),
                createOutcome("o2", "FaZe (+1.5)", 1.55, 1.5)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        HandicapBet bet1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, bet1.outcome());
        assertEquals(-1.5, bet1.param());
        assertEquals(StatType.MAPS, bet1.statType());

        HandicapBet bet2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, bet2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, bet2.outcome());
        assertEquals(1.5, bet2.param());
        assertEquals(StatType.MAPS, bet2.statType());
    }

    @Test
    @DisplayName("Should correctly handle Map 1 Total Rounds market")
    void testHandleMap1TotalRounds() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.CS2)
                .homeTeam("NaVi")
                .awayTeam("FaZe")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_tot_rounds");
        market.setName("Map 1 Total Rounds");
        market.setOutcomes(List.of(
                createOutcome("o1", "Over 21.5", 1.85, 21.5),
                createOutcome("o2", "Under 21.5", 1.95, 21.5)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        TotalBet betOver = (TotalBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, betOver.scope());
        assertEquals(BetSubject.MATCH, betOver.subject());
        assertEquals(TotalBet.Direction.OVER, betOver.direction());
        assertEquals(21.5, betOver.param());
        assertEquals(StatType.ROUNDS, betOver.statType());

        TotalBet betUnder = (TotalBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_1, betUnder.scope());
        assertEquals(TotalBet.Direction.UNDER, betUnder.direction());
        assertEquals(21.5, betUnder.param());
        assertEquals(StatType.ROUNDS, betUnder.statType());
    }

    @Test
    @DisplayName("Should correctly handle Map 1 Round Handicap market")
    void testHandleMap1RoundHandicap() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.CS2)
                .homeTeam("NaVi")
                .awayTeam("FaZe")
                .build();

        BcgameMarketDto market = new BcgameMarketDto();
        market.setId("m_hdp_rounds");
        market.setName("Map 1 Round Handicap");
        market.setOutcomes(List.of(
                createOutcome("o1", "NaVi (-2.5)", 1.80, -2.5),
                createOutcome("o2", "FaZe (+2.5)", 2.00, 2.5)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(market, context, items);

        assertEquals(2, items.size());

        HandicapBet bet1 = (HandicapBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, bet1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, bet1.outcome());
        assertEquals(-2.5, bet1.param());
        assertEquals(StatType.ROUNDS, bet1.statType());

        HandicapBet bet2 = (HandicapBet) items.get(1).getBetType();
        assertEquals(BetScope.MAP_1, bet2.scope());
        assertEquals(HandicapBet.Outcome.TEAM2, bet2.outcome());
        assertEquals(2.5, bet2.param());
        assertEquals(StatType.ROUNDS, bet2.statType());
    }

    @Test
    @DisplayName("Should correctly handle Map 1 Total Kills and Kill Handicap")
    void testHandleTotalKillsAndKillHandicap() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.DOTA2)
                .homeTeam("Team Liquid")
                .awayTeam("Gaimin Gladiators")
                .build();

        // 1. Total Kills
        BcgameMarketDto marketKills = new BcgameMarketDto();
        marketKills.setId("m_tot_kills");
        marketKills.setName("Map 1 Total Kills");
        marketKills.setOutcomes(List.of(
                createOutcome("o1", "Over 44.5", 1.85, 44.5),
                createOutcome("o2", "Under 44.5", 1.95, 44.5)
        ));

        List<OddItem> itemsKills = new ArrayList<>();
        handler.handle(marketKills, context, itemsKills);

        assertEquals(2, itemsKills.size());
        TotalBet betOver = (TotalBet) itemsKills.get(0).getBetType();
        assertEquals(BetScope.MAP_1, betOver.scope());
        assertEquals(TotalBet.Direction.OVER, betOver.direction());
        assertEquals(44.5, betOver.param());
        assertEquals(StatType.KILLS, betOver.statType());

        // 2. Kill Handicap
        BcgameMarketDto marketHdpKills = new BcgameMarketDto();
        marketHdpKills.setId("m_hdp_kills");
        marketHdpKills.setName("Map 1 Kill Handicap");
        marketHdpKills.setOutcomes(List.of(
                createOutcome("o1", "Team Liquid (-5.5)", 1.90, -5.5),
                createOutcome("o2", "Gaimin Gladiators (+5.5)", 1.90, 5.5)
        ));

        List<OddItem> itemsHdp = new ArrayList<>();
        handler.handle(marketHdpKills, context, itemsHdp);

        assertEquals(2, itemsHdp.size());
        HandicapBet betHdp1 = (HandicapBet) itemsHdp.get(0).getBetType();
        assertEquals(BetScope.MAP_1, betHdp1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, betHdp1.outcome());
        assertEquals(-5.5, betHdp1.param());
        assertEquals(StatType.KILLS, betHdp1.statType());
    }

    @Test
    @DisplayName("Should correctly handle First Blood market (Team 1 vs Team 2 and Yes/No)")
    void testHandleFirstBlood() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.LEAGUE_OF_LEGENDS)
                .homeTeam("T1")
                .awayTeam("Gen.G")
                .build();

        // 1. Team selection
        BcgameMarketDto marketFb = new BcgameMarketDto();
        marketFb.setId("m_fb1");
        marketFb.setName("Map 1 First Blood");
        marketFb.setOutcomes(List.of(
                createOutcome("o1", "T1", 1.80),
                createOutcome("o2", "Gen.G", 2.00)
        ));

        List<OddItem> items = new ArrayList<>();
        handler.handle(marketFb, context, items);

        assertEquals(2, items.size());
        BinaryMarketBet bet1 = (BinaryMarketBet) items.get(0).getBetType();
        assertEquals(BetScope.MAP_1, bet1.scope());
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, bet1.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bet1.outcome());
        assertEquals(StatType.FIRST_BLOOD, bet1.statType());

        BinaryMarketBet bet2 = (BinaryMarketBet) items.get(1).getBetType();
        assertEquals(BinaryMarketBet.Outcome.TEAM2, bet2.outcome());

        // 2. Yes/No selection
        BcgameMarketDto marketFbYesNo = new BcgameMarketDto();
        marketFbYesNo.setId("m_fb_yn");
        marketFbYesNo.setName("First Blood");
        marketFbYesNo.setOutcomes(List.of(
                createOutcome("oy", "Yes", 1.15),
                createOutcome("on", "No", 5.20)
        ));

        List<OddItem> itemsYn = new ArrayList<>();
        handler.handle(marketFbYesNo, context, itemsYn);

        assertEquals(2, itemsYn.size());
        BinaryMarketBet betY = (BinaryMarketBet) itemsYn.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, betY.scope());
        assertEquals(BinaryMarketBet.Outcome.YES, betY.outcome());

        BinaryMarketBet betN = (BinaryMarketBet) itemsYn.get(1).getBetType();
        assertEquals(BinaryMarketBet.Outcome.NO, betN.outcome());
    }

    @Test
    @DisplayName("Should correctly handle Towers, Roshan, Baron objectives")
    void testHandleObjectives() {
        BcgameMarketContext context = BcgameMarketContext.builder()
                .sportType(SportType.DOTA2)
                .homeTeam("Team Spirit")
                .awayTeam("BetBoom Team")
                .build();

        // 1. First Tower
        BcgameMarketDto marketTower = new BcgameMarketDto();
        marketTower.setId("m_ft");
        marketTower.setName("Map 1 First Tower");
        marketTower.setOutcomes(List.of(
                createOutcome("o1", "Team Spirit", 1.80),
                createOutcome("o2", "BetBoom Team", 2.00)
        ));

        List<OddItem> itemsTower = new ArrayList<>();
        handler.handle(marketTower, context, itemsTower);

        assertEquals(2, itemsTower.size());
        MatchResultBet betT1 = (MatchResultBet) itemsTower.get(0).getBetType();
        assertEquals(BetScope.MAP_1, betT1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, betT1.outcome());
        assertEquals(StatType.TOWERS, betT1.statType());

        // 2. First Roshan
        BcgameMarketDto marketRoshan = new BcgameMarketDto();
        marketRoshan.setId("m_fr");
        marketRoshan.setName("Map 1 First Roshan");
        marketRoshan.setOutcomes(List.of(
                createOutcome("o1", "Team Spirit", 1.70),
                createOutcome("o2", "BetBoom Team", 2.15)
        ));

        List<OddItem> itemsRoshan = new ArrayList<>();
        handler.handle(marketRoshan, context, itemsRoshan);

        assertEquals(2, itemsRoshan.size());
        MatchResultBet betR1 = (MatchResultBet) itemsRoshan.get(0).getBetType();
        assertEquals(BetScope.MAP_1, betR1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, betR1.outcome());
        assertEquals(StatType.ROSHAN, betR1.statType());

        // 3. First Baron
        BcgameMarketDto marketBaron = new BcgameMarketDto();
        marketBaron.setId("m_fb");
        marketBaron.setName("Map 1 First Baron");
        marketBaron.setOutcomes(List.of(
                createOutcome("o1", "Team Spirit", 1.90),
                createOutcome("o2", "BetBoom Team", 1.90)
        ));

        List<OddItem> itemsBaron = new ArrayList<>();
        handler.handle(marketBaron, context, itemsBaron);

        assertEquals(2, itemsBaron.size());
        MatchResultBet betB1 = (MatchResultBet) itemsBaron.get(0).getBetType();
        assertEquals(BetScope.MAP_1, betB1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, betB1.outcome());
        assertEquals(StatType.BARON, betB1.statType());

        // 4. Total Towers
        BcgameMarketDto marketTotTowers = new BcgameMarketDto();
        marketTotTowers.setId("m_tt");
        marketTotTowers.setName("Map 1 Total Towers");
        marketTotTowers.setOutcomes(List.of(
                createOutcome("o1", "Over 12.5", 1.85, 12.5),
                createOutcome("o2", "Under 12.5", 1.95, 12.5)
        ));

        List<OddItem> itemsTotTowers = new ArrayList<>();
        handler.handle(marketTotTowers, context, itemsTotTowers);

        assertEquals(2, itemsTotTowers.size());
        TotalBet betTotT = (TotalBet) itemsTotTowers.get(0).getBetType();
        assertEquals(BetScope.MAP_1, betTotT.scope());
        assertEquals(TotalBet.Direction.OVER, betTotT.direction());
        assertEquals(12.5, betTotT.param());
        assertEquals(StatType.TOWERS, betTotT.statType());
    }
}
