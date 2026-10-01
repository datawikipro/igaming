package pro.datawiki.igaming.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.analytics.dto.BookmakerThresholdDto;
import pro.datawiki.igaming.analytics.dto.CrawlerProbeResultDto;
import pro.datawiki.igaming.analytics.dto.PipelineStatsDto;
import pro.datawiki.igaming.dto.BookmakerFleetStatsDto;
import pro.datawiki.igaming.dto.DiagnosticsStatsDto;
import pro.datawiki.igaming.dto.FleetOverviewDto;
import pro.datawiki.igaming.dto.LoaderDelayDto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing Crawler Operations, evaluating compliance against Golden Rule #8
 * (threshold >= 500 active matches), and aggregating end-to-end ingestion pipeline health.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerOpsService {

    public static final long GOLDEN_RULE_8_THRESHOLD = 500L;

    private final RestTemplate restTemplate;

    @Value("${aggregator.stats.url:http://igaming-aggregator/api/diagnostics/stats}")
    private String aggregatorStatsUrl;

    @Value("${aggregator.fleet.url:http://igaming-aggregator/api/bookmakers/fleet}")
    private String aggregatorFleetUrl;

    @Value("${aggregator.fleet.refresh.url:http://igaming-aggregator/api/bookmakers/fleet/refresh}")
    private String aggregatorFleetRefreshUrl;

    @Value("${aggregator.delays.url:http://igaming-aggregator/api/diagnostics/loader-delays}")
    private String aggregatorDelaysUrl;

    /**
     * Retrieve aggregated operational metrics for the Ingestion Pipeline.
     */
    public PipelineStatsDto getPipelineStats() {
        DiagnosticsStatsDto diag = null;
        boolean diagAvailable = false;
        try {
            diag = restTemplate.getForObject(aggregatorStatsUrl, DiagnosticsStatsDto.class);
            diagAvailable = (diag != null);
        } catch (Exception e) {
            log.warn("Cannot reach aggregator stats at {}: {}", aggregatorStatsUrl, e.getMessage());
        }

        List<LoaderDelayDto> loaderDelays = List.of();
        try {
            LoaderDelayDto[] array = restTemplate.getForObject(aggregatorDelaysUrl, LoaderDelayDto[].class);
            if (array != null) {
                loaderDelays = Arrays.asList(array);
            }
        } catch (Exception e) {
            log.warn("Cannot reach aggregator loader delays at {}: {}", aggregatorDelaysUrl, e.getMessage());
        }

        FleetOverviewDto fleet = getFleetOverview();
        List<BookmakerThresholdDto> thresholds = (fleet != null && fleet.getBookmakers() != null)
                ? fleet.getBookmakers().stream().map(this::toThresholdDto).collect(Collectors.toList())
                : List.of();

        int total = thresholds.size();
        int online = (int) thresholds.stream().filter(t -> Boolean.TRUE.equals(t.getIsOnline())).count();
        int compliant = (int) thresholds.stream().filter(t -> "COMPLIANT".equals(t.getComplianceStatus())).count();
        int degraded = (int) thresholds.stream().filter(t -> "DEGRADED".equals(t.getComplianceStatus())).count();
        int defect = (int) thresholds.stream().filter(t -> "DEFECT".equals(t.getComplianceStatus())).count();

        double complianceRate = total > 0
                ? Math.round((compliant * 100.0 / total) * 10.0) / 10.0
                : 0.0;

        long matchesCount = diag != null && diag.getMatches() != null ? diag.getMatches() : (fleet != null ? fleet.getTotalActiveMatches() : 0L);
        long teamsCount = diag != null && diag.getTeams() != null ? diag.getTeams() : 0L;
        long normReq = diag != null && diag.getNormalizationRequests() != null ? diag.getNormalizationRequests() : 0L;
        long pendingNorm = diag != null && diag.getPendingNormalization() != null ? diag.getPendingNormalization() : 0L;
        long unrecOdds = diag != null && diag.getUnrecognizedOdds() != null ? diag.getUnrecognizedOdds() : 0L;
        long totalOdds = fleet != null ? fleet.getTotalActiveOdds() : 0L;

        String pipelineStatus;
        if (!diagAvailable && (fleet == null || total == 0)) {
            pipelineStatus = "DOWN";
        } else if (defect > 0 || complianceRate < 70.0 || pendingNorm > 500) {
            pipelineStatus = "DEGRADED";
        } else {
            pipelineStatus = "HEALTHY";
        }

        return PipelineStatsDto.builder()
                .matches(matchesCount)
                .teams(teamsCount)
                .normalizationRequests(normReq)
                .pendingNormalization(pendingNorm)
                .unrecognizedOdds(unrecOdds)
                .totalActiveOdds(totalOdds)
                .totalBookmakers(total)
                .onlineBookmakers(online)
                .compliantBookmakers(compliant)
                .degradedBookmakers(degraded)
                .defectBookmakers(defect)
                .complianceRate(complianceRate)
                .pipelineStatus(pipelineStatus)
                .kafkaTopic("odds.updates")
                .kafkaStatus(diagAvailable ? "STREAMING" : "CONNECTED")
                .timestamp(System.currentTimeMillis())
                .loaderDelays(loaderDelays)
                .build();
    }

    /**
     * Retrieve all bookmakers evaluated against Golden Rule #8 (>= 500 active matches).
     */
    public List<BookmakerThresholdDto> getThresholds() {
        FleetOverviewDto fleet = getFleetOverview();
        if (fleet == null || fleet.getBookmakers() == null) {
            return List.of();
        }
        return fleet.getBookmakers().stream()
                .map(this::toThresholdDto)
                .collect(Collectors.toList());
    }

    /**
     * Map a single crawler fleet item to a Golden Rule #8 threshold compliance assessment.
     */
    public BookmakerThresholdDto toThresholdDto(BookmakerFleetStatsDto b) {
        long matches = b.getActiveMatchesCount() != null ? b.getActiveMatchesCount() : 0L;
        long odds = b.getActiveOddsCount() != null ? b.getActiveOddsCount() : 0L;
        double delay = b.getDelayMinutes() != null ? b.getDelayMinutes() : 0.0;
        boolean online = Boolean.TRUE.equals(b.getIsOnline());

        // Golden Rule #8 progress: percentage towards 500 matches goal (capped at 100.0)
        double progress = Math.min(100.0, Math.round((matches * 100.0 / GOLDEN_RULE_8_THRESHOLD) * 10.0) / 10.0);

        // Compliance status: COMPLIANT (>=500), DEGRADED (1-499), DEFECT (0)
        String complianceStatus;
        if (matches >= GOLDEN_RULE_8_THRESHOLD) {
            complianceStatus = "COMPLIANT";
        } else if (matches > 0) {
            complianceStatus = "DEGRADED";
        } else {
            complianceStatus = "DEFECT";
        }

        // Freshness status: FRESH (<1m), NORMAL (1-5m), DELAYED (5-15m), STALE (>15m)
        String freshnessStatus;
        if (delay < 1.0) {
            freshnessStatus = "FRESH";
        } else if (delay <= 5.0) {
            freshnessStatus = "NORMAL";
        } else if (delay <= 15.0) {
            freshnessStatus = "DELAYED";
        } else {
            freshnessStatus = "STALE";
        }

        // Health status: HEALTHY, DEGRADED, OFFLINE
        String healthStatus;
        if (!online) {
            healthStatus = "OFFLINE";
        } else if ("DEFECT".equals(complianceStatus)) {
            healthStatus = "DEGRADED";
        } else if ("DEGRADED".equals(complianceStatus) || "DELAYED".equals(freshnessStatus) || "STALE".equals(freshnessStatus)) {
            healthStatus = "DEGRADED";
        } else {
            healthStatus = "HEALTHY";
        }

        String healthMessage = b.getHealthMessage();
        if (healthMessage == null || healthMessage.isBlank()) {
            if ("COMPLIANT".equals(complianceStatus)) {
                healthMessage = "Compliant with Golden Rule #8 (" + matches + " >= 500 matches)";
            } else if ("DEGRADED".equals(complianceStatus)) {
                healthMessage = "Degraded: " + matches + "/500 matches (below Golden Rule #8 threshold)";
            } else if (!online) {
                healthMessage = "Offline: No heartbeat received within 5 minutes";
            } else {
                healthMessage = "CRITICAL DEFECT: 0 active matches in line (Golden Rule #8 violation)";
            }
        }

        return BookmakerThresholdDto.builder()
                .id(b.getId())
                .name(b.getName())
                .logoEmoji(b.getLogoEmoji() != null ? b.getLogoEmoji() : "🌐")
                .engine(b.getEngine() != null ? b.getEngine() : "BASIC")
                .proxyRoute(b.getProxyRoute() != null ? b.getProxyRoute() : "DIRECT")
                .regions(b.getRegions() != null ? b.getRegions() : Set.of())
                .isOnline(online)
                .lastSeen(b.getLastSeen())
                .matchesCount(matches)
                .targetThreshold(GOLDEN_RULE_8_THRESHOLD)
                .thresholdProgress(progress)
                .complianceStatus(complianceStatus)
                .oddsCount(odds)
                .delayMinutes(Math.round(delay * 10.0) / 10.0)
                .freshnessStatus(freshnessStatus)
                .healthStatus(healthStatus)
                .healthMessage(healthMessage)
                .sportBreakdown(b.getSportBreakdown() != null ? b.getSportBreakdown() : Map.of())
                .topLeagues(b.getTopLeagues() != null ? b.getTopLeagues() : List.of())
                .build();
    }

    /**
     * Retrieve live fleet overview from aggregator.
     */
    public FleetOverviewDto getFleetOverview() {
        try {
            FleetOverviewDto fleet = restTemplate.getForObject(aggregatorFleetUrl, FleetOverviewDto.class);
            if (fleet != null && fleet.getBookmakers() != null && !fleet.getBookmakers().isEmpty()) {
                return fleet;
            }
        } catch (Exception e) {
            log.warn("Cannot reach aggregator fleet at {}: {}", aggregatorFleetUrl, e.getMessage());
        }
        return createFallbackFleetOverview();
    }

    /**
     * Force refresh aggregator fleet cache and retrieve updated overview.
     */
    public FleetOverviewDto refreshFleetOverview() {
        try {
            FleetOverviewDto fleet = restTemplate.postForObject(aggregatorFleetRefreshUrl, null, FleetOverviewDto.class);
            if (fleet != null && fleet.getBookmakers() != null && !fleet.getBookmakers().isEmpty()) {
                return fleet;
            }
        } catch (Exception e) {
            log.warn("Cannot refresh aggregator fleet at {}: {}", aggregatorFleetRefreshUrl, e.getMessage());
        }
        return getFleetOverview();
    }

    /**
     * Probe a single crawler for immediate liveness, latency, and threshold assessment.
     */
    public CrawlerProbeResultDto probeCrawler(String id) {
        long start = System.currentTimeMillis();
        FleetOverviewDto fleet = getFleetOverview();
        long elapsed = Math.max(1L, System.currentTimeMillis() - start);

        BookmakerFleetStatsDto found = null;
        if (fleet != null && fleet.getBookmakers() != null) {
            found = fleet.getBookmakers().stream()
                    .filter(b -> id.equalsIgnoreCase(b.getId()))
                    .findFirst()
                    .orElse(null);
        }

        if (found == null) {
            return CrawlerProbeResultDto.builder()
                    .bookmakerId(id)
                    .status("FAILED")
                    .responseTimeMs(elapsed)
                    .timestamp(System.currentTimeMillis())
                    .currentMatches(0L)
                    .currentOdds(0L)
                    .message("Unknown bookmaker [" + id + "]: not registered in crawler fleet")
                    .build();
        }

        BookmakerThresholdDto threshold = toThresholdDto(found);
        String status;
        String message;

        if ("COMPLIANT".equals(threshold.getComplianceStatus()) && Boolean.TRUE.equals(threshold.getIsOnline())) {
            status = "SUCCESS";
            message = String.format("Crawler [%s] responded in %dms. Active matches: %d, odds: %d. Golden Rule #8 COMPLIANT.",
                    id, elapsed, threshold.getMatchesCount(), threshold.getOddsCount());
        } else if ("DEGRADED".equals(threshold.getComplianceStatus()) || Boolean.TRUE.equals(threshold.getIsOnline())) {
            status = "DEGRADED";
            message = String.format("Crawler [%s] responded in %dms. Active matches: %d/500 (DEGRADED: below Golden Rule #8 threshold).",
                    id, elapsed, threshold.getMatchesCount());
        } else {
            status = "FAILED";
            message = String.format("Crawler [%s] probe FAILED. Status: %s. Active matches: %d (CRITICAL DEFECT: 0 matches in line).",
                    id, threshold.getHealthStatus(), threshold.getMatchesCount());
        }

        return CrawlerProbeResultDto.builder()
                .bookmakerId(id)
                .status(status)
                .responseTimeMs(elapsed)
                .timestamp(System.currentTimeMillis())
                .currentMatches(threshold.getMatchesCount())
                .currentOdds(threshold.getOddsCount())
                .message(message)
                .build();
    }

    /**
     * Fallback fleet overview when aggregator is unreachable or starting up.
     */
    private FleetOverviewDto createFallbackFleetOverview() {
        List<BookmakerFleetStatsDto> list = new ArrayList<>();
        list.add(createFallbackItem("fonbet", "Фонбет", "🔴", "DIRECT", Set.of("RU"), "Proprietary REST / WS"));
        list.add(createFallbackItem("winline", "Winline", "🟠", "DIRECT", Set.of("RU"), "Proprietary REST / WS"));
        list.add(createFallbackItem("betcity", "Бетсити", "🔵", "DIRECT", Set.of("RU"), "Proprietary REST / WS"));
        list.add(createFallbackItem("baltbet", "Балтбет", "🟢", "DIRECT", Set.of("RU"), "Proprietary REST / WS"));
        list.add(createFallbackItem("olimpbet", "Олимпбет", "🔴", "DIRECT", Set.of("RU"), "Proprietary REST / WS"));
        list.add(createFallbackItem("tennisi", "Тенниси", "🟡", "DIRECT", Set.of("RU"), "Proprietary REST / WS"));
        list.add(createFallbackItem("leon", "Леон", "⚫", "DIRECT", Set.of("RU"), "Proprietary REST / WS"));
        list.add(createFallbackItem("ligastavok", "Лига Ставок", "🟢", "DIRECT", Set.of("RU"), "Proprietary REST / WS"));
        list.add(createFallbackItem("betboom", "BetBoom", "🟡", "DIRECT", Set.of("RU"), "Proprietary REST / WS"));
        list.add(createFallbackItem("zenit", "Зенит", "🔵", "DIRECT", Set.of("RU"), "Proprietary REST / WS"));
        list.add(createFallbackItem("marathonbet", "Марафон", "🔵", "DIRECT", Set.of("RU", "CIS"), "Proprietary REST / WS"));
        list.add(createFallbackItem("pinnacle", "Pinnacle", "🟠", "OUTLINE_VPN_EU_NL", Set.of("GLOBAL"), "Proprietary REST (Sharp)"));
        list.add(createFallbackItem("sbobet", "Sbobet", "🔵", "OUTLINE_VPN_EU_NL", Set.of("ASIA", "GLOBAL"), "Asian Handicap Platform"));
        list.add(createFallbackItem("bet365", "Bet365", "🟢", "OUTLINE_VPN_EU_NL", Set.of("EU", "GLOBAL"), "Proprietary WS / Push"));
        list.add(createFallbackItem("bwin", "Bwin", "🟡", "OUTLINE_VPN_EU_NL", Set.of("EU", "GLOBAL"), "Entain Platform"));
        list.add(createFallbackItem("betsson", "Betsson", "🟠", "OUTLINE_VPN_EU_NL", Set.of("EU", "GLOBAL"), "Techsson / Digitain"));
        list.add(createFallbackItem("unibet", "Unibet", "🟢", "OUTLINE_VPN_EU_NL", Set.of("EU", "GLOBAL"), "Kambi Engine"));
        list.add(createFallbackItem("888sport", "888sport", "🟠", "OUTLINE_VPN_EU_NL", Set.of("EU", "GLOBAL"), "Kambi / Proprietary"));
        list.add(createFallbackItem("draftkings", "DraftKings", "🟢", "OUTLINE_US", Set.of("US"), "DraftKings / SBTech"));
        list.add(createFallbackItem("fanduel", "FanDuel", "🔵", "OUTLINE_US", Set.of("US"), "Flutter / OpenBet"));
        list.add(createFallbackItem("betmgm", "BetMGM", "🟡", "OUTLINE_US", Set.of("US"), "Entain US Platform"));
        list.add(createFallbackItem("caesars", "Caesars", "🟢", "OUTLINE_US", Set.of("US"), "Caesars / William Hill"));
        list.add(createFallbackItem("betb2b", "BetB2B Main", "🔵", "OUTLINE_VPN_EU_NL", Set.of("CIS", "GLOBAL"), "BetB2B Engine"));
        list.add(createFallbackItem("1xbet", "1xBet", "🔵", "OUTLINE_VPN_EU_NL", Set.of("CIS", "GLOBAL"), "BetB2B Engine"));
        list.add(createFallbackItem("melbet", "Melbet", "🟡", "OUTLINE_VPN_EU_NL", Set.of("CIS", "GLOBAL"), "BetB2B Engine"));
        list.add(createFallbackItem("wplay", "Wplay", "🔵", "OUTLINE_VPN_EU_NL", Set.of("LATAM"), "Playtech LatAm"));
        list.add(createFallbackItem("smarkets", "Smarkets", "🟢", "OUTLINE_VPN_EU_NL", Set.of("GB", "EU"), "Smarkets Streaming API P2P"));
        list.add(createFallbackItem("betfair", "Betfair Exchange", "🟡", "OUTLINE_VPN_EU_NL", Set.of("GB", "EU"), "Betfair JSON-RPC P2P"));

        return FleetOverviewDto.builder()
                .totalBookmakers(list.size())
                .onlineBookmakers(0)
                .compliantBookmakers(0)
                .degradedBookmakers(0)
                .offlineBookmakers(list.size())
                .totalActiveMatches(0L)
                .totalActiveOdds(0L)
                .bookmakers(list)
                .generatedAt(System.currentTimeMillis())
                .build();
    }

    private BookmakerFleetStatsDto createFallbackItem(String id, String name, String logoEmoji,
                                                      String proxyRoute, Set<String> regions, String engine) {
        return BookmakerFleetStatsDto.builder()
                .id(id)
                .name(name)
                .logoEmoji(logoEmoji)
                .engine(engine)
                .proxyRoute(proxyRoute)
                .regions(regions)
                .isOnline(false)
                .lastSeen(null)
                .activeMatchesCount(0L)
                .activeOddsCount(0L)
                .delayMinutes(null)
                .healthStatus("OFFLINE")
                .healthMessage("Aggregator disconnected or starting up")
                .sportBreakdown(Map.of())
                .topLeagues(List.of())
                .build();
    }
}
