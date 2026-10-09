package pro.datawiki.igaming.source.iddaa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.iddaa.dto.IddaaEventDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class IddaaDiscoveryService {

    private final IddaaApiClient apiClient;
    private final MatchService matchService;

    public void discoverAndSync() {
        log.debug("Running Iddaa discovery cycle...");
        List<IddaaEventDto> events = apiClient.fetchActiveEvents();
        if (!events.isEmpty()) {
            matchService.processEvents(events);
        }
    }
}
