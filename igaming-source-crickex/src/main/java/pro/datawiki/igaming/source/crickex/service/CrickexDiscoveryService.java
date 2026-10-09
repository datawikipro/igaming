package pro.datawiki.igaming.source.crickex.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.crickex.dto.CrickexEventDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrickexDiscoveryService {

    private final CrickexApiClient apiClient;
    private final MatchService matchService;

    public void discoverAndSync() {
        log.debug("Running Crickex discovery cycle...");
        List<CrickexEventDto> events = apiClient.fetchActiveEvents();
        if (!events.isEmpty()) {
            matchService.processEvents(events);
        }
    }
}
