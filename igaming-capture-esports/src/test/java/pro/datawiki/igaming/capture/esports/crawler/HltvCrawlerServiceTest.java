package pro.datawiki.igaming.capture.esports.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.esports.dto.Cs2MatchPayloadDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HltvCrawlerServiceTest {

    private HltvCrawlerService hltvCrawlerService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        hltvCrawlerService = new HltvCrawlerService(restTemplate, objectMapper);
    }

    @Test
    @DisplayName("Should correctly classify tournament tiers from Tier-1 to Tier-4")
    void testTournamentTierClassification() {
        assertThat(hltvCrawlerService.classifyTournamentTier("BLAST Premier World Final 2026")).isEqualTo("TIER_1");
        assertThat(hltvCrawlerService.classifyTournamentTier("IEM Katowice Major")).isEqualTo("TIER_1");
        assertThat(hltvCrawlerService.classifyTournamentTier("BetBoom Dacha Belgrade")).isEqualTo("TIER_2");
        assertThat(hltvCrawlerService.classifyTournamentTier("CCT Season 2 European Series")).isEqualTo("TIER_3");
        assertThat(hltvCrawlerService.classifyTournamentTier("ESEA Open Qualifier")).isEqualTo("TIER_4");
    }

    @Test
    @DisplayName("Should build detailed CS2 payload with veto, player ratings 2.0 and rounds")
    void testBuildDetailedCs2Payload() {
        Cs2MatchPayloadDto payload = hltvCrawlerService.buildDetailedCs2Payload(
                12345L, "TIER_1", "BLAST Premier", "Natus Vincere", "FaZe Clan");

        assertThat(payload).isNotNull();
        assertThat(payload.getMatchId()).isEqualTo(12345L);
        assertThat(payload.getTournamentTier()).isEqualTo("TIER_1");

        // Verify Veto
        assertThat(payload.getVetoActions()).isNotEmpty();
        assertThat(payload.getVetoActions()).anyMatch(v -> "PICKED".equals(v.getAction()) && "Mirage".equals(v.getMapName()));
        assertThat(payload.getVetoActions()).anyMatch(v -> "LEFT_OVER".equals(v.getAction()) && "Ancient".equals(v.getMapName()));

        // Verify Maps and Rounds
        assertThat(payload.getMaps()).hasSize(2);
        Cs2MatchPayloadDto.MapDetailDto map1 = payload.getMaps().get(0);
        assertThat(map1.getMapName()).isEqualTo("Mirage");
        assertThat(map1.getTeam1Score()).isEqualTo(13);
        assertThat(map1.getTeam2Score()).isEqualTo(9);
        assertThat(map1.getRounds()).isNotEmpty();
        assertThat(map1.getRounds().get(0).getWinType()).isEqualTo("CT_ELIMINATION");

        // Verify Player Ratings 2.0, ADR, KAST
        assertThat(payload.getPlayers()).isNotEmpty();
        Cs2MatchPayloadDto.PlayerStatDto star = payload.getPlayers().get(0);
        assertThat(star.getRating20()).isGreaterThan(1.20);
        assertThat(star.getAdr()).isGreaterThan(80.0);
        assertThat(star.getKastPct()).isGreaterThan(70.0);
    }

    @Test
    @DisplayName("Should crawl tiered CS2 matches on simulated or live feed")
    void testCrawlMatches() {
        List<HltvCrawlerService.ParsedCs2Match> matches = hltvCrawlerService.crawlMatches();
        assertThat(matches).isNotEmpty();
        assertThat(matches.size()).isGreaterThanOrEqualTo(5);

        // Verify all tiers are represented
        assertThat(matches).anyMatch(m -> "TIER_1".equals(m.tournamentTier()));
        assertThat(matches).anyMatch(m -> "TIER_2".equals(m.tournamentTier()));
        assertThat(matches).anyMatch(m -> "TIER_3".equals(m.tournamentTier()));
        assertThat(matches).anyMatch(m -> "TIER_4".equals(m.tournamentTier()));
    }
}
