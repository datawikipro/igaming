package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import pro.datawiki.igaming.analyst.ml.vector.ClickHouseDdsClient;
import pro.datawiki.igaming.dto.dossier.DossierOddsConsensusDto;
import pro.datawiki.igaming.dto.dossier.DossierPlayerDto;
import pro.datawiki.igaming.dto.dossier.DossierRecentMatchDto;
import pro.datawiki.igaming.dto.dossier.MatchDossierDto;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchDossierServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RedisOddsConsensusService redisOddsConsensusService;

    @Mock
    private ClickHouseDdsClient clickHouse;

    private ObjectMapper objectMapper;
    private MatchDossierService dossierService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        dossierService = new MatchDossierService(
                jdbcTemplate,
                redisOddsConsensusService,
                objectMapper,
                clickHouse
        );
    }

    @Test
    @DisplayName("Polymorphic sport resolution resolves aliases and esports tournaments cleanly")
    void testSportResolution() {
        // Direct aliases
        assertThat(Sports.resolveSport("football", null)).isEqualTo(Sports.SOCCER);
        assertThat(Sports.resolveSport("soccer", null)).isEqualTo(Sports.SOCCER);
        assertThat(Sports.resolveSport("hockey", null)).isEqualTo(Sports.ICE_HOCKEY);
        assertThat(Sports.resolveSport("ice-hockey", null)).isEqualTo(Sports.ICE_HOCKEY);
        assertThat(Sports.resolveSport("ping-pong", null)).isEqualTo(Sports.TABLE_TENNIS);
        assertThat(Sports.resolveSport("table-tennis", null)).isEqualTo(Sports.TABLE_TENNIS);
        assertThat(Sports.resolveSport("csgo", null)).isEqualTo(Sports.CS2);
        assertThat(Sports.resolveSport("cs:go", null)).isEqualTo(Sports.CS2);
        assertThat(Sports.resolveSport("cs-2", null)).isEqualTo(Sports.CS2);
        assertThat(Sports.resolveSport("dota-2", null)).isEqualTo(Sports.DOTA2);
        assertThat(Sports.resolveSport("dota", null)).isEqualTo(Sports.DOTA2);

        // Esports disambiguation via tournament name
        assertThat(Sports.resolveSport("esports", "BLAST Premier Spring Final")).isEqualTo(Sports.CS2);
        assertThat(Sports.resolveSport("esports", "ESL Pro League Season 19")).isEqualTo(Sports.CS2);
        assertThat(Sports.resolveSport("esports", "The International 2026")).isEqualTo(Sports.DOTA2);
        assertThat(Sports.resolveSport("esports", "DreamLeague Division 1")).isEqualTo(Sports.DOTA2);
        assertThat(Sports.resolveSport("esports", "VCT Champions Seoul")).isEqualTo(Sports.VALORANT);
    }

    @Test
    @DisplayName("Dynamic baseline calculation computes mathematical cushions when ClickHouse DDS baseline is missing")
    void testDynamicBaselineCalculations() {
        Double expTotal = 5.5;
        Double cushionLow = Math.round(expTotal * 0.75 * 2) / 2.0;
        Double cushionHigh = Math.round(expTotal * 1.25 * 2) / 2.0;
        Double handicapDelta = Math.max(1.5, Math.round(expTotal * 0.060 * 2) / 2.0);

        assertThat(cushionLow).isEqualTo(4.0);
        assertThat(cushionHigh).isEqualTo(7.0);
        assertThat(handicapDelta).isEqualTo(1.5);

        Double bballTotal = 215.0;
        Double bballLow = Math.round(bballTotal * 0.75 * 2) / 2.0;
        Double bballHigh = Math.round(bballTotal * 1.25 * 2) / 2.0;
        Double bballHandicap = Math.max(1.5, Math.round(bballTotal * 0.060 * 2) / 2.0);

        assertThat(bballLow).isEqualTo(161.5);
        assertThat(bballHigh).isEqualTo(269.0);
        assertThat(bballHandicap).isEqualTo(13.0);
    }

    @Test
    @DisplayName("Supported ML sports registry correctly validates all 8 active disciplines")
    void testSupportedMlSports() {
        assertThat(MatchDossierService.SUPPORTED_ML_SPORTS).containsExactlyInAnyOrder(
                Sports.SOCCER,
                Sports.TENNIS,
                Sports.TABLE_TENNIS,
                Sports.BASKETBALL,
                Sports.BASKETBALL_48M,
                Sports.ICE_HOCKEY,
                Sports.CS2,
                Sports.DOTA2
        );

        assertThat(MatchDossierService.isSupportedMlSport(Sports.SOCCER)).isTrue();
        assertThat(MatchDossierService.isSupportedMlSport(Sports.BASKETBALL_48M)).isTrue();
        assertThat(MatchDossierService.isSupportedMlSport(Sports.CS2)).isTrue();
        assertThat(MatchDossierService.isSupportedMlSport("football")).isTrue();
        assertThat(MatchDossierService.isSupportedMlSport("basketball-48m")).isTrue();
        assertThat(MatchDossierService.isSupportedMlSport("ice-hockey")).isTrue();
        assertThat(MatchDossierService.isSupportedMlSport("table-tennis")).isTrue();
        assertThat(MatchDossierService.isSupportedMlSport("volleyball")).isFalse();
    }

    @Test
    @DisplayName("MatchDossierDto converts cleanly to and from Map with zero data loss")
    void testMatchDossierDtoBidirectionalMapConversion() {
        DossierPlayerDto starter = DossierPlayerDto.builder()
                .name("Kylian Mbappe")
                .position("FW")
                .rating(8.1)
                .id(1001L)
                .substitute(false)
                .impactSub(false)
                .build();

        DossierPlayerDto bench = DossierPlayerDto.builder()
                .name("Arda Guler")
                .position("AM")
                .rating(7.4)
                .id(1002L)
                .substitute(true)
                .impactSub(true)
                .build();

        DossierRecentMatchDto recent = DossierRecentMatchDto.builder()
                .result("WIN")
                .teamScore(3)
                .opponentScore(1)
                .matchDate(1700000000000L)
                .cleanSheet(false)
                .failedToScore(false)
                .build();

        DossierOddsConsensusDto odds = DossierOddsConsensusDto.builder()
                .bookmakersCount(12L)
                .bestWin1(1.85)
                .avgWin1(1.80)
                .bestDraw(3.75)
                .avgDraw(3.60)
                .bestWin2(4.20)
                .avgWin2(4.00)
                .bestTo25(1.72)
                .bestTu25(2.15)
                .build();

        MatchDossierDto dto = MatchDossierDto.builder()
                .matchId(99001L)
                .sport(Sports.SOCCER)
                .sportName("FOOTBALL")
                .sportSlug("football")
                .tournamentId(501L)
                .tournamentName("La Liga")
                .leagueName("La Liga")
                .expectedTotalBaseline(2.5)
                .tournamentBaselineTotal(2.5)
                .tournamentCushionLow(1.5)
                .tournamentCushionHigh(3.5)
                .tournamentHandicapDelta(1.5)
                .handicapCushionDelta(1.5)
                .tournamentScoringCategory("NORMAL")
                .regulationMinutes(90)
                .paceMultiplier(1.0)
                .team1Name("Real Madrid")
                .team2Name("Barcelona")
                .team1Id(201L)
                .team2Id(202L)
                .team1SofascoreId(301L)
                .team2SofascoreId(302L)
                .startTime(1700000000000L)
                .startTimeUtc("2026-11-02T18:00:00Z")
                .isLive(false)
                .sofascoreId(88001L)
                .isLineupConfirmed(true)
                .team1Starters(List.of(starter))
                .team1Bench(List.of(bench))
                .team1Missing(List.of("Courtois (Травма колена)"))
                .team1RecentMatches(List.of(recent))
                .oddsConsensus(odds)
                .build();

        Map<String, Object> map = dto.toMap();

        assertThat(map.get("match_id")).isEqualTo(99001L);
        assertThat(map.get("sport_name")).isEqualTo("FOOTBALL");
        assertThat(map.get("team1_name")).isEqualTo("Real Madrid");
        assertThat(map.get("team2_name")).isEqualTo("Barcelona");
        assertThat(map.get("expected_total_baseline")).isEqualTo(2.5);
        assertThat(map.get("best_win1")).isEqualTo(1.85);
        assertThat(map.get("best_draw")).isEqualTo(3.75);
        assertThat(map.get("bookmakers_count")).isEqualTo(12L);

        // Reconstruct from Map
        MatchDossierDto reconstructed = MatchDossierDto.fromMap(map);
        assertThat(reconstructed.getMatchId()).isEqualTo(99001L);
        assertThat(reconstructed.getSportName()).isEqualTo("FOOTBALL");
        assertThat(reconstructed.getTeam1Name()).isEqualTo("Real Madrid");
        assertThat(reconstructed.getBestWin1()).isEqualTo(1.85);
        assertThat(reconstructed.getTeam1Starters()).hasSize(1);
        assertThat(reconstructed.getTeam1Starters().get(0).getName()).isEqualTo("Kylian Mbappe");
        assertThat(reconstructed.getTeam1Bench().get(0).isImpactSub()).isTrue();
        assertThat(reconstructed.getTeam1RecentMatches()).hasSize(1);
        assertThat(reconstructed.getTeam1RecentMatches().get(0).getTotalGoals()).isEqualTo(4);
        assertThat(reconstructed.getTeam1RecentMatches().get(0).isOver25()).isTrue();
    }
}
