package pro.datawiki.igaming.source.swisslos.service;

import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.source.swisslos.dto.SwisslosEventDto;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SwisslosOddsMapperTest {

    private final SwisslosOddsMapper mapper = new SwisslosOddsMapper();

    @Test
    void testMapToOddsUpdate() {
        SwisslosEventDto event = SwisslosEventDto.builder()
                .id("ev-wave4")
                .homeTeam("Team Home")
                .awayTeam("Team Away")
                .sport("Soccer")
                .startTime(System.currentTimeMillis() + 3600000L)
                .isLive(false)
                .odds(Map.of("1", 2.10, "X", 3.40, "2", 3.20))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdate(event);

        assertNotNull(request);
        assertEquals("swisslos", request.getBookmaker());
        assertEquals("ev-wave4", request.getExternalEventId());
        assertNotNull(request.getOdds());
        assertEquals(3, request.getOdds().size());

        OddItem item1 = request.getOdds().stream()
                .filter(o -> "1".equals(o.getFactorId()))
                .findFirst()
                .orElse(null);
        assertNotNull(item1);
        assertEquals(2.10, item1.getValue());
        assertTrue(item1.getBetType() instanceof MatchResultBet);
        MatchResultBet m1 = (MatchResultBet) item1.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, m1.outcome());
    }
}
