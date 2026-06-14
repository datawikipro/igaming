package pro.datawiki.igaming.source.fanduel.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.fanduel.service.FanDuelApiClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class FanDuelScraperScheduler {

    private final FanDuelApiClient apiClient;

    @Scheduled(fixedDelay = 30000, initialDelay = 5000)
    public void scrape() {
        log.info("Starting FanDuel scraping cycle...");
        // Use generic placeholders for now, the user can configure exact IDs later
        apiClient.processLeague("830.1", "American Football", "NFL");
        apiClient.processLeague("264.1", "Basketball", "NBA");
        apiClient.processLeague("323.1", "Ice Hockey", "NHL");
        apiClient.processLeague("345.1", "Baseball", "MLB");
    }
}
