package pro.datawiki.igaming.source.core.engine.xbet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XbetScoreParsingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private final AbstractXbetFamilyService service = new AbstractXbetFamilyService(
            null, null, null, null, null, null, null, null, null, null
    ) {
        @Override
        protected String resolveBaseUrl(String bookmakerName) {
            return "https://1xbet.com";
        }

        @Override
        protected boolean loadSingleMatchCard(MatchCache cache) {
            return false;
        }
    };

    @Test
    void testBasketballPeriodArraySummation() {
        MatchCache match = new MatchCache();
        match.setSportName("Баскетбол");

        ObjectNode scoreNode = mapper.createObjectNode();
        ArrayNode psArray = scoreNode.putArray("PS");

        ObjectNode p1 = psArray.addObject();
        ObjectNode val1 = p1.putObject("Value");
        val1.put("S1", 22);
        val1.put("S2", 19);

        ObjectNode p2 = psArray.addObject();
        ObjectNode val2 = p2.putObject("Value");
        val2.put("S1", 25);
        val2.put("S2", 21);

        // Even if FS says 2:0 in quarter counts
        ObjectNode fs = scoreNode.putObject("FS");
        fs.put("S1", "2");
        fs.put("S2", "0");

        service.parseScore(scoreNode, match);

        assertEquals("47", match.getScore1(), "Home total should sum 22+25=47");
        assertEquals("40", match.getScore2(), "Away total should sum 19+21=40");
    }

    @Test
    void testBasketballPeriodMapSummation() {
        MatchCache match = new MatchCache();
        match.setSportName("Basketball");

        ObjectNode scoreNode = mapper.createObjectNode();
        ObjectNode psMap = scoreNode.putObject("PS");

        ObjectNode p1 = psMap.putObject("1");
        p1.put("S1", 18);
        p1.put("S2", 20);

        ObjectNode p2 = psMap.putObject("2");
        p2.put("S1", 20);
        p2.put("S2", 15);

        service.parseScore(scoreNode, match);

        assertEquals("38", match.getScore1(), "Home total should sum 18+20=38");
        assertEquals("35", match.getScore2(), "Away total should sum 20+15=35");
    }

    @Test
    void testFootballScorePreservation() {
        MatchCache match = new MatchCache();
        match.setSportName("Футбол");

        ObjectNode scoreNode = mapper.createObjectNode();
        ObjectNode fs = scoreNode.putObject("FS");
        fs.put("S1", "2");
        fs.put("S2", "1");

        service.parseScore(scoreNode, match);

        assertEquals("2", match.getScore1());
        assertEquals("1", match.getScore2());
    }
}
