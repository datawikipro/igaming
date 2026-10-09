package pro.datawiki.igaming.source.tab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.tab.dto.TabEventDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TabDiscoveryService {

    private final TabApiClient apiClient;
    private final MatchService matchService;

    public void discoverAndSync() {
        log.debug("Running Tab discovery cycle...");
        List<TabEventDto> events = apiClient.fetchActiveEvents();
        if (!events.isEmpty()) {
            matchService.processEvents(events);
        }
    }
}
