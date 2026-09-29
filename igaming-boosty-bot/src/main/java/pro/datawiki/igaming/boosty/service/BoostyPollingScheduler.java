package pro.datawiki.igaming.boosty.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.boosty.boosty.BoostySubscriber;

import java.util.List;

/**
 * Scheduled polling component for Boosty API.
 *
 * <p>Two independent polling loops:
 * <ol>
 *   <li><b>Subscriber poll</b> — fetches all paying subscribers and reconciles with DB.
 *       Interval: {@code BOOSTY_POLLING_INTERVAL_MS} (default 60 seconds).</li>
 *   <li><b>Comments poll</b> — scans recent posts for new donor comments and persists them.
 *       Interval: {@code BOOSTY_COMMENTS_POLLING_INTERVAL_MS} (default 2 minutes).</li>
 * </ol>
 *
 * <p>Both loops are protected by try-catch to prevent one failure from stopping
 * the scheduler thread. Errors are logged at WARN level.
 *
 * <p>Uses {@code fixedDelayString} (not fixedRate) so that if a request takes longer
 * than expected, the next poll starts {@code interval} ms <em>after</em> completion,
 * never overlapping.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BoostyPollingScheduler {

    /**
     * Number of recent posts to scan per comment collection cycle.
     * Limits API calls: 20 posts × 1 comment request = 21 total calls per cycle.
     */
    private static final int POSTS_TO_SCAN_PER_CYCLE = 20;

    private final BoostyApiClient apiClient;
    private final BoostyDonorService donorService;
    private final BoostyCommentCollector commentCollector;

    // ─────────────────────────────────────────────────────────
    // Subscriber polling
    // ─────────────────────────────────────────────────────────

    /**
     * Poll Boosty subscriber list and reconcile donor records.
     *
     * <p>Runs every {@code BOOSTY_POLLING_INTERVAL_MS} ms with an initial delay
     * of {@code BOOSTY_POLLING_INITIAL_DELAY_MS} ms.
     */
    @Scheduled(
            fixedDelayString  = "${boosty.polling.interval-ms:60000}",
            initialDelayString = "${boosty.polling.initial-delay-ms:10000}"
    )
    public void pollSubscribers() {
        log.debug("BoostyPollingScheduler: subscriber poll starting");
        try {
            List<BoostySubscriber> subscribers = apiClient.fetchSubscribers();
            log.info("BoostyPollingScheduler: fetched {} subscribers from Boosty", subscribers.size());
            donorService.reconcile(subscribers);
        } catch (Exception e) {
            log.warn("BoostyPollingScheduler: subscriber poll error: {}", e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Comments polling
    // ─────────────────────────────────────────────────────────

    /**
     * Poll Boosty posts for new donor comments and persist/forward them.
     *
     * <p>Runs every {@code BOOSTY_COMMENTS_POLLING_INTERVAL_MS} ms.
     * Initial delay is longer than subscriber poll to let donor reconciliation
     * complete first (so comment author filter has fresh data).
     */
    @Scheduled(
            fixedDelayString  = "${boosty.polling.comments-interval-ms:120000}",
            initialDelayString = "${boosty.polling.initial-delay-ms:30000}"
    )
    public void pollComments() {
        log.debug("BoostyPollingScheduler: comments poll starting (scanning {} posts)",
                POSTS_TO_SCAN_PER_CYCLE);
        try {
            commentCollector.collectDonorComments(POSTS_TO_SCAN_PER_CYCLE);
        } catch (Exception e) {
            log.warn("BoostyPollingScheduler: comment poll error: {}", e.getMessage(), e);
        }
    }
}
