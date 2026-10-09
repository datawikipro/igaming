package pro.datawiki.igaming.source.stake.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.stake.config.StakeConfig;
import pro.datawiki.igaming.source.stake.dto.StakeFixtureDto;
import pro.datawiki.igaming.source.stake.dto.StakeGraphQLRequest;
import pro.datawiki.igaming.source.stake.dto.StakeGraphQLResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class StakeApiClient {

    private final RestTemplate stakeRestTemplate;
    private final StakeConfig config;

    private static final String SPORT_FIXTURES_QUERY = """
            query GetSportFixtures($sport: String!, $limit: Int!) {
              sport(slug: $sport) {
                id
                name
                slug
                fixtures(limit: $limit) {
                  id
                  slug
                  name
                  status
                  startTime
                  sport {
                    id
                    name
                    slug
                  }
                  tournament {
                    id
                    name
                    slug
                  }
                  competitors {
                    id
                    name
                    qualifier
                  }
                  markets {
                    id
                    name
                    status
                    outcomes {
                      id
                      name
                      odds
                      active
                      line
                      handicap
                      total
                    }
                  }
                }
              }
            }
            """;

    private static final List<String> TARGET_SPORTS = List.of(
            "soccer",
            "basketball",
            "tennis",
            "ice-hockey",
            "baseball",
            "american-football",
            "mma",
            "esports"
    );

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        headers.set("Origin", "https://stake.com");
        headers.set("Referer", "https://stake.com/sports");
        return headers;
    }

    public List<StakeFixtureDto> fetchAllFixtures() {
        List<StakeFixtureDto> all = new ArrayList<>();
        for (String sportSlug : TARGET_SPORTS) {
            try {
                List<StakeFixtureDto> fixtures = fetchFixturesBySport(sportSlug, 50);
                if (fixtures != null && !fixtures.isEmpty()) {
                    all.addAll(fixtures);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch Stake fixtures for sport {}: {}", sportSlug, e.getMessage());
            }
        }
        return all;
    }

    public List<StakeFixtureDto> fetchFixturesBySport(String sportSlug, int limit) {
        try {
            StakeGraphQLRequest request = new StakeGraphQLRequest(
                    SPORT_FIXTURES_QUERY,
                    Map.of("sport", sportSlug, "limit", limit)
            );

            HttpEntity<StakeGraphQLRequest> entity = new HttpEntity<>(request, createHeaders());

            ResponseEntity<StakeGraphQLResponse> response = stakeRestTemplate.exchange(
                    config.getGraphqlUrl(),
                    HttpMethod.POST,
                    entity,
                    StakeGraphQLResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                StakeGraphQLResponse body = response.getBody();
                if (body.getData() != null) {
                    if (body.getData().getSport() != null && body.getData().getSport().getFixtures() != null) {
                        return body.getData().getSport().getFixtures();
                    }
                    if (body.getData().getFixtures() != null) {
                        return body.getData().getFixtures();
                    }
                    if (body.getData().getUpcomingFixtures() != null) {
                        return body.getData().getUpcomingFixtures();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error calling Stake GraphQL for sport {}: {}", sportSlug, e.getMessage());
        }
        return Collections.emptyList();
    }
}
