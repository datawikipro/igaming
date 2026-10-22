package pro.datawiki.igaming.source.fanduel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.MatchPersistenceService;
import pro.datawiki.igaming.source.fanduel.config.FanDuelConfig;
import pro.datawiki.igaming.source.fanduel.dto.FanDuelEventGroupResponse;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FanDuelApiClient {

    private final ObjectMapper objectMapper;
    private final FanDuelOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;
    private final MatchPersistenceService persistenceService;
    private final FanDuelConfig config;

    private final Map<String, String> localStateHashCache = new ConcurrentHashMap<>();
    private HttpClient httpClient;

    private HttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        String proxyHost = System.getProperty("http.proxyHost");
        if (proxyHost == null || proxyHost.isBlank()) {
            proxyHost = System.getenv("HTTP_PROXY_HOST");
        }
        if (proxyHost == null || proxyHost.isBlank()) {
            proxyHost = "purevpn-us-direct.proxy.svc.cluster.local";
        }
        String proxyPortStr = System.getProperty("http.proxyPort");
        if (proxyPortStr == null || proxyPortStr.isBlank()) {
            proxyPortStr = System.getenv("HTTP_PROXY_PORT");
        }
        int proxyPort = 3128;
        if (proxyPortStr != null && !proxyPortStr.isBlank()) {
            try {
                proxyPort = Integer.parseInt(proxyPortStr.trim());
            } catch (Exception ignored) {}
        }

        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL);

        if (proxyHost != null && !proxyHost.isBlank()) {
            log.info("Configuring FanDuel HttpClient with proxy: {}:{}", proxyHost, proxyPort);
            clientBuilder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
        }

        httpClient = clientBuilder.build();
        return httpClient;
    }

    /**
     * Fetches and processes odds for a sport by its eventTypeId and its discovered competitions.
     */
    public int fetchSport(long eventTypeId, String sportName) {
        String url = String.format("%s/api/content-managed-page?page=SPORT&eventTypeId=%d&_ak=%s",
                config.getApi().getBaseUrl(), eventTypeId, config.getApi().getApiKey());
        FanDuelEventGroupResponse mainResp = fetchRawResponse(url);
        int totalSaved = processResponse(mainResp, sportName);

        if (mainResp != null && mainResp.getAttachments() != null) {
            if (mainResp.getAttachments().getCompetitions() != null) {
                for (FanDuelEventGroupResponse.FanDuelCompetition comp : mainResp.getAttachments().getCompetitions().values()) {
                    if (comp.getCompetitionId() == null) continue;
                    String compUrl = String.format("%s/api/content-managed-page?page=SPORT&eventTypeId=%d&competitionId=%d&_ak=%s",
                            config.getApi().getBaseUrl(), eventTypeId, comp.getCompetitionId(), config.getApi().getApiKey());
                    FanDuelEventGroupResponse compResp = fetchRawResponse(compUrl);
                    totalSaved += processResponse(compResp, sportName, comp.getName());
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            if (mainResp.getAttachments().getEventGroups() != null) {
                for (FanDuelEventGroupResponse.FanDuelEventGroup eg : mainResp.getAttachments().getEventGroups().values()) {
                    if (eg.getEventGroupId() == null) continue;
                    String egUrl = String.format("%s/api/content-managed-page?page=SPORT&eventTypeId=%d&eventGroupId=%d&_ak=%s",
                            config.getApi().getBaseUrl(), eventTypeId, eg.getEventGroupId(), config.getApi().getApiKey());
                    FanDuelEventGroupResponse egResp = fetchRawResponse(egUrl);
                    totalSaved += processResponse(egResp, sportName, eg.getName());
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return totalSaved;
    }

    public int fetchCompetition(long eventTypeId, long competitionId, String sportName, String compName) {
        String compUrl = String.format("%s/api/content-managed-page?page=SPORT&eventTypeId=%d&competitionId=%d&_ak=%s",
                config.getApi().getBaseUrl(), eventTypeId, competitionId, config.getApi().getApiKey());
        FanDuelEventGroupResponse compResp = fetchRawResponse(compUrl);
        return processResponse(compResp, sportName, compName);
    }

    public int fetchEventGroup(long eventTypeId, long eventGroupId, String sportName, String groupName) {
        String groupUrl = String.format("%s/api/content-managed-page?page=SPORT&eventTypeId=%d&eventGroupId=%d&_ak=%s",
                config.getApi().getBaseUrl(), eventTypeId, eventGroupId, config.getApi().getApiKey());
        FanDuelEventGroupResponse groupResp = fetchRawResponse(groupUrl);
        return processResponse(groupResp, sportName, groupName);
    }

    /**
     * Fetches and processes odds for a custom page (e.g. "nfl", "nba", "nhl", "mlb", "ncaaf", "ncaab", "wnba").
     */
    public int fetchCustomPage(String customPageId, String sportName) {
        String url = String.format("%s/api/content-managed-page?page=CUSTOM&customPageId=%s&_ak=%s",
                config.getApi().getBaseUrl(), customPageId, config.getApi().getApiKey());
        FanDuelEventGroupResponse resp = fetchRawResponse(url);
        return processResponse(resp, sportName, customPageId.toUpperCase());
    }

    /**
     * Fetches and processes odds for the featured FanDuel homepage (live / highlighted events across sports).
     */
    public int fetchHomepage() {
        String url = String.format("%s/api/content-managed-page?page=HOMEPAGE&_ak=%s",
                config.getApi().getBaseUrl(), config.getApi().getApiKey());
        FanDuelEventGroupResponse resp = fetchRawResponse(url);
        return processResponse(resp, "Featured", "Featured");
    }

    private FanDuelEventGroupResponse fetchRawResponse(String url) {
        log.info("Fetching FanDuel API: {}", url);
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                        .header("Accept", "application/json")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .header("Accept-Encoding", "gzip, deflate")
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();

                HttpResponse<byte[]> httpResponse = getHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

                if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300 || httpResponse.body() == null || httpResponse.body().length == 0) {
                    log.warn("FanDuel API returned status: {} for {} (attempt {}/3)", httpResponse.statusCode(), url, attempt);
                    Thread.sleep(1000);
                    continue;
                }

                byte[] body = httpResponse.body();
                if (body.length > 2 && body[0] == (byte) 0x1f && body[1] == (byte) 0x8b) {
                    try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(body))) {
                        body = gis.readAllBytes();
                    }
                }

                return objectMapper.readValue(body, FanDuelEventGroupResponse.class);

            } catch (Exception e) {
                log.warn("Error executing FanDuel fetch {} (attempt {}/3): {}", url, attempt, e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
        return null;
    }

    private int processResponse(FanDuelEventGroupResponse response, String defaultSportName) {
        return processResponse(response, defaultSportName, defaultSportName);
    }

    private int processResponse(FanDuelEventGroupResponse response, String defaultSportName, String defaultLeagueName) {
        if (response == null || response.getAttachments() == null || response.getAttachments().getEvents() == null || response.getAttachments().getEvents().isEmpty()) {
            return 0;
        }

        int pushed = 0;
        int skipped = 0;
        int saved = 0;

        for (FanDuelEventGroupResponse.FanDuelEvent event : response.getAttachments().getEvents().values()) {
            try {
                String leagueName = (defaultLeagueName != null && !defaultLeagueName.isBlank()) ? defaultLeagueName : defaultSportName;
                if (response.getAttachments() != null) {
                    if (event.getEventGroupId() != null && response.getAttachments().getEventGroups() != null) {
                        FanDuelEventGroupResponse.FanDuelEventGroup eg = response.getAttachments().getEventGroups().get(String.valueOf(event.getEventGroupId()));
                        if (eg != null && eg.getName() != null && !eg.getName().isBlank()) {
                            leagueName = eg.getName();
                        }
                    }
                }
                OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(event, response, defaultSportName, leagueName);
                if (request == null || request.getTeam1() == null || request.getTeam2() == null) {
                    continue;
                }

                String eventId = String.valueOf(event.getEventId());

                MatchCache matchCache = new MatchCache();
                matchCache.setBookmaker("fanduel");
                matchCache.setExternalId(eventId);
                matchCache.setSportName(defaultSportName);
                matchCache.setLeagueName(request.getLeagueName());
                matchCache.setTeam1(request.getTeam1());
                matchCache.setTeam2(request.getTeam2());
                matchCache.setIsLive(false);
                matchCache.setStartTime(request.getStartTime());
                matchCache.setEventUrl(request.getEventUrl());

                String serializedPayload = objectMapper.writeValueAsString(request);
                String currentHash = persistenceService.computeHash(serializedPayload);

                persistenceService.saveOrUpdateMatchMetadata(matchCache, serializedPayload);
                saved++;

                if (request.getOdds() != null && !request.getOdds().isEmpty()) {
                    String cachedHash = localStateHashCache.get(eventId);
                    if (cachedHash == null || !cachedHash.equals(currentHash)) {
                        aggregatorClient.pushOddsUpdate(request);
                        localStateHashCache.put(eventId, currentHash);
                        pushed++;
                    } else {
                        aggregatorClient.reportUnchangedOdds("fanduel", eventId);
                        skipped++;
                    }
                }
            } catch (Exception ex) {
                log.error("Error processing FanDuel event {}: {}", event.getEventId(), ex.getMessage());
            }
        }

        if (saved > 0) {
            log.info("FanDuel {}: saved={}, pushed={}, unchanged={}", defaultSportName, saved, pushed, skipped);
        }
        return saved;
    }
}

