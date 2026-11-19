package pro.datawiki.igaming.analyst.service.odds;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstitutionalConsensusOddsResolverTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private InstitutionalConsensusOddsResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new InstitutionalConsensusOddsResolver(redisTemplate, objectMapper);
    }

    private String buildOddJson(String bm, String typeCode, Double param, double value) {
        return String.format(java.util.Locale.US,
                "{\"bookmaker\":\"%s\",\"typeCode\":\"%s\",\"param\":%s,\"value\":%.2f,\"isSuspended\":false,\"isLive\":false}",
                bm, typeCode, param != null ? param.toString() : "null", value);
    }

    @Test
    @DisplayName("Sharp Anchor purges outlier quote (Zenit case: Pinnacle 2.05 vs Leon 5.70)")
    void testSharpAnchorPurgesOutlier() {
        Map<Object, Object> oddsMap = new HashMap<>();
        oddsMap.put("pinnacle:TO:4.0", buildOddJson("Pinnacle", "TOTAL_OVER", 4.0, 2.05));
        oddsMap.put("fonbet:TO:4.0", buildOddJson("Fonbet", "TOTAL_OVER", 4.0, 2.08));
        oddsMap.put("winline:TO:4.0", buildOddJson("Winline", "TOTAL_OVER", 4.0, 2.02));
        oddsMap.put("leon:TO:4.0", buildOddJson("Leon", "TOTAL_OVER", 4.0, 5.70)); // Broken outlier line!

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(anyString())).thenReturn(oddsMap);

        Optional<MarketQuoteDto> opt = resolver.resolveQuote(1597037L, "TOTAL_OVER_4.0");

        assertTrue(opt.isPresent(), "Expected valid quote to be resolved");
        MarketQuoteDto quote = opt.get();

        // Leon 5.70 MUST be purged! Best valid quote is Fonbet 2.08
        assertTrue(quote.value() < 2.20, "Selected odd must not be contaminated by Leon 5.70: " + quote.value());
        assertTrue(quote.isSharpAnchored(), "Must be anchored on Pinnacle sharp line");
        assertTrue(quote.isOutlierFiltered(), "Outlier flag must be true");
        assertEquals("SHARP_ANCHORED", quote.resolutionMode());
        assertEquals(2.05, quote.consensusValue(), 0.01);
    }

    @Test
    @DisplayName("Median consensus without sharp bookmakers purges extreme outlier")
    void testMedianConsensusPurgesOutlier() {
        Map<Object, Object> oddsMap = new HashMap<>();
        oddsMap.put("fonbet:WIN1:null", buildOddJson("Fonbet", "WIN1", null, 1.90));
        oddsMap.put("winline:WIN1:null", buildOddJson("Winline", "WIN1", null, 1.95));
        oddsMap.put("betcity:WIN1:null", buildOddJson("Betcity", "WIN1", null, 2.00));
        oddsMap.put("marathon:WIN1:null", buildOddJson("Marathon", "WIN1", null, 3.80)); // Broken line!

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(anyString())).thenReturn(oddsMap);

        Optional<MarketQuoteDto> opt = resolver.resolveQuote(1001L, "WIN1");

        assertTrue(opt.isPresent());
        MarketQuoteDto quote = opt.get();

        // Median is 1.975 (or 1.95). 3.80 is +90% divergent and must be purged.
        assertTrue(quote.value() <= 2.08, "Selected odd must be capped near consensus: " + quote.value());
        assertFalse(quote.isSharpAnchored());
        assertTrue(quote.isOutlierFiltered());
        assertEquals("MEDIAN_CONSENSUS", quote.resolutionMode());
    }

    @Test
    @DisplayName("Quorum rule vetoes single soft bookmaker quote without sharp anchor")
    void testQuorumRuleVetoesSingleSoftBookmaker() {
        Map<Object, Object> oddsMap = new HashMap<>();
        // Only Leon quotes this line, and Leon is not a sharp bookmaker
        oddsMap.put("leon:WIN1:null", buildOddJson("Leon", "WIN1", null, 2.15));

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(anyString())).thenReturn(oddsMap);

        Optional<MarketQuoteDto> opt = resolver.resolveQuote(2002L, "WIN1");

        assertTrue(opt.isEmpty(), "Single soft bookmaker quote must be vetoed by quorum rule");
    }

    @Test
    @DisplayName("Single Sharp bookmaker quote (Pinnacle) is accepted without quorum veto")
    void testSingleSharpBookmakerAccepted() {
        Map<Object, Object> oddsMap = new HashMap<>();
        oddsMap.put("pinnacle:WIN1:null", buildOddJson("Pinnacle", "WIN1", null, 1.85));

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(anyString())).thenReturn(oddsMap);

        Optional<MarketQuoteDto> opt = resolver.resolveQuote(3003L, "WIN1");

        assertTrue(opt.isPresent(), "Pinnacle alone is a trusted gold anchor");
        assertEquals(1.85, opt.get().value(), 0.01);
        assertTrue(opt.get().isSharpAnchored());
    }

    @Test
    @DisplayName("Synthetic Double Chance is derived from clean median W1 and X")
    void testSyntheticDoubleChanceDerivation() {
        Map<Object, Object> oddsMap = new HashMap<>();
        oddsMap.put("fonbet:WIN1:null", buildOddJson("Fonbet", "WIN1", null, 2.00));
        oddsMap.put("winline:WIN1:null", buildOddJson("Winline", "WIN1", null, 2.05));
        oddsMap.put("fonbet:DRAW:null", buildOddJson("Fonbet", "DRAW", null, 3.50));
        oddsMap.put("winline:DRAW:null", buildOddJson("Winline", "DRAW", null, 3.40));

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(anyString())).thenReturn(oddsMap);

        Optional<MarketQuoteDto> opt = resolver.resolveQuote(4004L, "DOUBLE_CHANCE_1X");

        assertTrue(opt.isPresent());
        MarketQuoteDto quote = opt.get();
        assertEquals("SYNTHETIC_CONSENSUS", quote.resolutionMode());
        assertTrue(quote.value() >= 1.20 && quote.value() <= 1.40, "Derived synthetic odd must be realistic: " + quote.value());
    }
}
