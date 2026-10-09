package pro.datawiki.igaming.source.bet9ja.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.bet9ja.dto.Bet9jaEventDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class Bet9jaDiscoveryService {

    private final Bet9jaApiClient apiClient;
    private final MatchService matchService;

    public void discoverAndSync() {
        log.debug("Running Bet9ja discovery cycle...");
        List<Bet9jaEventDto> events = apiClient.fetchActiveEvents();
        if (!events.isEmpty()) {
            matchService.processEvents(events);
        }
    }
}