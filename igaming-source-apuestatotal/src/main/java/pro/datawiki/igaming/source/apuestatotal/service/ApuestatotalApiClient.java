package pro.datawiki.igaming.source.apuestatotal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApuestatotalApiClient {

    private final ApuestatotalDiscoveryService discoveryService;

    public void fetchSports() {
        log.info("Fetching sports and events via Apuesta Total discovery service...");
        discoveryService.discoverEvents();
    }
}
