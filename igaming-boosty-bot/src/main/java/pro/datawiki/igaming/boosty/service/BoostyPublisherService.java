package pro.datawiki.igaming.boosty.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.boosty.model.BoostyContentTemplate;
import pro.datawiki.igaming.boosty.model.BoostySubscriptionTier;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Publisher service for scheduled Boosty content posts.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Publish FREE weekly digest every Monday at 10:00 MSK</li>
 *   <li>Publish BASIC daily signals every day at 09:00 MSK</li>
 *   <li>Publish PREMIUM weekly report every Sunday at 20:00 MSK</li>
 *   <li>Expose manual post trigger via {@link #publishPost(BoostyContentTemplate, Object...)}</li>
 * </ul>
 *
 * <p>All posts are automatically appended with the mandatory disclaimer
 * per AGENTS.md Rule #10 and social-media-bot spec.
 *
 * <p>Freebet posts (FREE_FREEBET_PROMO) always include the guaranteed 80% cash
 * calculation per AGENTS.md Rule #10.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BoostyPublisherService {

    private static final String DISCLAIMER =
            "\n\n⚠️ Ставки на спорт сопряжены с финансовыми рисками. " +
            "Мы против лудомании и необдуманного беттинга. Играйте ответственно.";

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final BoostyApiClient apiClient;

    // ─────────────────────────────────────────────────────────
    // Scheduled publishers
    // ─────────────────────────────────────────────────────────

    /**
     * Weekly FREE digest — every Monday at 10:00 MSK (07:00 UTC).
     *
     * <p>Posts a teaser of public arbitrage opportunities (≤5% yield)
     * with a call-to-action to upgrade to Premium.
     */
    @Scheduled(cron = "${boosty.publish.cron.weekly-digest:0 0 7 * * MON}", zone = "UTC")
    public void publishWeeklyDigest() {
        log.info("BoostyPublisherService: publishing FREE weekly digest");
        String weekLabel = "неделя " + LocalDate.now().format(DATE_FMT);
        String signals   = "— Актуальные публичные вилки доступны на https://smartbet.guru";

        publishPost(
                BoostyContentTemplate.FREE_WEEKLY_DIGEST,
                weekLabel,
                signals
        );
    }

    /**
     * Daily BASIC signals — every day at 09:00 MSK (06:00 UTC).
     *
     * <p>Posts a daily signal digest for Basic-tier subscribers (yield ≤10%).
     */
    @Scheduled(cron = "${boosty.publish.cron.daily-signals:0 0 6 * * *}", zone = "UTC")
    public void publishDailyBasicSignals() {
        log.info("BoostyPublisherService: publishing BASIC daily signals");
        String dateLabel = LocalDate.now().format(DATE_FMT);
        String signals   = "— Загрузите дайджест на https://smartbet.guru";

        publishPost(
                BoostyContentTemplate.BASIC_DAILY_SIGNALS,
                dateLabel,
                signals
        );
    }

    /**
     * Weekly PREMIUM report — every Sunday at 20:00 MSK (17:00 UTC).
     *
     * <p>Posts a comprehensive weekly performance report for Premium subscribers.
     */
    @Scheduled(cron = "${boosty.publish.cron.premium-report:0 0 17 * * SUN}", zone = "UTC")
    public void publishPremiumWeeklyReport() {
        log.info("BoostyPublisherService: publishing PREMIUM weekly report");
        String period   = LocalDate.now().minusDays(7).format(DATE_FMT)
                          + " — " + LocalDate.now().format(DATE_FMT);

        publishPost(
                BoostyContentTemplate.PREMIUM_WEEKLY_REPORT,
                period,
                "Н/Д",    // total surebets found — filled by aggregator integration
                "Н/Д",    // avg yield
                "Н/Д",    // best yield
                "Н/Д",    // top bookmakers
                "Полная статистика доступна на https://smartbet.guru"
        );
    }

    // ─────────────────────────────────────────────────────────
    // Manual / on-demand publisher
    // ─────────────────────────────────────────────────────────

    /**
     * Publish a Boosty post using the given content template with the provided arguments.
     *
     * <p>Fills the title and body templates using {@link String#format(String, Object...)},
     * appends the mandatory disclaimer, and calls the Boosty API.
     *
     * @param template content template to use
     * @param args     positional arguments to fill into the template placeholders
     * @return API result map (or sandbox-mode response if token is not configured)
     */
    public Map<String, Object> publishPost(BoostyContentTemplate template, Object... args) {
        String title;
        String body;
        try {
            // Count %s placeholders to split args between title and body
            int titlePlaceholders = countPlaceholders(template.getTitleTemplate());
            Object[] titleArgs    = subArray(args, 0, titlePlaceholders);
            Object[] bodyArgs     = subArray(args, titlePlaceholders, args.length);

            title = String.format(template.getTitleTemplate(), titleArgs);
            body  = String.format(template.getBodyTemplate(),  bodyArgs);
        } catch (Exception e) {
            log.warn("BoostyPublisherService: template format error for {}: {}",
                    template.name(), e.getMessage());
            title = template.getTitleTemplate();
            body  = template.getBodyTemplate();
        }

        String fullBody  = body.trim() + DISCLAIMER;
        String teaser    = buildTeaser(template);
        Integer minPrice = template.getMinTierPriceRub() > 0 ? template.getMinTierPriceRub() : null;

        log.info("BoostyPublisherService: posting [{}] title='{}' tier={} minPrice={}₽",
                template.name(), title, template.getMinTier(), minPrice);

        Map<String, Object> result = apiClient.publishPost(title, fullBody, teaser, minPrice);

        log.info("BoostyPublisherService: post result={}", result);
        return result;
    }

    // ─────────────────────────────────────────────────────────
    // Freebet helper
    // ─────────────────────────────────────────────────────────

    /**
     * Publish a freebet promotion post with the guaranteed 80% cash calculation.
     *
     * <p>Per AGENTS.md Rule #10: every freebet post MUST include the cash conversion formula.
     *
     * @param bookmakerName   bookmaker name (e.g., "Фонбет")
     * @param freebetAmountRub freebet nominal in RUB (e.g., 3000)
     * @param affiliateUrl    affiliate tracking URL
     */
    public Map<String, Object> publishFreebetPromo(
            String bookmakerName,
            int freebetAmountRub,
            String affiliateUrl) {

        int guaranteedCash = (int) Math.round(freebetAmountRub * 0.80);

        return publishPost(
                BoostyContentTemplate.FREE_FREEBET_PROMO,
                bookmakerName,
                String.valueOf(guaranteedCash),
                bookmakerName,
                String.valueOf(freebetAmountRub),
                String.valueOf(freebetAmountRub),
                String.valueOf(guaranteedCash),
                affiliateUrl
        );
    }

    // ─────────────────────────────────────────────────────────
    // Subscription tier info — used by the REST controller
    // ─────────────────────────────────────────────────────────

    /**
     * Return the list of all subscription tiers with their public descriptions.
     * Intended for the admin dashboard and portal API.
     *
     * @return ordered list (FREE → BASIC → PREMIUM)
     */
    public List<Map<String, Object>> getSubscriptionTiers() {
        return List.of(
                tierInfo(BoostySubscriptionTier.FREE),
                tierInfo(BoostySubscriptionTier.BASIC),
                tierInfo(BoostySubscriptionTier.PREMIUM)
        );
    }

    private Map<String, Object> tierInfo(BoostySubscriptionTier tier) {
        String maxYield = (tier.getMaxYieldPct() == Double.MAX_VALUE)
                ? "unlimited (20%+)"
                : "up to " + (int) tier.getMaxYieldPct() + "%";
        return Map.of(
                "tier",          tier.name(),
                "displayName",   tier.getDisplayName(),
                "priceRub",      tier.getPriceRub(),
                "marketingLabel", tier.toMarketingLabel(),
                "benefits",      tier.getBenefits(),
                "maxYield",      maxYield,
                "boostyUrl",     "https://boosty.to/" + apiClient.getBlogName()
        );
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    private static int countPlaceholders(String template) {
        int count = 0;
        int idx   = 0;
        while ((idx = template.indexOf("%s", idx)) != -1) {
            count++;
            idx += 2;
        }
        return count;
    }

    private static Object[] subArray(Object[] arr, int from, int to) {
        int len = Math.max(0, Math.min(to, arr.length) - Math.max(0, from));
        Object[] result = new Object[len];
        System.arraycopy(arr, Math.max(0, from), result, 0, len);
        return result;
    }

    private static String buildTeaser(BoostyContentTemplate template) {
        return switch (template.getMinTier()) {
            case FREE    -> "Публичный контент SmartBet.guru — читайте бесплатно!";
            case BASIC   -> "Контент для подписчиков уровня «Базовый» (299 ₽/мес)";
            case PREMIUM -> "Эксклюзив для Premium-донов SmartBet.guru (2 500 ₽/мес). " +
                           "Подпишись: https://boosty.to/smartbetguru";
        };
    }
}
