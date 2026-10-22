package pro.datawiki.igaming.source.betb2b.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betb2b.service.Betb2bService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnExpression("'${app.role:league-crawler}'.contains('league-crawler') or '${app.role:}'.equals('unified')")
public class MatchFetchScheduler {

    private final Betb2bService matchService;

    @Scheduled(fixedDelayString = "${app.betb2b.fetch-interval-seconds:300}000")
    public void fetchMatches() {
        try (var mdc = MDC.putCloseable("role", "CRAWLER")) {
            log.info("Starting periodic event discovery for Betb2b Family...");
            matchService.discoverEvents();
        }
    }
}
