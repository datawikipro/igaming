package pro.datawiki.igaming.source.betmgm.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betmgm.service.BetMGMApiClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class BetMGMScraperScheduler {

    private final BetMGMApiClient apiClient;

    @Scheduled(fixedDelay = 30000, initialDelay = 5000)
    public void scrape() {
        log.info("Starting BetMGM scraping cycle...");
        // Use generic placeholders for now, the user can configure exact IDs later
        apiClient.processLeague("11", "American Football", "NFL");
        apiClient.processLeague("7", "Basketball", "NBA");
        apiClient.processLeague("12", "Ice Hockey", "NHL");
        apiClient.processLeague("23", "Baseball", "MLB");
    }
}
