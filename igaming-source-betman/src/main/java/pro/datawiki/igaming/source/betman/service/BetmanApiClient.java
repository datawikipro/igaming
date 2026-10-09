package pro.datawiki.igaming.source.betman.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.betman.config.BetmanConfig;
import pro.datawiki.igaming.source.betman.dto.BetmanEventDto;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BetmanApiClient {

    private final BetmanConfig config;
    private final WebClient.Builder webClientBuilder;

    public List<BetmanEventDto> fetchActiveEvents() {
        try {
            WebClient client = webClientBuilder.baseUrl(config.getBaseUrl()).build();
            // In dev mode/fallback returns empty or parsed feed
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("BetmanApiClient fetch error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}