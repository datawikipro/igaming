package pro.datawiki.igaming.analyst.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.datawiki.igaming.analyst.repository.MatchExternalContextRepository;
import pro.datawiki.igaming.analyst.service.odds.AntiPublicLineDetectorService;
import pro.datawiki.igaming.dto.sport.Sports;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OddsQualityAssessmentServiceTest {

    @Mock
    private AntiPublicLineDetectorService antiPublicDetector;
    @Mock
    private MatchDemographicsGeoService geoService;
    @Mock
    private MatchWeatherService weatherService;
    @Mock
    private MarketAnomalyNewsRadarService newsRadarService;
    @Mock
    private MatchExternalContextRepository contextRepository;

    @Test
    void gradesEliteValueWithMetroBiasAndCleanNewsAndPersistsContext() {
        OddsQualityAssessmentService svc = new OddsQualityAssessmentService(
                antiPublicDetector, geoService, weatherService, newsRadarService, contextRepository, new ObjectMapper()
        );

        when(antiPublicDetector.analyzeMarketMovement(555L, "WIN2", 52.0)).thenReturn(
                new AntiPublicLineDetectorService.AntiPublicSignal(
                        "SHARP_VS_SOFT_VALUE", 2.20, 2.25, 2.42, 2.3, 7.6, true, true, "Sharp anchor note"
                )
        );
        MatchDemographicsGeoService.TeamGeoProfile moscow = new MatchDemographicsGeoService.TeamGeoProfile(
                10L, 1L, "Moscow", 13_000_000L, true, 55.75, 37.61, 150, "Europe/Moscow", 45000, false
        );
        MatchDemographicsGeoService.TeamGeoProfile smallCity = new MatchDemographicsGeoService.TeamGeoProfile(
                20L, 2L, "Villarreal", 51_000L, false, 39.94, -0.10, 42, "Europe/Madrid", 23000, false
        );
        when(geoService.resolveMatchGeoContext(any())).thenReturn(
                new MatchDemographicsGeoService.MatchGeoContext(moscow, smallCity, 2.406, 3300, 108, 2.0, true, "Metro note")
        );
        when(weatherService.fetchMatchWeather(anyDouble(), anyDouble(), anyBoolean(), any(), anyString())).thenReturn(
                MatchWeatherService.classifyWeather(16.0, 10.0, 0.0, 1)
        );
        when(newsRadarService.scanAnomalyNews(eq(555L), anyString(), anyString(), anyDouble())).thenReturn(
                MarketAnomalyNewsRadarService.NewsRadarResult.notTriggered()
        );

        OddsQualityAssessmentService.OddsQualityVerdict verdict = svc.assessAndPersist(
                555L, "WIN2", 52.0, 2.42, "Fonbet (RU)",
                Map.of("team1_name", "Spartak", "team2_name", "Villarreal", "start_time", System.currentTimeMillis() + 3600_000L),
                Sports.get("football")
        );

        assertEquals("ELITE_VALUE", verdict.qualityGrade());
        assertTrue(verdict.riskRewardScore() >= 85.0);
        assertNull(verdict.riskWarning());
        assertNotNull(verdict.humanSummary());
        assertTrue(verdict.humanSummary().contains("ELITE_VALUE"));
        verify(contextRepository).save(any());
    }

    @Test
    void flagsInsightWarningWhenBigDriftMatchesInjuryNews() {
        OddsQualityAssessmentService svc = new OddsQualityAssessmentService(
                antiPublicDetector, geoService, weatherService, newsRadarService, contextRepository, new ObjectMapper()
        );

        when(antiPublicDetector.analyzeMarketMovement(556L, "WIN1", 72.0)).thenReturn(
                new AntiPublicLineDetectorService.AntiPublicSignal(
                        "DRIFT_UP", 1.38, 1.72, 1.72, 24.6, 0.0, true, false, "Drift up"
                )
        );
        when(geoService.resolveMatchGeoContext(any())).thenReturn(MatchDemographicsGeoService.MatchGeoContext.empty());
        when(weatherService.fetchMatchWeather(anyDouble(), anyDouble(), anyBoolean(), any(), anyString())).thenReturn(
                MatchWeatherService.WeatherForecast.unavailable()
        );
        when(newsRadarService.scanAnomalyNews(eq(556L), anyString(), anyString(), eq(24.6))).thenReturn(
                new MarketAnomalyNewsRadarService.NewsRadarResult(
                        true, "INJURY_OR_ROTATION_INSIGHT", "🚨 NEWS RADAR: Star striker ruled out with hamstring injury",
                        java.util.List.of("Star striker ruled out with hamstring injury")
                )
        );

        OddsQualityAssessmentService.OddsQualityVerdict verdict = svc.assessAndPersist(
                556L, "WIN1", 72.0, 1.72, "Pinnacle",
                Map.of("team1_name", "Real Madrid", "team2_name", "Getafe"),
                Sports.get("football")
        );

        assertEquals("INSIGHT_WARNING", verdict.qualityGrade());
        assertEquals(15.0, verdict.riskRewardScore());
        assertNotNull(verdict.riskWarning());
        assertTrue(verdict.riskWarning().contains("ruled out"));
    }

    @Test
    void classifiesWeatherAndRssHeadlinesCorrectly() {
        MatchWeatherService.WeatherForecast storm = MatchWeatherService.classifyWeather(11.0, 38.0, 4.2, 65);
        assertEquals("HEAVY_RAIN", storm.riskLevel());

        MarketAnomalyNewsRadarService radar = new MarketAnomalyNewsRadarService(null, new ObjectMapper());
        String rss = "<rss><channel><item><title>Arsenal star Bukayo Saka ruled out with ankle injury</title></item></channel></rss>";
        MarketAnomalyNewsRadarService.NewsRadarResult r = radar.classifyRssHeadlines(rss);
        assertEquals("INJURY_OR_ROTATION_INSIGHT", r.signalType());
        assertTrue(r.headlineSummary().contains("Bukayo Saka"));
    }
}
