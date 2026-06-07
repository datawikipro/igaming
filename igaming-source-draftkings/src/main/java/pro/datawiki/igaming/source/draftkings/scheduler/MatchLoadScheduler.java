package pro.datawiki.igaming.source.draftkings.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.draftkings.service.MatchService;

/**
 * Match-loader role: loads match cards from match_cache and pushes odds updates to aggregator.
 * Only active when app.role=match-loader.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "match-loader")
public class MatchLoadScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${app.match-loader.poll-delay-ms:1000}")
    public void loadMatches() {
        try {
            int batchSize = 10;
            int processed = matchService.loadMatchCards(batchSize);
            if (processed > 0) {
                log.info("DraftKings match-loader processed {} matches", processed);
            }
        } catch (Exception e) {
            log.error("Error during scheduled DraftKings match loading: {}", e.getMessage());
        }
    }
}
