package pro.datawiki.igaming.source.bovada.service;

import com.fasterxml.jackson.core.type.TypeReference;
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
import pro.datawiki.igaming.source.bovada.config.BovadaConfig;
import pro.datawiki.igaming.source.bovada.dto.BovadaEventGroupDto;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class BovadaApiClient {

    private final RestTemplate restTemplate;
    private final BovadaConfig bovadaConfig;
    private final ObjectMapper objectMapper;

    public BovadaApiClient(@Qualifier("bovadaRestTemplate") RestTemplate restTemplate,
                           BovadaConfig bovadaConfig,
                           ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.bovadaConfig = bovadaConfig;
        this.objectMapper = objectMapper;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        // Bovada Cloudflare edge mandates Accept-Language: en-US,en;q=0.9
        headers.set("Accept-Language", "en-US,en;q=0.9");
        headers.set("Origin", "https://www.bovada.lv");
        headers.set("Referer", "https://www.bovada.lv/");
        headers.set("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"");
        headers.set("Sec-Ch-Ua-Mobile", "?0");
        headers.set("Sec-Ch-Ua-Platform", "\"Windows\"");
        headers.set("Sec-Fetch-Dest", "empty");
        headers.set("Sec-Fetch-Mode", "cors");
        headers.set("Sec-Fetch-Site", "same-origin");
        return headers;
    }

    public List<BovadaEventGroupDto> getSportEvents(String sportSlug) {
        String baseUrl = bovadaConfig.getApi().getBaseUrl();
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/" + sportSlug)
                .queryParam("lang", "en")
                .toUriString();

        try {
            log.debug("Fetching Bovada events for sport '{}' from {}", sportSlug, url);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), new TypeReference<List<BovadaEventGroupDto>>() {});
            } else {
                log.warn("Bovada API returned non-2xx status {} for sport '{}'", response.getStatusCode(), sportSlug);
            }
        } catch (Exception e) {
            log.error("Error fetching Bovada events for sport '{}': {}", sportSlug, e.getMessage(), e);
        }
        return Collections.emptyList();
    }
}
