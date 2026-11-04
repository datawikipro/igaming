package pro.datawiki.igaming.agent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.agent.client.PlaneAiWorkerClient;
import pro.datawiki.igaming.agent.config.PlaneAiProperties;
import pro.datawiki.igaming.agent.dto.WorkerGenerateRequest;
import pro.datawiki.igaming.agent.dto.WorkerGenerateResponse;
import pro.datawiki.igaming.agent.dto.WorkerStatusDto;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaneAiWorkerPoolDispatcher {

    private final PlaneAiWorkerClient workerClient;
    private final PlaneAiProperties properties;

    private final AtomicInteger roundRobinCursor = new AtomicInteger(0);
    private final Map<Integer, Boolean> workerHealthMap = new ConcurrentHashMap<>();

    /**
     * Executes an empirical prompt synchronously against the 20-worker Plane AI pool.
     * Implements Round-Robin load balancing and failover across up to maxAttempts workers.
     * Strictly verifies that the selected worker returns status == "ok".
     */
    public WorkerGenerateResponse executeEmpiricalPrompt(String prompt) {
        return executeEmpiricalPrompt(prompt, properties.getModel(), properties.getTimeoutSeconds());
    }

    public WorkerGenerateResponse executeEmpiricalPrompt(String prompt, String model, int timeoutSeconds) {
        int poolSize = properties.getCount();
        int attempts = Math.max(poolSize, properties.getMaxAttempts());
        int busyStreak = 0;

        for (int a = 0; a < attempts; a++) {
            int podIndex = Math.floorMod(roundRobinCursor.getAndIncrement(), poolSize);
            String podName = "plane-ai-worker-" + podIndex;

            // Pre-flight check: query worker status if available
            Optional<WorkerStatusDto> statusOpt = workerClient.queryStatus(podIndex);
            if (statusOpt.isPresent()) {
                WorkerStatusDto status = statusOpt.get();
                if ("BUSY".equalsIgnoreCase(status.getStatus())) {
                    busyStreak++;
                    log.debug("Worker {} is currently BUSY (streak={}), skipping to next account in pool...", podName, busyStreak);
                    if (busyStreak % poolSize == 0) {
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException ignored) {}
                    }
                    continue;
                }
                if (Boolean.FALSE.equals(status.getHasEnoughTokens())) {
                    log.debug("Worker {} has insufficient quota ({}), skipping...", podName, status.getQuotaMessage());
                    continue;
                }
            }
            busyStreak = 0;

            log.info("🚀 [Plane AI Dispatcher] Routing empirical audit to worker {} (attempt {}/{}, model={})...",
                    podName, a + 1, attempts, model);

            WorkerGenerateRequest request = WorkerGenerateRequest.builder()
                    .prompt(prompt)
                    .model(model != null ? model : properties.getModel())
                    .timeout(timeoutSeconds)
                    .build();

            WorkerGenerateResponse response = workerClient.executeSynchronously(podIndex, request);

            // Verification: verify that worker returned HTTP 200 with status "ok"
            if (response != null && "ok".equalsIgnoreCase(response.getStatus())
                    && response.getResponse() != null && !response.getResponse().isBlank()) {
                workerHealthMap.put(podIndex, true);
                log.info("✅ [Plane AI Dispatcher] Worker {} completed empirical audit in {}s (len={})",
                        response.getPod(), response.getDurationSeconds(), response.getResponse().length());
                return response;
            } else if (response != null && "busy".equalsIgnoreCase(response.getStatus())) {
                busyStreak++;
                log.info("⚡ [Plane AI Dispatcher] Worker {} is BUSY, hopping immediately to next worker...", podName);
                if (busyStreak % poolSize == 0) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ignored) {}
                }
            } else {
                workerHealthMap.put(podIndex, false);
                log.warn("⚠️ [Plane AI Dispatcher] Worker {} failed audit (attempt {}/{}): {}",
                        podName, a + 1, attempts, response != null ? response.getError() : "null response");
                if (response != null && response.getError() != null && response.getError().contains("Authentication required")) {
                    workerClient.triggerRefreshQuota(podIndex);
                }
            }
        }

        log.error("❌ [Plane AI Dispatcher] All {} worker attempts failed for prompt audit!", attempts);
        return WorkerGenerateResponse.builder()
                .status("error")
                .error("All " + attempts + " Plane AI worker attempts failed or timed out")
                .build();
    }

    /**
     * Polls status of all configured worker pods in parallel.
     */
    public List<WorkerStatusDto> pollAllWorkers() {
        int poolSize = properties.getCount();
        List<CompletableFuture<WorkerStatusDto>> futures = new ArrayList<>();

        for (int i = 0; i < poolSize; i++) {
            final int idx = i;
            futures.add(CompletableFuture.supplyAsync(() ->
                    workerClient.queryStatus(idx)
                            .orElse(WorkerStatusDto.builder()
                                    .podIndex(idx)
                                    .podName("plane-ai-worker-" + idx)
                                    .status("UNREACHABLE")
                                    .hasEnoughTokens(false)
                                    .quotaMessage("Connection refused or pod starting")
                                    .build())
            ));
        }

        List<WorkerStatusDto> result = new ArrayList<>();
        for (var f : futures) {
            try {
                result.add(f.get(4, TimeUnit.SECONDS));
            } catch (Exception e) {
                log.debug("Timeout gathering worker status: {}", e.getMessage());
            }
        }
        return result;
    }

    public Map<String, Object> getPoolStatusSummary() {
        List<WorkerStatusDto> workers = pollAllWorkers();
        long idleCount = workers.stream().filter(w -> "IDLE".equalsIgnoreCase(w.getStatus())).count();
        long tokenReadyCount = workers.stream().filter(w -> Boolean.TRUE.equals(w.getHasEnoughTokens())).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalConfigured", properties.getCount());
        summary.put("onlineWorkers", workers.stream().filter(w -> !"UNREACHABLE".equalsIgnoreCase(w.getStatus())).count());
        summary.put("idleWorkers", idleCount);
        summary.put("tokenReadyWorkers", tokenReadyCount);
        summary.put("workers", workers);
        return summary;
    }
}
