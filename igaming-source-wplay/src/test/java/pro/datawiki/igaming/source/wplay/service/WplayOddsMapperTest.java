package pro.datawiki.igaming.source.wplay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import static org.junit.jupiter.api.Assertions.*;

class WplayOddsMapperTest {

    private WplayOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WplayOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("wplay", null));
        assertFalse(mapper.supports("fonbet", null));
    }

    @Test
    void testMapHtmlOdds() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("33035765");
        cache.setTeam1("Atlético Nacional");
        cache.setTeam2("Millonarios");
        cache.setSportName("Soccer");

        String sampleHtml = """
                <div>
                <button type="button" class="price">
                   <span class="seln-name">Atlético Nacional</span>
                   <span class="price dec">1.87</span>
                </button>
                <button type="button" class="price">
                   <span class="seln-name">Empate</span>
                   <span class="price dec">3.10</span>
                </button>
                <button type="button" class="price">
                   <span class="seln-name">Millonarios</span>
                   <span class="price dec">3.75</span>
                </button>
                </div>
                """;

        OddsUpdateRequest request = mapper.mapHtmlToOddsUpdateRequest(cache, sampleHtml);
        assertNotNull(request);
        assertEquals("wplay", request.getBookmaker());
        assertEquals("33035765", request.getExternalEventId());
        assertEquals(3, request.getOdds().size());

        assertEquals(1.87, request.getOdds().get(0).getValue());
        assertInstanceOf(MatchResultBet.class, request.getOdds().get(0).getBetType());
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) request.getOdds().get(0).getBetType()).outcome());

        assertEquals(3.10, request.getOdds().get(1).getValue());
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) request.getOdds().get(1).getBetType()).outcome());

        assertEquals(3.75, request.getOdds().get(2).getValue());
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) request.getOdds().get(2).getBetType()).outcome());
    }
}
