package pro.datawiki.igaming.source.bovada.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.bovada.dto.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BovadaOddsMapperTest {

    private BovadaOddsMapper oddsMapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        oddsMapper = new BovadaOddsMapper();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSupportsBovada() {
        assertTrue(oddsMapper.supports("bovada", SportType.AMERICAN_FOOTBALL));
        assertTrue(oddsMapper.supports("BOVADA", SportType.FOOTBALL));
        assertFalse(oddsMapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMapRealBovadaPayload() throws Exception {
        File sampleFile = new File("C:/Users/chernousov_a/.gemini/antigravity/brain/63da9c12-8bf3-489c-9aae-4ec721ff819b/scratch/bovada_nfl.json");
        if (!sampleFile.exists()) {
            return; // Skip if run in environment without local scratch
        }

        List<BovadaEventGroupDto> groups = objectMapper.readValue(sampleFile, new TypeReference<>() {});
        assertNotNull(groups);
        assertFalse(groups.isEmpty());

        BovadaEventGroupDto group = groups.get(0);
        assertNotNull(group.getEvents());
        assertFalse(group.getEvents().isEmpty());

        BovadaEventDto event = group.getEvents().get(0);
        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                event, group.getPath(), SportType.AMERICAN_FOOTBALL, "Football", "NFL");

        assertNotNull(request);
        assertEquals("bovada", request.getBookmaker());
        assertTrue(request.getRegions().contains(BookmakerRegion.US));
        assertEquals("Pittsburgh Steelers", request.getTeam1()); // Home
        assertEquals("Atlanta Falcons", request.getTeam2()); // Away
        assertNotNull(request.getOdds());
        assertFalse(request.getOdds().isEmpty());

        boolean hasMoneyline = false;
        boolean hasSpread = false;
        boolean hasTotal = false;

        for (var item : request.getOdds()) {
            assertTrue(item.getValue() > 1.0, "Odds value must be > 1.0");
            assertNotNull(item.getBetType(), "Canonical bet type must not be null");

            if (item.getBetType() instanceof MatchResultBet) {
                hasMoneyline = true;
            } else if (item.getBetType() instanceof HandicapBet) {
                hasSpread = true;
            } else if (item.getBetType() instanceof TotalBet) {
                hasTotal = true;
            }
        }

        assertTrue(hasMoneyline, "Should map Moneyline market");
        assertTrue(hasSpread, "Should map Point Spread market");
        assertTrue(hasTotal, "Should map Total (Over/Under) market");
    }

    @Test
    void testTeamTotalsDistinguishedFromMatchTotals() {
        BovadaEventDto event = new BovadaEventDto();
        event.setId("12345");
        event.setDescription("Burgos vs Eldense");

        BovadaCompetitorDto c1 = new BovadaCompetitorDto();
        c1.setName("Burgos");
        c1.setHome(true);
        BovadaCompetitorDto c2 = new BovadaCompetitorDto();
        c2.setName("Eldense");
        c2.setHome(false);
        event.setCompetitors(List.of(c1, c2));

        // Match total market: "Total Goals"
        BovadaMarketDto matchTotalMarket = new BovadaMarketDto();
        matchTotalMarket.setKey("2W-OU");
        matchTotalMarket.setDescription("Total Goals");
        BovadaOutcomeDto mOut1 = new BovadaOutcomeDto();
        mOut1.setType("O");
        mOut1.setDescription("Over");
        BovadaPriceDto mPrice1 = new BovadaPriceDto();
        mPrice1.setDecimal("1.34");
        mPrice1.setHandicap("1.5");
        mOut1.setPrice(mPrice1);
        matchTotalMarket.setOutcomes(List.of(mOut1));

        // Team 2 total market: "Total Goals - Eldense"
        BovadaMarketDto team2TotalMarket = new BovadaMarketDto();
        team2TotalMarket.setKey("2W-OU");
        team2TotalMarket.setDescription("Total Goals - Eldense");
        BovadaOutcomeDto t2Out1 = new BovadaOutcomeDto();
        t2Out1.setType("O");
        t2Out1.setDescription("Over");
        BovadaPriceDto t2Price1 = new BovadaPriceDto();
        t2Price1.setDecimal("3.85");
        t2Price1.setHandicap("1.5");
        t2Out1.setPrice(t2Price1);
        team2TotalMarket.setOutcomes(List.of(t2Out1));

        BovadaDisplayGroupDto dg = new BovadaDisplayGroupDto();
        dg.setMarkets(List.of(matchTotalMarket, team2TotalMarket));
        event.setDisplayGroups(List.of(dg));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                event, null, SportType.FOOTBALL, "Football", "LaLiga 2");

        assertEquals(2, request.getOdds().size());
        var matchTotal = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("total"))
                .findFirst().orElseThrow();
        assertEquals(1.34, matchTotal.getValue());
        assertEquals(pro.datawiki.igaming.dto.market.BetSubject.MATCH, ((TotalBet) matchTotal.getBetType()).subject());

        var team2Total = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("total_team2"))
                .findFirst().orElseThrow();
        assertEquals(3.85, team2Total.getValue());
        assertEquals(pro.datawiki.igaming.dto.market.BetSubject.TEAM2, ((TotalBet) team2Total.getBetType()).subject());
        assertNotEquals(matchTotal.getFactorId(), team2Total.getFactorId());
    }

    @Test
    void testSubPeriodMarketsExcluded() {
        BovadaEventDto event = new BovadaEventDto();
        event.setId("sub_period_event");
        event.setDescription("Kansas City Chiefs vs Baltimore Ravens");

        BovadaCompetitorDto c1 = new BovadaCompetitorDto();
        c1.setName("Kansas City Chiefs");
        c1.setHome(true);
        BovadaCompetitorDto c2 = new BovadaCompetitorDto();
        c2.setName("Baltimore Ravens");
        c2.setHome(false);
        event.setCompetitors(List.of(c1, c2));

        // Sub-period market by period description: "1st Half"
        BovadaMarketDto halfMarket = new BovadaMarketDto();
        halfMarket.setKey("2W-OU");
        halfMarket.setDescription("Total Points");
        BovadaPeriodDto halfPeriod = new BovadaPeriodDto();
        halfPeriod.setDescription("1st Half of Match");
        halfPeriod.setAbbreviation("1H");
        halfMarket.setPeriod(halfPeriod);

        BovadaOutcomeDto hOut = new BovadaOutcomeDto();
        hOut.setType("O");
        hOut.setDescription("Over");
        BovadaPriceDto hPrice = new BovadaPriceDto();
        hPrice.setDecimal("1.91");
        hPrice.setHandicap("23.5");
        hOut.setPrice(hPrice);
        halfMarket.setOutcomes(List.of(hOut));

        // Sub-period display group: "Quarter Lines"
        BovadaDisplayGroupDto qGroup = new BovadaDisplayGroupDto();
        qGroup.setDescription("Quarter Lines");
        BovadaMarketDto qMarket = new BovadaMarketDto();
        qMarket.setKey("2W-OU");
        qMarket.setDescription("Total Points");
        qMarket.setOutcomes(List.of(hOut));
        qGroup.setMarkets(List.of(qMarket));

        // Main match group
        BovadaDisplayGroupDto mainGroup = new BovadaDisplayGroupDto();
        mainGroup.setDescription("Game Lines");
        mainGroup.setMarkets(List.of(halfMarket));

        event.setDisplayGroups(List.of(mainGroup, qGroup));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                event, null, SportType.AMERICAN_FOOTBALL, "Football", "NFL");

        assertNotNull(request);
        assertTrue(request.getOdds().isEmpty(), "Sub-period markets must be strictly excluded from full-match lines");
    }

    @Test
    void testPlayerPropsAndSpecialMarketsExcluded() {
        BovadaEventDto event = new BovadaEventDto();
        event.setId("prop_event");
        event.setDescription("Kansas City Chiefs vs Baltimore Ravens");

        BovadaCompetitorDto c1 = new BovadaCompetitorDto();
        c1.setName("Kansas City Chiefs");
        c1.setHome(true);
        BovadaCompetitorDto c2 = new BovadaCompetitorDto();
        c2.setName("Baltimore Ravens");
        c2.setHome(false);
        event.setCompetitors(List.of(c1, c2));

        // Player prop market
        BovadaMarketDto propMarket = new BovadaMarketDto();
        propMarket.setKey("2W-OU");
        propMarket.setDescription("Total Passing Yards - Patrick Mahomes");
        BovadaOutcomeDto pOut = new BovadaOutcomeDto();
        pOut.setType("O");
        pOut.setDescription("Over");
        BovadaPriceDto pPrice = new BovadaPriceDto();
        pPrice.setDecimal("1.85");
        pPrice.setHandicap("275.5");
        pOut.setPrice(pPrice);
        propMarket.setOutcomes(List.of(pOut));

        // Corner handicap market
        BovadaMarketDto cornerMarket = new BovadaMarketDto();
        cornerMarket.setKey("2W-HCAP");
        cornerMarket.setDescription("Corners Point Spread");
        BovadaOutcomeDto cOut = new BovadaOutcomeDto();
        cOut.setType("H");
        cOut.setDescription("Home");
        BovadaPriceDto cPrice = new BovadaPriceDto();
        cPrice.setDecimal("1.90");
        cPrice.setHandicap("-1.5");
        cOut.setPrice(cPrice);
        cornerMarket.setOutcomes(List.of(cOut));

        BovadaDisplayGroupDto dg = new BovadaDisplayGroupDto();
        dg.setDescription("Game Lines");
        dg.setMarkets(List.of(propMarket, cornerMarket));
        event.setDisplayGroups(List.of(dg));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                event, null, SportType.AMERICAN_FOOTBALL, "Football", "NFL");

        assertNotNull(request);
        assertTrue(request.getOdds().isEmpty(), "Player props and statistical markets must not map to match lines");
    }

    @Test
    void testTeamNamesWithNumbersNotMismatched() {
        BovadaEventDto event = new BovadaEventDto();
        event.setId("nfl_event_numbers");
        event.setDescription("San Francisco 49ers vs Philadelphia 76ers");

        BovadaCompetitorDto c1 = new BovadaCompetitorDto();
        c1.setName("San Francisco 49ers");
        c1.setHome(true);
        BovadaCompetitorDto c2 = new BovadaCompetitorDto();
        c2.setName("Philadelphia 76ers");
        c2.setHome(false);
        event.setCompetitors(List.of(c1, c2));

        // Spread with explicit line in description: "San Francisco 49ers -3.5"
        BovadaMarketDto spreadMarket = new BovadaMarketDto();
        spreadMarket.setKey("2W-HCAP");
        spreadMarket.setDescription("Point Spread");

        BovadaOutcomeDto o1 = new BovadaOutcomeDto();
        o1.setType("H");
        o1.setDescription("San Francisco 49ers -3.5");
        BovadaPriceDto p1 = new BovadaPriceDto();
        p1.setDecimal("1.91");
        // No explicit handicap in price, must parse from description correctly!
        o1.setPrice(p1);

        BovadaOutcomeDto o2 = new BovadaOutcomeDto();
        o2.setType("A");
        o2.setDescription("Philadelphia 76ers +3.5");
        BovadaPriceDto p2 = new BovadaPriceDto();
        p2.setDecimal("1.91");
        o2.setPrice(p2);

        spreadMarket.setOutcomes(List.of(o1, o2));

        BovadaDisplayGroupDto dg = new BovadaDisplayGroupDto();
        dg.setDescription("Game Lines");
        dg.setMarkets(List.of(spreadMarket));
        event.setDisplayGroups(List.of(dg));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                event, null, SportType.AMERICAN_FOOTBALL, "Football", "NFL");

        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        var h1 = request.getOdds().stream().filter(o -> o.getName().contains("49ers")).findFirst().orElseThrow();
        var handicapBet1 = (HandicapBet) h1.getBetType();
        assertEquals(-3.5, handicapBet1.param(), "Must parse -3.5 spread line, not team number 49!");

        var h2 = request.getOdds().stream().filter(o -> o.getName().contains("76ers")).findFirst().orElseThrow();
        var handicapBet2 = (HandicapBet) h2.getBetType();
        assertEquals(3.5, handicapBet2.param(), "Must parse +3.5 spread line, not team number 76!");
    }

    @Test
    void testSoccerTwoWayMappedToDrawNoBet() {
        BovadaEventDto event = new BovadaEventDto();
        event.setId("soccer_event");
        event.setDescription("Arsenal vs Chelsea");

        BovadaCompetitorDto c1 = new BovadaCompetitorDto();
        c1.setName("Arsenal");
        c1.setHome(true);
        BovadaCompetitorDto c2 = new BovadaCompetitorDto();
        c2.setName("Chelsea");
        c2.setHome(false);
        event.setCompetitors(List.of(c1, c2));

        // 2-way moneyline in soccer (Draw No Bet)
        BovadaMarketDto dnbMarket = new BovadaMarketDto();
        dnbMarket.setKey("2W-12");
        dnbMarket.setDescription("Moneyline");

        BovadaOutcomeDto o1 = new BovadaOutcomeDto();
        o1.setType("H");
        o1.setDescription("Arsenal");
        BovadaPriceDto p1 = new BovadaPriceDto();
        p1.setDecimal("1.45");
        o1.setPrice(p1);

        BovadaOutcomeDto o2 = new BovadaOutcomeDto();
        o2.setType("A");
        o2.setDescription("Chelsea");
        BovadaPriceDto p2 = new BovadaPriceDto();
        p2.setDecimal("2.75");
        o2.setPrice(p2);

        dnbMarket.setOutcomes(List.of(o1, o2));

        BovadaDisplayGroupDto dg = new BovadaDisplayGroupDto();
        dg.setDescription("Game Lines");
        dg.setMarkets(List.of(dnbMarket));
        event.setDisplayGroups(List.of(dg));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                event, null, SportType.FOOTBALL, "Football", "Premier League");

        assertNotNull(request);
        assertEquals(2, request.getOdds().size());
        for (var odd : request.getOdds()) {
            assertTrue(odd.getBetType() instanceof HandicapBet, "Soccer 2W moneyline must be mapped to Handicap 0 (Draw No Bet)");
            assertEquals(0.0, ((HandicapBet) odd.getBetType()).param());
        }
    }

    @Test
    void testEuropean3WayHandicapExcluded() {
        BovadaEventDto event = new BovadaEventDto();
        event.setId("euro_handicap_event");
        event.setDescription("Real Madrid vs Barcelona");

        BovadaCompetitorDto c1 = new BovadaCompetitorDto();
        c1.setName("Real Madrid");
        c1.setHome(true);
        BovadaCompetitorDto c2 = new BovadaCompetitorDto();
        c2.setName("Barcelona");
        c2.setHome(false);
        event.setCompetitors(List.of(c1, c2));

        BovadaMarketDto euroHcap = new BovadaMarketDto();
        euroHcap.setKey("3W-HCAP");
        euroHcap.setDescription("3-Way Handicap");

        BovadaOutcomeDto o1 = new BovadaOutcomeDto();
        o1.setType("H");
        o1.setDescription("Real Madrid (-1)");
        BovadaPriceDto p1 = new BovadaPriceDto();
        p1.setDecimal("2.35");
        p1.setHandicap("-1");
        o1.setPrice(p1);

        euroHcap.setOutcomes(List.of(o1));

        BovadaDisplayGroupDto dg = new BovadaDisplayGroupDto();
        dg.setDescription("Game Lines");
        dg.setMarkets(List.of(euroHcap));
        event.setDisplayGroups(List.of(dg));

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(
                event, null, SportType.FOOTBALL, "Football", "LaLiga");

        assertNotNull(request);
        assertTrue(request.getOdds().isEmpty(), "European 3-way handicap must not be mapped as 2-way spread");
    }
}
