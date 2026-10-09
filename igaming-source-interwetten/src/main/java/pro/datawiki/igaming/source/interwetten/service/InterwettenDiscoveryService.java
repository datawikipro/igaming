package pro.datawiki.igaming.source.interwetten.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.interwetten.dto.InterwettenEventDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterwettenDiscoveryService {

    private final InterwettenApiClient apiClient;
    private final MatchService matchService;

    public void discoverAndSync() {
        log.debug("Running Interwetten discovery cycle...");
        List<InterwettenEventDto> events = apiClient.fetchActiveEvents();
        if (!events.isEmpty()) {
            matchService.processEvents(events);
        }
    }
}