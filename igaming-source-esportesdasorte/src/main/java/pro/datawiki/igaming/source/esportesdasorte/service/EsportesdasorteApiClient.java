package pro.datawiki.igaming.source.esportesdasorte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EsportesdasorteApiClient {

    private final EsportesdasorteDiscoveryService discoveryService;

    public void fetchSports() {
        log.info("Fetching sports and events via Esportes da Sorte discovery service...");
        discoveryService.discoverEvents();
    }
}
