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
    @Mock
    private pro.datawiki.igaming.mdm.repository.MdmTeamMergeMapRepository teamMergeMapRepository;

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
    private MdmTeam arsenalU19;
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
                "mdm.matches.upserted",
                null, null, null, null, null, null, null, null,
                teamMergeMapRepository
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

        arsenalU19 = MdmTeam.builder()
                .id(1004L)
                .sport(football)
                .category(england)
                .canonicalName("Arsenal U19")
                .gender("MALE")
                .ageCategory("U19")
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
        verify(anomalyReportingService).reportMutantMatch(
                eq("winline"), eq(premierLeague), eq(arsenalWomen), eq(chelsea), eq("GENDER_MISMATCH"), anyString());
    }

    @Test
    @DisplayName("Should reject and report mutant match when teams have different age categories")
    void testMutantMatchAgeMismatchRejection() {
        when(matchRepository.findCandidateMatches(eq(1), eq(1004L), eq(1002L), any(), any()))
                .thenReturn(Collections.emptyList());

        when(sportRepository.findById(1)).thenReturn(Optional.of(football));
        when(tournamentRepository.findById(100L)).thenReturn(Optional.of(premierLeague));
        when(teamRepository.findById(1004L)).thenReturn(Optional.of(arsenalU19));
        when(teamRepository.findById(1002L)).thenReturn(Optional.of(chelsea));

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentId(100L)
                .homeTeamId(1004L)
                .awayTeamId(1002L)
                .startTimeEpochSec(1761400000L)
                .createdBySource("pinnacle")
                .build();

        assertThatThrownBy(() -> service.findOrCreateMatch(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age category mismatch")
                .hasMessageContaining("Arsenal U19");

        verify(anomalyReportingService).reportMutantMatch(
                eq("pinnacle"), eq(premierLeague), eq(arsenalU19), eq(chelsea), eq("AGE_CATEGORY_MISMATCH"), anyString());
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

    @Test
    @DisplayName("Resolve bookmaker line with Cyrillic parenthesized age and phonetic city from active tournament roster")
    void testBookmakerResolutionWithRosterAndPhonetics() {
        MdmTournament u23League = MdmTournament.builder()
                .id(7605L)
                .sport(football)
                .canonicalName("U23 Liga Next Gen, 1st Phase")
                .slug("u23-liga-next-gen-1st-phase")
                .gender("MALE")
                .ageCategory("U23")
                .build();

        MdmTeam farenseU23 = MdmTeam.builder()
                .id(129864L)
                .sport(football)
                .canonicalName("SC Farense U23")
                .slug("sc-farense-u23")
                .gender("MALE")
                .ageCategory("U23")
                .build();

        MdmTeam viseuU23 = MdmTeam.builder()
                .id(131851L)
                .sport(football)
                .canonicalName("Académico Viseu U23")
                .slug("academico-viseu-u23")
                .gender("MALE")
                .ageCategory("U23")
                .build();

        when(tournamentRepository.findFirstBySportIdAndCanonicalNameIgnoreCase(1, "U23 Liga Next Gen, 1st Phase"))
                .thenReturn(Optional.of(u23League));
        when(tournamentRepository.findById(7605L))
                .thenReturn(Optional.of(u23League));
        when(tournamentTeamRepository.findActiveTeamsByTournamentId(7605L))
                .thenReturn(List.of(farenseU23, viseuU23));

        OffsetDateTime matchTime = OffsetDateTime.of(2026, 10, 27, 11, 0, 0, 0, ZoneOffset.UTC);
        MdmMatch canonicalMatch = MdmMatch.builder()
                .id(1064764L)
                .sport(football)
                .tournament(u23League)
                .homeTeam(farenseU23)
                .awayTeam(viseuU23)
                .startTime(matchTime)
                .sourceOrigin("SOFASCORE")
                .sofascoreId(13955688L)
                .status("SCHEDULED")
                .build();

        when(matchRepository.findCandidateMatches(eq(1), eq(129864L), eq(131851L), any(), any()))
                .thenReturn(List.of(canonicalMatch));
        when(matchRepository.save(any(MdmMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentName("U23 Liga Next Gen, 1st Phase")
                .homeTeamName("Фаренсе(23)")
                .awayTeamName("Визеу(23)")
                .startTimeEpochSec(matchTime.toEpochSecond() + 7200) // 2 hours difference
                .createdBySource("melbet")
                .build();

        CanonicalMatchHeaderDto result = service.findOrCreateMatch(request);

        assertThat(result).isNotNull();
        assertThat(result.getMatchId()).isEqualTo(1064764L);
        assertThat(result.getTournamentId()).isEqualTo(7605L);
        assertThat(result.getTournamentName()).isEqualTo("U23 Liga Next Gen, 1st Phase");
        assertThat(result.getHomeTeamId()).isEqualTo(129864L);
        assertThat(result.getHomeTeamName()).isEqualTo("SC Farense U23");
        assertThat(result.getAwayTeamId()).isEqualTo(131851L);
        assertThat(result.getAwayTeamName()).isEqualTo("Académico Viseu U23");
    }

    @Test
    @DisplayName("Dedicated findOrCreateTournament resolves existing league by country and category")
    void testDedicatedFindOrCreateTournamentWithCountry() {
        when(tournamentRepository.findFirstBySportIdAndNameAndCountry(1, "Premier League", "England"))
                .thenReturn(Optional.of(premierLeague));

        pro.datawiki.igaming.dto.TournamentResolutionRequest req = pro.datawiki.igaming.dto.TournamentResolutionRequest.builder()
                .sportId(1)
                .tournamentName("Premier League")
                .countryName("England")
                .build();

        pro.datawiki.igaming.dto.CanonicalTournamentDto result = service.findOrCreateTournament(req);

        assertThat(result).isNotNull();
        assertThat(result.getTournamentId()).isEqualTo(100L);
        assertThat(result.getCanonicalName()).isEqualTo("Premier League");
    }

    @Test
    @DisplayName("Dedicated findOrCreateTeam resolves team via tournament roster context and country")
    void testDedicatedFindOrCreateTeamWithRosterAndCountry() {
        MdmTournament u23League = MdmTournament.builder()
                .id(7605L)
                .sport(football)
                .canonicalName("U23 Liga Next Gen, 1st Phase")
                .gender("MALE")
                .ageCategory("U23")
                .countryName("Portugal")
                .build();

        MdmTeam farenseU23 = MdmTeam.builder()
                .id(129864L)
                .sport(football)
                .canonicalName("SC Farense U23")
                .slug("sc-farense-u23")
                .gender("MALE")
                .ageCategory("U23")
                .countryName("Portugal")
                .build();

        when(tournamentRepository.findById(7605L)).thenReturn(Optional.of(u23League));
        when(tournamentTeamRepository.findActiveTeamsByTournamentId(7605L)).thenReturn(List.of(farenseU23));

        pro.datawiki.igaming.dto.TeamResolutionRequest req = pro.datawiki.igaming.dto.TeamResolutionRequest.builder()
                .sportId(1)
                .tournamentId(7605L)
                .teamName("Фаренсе(23)")
                .countryName("Portugal")
                .build();

        pro.datawiki.igaming.dto.CanonicalTeamDto result = service.findOrCreateTeam(req);

        assertThat(result).isNotNull();
        assertThat(result.getTeamId()).isEqualTo(129864L);
        assertThat(result.getCanonicalName()).isEqualTo("SC Farense U23");
        assertThat(result.getAgeCategory()).isEqualTo("U23");
    }

    @Test
    @DisplayName("findOrCreateMatch with pre-resolved IDs bypasses entity resolution and succeeds")
    void testFindOrCreateMatchWithPreResolvedIds() {
        OffsetDateTime matchTime = OffsetDateTime.of(2026, 10, 27, 18, 0, 0, 0, ZoneOffset.UTC);
        MdmMatch existingMatch = MdmMatch.builder()
                .id(888888L)
                .sport(football)
                .tournament(premierLeague)
                .homeTeam(arsenal)
                .awayTeam(chelsea)
                .startTime(matchTime)
                .sourceOrigin("BOOKMAKER")
                .status("SCHEDULED")
                .build();

        when(matchRepository.findCandidateMatches(eq(1), eq(1001L), eq(1002L), any(), any()))
                .thenReturn(List.of(existingMatch));

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentId(100L)
                .homeTeamId(1001L)
                .awayTeamId(1002L)
                .startTimeEpochSec(matchTime.toEpochSecond())
                .createdBySource("parimatch")
                .build();

        CanonicalMatchHeaderDto result = service.findOrCreateMatch(request);

        assertThat(result).isNotNull();
        assertThat(result.getMatchId()).isEqualTo(888888L);
        assertThat(result.getTournamentId()).isEqualTo(100L);
        assertThat(result.getHomeTeamId()).isEqualTo(1001L);
        assertThat(result.getAwayTeamId()).isEqualTo(1002L);
        // Verify that entity name lookups were NEVER invoked because IDs were pre-resolved
        verify(tournamentRepository, never()).findFirstBySportIdAndCanonicalNameIgnoreCase(any(), any());
        verify(teamRepository, never()).findBySportIdAndSlug(any(), any());
    }

    @Test
    @DisplayName("Should enrich existing canonical team with SofaScore ID when sofaId is provided")
    void testFindOrCreateTeamEnrichesSofascoreId() {
        MdmTeam existingTeam = MdmTeam.builder()
                .id(777L)
                .sport(football)
                .canonicalName("FC Porto")
                .slug("fc-porto")
                .sofascoreId(null)
                .sourceOrigin("BOOKMAKER")
                .build();

        when(teamRepository.findBySofascoreId(3002L)).thenReturn(Optional.empty());
        when(teamRepository.findBySportIdAndSlug(1, "fc-porto")).thenReturn(Optional.of(existingTeam));
        when(teamRepository.save(any(MdmTeam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pro.datawiki.igaming.dto.TeamResolutionRequest request = pro.datawiki.igaming.dto.TeamResolutionRequest.builder()
                .sportId(1)
                .teamName("FC Porto")
                .sofascoreTeamId(3002L)
                .createdBySource("sofascore")
                .build();

        pro.datawiki.igaming.dto.CanonicalTeamDto result = service.findOrCreateTeam(request);

        assertThat(result).isNotNull();
        assertThat(result.getTeamId()).isEqualTo(777L);
        assertThat(existingTeam.getSofascoreId()).isEqualTo(3002L);
        verify(teamRepository).save(existingTeam);
    }

    @Test
    @DisplayName("Should remap local duplicate team ID via mdm_team_merge_map to canonical ID")
    void testFindOrCreateMatchRemapsLocalTeamsViaMergeMap() {
        OffsetDateTime matchTime = OffsetDateTime.of(2025, 10, 25, 15, 0, 0, 0, ZoneOffset.UTC);
        MdmMatch existingCanonicalMatch = MdmMatch.builder()
                .id(55555L)
                .sport(football)
                .tournament(premierLeague)
                .homeTeam(arsenal) // ID 1001
                .awayTeam(chelsea) // ID 1002
                .startTime(matchTime)
                .status("SCHEDULED")
                .build();

        // 9999L is a local duplicate of 1001L (Arsenal)
        pro.datawiki.igaming.mdm.domain.MdmTeamMergeMap mergeEntry = pro.datawiki.igaming.mdm.domain.MdmTeamMergeMap.builder()
                .localId(9999L)
                .sofaId(1001L)
                .localName("Local Arsenal")
                .sofaName("Arsenal")
                .sportId(1)
                .build();

        when(teamMergeMapRepository.findByLocalId(9999L)).thenReturn(Optional.of(mergeEntry));
        when(matchRepository.findCandidateMatches(eq(1), eq(1001L), eq(1002L), any(), any()))
                .thenReturn(List.of(existingCanonicalMatch));

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(1)
                .tournamentId(100L)
                .homeTeamId(9999L) // duplicate ID!
                .awayTeamId(1002L)
                .startTimeEpochSec(matchTime.toEpochSecond())
                .createdBySource("marathonbet")
                .build();

        CanonicalMatchHeaderDto result = service.findOrCreateMatch(request);

        assertThat(result).isNotNull();
        assertThat(result.getMatchId()).isEqualTo(55555L);
        assertThat(result.getHomeTeamId()).isEqualTo(1001L); // Remapped!
        assertThat(result.getAwayTeamId()).isEqualTo(1002L);
    }

    @Test
    @DisplayName("Should resolve match by temporal anchor cross-language (Cyrillic to English canonical)")
    void testTemporalMatchAnchorCrossLanguageMatching() {
        OffsetDateTime matchTime = OffsetDateTime.of(2025, 10, 25, 18, 0, 0, 0, ZoneOffset.UTC);
        MdmSport basketball = MdmSport.builder().id(3).slug("basketball").build();
        MdmTournament euroleague = MdmTournament.builder()
                .id(200L)
                .sport(basketball)
                .canonicalName("Euroleague")
                .build();
        MdmTeam realMadrid = MdmTeam.builder()
                .id(3001L)
                .sport(basketball)
                .canonicalName("Real Madrid")
                .build();
        MdmTeam fenerbahce = MdmTeam.builder()
                .id(3002L)
                .sport(basketball)
                .canonicalName("Fenerbahce")
                .build();

        MdmMatch existingMatch = MdmMatch.builder()
                .id(77777L)
                .sport(basketball)
                .tournament(euroleague)
                .homeTeam(realMadrid)
                .awayTeam(fenerbahce)
                .startTime(matchTime)
                .status("SCHEDULED")
                .build();

        when(matchRepository.findMatchesInWindow(eq(3), any(), any()))
                .thenReturn(List.of(existingMatch));

        MatchResolutionRequest request = MatchResolutionRequest.builder()
                .sportId(3)
                .tournamentName("Евролига")
                .homeTeamName("Реал Мадрид")
                .awayTeamName("Фенербахче")
                .startTimeEpochSec(matchTime.toEpochSecond())
                .createdBySource("fon-bet-ru")
                .build();

        CanonicalMatchHeaderDto result = service.findOrCreateMatch(request);

        assertThat(result).isNotNull();
        assertThat(result.getMatchId()).isEqualTo(77777L);
        assertThat(result.getHomeTeamId()).isEqualTo(3001L);
        assertThat(result.getAwayTeamId()).isEqualTo(3002L);
    }
}
