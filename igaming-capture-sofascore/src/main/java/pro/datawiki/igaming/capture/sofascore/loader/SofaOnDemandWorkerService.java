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
import pro.datawiki.igaming.capture.sofascore.service.SofaOnDemandService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dedicated high-priority worker for processing on-demand match refresh tasks.
 * Active when app.role is 'on-demand' or 'standalone'.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.role", havingValue = "on-demand")
public class SofaOnDemandWorkerService {

    private final SofaQueueService queueService;
    private final SofaOnDemandService onDemandService;

    @Value("${sofascore.ondemand.poll-timeout-sec:3}")
    private long pollTimeoutSec;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executor;
    private final AtomicLong processedCount = new AtomicLong(0);

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        running.set(true);
        log.info("[SofaOnDemandWorker] Starting dedicated on-demand task listener...");
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sofa-ondemand-worker");
            t.setDaemon(true);
            return t;
        });

        executor.submit(this::runWorkerLoop);
        log.info("[SofaOnDemandWorker] Started dedicated on-demand worker.");
    }

    private void runWorkerLoop() {
        while (running.get()) {
            try {
                SofaTaskDto task = queueService.popOnDemand(pollTimeoutSec);
                if (task != null) {
                    long count = processedCount.incrementAndGet();
                    log.info("[SofaOnDemandWorker] Processing on-demand task #{} (taskId={}, eventId={})",
                            count, task.getTaskId(), task.getEventId());
                    onDemandService.processTask(task);
                }
            } catch (Exception e) {
                if (running.get()) {
                    log.error("[SofaOnDemandWorker] Worker loop error: {}", e.getMessage(), e);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        log.info("[SofaOnDemandWorker] Worker stopped.");
    }

    @PreDestroy
    public void stop() {
        running.set(false);
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
        log.info("[SofaOnDemandWorker] Shutdown complete. Processed tasks: {}", processedCount.get());
    }

    public long getProcessedCount() {
        return processedCount.get();
    }
}
