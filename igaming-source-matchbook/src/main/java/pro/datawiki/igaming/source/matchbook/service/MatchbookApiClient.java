package pro.datawiki.igaming.source.matchbook.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.datawiki.igaming.source.matchbook.config.MatchbookConfig;
import pro.datawiki.igaming.source.matchbook.dto.MatchbookEvent;
import pro.datawiki.igaming.source.matchbook.dto.MatchbookEventsResponse;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class MatchbookApiClient {

    private final RestTemplate restTemplate;
    private final MatchbookConfig config;

    public MatchbookApiClient(@Qualifier("matchbookRestTemplate") RestTemplate restTemplate,
                              MatchbookConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        return headers;
    }

    public List<MatchbookEvent> fetchEvents(int offset, int perPage) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                    .path("/events")
                    .queryParam("states", "open")
                    .queryParam("include-prices", "true")
                    .queryParam("offset", offset)
                    .queryParam("per-page", perPage > 0 ? perPage : config.getApi().getBatchSize())
                    .toUriString();

            ResponseEntity<MatchbookEventsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    MatchbookEventsResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getEvents();
            }
        } catch (Exception e) {
            log.error("Failed to fetch Matchbook events (offset={}, perPage={}): {}", offset, perPage, e.getMessage());
        }
        return Collections.emptyList();
    }
}
