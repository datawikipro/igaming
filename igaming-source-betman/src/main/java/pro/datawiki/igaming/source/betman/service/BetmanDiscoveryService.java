package pro.datawiki.igaming.source.betman.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.betman.dto.BetmanEventDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetmanDiscoveryService {

    private final BetmanApiClient apiClient;
    private final MatchService matchService;

    public void discoverAndSync() {
        log.debug("Running Betman discovery cycle...");
        List<BetmanEventDto> events = apiClient.fetchActiveEvents();
        if (!events.isEmpty()) {
            matchService.processEvents(events);
        }
    }
}