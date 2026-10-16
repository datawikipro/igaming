package pro.datawiki.igaming.capture.sofascore.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pro.datawiki.igaming.capture.sofascore.dto.BackfillProgressDto;
import pro.datawiki.igaming.capture.sofascore.dto.BackfillResult;
import pro.datawiki.igaming.capture.sofascore.service.SofaHistoryBackfillService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SofaHistoryBackfillControllerTest {

    @Mock
    private SofaHistoryBackfillService backfillService;

    @InjectMocks
    private SofaHistoryBackfillController controller;

    @Test
    @DisplayName("POST /date triggers backfillDate and returns summary")
    void testBackfillDateEndpoint() {
        LocalDate date = LocalDate.of(2024, 10, 16);
        when(backfillService.backfillDate("football", date)).thenReturn(15);

        ResponseEntity<Map<String, Object>> response = controller.backfillDate("football", date);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("eventsIngested")).isEqualTo(15);
        assertThat(response.getBody().get("status")).isEqualTo("SUCCESS");
        verify(backfillService).backfillDate("football", date);
    }

    @Test
    @DisplayName("POST /range sync returns BackfillResult")
    void testBackfillRangeSync() {
        LocalDate start = LocalDate.of(2024, 10, 16);
        LocalDate end = LocalDate.of(2024, 10, 18);
        BackfillResult mockResult = BackfillResult.builder()
                .status("COMPLETED")
                .startDate(start)
                .endDate(end)
                .totalDatesProcessed(3)
                .totalEventsIngested(45)
                .build();

        when(backfillService.backfillRange("football", start, end, 250)).thenReturn(mockResult);

        ResponseEntity<?> response = controller.backfillRange("football", start, end, 250, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResult);
    }

    @Test
    @DisplayName("POST /range async triggers background execution and returns 202 Accepted")
    void testBackfillRangeAsync() {
        LocalDate start = LocalDate.of(2024, 10, 16);
        LocalDate end = LocalDate.of(2024, 10, 18);

        when(backfillService.backfillRangeAsync(anyList(), eq(start), eq(end), eq(250L)))
                .thenReturn(CompletableFuture.completedFuture(BackfillResult.builder().build()));

        ResponseEntity<?> response = controller.backfillRange("football", start, end, 250, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("status")).isEqualTo("STARTED");
        verify(backfillService).backfillRangeAsync(Collections.singletonList("football"), start, end, 250);
    }

    @Test
    @DisplayName("POST /all triggers multi-sport backfill")
    void testBackfillAllSports() {
        LocalDate start = LocalDate.of(2024, 10, 16);
        LocalDate end = LocalDate.of(2024, 10, 18);
        List<String> sports = List.of("football", "tennis");

        when(backfillService.backfillRangeAsync(eq(sports), eq(start), eq(end), eq(250L)))
                .thenReturn(CompletableFuture.completedFuture(BackfillResult.builder().build()));

        ResponseEntity<?> response = controller.backfillAllSports(sports, start, end, 250, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("status")).isEqualTo("STARTED");
        verify(backfillService).backfillRangeAsync(sports, start, end, 250);
    }

    @Test
    @DisplayName("GET /status returns current progress")
    void testGetStatus() {
        BackfillProgressDto mockProgress = BackfillProgressDto.builder()
                .running(true)
                .currentSport("football")
                .daysProcessed(10)
                .totalDays(100)
                .eventsIngested(250)
                .build();

        when(backfillService.getCurrentProgress()).thenReturn(mockProgress);

        ResponseEntity<BackfillProgressDto> response = controller.getStatus();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockProgress);
    }
}
