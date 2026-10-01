package pro.datawiki.igaming.analytics.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.analytics.dto.crawler.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CrawlerOpsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private CrawlerOpsService crawlerOpsService;

    @BeforeEach
    void setUp() {
        crawlerOpsService = new CrawlerOpsService(restTemplate);
        ReflectionTestUtils.setField(crawlerOpsService, "aggregatorStatsUrl", "http://igaming-aggregator/api/diagnostics/stats");
        ReflectionTestUtils.setField(crawlerOpsService, "aggregatorManagementUrl", "http://igaming-aggregator/api/v1/management");
        ReflectionTestUtils.setField(crawlerOpsService, "defaultMinMatches", 500);
        ReflectionTestUtils.setField(crawlerOpsService, "defaultWarningMatches", 300);
        ReflectionTestUtils.setField(crawlerOpsService, "defaultStaleSeconds", 180);
        crawlerOpsService.init();
    }

    @Test
    @DisplayName("init: registers exactly 52 bookmakers inventory across all target regions")
    void testInventoryInitialization() {
        List<CrawlerPipelineDto> pipelines = crawlerOpsService.getAllPipelines();
        assertThat(pipelines).hasSize(52);

        long ruCount = pipelines.stream().filter(p -> "RU_CUPIS".equals(p.getRegion())).count();
        long euCount = pipelines.stream().filter(p -> "EU_OFFSHORE".equals(p.getRegion())).count();
        long usCount = pipelines.stream().filter(p -> "US".equals(p.getRegion())).count();
        long latamCount = pipelines.stream().filter(p -> "LATAM".equals(p.getRegion())).count();

        assertThat(ruCount).isEqualTo(12);
        assertThat(euCount).isEqualTo(29);
        assertThat(usCount).isEqualTo(5);
        assertThat(latamCount).isEqualTo(6);
    }

    @Test
    @DisplayName("getPipeline: returns correct pipeline for known bookmaker")
    void testGetPipeline() {
        CrawlerPipelineDto winline = crawlerOpsService.getPipeline("winline");
        assertThat(winline).isNotNull();
        assertThat(winline.getBookmakerName()).isEqualTo("Winline (RU)");
        assertThat(winline.getRegion()).isEqualTo("RU_CUPIS");
        assertThat(winline.getRoutingType()).isEqualTo("DIRECT_SPB");
        assertThat(winline.getActiveMatches()).isGreaterThanOrEqualTo(500);
        assertThat(winline.getThresholdStatus()).isEqualTo("PASS");

        CrawlerPipelineDto pinnacle = crawlerOpsService.getPipeline("pinnacle");
        assertThat(pinnacle).isNotNull();
        assertThat(pinnacle.getRoutingType()).isEqualTo("OUTLINE_EU_NL");
    }

    @Test
    @DisplayName("getPipelineStats: computes aggregated metrics and Rule 8 compliance")
    void testGetPipelineStats() {
        PipelineStatsDto stats = crawlerOpsService.getPipelineStats();
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalPipelines()).isEqualTo(52);
        assertThat(stats.getActivePipelines()).isEqualTo(52);
        assertThat(stats.getThresholdMetCount()).isGreaterThan(0);
        assertThat(stats.getTotalActiveMatches()).isGreaterThan(20000);
        assertThat(stats.getOverallIngestionRate()).isGreaterThan(0);
        assertThat(stats.getRule8CompliancePercent()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    @DisplayName("updateThreshold: dynamically updates min matches and recomputes status")
    void testUpdateThreshold() {
        // Increase threshold for winline to 1000
        ThresholdRuleDto rule = ThresholdRuleDto.builder()
                .bookmakerCode("WINLINE")
                .minMatchesThreshold(1000)
                .warningMatchesThreshold(500)
                .maxStaleSeconds(120)
                .build();

        crawlerOpsService.updateThreshold(rule);

        CrawlerPipelineDto winline = crawlerOpsService.getPipeline("winline");
        assertThat(winline.getMinThreshold()).isEqualTo(1000);
        // If winline has 620 matches, it should now be WARN (620 < 1000 and >= 500)
        assertThat(winline.getThresholdStatus()).isEqualTo("WARN");

        // Verify active alerts includes winline warning
        List<PipelineAlertDto> alerts = crawlerOpsService.getActiveAlerts();
        assertThat(alerts).anyMatch(a -> a.getBookmakerCode().equalsIgnoreCase("winline"));
    }

    @Test
    @DisplayName("triggerSync: executes on-demand refresh and bumps line volume")
    void testTriggerSync() {
        int initialMatches = crawlerOpsService.getPipeline("betcity").getActiveMatches();

        TriggerSyncResponseDto response = crawlerOpsService.triggerSync("betcity");
        assertThat(response).isNotNull();
        assertThat(response.isTriggered()).isTrue();
        assertThat(response.getMessage()).contains("успешно запущена");

        CrawlerPipelineDto updated = crawlerOpsService.getPipeline("betcity");
        assertThat(updated.getActiveMatches()).isGreaterThanOrEqualTo(initialMatches);
        assertThat(updated.getStatus()).isEqualTo("RUNNING");
    }
}
