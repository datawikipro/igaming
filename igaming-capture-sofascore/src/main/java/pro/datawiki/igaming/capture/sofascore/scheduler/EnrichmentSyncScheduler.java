package pro.datawiki.igaming.capture.sofascore.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.provider.MatchResultProvider.MatchMetadata;
import pro.datawiki.igaming.capture.sofascore.provider.SofaScoreEnrichmentProvider;
import pro.datawiki.igaming.capture.sofascore.provider.SofaScoreResultProvider;
import pro.datawiki.igaming.dto.MatchEnrichmentDto;

import java.util.Optional;

/**
 * Periodically polls upcoming high-profile matches and enriches them with
 * confirmed lineups, head-to-head statistics, and tournament storylines from SofaScore.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnrichmentSyncScheduler {

    private final RestTemplate restTemplate;
    private final SofaScoreEnrichmentProvider enrichmentProvider;

    @Value("${app.aggregator.url:http://localhost:3034}")
    private String aggregatorUrl;

    @Value("${app.enrichment.enabled:false}")
    private boolean enabled;

    /**
     * Runs every 15 minutes (initial delay 30s) to fetch lineups, H2H, and storylines
     * for upcoming matches.
     */
    @Scheduled(initialDelay = 30000, fixedDelayString = "${app.enrichment.fixed-delay:900000}")
    public void syncEnrichment() {
        if (!enabled) {
            return;
        }

        log.info("[EnrichmentSyncScheduler] Starting sync of lineups, H2H and storylines for upcoming matches...");
        try {
            String pendingUrl = aggregatorUrl + "/api/matches/needing/results";
            ResponseEntity<MatchMetadata[]> response = restTemplate.getForEntity(pendingUrl, MatchMetadata[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return;
            }

            MatchMetadata[] matches = response.getBody();
            int enrichedCount = 0;

            for (MatchMetadata match : matches) {
                // If match has an associated external reference or ID
                try {
                    long eventId = match.id(); // fallback or search ID
                    Optional<MatchEnrichmentDto> enrichmentOpt = enrichmentProvider.fetchEnrichment(eventId);

                    if (enrichmentOpt.isPresent()) {
                        MatchEnrichmentDto dto = enrichmentOpt.get();
                        dto.setMatchId(match.id());

                        String updateUrl = aggregatorUrl + "/api/matches/" + match.id() + "/enrichment";
                        restTemplate.postForLocation(updateUrl, dto);
                        enrichedCount++;
                        log.info("[EnrichmentSyncScheduler] Successfully uploaded lineups/H2H for match ID {}", match.id());
                    }
                } catch (Exception e) {
                    log.debug("[EnrichmentSyncScheduler] Enrichment failed for match {}: {}", match.id(), e.getMessage());
                }
            }

            log.info("[EnrichmentSyncScheduler] Finished enrichment pass. Enriched {}/{} matches.", enrichedCount, matches.length);
        } catch (Exception e) {
            log.error("[EnrichmentSyncScheduler] Error in enrichment cycle: {}", e.getMessage());
        }
    }
}
