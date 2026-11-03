package pro.datawiki.igaming.source.betfair.service.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.betfair.config.BetfairConfig;
import pro.datawiki.igaming.source.betfair.dto.exchange.BetfairMarketBook;
import pro.datawiki.igaming.source.betfair.dto.exchange.BetfairRunnerBook;
import pro.datawiki.igaming.source.betfair.dto.exchange.SmartMoneyAlert;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.domain.MatchFactor;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Periodically crawls Betfair Exchange liquidity, evaluates Smart Money volume jumps,
 * publishes alerts to Kafka topic `odds.smart-money`, and synchronizes outcomes into match_factor and match_cache.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "betfair.exchange.smart-money.enabled", havingValue = "true", matchIfMissing = true)
public class BetfairExchangeScheduler {

    private final BetfairExchangeCrawler exchangeCrawler;
    private final SmartMoneyDetector smartMoneyDetector;
    private final SmartMoneyKafkaPublisher smartMoneyKafkaPublisher;
    private final MatchPersistenceService persistenceService;
    private final MatchCacheRepository matchCacheRepository;
    private final BetfairConfig betfairConfig;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${betfair.exchange.smart-money.poll-interval-ms:15000}", initialDelay = 3000)
    public void runExchangeSmartMoneyCycle() {
        log.info("Starting Betfair Exchange liquidity & Smart Money detection cycle...");
        try {
            int targetThreshold = betfairConfig.getExchange() != null ? betfairConfig.getExchange().getMinMatchesThreshold() : 500;
            List<BetfairMarketBook> marketBooks = exchangeCrawler.crawlExchangeMarkets(targetThreshold);

            int totalAlerts = 0;
            int totalSynced = 0;

            for (BetfairMarketBook market : marketBooks) {
                if (market == null || market.getMarketId() == null) continue;

                // 1. Detect Smart Money / volume surges
                List<SmartMoneyAlert> alerts = smartMoneyDetector.detectAnomalies(market);
                for (SmartMoneyAlert alert : alerts) {
                    smartMoneyKafkaPublisher.publishAlert(alert);
                    totalAlerts++;
                }

                // 2. Synchronize market and factors to match_factor and match_cache
                try {
                    syncToMatchCacheAndFactors(market);
                    totalSynced++;
                } catch (Exception e) {
                    log.debug("Failed to sync exchange market {}: {}", market.getMarketId(), e.getMessage());
                }
            }

            log.info("Betfair Exchange cycle finished: {} markets processed, {} smart money alerts dispatched, {} synchronized to DB",
                    marketBooks.size(), totalAlerts, totalSynced);

        } catch (Exception e) {
            log.error("Unexpected error during Betfair Exchange cycle: {}", e.getMessage(), e);
        }
    }

    /**
     * Maps Betfair Exchange runners and volume to MatchCache and MatchFactor entities.
     */
    public void syncToMatchCacheAndFactors(BetfairMarketBook market) {
        String eventId = market.getEventId();
        String sport = market.getSport() != null ? market.getSport() : "Soccer";
        String league = market.getLeague() != null ? market.getLeague() : "Exchange Market";

        String team1 = "Home";
        String team2 = "Away";
        if (market.getMatchName() != null && market.getMatchName().contains(" vs ")) {
            String[] parts = market.getMatchName().split(" vs ", 2);
            team1 = parts[0].trim();
            team2 = parts[1].trim();
        }

        MatchCache match = new MatchCache();
        match.setBookmaker("betfair");
        match.setExternalId(eventId);
        match.setSportName(sport);
        match.setLeagueName(league);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setStartTime(market.getEventDate() != null ? market.getEventDate() : System.currentTimeMillis() + 7200000L);
        match.setIsLive(market.isLive());
        match.setEventUrl("https://www.betfair.com/exchange/plus/football/market/" + market.getMarketId());
        match.setStatus(MatchCache.Status.PROCESSED);
        match.setUpdatedAt(LocalDateTime.now());

        // Build MatchFactor list (Odds prices and Traded Volumes)
        List<MatchFactor> factors = new ArrayList<>();
        if (market.getRunners() != null) {
            for (BetfairRunnerBook runner : market.getRunners()) {
                if (runner == null || runner.getSelectionId() == null) continue;

                // Price factor
                MatchFactor priceFactor = new MatchFactor();
                priceFactor.setFactorId(runner.getSelectionId());
                priceFactor.setName(runner.getRunnerName() != null ? runner.getRunnerName() : "Runner_" + runner.getSelectionId());
                priceFactor.setValue(runner.getBestBackPrice() != null ? runner.getBestBackPrice() : 1.0);
                priceFactor.setMatch(match);
                factors.add(priceFactor);

                // Traded volume factor
                if (runner.getTotalMatched() != null && runner.getTotalMatched() > 0.0) {
                    MatchFactor volFactor = new MatchFactor();
                    volFactor.setFactorId("VOL_" + runner.getSelectionId());
                    volFactor.setName("VOLUME_" + runner.getRunnerName());
                    volFactor.setValue(runner.getTotalMatched());
                    volFactor.setMatch(match);
                    factors.add(volFactor);
                }
            }
        }

        String serializedPayload;
        try {
            serializedPayload = objectMapper.writeValueAsString(market);
        } catch (Exception e) {
            serializedPayload = "{\"marketId\":\"" + market.getMarketId() + "\"}";
        }

        persistenceService.saveOrUpdateMatchMetadata(match, serializedPayload, factors);
    }
}
