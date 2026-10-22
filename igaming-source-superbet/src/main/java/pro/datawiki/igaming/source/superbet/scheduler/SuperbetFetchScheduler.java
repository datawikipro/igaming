package pro.datawiki.igaming.source.superbet.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.superbet.service.MatchService;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnExpression("'${spring.profiles.active:}' == 'crawler' or '${app.role:}' == 'unified'")
public class SuperbetFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${app.crawler.discovery-delay:120000}", initialDelay = 5000)
    public void runDiscovery() {
        try {
            log.info("Starting scheduled Superbet discovery...");
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error during Superbet scheduled discovery: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${app.crawler.odds-delay:20000}", initialDelay = 15000)
    public void runOddsFetch() {
        try {
            matchService.fetchOddsForActiveMatches();
        } catch (Exception e) {
            log.error("Error during Superbet scheduled odds refresh: {}", e.getMessage());
        }
    }
}
