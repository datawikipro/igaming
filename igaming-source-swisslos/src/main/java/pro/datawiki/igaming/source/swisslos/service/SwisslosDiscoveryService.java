package pro.datawiki.igaming.source.swisslos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.swisslos.dto.SwisslosEventDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SwisslosDiscoveryService {

    private final SwisslosApiClient apiClient;
    private final MatchService matchService;

    public void discoverAndSync() {
        log.debug("Running Swisslos discovery cycle...");
        List<SwisslosEventDto> events = apiClient.fetchActiveEvents();
        if (!events.isEmpty()) {
            matchService.processEvents(events);
        }
    }
}
