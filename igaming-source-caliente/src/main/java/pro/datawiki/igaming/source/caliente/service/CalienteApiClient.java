package pro.datawiki.igaming.source.caliente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.caliente.dto.CalienteEventDto;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalienteApiClient {

    private final WebClient calienteWebClient;

    public List<CalienteEventDto> fetchUpcomingEvents() {
        try {
            log.info("Fetching upcoming events from Caliente");
            List<CalienteEventDto> events = calienteWebClient.get()
                    .uri("/events/upcoming")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CalienteEventDto>>() {})
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch upcoming events from Caliente: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<CalienteEventDto> fetchLiveEvents() {
        try {
            log.info("Fetching live events from Caliente");
            List<CalienteEventDto> events = calienteWebClient.get()
                    .uri("/events/live")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CalienteEventDto>>() {})
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch live events from Caliente: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}