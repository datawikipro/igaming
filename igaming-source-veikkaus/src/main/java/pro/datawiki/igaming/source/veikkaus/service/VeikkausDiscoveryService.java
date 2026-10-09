package pro.datawiki.igaming.source.veikkaus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.veikkaus.dto.VeikkausEventDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VeikkausDiscoveryService {

    private final VeikkausApiClient apiClient;
    private final MatchService matchService;

    public void discoverAndSync() {
        log.debug("Running Veikkaus discovery cycle...");
        List<VeikkausEventDto> events = apiClient.fetchActiveEvents();
        if (!events.isEmpty()) {
            matchService.processEvents(events);
        }
    }
}