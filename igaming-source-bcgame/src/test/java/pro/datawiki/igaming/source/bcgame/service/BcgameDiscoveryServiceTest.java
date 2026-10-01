package pro.datawiki.igaming.source.bcgame.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import pro.datawiki.igaming.source.bcgame.dto.BcgameEventDto;
import pro.datawiki.igaming.source.bcgame.dto.BcgameSportDto;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BcgameDiscoveryServiceTest {

    @Mock
    private BcgameApiClient apiClient;

    @Mock
    private MatchPersistenceService persistenceService;

    @InjectMocks
    private BcgameDiscoveryService discoveryService;

    @BeforeEach
    void setUp() {
        discoveryService.clearCache();
    }

    @Test
    @DisplayName("discoverEvents: processes all sports and persists events into match_cache")
    void discoverEvents_success() {
        BcgameSportDto football = new BcgameSportDto("1", "Soccer");
        BcgameSportDto cs2 = new BcgameSportDto("esports-cs2", "CS2");

        when(apiClient.getSports()).thenReturn(List.of(football, cs2));

        BcgameEventDto match1 = new BcgameEventDto();
        match1.setId("ev-101");
        match1.setHomeTeam("Arsenal");
        match1.setAwayTeam("Chelsea");
        match1.setTournamentName("Premier League");
        match1.setStartTime(1700000000000L);
        match1.setIsLive(false);

        BcgameEventDto match2 = new BcgameEventDto();
        match2.setId("ev-102");
        match2.setName("NaVi vs FaZe");
        match2.setTournamentName("Major");
        match2.setStartTime(1700001000000L);
        match2.setStatus("live");

        when(apiClient.getEvents("1")).thenReturn(List.of(match1));
        when(apiClient.getEvents("esports-cs2")).thenReturn(List.of(match2));

        int saved = discoveryService.discoverEvents();

        assertThat(saved).isEqualTo(2);

        ArgumentCaptor<MatchCache> captor = ArgumentCaptor.forClass(MatchCache.class);
        verify(persistenceService, times(2)).saveOrUpdateMatchMetadata(captor.capture(), anyString());

        List<MatchCache> savedMatches = captor.getAllValues();
        assertThat(savedMatches.get(0).getExternalId()).isEqualTo("ev-101");
        assertThat(savedMatches.get(0).getBookmaker()).isEqualTo("bcgame");
        assertThat(savedMatches.get(0).getTeam1()).isEqualTo("Arsenal");
        assertThat(savedMatches.get(0).getTeam2()).isEqualTo("Chelsea");
        assertThat(savedMatches.get(0).getSportName()).isEqualTo("Soccer");
        assertThat(savedMatches.get(0).getLeagueName()).isEqualTo("Premier League");
        assertThat(savedMatches.get(0).getIsLive()).isFalse();
        assertThat(savedMatches.get(0).getEventUrl()).isEqualTo("https://bc.game/sports/event/ev-101");

        assertThat(savedMatches.get(1).getExternalId()).isEqualTo("ev-102");
        assertThat(savedMatches.get(1).getTeam1()).isEqualTo("NaVi");
        assertThat(savedMatches.get(1).getTeam2()).isEqualTo("FaZe");
        assertThat(savedMatches.get(1).getSportName()).isEqualTo("CS2");
        assertThat(savedMatches.get(1).getIsLive()).isTrue();
    }

    @Test
    @DisplayName("discoverEvents: returns 0 when sports catalog is null or empty")
    void discoverEvents_emptySports() {
        when(apiClient.getSports()).thenReturn(Collections.emptyList());
        int saved = discoveryService.discoverEvents();
        assertThat(saved).isEqualTo(0);
        verifyNoInteractions(persistenceService);

        when(apiClient.getSports()).thenReturn(null);
        int savedNull = discoveryService.discoverEvents();
        assertThat(savedNull).isEqualTo(0);
    }

    @Test
    @DisplayName("discoverEvents: continues processing when one sport throws exception")
    void discoverEvents_sportApiThrowsException() {
        BcgameSportDto badSport = new BcgameSportDto("bad-sport", "Bad Sport");
        BcgameSportDto goodSport = new BcgameSportDto("good-sport", "Good Sport");

        when(apiClient.getSports()).thenReturn(List.of(badSport, goodSport));
        when(apiClient.getEvents("bad-sport")).thenThrow(new RuntimeException("API connection timeout"));

        BcgameEventDto goodEvent = new BcgameEventDto();
        goodEvent.setId("good-ev-1");
        goodEvent.setHomeTeam("Lakers");
        goodEvent.setAwayTeam("Celtics");
        when(apiClient.getEvents("good-sport")).thenReturn(List.of(goodEvent));

        int saved = discoveryService.discoverEvents();
        assertThat(saved).isEqualTo(1);
        verify(persistenceService, times(1)).saveOrUpdateMatchMetadata(any(MatchCache.class), anyString());
    }

    @Test
    @DisplayName("processAndSaveEvent: parses event names with various delimiters (vs, -, v, @)")
    void processAndSaveEvent_parsesEventNames() {
        BcgameEventDto ev1 = new BcgameEventDto();
        ev1.setId("ev-vs");
        ev1.setName("Real Madrid vs Barcelona");
        assertThat(discoveryService.processAndSaveEvent(ev1)).isTrue();

        BcgameEventDto ev2 = new BcgameEventDto();
        ev2.setId("ev-dash");
        ev2.setName("Liverpool - Manchester City");
        assertThat(discoveryService.processAndSaveEvent(ev2)).isTrue();

        BcgameEventDto ev3 = new BcgameEventDto();
        ev3.setId("ev-v");
        ev3.setName("Djokovic v Alcaraz");
        assertThat(discoveryService.processAndSaveEvent(ev3)).isTrue();

        BcgameEventDto ev4 = new BcgameEventDto();
        ev4.setId("ev-at");
        ev4.setName("Golden State Warriors @ Boston Celtics");
        assertThat(discoveryService.processAndSaveEvent(ev4)).isTrue();

        ArgumentCaptor<MatchCache> captor = ArgumentCaptor.forClass(MatchCache.class);
        verify(persistenceService, times(4)).saveOrUpdateMatchMetadata(captor.capture(), anyString());

        List<MatchCache> matches = captor.getAllValues();
        assertThat(matches.get(0).getTeam1()).isEqualTo("Real Madrid");
        assertThat(matches.get(0).getTeam2()).isEqualTo("Barcelona");

        assertThat(matches.get(1).getTeam1()).isEqualTo("Liverpool");
        assertThat(matches.get(1).getTeam2()).isEqualTo("Manchester City");

        assertThat(matches.get(2).getTeam1()).isEqualTo("Djokovic");
        assertThat(matches.get(2).getTeam2()).isEqualTo("Alcaraz");

        assertThat(matches.get(3).getTeam1()).isEqualTo("Boston Celtics"); // Home
        assertThat(matches.get(3).getTeam2()).isEqualTo("Golden State Warriors"); // Away
    }

    @Test
    @DisplayName("processAndSaveEvent: skips events with missing teams or invalid IDs")
    void processAndSaveEvent_skipsInvalid() {
        assertThat(discoveryService.processAndSaveEvent(null)).isFalse();

        BcgameEventDto noId = new BcgameEventDto();
        noId.setHomeTeam("A");
        noId.setAwayTeam("B");
        assertThat(discoveryService.processAndSaveEvent(noId)).isFalse();

        BcgameEventDto emptyId = new BcgameEventDto();
        emptyId.setId("   ");
        assertThat(discoveryService.processAndSaveEvent(emptyId)).isFalse();

        BcgameEventDto noTeams = new BcgameEventDto();
        noTeams.setId("ev-no-teams");
        noTeams.setName("Single Team Or Unknown Tournament Banner");
        assertThat(discoveryService.processAndSaveEvent(noTeams)).isFalse();

        verifyNoInteractions(persistenceService);
    }

    @Test
    @DisplayName("processAndSaveEvent: throttles identical event footprint to avoid redundant DB writes")
    void processAndSaveEvent_throttling() {
        BcgameEventDto event = new BcgameEventDto();
        event.setId("ev-throttle");
        event.setHomeTeam("Team A");
        event.setAwayTeam("Team B");
        event.setSportName("Soccer");
        event.setTournamentName("League");
        event.setStartTime(1700000000000L);
        event.setIsLive(false);

        // First call -> saves to DB
        boolean first = discoveryService.processAndSaveEvent(event);
        assertThat(first).isTrue();
        verify(persistenceService, times(1)).saveOrUpdateMatchMetadata(any(MatchCache.class), anyString());

        // Second call with same payload within throttle duration -> throttled, returns true, no additional DB save
        boolean second = discoveryService.processAndSaveEvent(event);
        assertThat(second).isTrue();
        verify(persistenceService, times(1)).saveOrUpdateMatchMetadata(any(MatchCache.class), anyString());

        // Event changes to Live -> payload changes, saves to DB
        event.setIsLive(true);
        boolean third = discoveryService.processAndSaveEvent(event);
        assertThat(third).isTrue();
        verify(persistenceService, times(2)).saveOrUpdateMatchMetadata(any(MatchCache.class), anyString());
    }

    @Test
    @DisplayName("processAndSaveEvent: handles OptimisticLockingFailureException gracefully")
    void processAndSaveEvent_optimisticLock() {
        BcgameEventDto event = new BcgameEventDto();
        event.setId("ev-opt-lock");
        event.setHomeTeam("Team A");
        event.setAwayTeam("Team B");

        doThrow(new OptimisticLockingFailureException("Row updated by another transaction"))
                .when(persistenceService).saveOrUpdateMatchMetadata(any(), anyString());

        boolean result = discoveryService.processAndSaveEvent(event);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("processAndSaveEvent: returns false on unexpected persistence exception")
    void processAndSaveEvent_genericException() {
        BcgameEventDto event = new BcgameEventDto();
        event.setId("ev-err");
        event.setHomeTeam("Team A");
        event.setAwayTeam("Team B");

        doThrow(new RuntimeException("Database down"))
                .when(persistenceService).saveOrUpdateMatchMetadata(any(), anyString());

        boolean result = discoveryService.processAndSaveEvent(event);
        assertThat(result).isFalse();
    }
}
