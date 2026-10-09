package pro.datawiki.igaming.source.winamax.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.winamax.dto.WinamaxEventDto;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WinamaxApiClient {

    private final WebClient winamaxWebClient;

    public List<WinamaxEventDto> fetchUpcomingEvents() {
        try {
            log.info("Fetching upcoming events from Winamax");
            List<WinamaxEventDto> events = winamaxWebClient.get()
                    .uri("/events/upcoming")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<WinamaxEventDto>>() {})
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch upcoming events from Winamax: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<WinamaxEventDto> fetchLiveEvents() {
        try {
            log.info("Fetching live events from Winamax");
            List<WinamaxEventDto> events = winamaxWebClient.get()
                    .uri("/events/live")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<WinamaxEventDto>>() {})
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch live events from Winamax: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}