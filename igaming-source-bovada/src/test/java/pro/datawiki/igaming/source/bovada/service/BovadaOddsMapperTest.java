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
import pro.datawiki.igaming.source.bovada.dto.BovadaEventDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaEventGroupDto;

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
}
