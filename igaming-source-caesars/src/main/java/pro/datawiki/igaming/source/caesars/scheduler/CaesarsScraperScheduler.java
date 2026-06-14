package pro.datawiki.igaming.source.caesars.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.caesars.service.CaesarsApiClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaesarsScraperScheduler {

    private final CaesarsApiClient apiClient;

    @Scheduled(fixedDelay = 30000, initialDelay = 5000)
    public void scrape() {
        log.info("Starting Caesars scraping cycle...");
        // Use generic placeholders for now, the user can configure exact IDs later
        apiClient.processLeague("007d7c61-07a7-4e18-bb40-15104b6eac92", "American Football", "NFL");
        apiClient.processLeague("5806c896-4eeb-4de1-a2df-8a42bb135d55", "Basketball", "NBA");
        apiClient.processLeague("b7b715a9-c7e8-4c47-af0a-77385b525e09", "Ice Hockey", "NHL");
        apiClient.processLeague("04f90892-3afa-4e84-acce-5b89f151063d", "Baseball", "MLB");
    }
}
