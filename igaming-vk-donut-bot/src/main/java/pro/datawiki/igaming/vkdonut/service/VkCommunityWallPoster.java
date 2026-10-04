package pro.datawiki.igaming.vkdonut.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Scheduled auto-poster for the SmartBet.guru VK Community public wall.
 *
 * Unlike {@link DonutSignalBroadcaster} (private messages to Donut donors),
 * this service publishes <em>public</em> wall posts visible to all community members
 * — arbitrage opportunities, freebet promos with 80% guaranteed cash math, and
 * educational content — on a configurable schedule.
 *
 * Every post includes:
 *  - 80% guaranteed cash calculation for freebets (Rule 10)
 *  - Mandatory responsible gambling disclaimer (social-media-bot spec)
 *  - UTM-tagged affiliate links to smartbet.guru/go/{bookmaker}
 *  - Affiliate link to the SmartBet freebet calculator
 *
 * VK API method: wall.post (public, from_group=1, no donut_paid_duration).
 * Routing: ru-proxy direct (РФ-букмекеры, VK API — Russian domestic network).
 *
 * Reference intervals (configurable via env):
 *  - Public surebet signal: every 30 minutes
 *  - Freebet promo digest:  every 6 hours
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VkCommunityWallPoster {

    private static final String DISCLAIMER =
            "\n\n⚠️ Ставки на спорт сопряжены с финансовыми рисками. " +
            "Мы против лудомании и необдуманного беттинга. Играйте ответственно.";

    /** Calculator UTM link appended to every promo post. */
    private static final String CALCULATOR_URL =
            "https://smartbet.guru/tools/freebet-calculator?utm_source=vk&utm_medium=community&utm_campaign=autopost";

    /** 80% guaranteed cash conversion coefficient (Rule 10 math). */
    private static final double FREEBET_CONVERSION_RATE = 0.80;

    private final VkApiClient vkApiClient;
    private final PortalSignalClient signalClient;

    @Value("${vk.community.id:0}")
    private Long communityId;

    // -------------------------------------------------------
    // Scheduled: Public Surebet Signal Post
    // -------------------------------------------------------

    /**
     * Publishes the latest surebet signal to the public VK community wall.
     * Default: every 30 minutes (configurable via VK_PUBLIC_SIGNAL_INTERVAL_MS).
     */
    @Scheduled(
            fixedRateString = "${vk.community.signal-post-interval-ms:1800000}",
            initialDelayString = "${vk.community.signal-post-initial-delay-ms:60000}"
    )
    public void publishPublicSurebetSignal() {
        List<Map<String, Object>> signals = signalClient.fetchPremiumSignals();
        if (signals.isEmpty()) {
            log.debug("VK Community Poster: no signals available for public post.");
            return;
        }
        // Take the top signal
        Map<String, Object> signal = signals.get(0);
        String message = formatPublicSignalPost(signal);
        publishToWall(message);
    }

    /**
     * Publishes a freebet promo digest to the community wall.
     * Default: every 6 hours (configurable via VK_PROMO_DIGEST_INTERVAL_MS).
     */
    @Scheduled(
            fixedRateString = "${vk.community.promo-post-interval-ms:21600000}",
            initialDelayString = "${vk.community.promo-post-initial-delay-ms:120000}"
    )
    public void publishFreebetPromoDigest() {
        List<Map<String, Object>> promos = signalClient.fetchFreebetPromos();
        if (promos.isEmpty()) {
            log.debug("VK Community Poster: no freebet promos available.");
            return;
        }
        String message = formatFreebetDigestPost(promos);
        publishToWall(message);
    }

    // -------------------------------------------------------
    // Manual Publish (for admin API and tests)
    // -------------------------------------------------------

    /**
     * Publish a custom plain text post to the public VK community wall.
     * Disclaimer is always appended.
     *
     * @param title   Post title (may be null or blank)
     * @param content Post body text
     * @return VK API response map
     */
    public Map<String, Object> publishCustomPost(String title, String content) {
        StringBuilder sb = new StringBuilder();
        if (title != null && !title.isBlank()) {
            sb.append("📢 ").append(title.trim()).append("\n\n");
        }
        sb.append(content != null ? content.trim() : "");
        sb.append(DISCLAIMER);
        return publishToWall(sb.toString());
    }

    /**
     * Publish a freebet promo post for a specific bookmaker with 80% guaranteed cash math.
     *
     * @param bookmakerName    Bookmaker display name (e.g. "Фонбет")
     * @param freebetAmountRub Freebet nominal in rubles (e.g. 3000)
     * @param affiliateSlug    URL slug for affiliate link (e.g. "fonbet")
     * @return VK API response map
     */
    public Map<String, Object> publishFreebetPost(String bookmakerName, int freebetAmountRub, String affiliateSlug) {
        int guaranteedCash = (int) Math.round(freebetAmountRub * FREEBET_CONVERSION_RATE);
        String affiliateLink = String.format(
                "https://smartbet.guru/go/%s?utm_source=vk&utm_medium=community&utm_campaign=freebet",
                affiliateSlug);

        String message = String.format(
                "🎁 Фрибет %s — %,d ₽ → %,d ₽ гарантированного кэша!\n\n" +
                "✅ Как это работает:\n" +
                "Любой фрибет (SNR) конвертируется в 80%% реальных денег через вилку " +
                "на высоких коэффициентах (K₁ ≈ 4.5–6.0).\n\n" +
                "📊 Формула: η = (K₁ - 1)×(K₂ - 1) / K₂ ≈ 0.80\n\n" +
                "🔗 Зарегистрироваться в %s: %s\n" +
                "🧮 Калькулятор 80%%: %s" +
                DISCLAIMER,
                bookmakerName, freebetAmountRub, guaranteedCash,
                bookmakerName, affiliateLink,
                CALCULATOR_URL
        );
        return publishToWall(message);
    }

    // -------------------------------------------------------
    // Message Formatting
    // -------------------------------------------------------

    /**
     * Formats a public surebet signal post for the VK community wall.
     * Includes arbitrage math, bookmaker links, and the 80% freebet note if applicable.
     */
    public String formatPublicSignalPost(Map<String, Object> signal) {
        String bk1    = stringify(signal.get("bk1"));
        String bk2    = stringify(signal.get("bk2"));
        String match  = stringify(signal.get("match"));
        String sport  = stringify(signal.get("sport"));
        double yieldPct = toDouble(signal.get("yield_pct"));
        String bet1   = stringify(signal.get("bet1_type"));
        String bet2   = stringify(signal.get("bet2_type"));
        double odds1  = toDouble(signal.get("odds1"));
        double odds2  = toDouble(signal.get("odds2"));
        boolean isFreebet = Boolean.TRUE.equals(signal.get("is_freebet_friendly"));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "⚡ Вилка SmartBet.guru | +%.1f%%\n\n" +
                "🏆 %s (%s)\n\n" +
                "📊 Плечо 1: %s → %s @ %.2f\n" +
                "📊 Плечо 2: %s → %s @ %.2f\n\n" +
                "💰 Гарантированная доходность: +%.1f%%\n",
                yieldPct,
                match, sport,
                bk1, bet1, odds1,
                bk2, bet2, odds2,
                yieldPct
        ));

        if (isFreebet) {
            sb.append(String.format(
                    "\n🎁 Вилку можно закрыть фрибетом: %.0f%% гарантированного кэша\n" +
                    "🧮 Калькулятор: %s\n",
                    FREEBET_CONVERSION_RATE * 100,
                    CALCULATOR_URL
            ));
        }

        sb.append("\n🔗 Все сигналы: https://smartbet.guru/surebets");
        sb.append(DISCLAIMER);
        return sb.toString();
    }

    /**
     * Formats a freebet promo digest post covering multiple bookmakers.
     * Calculates 80% guaranteed cash for each offer per Rule 10.
     */
    public String formatFreebetDigestPost(List<Map<String, Object>> promos) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎁 Дайджест фрибетов SmartBet.guru — ТОП акций этой недели\n\n");

        int count = Math.min(promos.size(), 5);
        for (int i = 0; i < count; i++) {
            Map<String, Object> promo = promos.get(i);
            String bkName  = stringify(promo.get("bookmaker_name"));
            int amount     = (int) toDouble(promo.get("freebet_amount_rub"));
            int guaranteed = (int) Math.round(amount * FREEBET_CONVERSION_RATE);
            String slug    = stringify(promo.get("bookmaker_slug"));
            String affLink = String.format(
                    "https://smartbet.guru/go/%s?utm_source=vk&utm_medium=digest",
                    slug.toLowerCase());

            sb.append(String.format(
                    "%d. %s — фрибет %,d ₽ → %,d ₽ кэша\n" +
                    "   🔗 %s\n\n",
                    i + 1, bkName, amount, guaranteed, affLink
            ));
        }

        sb.append(String.format(
                "🧮 Рассчитать 80%% с вашего фрибета: %s\n" +
                "📋 Все акции: https://smartbet.guru/promos",
                CALCULATOR_URL
        ));
        sb.append(DISCLAIMER);
        return sb.toString();
    }

    // -------------------------------------------------------
    // Internal
    // -------------------------------------------------------

    private Map<String, Object> publishToWall(String message) {
        if (communityId == null || communityId <= 0) {
            log.warn("VK Community Poster: VK_COMMUNITY_ID not set, skipping wall.post");
            return Map.of("status", "skipped", "reason", "community_id_not_configured");
        }
        try {
            // Public wall post: donutPaidDuration=0 means no Donut restriction
            Map<String, Object> result = vkApiClient.postWall(message, 0);
            log.info("VK Community public wall post published: result={}", result);
            return result;
        } catch (Exception e) {
            log.error("VK Community Poster: wall.post failed: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    private String stringify(Object obj) {
        return obj != null ? obj.toString() : "N/A";
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number num) return num.doubleValue();
        return 0.0;
    }
}
