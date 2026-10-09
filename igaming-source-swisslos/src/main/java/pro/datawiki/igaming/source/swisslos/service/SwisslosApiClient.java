package pro.datawiki.igaming.source.swisslos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.swisslos.dto.SwisslosEventDto;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SwisslosApiClient {

    private final WebClient swisslosWebClient;

    public List<SwisslosEventDto> fetchActiveEvents() {
        try {
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch events from Swisslos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
