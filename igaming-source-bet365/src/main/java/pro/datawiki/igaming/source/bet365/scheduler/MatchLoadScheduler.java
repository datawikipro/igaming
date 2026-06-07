package pro.datawiki.igaming.source.bet365.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.bet365.service.MatchService;

/**
 * Match-loader role: loads match cards (odds details) for pending matches and reports them to the aggregator.
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
                log.info("Match-loader processed {} Bet365 matches", processed);
            }
        } catch (Exception e) {
            log.error("Error during scheduled match loading for Bet365: {}", e.getMessage());
        }
    }
}
