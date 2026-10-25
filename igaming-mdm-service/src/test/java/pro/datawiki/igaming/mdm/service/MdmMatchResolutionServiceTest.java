package pro.datawiki.igaming.mdm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.MatchResolutionRequest;
import pro.datawiki.igaming.mdm.domain.MdmCategory;
import pro.datawiki.igaming.mdm.domain.MdmMatch;
import pro.datawiki.igaming.mdm.domain.MdmSport;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTournament;
import pro.datawiki.igaming.mdm.repository.MdmMatchRepository;
import pro.datawiki.igaming.mdm.repository.MdmSportRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentTeamRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MdmMatchResolutionServiceTest {

    @Mock
    private MdmMatchRepository matchRepository;
    @Mock
    private MdmSportRepository sportRepository;
    @Mock
    private MdmTournamentRepository tournamentRepository;
    @Mock
    private MdmTeamRepository teamRepository;
    @Mock
    private MdmTournamentTeamRepository tournamentTeamRepository;
    @Mock
    private IngestionBlueprintService blueprintService;
    @Mock
    private AnomalyReportingService anomalyReportingService;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private MdmMatchResolutionService service;

    private MdmSport football;
    private MdmSport hockey;
    private MdmCategory england;
    private MdmCategory uruguay;
    private MdmTournament premierLeague;
    private MdmTeam arsenal;
    private MdmTeam chelsea;
    private MdmTeam liverpoolUruguay;
    private MdmTeam arsenalWomen;
    private MdmTeam nhlBruins;

    @BeforeEach
    void setUp() {
        service = new MdmMatchResolutionService(
                matchRepository,
                sportRepository,
                tournamentRepository,
                teamRepository,
                tournamentTeamRepository,
                blueprintService,
                anomalyReportingService,
                kafkaTemplate,
                "mdm.matches.upserted"
        );

        football = MdmSport.builder().id(1).slug("football").build();
        hockey = MdmSport.builder().id(2).slug("ice-hockey").build();

        england = MdmCategory.builder().id(10).slug("england").build();
        uruguay = MdmCategory.builder().id(20).slug("uruguay").build();

        premierLeague = MdmTournament.builder()
                .id(100L)
                .sofascoreId(17L)
                .sport(football)
                .category(england)
                .canonicalName("Premier League")
                .gender("MALE")
                .ageCategory("SENIOR")
                .isCup(false)
                .build();

        arsenal = MdmTeam.builder()
                .id(1001L)
                .sport(football)
                .category(england)
                .canonicalName("Arsenal")
                .gender("MALE")
                .ageCategory("SENIOR")
                .build();

        chelsea = MdmTeam.builder()
                .id(1002L)
                .sport(football)
                .category(england)
                .canonicalName("Chelsea")
                .gender("MALE")
                .ageCategory("SENIOR")
                .build();

        liverpoolUruguay = MdmTeam.builder()
                .id(3001L)
                .sport(football)
                .category(uruguay) // Uruguay club with English name!
                .canonicalName("Liverpool")
                .gender("MALE")
                .ageCategory("SENIOR")
                .build();

        arsenalWomen = MdmTeam.builder()
                .id(1003L)
                .sport(football)
                .category(england)
                .canonicalName("Arsenal (W)")
                .gender("FEMALE")
                .ageCategory("SENIOR")
                .build();

        nhlBruins = MdmTeam.builder().id(2001L).sport(hockey).canonicalName("Boston Bruins").build();
    }

    @Test
    @DisplayName("Should throw exception when home team is identical to away team")
    void testIdenticalTeamsViolation() {
        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentId(100L)
                .homeTeamId(1001L)
                .awayTeamId(1001L)
                .startTimeEpochSec(1761400000L)
                .build();

        assertThatThrownBy(() -> service.findOrCreateMatch(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be identical");
    }

    @Test
    @DisplayName("Should throw integrity violation and report to Kafka when home team belongs to a different sport")
    void testCrossSportIntegrityViolation() {
        when(matchRepository.findCandidateMatches(eq(1), eq(2001L), eq(1002L), any(), any()))
                .thenReturn(Collections.emptyList());

        when(sportRepository.findById(1)).thenReturn(Optional.of(football));
        when(tournamentRepository.findById(100L)).thenReturn(Optional.of(premierLeague));
        when(teamRepository.findById(2001L)).thenReturn(Optional.of(nhlBruins)); // Hockey team in football match!
        when(teamRepository.findById(1002L)).thenReturn(Optional.of(chelsea));

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentId(100L)
                .homeTeamId(2001L)
                .awayTeamId(1002L)
                .startTimeEpochSec(1761400000L)
                .createdBySource("fon-bet-ru")
                .build();

        assertThatThrownBy(() -> service.findOrCreateMatch(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Integrity violation")
                .hasMessageContaining("Boston Bruins");

        verify(anomalyReportingService).reportLeagueMismatch(
                eq("fon-bet-ru"), eq("SPORT_MISMATCH"), eq(100L), eq("Premier League"), eq(2001L), eq("Boston Bruins"), anyString());
    }

    @Test
    @DisplayName("Should reject and report to Kafka when club from another country attempts to play in domestic league")
    void testCountryMismatchRejection() {
        when(matchRepository.findCandidateMatches(eq(1), eq(1001L), eq(3001L), any(), any()))
                .thenReturn(Collections.emptyList());

        when(sportRepository.findById(1)).thenReturn(Optional.of(football));
        when(tournamentRepository.findById(100L)).thenReturn(Optional.of(premierLeague));
        when(teamRepository.findById(1001L)).thenReturn(Optional.of(arsenal));
        when(teamRepository.findById(3001L)).thenReturn(Optional.of(liverpoolUruguay)); // Uruguay club in English Premier League!

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentId(100L)
                .homeTeamId(1001L)
                .awayTeamId(3001L)
                .startTimeEpochSec(1761400000L)
                .createdBySource("betcity")
                .build();

        assertThatThrownBy(() -> service.findOrCreateMatch(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Country/Category mismatch")
                .hasMessageContaining("Liverpool");

        verify(anomalyReportingService).reportLeagueMismatch(
                eq("betcity"), eq("COUNTRY_MISMATCH"), eq(100L), eq("Premier League"), eq(3001L), eq("Liverpool"), anyString());
    }

    @Test
    @DisplayName("Should reject and report to Kafka when female team is placed into male tournament")
    void testGenderMismatchRejection() {
        when(matchRepository.findCandidateMatches(eq(1), eq(1003L), eq(1002L), any(), any()))
                .thenReturn(Collections.emptyList());

        when(sportRepository.findById(1)).thenReturn(Optional.of(football));
        when(tournamentRepository.findById(100L)).thenReturn(Optional.of(premierLeague));
        when(teamRepository.findById(1003L)).thenReturn(Optional.of(arsenalWomen));
        when(teamRepository.findById(1002L)).thenReturn(Optional.of(chelsea));

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentId(100L)
                .homeTeamId(1003L)
                .awayTeamId(1002L)
                .startTimeEpochSec(1761400000L)
                .createdBySource("winline")
                .build();

        assertThatThrownBy(() -> service.findOrCreateMatch(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Gender mismatch")
                .hasMessageContaining("Arsenal (W)");

        verify(anomalyReportingService).reportLeagueMismatch(
                eq("winline"), eq("GENDER_MISMATCH"), eq(100L), eq("Premier League"), eq(1003L), eq("Arsenal (W)"), anyString());
    }

    @Test
    @DisplayName("Should reject bookmaker match when team is not in established league roster")
    void testLeagueRosterViolation() {
        when(matchRepository.findCandidateMatches(eq(1), eq(1001L), eq(1002L), any(), any()))
                .thenReturn(Collections.emptyList());

        when(sportRepository.findById(1)).thenReturn(Optional.of(football));
        when(tournamentRepository.findById(100L)).thenReturn(Optional.of(premierLeague));
        when(teamRepository.findById(1001L)).thenReturn(Optional.of(arsenal));
        when(teamRepository.findById(1002L)).thenReturn(Optional.of(chelsea));

        // Established league roster exists (20 teams)
        when(tournamentTeamRepository.countByTournamentIdAndIsActiveTrue(100L)).thenReturn(20L);
        when(tournamentTeamRepository.existsByTournamentIdAndTeamId(100L, 1001L)).thenReturn(true);
        when(tournamentTeamRepository.existsByTournamentIdAndTeamId(100L, 1002L)).thenReturn(false); // Chelsea not in roster!

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentId(100L)
                .homeTeamId(1001L)
                .awayTeamId(1002L)
                .startTimeEpochSec(1761400000L)
                .sourceOrigin("BOOKMAKER")
                .createdBySource("fon-bet-ru")
                .build();

        assertThatThrownBy(() -> service.findOrCreateMatch(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("League roster violation")
                .hasMessageContaining("Chelsea");

        verify(anomalyReportingService).reportLeagueMismatch(
                eq("fon-bet-ru"), eq("ROSTER_VIOLATION"), eq(100L), eq("Premier League"), eq(1002L), eq("Chelsea"), anyString());
    }

    @Test
    @DisplayName("Should create a new bookmaker match and register teams in tournament roster")
    void testCreateNewBookmakerMatch() {
        when(matchRepository.findCandidateMatches(eq(1), eq(1001L), eq(1002L), any(), any()))
                .thenReturn(Collections.emptyList());

        when(sportRepository.findById(1)).thenReturn(Optional.of(football));
        when(tournamentRepository.findById(100L)).thenReturn(Optional.of(premierLeague));
        when(teamRepository.findById(1001L)).thenReturn(Optional.of(arsenal));
        when(teamRepository.findById(1002L)).thenReturn(Optional.of(chelsea));

        when(tournamentTeamRepository.countByTournamentIdAndIsActiveTrue(100L)).thenReturn(0L); // Initial roster empty

        when(matchRepository.save(any(MdmMatch.class))).thenAnswer(invocation -> {
            MdmMatch m = invocation.getArgument(0);
            m.setId(99001L);
            return m;
        });

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentId(100L)
                .homeTeamId(1001L)
                .awayTeamId(1002L)
                .startTimeEpochSec(1761400000L)
                .sourceOrigin("BOOKMAKER")
                .createdBySource("fon-bet-ru")
                .build();

        CanonicalMatchHeaderDto result = service.findOrCreateMatch(request);

        assertThat(result).isNotNull();
        assertThat(result.getMatchId()).isEqualTo(99001L);
        assertThat(result.getSourceOrigin()).isEqualTo("BOOKMAKER");
        assertThat(result.getSportId()).isEqualTo(1);
        assertThat(result.getHomeTeamId()).isEqualTo(1001L);
        assertThat(result.getAwayTeamId()).isEqualTo(1002L);

        verify(matchRepository).save(any(MdmMatch.class));
        verify(blueprintService).invalidateAll();
        verify(tournamentTeamRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Should enrich and promote existing bookmaker match when SofaScore event arrives")
    void testEnrichWithSofaScore() {
        OffsetDateTime startTime = OffsetDateTime.of(2026, 10, 25, 18, 0, 0, 0, ZoneOffset.UTC);
        MdmMatch existing = MdmMatch.builder()
                .id(99001L)
                .sport(football)
                .tournament(premierLeague)
                .homeTeam(arsenal)
                .awayTeam(chelsea)
                .startTime(startTime)
                .sourceOrigin("BOOKMAKER")
                .createdBySource("fon-bet-ru")
                .status("SCHEDULED")
                .build();

        when(matchRepository.findBySofascoreId(555777L)).thenReturn(Optional.empty());
        when(matchRepository.findCandidateMatches(eq(1), eq(1001L), eq(1002L), any(), any()))
                .thenReturn(List.of(existing));
        when(matchRepository.save(any(MdmMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentId(100L)
                .homeTeamId(1001L)
                .awayTeamId(1002L)
                .startTimeEpochSec(startTime.toEpochSecond())
                .sofascoreId(555777L)
                .sourceOrigin("SOFASCORE")
                .createdBySource("sofascore")
                .build();

        CanonicalMatchHeaderDto result = service.findOrCreateMatch(request);

        assertThat(result.getMatchId()).isEqualTo(99001L);
        assertThat(result.getSofascoreId()).isEqualTo(555777L);
        assertThat(result.getSourceOrigin()).isEqualTo("SOFASCORE");

        verify(matchRepository).save(existing);
        assertThat(existing.getSourceOrigin()).isEqualTo("SOFASCORE");
        assertThat(existing.getSofascoreId()).isEqualTo(555777L);
    }

    @Test
    @DisplayName("Should auto-create teams, tournament, and canonical match from raw SofaScore event")
    void testSofaScoreAutoCreateMatchAndEntities() {
        when(matchRepository.findBySofascoreId(888999L)).thenReturn(Optional.empty());
        when(sportRepository.findBySlug("football")).thenReturn(Optional.of(football));
        when(teamRepository.findBySofascoreId(12345L)).thenReturn(Optional.empty());
        when(teamRepository.findBySofascoreId(67890L)).thenReturn(Optional.empty());
        when(tournamentRepository.findBySofascoreId(999L)).thenReturn(Optional.empty());

        when(sportRepository.findById(1)).thenReturn(Optional.of(football));
        when(tournamentRepository.findById(anyLong())).thenAnswer(inv -> {
            MdmTournament t = MdmTournament.builder().id(inv.getArgument(0)).sport(football).canonicalName("La Liga").build();
            return Optional.of(t);
        });
        when(teamRepository.findById(anyLong())).thenAnswer(inv -> {
            long id = inv.getArgument(0);
            MdmTeam tm = MdmTeam.builder().id(id).sport(football).canonicalName(id == 10L ? "Real Madrid" : "Barcelona").build();
            return Optional.of(tm);
        });

        when(teamRepository.save(any(MdmTeam.class))).thenAnswer(inv -> {
            MdmTeam t = inv.getArgument(0);
            if (t.getId() == null) t.setId(t.getCanonicalName().contains("Real") ? 10L : 20L);
            return t;
        });

        when(tournamentRepository.save(any(MdmTournament.class))).thenAnswer(inv -> {
            MdmTournament tr = inv.getArgument(0);
            if (tr.getId() == null) tr.setId(500L);
            return tr;
        });

        when(matchRepository.save(any(MdmMatch.class))).thenAnswer(inv -> {
            MdmMatch m = inv.getArgument(0);
            m.setId(1000005L);
            return m;
        });

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportSlug("football")
                .sofascoreId(888999L)
                .sofascoreTournamentId(999L)
                .tournamentName("La Liga")
                .sofascoreHomeTeamId(12345L)
                .homeTeamName("Real Madrid")
                .sofascoreAwayTeamId(67890L)
                .awayTeamName("Barcelona")
                .startTimeEpochSec(1761400000L)
                .sourceOrigin("SOFASCORE")
                .createdBySource("sofascore")
                .build();

        CanonicalMatchHeaderDto result = service.findOrCreateMatch(request);

        assertThat(result).isNotNull();
        assertThat(result.getMatchId()).isEqualTo(1000005L);
        assertThat(result.getSofascoreId()).isEqualTo(888999L);
        assertThat(result.getSourceOrigin()).isEqualTo("SOFASCORE");
        assertThat(result.getTournamentName()).isEqualTo("La Liga");
        assertThat(result.getHomeTeamName()).isEqualTo("Real Madrid");
        assertThat(result.getAwayTeamName()).isEqualTo("Barcelona");

        verify(teamRepository, atLeast(2)).save(any(MdmTeam.class));
        verify(tournamentRepository, atLeastOnce()).save(any(MdmTournament.class));
        verify(matchRepository).save(any(MdmMatch.class));
    }
}
