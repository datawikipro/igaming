package pro.datawiki.igaming.vkdonut;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pro.datawiki.igaming.vkdonut.service.PortalSignalClient;
import pro.datawiki.igaming.vkdonut.service.VkApiClient;
import pro.datawiki.igaming.vkdonut.service.VkCommunityWallPoster;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VkCommunityWallPoster.
 *
 * Covers:
 *  - Public surebet signal post formatting (mandatory disclaimer, 80% freebet note, UTM links)
 *  - Freebet promo digest formatting (80% cash math per Rule 10)
 *  - Custom post publish (disclaimer appended)
 *  - Freebet post (80% guaranteed cash calculation)
 *  - Guard: community_id=0 → skip wall.post (no token configured)
 */
class VkCommunityWallPosterTest {

    private VkApiClient vkApiClient;
    private PortalSignalClient portalSignalClient;
    private VkCommunityWallPoster poster;

    @BeforeEach
    void setUp() {
        vkApiClient = mock(VkApiClient.class);
        portalSignalClient = mock(PortalSignalClient.class);
        when(vkApiClient.postWall(anyString(), anyInt())).thenReturn(Map.of("post_id", 42));

        poster = new VkCommunityWallPoster(vkApiClient, portalSignalClient);
        // Set community ID via reflection (Spring @Value not wired in unit tests)
        setField(poster, "communityId", 229123456L);
    }

    // ------------------------------------------------------------------
    // formatPublicSignalPost
    // ------------------------------------------------------------------

    @Test
    void testFormatPublicSignalPost_containsMandatoryDisclaimer() {
        Map<String, Object> signal = buildSignal(2.10, 2.05, 3.5, true);
        String msg = poster.formatPublicSignalPost(signal);

        assertTrue(msg.contains("Ставки на спорт сопряжены с финансовыми рисками"),
                "Mandatory gambling disclaimer must be present in every VK public post (Rule 10 / social-media-bot spec)");
    }

    @Test
    void testFormatPublicSignalPost_containsYieldAndSmartBetLink() {
        Map<String, Object> signal = buildSignal(2.20, 2.15, 4.8, true);
        String msg = poster.formatPublicSignalPost(signal);

        assertTrue(msg.contains("+4.8%") || msg.contains("4.8"), "Yield must be shown in post");
        assertTrue(msg.contains("smartbet.guru"), "SmartBet link must be present");
    }

    @Test
    void testFormatPublicSignalPost_freebetFriendly_shows80PercentNote() {
        Map<String, Object> signal = buildSignal(5.0, 1.25, 7.0, true);
        String msg = poster.formatPublicSignalPost(signal);

        assertTrue(msg.contains("80"), "80% freebet conversion note must appear for freebet-friendly signals");
        assertTrue(msg.contains("freebet-calculator"), "Freebet calculator UTM link must be present");
    }

    @Test
    void testFormatPublicSignalPost_notFreebetFriendly_noFreebetNote() {
        Map<String, Object> signal = buildSignal(1.90, 2.05, 2.1, false);
        String msg = poster.formatPublicSignalPost(signal);

        assertFalse(msg.contains("freebet-calculator"),
                "Calculator link must NOT appear for non-freebet-friendly signals");
    }

    // ------------------------------------------------------------------
    // formatFreebetDigestPost
    // ------------------------------------------------------------------

    @Test
    void testFormatFreebetDigestPost_calculates80PctGuaranteedCash() {
        List<Map<String, Object>> promos = List.of(
                buildPromo("Фонбет", "fonbet", 3000),
                buildPromo("Мелбет", "melbet", 5000)
        );
        String msg = poster.formatFreebetDigestPost(promos);

        // Фонбет: 3000 * 0.80 = 2400 — check digits regardless of locale separator
        assertTrue(msg.contains("2400") || msg.contains("2 400") || msg.contains("2,400"),
                "Must show 80% guaranteed cash = 2400 for 3000 RUB freebet. Got: " + msg);
        // Мелбет: 5000 * 0.80 = 4000
        assertTrue(msg.contains("4000") || msg.contains("4 000") || msg.contains("4,000"),
                "Must show 80% guaranteed cash = 4000 for 5000 RUB freebet. Got: " + msg);
        assertTrue(msg.contains("Ставки на спорт сопряжены с финансовыми рисками"),
                "Mandatory disclaimer must be in digest post");
        assertTrue(msg.contains("smartbet.guru/promos"), "Promos page link must be in digest");
    }

