package pro.datawiki.igaming.capture.sofascore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.dto.BackfillResult;
import pro.datawiki.igaming.dto.raw.SofaRawEventDto;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SofaHistoryBackfillServiceTest {

    @Mock
    private RestTemplate sofaScoreRestTemplate;

    @Mock
    private SofaRawEventService rawEventService;

    private ObjectMapper objectMapper;
    private SofaHistoryBackfillService backfillService;

    private static final String SAMPLE_JSON = """
            {
              "events": [
                {
                  "id": 11854321,
                  "slug": "real-madrid-barcelona",
                  "startTimestamp": 1729080000,
                  "tournament": {
                    "name": "LaLiga"
                  },
                  "season": {
                    "name": "LaLiga 24/25"
                  },
                  "status": {
                    "type": "finished",
                    "description": "Ended"
                  },
                  "homeTeam": {
                    "id": 2829,
                    "name": "Real Madrid"
                  },
                  "awayTeam": {
                    "id": 2817,
                    "name": "Barcelona"
                  },
                  "homeScore": {
                    "current": 2
                  },
                  "awayScore": {
                    "current": 1
                  }
                },
                {
                  "id": 11854322,
                  "slug": "arsenal-chelsea",
                  "startTimestamp": 1729083600,
                  "tournament": {
                    "name": "Premier League"
                  },
                  "season": {
                    "name": "Premier League 24/25"
                  },
                  "status": {
                    "type": "finished"
                  },
                  "homeTeam": {
                    "id": 42,
                    "name": "Arsenal"
                  },
                  "awayTeam": {
                    "id": 38,
                    "name": "Chelsea"
                  },
                  "homeScore": {
                    "current": 1
                  },
                  "awayScore": {
                    "current": 1
                  }
                }
              ]
            }
            """;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        backfillService = new SofaHistoryBackfillService(sofaScoreRestTemplate, objectMapper, rawEventService);
    }

    @Test
    @DisplayName("backfillDate parses events and saves raw JSON to DB and Kafka")
    void testBackfillDateSuccess() {
        LocalDate date = LocalDate.of(2024, 10, 16);
        ResponseEntity<String> response = new ResponseEntity<>(SAMPLE_JSON, HttpStatus.OK);

        when(sofaScoreRestTemplate.exchange(
                eq(SofaHistoryBackfillService.SOFASCORE_SCHEDULE_URL),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class),
                eq("football"),
                eq("2024-10-16")
        )).thenReturn(response);

        int ingested = backfillService.backfillDate("football", date);

        assertThat(ingested).isEqualTo(2);

        ArgumentCaptor<SofaRawEventDto> captor = ArgumentCaptor.forClass(SofaRawEventDto.class);
        verify(rawEventService, times(2)).recordRawEvent(captor.capture());

        List<SofaRawEventDto> dtos = captor.getAllValues();
        SofaRawEventDto event1 = dtos.get(0);
        assertThat(event1.getEventId()).isEqualTo(11854321L);
        assertThat(event1.getSport()).isEqualTo("football");
        assertThat(event1.getTournamentName()).isEqualTo("LaLiga");
        assertThat(event1.getSeason()).isEqualTo("LaLiga 24/25");
        assertThat(event1.getHomeTeam()).isEqualTo("Real Madrid");
        assertThat(event1.getAwayTeam()).isEqualTo("Barcelona");
        assertThat(event1.getHomeScore()).isEqualTo(2);
        assertThat(event1.getAwayScore()).isEqualTo(1);
        assertThat(event1.getStatus()).isEqualTo("finished");
        assertThat(event1.getEventJson()).contains("real-madrid-barcelona");

        SofaRawEventDto event2 = dtos.get(1);
        assertThat(event2.getEventId()).isEqualTo(11854322L);
        assertThat(event2.getHomeTeam()).isEqualTo("Arsenal");
        assertThat(event2.getAwayTeam()).isEqualTo("Chelsea");
    }

    @Test
    @DisplayName("backfillDate handles 403 Forbidden without throwing exception")
    void testBackfillDateHandles403() {
        LocalDate date = LocalDate.of(2024, 10, 16);

        when(sofaScoreRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class),
                anyString(),
                anyString()
        )).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Forbidden Varnish"));

        int ingested = backfillService.backfillDate("football", date);

        assertThat(ingested).isEqualTo(0);
        verify(rawEventService, never()).recordRawEvent(any());
    }

    @Test
    @DisplayName("backfillDate handles empty events array")
    void testBackfillDateEmptyEvents() {
        LocalDate date = LocalDate.of(2024, 10, 16);
        ResponseEntity<String> response = new ResponseEntity<>("{\"events\":[]}", HttpStatus.OK);

        when(sofaScoreRestTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class), anyString(), anyString()
        )).thenReturn(response);

        int ingested = backfillService.backfillDate("football", date);

        assertThat(ingested).isEqualTo(0);
        verify(rawEventService, never()).recordRawEvent(any());
    }

    @Test
    @DisplayName("backfillRange iterates over dates and returns summary")
    void testBackfillRange() {
        LocalDate start = LocalDate.of(2024, 10, 16);
        LocalDate end = LocalDate.of(2024, 10, 18);
        ResponseEntity<String> response = new ResponseEntity<>(SAMPLE_JSON, HttpStatus.OK);

        when(sofaScoreRestTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class), anyString(), anyString()
        )).thenReturn(response);

        BackfillResult result = backfillService.backfillRange("football", start, end, 10);

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getTotalDatesProcessed()).isEqualTo(3);
        assertThat(result.getTotalEventsIngested()).isEqualTo(6); // 2 events * 3 days
        assertThat(result.getErrorsCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("mapSportToSlug correctly normalizes sports")
    void testMapSportToSlug() {
        assertThat(backfillService.mapSportToSlug("FOOTBALL")).isEqualTo("football");
        assertThat(backfillService.mapSportToSlug("HOCKEY")).isEqualTo("ice-hockey");
        assertThat(backfillService.mapSportToSlug("TENNIS")).isEqualTo("tennis");
        assertThat(backfillService.mapSportToSlug("CS2")).isEqualTo("esports");
        assertThat(backfillService.mapSportToSlug("DOTA2")).isEqualTo("esports");
        assertThat(backfillService.mapSportToSlug("Basketball")).isEqualTo("basketball");
    }
}
