package pro.datawiki.igaming.capture.sofascore.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.capture.sofascore.service.SofaReconciliationService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Autonomous in-cluster scheduler for SofaScore ground truth synchronization and reconciliation.
 * Runs exclusively in the 'loader' pod (app.role=loader).
 * Maintains full 32-day historical coverage in sofascore_raw_events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.role", havingValue = "loader")
public class SofaAutonomousScheduler {

    private final SofaReconciliationService reconciliationService;

    @Value("${app.sofascore.reconciliation.enabled:false}")
    private boolean reconciliationEnabled;

    /**
     * Active window reconciliation: legacy reconciliation against match_record.
     * Fully disabled by default in favor of Sofa First MDM Ground Truth architecture.
     */
    @Scheduled(initialDelay = 15000, fixedDelay = 300000)
    public void syncActiveWindow() {
        if (!reconciliationEnabled) {
            log.debug("[SofaAutonomousScheduler] Legacy active window reconciliation is disabled (MDM Sofa-First active).");
            return;
        }
        log.info("[SofaAutonomousScheduler] Running scheduled active window reconciliation (last 48h to +48h)...");
        try {
            Instant now = Instant.now();
            Instant start = now.minus(48, ChronoUnit.HOURS);
            Instant end = now.plus(48, ChronoUnit.HOURS);
            var stats = reconciliationService.reconcileWindow(start, end, false);
            log.info("[SofaAutonomousScheduler] Active window reconciliation completed: {} newly matched in {} ms (women={}, youth={})",
                    stats.getNewlyMatched(), stats.getDurationMs(), stats.getWomenMatches(), stats.getYouthMatches());
        } catch (Exception e) {
            log.error("[SofaAutonomousScheduler] Error in active window reconciliation: {}", e.getMessage(), e);
        }
    }

    /**
     * Deep historical backfill check: ensures all 32 past days have complete multi-sport ground truth.
     * Runs 60 seconds after startup, and then every 4 hours.
     */
    @Scheduled(initialDelay = 60000, fixedDelay = 14400000)
    public void checkAndBackfillHistory() {
        log.info("[SofaAutonomousScheduler] Starting background historical backfill check (32 days)...");
        try {
            reconciliationService.backfillMissingHistoricalDates(32);
            log.info("[SofaAutonomousScheduler] Historical backfill check completed.");
        } catch (Exception e) {
            log.error("[SofaAutonomousScheduler] Error in historical backfill check: {}", e.getMessage(), e);
        }
    }
}
