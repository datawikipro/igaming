package pro.datawiki.igaming.source.vaidebet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class VaidebetApiClient {

    private final VaidebetDiscoveryService discoveryService;

    public void fetchSports() {
        log.info("Fetching sports and events via Vaidebet discovery service...");
        discoveryService.discoverEvents();
    }
}
