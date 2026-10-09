package pro.datawiki.igaming.source.norsktipping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.norsktipping.dto.NorskTippingEventDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NorskTippingDiscoveryService {

    private final NorskTippingApiClient apiClient;
    private final MatchService matchService;

    public void discoverAndSync() {
        log.debug("Running NorskTipping discovery cycle...");
        List<NorskTippingEventDto> events = apiClient.fetchActiveEvents();
        if (!events.isEmpty()) {
            matchService.processEvents(events);
        }
    }
}
