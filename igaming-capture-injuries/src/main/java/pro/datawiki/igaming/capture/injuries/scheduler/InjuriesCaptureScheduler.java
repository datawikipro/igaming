package pro.datawiki.igaming.capture.injuries.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.client.MdmIngestionClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.injury.crawler.InjuryCrawlerProvider;
import pro.datawiki.igaming.source.core.injury.model.MatchInjuryReport;
import pro.datawiki.igaming.source.core.injury.model.PlayerInjuryInfo;
import pro.datawiki.igaming.source.core.injury.publisher.InjuryKafkaPublisher;
import pro.datawiki.igaming.source.core.injury.service.InjuryFactorCalculator;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class InjuriesCaptureScheduler {

    private final List<InjuryCrawlerProvider> crawlerProviders;
    private final MdmIngestionClient mdmClient;
    private final InjuryKafkaPublisher kafkaPublisher;
    private final InjuryFactorCalculator factorCalculator;
    private final MatchPersistenceService persistenceService;
    private final MatchCacheRepository matchCacheRepository;

    @Value("${app.capture.injuries.enabled:true}")
    private boolean enabled;

    // In-memory cache of recent reports by match External ID or composite key
    private final Map<String, MatchInjuryReport> latestReports = new ConcurrentHashMap<>();
    private final AtomicLong lastCrawlTime = new AtomicLong(0);
    private final AtomicInteger totalInjuriesCaptured = new AtomicInteger(0);

    @Scheduled(
            fixedDelayString = "${app.capture.injuries.fixed-delay:300000}",
            initialDelayString = "${app.capture.injuries.initial-delay:15000}"
    )
    public void runCaptureCycle() {
        if (!enabled) {
            log.trace("[Injuries Capture Engine] Injuries capture cycle is disabled.");
            return;
        }

        log.info("[Injuries Capture Engine] Starting multi-source injury & suspension capture cycle across {} providers...",
                crawlerProviders.size());
        long start = System.currentTimeMillis();

        List<PlayerInjuryInfo> allCrawledInjuries = new ArrayList<>();

        for (InjuryCrawlerProvider provider : crawlerProviders) {
            try {
                log.info("[Injuries Capture Engine] Executing crawler: {}", provider.getSource());
                List<PlayerInjuryInfo> items = provider.fetchInjuries();
                if (items != null && !items.isEmpty()) {
                    allCrawledInjuries.addAll(items);
                    log.info("[Injuries Capture Engine] Provider '{}' retrieved {} injury records",
                            provider.getSource(), items.size());
                }
            } catch (Exception e) {
                log.error("[Injuries Capture Engine] Provider '{}' encountered error: {}",
                        provider.getSource(), e.getMessage(), e);
            }
        }

        totalInjuriesCaptured.set(allCrawledInjuries.size());
        lastCrawlTime.set(System.currentTimeMillis());

        if (allCrawledInjuries.isEmpty()) {
            log.info("[Injuries Capture Engine] No raw injury records retrieved in this cycle.");
            return;
        }

        // Group crawled injuries by canonical team name
        Map<String, List<PlayerInjuryInfo>> injuriesByTeam = new HashMap<>();
        for (PlayerInjuryInfo injury : allCrawledInjuries) {
            String teamKey = injury.getCanonicalTeamName() != null
                    ? injury.getCanonicalTeamName().toLowerCase(Locale.ROOT)
                    : (injury.getRawTeamName() != null ? injury.getRawTeamName().toLowerCase(Locale.ROOT) : "unknown");

            injuriesByTeam.computeIfAbsent(teamKey, k -> new ArrayList<>()).add(injury);
        }

        // Link with active upcoming pre-match fixtures from MatchCache
        try {
            List<MatchCache> activeMatches = matchCacheRepository.findTop500ByOrderByUpdatedAtDesc();
            int enrichedMatchesCount = 0;

            for (MatchCache match : activeMatches) {
                if (Boolean.TRUE.equals(match.getIsLive())) {
                    continue; // Injuries are primarily pre-match 24-48h intelligence
                }

                String homeTeam = match.getTeam1();
                String awayTeam = match.getTeam2();
                if (homeTeam == null || awayTeam == null) continue;

                List<PlayerInjuryInfo> homeInjuries = findInjuriesForTeam(homeTeam, injuriesByTeam);
                List<PlayerInjuryInfo> awayInjuries = findInjuriesForTeam(awayTeam, injuriesByTeam);

                if (!homeInjuries.isEmpty() || !awayInjuries.isEmpty()) {
                    MatchInjuryReport report = MatchInjuryReport.builder()
                            .matchExternalId(match.getExternalId())
                            .sport(match.getSportName() != null ? match.getSportName() : "football")
                            .leagueName(match.getLeagueName())
                            .homeTeam(homeTeam)
                            .awayTeam(awayTeam)
                            .matchStartTime(match.getStartTime())
                            .homeInjuries(homeInjuries)
                            .awayInjuries(awayInjuries)
                            .confidenceScore(0.95)
                            .generatedAt(LocalDateTime.now())
                            .build();

                    factorCalculator.calculateImpactMetrics(report);

                    // 1. Publish to Kafka topic sports.canonical.injuries
                    kafkaPublisher.publish(report);

                    // 2. Persist factor adjustments to match_factor & match_cache
                    persistenceService.applyInjuryReport(match, report);

                    latestReports.put(match.getExternalId(), report);
                    enrichedMatchesCount++;
                }
            }

            log.info("[Injuries Capture Engine] Cycle complete in {}ms. Parsed {} injuries, enriched {} matches.",
                    (System.currentTimeMillis() - start), allCrawledInjuries.size(), enrichedMatchesCount);
        } catch (Exception e) {
            log.error("[Injuries Capture Engine] Error matching injuries to fixtures: {}", e.getMessage(), e);
        }
    }

    private List<PlayerInjuryInfo> findInjuriesForTeam(String teamName, Map<String, List<PlayerInjuryInfo>> map) {
        String cleaned = teamName.toLowerCase(Locale.ROOT).trim();
        List<PlayerInjuryInfo> direct = map.get(cleaned);
        if (direct != null) return direct;

        for (Map.Entry<String, List<PlayerInjuryInfo>> entry : map.entrySet()) {
            if (cleaned.contains(entry.getKey()) || entry.getKey().contains(cleaned)) {
                return entry.getValue();
            }
        }
        return Collections.emptyList();
    }

    public Map<String, Object> getDiagnostics() {
        return Map.of(
                "totalInjuriesCaptured", totalInjuriesCaptured.get(),
                "lastCrawlTime", lastCrawlTime.get(),
                "cachedReportsCount", latestReports.size(),
                "providersCount", crawlerProviders.size(),
                "enabled", enabled
        );
    }

    public Optional<MatchInjuryReport> getReportForMatch(String externalId) {
        return Optional.ofNullable(latestReports.get(externalId));
    }
}
