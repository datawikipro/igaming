package pro.datawiki.igaming.analytics.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.analytics.dto.BookmakerThresholdDto;
import pro.datawiki.igaming.analytics.dto.CrawlerProbeResultDto;
import pro.datawiki.igaming.analytics.dto.PipelineStatsDto;
import pro.datawiki.igaming.dto.BookmakerFleetStatsDto;
import pro.datawiki.igaming.dto.DiagnosticsStatsDto;
import pro.datawiki.igaming.dto.FleetOverviewDto;
import pro.datawiki.igaming.dto.LoaderDelayDto;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrawlerOpsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private CrawlerOpsService service;

    @BeforeEach
    void setUp() {
        service = new CrawlerOpsService(restTemplate);
        ReflectionTestUtils.setField(service, "aggregatorStatsUrl", "http://igaming-aggregator/api/diagnostics/stats");
        ReflectionTestUtils.setField(service, "aggregatorFleetUrl", "http://igaming-aggregator/api/bookmakers/fleet");
        ReflectionTestUtils.setField(service, "aggregatorFleetRefreshUrl", "http://igaming-aggregator/api/bookmakers/fleet/refresh");
        ReflectionTestUtils.setField(service, "aggregatorDelaysUrl", "http://igaming-aggregator/api/diagnostics/loader-delays");
    }

    @Test
    void testToThresholdDto_Compliant() {
        BookmakerFleetStatsDto input = BookmakerFleetStatsDto.builder()
                .id("fonbet")
                .name("Фонбет")
                .logoEmoji("🔴")
                .engine("Proprietary REST / WS")
                .proxyRoute("DIRECT")
                .regions(Set.of("RU"))
                .isOnline(true)
                .lastSeen(System.currentTimeMillis())
                .activeMatchesCount(1200L)
                .activeOddsCount(35000L)
                .delayMinutes(0.5)
                .build();

        BookmakerThresholdDto dto = service.toThresholdDto(input);

        assertNotNull(dto);
        assertEquals("fonbet", dto.getId());
        assertEquals(1200L, dto.getMatchesCount());
        assertEquals(500L, dto.getTargetThreshold());
        assertEquals(100.0, dto.getThresholdProgress());
        assertEquals("COMPLIANT", dto.getComplianceStatus());
        assertEquals("FRESH", dto.getFreshnessStatus());
        assertEquals("HEALTHY", dto.getHealthStatus());
        assertTrue(dto.getHealthMessage().contains("Compliant with Golden Rule #8"));
    }

    @Test
    void testToThresholdDto_Degraded() {
        BookmakerFleetStatsDto input = BookmakerFleetStatsDto.builder()
                .id("betcity")
                .name("Бетсити")
                .logoEmoji("🔵")
                .engine("Proprietary REST / WS")
                .proxyRoute("DIRECT")
                .regions(Set.of("RU"))
                .isOnline(true)
                .lastSeen(System.currentTimeMillis())
                .activeMatchesCount(250L)
                .activeOddsCount(7000L)
                .delayMinutes(3.0)
                .build();

        BookmakerThresholdDto dto = service.toThresholdDto(input);

        assertNotNull(dto);
        assertEquals(250L, dto.getMatchesCount());
        assertEquals(50.0, dto.getThresholdProgress());
        assertEquals("DEGRADED", dto.getComplianceStatus());
        assertEquals("NORMAL", dto.getFreshnessStatus());
        assertEquals("DEGRADED", dto.getHealthStatus());
        assertTrue(dto.getHealthMessage().contains("below Golden Rule #8 threshold"));
    }

    @Test
    void testToThresholdDto_Defect() {
        BookmakerFleetStatsDto input = BookmakerFleetStatsDto.builder()
                .id("tennisi")
                .name("Тенниси")
                .logoEmoji("🟡")
                .engine("Proprietary REST / WS")
                .proxyRoute("DIRECT")
                .regions(Set.of("RU"))
                .isOnline(true)
                .activeMatchesCount(0L)
                .activeOddsCount(0L)
                .delayMinutes(20.0)
                .build();

        BookmakerThresholdDto dto = service.toThresholdDto(input);

        assertNotNull(dto);
        assertEquals(0L, dto.getMatchesCount());
        assertEquals(0.0, dto.getThresholdProgress());
        assertEquals("DEFECT", dto.getComplianceStatus());
        assertEquals("STALE", dto.getFreshnessStatus());
        assertEquals("DEGRADED", dto.getHealthStatus());
        assertTrue(dto.getHealthMessage().contains("CRITICAL DEFECT"));
    }

    @Test
    void testToThresholdDto_Offline() {
        BookmakerFleetStatsDto input = BookmakerFleetStatsDto.builder()
                .id("offline-bookmaker")
                .name("Offline")
                .isOnline(false)
                .activeMatchesCount(0L)
                .build();

        BookmakerThresholdDto dto = service.toThresholdDto(input);

        assertNotNull(dto);
        assertFalse(dto.getIsOnline());
        assertEquals("OFFLINE", dto.getHealthStatus());
        assertEquals("DEFECT", dto.getComplianceStatus());
    }

    @Test
    void testGetPipelineStats() {
        DiagnosticsStatsDto diag = DiagnosticsStatsDto.builder()
                .matches(5000L)
                .teams(2000L)
                .normalizationRequests(100L)
                .pendingNormalization(10L)
                .unrecognizedOdds(5L)
                .build();

        FleetOverviewDto fleet = FleetOverviewDto.builder()
                .totalBookmakers(2)
                .onlineBookmakers(2)
                .totalActiveMatches(5000L)
                .totalActiveOdds(120000L)
                .bookmakers(List.of(
                        BookmakerFleetStatsDto.builder().id("b1").isOnline(true).activeMatchesCount(800L).activeOddsCount(50000L).build(),
                        BookmakerFleetStatsDto.builder().id("b2").isOnline(true).activeMatchesCount(200L).activeOddsCount(10000L).build()
                ))
                .build();

        when(restTemplate.getForObject("http://igaming-aggregator/api/diagnostics/stats", DiagnosticsStatsDto.class))
                .thenReturn(diag);
        when(restTemplate.getForObject("http://igaming-aggregator/api/bookmakers/fleet", FleetOverviewDto.class))
                .thenReturn(fleet);
        when(restTemplate.getForObject("http://igaming-aggregator/api/diagnostics/loader-delays", LoaderDelayDto[].class))
                .thenReturn(new LoaderDelayDto[]{});

        PipelineStatsDto stats = service.getPipelineStats();

        assertNotNull(stats);
        assertEquals(5000L, stats.getMatches());
        assertEquals(2000L, stats.getTeams());
        assertEquals(2, stats.getTotalBookmakers());
        assertEquals(2, stats.getOnlineBookmakers());
        assertEquals(1, stats.getCompliantBookmakers());
        assertEquals(1, stats.getDegradedBookmakers());
        assertEquals(0, stats.getDefectBookmakers());
        assertEquals(50.0, stats.getComplianceRate());
        assertEquals("odds.updates", stats.getKafkaTopic());
        assertEquals("STREAMING", stats.getKafkaStatus());
    }

    @Test
    void testProbeCrawler_Success() {
        FleetOverviewDto fleet = FleetOverviewDto.builder()
                .bookmakers(List.of(
                        BookmakerFleetStatsDto.builder()
                                .id("winline")
                                .name("Winline")
                                .isOnline(true)
                                .activeMatchesCount(950L)
                                .activeOddsCount(40000L)
                                .build()
                ))
                .build();

        when(restTemplate.getForObject("http://igaming-aggregator/api/bookmakers/fleet", FleetOverviewDto.class))
                .thenReturn(fleet);

        CrawlerProbeResultDto probe = service.probeCrawler("winline");

        assertNotNull(probe);
        assertEquals("winline", probe.getBookmakerId());
        assertEquals("SUCCESS", probe.getStatus());
        assertEquals(950L, probe.getCurrentMatches());
        assertEquals(40000L, probe.getCurrentOdds());
        assertTrue(probe.getMessage().contains("COMPLIANT"));
    }

    @Test
    void testProbeCrawler_NotFound() {
        FleetOverviewDto fleet = FleetOverviewDto.builder()
                .bookmakers(List.of())
                .build();

        when(restTemplate.getForObject("http://igaming-aggregator/api/bookmakers/fleet", FleetOverviewDto.class))
                .thenReturn(fleet);

        CrawlerProbeResultDto probe = service.probeCrawler("unknown-crawler");

        assertNotNull(probe);
        assertEquals("unknown-crawler", probe.getBookmakerId());
        assertEquals("FAILED", probe.getStatus());
        assertTrue(probe.getMessage().contains("not registered"));
    }
}
