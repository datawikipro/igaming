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
import pro.datawiki.igaming.dto.MatchEnrichmentDto;

import java.util.Optional;

/**
 * Periodically polls upcoming matches and enriches them with
 * confirmed lineups, starters, missing players, head-to-head statistics, and tournament storylines.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnrichmentSyncScheduler {

    private final RestTemplate restTemplate;
    private final SofaScoreEnrichmentProvider enrichmentProvider;

    @Value("${app.aggregator.url:http://localhost:3034}")
    private String aggregatorUrl;

    @Value("${app.enrichment.enabled:true}")
    private boolean enabled;

    /**
     * Runs periodically to fetch lineups, H2H, and storylines for upcoming matches.
     */
    @Scheduled(initialDelay = 15000, fixedDelayString = "${app.enrichment.fixed-delay:120000}")
    public void syncEnrichment() {
        if (!enabled) {
            log.trace("Lineup/Enrichment sync is disabled.");
            return;
        }

        log.info("[EnrichmentSyncScheduler] Starting sync of lineups, starters and missing players for upcoming matches...");
        try {
            String pendingUrl = aggregatorUrl + "/api/matches/needing/enrichment?limit=100";
            ResponseEntity<MatchMetadata[]> response = restTemplate.getForEntity(pendingUrl, MatchMetadata[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("[EnrichmentSyncScheduler] Aggregator returned non-2xx status: {}", response.getStatusCode());
                return;
            }

            MatchMetadata[] matches = response.getBody();
            if (matches.length == 0) {
                log.info("[EnrichmentSyncScheduler] No upcoming matches found needing enrichment.");
                return;
            }

            log.info("[EnrichmentSyncScheduler] Found {} upcoming matches needing enrichment.", matches.length);
            int enrichedCount = 0;

            for (MatchMetadata match : matches) {
                try {
                    Optional<MatchEnrichmentDto> enrichmentOpt = enrichmentProvider.fetchEnrichment(match);

                    if (enrichmentOpt.isPresent()) {
                        MatchEnrichmentDto dto = enrichmentOpt.get();
                        dto.setMatchId(match.id());

                        String updateUrl = aggregatorUrl + "/api/matches/" + match.id() + "/enrichment";
                        restTemplate.postForLocation(updateUrl, dto);
                        enrichedCount++;
                        log.info("[EnrichmentSyncScheduler] Uploaded lineups/injuries for match ID {}: {} vs {}",
                                match.id(), match.team1Name(), match.team2Name());
                    }
                } catch (Exception e) {
                    log.debug("[EnrichmentSyncScheduler] Enrichment failed for match {}: {}", match.id(), e.getMessage());
                }
            }

            log.info("[EnrichmentSyncScheduler] Finished enrichment pass. Enriched {}/{} matches.", enrichedCount, matches.length);
        } catch (Exception e) {
            log.error("[EnrichmentSyncScheduler] Error in enrichment cycle: {}", e.getMessage(), e);
        }
    }
}
