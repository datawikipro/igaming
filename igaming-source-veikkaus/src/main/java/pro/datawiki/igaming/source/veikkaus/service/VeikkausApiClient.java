package pro.datawiki.igaming.source.veikkaus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.veikkaus.config.VeikkausConfig;
import pro.datawiki.igaming.source.veikkaus.dto.VeikkausEventDto;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VeikkausApiClient {

    private final VeikkausConfig config;
    private final WebClient.Builder webClientBuilder;

    public List<VeikkausEventDto> fetchActiveEvents() {
        try {
            WebClient client = webClientBuilder.baseUrl(config.getBaseUrl()).build();
            // In dev mode/fallback returns empty or parsed feed
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("VeikkausApiClient fetch error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}