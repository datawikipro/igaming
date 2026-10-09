package pro.datawiki.igaming.source.stake.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.stake.dto.StakeFixtureDto;
import pro.datawiki.igaming.source.stake.dto.StakeMarketDto;
import pro.datawiki.igaming.source.stake.dto.StakeOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class StakeOddsMapperTest {

    private StakeOddsMapper mapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;
    private BetTypeResolverService betTypeResolver;

    @BeforeEach
    void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        betTypeResolver = Mockito.mock(BetTypeResolverService.class);

        when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        mapper = new StakeOddsMapper(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Test
    void testMapStakeFixture() {
        StakeFixtureDto fixture = new StakeFixtureDto();
        fixture.setId("stk_1001");
        fixture.setName("Manchester City vs Liverpool");
        fixture.setStatus("active");
        fixture.setStartTime(1700000000000L);

        StakeFixtureDto.SportRef sport = new StakeFixtureDto.SportRef();
        sport.setName("Soccer");
        sport.setSlug("soccer");
        fixture.setSport(sport);

        StakeFixtureDto.TournamentRef tournament = new StakeFixtureDto.TournamentRef();
        tournament.setName("Premier League");
        fixture.setTournament(tournament);

        StakeFixtureDto.CompetitorRef comp1 = new StakeFixtureDto.CompetitorRef();
        comp1.setName("Manchester City");
        comp1.setQualifier("home");

        StakeFixtureDto.CompetitorRef comp2 = new StakeFixtureDto.CompetitorRef();
        comp2.setName("Liverpool");
        comp2.setQualifier("away");

        fixture.setCompetitors(List.of(comp1, comp2));

        // 1X2 market
        StakeMarketDto m1X2 = new StakeMarketDto();
        m1X2.setId("m1");
        m1X2.setName("1X2");

        StakeOutcomeDto o1 = new StakeOutcomeDto();
        o1.setId("o1");
        o1.setName("Manchester City");
        o1.setOdds(1.95);

        StakeOutcomeDto oX = new StakeOutcomeDto();
        oX.setId("oX");
        oX.setName("Draw");
        oX.setOdds(3.60);

        StakeOutcomeDto o2 = new StakeOutcomeDto();
        o2.setId("o2");
        o2.setName("Liverpool");
        o2.setOdds(3.80);

        m1X2.setOutcomes(List.of(o1, oX, o2));

        // Total
        StakeMarketDto mTotal = new StakeMarketDto();
        mTotal.setId("m2");
        mTotal.setName("Total Goals");

        StakeOutcomeDto oOver = new StakeOutcomeDto();
        oOver.setId("oOver");
        oOver.setName("Over 2.5");
        oOver.setOdds(1.70);
        oOver.setLine(2.5);

        StakeOutcomeDto oUnder = new StakeOutcomeDto();
        oUnder.setId("oUnder");
        oUnder.setName("Under 2.5");
        oUnder.setOdds(2.15);
        oUnder.setLine(2.5);

        mTotal.setOutcomes(List.of(oOver, oUnder));

        // BTTS
        StakeMarketDto mBtts = new StakeMarketDto();
        mBtts.setId("m3");
        mBtts.setName("Both Teams To Score");

        StakeOutcomeDto bttsYes = new StakeOutcomeDto();
        bttsYes.setId("bttsYes");
        bttsYes.setName("Yes");
        bttsYes.setOdds(1.65);

        StakeOutcomeDto bttsNo = new StakeOutcomeDto();
        bttsNo.setId("bttsNo");
        bttsNo.setName("No");
        bttsNo.setOdds(2.20);

        mBtts.setOutcomes(List.of(bttsYes, bttsNo));

        fixture.setMarkets(List.of(m1X2, mTotal, mBtts));

        OddsUpdateRequest result = mapper.mapToOddsUpdateRequest(fixture);

        assertNotNull(result);
        assertEquals("stake", result.getBookmaker());
        assertEquals("stk_1001", result.getExternalEventId());
        assertEquals("Manchester City", result.getTeam1());
        assertEquals("Liverpool", result.getTeam2());
        assertEquals(7, result.getOdds().size());

        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet && o.getValue() == 1.95));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet && o.getValue() == 1.70));
        assertTrue(result.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet && o.getValue() == 1.65));
    }
}
