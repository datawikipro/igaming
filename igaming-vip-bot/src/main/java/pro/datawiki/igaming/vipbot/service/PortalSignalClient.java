package pro.datawiki.igaming.vipbot.service;

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
 * Fetches premium (VIP-grade) surebet signals from the igaming-portal API.
 *
 * This client calls the portal's internal surebet endpoint with a VIP service
 * token and retrieves signals with yield above the Premium threshold (>5.0%).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortalSignalClient {

    private final RestTemplate restTemplate;

    @Value("${portal.api.url:http://igaming-portal.igaming-dev.svc.cluster.local:80}")
    private String portalUrl;

    @Value("${vip.portal.service-token:}")
    private String serviceToken;

    /**
     * Fetch the latest premium surebets (yield > 5%) from the portal.
     *
     * @return List of signal maps (empty if unavailable or no signals)
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchPremiumSignals() {
        try {
            String url = portalUrl + "/api/v1/surebets?min_yield=5.0&limit=10&sort=yield_desc";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            if (serviceToken != null && !serviceToken.isBlank()) {
                headers.set("X-Service-Token", serviceToken);
            }

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Fetched {} premium signals from portal", response.getBody().size());
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch premium signals from portal: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
