package pro.datawiki.igaming.analyst.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import pro.datawiki.igaming.analyst.dto.AnalystStatsDto;
import pro.datawiki.igaming.analyst.dto.ModelVersionDto;
import pro.datawiki.igaming.analyst.entity.AiModelVersion;
import pro.datawiki.igaming.analyst.repository.AiModelVersionRepository;
import pro.datawiki.igaming.analyst.repository.MatchAiRecommendationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalystStatisticsServiceTest {

    @Mock
    private MatchAiRecommendationRepository recommendationRepository;

    @Mock
    private AiModelVersionRepository aiModelVersionRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private AnalystStatisticsService statisticsService;

    @Test
    void testGetPerformanceStatsEmpty() {
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(Collections.emptyList());

        AnalystStatsDto stats = statisticsService.getPerformanceStats();
        assertNotNull(stats);
        assertEquals(0L, stats.getTotalEvaluations());
        assertEquals(0.0, stats.getWinratePct());
        assertEquals(BigDecimal.ZERO, stats.getTotalPnl());
    }

    @Test
    void testGetModelVersions() {
        AiModelVersion v = new AiModelVersion();
        v.setVersionCode("v0.3.5.1.1");
        v.setDisplayName("MultiTask RTX 4090");
        v.setReleaseDate(LocalDateTime.now());
        v.setActive(true);

        when(aiModelVersionRepository.findAllByOrderByReleaseDateDesc())
                .thenReturn(List.of(v));
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(Collections.emptyList());

        List<ModelVersionDto> versions = statisticsService.getModelVersions();
        assertNotNull(versions);
        assertEquals(1, versions.size());
        assertEquals("v0.3.5.1.1", versions.get(0).getVersionCode());
        assertTrue(versions.get(0).isActive());
    }

    @Test
    void testGetAvailableBookmakersStats() {
        when(recommendationRepository.findByEvaluationStageNotIn(any()))
                .thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = statisticsService.getAvailableBookmakersStats();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // All known bookmakers from meta map should be present with activeCount = 0
        assertTrue(result.stream().anyMatch(m -> "winline".equals(m.get("id"))));
    }
}
