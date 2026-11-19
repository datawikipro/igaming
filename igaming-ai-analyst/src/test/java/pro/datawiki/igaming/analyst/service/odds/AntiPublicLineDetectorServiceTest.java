package pro.datawiki.igaming.analyst.service.odds;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AntiPublicLineDetectorServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOps;

    @Mock
    private MarketOddsResolver marketOddsResolver;

    private AntiPublicLineDetectorService detector;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
        detector = new AntiPublicLineDetectorService(redisTemplate, marketOddsResolver);
    }

    @Test
    void detectsAntiPublicFadeWhenLineDriftsUpAndMmrConfirmsEdge() {
        when(marketOddsResolver.resolveQuote(1001L, "WIN2")).thenReturn(
                Optional.of(MarketQuoteDto.of(2.50, "Fonbet (RU)", 2.45, 2.0, 5, true, false, "SHARP_ANCHORED"))
        );
        when(hashOps.putIfAbsent(eq("odds:open:1001"), eq("WIN2"), anyString())).thenReturn(false);
        when(hashOps.get("odds:open:1001", "WIN2")).thenReturn("2.1800");

        AntiPublicLineDetectorService.AntiPublicSignal signal = detector.analyzeMarketMovement(1001L, "WIN2", 48.0);

        assertEquals("ANTI_PUBLIC_FADE", signal.steamDirection());
        assertTrue(signal.antiPublicSweetSpot());
        assertTrue(signal.driftPct() > 10.0);
        assertNotNull(signal.rationaleNote());
        assertTrue(signal.rationaleNote().contains("ANTI-PUBLIC FADE"));
    }

    @Test
    void detectsSharpVsSoftValueWhenSoftBookExceedsPinnacleAnchor() {
        when(marketOddsResolver.resolveQuote(1002L, "WIN2")).thenReturn(
                Optional.of(MarketQuoteDto.of(2.42, "Olimpbet (RU)", 2.28, 6.1, 6, true, false, "SHARP_ANCHORED"))
        );
        when(hashOps.putIfAbsent(eq("odds:open:1002"), eq("WIN2"), anyString())).thenReturn(true);

        AntiPublicLineDetectorService.AntiPublicSignal signal = detector.analyzeMarketMovement(1002L, "WIN2", 44.0);

        assertEquals("SHARP_VS_SOFT_VALUE", signal.steamDirection());
        assertTrue(signal.sharpAnchored());
        assertTrue(signal.antiPublicSweetSpot());
        assertNotNull(signal.rationaleNote());
        assertTrue(signal.rationaleNote().contains("SHARP-VS-SOFT VALUE"));
    }

    @Test
    void detectsSharpSteamConfirmWhenConsensusDrops() {
        when(marketOddsResolver.resolveQuote(1003L, "WIN1")).thenReturn(
                Optional.of(MarketQuoteDto.of(1.95, "Pinnacle (COM)", 1.95, 0.0, 4, true, false, "SHARP_ANCHORED"))
        );
        when(hashOps.putIfAbsent(eq("odds:open:1003"), eq("WIN1"), anyString())).thenReturn(false);
        when(hashOps.get("odds:open:1003", "WIN1")).thenReturn("2.1500");

        AntiPublicLineDetectorService.AntiPublicSignal signal = detector.analyzeMarketMovement(1003L, "WIN1", 55.0);

        assertEquals("SHARP_STEAM_CONFIRM", signal.steamDirection());
        assertTrue(signal.driftPct() < -5.0);
    }
}
