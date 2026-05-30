package pro.datawiki.igaming.source.xbet.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.xbet.service.XbetService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.role", havingValue = "league-crawler", matchIfMissing = true)
public class MatchFetchScheduler {

    private final XbetService matchService;

    @Scheduled(fixedDelayString = "${app.xbet.fetch-interval-seconds:300}000")
    public void fetchMatches() {
        log.info("Starting periodic event discovery for 1xBet Family...");
        matchService.discoverEvents();
    }
}
