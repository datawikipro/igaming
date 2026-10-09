package pro.datawiki.igaming.source.cloudbet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.cloudbet.dto.CloudbetFixtureDto;
import pro.datawiki.igaming.source.cloudbet.dto.CloudbetMarketDto;
import pro.datawiki.igaming.source.cloudbet.dto.CloudbetSelectionDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CloudbetOddsMapperTest {

    private CloudbetOddsMapper mapper;
    private SportNormalizationService sportNormalizationService;

    @BeforeEach
    void setUp() {
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);
        mapper = new CloudbetOddsMapper(sportNormalizationService);
    }

    @Test
    void testMapCloudbetMarkets() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("cb_3001");
        cache.setTeam1("Arsenal");
        cache.setTeam2("Chelsea");
        cache.setSportName("Soccer");
        cache.setLeagueName("English Premier League");
        cache.setStartTime(1715000000000L);
        cache.setIsLive(false);

        CloudbetFixtureDto fixture = new CloudbetFixtureDto();
        fixture.setId("cb_3001");
        fixture.setName("Arsenal vs Chelsea");

        CloudbetFixtureDto.TeamRef home = new CloudbetFixtureDto.TeamRef();
        home.setName("Arsenal");
        fixture.setHome(home);

        CloudbetFixtureDto.TeamRef away = new CloudbetFixtureDto.TeamRef();
        away.setName("Chelsea");
        fixture.setAway(away);

        Map<String, CloudbetMarketDto> markets = new HashMap<>();

        // Match Odds (1X2)
        CloudbetMarketDto m1X2 = new CloudbetMarketDto();
        m1X2.setKey("soccer.match_odds");

        CloudbetSelectionDto s1 = new CloudbetSelectionDto();
        s1.setOutcome("home");
        s1.setPrice(1.85);

        CloudbetSelectionDto sX = new CloudbetSelectionDto();
        sX.setOutcome("draw");
        sX.setPrice(3.75);

        CloudbetSelectionDto s2 = new CloudbetSelectionDto();
        s2.setOutcome("away");
        s2.setPrice(4.20);

        m1X2.setSelections(List.of(s1, sX, s2));
        markets.put("soccer.match_odds", m1X2);

        // Total Goals (Over/Under)
        CloudbetMarketDto mTotal = new CloudbetMarketDto();
        mTotal.setKey("soccer.total_goals");

        CloudbetSelectionDto sOver = new CloudbetSelectionDto();
        sOver.setOutcome("over");
        sOver.setPrice(1.78);
        sOver.setParams(2.5);

        CloudbetSelectionDto sUnder = new CloudbetSelectionDto();
        sUnder.setOutcome("under");
        sUnder.setPrice(2.05);
        sUnder.setParams(2.5);

        mTotal.setSelections(List.of(sOver, sUnder));
        markets.put("soccer.total_goals", mTotal);

        // Both Teams to Score
        CloudbetMarketDto mBtts = new CloudbetMarketDto();
        mBtts.setKey("soccer.both_teams_to_score");

        CloudbetSelectionDto sYes = new CloudbetSelectionDto();
        sYes.setOutcome("yes");
        sYes.setPrice(1.68);

        CloudbetSelectionDto sNo = new CloudbetSelectionDto();
        sNo.setOutcome("no");
        sNo.setPrice(2.15);

        mBtts.setSelections(List.of(sYes, sNo));
        markets.put("soccer.both_teams_to_score", mBtts);

        fixture.setMarkets(markets);

        OddsUpdateRequest result = mapper.mapToOddsUpdateRequest(cache, fixture);

        assertNotNull(result);
        assertEquals("cloudbet", result.getBookmaker());
        assertEquals("cb_3001", result.getExternalEventId());
        assertEquals("Arsenal", result.getTeam1());
        assertEquals("Chelsea", result.getTeam2());
        assertEquals(7, result.getOdds().size());

        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 1.85));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.78));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.68));
    }
}
