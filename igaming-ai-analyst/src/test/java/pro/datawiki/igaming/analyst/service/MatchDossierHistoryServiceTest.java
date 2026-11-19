package pro.datawiki.igaming.analyst.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import pro.datawiki.igaming.analyst.ml.vector.ClickHouseDdsClient;
import pro.datawiki.igaming.dto.dossier.DossierRecentMatchDto;
import pro.datawiki.igaming.dto.sport.Sports;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchDossierHistoryServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ClickHouseDdsClient clickHouse;

    private MatchDossierHistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new MatchDossierHistoryService(jdbcTemplate, clickHouse);
    }

    @Test
    @DisplayName("fetchRecentMatches falls back to mdm_match when sofascore_raw_events has 0 matches")
    void testFallbackToMdmMatch() {
        // SofaScore returns empty list
        doReturn(Collections.emptyList()).when(jdbcTemplate).queryForList(contains("sofascore_raw_events"), any(Object.class), any(Object.class), any(Object.class));

        // mdm_match returns 2 finished matches
        Map<String, Object> m1 = Map.of(
                "id", 101L,
                "start_time", new Timestamp(System.currentTimeMillis() - 86400000L),
                "score1", "2",
                "score2", "1",
                "home_team_id", 555L,
                "away_team_id", 666L
        );
        Map<String, Object> m2 = Map.of(
                "id", 102L,
                "start_time", new Timestamp(System.currentTimeMillis() - 172800000L),
                "score1", "0",
                "score2", "2",
                "home_team_id", 777L,
                "away_team_id", 555L
        );
        doReturn(List.of(m1, m2)).when(jdbcTemplate).queryForList(contains("mdm_match"), any(Object.class), any(Object.class), any(Object.class));

        List<DossierRecentMatchDto> matches = historyService.fetchRecentMatches(Sports.SOCCER, 555L, "Test Team");

        assertThat(matches).hasSize(2);
        assertThat(matches.get(0).getResult()).isEqualTo("WIN");
        assertThat(matches.get(0).getTeamScore()).isEqualTo(2);
        assertThat(matches.get(0).getOpponentScore()).isEqualTo(1);
        assertThat(matches.get(1).getResult()).isEqualTo("WIN");
        assertThat(matches.get(1).getTeamScore()).isEqualTo(2);
        assertThat(matches.get(1).getOpponentScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("fetchRecentMatches falls back to mdm_team_translation and mdm_match when direct ID has no matches")
    void testFallbackToTranslationAndMdmMatch() {
        // Direct ID searches return empty
        doReturn(Collections.emptyList()).when(jdbcTemplate).queryForList(contains("sofascore_raw_events"), any(Object.class), any(Object.class), any(Object.class));
        doReturn(Collections.emptyList()).when(jdbcTemplate).queryForList(contains("mdm_match"), any(Object.class), eq(999L), eq(999L));

        // Translation resolves name to teamId 888L
        doReturn(List.of(888L)).when(jdbcTemplate).query(contains("mdm_team_translation"), any(org.springframework.jdbc.core.RowMapper.class), any(Object.class), any(Object.class));

        // mdm_match query for resolved teamId 888L returns matches
        Map<String, Object> m1 = Map.of(
                "score1", "3",
                "score2", "1",
                "start_time", 1793289600000L,
                "home_team_id", 888L,
                "away_team_id", 777L
        );
        doReturn(List.of(m1)).when(jdbcTemplate).queryForList(contains("mdm_match"), any(Object.class), eq(888L), eq(888L));

        List<DossierRecentMatchDto> matches = historyService.fetchRecentMatches(Sports.SOCCER, 999L, "Arsenal FC");

        assertThat(matches).isNotEmpty();
        assertThat(matches.get(0).getResult()).isEqualTo("WIN");
        assertThat(matches.get(0).getTeamScore()).isEqualTo(3);
        assertThat(matches.get(0).getOpponentScore()).isEqualTo(1);
    }
}
