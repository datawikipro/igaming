package pro.datawiki.igaming.capture.sofascore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.domain.SofaRawEventEntity;
import pro.datawiki.igaming.capture.sofascore.dto.SofaTaskDto;
import pro.datawiki.igaming.capture.sofascore.provider.SofaScoreEnrichmentProvider;
import pro.datawiki.igaming.capture.sofascore.queue.SofaQueueService;
import pro.datawiki.igaming.capture.sofascore.repository.SofaRawEventRepository;
import pro.datawiki.igaming.dto.MatchEnrichmentDto;
import pro.datawiki.igaming.dto.MatchLineupsDto;
import pro.datawiki.igaming.dto.ondemand.SofaOnDemandRequest;
import pro.datawiki.igaming.dto.ondemand.SofaOnDemandResponse;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SofaOnDemandServiceTest {

    @Mock
    private SofaScoreEnrichmentProvider enrichmentProvider;

    @Mock
    private SofaQueueService queueService;

    @Mock
    private SofaRawEventRepository rawEventRepository;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SofaOnDemandService onDemandService;

    private MatchEnrichmentDto sampleEnrichment;
    private SofaRawEventEntity sampleRawEntity;

    @BeforeEach
    void setUp() {
        MatchLineupsDto lineups = MatchLineupsDto.builder()
                .confirmed(true)
                .formationTeam1("4-3-3")
                .formationTeam2("4-2-3-1")
                .startersTeam1(List.of(
                        MatchLineupsDto.PlayerLineupDto.builder().name("Bukayo Saka").shirtNumber(7).position("F").build(),
                        MatchLineupsDto.PlayerLineupDto.builder().name("Declan Rice").shirtNumber(41).position("M").build()
                ))
                .startersTeam2(List.of(
                        MatchLineupsDto.PlayerLineupDto.builder().name("Cole Palmer").shirtNumber(20).position("M").build()
                ))
                .missingTeam1(List.of(
                        MatchLineupsDto.MissingPlayerDto.builder().name("Martin Odegaard").reason("Травма лодыжки").type("INJURY").build()
                ))
                .missingTeam2(List.of(
                        MatchLineupsDto.MissingPlayerDto.builder().name("Reece James").reason("Мышечная травма").type("INJURY").build()
                ))
                .build();

        sampleEnrichment = MatchEnrichmentDto.builder()
                .lineups(lineups)
                .updatedAt(System.currentTimeMillis())
                .build();

        sampleRawEntity = SofaRawEventEntity.builder()
                .eventId(12345L)
                .sport("football")
                .tournament("Premier League")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .status("IN_PROGRESS")
                .homeScore(2)
                .awayScore(1)
                .capturedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("refreshMatchSync should return fresh match data with confirmed lineups and starters")
    void testRefreshMatchSync_Success() {
        SofaOnDemandRequest request = SofaOnDemandRequest.builder()
                .matchId(55L)
                .eventId(12345L)
                .sport("football")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .async(false)
                .build();

        when(enrichmentProvider.fetchEnrichment(12345L)).thenReturn(Optional.of(sampleEnrichment));
        when(rawEventRepository.findById(12345L)).thenReturn(Optional.of(sampleRawEntity));

        SofaOnDemandResponse response = onDemandService.refreshMatchSync(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getHomeScore()).isEqualTo(2);
        assertThat(response.getAwayScore()).isEqualTo(1);
        assertThat(response.isLineupsConfirmed()).isTrue();
        assertThat(response.getStartersHome()).contains("Bukayo Saka");
        assertThat(response.getMissingHome()).anyMatch(m -> m.contains("Martin Odegaard"));
        assertThat(response.getMissingAway()).anyMatch(m -> m.contains("Reece James"));

        verify(queueService, times(1)).saveTaskResult(eq(response.getTaskId()), any());
    }

    @Test
    @DisplayName("submitMatchRefreshAsync should enqueue task with priority 0 and return QUEUED status")
    void testSubmitMatchRefreshAsync() {
        SofaOnDemandRequest request = SofaOnDemandRequest.builder()
                .matchId(55L)
                .eventId(12345L)
                .sport("football")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .async(true)
                .build();

        SofaOnDemandResponse response = onDemandService.submitMatchRefreshAsync(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("QUEUED");
        assertThat(response.getTaskId()).startsWith("sod-async-");

        verify(queueService, times(1)).pushOnDemandTask(argThat(task ->
                task.getType() == SofaTaskDto.TaskType.ON_DEMAND &&
                task.getPriority() == 0 &&
                task.getEventId().equals(12345L)
        ));
    }

    @Test
    @DisplayName("getTaskStatus should return cached result if completed")
    void testGetTaskStatus_FromCache() {
        String taskId = "sod-123";
        SofaOnDemandResponse cached = SofaOnDemandResponse.builder()
                .taskId(taskId)
                .status("COMPLETED")
                .homeScore(3)
                .awayScore(0)
                .build();

        when(queueService.getTaskResult(taskId)).thenReturn(cached);

        SofaOnDemandResponse response = onDemandService.getTaskStatus(taskId);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getHomeScore()).isEqualTo(3);
    }

    @Test
    @DisplayName("processTask should execute queued task and save result to Redis")
    void testProcessTask() {
        SofaTaskDto task = SofaTaskDto.builder()
                .taskId("sod-async-777")
                .eventId(12345L)
                .sport("football")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .type(SofaTaskDto.TaskType.ON_DEMAND)
                .priority(0)
                .build();

        when(enrichmentProvider.fetchEnrichment(12345L)).thenReturn(Optional.of(sampleEnrichment));
        when(rawEventRepository.findById(12345L)).thenReturn(Optional.of(sampleRawEntity));

        SofaOnDemandResponse response = onDemandService.processTask(task);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        verify(queueService, times(1)).setTaskStatus("sod-async-777", "PROCESSING");
        verify(queueService, times(1)).saveTaskResult(eq("sod-async-777"), any());
    }
}
