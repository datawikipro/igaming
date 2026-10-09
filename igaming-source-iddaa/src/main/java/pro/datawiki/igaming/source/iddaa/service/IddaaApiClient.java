package pro.datawiki.igaming.source.iddaa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.iddaa.dto.IddaaEventDto;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class IddaaApiClient {

    private final WebClient iddaaWebClient;

    public List<IddaaEventDto> fetchActiveEvents() {
        try {
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch events from Iddaa: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
