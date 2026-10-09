package pro.datawiki.igaming.source.winner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.winner.dto.WinnerEventDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WinnerDiscoveryService {

    private final WinnerApiClient apiClient;
    private final MatchService matchService;

    public void discoverAndSync() {
        log.debug("Running Winner discovery cycle...");
        List<WinnerEventDto> events = apiClient.fetchActiveEvents();
        if (!events.isEmpty()) {
            matchService.processEvents(events);
        }
    }
}