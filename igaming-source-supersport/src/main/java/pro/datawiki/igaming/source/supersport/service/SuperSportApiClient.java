package pro.datawiki.igaming.source.supersport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.supersport.dto.SuperSportEventDto;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuperSportApiClient {

    private final WebClient supersportWebClient;

    public List<SuperSportEventDto> fetchUpcomingEvents() {
        try {
            log.info("Fetching upcoming events from SuperSport (supersport.hr)");
            List<SuperSportEventDto> events = supersportWebClient.get()
                    .uri("/api/events/upcoming")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<SuperSportEventDto>>() {})
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch upcoming events from SuperSport (supersport.hr): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<SuperSportEventDto> fetchLiveEvents() {
        try {
            log.info("Fetching live events from SuperSport (supersport.hr)");
            List<SuperSportEventDto> events = supersportWebClient.get()
                    .uri("/api/events/live")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<SuperSportEventDto>>() {})
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch live events from SuperSport (supersport.hr): {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
