package pro.datawiki.igaming.capture.sofascore.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.sport.basketball.BasketballSport;
import pro.datawiki.igaming.dto.sport.hockey.IceHockeySport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedStatsCaptureTest {

    private AdvancedStatsCalculator calculator;
    private AdvancedMetricsKafkaPublisher publisher;
    private AdvancedStatsCaptureService captureService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        calculator = new AdvancedStatsCalculator();
        publisher = new AdvancedMetricsKafkaPublisher(objectMapper);
        captureService = new AdvancedStatsCaptureService(calculator, publisher);
    }

    @Test
    @DisplayName("Ice Hockey (KHL / NHL): Corsi, Fenwick, HDCF, and Goalie GSAx calculations")
    void testIceHockeyAdvancedMetricsCalculations() {
        // Match: SKA St. Petersburg vs CSKA Moscow (KHL)
        // Home: 32 SOG, 12 missed, 16 blocked. Slot shots = 14. Goals = 4. Away goalie xGA = 3.65.
        // Away: 28 SOG, 10 missed, 14 blocked. Slot shots = 10. Goals = 2. Home goalie xGA = 2.85.
        IceHockeyAdvancedMetrics metrics = calculator.computeHockeyMetrics(
                32, 28,
                12, 10,
                16, 14,
                14, 10,
                4, 2,
                2.85, 3.65,
                "Nikita Serebryakov", "Ivan Fedotov"
        );

        assertNotNull(metrics);

        // Corsi = SOG + Missed + Blocked
        // Home CF = 32 + 12 + 16 = 60
        // Away CF = 28 + 10 + 14 = 52
        assertEquals(60.0, metrics.getHomeCorsiFor());
        assertEquals(52.0, metrics.getHomeCorsiAgainst());
        // Corsi% = 60 / (60 + 52) * 100 = 60 / 112 * 100 = 53.57%
        assertEquals(53.57, metrics.getHomeCorsiPct());
        assertEquals(46.43, metrics.getAwayCorsiPct());

        // Fenwick = SOG + Missed
        // Home FF = 32 + 12 = 44
        // Away FF = 28 + 10 = 38
        assertEquals(44.0, metrics.getHomeFenwickFor());
        assertEquals(38.0, metrics.getHomeFenwickAgainst());
        // Fenwick% = 44 / (44 + 38) * 100 = 44 / 82 * 100 = 53.66%
        assertEquals(53.66, metrics.getHomeFenwickPct());
        assertEquals(46.34, metrics.getAwayFenwickPct());

        // High Danger Scoring Chances (HDCF)
        assertEquals(14.0, metrics.getHomeHdcf());
        assertEquals(10.0, metrics.getHomeHdca());
        // HDCF% = 14 / 24 * 100 = 58.33%
        assertEquals(58.33, metrics.getHomeHdcfPct());

        // Goaltender GSAx = xGA - actual GA
        // Home goalie: faces 2 goals, xGA = 2.85 -> GSAx = 2.85 - 2 = +0.85 (outperforming)
        // Away goalie: faces 4 goals, xGA = 3.65 -> GSAx = 3.65 - 4 = -0.35
        assertEquals(0.85, metrics.getHomeGoalieGsax());
        assertEquals(-0.35, metrics.getAwayGoalieGsax());
        assertEquals("Nikita Serebryakov", metrics.getHomeGoalieName());
        assertEquals(26, metrics.getHomeGoalieSaves()); // 28 shots - 2 goals
    }

    @Test
    @DisplayName("Basketball (NBA / EuroLeague): Pace, ORtg, DRtg, TS%, and Bench Rotation Depth calculations")
    void testBasketballAdvancedMetricsCalculations() {
        // Match: Real Madrid vs Panathinaikos (EuroLeague)
        // Regulation minutes = 200 (40 mins * 5 players)
        // Home: 88 pts, 65 FGA, 20 FTA, 10 ORB, 12 TOV, Bench pts = 34, Bench minutes: [18.5, 15.0, 12.0, 8.0, 4.0]
        // Away: 82 pts, 62 FGA, 18 FTA, 8 ORB, 14 TOV, Bench pts = 26, Bench minutes: [20.0, 14.5, 9.0]
        List<Double> homeBenchMinutes = List.of(18.5, 15.0, 12.0, 8.0, 4.0);
        List<Double> awayBenchMinutes = List.of(20.0, 14.5, 9.0);

        BasketballAdvancedMetrics metrics = calculator.computeBasketballMetrics(
                88, 82,
                65, 62,
                20, 18,
                10, 8,
                12, 14,
                200.0,
                homeBenchMinutes, awayBenchMinutes,
                34, 26
        );

        assertNotNull(metrics);

        // Home Poss = 65 + 0.44 * 20 - 10 + 12 = 65 + 8.8 - 10 + 12 = 75.8
        // Away Poss = 62 + 0.44 * 18 - 8 + 14 = 62 + 7.92 - 8 + 14 = 75.92
        assertEquals(75.8, metrics.getHomePossessions());
        assertEquals(75.92, metrics.getAwayPossessions());

        // Average possessions = (75.8 + 75.92) / 2 = 75.86
        // Pace per 48m = 48.0 * (75.86 / 40.0) = 91.03
        assertEquals(91.03, metrics.getPace());

        // ORtg = 100 * (88 / 75.8) = 116.09
        assertEquals(116.09, metrics.getHomeOffensiveRating());
        // DRtg = 100 * (82 / 75.92) = 108.01
        assertEquals(108.01, metrics.getHomeDefensiveRating());
        // NetRtg = 116.09 - 108.01 = 8.08
        assertEquals(8.08, metrics.getHomeNetRating());

        // True Shooting %: 88 / (2 * (65 + 0.44 * 20)) * 100 = 88 / (2 * 73.8) * 100 = 88 / 147.6 * 100 = 59.62%
        assertEquals(59.62, metrics.getHomeTrueShootingPct());

        // Bench Rotation Depth (players with >= 10.0 min):
        // Home has 18.5, 15.0, 12.0 -> 3 players >= 10 mins
        // Away has 20.0, 14.5 -> 2 players >= 10 mins
        assertEquals(3, metrics.getHomeBenchRotationDepth());
        assertEquals(2, metrics.getAwayBenchRotationDepth());

        // Bench Scoring Share: 34 / 88 = 0.39 (38.64%)
        assertEquals(0.39, metrics.getHomeBenchScoringShare());
    }

    @Test
    @DisplayName("Kafka topic configuration matches canonical specification")
    void testKafkaTopicSpecification() {
        assertEquals("sports.canonical.advanced-metrics", CanonicalAdvancedMetricsEvent.TOPIC_NAME);
        assertEquals("sports.canonical.advanced-metrics", publisher.getTopicName());
    }

    @Test
    @DisplayName("End-to-end Ice Hockey capture and publication to sports.canonical.advanced-metrics")
    void testCaptureAndPublishHockey() {
        CanonicalAdvancedMetricsEvent event = captureService.captureAndPublishHockeyStats(
                "SOFASCORE", "NHL", "nhl-event-99412",
                System.currentTimeMillis(), "Florida Panthers", "Edmonton Oilers",
                35, 30,
                15, 12,
                18, 14,
                16, 12,
                5, 3,
                3.10, 4.20,
                "Sergei Bobrovsky", "Stuart Skinner"
        );

        assertNotNull(event);
        assertEquals("ICE_HOCKEY", event.getSport());
        assertEquals("NHL", event.getLeagueName());
        assertNotNull(event.getHockeyMetrics());
        assertTrue(event.getHockeyMetrics().getHomeCorsiPct() > 50.0);
        assertTrue(publisher.getPublishedCount() >= 1);
    }

    @Test
    @DisplayName("End-to-end Basketball capture and publication to sports.canonical.advanced-metrics")
    void testCaptureAndPublishBasketball() {
        CanonicalAdvancedMetricsEvent event = captureService.captureAndPublishBasketballStats(
                "SOFASCORE", "NBA", "nba-event-77218",
                System.currentTimeMillis(), "Boston Celtics", "Dallas Mavericks",
                112, 104,
                88, 85,
                24, 20,
                12, 9,
                10, 13,
                240.0,
                List.of(24.0, 18.5, 14.0, 8.0),
                List.of(22.0, 16.0, 9.5),
                38, 28
        );

        assertNotNull(event);
        assertEquals("BASKETBALL", event.getSport());
        assertEquals("NBA", event.getLeagueName());
        assertNotNull(event.getBasketballMetrics());
        assertTrue(event.getBasketballMetrics().getPace() > 90.0);
        assertEquals(3, event.getBasketballMetrics().getHomeBenchRotationDepth());
        assertTrue(publisher.getPublishedCount() >= 1);
    }

    @Test
    @DisplayName("Domain contracts IceHockeySport and BasketballSport expose institutional metric names")
    void testDomainSportContracts() {
        IceHockeySport hockeySport = new IceHockeySport();
        List<String> hockeyMetrics = hockeySport.getInstitutionalMetricNames();
        assertTrue(hockeyMetrics.contains("corsi_for_pct"));
        assertTrue(hockeyMetrics.contains("fenwick_for_pct"));
        assertTrue(hockeyMetrics.contains("high_danger_scoring_chances_pct"));
        assertTrue(hockeyMetrics.contains("goalie_gsax"));

        BasketballSport basketballSport = new BasketballSport("BASKETBALL", "basketball", "🏀", java.util.Map.of()) {};
        List<String> bkbMetrics = basketballSport.getInstitutionalMetricNames();
        assertTrue(bkbMetrics.contains("pace"));
        assertTrue(bkbMetrics.contains("offensive_rating"));
        assertTrue(bkbMetrics.contains("defensive_rating"));
        assertTrue(bkbMetrics.contains("true_shooting_pct"));
        assertTrue(bkbMetrics.contains("bench_rotation_depth"));
    }
}
