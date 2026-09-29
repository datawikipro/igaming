package pro.datawiki.igaming.vkdonut.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client for fetching Premium surebet signals from the igaming-portal API.
 *
 * Mirrors the portal client pattern from igaming-vip-bot.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortalSignalClient {

    private final RestTemplate restTemplate;

    @Value("${portal.api.url:http://igaming-portal.igaming-dev.svc.cluster.local:80}")
    private String portalApiUrl;

    /**
     * Fetch the latest premium surebet signals from the portal.
     *
     * @return List of signal maps. Empty list on error or no data.
     */
    public List<Map<String, Object>> fetchPremiumSignals() {
        String url = portalApiUrl + "/api/v1/surebets/premium?limit=5";
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("PortalSignalClient: failed to fetch signals from {}: {}", url, e.getMessage());
        }
        return Collections.emptyList();
    }
}
