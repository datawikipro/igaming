package pro.datawiki.igaming.source.tab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.tab.dto.TabEventDto;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TabApiClient {

    private final WebClient tabWebClient;

    public List<TabEventDto> fetchActiveEvents() {
        try {
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch events from Tab: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
