package pro.datawiki.igaming.source.unibet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.datawiki.igaming.source.unibet.config.UnibetConfig;
import pro.datawiki.igaming.source.unibet.dto.kambi.KambiEventsResponse;
import pro.datawiki.igaming.source.unibet.dto.kambi.KambiEventDetailsResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class UnibetApiClient {

    private final RestTemplate restTemplate;
    private final UnibetConfig unibetConfig;

    public KambiEventsResponse getEvents() {
        String url = UriComponentsBuilder.fromHttpUrl(unibetConfig.getApi().getBaseUrl() + "/" + unibetConfig.getApi().getBrand() + "/listView/all/all/all/all.json")
                .queryParam("lang", unibetConfig.getApi().getLocale())
                .queryParam("market", unibetConfig.getApi().getMarket())
                .toUriString();

        try {
            log.debug("Fetching Unibet (Kambi) events from {}", url);
            ResponseEntity<KambiEventsResponse> response = restTemplate.getForEntity(url, KambiEventsResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch Unibet events: {}", e.getMessage());
            return null;
        }
    }

    public KambiEventDetailsResponse getEventDetails(Long eventId) {
        String url = UriComponentsBuilder.fromHttpUrl(unibetConfig.getApi().getBaseUrl() + "/" + unibetConfig.getApi().getBrand() + "/betoffer/event/" + eventId + ".json")
                .queryParam("lang", unibetConfig.getApi().getLocale())
                .toUriString();

        try {
            log.debug("Fetching Unibet (Kambi) event details for {} from {}", eventId, url);
            ResponseEntity<KambiEventDetailsResponse> response = restTemplate.getForEntity(url, KambiEventDetailsResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch Unibet event details for event {}: {}", eventId, e.getMessage());
            return null;
        }
    }
}
