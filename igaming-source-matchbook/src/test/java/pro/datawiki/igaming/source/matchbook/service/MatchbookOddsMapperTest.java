package pro.datawiki.igaming.source.matchbook.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.matchbook.dto.MatchbookEvent;
import pro.datawiki.igaming.source.matchbook.dto.MatchbookMarket;
import pro.datawiki.igaming.source.matchbook.dto.MatchbookPrice;
import pro.datawiki.igaming.source.matchbook.dto.MatchbookRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchbookOddsMapperTest {

    private MatchbookOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MatchbookOddsMapper();
    }

    @Test
    void testMapFootballMatchOddsAndTotals() {
        MatchbookEvent event = new MatchbookEvent();
        event.setId(12345678L);
        event.setName("Arsenal vs Chelsea");
        event.setSportId(2); // Football
        event.setStart("2026-09-13T17:00:00.000Z");
        event.setInRunningFlag(false);

        // 1X2 Market
        MatchbookMarket m1X2 = new MatchbookMarket();
        m1X2.setId(101L);
        m1X2.setName("Match Odds");
        m1X2.setMarketType("one_x_two");

        MatchbookRunner rHome = new MatchbookRunner();
        rHome.setId(201L);
        rHome.setName("Arsenal");
        MatchbookPrice pHome = new MatchbookPrice();
        pHome.setSide("back");
        pHome.setDecimalOdds(2.10);
        rHome.setPrices(List.of(pHome));

        MatchbookRunner rDraw = new MatchbookRunner();
        rDraw.setId(202L);
        rDraw.setName("The Draw");
        MatchbookPrice pDraw = new MatchbookPrice();
        pDraw.setSide("back");
        pDraw.setDecimalOdds(3.50);
        rDraw.setPrices(List.of(pDraw));

        MatchbookRunner rAway = new MatchbookRunner();
        rAway.setId(203L);
        rAway.setName("Chelsea");
        MatchbookPrice pAway = new MatchbookPrice();
        pAway.setSide("back");
        pAway.setDecimalOdds(3.60);
        rAway.setPrices(List.of(pAway));

        m1X2.setRunners(List.of(rHome, rDraw, rAway));

        // Over/Under 2.5 Market
        MatchbookMarket mTotal = new MatchbookMarket();
        mTotal.setId(102L);
        mTotal.setName("Over/Under 2.5 Goals");
        mTotal.setMarketType("over_under");

        MatchbookRunner rOver = new MatchbookRunner();
        rOver.setId(204L);
        rOver.setName("Over 2.5");
        MatchbookPrice pOver = new MatchbookPrice();
        pOver.setSide("back");
        pOver.setDecimalOdds(1.95);
        rOver.setPrices(List.of(pOver));

        MatchbookRunner rUnder = new MatchbookRunner();
        rUnder.setId(205L);
        rUnder.setName("Under 2.5");
        MatchbookPrice pUnder = new MatchbookPrice();
        pUnder.setSide("back");
        pUnder.setDecimalOdds(1.91);
        rUnder.setPrices(List.of(pUnder));

        mTotal.setRunners(List.of(rOver, rUnder));

        event.setMarkets(List.of(m1X2, mTotal));

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);

        assertNotNull(request);
        assertEquals("matchbook", request.getBookmaker());
        assertEquals("Arsenal", request.getTeam1());
        assertEquals("Chelsea", request.getTeam2());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertFalse(request.getIsLive());
        assertEquals(5, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("WIN1") && o.getValue() == 2.10));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("DRAW") && o.getValue() == 3.50));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("WIN2") && o.getValue() == 3.60));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("TOTAL") && o.getBetType().code().contains("OVER") && o.getValue() == 1.95));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() != null && o.getBetType().code().contains("TOTAL") && o.getBetType().code().contains("UNDER") && o.getValue() == 1.91));
    }

    @Test
    void testResolveSportTypes() {
        assertEquals(SportType.AMERICAN_FOOTBALL, mapper.resolveSportType(1));
        assertEquals(SportType.FOOTBALL, mapper.resolveSportType(2));
        assertEquals(SportType.BASEBALL, mapper.resolveSportType(3));
        assertEquals(SportType.BASKETBALL, mapper.resolveSportType(4));
        assertEquals(SportType.CRICKET, mapper.resolveSportType(6));
        assertEquals(SportType.TENNIS, mapper.resolveSportType(9));
    }
}
