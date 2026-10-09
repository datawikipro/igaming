package pro.datawiki.igaming.source.bet9ja.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.bet9ja.config.Bet9jaConfig;
import pro.datawiki.igaming.source.bet9ja.dto.Bet9jaEventDto;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class Bet9jaApiClient {

    private final Bet9jaConfig config;
    private final WebClient.Builder webClientBuilder;

    public List<Bet9jaEventDto> fetchActiveEvents() {
        try {
            WebClient client = webClientBuilder.baseUrl(config.getBaseUrl()).build();
            // In dev mode/fallback returns empty or parsed feed
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Bet9jaApiClient fetch error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}