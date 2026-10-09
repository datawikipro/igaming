package pro.datawiki.igaming.source.vbet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.vbet.config.VbetConfig;
import pro.datawiki.igaming.source.vbet.dto.VbetEvent;
import pro.datawiki.igaming.source.vbet.utils.VbetCryptoUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VbetApiClient {

    private final RestTemplate vbetRestTemplate;
    private final VbetConfig vbetConfig;
    private final ObjectMapper objectMapper;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        headers.set("Referer", "https://sport.vbet.com/");
        headers.set("Origin", "https://sport.vbet.com");
        return headers;
    }

    public List<VbetEvent> getPrematchEvents() {
        String url = vbetConfig.getBaseUrl() + "/prematch/gettopeventslist?stakeTypes=1&stakeTypes=702&stakeTypes=2&stakeTypes=3&stakeTypes=46&stakeTypes=992&langId=1&partnerId=" + vbetConfig.getPartnerId();
        return fetchEventsFromUrl(url);
    }

    public List<VbetEvent> getLiveEvents() {
        String url = vbetConfig.getBaseUrl() + "/live/geteventslist?stakeTypes=1&stakeTypes=702&stakeTypes=2&stakeTypes=3&stakeTypes=46&stakeTypes=992&langId=1&partnerId=" + vbetConfig.getPartnerId();
        return fetchEventsFromUrl(url);
    }

    public VbetEvent getEventDetails(Long eventId) {
        String url = vbetConfig.getBaseUrl() + "/event/" + eventId;
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = vbetRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String decrypted = VbetCryptoUtils.decrypt(response.getBody());
                if (decrypted != null && decrypted.startsWith("{")) {
                    return objectMapper.readValue(decrypted, VbetEvent.class);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch event details for {} from Vbet: {}", eventId, e.getMessage());
        }
        return null;
    }

    private List<VbetEvent> fetchEventsFromUrl(String url) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = vbetRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() != null) {
                String decrypted = VbetCryptoUtils.decrypt(response.getBody());
                if (decrypted != null) {
                    decrypted = decrypted.trim();
                    if (decrypted.startsWith("[")) {
                        return objectMapper.readValue(decrypted, new TypeReference<List<VbetEvent>>() {});
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed fetching events from Vbet URL {}: {}", url, e.getMessage());
        }
        return new ArrayList<>();
    }
}
