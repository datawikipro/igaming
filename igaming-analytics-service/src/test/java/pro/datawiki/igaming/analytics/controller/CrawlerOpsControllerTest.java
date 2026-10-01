package pro.datawiki.igaming.analytics.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pro.datawiki.igaming.analytics.dto.BookmakerThresholdDto;
import pro.datawiki.igaming.analytics.dto.CrawlerProbeResultDto;
import pro.datawiki.igaming.analytics.dto.PipelineStatsDto;
import pro.datawiki.igaming.analytics.service.CrawlerOpsService;
import pro.datawiki.igaming.dto.BookmakerFleetStatsDto;
import pro.datawiki.igaming.dto.FleetOverviewDto;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CrawlerOpsControllerTest {

    @Mock
    private CrawlerOpsService crawlerOpsService;

    @InjectMocks
    private CrawlerOpsController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetPipelineStats() throws Exception {
        PipelineStatsDto stats = PipelineStatsDto.builder()
                .matches(12500L)
                .teams(3400L)
                .normalizationRequests(500L)
                .pendingNormalization(12L)
                .unrecognizedOdds(3L)
                .totalActiveOdds(250000L)
                .totalBookmakers(52)
                .onlineBookmakers(48)
                .compliantBookmakers(42)
                .degradedBookmakers(6)
                .defectBookmakers(4)
                .complianceRate(80.8)
                .pipelineStatus("HEALTHY")
                .kafkaTopic("odds.updates")
                .kafkaStatus("STREAMING")
                .timestamp(System.currentTimeMillis())
                .build();

        when(crawlerOpsService.getPipelineStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/crawler-ops/pipeline/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches", is(12500)))
                .andExpect(jsonPath("$.totalBookmakers", is(52)))
                .andExpect(jsonPath("$.compliantBookmakers", is(42)))
                .andExpect(jsonPath("$.complianceRate", is(80.8)))
                .andExpect(jsonPath("$.pipelineStatus", is("HEALTHY")))
                .andExpect(jsonPath("$.kafkaTopic", is("odds.updates")))
                .andExpect(jsonPath("$.kafkaStatus", is("STREAMING")));
    }

    @Test
    void testGetThresholds() throws Exception {
        BookmakerThresholdDto t1 = BookmakerThresholdDto.builder()
                .id("fonbet")
                .name("Фонбет")
                .logoEmoji("🔴")
                .engine("Proprietary REST / WS")
                .proxyRoute("DIRECT")
                .isOnline(true)
                .matchesCount(1200L)
                .targetThreshold(500L)
                .thresholdProgress(100.0)
                .complianceStatus("COMPLIANT")
                .healthStatus("HEALTHY")
                .build();

        BookmakerThresholdDto t2 = BookmakerThresholdDto.builder()
                .id("pinnacle")
                .name("Pinnacle")
                .logoEmoji("🟠")
                .engine("Proprietary REST (Sharp)")
                .proxyRoute("OUTLINE_VPN_EU_NL")
                .isOnline(true)
                .matchesCount(320L)
                .targetThreshold(500L)
                .thresholdProgress(64.0)
                .complianceStatus("DEGRADED")
                .healthStatus("DEGRADED")
                .build();

        when(crawlerOpsService.getThresholds()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/v1/crawler-ops/thresholds")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("fonbet")))
                .andExpect(jsonPath("$[0].complianceStatus", is("COMPLIANT")))
                .andExpect(jsonPath("$[0].matchesCount", is(1200)))
                .andExpect(jsonPath("$[1].id", is("pinnacle")))
                .andExpect(jsonPath("$[1].complianceStatus", is("DEGRADED")))
                .andExpect(jsonPath("$[1].thresholdProgress", is(64.0)));
    }

    @Test
    void testGetFleet() throws Exception {
        FleetOverviewDto fleet = FleetOverviewDto.builder()
                .totalBookmakers(1)
                .onlineBookmakers(1)
                .compliantBookmakers(1)
                .bookmakers(List.of(
                        BookmakerFleetStatsDto.builder().id("winline").name("Winline").isOnline(true).build()
                ))
                .build();

        when(crawlerOpsService.getFleetOverview()).thenReturn(fleet);

        mockMvc.perform(get("/api/v1/crawler-ops/fleet")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookmakers", is(1)))
                .andExpect(jsonPath("$.bookmakers[0].id", is("winline")));
    }

    @Test
    void testRefreshFleet() throws Exception {
        FleetOverviewDto fleet = FleetOverviewDto.builder()
                .totalBookmakers(2)
                .build();

        when(crawlerOpsService.refreshFleetOverview()).thenReturn(fleet);

        mockMvc.perform(post("/api/v1/crawler-ops/fleet/refresh")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookmakers", is(2)));
    }

    @Test
    void testProbeCrawler() throws Exception {
        CrawlerProbeResultDto result = CrawlerProbeResultDto.builder()
                .bookmakerId("winline")
                .status("SUCCESS")
                .responseTimeMs(45L)
                .currentMatches(850L)
                .currentOdds(30000L)
                .message("Crawler [winline] responded in 45ms. Golden Rule #8 COMPLIANT.")
                .build();

        when(crawlerOpsService.probeCrawler("winline")).thenReturn(result);

        mockMvc.perform(post("/api/v1/crawler-ops/crawlers/winline/probe")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmakerId", is("winline")))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.responseTimeMs", is(45)))
                .andExpect(jsonPath("$.currentMatches", is(850)));
    }
}
