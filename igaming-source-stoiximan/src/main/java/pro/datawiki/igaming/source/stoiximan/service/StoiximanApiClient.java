package pro.datawiki.igaming.source.stoiximan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventDetailsResponse;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEventsResponse;
import pro.datawiki.igaming.source.stoiximan.config.StoiximanConfig;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StoiximanApiClient {

    private final RestTemplate restTemplate;
    private final List<String> brandPool = new ArrayList<>();
    private final StoiximanConfig config;
    private final ObjectMapper objectMapper;
    private volatile int stickyBrandIdx = 0;

    public StoiximanApiClient(@Qualifier("stoiximanRestTemplate") RestTemplate restTemplate,
                              StoiximanConfig config,
                              ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.objectMapper = objectMapper;

        String configuredBrand = config.getApi().getBrand();
        if (configuredBrand != null && !configuredBrand.isBlank()) {
            brandPool.add(configuredBrand);
        }
        for (String b : new String[]{"kambi", "paf", "svenskaspel", "atg", "rsiuspa"}) {
            if (!brandPool.contains(b)) {
                brandPool.add(b);
            }
        }

        String hostEnv = System.getenv("HOSTNAME");
        int seed = hostEnv != null ? Math.abs(hostEnv.hashCode()) : 0;
        this.stickyBrandIdx = seed % brandPool.size();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Encoding", "gzip, deflate");
        headers.set("Origin", "https://www.stoiximan.gr");
        headers.set("Referer", "https://www.stoiximan.gr/");
        return headers;
    }

    public KambiEventsResponse getSportEvents(String sportSlug) {
        int brandCount = brandPool.size();
        int maxAttempts = Math.min(3, brandCount);

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int bIdx = (stickyBrandIdx + attempt) % brandCount;
            String brand = brandPool.get(bIdx);

            String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                    .pathSegment(brand, "listView", sportSlug + ".json")
                    .queryParam("lang", config.getApi().getLocale())
                    .queryParam("market", config.getApi().getMarket())
                    .toUriString();

            try {
                HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    stickyBrandIdx = bIdx;
                    return objectMapper.readValue(response.getBody(), KambiEventsResponse.class);
                }
            } catch (Exception e) {
                log.debug("Brand '{}' failed for sport '{}': {}", brand, sportSlug, e.getMessage());
            }
        }
        log.warn("All attempts failed for sport '{}'", sportSlug);
        return null;
    }

    public KambiEventDetailsResponse getEventDetails(Long eventId) {
        String brand = brandPool.get(stickyBrandIdx % brandPool.size());
        String url = UriComponentsBuilder.fromHttpUrl(config.getApi().getBaseUrl())
                .pathSegment(brand, "betoffer", "event", String.valueOf(eventId) + ".json")
                .queryParam("lang", config.getApi().getLocale())
                .queryParam("market", config.getApi().getMarket())
                .toUriString();

        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), KambiEventDetailsResponse.class);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch Stoiximan betOffers for event {}: {}", eventId, e.getMessage());
        }
        return null;
    }
}
