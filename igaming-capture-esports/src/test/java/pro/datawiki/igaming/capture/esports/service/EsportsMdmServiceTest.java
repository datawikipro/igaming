package pro.datawiki.igaming.capture.esports.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.esports.Cs2Sport;
import pro.datawiki.igaming.dto.sport.esports.Dota2Sport;
import pro.datawiki.igaming.dto.sport.esports.ValorantSport;
import pro.datawiki.igaming.mdm.client.MdmIngestionClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EsportsMdmServiceTest {

    private MdmIngestionClient mdmIngestionClient;
    private KafkaTemplate<String, Object> kafkaTemplate;
    private EsportsMdmService esportsMdmService;

    @BeforeEach
    void setUp() {
        mdmIngestionClient = Mockito.mock(MdmIngestionClient.class);
        kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        esportsMdmService = new EsportsMdmService(mdmIngestionClient, Optional.of(kafkaTemplate));
    }

    @Test
    @DisplayName("Should resolve polymorphic Sport instances for CS2, Dota 2, and Valorant")
    void testResolveSportInstances() {
        Sport cs2 = esportsMdmService.resolveSportInstance("CS2");
        assertThat(cs2).isInstanceOf(Cs2Sport.class);
        assertThat(cs2.getCode()).isEqualTo("CS2");
        assertThat(cs2.getSlug()).isEqualTo("cs2");

        Sport dota2 = esportsMdmService.resolveSportInstance("DOTA2");
        assertThat(dota2).isInstanceOf(Dota2Sport.class);
        assertThat(dota2.getCode()).isEqualTo("DOTA2");
        assertThat(dota2.getSlug()).isEqualTo("dota2");

        Sport val = esportsMdmService.resolveSportInstance("VALORANT");
        assertThat(val).isInstanceOf(ValorantSport.class);
        assertThat(val.getCode()).isEqualTo("VALORANT");
        assertThat(val.getSlug()).isEqualTo("valorant");
    }

    @Test
    @DisplayName("Should resolve match as MAPPED when sport, teams, and match ID exist in blueprint")
    void testResolveMatchSuccess() {
        MdmIngestionClient.IngestionBlueprint bp = new MdmIngestionClient.IngestionBlueprint();
        bp.sports.put("CS2", new MdmIngestionClient.CanonicalSport(10, "cs2", "CS2"));
        bp.teams.put("natus vincere", new MdmIngestionClient.CanonicalTeam(101L, 10, "navi", 1001L, "Natus Vincere"));
        bp.teams.put("faze clan", new MdmIngestionClient.CanonicalTeam(102L, 10, "faze", 1002L, "FaZe Clan"));
        bp.tournaments.put("blast premier", new MdmIngestionClient.CanonicalTournament(501L, 10, "blast", 5001L, "BLAST Premier"));

        when(mdmIngestionClient.getBlueprint(anyString())).thenReturn(bp);
        when(mdmIngestionClient.resolveMatchId(anyString(), eq(10), eq(501L), eq(101L), eq(102L), anyLong()))
                .thenReturn(Optional.of(999001L));

        EsportsMdmService.MatchResolutionResult result = esportsMdmService.resolveMatch(
                "CS2", "blast premier", "Natus Vincere", "FaZe Clan", 1700000000L);

        assertThat(result.isMapped()).isTrue();
        assertThat(result.getMatchId()).isEqualTo(999001L);
        assertThat(result.getHomeTeamId()).isEqualTo(101L);
        assertThat(result.getAwayTeamId()).isEqualTo(102L);
    }

    @Test
    @DisplayName("Should report anomaly and mark as UNMAPPED when team is not found in blueprint")
    void testResolveMatchUnmappedTeam() {
        MdmIngestionClient.IngestionBlueprint bp = new MdmIngestionClient.IngestionBlueprint();
        bp.sports.put("CS2", new MdmIngestionClient.CanonicalSport(10, "cs2", "CS2"));
        bp.teams.put("natus vincere", new MdmIngestionClient.CanonicalTeam(101L, 10, "navi", 1001L, "Natus Vincere"));

        when(mdmIngestionClient.getBlueprint(anyString())).thenReturn(bp);

        EsportsMdmService.MatchResolutionResult result = esportsMdmService.resolveMatch(
                "CS2", "blast premier", "Natus Vincere", "Unknown Team XYZ", 1700000000L);

        assertThat(result.isMapped()).isFalse();
        verify(mdmIngestionClient).reportAnomaly(anyString(), eq("TEAM"), eq("Unknown Team XYZ"), eq("Unknown Team XYZ"), any(), anyString());
    }

    @Test
    @DisplayName("Should invalidate MDM blueprint cache on refresh")
    void testRefreshBlueprint() {
        esportsMdmService.refreshBlueprint();
        verify(mdmIngestionClient).invalidateCache(anyString());
    }
}
