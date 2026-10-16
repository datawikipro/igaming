package pro.datawiki.igaming.capture.sofascore.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pro.datawiki.igaming.capture.sofascore.queue.SofaQueueService;
import pro.datawiki.igaming.capture.sofascore.service.SofaOnDemandService;
import pro.datawiki.igaming.dto.ondemand.SofaOnDemandRequest;
import pro.datawiki.igaming.dto.ondemand.SofaOnDemandResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SofaOnDemandControllerTest {

    @Mock
    private SofaOnDemandService onDemandService;

    @Mock
    private SofaQueueService queueService;

    @InjectMocks
    private SofaOnDemandController controller;

    private SofaOnDemandRequest syncRequest;
    private SofaOnDemandRequest asyncRequest;

    @BeforeEach
    void setUp() {
        syncRequest = SofaOnDemandRequest.builder()
                .matchId(101L)
                .eventId(1234567L)
                .sport("football")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .async(false)
                .build();

        asyncRequest = SofaOnDemandRequest.builder()
                .matchId(101L)
                .eventId(1234567L)
                .sport("football")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .async(true)
                .build();
    }

    @Test
    @DisplayName("Sync refresh should return HTTP 200 with COMPLETED status and lineups")
    void testRefreshMatch_Sync() {
        SofaOnDemandResponse expectedResponse = SofaOnDemandResponse.builder()
                .taskId("sod-sync-12345")
                .status("COMPLETED")
                .matchId(101L)
                .eventId(1234567L)
                .sport("football")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .statusType("IN_PROGRESS")
                .homeScore(2)
                .awayScore(1)
                .lineupsConfirmed(true)
                .startersHome(List.of("Raya", "Saliba", "Saka"))
                .startersAway(List.of("Sanchez", "Palmer"))
                .build();

        when(onDemandService.refreshMatchSync(any())).thenReturn(expectedResponse);

        ResponseEntity<SofaOnDemandResponse> response = controller.refreshMatch(syncRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getBody().isLineupsConfirmed()).isTrue();
        assertThat(response.getBody().getStartersHome()).contains("Saka");
        verify(onDemandService, times(1)).refreshMatchSync(any());
        verify(onDemandService, never()).submitMatchRefreshAsync(any());
    }

    @Test
    @DisplayName("Async refresh should return HTTP 202 with QUEUED status and taskId")
    void testRefreshMatch_Async() {
        SofaOnDemandResponse queuedResponse = SofaOnDemandResponse.builder()
                .taskId("sod-async-abc-123")
                .status("QUEUED")
                .matchId(101L)
                .eventId(1234567L)
                .build();

        when(onDemandService.submitMatchRefreshAsync(any())).thenReturn(queuedResponse);

        ResponseEntity<SofaOnDemandResponse> response = controller.refreshMatch(asyncRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("QUEUED");
        assertThat(response.getBody().getTaskId()).isEqualTo("sod-async-abc-123");
        verify(onDemandService, times(1)).submitMatchRefreshAsync(any());
        verify(onDemandService, never()).refreshMatchSync(any());
    }

    @Test
    @DisplayName("Get status should return task response when found")
    void testGetStatus_Found() {
        SofaOnDemandResponse doneResponse = SofaOnDemandResponse.builder()
                .taskId("sod-task-1")
                .status("COMPLETED")
                .homeScore(1)
                .awayScore(0)
                .build();

        when(onDemandService.getTaskStatus("sod-task-1")).thenReturn(doneResponse);

        ResponseEntity<SofaOnDemandResponse> response = controller.getStatus("sod-task-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("Get status should return HTTP 404 when task not found")
    void testGetStatus_NotFound() {
        SofaOnDemandResponse notFound = SofaOnDemandResponse.builder()
                .taskId("unknown")
                .status("NOT_FOUND")
                .message("No task found")
                .build();

        when(onDemandService.getTaskStatus("unknown")).thenReturn(notFound);

        ResponseEntity<SofaOnDemandResponse> response = controller.getStatus("unknown");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("Queue stats should return counts across all tiers")
    void testGetQueueStats() {
        when(queueService.getOnDemandQueueSize()).thenReturn(0L);
        when(queueService.getLiveQueueSize()).thenReturn(5L);
        when(queueService.getPrematchQueueSize()).thenReturn(12L);
        when(queueService.getHistoryQueueSize()).thenReturn(150L);

        ResponseEntity<Map<String, Object>> response = controller.getQueueStats();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("live_queue_size", 5L);
        assertThat(response.getBody()).containsEntry("history_queue_size", 150L);
    }
}
