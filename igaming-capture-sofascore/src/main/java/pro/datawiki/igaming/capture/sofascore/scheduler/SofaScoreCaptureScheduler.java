package pro.datawiki.igaming.capture.sofascore.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.provider.MatchResultProvider;
import pro.datawiki.igaming.capture.sofascore.provider.MatchResultProvider.MatchMetadata;
import pro.datawiki.igaming.capture.sofascore.provider.MatchResultProvider.MatchResultUpdate;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SofaScoreCaptureScheduler {

    private final RestTemplate restTemplate;
    private final List<MatchResultProvider> resultProviders;

    @Value("${app.aggregator.url:http://localhost:3034}")
    private String aggregatorUrl;

    @Value("${app.capture.enabled:true}")
    private boolean enabled;

    @Scheduled(fixedDelayString = "${app.capture.fixed-delay:120000}", initialDelay = 15000)
    public void runCapture() {
        if (!enabled) {
            log.trace("Match result capture is disabled.");
            return;
        }

        log.info("[Match Result Ingestion Engine] Polling aggregator for matches needing official results...");
        try {
            String pendingUrl = aggregatorUrl + "/api/matches/needing/results";
            ResponseEntity<MatchMetadata[]> response = restTemplate.getForEntity(pendingUrl, MatchMetadata[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("[Result Engine] Aggregator returned non-2xx status: {}", response.getStatusCode());
                return;
            }

            MatchMetadata[] matches = response.getBody();
            if (matches.length == 0) {
                log.info("[Result Engine] No matches currently needing results.");
                return;
            }

            log.info("[Result Engine] Found {} matches needing results. Dispatching to {} providers...",
                    matches.length, resultProviders.size());
            int updatedCount = 0;

            for (MatchMetadata match : matches) {
                try {
                    Optional<MatchResultUpdate> captured = tryAllProviders(match);
                    if (captured.isPresent()) {
                        sendResultToAggregator(match.id(), captured.get());
                        updatedCount++;
                    }
                } catch (Exception e) {
                    log.error("[Result Engine] Failed to process match ID {}: {}", match.id(), e.getMessage());
                }
            }

            log.info("[Result Engine] Done polling. Settled and updated {}/{} matches.", updatedCount, matches.length);
        } catch (Exception e) {
            log.error("[Result Engine] Error in polling loop: {}", e.getMessage(), e);
        }
    }

    private Optional<MatchResultUpdate> tryAllProviders(MatchMetadata match) {
        for (MatchResultProvider provider : resultProviders) {
            if (provider.supports(match.sportName())) {
                try {
                    Optional<MatchResultUpdate> res = provider.fetchResult(match);
                    if (res.isPresent()) {
                        return res;
                    }
                } catch (Exception e) {
                    log.debug("[Result Engine] Provider {} threw error for match {}: {}",
                            provider.getClass().getSimpleName(), match.id(), e.getMessage());
                }
            }
        }
        return Optional.empty();
    }

    private void sendResultToAggregator(Long matchId, MatchResultUpdate update) {
        String updateUrl = aggregatorUrl + "/api/matches/" + matchId + "/result";
        log.info("[Result Engine] Sending confirmed score for match ID {} -> {}:{} ({}) to {}",
                matchId, update.score1(), update.score2(), update.matchResult(), updateUrl);
        try {
            restTemplate.postForLocation(updateUrl, update);
        } catch (Exception e) {
            log.error("[Result Engine] Failed to send update to aggregator for match ID {}: {}", matchId, e.getMessage());
        }
    }
}
