package pro.datawiki.igaming.analytics.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pro.datawiki.igaming.analytics.dto.crawler.*;
import pro.datawiki.igaming.analytics.service.CrawlerOpsService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CrawlerOpsControllerTest {

    private MockMvc mockMvc;
    private CrawlerOpsService crawlerOpsService;

    @BeforeEach
    void setUp() {
        crawlerOpsService = mock(CrawlerOpsService.class);
        CrawlerOpsController controller = new CrawlerOpsController(crawlerOpsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/v1/crawler-ops/pipelines returns list of pipelines")
    void testGetPipelines() throws Exception {
        CrawlerPipelineDto dto = CrawlerPipelineDto.builder()
                .bookmakerCode("winline")
                .bookmakerName("Winline (RU)")
                .region("RU_CUPIS")
                .activeMatches(600)
                .minThreshold(500)
                .thresholdStatus("PASS")
                .status("RUNNING")
                .build();

        when(crawlerOpsService.getAllPipelines()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/crawler-ops/pipelines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookmakerCode").value("winline"))
                .andExpect(jsonPath("$[0].thresholdStatus").value("PASS"));
    }

    @Test
    @DisplayName("GET /api/v1/crawler-ops/pipelines/{bookmaker} returns pipeline or 404")
    void testGetPipelineByCode() throws Exception {
        CrawlerPipelineDto dto = CrawlerPipelineDto.builder()
                .bookmakerCode("pinnacle")
                .bookmakerName("Pinnacle")
                .build();

        when(crawlerOpsService.getPipeline("pinnacle")).thenReturn(dto);
        when(crawlerOpsService.getPipeline("unknown")).thenReturn(null);

        mockMvc.perform(get("/api/v1/crawler-ops/pipelines/pinnacle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmakerCode").value("pinnacle"));

        mockMvc.perform(get("/api/v1/crawler-ops/pipelines/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/crawler-ops/pipelines/stats returns aggregated stats")
    void testGetPipelineStats() throws Exception {
        PipelineStatsDto stats = PipelineStatsDto.builder()
                .totalPipelines(52)
                .activePipelines(52)
                .thresholdMetCount(52)
                .rule8CompliancePercent(100.0)
                .build();

        when(crawlerOpsService.getPipelineStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/crawler-ops/pipelines/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPipelines").value(52))
                .andExpect(jsonPath("$.rule8CompliancePercent").value(100.0));
    }

    @Test
    @DisplayName("POST /api/v1/crawler-ops/pipelines/{bookmaker}/sync triggers sync")
    void testTriggerSync() throws Exception {
        TriggerSyncResponseDto res = TriggerSyncResponseDto.builder()
                .bookmakerCode("winline")
                .triggered(true)
                .message("Sync triggered")
                .build();

        when(crawlerOpsService.triggerSync("winline")).thenReturn(res);

        mockMvc.perform(post("/api/v1/crawler-ops/pipelines/winline/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.triggered").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/crawler-ops/thresholds updates threshold")
    void testUpdateThreshold() throws Exception {
        ThresholdRuleDto rule = ThresholdRuleDto.builder()
                .bookmakerCode("GLOBAL")
                .minMatchesThreshold(500)
                .build();

        when(crawlerOpsService.updateThreshold(any())).thenReturn(rule);

        mockMvc.perform(post("/api/v1/crawler-ops/thresholds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookmakerCode\":\"GLOBAL\",\"minMatchesThreshold\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmakerCode").value("GLOBAL"));
    }
}
