package pro.datawiki.igaming.source.novig.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.novig.config.NovigConfig;
import pro.datawiki.igaming.source.novig.dto.NovigEventDto;
import pro.datawiki.igaming.source.novig.dto.NovigGraphQLRequest;
import pro.datawiki.igaming.source.novig.dto.NovigGraphQLResponse;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NovigApiClient {

    private final RestTemplate restTemplate;
    private final NovigConfig config;

    private static final String ACTIVE_EVENTS_QUERY = """
            query GetNovigActiveEvents($startTime: timestamptz!, $limit: Int!) {
              event(
                where: {
                  scheduled_start: { _gte: $startTime }
                  markets: { status: { _eq: "OPEN" } }
                }
                order_by: { scheduled_start: asc }
                limit: $limit
              ) {
                id
                description
                status
                scheduled_start
                league
                markets(where: { status: { _eq: "OPEN" } }) {
                  id
                  description
                  type
                  strike
                  status
                  outcomes {
                    id
                    description
                    type
                    available
                    altAvailable
                    last
                  }
                }
              }
            }
            """;

    public NovigApiClient(@Qualifier("novigRestTemplate") RestTemplate restTemplate,
                          NovigConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        headers.set("Origin", "https://app.novig.us");
        headers.set("Referer", "https://app.novig.us/");
        return headers;
    }

    public List<NovigEventDto> fetchUpcomingEvents(Instant since, int limit) {
        try {
            NovigGraphQLRequest request = new NovigGraphQLRequest(
                    ACTIVE_EVENTS_QUERY,
                    Map.of(
                            "startTime", since.toString(),
                            "limit", limit > 0 ? limit : config.getApi().getBatchSize()
                    )
            );

            HttpEntity<NovigGraphQLRequest> entity = new HttpEntity<>(request, createHeaders());

            ResponseEntity<NovigGraphQLResponse> response = restTemplate.exchange(
                    config.getApi().getGraphqlUrl(),
                    HttpMethod.POST,
                    entity,
                    NovigGraphQLResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                NovigGraphQLResponse body = response.getBody();
                if (body.getData() != null && body.getData().getEvent() != null) {
                    return body.getData().getEvent();
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch Novig active events via GraphQL: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }
}
