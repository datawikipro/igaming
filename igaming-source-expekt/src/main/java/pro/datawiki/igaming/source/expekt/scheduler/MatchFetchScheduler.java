package pro.datawiki.igaming.source.expekt.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.expekt.service.MatchService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler")
public class MatchFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${expekt.fetch.delay.ms:15000}")
    public void scheduleFetch() {
        log.debug("League-crawler: starting Expekt discovery cycle...");
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error during Expekt discovery cycle", e);
        }
    }
}