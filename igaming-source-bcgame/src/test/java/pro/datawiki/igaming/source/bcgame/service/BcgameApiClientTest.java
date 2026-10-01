package pro.datawiki.igaming.source.bcgame.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.bcgame.config.BcgameConfig;
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameSportDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BcgameApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BcgameConfig config;
    private ObjectMapper objectMapper;
    private BcgameApiClient apiClient;

    @BeforeEach
    void setUp() {
        config = new BcgameConfig();
        config.setBaseUrl("https://bc.game/api/sports");
        objectMapper = new ObjectMapper();
        apiClient = new BcgameApiClient(restTemplate, config, objectMapper);
    }

    @Test
    @DisplayName("Should successfully fetch sports catalog when data array is returned")
    void testGetSportsSuccessWithDataNode() {
        String json = """
                {
                    "code": 0,
                    "data": [
                        {"id": "1", "name": "Soccer", "slug": "soccer", "liveCount": 10},
                        {"id": "2", "name": "Basketball", "slug": "basketball", "liveCount": 5}
                    ]
                }
                """;

        when(restTemplate.exchange(
                eq("https://bc.game/api/sports/v1/sports"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(json, HttpStatus.OK));

        List<BcgameSportDto> sports = apiClient.getSports();

        assertNotNull(sports);
        assertEquals(2, sports.size());
        assertEquals("1", sports.get(0).getId());
        assertEquals("Soccer", sports.get(0).getName());
        assertEquals("2", sports.get(1).getId());
        assertEquals("Basketball", sports.get(1).getName());
    }

    @Test
    @DisplayName("Should successfully fetch sports catalog when direct array is returned")
    void testGetSportsSuccessWithDirectArray() {
        String json = """
                [
                    {"id": "5", "name": "CS2"},
                    {"id": "6", "name": "Dota 2"}
                ]
                """;

        when(restTemplate.exchange(
                eq("https://bc.game/api/sports/v1/sports"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(json, HttpStatus.OK));

        List<BcgameSportDto> sports = apiClient.getSports();

        assertNotNull(sports);
        assertEquals(2, sports.size());
        assertEquals("5", sports.get(0).getId());
        assertEquals("CS2", sports.get(0).getName());
    }

    @Test
    @DisplayName("Should use fallback sports catalog when sports fetch fails")
    void testGetSportsFallbackOnException() {
        when(restTemplate.exchange(
                eq("https://bc.game/api/sports/v1/sports"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RestClientException("Connection timed out"));

        List<BcgameSportDto> sports = apiClient.getSports();

        assertNotNull(sports);
        assertFalse(sports.isEmpty());
        assertEquals(8, sports.size());
        assertTrue(sports.stream().anyMatch(s -> "CS2".equals(s.getName())));
        assertTrue(sports.stream().anyMatch(s -> "Soccer".equals(s.getName())));
    }

    @Test
    @DisplayName("Should successfully fetch events list for sport")
    void testGetEventsSuccess() {
        String json = """
                {
                    "data": [
                        {
                            "id": "ev-1",
                            "name": "Arsenal vs Chelsea",
                            "homeTeam": "Arsenal",
                            "awayTeam": "Chelsea",
                            "sportId": "1",
                            "isLive": false
                        },
                        {
                            "id": "ev-2",
                            "name": "Liverpool vs Man City",
                            "homeTeam": "Liverpool",
                            "awayTeam": "Man City",
                            "sportId": "1",
                            "isLive": true
                        }
                    ]
                }
                """;

        when(restTemplate.exchange(
                eq("https://bc.game/api/sports/v1/events?sportId=1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(json, HttpStatus.OK));

        List<BcgameEventDto> events = apiClient.getEvents("1");

        assertNotNull(events);
        assertEquals(2, events.size());
        assertEquals("ev-1", events.get(0).getId());
        assertEquals("Arsenal", events.get(0).getHomeTeam());
        assertEquals("Chelsea", events.get(0).getAwayTeam());
        assertFalse(events.get(0).getIsLive());

        assertEquals("ev-2", events.get(1).getId());
        assertTrue(events.get(1).getIsLive());
    }

    @Test
    @DisplayName("Should return empty list on events fetch error")
    void testGetEventsError() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RestClientException("503 Service Unavailable"));

        List<BcgameEventDto> events = apiClient.getEvents("99");

        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    @DisplayName("Should successfully fetch event details with markets and outcomes")
    void testGetEventDetailsSuccess() {
        String json = """
                {
                    "data": {
                        "id": "ev-100",
                        "name": "Real Madrid vs Barcelona",
                        "homeTeam": "Real Madrid",
                        "awayTeam": "Barcelona",
                        "sportName": "Soccer",
                        "markets": [
                            {
                                "id": "m-1",
                                "name": "1X2",
                                "outcomes": [
                                    {"id": "o-1", "name": "Real Madrid", "odds": 2.10},
                                    {"id": "o-2", "name": "Draw", "odds": 3.40},
                                    {"id": "o-3", "name": "Barcelona", "odds": 3.20}
                                ]
                            }
                        ]
                    }
                }
                """;

        when(restTemplate.exchange(
                eq("https://bc.game/api/sports/v1/events/ev-100"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(json, HttpStatus.OK));

        BcgameEventDto details = apiClient.getEventDetails("ev-100");

        assertNotNull(details);
        assertEquals("ev-100", details.getId());
        assertEquals("Real Madrid", details.getHomeTeam());
        assertEquals(1, details.getMarkets().size());
        assertEquals("1X2", details.getMarkets().get(0).getName());
        assertEquals(3, details.getMarkets().get(0).getOutcomes().size());
    }

    @Test
    @DisplayName("Should return null on event details fetch error")
    void testGetEventDetailsError() {
        when(restTemplate.exchange(
                eq("https://bc.game/api/sports/v1/events/ev-404"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RestClientException("404 Not Found"));

        BcgameEventDto details = apiClient.getEventDetails("ev-404");

        assertNull(details);
    }

    @Test
    @DisplayName("Should include required headers in requests")
    void testRequestHeaders() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("{\"data\": []}", HttpStatus.OK));

        apiClient.getEvents("1");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), entityCaptor.capture(), eq(String.class));

        HttpEntity entity = entityCaptor.getValue();
        assertNotNull(entity.getHeaders());
        assertNotNull(entity.getHeaders().getFirst("User-Agent"));
        assertTrue(entity.getHeaders().getFirst("User-Agent").contains("Mozilla/5.0"));
        assertEquals("application/json", entity.getHeaders().getFirst("Accept"));
    }
}