    @Test
    void testFormatFreebetDigestPost_containsAffiliateLinks() {
        List<Map<String, Object>> promos = List.of(buildPromo("Фонбет", "fonbet", 3000));
        String msg = poster.formatFreebetDigestPost(promos);

        assertTrue(msg.contains("smartbet.guru/go/fonbet"), "Affiliate link for fonbet must be present");
        assertTrue(msg.contains("utm_source=vk"), "UTM source tag must be present");
    }

    // ------------------------------------------------------------------
    // publishFreebetPost
    // ------------------------------------------------------------------

    @Test
    void testPublishFreebetPost_calculatesCorrectGuaranteedCash() {
        Map<String, Object> result = poster.publishFreebetPost("Мелбет", 5000, "melbet");

        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        verify(vkApiClient).postWall(msgCaptor.capture(), eq(0));

        String postedMsg = msgCaptor.getValue();
        // 5000 * 0.80 = 4000
        assertTrue(postedMsg.contains("4 000") || postedMsg.contains("4000"),
                "Guaranteed cash must be 4000 (80% of 5000 RUB freebet) per Rule 10");
        assertTrue(postedMsg.contains("Мелбет"), "Bookmaker name must be in the post");
        assertTrue(postedMsg.contains("smartbet.guru/go/melbet"), "Affiliate link must be present");
        assertTrue(postedMsg.contains("Ставки на спорт сопряжены"), "Disclaimer must be present");
        assertNotNull(result.get("post_id"));
    }

    // ------------------------------------------------------------------
    // publishCustomPost
    // ------------------------------------------------------------------

    @Test
    void testPublishCustomPost_alwaysAppendsDisclaimer() {
        Map<String, Object> result = poster.publishCustomPost("Заголовок", "Контент поста");

        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        verify(vkApiClient).postWall(msgCaptor.capture(), eq(0));

        assertTrue(msgCaptor.getValue().contains("Ставки на спорт сопряжены с финансовыми рисками"),
                "Disclaimer must always be appended to every wall post");
    }

    @Test
    void testPublishCustomPost_nullTitle_noNullInOutput() {
        poster.publishCustomPost(null, "Только контент");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(vkApiClient).postWall(captor.capture(), anyInt());
        assertFalse(captor.getValue().contains("null"), "null title must not appear in post text");
    }

    // ------------------------------------------------------------------
    // Guard: skip if communityId not configured
    // ------------------------------------------------------------------

    @Test
    void testPublishToWall_skipsIfCommunityIdZero() {
        setField(poster, "communityId", 0L);
        Map<String, Object> result = poster.publishCustomPost("Test", "Body");

        verify(vkApiClient, never()).postWall(anyString(), anyInt());
        assertEquals("skipped", result.get("status"));
    }

    // ------------------------------------------------------------------
    // Scheduled trigger — no signals → no API call
    // ------------------------------------------------------------------

    @Test
    void testPublishPublicSurebetSignal_noSignals_doesNotCallApi() {
        when(portalSignalClient.fetchPremiumSignals()).thenReturn(Collections.emptyList());
        poster.publishPublicSurebetSignal();
        verify(vkApiClient, never()).postWall(anyString(), anyInt());
    }

    @Test
    void testPublishFreebetPromoDigest_noPromos_doesNotCallApi() {
        when(portalSignalClient.fetchFreebetPromos()).thenReturn(Collections.emptyList());
        poster.publishFreebetPromoDigest();
        verify(vkApiClient, never()).postWall(anyString(), anyInt());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Map<String, Object> buildSignal(double odds1, double odds2, double yieldPct, boolean isFreebet) {
        return Map.of(
                "bk1", "Фонбет", "bet1_type", "П1", "odds1", odds1,
                "bk2", "Мелбет", "bet2_type", "П2", "odds2", odds2,
                "match", "Спартак - ЦСКА", "sport", "Football",
                "yield_pct", yieldPct, "is_freebet_friendly", isFreebet
        );
    }

    private Map<String, Object> buildPromo(String name, String slug, int amount) {
        return Map.of(
                "bookmaker_name", name,
                "bookmaker_slug", slug,
                "freebet_amount_rub", amount,
                "guaranteed_cash_80", (int)(amount * 0.80)
        );
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}
