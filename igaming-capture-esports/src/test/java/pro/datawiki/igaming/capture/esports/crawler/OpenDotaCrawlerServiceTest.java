package pro.datawiki.igaming.capture.esports.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.esports.dto.Dota2MatchPayloadDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenDotaCrawlerServiceTest {

    private OpenDotaCrawlerService openDotaCrawlerService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        openDotaCrawlerService = new OpenDotaCrawlerService(restTemplate, objectMapper);
    }

    @Test
    @DisplayName("Should build detailed Dota 2 payload with drafts, networth and item timings")
    void testBuildDetailedDota2Payload() {
        Dota2MatchPayloadDto payload = openDotaCrawlerService.buildDetailedDota2Payload(
                789001L, 15001L, "The International", "Team Spirit", "Gaimin Gladiators");

        assertThat(payload).isNotNull();
        assertThat(payload.getMatchId()).isEqualTo(789001L);
        assertThat(payload.getRadiantTeam()).isEqualTo("Team Spirit");
        assertThat(payload.getDireTeam()).isEqualTo("Gaimin Gladiators");

        // Verify Hero Drafts
        assertThat(payload.getDrafts()).isNotEmpty();
        assertThat(payload.getDrafts()).anyMatch(d -> d.getIsPick() && "Invoker".equals(d.getHeroName()));
        assertThat(payload.getDrafts()).anyMatch(d -> !d.getIsPick() && "Abaddon".equals(d.getHeroName()));

        // Verify Net Worth and XP advantage timelines
        assertThat(payload.getRadiantGoldAdvTimeline()).isNotEmpty();
        assertThat(payload.getRadiantXpAdvTimeline()).isNotEmpty();
        assertThat(payload.getRadiantGoldAdvTimeline().get(payload.getRadiantGoldAdvTimeline().size() - 1)).isGreaterThan(10000);

        // Verify Item Timings (BKB, Blink)
        assertThat(payload.getItemTimings()).isNotEmpty();
        assertThat(payload.getItemTimings()).anyMatch(i -> "black_king_bar".equals(i.getItemName()));
        assertThat(payload.getItemTimings()).anyMatch(i -> "blink".equals(i.getItemName()));
    }

    @Test
    @DisplayName("Should crawl pro matches and return structured match objects")
    void testCrawlProMatches() {
        List<OpenDotaCrawlerService.ParsedDota2Match> matches = openDotaCrawlerService.crawlProMatches();
        assertThat(matches).isNotEmpty();
        assertThat(matches.get(0).radiantTeam()).isNotBlank();
        assertThat(matches.get(0).direTeam()).isNotBlank();
        assertThat(matches.get(0).payload().getDrafts()).isNotEmpty();
    }
}
