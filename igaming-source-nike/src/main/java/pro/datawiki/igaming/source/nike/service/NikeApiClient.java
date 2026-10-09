package pro.datawiki.igaming.source.nike.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.nike.dto.NikeEventDto;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NikeApiClient {

    private final WebClient nikeWebClient;

    public List<NikeEventDto> fetchUpcomingEvents() {
        try {
            log.info("Fetching upcoming events from Niké (nike.sk)");
            List<NikeEventDto> events = nikeWebClient.get()
                    .uri("/api/events/upcoming")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<NikeEventDto>>() {})
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch upcoming events from Niké (nike.sk): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<NikeEventDto> fetchLiveEvents() {
        try {
            log.info("Fetching live events from Niké (nike.sk)");
            List<NikeEventDto> events = nikeWebClient.get()
                    .uri("/api/events/live")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<NikeEventDto>>() {})
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch live events from Niké (nike.sk): {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
