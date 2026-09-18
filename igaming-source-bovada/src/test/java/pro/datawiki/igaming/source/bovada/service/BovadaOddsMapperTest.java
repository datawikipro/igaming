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
}
