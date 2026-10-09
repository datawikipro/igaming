package pro.datawiki.igaming.source.novig.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.novig.dto.NovigEventDto;
import pro.datawiki.igaming.source.novig.dto.NovigMarketDto;
import pro.datawiki.igaming.source.novig.dto.NovigOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NovigOddsMapperTest {

    private NovigOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NovigOddsMapper();
    }

    @Test
    void testProbabilityToOddsConversion() {
        NovigOutcomeDto outcome = new NovigOutcomeDto();
        outcome.setAvailable(0.50);
        assertEquals(2.0, outcome.getDecimalOdds(), 0.001);

        outcome.setAvailable(0.25);
        assertEquals(4.0, outcome.getDecimalOdds(), 0.001);

        outcome.setAvailable(0.09);
        assertEquals(11.111, outcome.getDecimalOdds(), 0.001);
    }

    @Test
    void testMapNflEventWithMoneylineSpreadTotal() {
        NovigEventDto event = new NovigEventDto();
        event.setId("evt-12345");
        event.setDescription("Kansas City Chiefs @ Baltimore Ravens");
        event.setLeague("NFL");
        event.setStatus("OPEN_PREGAME");
        event.setScheduledStart("2026-09-15T00:20:00Z");

        // Moneyline Market
        NovigMarketDto mlMarket = new NovigMarketDto();
        mlMarket.setId("m-ml");
        mlMarket.setType("MONEYLINE");

        NovigOutcomeDto oHome = new NovigOutcomeDto();
        oHome.setId("o-home");
        oHome.setDescription("Baltimore Ravens");
        oHome.setAvailable(0.55); // 1.818

        NovigOutcomeDto oAway = new NovigOutcomeDto();
        oAway.setId("o-away");
        oAway.setDescription("Kansas City Chiefs");
        oAway.setAvailable(0.48); // 2.083

        mlMarket.setOutcomes(List.of(oHome, oAway));

        // Spread Market
        NovigMarketDto spMarket = new NovigMarketDto();
        spMarket.setId("m-sp");
        spMarket.setType("SPREAD");
        spMarket.setStrike(-2.5);

        NovigOutcomeDto oSpHome = new NovigOutcomeDto();
        oSpHome.setId("o-sphome");
        oSpHome.setDescription("Baltimore Ravens");
        oSpHome.setAvailable(0.52);

        NovigOutcomeDto oSpAway = new NovigOutcomeDto();
        oSpAway.setId("o-spaway");
        oSpAway.setDescription("Kansas City Chiefs");
        oSpAway.setAvailable(0.50);

        spMarket.setOutcomes(List.of(oSpHome, oSpAway));

        // Total Market
        NovigMarketDto totMarket = new NovigMarketDto();
        totMarket.setId("m-tot");
        totMarket.setType("TOTAL");
        totMarket.setStrike(47.5);

        NovigOutcomeDto oOver = new NovigOutcomeDto();
        oOver.setId("o-over");
        oOver.setDescription("Over 47.5");
        oOver.setType("Over");
        oOver.setAvailable(0.51);

        NovigOutcomeDto oUnder = new NovigOutcomeDto();
        oUnder.setId("o-under");
        oUnder.setDescription("Under 47.5");
        oUnder.setType("Under");
        oUnder.setAvailable(0.51);

        totMarket.setOutcomes(List.of(oOver, oUnder));

        event.setMarkets(List.of(mlMarket, spMarket, totMarket));

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);

        assertNotNull(request);
        assertEquals("novig", request.getBookmaker());
        assertEquals("Baltimore Ravens", request.getTeam1()); // Home
        assertEquals("Kansas City Chiefs", request.getTeam2()); // Away
        assertEquals(SportType.AMERICAN_FOOTBALL, request.getSportType());
        assertFalse(request.getIsLive());
        assertEquals(6, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("WIN1") && Math.abs(o.getValue() - 1.818) < 0.01));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("WIN2") && Math.abs(o.getValue() - 2.083) < 0.01));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("HANDICAP") && o.getGroupName().equals("spread")));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("TOTAL") && o.getBetType().code().contains("OVER")));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("TOTAL") && o.getBetType().code().contains("UNDER")));
    }
}
