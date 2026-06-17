package pro.datawiki.igaming.source.caesars.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.caesars.dto.CaesarsEventGroupResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaesarsApiClient {

    private final BrowserService browserService;
    private final ObjectMapper objectMapper;
    private final CaesarsOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;

    public void processLeague(String leagueId, String sportName, String leagueName) {
        String url = "https://sportsbook.caesars.com/us/mi/bet/competitions/" + leagueId;
        log.info("Fetching Caesars league: {} (URL: {})", leagueName, url);

        try {
            String json = browserService.navigateAndInterceptResponse(
                    url, 
                    urlStr -> urlStr.contains("api.americanwagering.com") && (urlStr.contains("competitions") || urlStr.contains("events")), 
                    15000
            );
            
            if (json == null || json.isEmpty()) {
                log.warn("Empty response for Caesars league {}", leagueName);
                return;
            }

            CaesarsEventGroupResponse response = objectMapper.readValue(json, CaesarsEventGroupResponse.class);
            
            if (response.getCompetitions() != null && !response.getCompetitions().isEmpty()) {
                for (CaesarsEventGroupResponse.Competition comp : response.getCompetitions()) {
                    if (comp.getEvents() != null) {
                        List<OddsUpdateRequest> requests = comp.getEvents().stream()
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
                }
            }

        } catch (Exception e) {
            log.error("Failed to process Caesars league {}: {}", leagueName, e.getMessage());
        }
    }
}
