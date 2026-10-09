package pro.datawiki.igaming.source.supersport.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.supersport.service.MatchService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperSportFetchScheduler {

    private final MatchService matchService;

    @Scheduled(fixedDelayString = "${app.crawler.discovery-delay:120000}")
    public void scheduleDiscovery() {
        try {
            matchService.discoverEvents();
        } catch (Exception e) {
            log.error("Error in SuperSport discovery schedule: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${app.crawler.odds-delay:20000}")
    public void scheduleOddsFetch() {
        try {
            matchService.fetchOddsForActiveMatches();
        } catch (Exception e) {
            log.error("Error in SuperSport odds fetch schedule: {}", e.getMessage());
        }
    }
}
