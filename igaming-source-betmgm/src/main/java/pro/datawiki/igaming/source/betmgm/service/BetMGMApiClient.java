package pro.datawiki.igaming.source.betmgm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.betmgm.dto.BetMGMEventGroupResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BetMGMApiClient {

    private final BrowserService browserService;
    private final ObjectMapper objectMapper;
    private final BetMGMOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;

    public void processLeague(String leagueId, String sportName, String leagueName) {
        String url = "https://sports.betmgm.com/en/sports/events/" + leagueId;
        log.info("Fetching BetMGM league: {} (URL: {})", leagueName, url);

        try {
            String json = browserService.navigateAndInterceptResponse(url, urlStr -> urlStr.contains("betmgm.com") && urlStr.contains("fixture"), 15000);
            
            if (json == null || json.isEmpty()) {
                log.warn("Empty response for BetMGM league {}", leagueName);
                return;
            }

            BetMGMEventGroupResponse response = objectMapper.readValue(json, BetMGMEventGroupResponse.class);
            
            if (response.getFixtures() != null) {
                List<OddsUpdateRequest> requests = response.getFixtures().stream()
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
            log.error("Failed to process BetMGM league {}: {}", leagueName, e.getMessage());
        }
    }
}
