package pro.datawiki.igaming.source.crickex.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.crickex.dto.CrickexEventDto;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrickexApiClient {

    private final WebClient crickexWebClient;

    public List<CrickexEventDto> fetchActiveEvents() {
        try {
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch events from Crickex: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
