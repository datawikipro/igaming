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

    private synchronized HttpClient getHttpClient() {
        if (httpClient == null) {
            String proxyHost = System.getProperty("http.proxyHost", "100.83.113.50");
            String proxyPortStr = System.getProperty("http.proxyPort", "3128");
            int proxyPort = 3128;
            try {
                proxyPort = Integer.parseInt(proxyPortStr);
            } catch (Exception ignored) {}

            HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NORMAL);

            try {
                clientBuilder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
            } catch (Exception e) {
                log.warn("Could not set proxy on HttpClient: {}", e.getMessage());
            }

            httpClient = clientBuilder.build();
        }
        return httpClient;
    }

    /**
     * Fetches and processes odds for a sport by its eventTypeId.
     */
    public int fetchSport(long eventTypeId, String sportName) {
        String url = String.format("%s/api/content-managed-page?page=SPORT&eventTypeId=%d&_ak=%s",
                config.getApi().getBaseUrl(), eventTypeId, config.getApi().getApiKey());
        return executeFetch(url, sportName);
    }

    /**
     * Fetches and processes odds for a custom page (e.g. "nfl", "nba", "nhl", "mlb").
     */
    public int fetchCustomPage(String customPageId, String sportName) {
        String url = String.format("%s/api/content-managed-page?page=CUSTOM&customPageId=%s&_ak=%s",
                config.getApi().getBaseUrl(), customPageId, config.getApi().getApiKey());
        return executeFetch(url, sportName);
    }

    private int executeFetch(String url, String defaultSportName) {
        log.info("Fetching FanDuel API: {}", url);
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .header("Accept", "application/json")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            HttpResponse<byte[]> httpResponse = getHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300 || httpResponse.body() == null || httpResponse.body().length == 0) {
                log.warn("FanDuel API returned status: {} for {}", httpResponse.statusCode(), url);
                return 0;
            }

            byte[] body = httpResponse.body();
            if (body.length > 2 && body[0] == (byte) 0x1f && body[1] == (byte) 0x8b) {
                try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(body))) {
                    body = gis.readAllBytes();
                }
            }

            FanDuelEventGroupResponse response = objectMapper.readValue(body, FanDuelEventGroupResponse.class);

            if (response.getAttachments() == null || response.getAttachments().getEvents() == null || response.getAttachments().getEvents().isEmpty()) {
                log.info("No events in FanDuel response for {}", url);
                return 0;
            }

            int pushed = 0;
            int skipped = 0;
            int saved = 0;

            for (FanDuelEventGroupResponse.FanDuelEvent event : response.getAttachments().getEvents().values()) {
                try {
                    String leagueName = defaultSportName;
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

            log.info("FanDuel {}: saved={}, pushed={}, unchanged={}", defaultSportName, saved, pushed, skipped);
            return saved;

        } catch (Exception e) {
            log.error("Error executing FanDuel fetch {}: {}", url, e.getMessage());
            return 0;
        }
    }
}

