package pro.datawiki.igaming.capture.sofascore.loader;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.capture.sofascore.dto.SofaTaskDto;
import pro.datawiki.igaming.capture.sofascore.queue.SofaQueueService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Two-stream Loader Service running inside a single pod.
 * Stream 1 (sofa-live-worker): High-priority processing of Live and Prematch tasks.
 * Stream 2 (sofa-history-worker): Background rate-limited processing of historical archive tasks.
 * 
 * Activated when app.role is 'loader' or 'standalone'.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.role", havingValue = "loader")
public class SofaTwoStreamLoaderService {

    private final SofaQueueService queueService;
    private final SofaTaskProcessor taskProcessor;

    @Value("${sofascore.loader.history-delay-ms:250}")
    private long historyDelayMs;

    @Value("${sofascore.loader.poll-timeout-sec:5}")
    private long pollTimeoutSec;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService liveExecutor;
    private ExecutorService historyExecutor;

    private final AtomicLong liveProcessedCount = new AtomicLong(0);
    private final AtomicLong historyProcessedCount = new AtomicLong(0);

    @EventListener(ApplicationReadyEvent.class)
    public void startWorkers() {
        running.set(true);
        log.info("[SofaTwoStreamLoader] Initializing dual worker streams in single pod...");

        // Stream 1: Live & Prematch Worker
        liveExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sofa-live-worker");
            t.setDaemon(true);
            return t;
        });

        // Stream 2: History Backfill Worker
        historyExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sofa-history-worker");
            t.setDaemon(true);
            return t;
        });

        liveExecutor.submit(this::runLiveStreamLoop);
        historyExecutor.submit(this::runHistoryStreamLoop);

        log.info("[SofaTwoStreamLoader] Dual worker streams started successfully.");
    }

    private void runLiveStreamLoop() {
        log.info("[sofa-live-worker] Live stream listener started. Waiting for tasks from Redis...");
        while (running.get()) {
            try {
                SofaTaskDto task = queueService.popLiveOrPrematch(pollTimeoutSec);
                if (task != null) {
                    long start = System.currentTimeMillis();
                    boolean ok = taskProcessor.processTask(task);
                    long duration = System.currentTimeMillis() - start;

                    long count = liveProcessedCount.incrementAndGet();
                    log.info("[sofa-live-worker] Processed {} task #{} (eventId={}) in {} ms (success={})",
                            task.getType(), count, task.getEventId(), duration, ok);
                }
            } catch (Exception e) {
                if (running.get()) {
                    log.error("[sofa-live-worker] Unexpected loop error: {}", e.getMessage(), e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        log.info("[sofa-live-worker] Stream stopped.");
    }

    private void runHistoryStreamLoop() {
        log.info("[sofa-history-worker] History stream listener started. Waiting for tasks from Redis...");
        while (running.get()) {
            try {
                SofaTaskDto task = queueService.popHistory(pollTimeoutSec);
                if (task != null) {
                    long start = System.currentTimeMillis();
                    boolean ok = taskProcessor.processTask(task);
                    long duration = System.currentTimeMillis() - start;

                    long count = historyProcessedCount.incrementAndGet();
                    log.info("[sofa-history-worker] Processed history task #{} (eventId={}) in {} ms (success={})",
                            count, task.getEventId(), duration, ok);

                    // Rate-limiting pause between historical matches to prevent WAF throttling
                    if (historyDelayMs > 0) {
                        Thread.sleep(historyDelayMs);
                    }
                }
            } catch (Exception e) {
                if (running.get()) {
                    log.error("[sofa-history-worker] Unexpected loop error: {}", e.getMessage(), e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        log.info("[sofa-history-worker] Stream stopped.");
    }

    @PreDestroy
    public void stopWorkers() {
        log.info("[SofaTwoStreamLoader] Stopping dual worker streams...");
        running.set(false);
        shutdownExecutor(liveExecutor, "liveExecutor");
        shutdownExecutor(historyExecutor, "historyExecutor");
        log.info("[SofaTwoStreamLoader] Worker streams shutdown complete. (Total live={}, history={})",
                liveProcessedCount.get(), historyProcessedCount.get());
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public long getLiveProcessedCount() {
        return liveProcessedCount.get();
    }

    public long getHistoryProcessedCount() {
        return historyProcessedCount.get();
    }
}
