package pro.datawiki.igaming.source.fanduel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.fanduel.dto.FanDuelEventGroupResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FanDuelApiClient {

    private final BrowserService browserService;
    private final ObjectMapper objectMapper;
    private final FanDuelOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;

    public void processLeague(String leagueId, String sportName, String leagueName) {
        String url = "https://sportsbook.fanduel.com/navigation/" + leagueName.toLowerCase();
        log.info("Fetching FanDuel league: {} (URL: {})", leagueName, url);

        try {
            // Wait for response via headless browser to bypass basic WAF
            String json = browserService.navigateAndInterceptResponse(
                    url, 
                    urlStr -> urlStr.startsWith("https://sportsbook.fanduel.com/api/v5/eventgroup/") && urlStr.contains(leagueId), 
                    15000
            );
            
            if (json == null || json.isEmpty()) {
                log.warn("Empty response for FanDuel league {}", leagueName);
                return;
            }

            FanDuelEventGroupResponse response = objectMapper.readValue(json, FanDuelEventGroupResponse.class);
            
            if (response.getAttachments() != null && response.getAttachments().getEvents() != null) {
                List<OddsUpdateRequest> requests = response.getAttachments().getEvents().values().stream()
                        .map(event -> oddsMapper.mapToOddsUpdateRequest(event, response, sportName, leagueName))
                        .filter(req -> req != null && req.getOdds() != null && !req.getOdds().isEmpty())
                        .collect(Collectors.toList());

                for (OddsUpdateRequest request : requests) {
                    try {
                        aggregatorClient.pushOddsUpdate(request);
                    } catch (Exception e) {
                        log.error("Failed to send odds update for event {}: {}", request.getExternalEventId(), e.getMessage());
                    }
                }
                log.info("Processed {} events for {}", requests.size(), leagueName);
            }

        } catch (Exception e) {
            log.error("Failed to process FanDuel league {}: {}", leagueName, e.getMessage());
        }
    }
}
