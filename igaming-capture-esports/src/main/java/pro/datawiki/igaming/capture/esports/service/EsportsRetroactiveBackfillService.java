package pro.datawiki.igaming.capture.esports.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.capture.esports.domain.EsportsRawEventEntity;
import pro.datawiki.igaming.capture.esports.repository.EsportsRawEventRepository;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background Retroactive Backfill Service.
 * Periodically or on-demand:
 * 1. Invalidates and pulls fresh MDM Ingestion Blueprint.
 * 2. Scans all 'UNMAPPED' records in 'esports_raw_events'.
 * 3. Re-evaluates O(1) matching against newly registered teams and tournaments.
 * 4. Upgrades resolved events to 'MAPPED' with canonical mdm_match_id.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EsportsRetroactiveBackfillService {

    private final EsportsRawEventRepository esportsRawEventRepository;
    private final EsportsMdmService esportsMdmService;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Executes retroactive backfill across all unmapped esports events.
     */
    @Transactional
    public BackfillReport executeBackfill() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("⏳ [BACKFILL] Retroactive backfill is already running. Skipping concurrent request.");
            return BackfillReport.builder().status("ALREADY_RUNNING").build();
        }

        Instant startTime = Instant.now();
        log.info("🚀 [BACKFILL] Starting Esports Retroactive Backfill run...");

        int totalUnmapped = 0;
        int newlyMapped = 0;
        int stillUnmapped = 0;

        try {
            // 1. Refresh MDM Ingestion Blueprint
            esportsMdmService.refreshBlueprint();

            // 2. Fetch all unmapped events
            List<EsportsRawEventEntity> unmappedEvents = esportsRawEventRepository.findUnmapped();
            totalUnmapped = unmappedEvents.size();
            log.info("📊 [BACKFILL] Found {} unmapped esports events to evaluate", totalUnmapped);

            for (EsportsRawEventEntity event : unmappedEvents) {
                try {
                    EsportsMdmService.MatchResolutionResult result = esportsMdmService.resolveMatch(
                            event.getGame(),
                            event.getTournamentRaw(),
                            event.getTeam1Raw(),
                            event.getTeam2Raw(),
                            event.getMatchDate()
                    );

                    if (result.isMapped()) {
                        event.setMdmStatus("MAPPED");
                        event.setMdmMatchId(result.getMatchId());
                        event.setMdmHomeTeamId(result.getHomeTeamId());
                        event.setMdmAwayTeamId(result.getAwayTeamId());
                        event.setUpdatedAt(Instant.now());
                        esportsRawEventRepository.save(event);

                        newlyMapped++;
                        log.info("✨ [BACKFILL-SUCCESS] Event {} ({} vs {}) successfully mapped to mdm_match_id={}",
                                event.getEventId(), event.getTeam1Raw(), event.getTeam2Raw(), result.getMatchId());
                    } else {
                        stillUnmapped++;
                    }
                } catch (Exception ex) {
                    stillUnmapped++;
                    log.warn("⚠️ [BACKFILL] Failed re-evaluating event {}: {}", event.getEventId(), ex.getMessage());
                }
            }

            log.info("🏁 [BACKFILL] Completed: total={}, newlyMapped={}, stillUnmapped={}",
                    totalUnmapped, newlyMapped, stillUnmapped);

            return BackfillReport.builder()
                    .status("COMPLETED")
                    .totalScanned(totalUnmapped)
                    .newlyMapped(newlyMapped)
                    .stillUnmapped(stillUnmapped)
                    .durationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli())
                    .build();

        } finally {
            isRunning.set(false);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackfillReport {
        private String status;
        private int totalScanned;
        private int newlyMapped;
        private int stillUnmapped;
        private long durationMs;
    }
}
