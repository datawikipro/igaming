package pro.datawiki.igaming.source.betfair.service.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.betfair.config.BetfairConfig;
import pro.datawiki.igaming.source.betfair.dto.exchange.BetfairMarketBook;
import pro.datawiki.igaming.source.betfair.dto.exchange.BetfairRunnerBook;
import pro.datawiki.igaming.source.betfair.dto.exchange.SmartMoneyAlert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects abnormal traded volume jumps and institutional steam (Smart Money)
 * across Betfair Exchange ladders.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SmartMoneyDetector {

    private final BetfairConfig betfairConfig;

    private final Map<String, RunnerVolumeState> volumeTracker = new ConcurrentHashMap<>();

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Inspects a market book for sudden volume surges on any runner.
     */
    public List<SmartMoneyAlert> detectAnomalies(BetfairMarketBook marketBook) {
        List<SmartMoneyAlert> alerts = new ArrayList<>();
        if (marketBook == null || marketBook.getRunners() == null || marketBook.getRunners().isEmpty()) {
            return alerts;
        }

        double minDelta = betfairConfig.getSmartMoney().getMinDeltaVolume();
        double minRatio = betfairConfig.getSmartMoney().getVolumeSpikeRatio();
        double marketTotalVolume = marketBook.getTotalMatched() != null ? marketBook.getTotalMatched() : 0.0;

        for (BetfairRunnerBook runner : marketBook.getRunners()) {
            if (runner == null || runner.getSelectionId() == null) continue;

            Double currentVolume = runner.getTotalMatched();
            if (currentVolume == null || currentVolume <= 0.0) continue;

            String stateKey = marketBook.getMarketId() + ":" + runner.getSelectionId();
            Double currentPrice = runner.getLastPriceTraded() != null ? runner.getLastPriceTraded() : runner.getBestBackPrice();
            if (currentPrice == null) currentPrice = 1.0;

            RunnerVolumeState previousState = volumeTracker.get(stateKey);

            if (previousState == null) {
                // Initialize baseline state
                volumeTracker.put(stateKey, new RunnerVolumeState(currentVolume, currentPrice, System.currentTimeMillis()));
                continue;
            }

            double volumeDelta = currentVolume - previousState.getLastTradedVolume();
            if (volumeDelta <= 0.0) {
                // Volume did not increase, just update timestamp
                previousState.setLastSeenTimestamp(System.currentTimeMillis());
                continue;
            }

            double prevVolume = previousState.getLastTradedVolume();
            double growthRatio = prevVolume > 0.0 ? (volumeDelta / prevVolume) : 1.0;
            double growthPercent = roundTwoDecimals(growthRatio * 100.0);

            // Anomaly criteria: either absolute leap exceeds threshold with minimum growth,
            // or massive institutional chunk (>= $10,000 / £10,000)
            boolean isAnomaly = (volumeDelta >= minDelta && growthRatio >= minRatio)
                    || (volumeDelta >= 10000.0);

            if (isAnomaly) {
                String severity = determineSeverity(volumeDelta, growthRatio);
                String direction = determineDirection(previousState.getLastPrice(), currentPrice);
                double volumeShare = marketTotalVolume > 0.0 ? roundTwoDecimals((currentVolume / marketTotalVolume) * 100.0) : 0.0;

                String alertId = String.format("sma-%d-%s", System.currentTimeMillis(), runner.getSelectionId());
                String description = String.format(
                        "Smart Money alert: [%s] Institutional %s on %s in '%s' (+%.0f volume surge, +%.1f%% growth, odds %.2f -> %.2f, market share: %.1f%%)",
                        severity, direction, runner.getRunnerName(), marketBook.getMatchName(),
                        volumeDelta, growthPercent, previousState.getLastPrice(), currentPrice, volumeShare
                );

                SmartMoneyAlert alert = SmartMoneyAlert.builder()
                        .alertId(alertId)
                        .bookmaker("betfair")
                        .exchange("betfair-exchange")
                        .eventId(marketBook.getEventId())
                        .matchName(marketBook.getMatchName())
                        .sport(marketBook.getSport() != null ? marketBook.getSport() : "Soccer")
                        .league(marketBook.getLeague() != null ? marketBook.getLeague() : "Exchange Match")
                        .marketId(marketBook.getMarketId())
                        .marketName(marketBook.getMarketName())
                        .runnerName(runner.getRunnerName())
                        .selectionId(runner.getSelectionId())
                        .previousTradedVolume(roundTwoDecimals(prevVolume))
                        .currentTradedVolume(roundTwoDecimals(currentVolume))
                        .volumeDelta(roundTwoDecimals(volumeDelta))
                        .volumeGrowthPercent(growthPercent)
                        .volumeShareOfMarket(volumeShare)
                        .lastPriceTraded(currentPrice)
                        .bestBackPrice(runner.getBestBackPrice())
                        .bestLayPrice(runner.getBestLayPrice())
                        .severity(severity)
                        .direction(direction)
                        .detectedAt(LocalDateTime.now().format(ISO_FORMATTER))
                        .description(description)
                        .build();

                log.info("[SMART MONEY ALERT] {} on runner '{}' ({}) in match '{}' - Delta: +{}, Growth: {}%",
                        severity, runner.getRunnerName(), direction, marketBook.getMatchName(),
                        roundTwoDecimals(volumeDelta), growthPercent);

                alerts.add(alert);
            }

            // Update tracked state
            previousState.setLastTradedVolume(currentVolume);
            previousState.setLastPrice(currentPrice);
            previousState.setLastSeenTimestamp(System.currentTimeMillis());
        }

        // Periodic cleanup if map grows too large
        if (volumeTracker.size() > 50000) {
            long cutoff = System.currentTimeMillis() - (6 * 3600 * 1000); // 6 hours
            volumeTracker.entrySet().removeIf(entry -> entry.getValue().getLastSeenTimestamp() < cutoff);
        }

        return alerts;
    }

    private String determineSeverity(double volumeDelta, double growthRatio) {
        if (volumeDelta >= 10000.0 || growthRatio >= 1.0) {
            return "INSTITUTIONAL_LOAD";
        } else if (volumeDelta >= 5000.0 || growthRatio >= 0.50) {
            return "CRITICAL_STEAM";
        } else if (volumeDelta >= 2500.0) {
            return "SIGNIFICANT_INFLOW";
        }
        return "VOLUME_SPIKE";
    }

    private String determineDirection(double oldPrice, double newPrice) {
        if (newPrice < oldPrice - 0.02) {
            return "BACK_STEAM"; // Odds shortened: heavy backing
        } else if (newPrice > oldPrice + 0.02) {
            return "LAY_PRESSURE"; // Odds lengthened: heavy laying
        }
        return "LIQUIDITY_SURGE";
    }

    private double roundTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public void clearTracker() {
        volumeTracker.clear();
    }

    public int getTrackerSize() {
        return volumeTracker.size();
    }

    @Data
    @AllArgsConstructor
    public static class RunnerVolumeState {
        private double lastTradedVolume;
        private double lastPrice;
        private long lastSeenTimestamp;
    }
}
