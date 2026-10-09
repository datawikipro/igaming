package pro.datawiki.igaming.source.estave.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.estave.dto.EstaveEventDto;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstaveApiClient {

    private final WebClient estaveWebClient;

    public List<EstaveEventDto> fetchUpcomingEvents() {
        try {
            log.info("Fetching upcoming events from Športna Loterija (e-stave.com)");
            List<EstaveEventDto> events = estaveWebClient.get()
                    .uri("/api/events/upcoming")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<EstaveEventDto>>() {})
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch upcoming events from Športna Loterija (e-stave.com): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<EstaveEventDto> fetchLiveEvents() {
        try {
            log.info("Fetching live events from Športna Loterija (e-stave.com)");
            List<EstaveEventDto> events = estaveWebClient.get()
                    .uri("/api/events/live")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<EstaveEventDto>>() {})
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch live events from Športna Loterija (e-stave.com): {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
