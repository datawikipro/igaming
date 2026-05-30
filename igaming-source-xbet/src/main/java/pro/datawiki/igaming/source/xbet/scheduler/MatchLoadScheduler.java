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
@ConditionalOnProperty(name = "app.role", havingValue = "match-loader")
public class MatchLoadScheduler {

    private final XbetService matchService;

    @Scheduled(fixedDelayString = "${app.match-loader.poll-delay-ms:5000}")
    public void loadMatches() {
        matchService.loadMatchCards(5);
    }
}
