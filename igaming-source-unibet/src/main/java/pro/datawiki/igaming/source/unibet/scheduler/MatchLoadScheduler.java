package pro.datawiki.igaming.source.unibet.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.unibet.service.MatchService;

/**
 * Match-loader role: picks pending/stale matches from match_cache and loads
 * their market cards from Unibet (Kambi) API, then pushes odds to the aggregator.
 * 
 * Multiple replicas can run concurrently — uses SELECT FOR UPDATE SKIP LOCKED
 * to avoid processing the same match twice.
 * 
 * Only active when app.role=match-loader.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "match-loader")
public class MatchLoadScheduler {

    private final MatchService matchService;

    @Value("${app.match-loader.batch-size:10}")
    private int batchSize;

    /**
     * Continuously process pending matches without delay between cycles.
     */
    @Scheduled(fixedDelayString = "${app.match-loader.poll-delay-ms:100}")
    public void loadMatches() {
        try {
            int processed = matchService.loadMatchCards(batchSize);
            if (processed > 0) {
                log.debug("Match-loader: processed {} Unibet match cards this cycle", processed);
            }
        } catch (OptimisticLockingFailureException e) {
            log.debug("Optimistic locking conflict in Unibet match-loader batch fetch: {}", e.getMessage());
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Row was updated or deleted by another transaction")
                    || msg.contains("OptimisticLock") || msg.contains("StaleObjectState"))) {
                log.debug("Optimistic locking conflict in Unibet match-loader (nested): {}", msg);
            } else {
                log.error("Error during Unibet match card loading", e);
            }
        }
    }
}
