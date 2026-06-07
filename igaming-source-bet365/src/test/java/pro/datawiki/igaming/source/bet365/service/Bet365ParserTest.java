package pro.datawiki.igaming.source.bet365.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.bet365.config.Bet365Config;
import pro.datawiki.igaming.source.core.browser.BrowserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class Bet365ParserTest {

    @Test
    public void testParserWithLobbyHtml() throws IOException {
        String htmlPath = "C:\\Users\\chernousov_a\\.gemini\\antigravity\\brain\\1c68c6a8-de19-485c-b400-013c8391f435\\scratch\\bet365-lobby.html";
        String htmlContent = Files.readString(Paths.get(htmlPath));

        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        
        Bet365Config.Api apiConfig = new Bet365Config.Api();
        apiConfig.setBaseUrl("https://www.bet365.com");
        
        Bet365Config config = Mockito.mock(Bet365Config.class);
        when(config.getApi()).thenReturn(apiConfig);

        BrowserService browserService = Mockito.mock(BrowserService.class);
        when(browserService.navigateAndGetBody(anyString(), anyInt(), anyString())).thenReturn(htmlContent);

        Bet365ApiClient client = new Bet365ApiClient(restTemplate, config, browserService);

        Bet365ApiClient.Bet365EventsResponse response = client.getEvents();
        assertNotNull(response);
        assertNotNull(response.getEvents());
        
        List<Bet365ApiClient.Bet365Event> events = response.getEvents();
        System.out.println("Parsed events count: " + events.size());
        for (Bet365ApiClient.Bet365Event event : events) {
            System.out.println("Event ID: " + event.getId());
            System.out.println("Sport: " + event.getSport());
            System.out.println("League: " + event.getLeague());
            System.out.println("HomeTeam: " + event.getHomeTeam());
            System.out.println("AwayTeam: " + event.getAwayTeam());
            System.out.println("StartTime: " + event.getStartTime());
            System.out.println("IsLive: " + event.isLive());
        }
        
        // Assert that we successfully parsed some events from the real HTML
        assertFalse(events.isEmpty(), "Should parse events from lobby HTML");

        int checkedOdds = 0;
        for (Bet365ApiClient.Bet365Event event : events) {
            assertNotNull(event.getId());
            assertNotNull(event.getSport());
            assertNotNull(event.getLeague());
            assertNotNull(event.getHomeTeam());
            assertNotNull(event.getAwayTeam());
            assertTrue(event.getStartTime() > 0);
            
            // Check that odds are cached for this event
            Bet365ApiClient.Bet365OddsResponse oddsResponse = client.getEventOdds(event.getId());
            assertNotNull(oddsResponse);
            assertEquals(event.getId(), oddsResponse.getEventId());
            assertNotNull(oddsResponse.getOdds());
            
            if (!oddsResponse.getOdds().isEmpty()) {
                checkedOdds += oddsResponse.getOdds().size();
                for (Bet365ApiClient.Bet365Odd odd : oddsResponse.getOdds()) {
                    assertNotNull(odd.getCode());
                    assertNotNull(odd.getMarketName());
                    assertNotNull(odd.getOutcomeName());
                    assertTrue(odd.getValue() > 0.0);
                }
            }
        }
        
        System.out.println("Successfully validated " + checkedOdds + " odds outcomes across " + events.size() + " matches.");
        assertTrue(checkedOdds > 0, "Should have parsed at least some odds");
    }
}
