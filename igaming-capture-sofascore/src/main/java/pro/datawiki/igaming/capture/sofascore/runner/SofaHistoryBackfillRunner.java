package pro.datawiki.igaming.capture.sofascore.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.capture.sofascore.service.SofaHistoryBackfillService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Optional startup runner triggered when sofascore.backfill.auto-start=true.
 * Useful for automated K8s batch jobs and historical seed environments.
 */
@Component
@ConditionalOnProperty(name = "sofascore.backfill.auto-start", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class SofaHistoryBackfillRunner implements CommandLineRunner {

    private final SofaHistoryBackfillService backfillService;

    @Value("${sofascore.backfill.start-date:2024-10-16}")
    private String startDateStr;

    @Value("${sofascore.backfill.delay-ms:250}")
    private long delayMs;

    @Value("${sofascore.backfill.sports:football,basketball,ice-hockey,tennis,volleyball,cs2,dota2}")
    private String sportsStr;

    @Override
    public void run(String... args) {
        log.info("[SofaBackfill Runner] Auto-start triggered via configuration.");

        LocalDate startDate;
        try {
            startDate = LocalDate.parse(startDateStr);
        } catch (Exception e) {
            log.warn("[SofaBackfill Runner] Invalid start-date '{}', defaulting to 2024-10-16", startDateStr);
            startDate = LocalDate.of(2024, 10, 16);
        }

        LocalDate endDate = LocalDate.now();
        List<String> sports = Arrays.stream(sportsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        log.info("[SofaBackfill Runner] Launching background backfill from {} to {} for sports: {}",
                startDate, endDate, sports);

        backfillService.backfillRangeAsync(sports, startDate, endDate, delayMs)
                .thenAccept(result -> log.info("[SofaBackfill Runner] Background run completed: {}", result))
                .exceptionally(ex -> {
                    log.error("[SofaBackfill Runner] Background run failed: {}", ex.getMessage(), ex);
                    return null;
                });
    }
}
