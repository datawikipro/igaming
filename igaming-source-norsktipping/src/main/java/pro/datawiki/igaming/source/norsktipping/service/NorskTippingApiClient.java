package pro.datawiki.igaming.source.norsktipping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.source.norsktipping.dto.NorskTippingEventDto;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NorskTippingApiClient {

    private final WebClient norsktippingWebClient;

    public List<NorskTippingEventDto> fetchActiveEvents() {
        try {
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch events from NorskTipping: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
