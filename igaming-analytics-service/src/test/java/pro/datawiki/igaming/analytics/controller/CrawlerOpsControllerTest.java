package pro.datawiki.igaming.analytics.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.analytics.dto.BookmakerThresholdDto;
import pro.datawiki.igaming.analytics.dto.CrawlerProbeResultDto;
import pro.datawiki.igaming.analytics.dto.PipelineStatsDto;
import pro.datawiki.igaming.dto.BookmakerFleetStatsDto;
import pro.datawiki.igaming.dto.DiagnosticsStatsDto;
import pro.datawiki.igaming.dto.FleetOverviewDto;
import pro.datawiki.igaming.dto.LoaderDelayDto;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrawlerOpsControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CrawlerOpsController controller;

    private FleetOverviewDto mockFleet;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "aggregatorFleetUrl", "http://igaming-aggregator/api/bookmakers/fleet");
        ReflectionTestUtils.setField(controller, "aggregatorFleetRefreshUrl", "http://igaming-aggregator/api/bookmakers/fleet/refresh");
        ReflectionTestUtils.setField(controller, "aggregatorStatsUrl", "http://igaming-aggregator/api/diagnostics/stats");
        ReflectionTestUtils.setField(controller, "aggregatorDelaysUrl", "http://igaming-aggregator/api/diagnostics/loader-delays");

        BookmakerFleetStatsDto winline = BookmakerFleetStatsDto.builder()
                .id("winline")
                .name("Winline")
                .logoEmoji("🟠")
                .engine("Headless Stealth")
                .proxyRoute("DIRECT")
                .regions(Set.of("RU"))
                .isOnline(true)
                .lastSeen(System.currentTimeMillis())
                .activeMatchesCount(850L) // >= 500 -> COMPLIANT
                .activeOddsCount(12400L)
                .delayMinutes(0.5)
                .healthStatus("HEALTHY")
                .build();

        BookmakerFleetStatsDto leon = BookmakerFleetStatsDto.builder()
                .id("leon")
                .name("Leon")
                .logoEmoji("🦁")
                .engine("BASIC")
                .proxyRoute("DIRECT")
                .regions(Set.of("RU"))
                .isOnline(true)
                .lastSeen(System.currentTimeMillis())
                .activeMatchesCount(250L) // < 500 -> DEGRADED
                .activeOddsCount(3100L)
                .delayMinutes(2.0)
                .healthStatus("DEGRADED")
                .build();

        BookmakerFleetStatsDto offlineBk = BookmakerFleetStatsDto.builder()
                .id("badbookie")
                .name("Bad Bookie")
                .logoEmoji("💀")
                .engine("BASIC")
                .proxyRoute("OUTLINE_US")
                .regions(Set.of("US"))
                .isOnline(false)
                .lastSeen(System.currentTimeMillis() - 600000)
                .activeMatchesCount(0L) // 0 -> DEFECT
                .activeOddsCount(0L)
                .delayMinutes(45.0)
                .healthStatus("OFFLINE")
                .build();

        mockFleet = FleetOverviewDto.builder()
                .totalBookmakers(3)
                .onlineBookmakers(2)
                .compliantBookmakers(1)
                .degradedBookmakers(1)
                .offlineBookmakers(1)
                .totalActiveMatches(1100L)
                .totalActiveOdds(15500L)
                .bookmakers(List.of(winline, leon, offlineBk))
                .generatedAt(System.currentTimeMillis())
                .build();
    }

    @Test
    @DisplayName("getFleet returns fleet overview from aggregator")
    void testGetFleet() {
        when(restTemplate.getForObject(eq("http://igaming-aggregator/api/bookmakers/fleet"), eq(FleetOverviewDto.class)))
                .thenReturn(mockFleet);

        ResponseEntity<FleetOverviewDto> response = controller.getFleet();
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTotalBookmakers());
        assertEquals(2, response.getBody().getOnlineBookmakers());
        assertEquals(1100L, response.getBody().getTotalActiveMatches());
    }

    @Test
    @DisplayName("refreshFleet triggers aggregator refresh endpoint")
    void testRefreshFleet() {
        when(restTemplate.postForObject(eq("http://igaming-aggregator/api/bookmakers/fleet/refresh"), isNull(), eq(FleetOverviewDto.class)))
                .thenReturn(mockFleet);

        ResponseEntity<FleetOverviewDto> response = controller.refreshFleet();
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTotalBookmakers());
    }

    @Test
    @DisplayName("getDelays returns list of loader delays")
    void testGetDelays() {
        LoaderDelayDto delay = LoaderDelayDto.builder()
                .bookmaker("winline")
                .matchesCount(850L)
                .oddsCount(12400L)
                .delayMinutes(0.5)
                .build();

        when(restTemplate.getForObject(eq("http://igaming-aggregator/api/diagnostics/loader-delays"), eq(LoaderDelayDto[].class)))
                .thenReturn(new LoaderDelayDto[]{delay});

        ResponseEntity<List<LoaderDelayDto>> response = controller.getDelays();
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("winline", response.getBody().get(0).getBookmaker());
    }

    @Test
    @DisplayName("getPipelineStats combines diagnostics and fleet data")
    void testGetPipelineStats() {
        DiagnosticsStatsDto diagStats = DiagnosticsStatsDto.builder()
                .matches(25000L)
                .teams(110000L)
                .normalizationRequests(50000L)
                .pendingNormalization(1200L)
                .unrecognizedOdds(300L)
                .build();

        when(restTemplate.getForObject(eq("http://igaming-aggregator/api/diagnostics/stats"), eq(DiagnosticsStatsDto.class)))
                .thenReturn(diagStats);
        when(restTemplate.getForObject(eq("http://igaming-aggregator/api/bookmakers/fleet"), eq(FleetOverviewDto.class)))
                .thenReturn(mockFleet);

        ResponseEntity<PipelineStatsDto> response = controller.getPipelineStats();
        assertNotNull(response.getBody());
        PipelineStatsDto stats = response.getBody();

        assertEquals(25000L, stats.getMatches());
        assertEquals(110000L, stats.getTeams());
        assertEquals(3, stats.getTotalBookmakers());
        assertEquals(2, stats.getOnlineBookmakers());
        assertEquals(1, stats.getCompliantBookmakers());
        assertEquals(50.0, stats.getComplianceRate()); // 1 compliant out of 2 online = 50%
        assertEquals("odds.updates", stats.getKafkaTopic());
        assertEquals("STREAMING", stats.getKafkaStatus());
    }

    @Test
    @DisplayName("getThresholds accurately evaluates Golden Rule #8 (>= 500 matches)")
    void testGetThresholds() {
        when(restTemplate.getForObject(eq("http://igaming-aggregator/api/bookmakers/fleet"), eq(FleetOverviewDto.class)))
                .thenReturn(mockFleet);

        ResponseEntity<List<BookmakerThresholdDto>> response = controller.getThresholds(null, null, null);
        assertNotNull(response.getBody());
        List<BookmakerThresholdDto> thresholds = response.getBody();
        assertEquals(3, thresholds.size());

        // Winline: 850 matches >= 500 -> COMPLIANT, progress 100%
        BookmakerThresholdDto winlineDto = thresholds.stream().filter(t -> "winline".equals(t.getId())).findFirst().orElseThrow();
        assertEquals("COMPLIANT", winlineDto.getComplianceStatus());
        assertEquals(100.0, winlineDto.getThresholdProgress());
        assertEquals(500L, winlineDto.getTargetThreshold());
        assertEquals("FRESH", winlineDto.getFreshnessStatus());

        // Leon: 250 matches < 500 -> DEGRADED, progress 50%
        BookmakerThresholdDto leonDto = thresholds.stream().filter(t -> "leon".equals(t.getId())).findFirst().orElseThrow();
        assertEquals("DEGRADED", leonDto.getComplianceStatus());
        assertEquals(50.0, leonDto.getThresholdProgress());
        assertEquals("NORMAL", leonDto.getFreshnessStatus());

        // Bad Bookie: 0 matches -> DEFECT
        BookmakerThresholdDto defectDto = thresholds.stream().filter(t -> "badbookie".equals(t.getId())).findFirst().orElseThrow();
        assertEquals("DEFECT", defectDto.getComplianceStatus());
        assertEquals(0.0, defectDto.getThresholdProgress());
        assertEquals("STALE", defectDto.getFreshnessStatus());
    }

    @Test
    @DisplayName("getThresholds filters by status, search, and proxyRoute")
    void testGetThresholdsFilters() {
        when(restTemplate.getForObject(eq("http://igaming-aggregator/api/bookmakers/fleet"), eq(FleetOverviewDto.class)))
                .thenReturn(mockFleet);

        // Filter COMPLIANT
        List<BookmakerThresholdDto> compliantOnly = controller.getThresholds("COMPLIANT", null, null).getBody();
        assertEquals(1, compliantOnly.size());
        assertEquals("winline", compliantOnly.get(0).getId());

        // Filter by proxy route OUTLINE_US
        List<BookmakerThresholdDto> usOnly = controller.getThresholds(null, "OUTLINE_US", null).getBody();
        assertEquals(1, usOnly.size());
        assertEquals("badbookie", usOnly.get(0).getId());

        // Search by name "leo"
        List<BookmakerThresholdDto> searchResult = controller.getThresholds(null, null, "leo").getBody();
        assertEquals(1, searchResult.size());
        assertEquals("leon", searchResult.get(0).getId());
    }

    @Test
    @DisplayName("probeCrawler executes diagnostic probe for crawler")
    void testProbeCrawler() {
        when(restTemplate.getForObject(eq("http://igaming-aggregator/api/bookmakers/fleet"), eq(FleetOverviewDto.class)))
                .thenReturn(mockFleet);

        // Probe Winline (Compliant)
        ResponseEntity<CrawlerProbeResultDto> probeWin = controller.probeCrawler("winline");
        assertNotNull(probeWin.getBody());
        assertEquals("SUCCESS", probeWin.getBody().getStatus());
        assertEquals(850L, probeWin.getBody().getCurrentMatches());

        // Probe Leon (Degraded)
        ResponseEntity<CrawlerProbeResultDto> probeLeon = controller.probeCrawler("leon");
        assertNotNull(probeLeon.getBody());
        assertEquals("DEGRADED", probeLeon.getBody().getStatus());
        assertTrue(probeLeon.getBody().getMessage().contains("below Golden Rule #8 threshold"));

        // Probe Bad Bookie (Offline)
        ResponseEntity<CrawlerProbeResultDto> probeBad = controller.probeCrawler("badbookie");
        assertNotNull(probeBad.getBody());
        assertEquals("FAILED", probeBad.getBody().getStatus());
        assertTrue(probeBad.getBody().getMessage().contains("OFFLINE"));
    }
}
