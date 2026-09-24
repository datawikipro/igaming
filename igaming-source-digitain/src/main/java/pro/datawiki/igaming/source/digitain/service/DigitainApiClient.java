package pro.datawiki.igaming.source.digitain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DigitainApiClient {

    private final DigitainDiscoveryService discoveryService;

    public void fetchSports() {
        log.info("Fetching sports and events via Digitain discovery service...");
        discoveryService.discoverEvents();
    }
}
