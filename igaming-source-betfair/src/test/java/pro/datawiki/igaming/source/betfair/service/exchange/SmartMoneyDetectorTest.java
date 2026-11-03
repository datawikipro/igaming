package pro.datawiki.igaming.source.betfair.service.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.source.betfair.config.BetfairConfig;
import pro.datawiki.igaming.source.betfair.dto.exchange.BetfairMarketBook;
import pro.datawiki.igaming.source.betfair.dto.exchange.BetfairRunnerBook;
import pro.datawiki.igaming.source.betfair.dto.exchange.SmartMoneyAlert;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmartMoneyDetectorTest {

    private SmartMoneyDetector detector;
    private BetfairConfig config;

    @BeforeEach
    void setUp() {
        config = new BetfairConfig();
        config.getSmartMoney().setEnabled(true);
        config.getSmartMoney().setMinDeltaVolume(2500.0);
        config.getSmartMoney().setVolumeSpikeRatio(0.30);

        detector = new SmartMoneyDetector(config);
    }

    @Test
    void testFirstObservation_EstablishesBaseline_NoAlert() {
        BetfairRunnerBook runner = BetfairRunnerBook.builder()
                .selectionId("12345")
                .runnerName("Arsenal")
                .totalMatched(50000.0)
                .bestBackPrice(1.85)
                .build();

        BetfairMarketBook market = BetfairMarketBook.builder()
                .marketId("1.1001")
                .eventId("bf-001")
                .matchName("Arsenal vs Chelsea")
                .sport("Soccer")
                .league("Premier League")
                .totalMatched(100000.0)
                .runners(List.of(runner))
                .build();

        List<SmartMoneyAlert> alerts = detector.detectAnomalies(market);
        assertTrue(alerts.isEmpty(), "First observation should establish baseline and not fire alert");
        assertEquals(1, detector.getTrackerSize());
    }

    @Test
    void testModerateVolumeIncrease_BelowThreshold_NoAlert() {
        BetfairRunnerBook runner1 = BetfairRunnerBook.builder()
                .selectionId("12345")
                .runnerName("Arsenal")
                .totalMatched(50000.0)
                .bestBackPrice(1.85)
                .build();

        BetfairMarketBook market1 = BetfairMarketBook.builder()
                .marketId("1.1001")
                .eventId("bf-001")
                .matchName("Arsenal vs Chelsea")
                .totalMatched(100000.0)
                .runners(List.of(runner1))
                .build();

        detector.detectAnomalies(market1);

        // Small volume change (+500, ratio = 1%)
        BetfairRunnerBook runner2 = BetfairRunnerBook.builder()
                .selectionId("12345")
                .runnerName("Arsenal")
                .totalMatched(50500.0)
                .bestBackPrice(1.85)
                .build();

        BetfairMarketBook market2 = BetfairMarketBook.builder()
                .marketId("1.1001")
                .eventId("bf-001")
                .matchName("Arsenal vs Chelsea")
                .totalMatched(100500.0)
                .runners(List.of(runner2))
                .build();

        List<SmartMoneyAlert> alerts = detector.detectAnomalies(market2);
        assertTrue(alerts.isEmpty(), "Moderate volume increase below threshold should not fire alert");
    }

    @Test
    void testAbnormalVolumeJump_TriggersSmartMoneyAlert_BackSteam() {
        // 1. Initial snapshot: £50,000 at 2.10
        BetfairRunnerBook runner1 = BetfairRunnerBook.builder()
                .selectionId("12345")
                .runnerName("Arsenal")
                .totalMatched(50000.0)
                .bestBackPrice(2.10)
                .lastPriceTraded(2.10)
                .build();

        BetfairMarketBook market1 = BetfairMarketBook.builder()
                .marketId("1.1001")
                .eventId("bf-001")
                .matchName("Arsenal vs Chelsea")
                .totalMatched(100000.0)
                .runners(List.of(runner1))
                .build();

        detector.detectAnomalies(market1);

        // 2. Sudden institutional steam: volume jumps from £50,000 to £85,000 (+£35,000, 70% surge)
        // and odds shorten from 2.10 down to 1.82
        BetfairRunnerBook runner2 = BetfairRunnerBook.builder()
                .selectionId("12345")
                .runnerName("Arsenal")
                .totalMatched(85000.0)
                .bestBackPrice(1.82)
                .lastPriceTraded(1.82)
                .build();

        BetfairMarketBook market2 = BetfairMarketBook.builder()
                .marketId("1.1001")
                .eventId("bf-001")
                .matchName("Arsenal vs Chelsea")
                .sport("Soccer")
                .league("Premier League")
                .totalMatched(150000.0)
                .runners(List.of(runner2))
                .build();

        List<SmartMoneyAlert> alerts = detector.detectAnomalies(market2);
        assertEquals(1, alerts.size(), "Should trigger exactly 1 Smart Money alert");

        SmartMoneyAlert alert = alerts.get(0);
        assertEquals("betfair", alert.getBookmaker());
        assertEquals("betfair-exchange", alert.getExchange());
        assertEquals("bf-001", alert.getEventId());
        assertEquals("1.1001", alert.getMarketId());
        assertEquals("12345", alert.getSelectionId());
        assertEquals("Arsenal", alert.getRunnerName());
        assertEquals(50000.0, alert.getPreviousTradedVolume());
        assertEquals(85000.0, alert.getCurrentTradedVolume());
        assertEquals(35000.0, alert.getVolumeDelta());
        assertEquals(70.0, alert.getVolumeGrowthPercent());
        assertEquals("BACK_STEAM", alert.getDirection(), "Shortened price with volume surge indicates BACK_STEAM");
        assertEquals("INSTITUTIONAL_LOAD", alert.getSeverity(), "Delta >= 10,000 indicates INSTITUTIONAL_LOAD");
        assertNotNull(alert.getDetectedAt());
        assertNotNull(alert.getDescription());
        assertTrue(alert.getDescription().contains("Arsenal"));
    }

    @Test
    void testLayPressure_OddsLengthenedWithVolume() {
        // Initial snapshot
        BetfairRunnerBook runner1 = BetfairRunnerBook.builder()
                .selectionId("99999")
                .runnerName("Chelsea")
                .totalMatched(20000.0)
                .bestBackPrice(3.20)
                .lastPriceTraded(3.20)
                .build();

        BetfairMarketBook market1 = BetfairMarketBook.builder()
                .marketId("1.2002")
                .eventId("bf-002")
                .matchName("Arsenal vs Chelsea")
                .totalMatched(50000.0)
                .runners(List.of(runner1))
                .build();

        detector.detectAnomalies(market1);

        // Volume increases by £8,000 (40%), price drifts out to 3.80
        BetfairRunnerBook runner2 = BetfairRunnerBook.builder()
                .selectionId("99999")
                .runnerName("Chelsea")
                .totalMatched(28000.0)
                .bestBackPrice(3.80)
                .lastPriceTraded(3.80)
                .build();

        BetfairMarketBook market2 = BetfairMarketBook.builder()
                .marketId("1.2002")
                .eventId("bf-002")
                .matchName("Arsenal vs Chelsea")
                .totalMatched(60000.0)
                .runners(List.of(runner2))
                .build();

        List<SmartMoneyAlert> alerts = detector.detectAnomalies(market2);
        assertEquals(1, alerts.size());

        SmartMoneyAlert alert = alerts.get(0);
        assertEquals("LAY_PRESSURE", alert.getDirection());
        assertEquals("CRITICAL_STEAM", alert.getSeverity());
    }
}
