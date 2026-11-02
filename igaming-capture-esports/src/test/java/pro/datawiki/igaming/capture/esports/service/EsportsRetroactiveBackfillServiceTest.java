package pro.datawiki.igaming.capture.esports.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.capture.esports.domain.EsportsRawEventEntity;
import pro.datawiki.igaming.capture.esports.repository.EsportsRawEventRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EsportsRetroactiveBackfillServiceTest {

    private EsportsRawEventRepository repository;
    private EsportsMdmService mdmService;
    private EsportsRetroactiveBackfillService backfillService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(EsportsRawEventRepository.class);
        mdmService = Mockito.mock(EsportsMdmService.class);
        backfillService = new EsportsRetroactiveBackfillService(repository, mdmService);
    }

    @Test
    @DisplayName("Should scan unmapped events and enrich with MDM IDs upon successful matching")
    void testExecuteBackfillSuccess() {
        EsportsRawEventEntity unmappedEntity = EsportsRawEventEntity.builder()
                .eventId("hltv-1001")
                .game("CS2")
                .tournamentRaw("BLAST Premier")
                .team1Raw("Natus Vincere")
                .team2Raw("FaZe Clan")
                .matchDate(1700000000L)
                .mdmStatus("UNMAPPED")
                .build();

        when(repository.findUnmapped()).thenReturn(List.of(unmappedEntity));
        when(mdmService.resolveMatch("CS2", "BLAST Premier", "Natus Vincere", "FaZe Clan", 1700000000L))
                .thenReturn(EsportsMdmService.MatchResolutionResult.builder()
                        .mapped(true)
                        .matchId(555001L)
                        .homeTeamId(101L)
                        .awayTeamId(102L)
                        .build());

        EsportsRetroactiveBackfillService.BackfillReport report = backfillService.executeBackfill();

        assertThat(report.getStatus()).isEqualTo("COMPLETED");
        assertThat(report.getTotalScanned()).isEqualTo(1);
        assertThat(report.getNewlyMapped()).isEqualTo(1);
        assertThat(report.getStillUnmapped()).isEqualTo(0);

        assertThat(unmappedEntity.getMdmStatus()).isEqualTo("MAPPED");
        assertThat(unmappedEntity.getMdmMatchId()).isEqualTo(555001L);
        verify(repository).save(unmappedEntity);
        verify(mdmService).refreshBlueprint();
    }
}
