package pro.datawiki.igaming.source.betb2b.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betb2b.service.Betb2bService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.role", havingValue = "match-loader")
public class MatchLoadScheduler {

    private final Betb2bService matchService;

    @Scheduled(fixedDelayString = "${app.match-loader.poll-delay-ms:5000}")
    public void loadMatches() {
        matchService.loadMatchCards(5);
    }
}
