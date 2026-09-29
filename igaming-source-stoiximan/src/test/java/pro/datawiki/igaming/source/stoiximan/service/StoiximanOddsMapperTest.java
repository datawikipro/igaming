package pro.datawiki.igaming.source.stoiximan.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.*;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

public class StoiximanOddsMapperTest {

    private StoiximanOddsMapper oddsMapper;
    private UnmappedBetService unmappedBetService;
    private SportNormalizationService sportNormalizationService;

    @BeforeEach
    public void setUp() {
        unmappedBetService = Mockito.mock(UnmappedBetService.class);
        sportNormalizationService = Mockito.mock(SportNormalizationService.class);

        Mockito.when(sportNormalizationService.normalize(anyString())).thenReturn(SportType.FOOTBALL);

        oddsMapper = new StoiximanOddsMapper(unmappedBetService, sportNormalizationService);
    }

    @Test
    public void testMapToOddsUpdateRequest_FullLineAndStatistics() {
        MatchCache matchCache = new MatchCache();
        matchCache.setExternalId("10203040");
        matchCache.setSportName("Football");
        matchCache.setLeagueName("Super League Greece");
        matchCache.setTeam1("Olympiacos");
        matchCache.setTeam2("Panathinaikos");
        matchCache.setIsLive(false);

        List<KambiBetOffer> betoffers = new ArrayList<>();

        // 1. Match Winner (1X2)
        KambiBetOffer bo1x2 = new KambiBetOffer();
        KambiBetOffer.KambiCriterion c1x2 = new KambiBetOffer.KambiCriterion();
        c1x2.setLabel("Match");
        c1x2.setEnglishLabel("Match");
        bo1x2.setCriterion(c1x2);
        KambiOutcome o1 = new KambiOutcome();
        o1.setId(1001L);
        o1.setType("OT_ONE");
        o1.setLabel("Olympiacos");
        o1.setOdds(2050); // 2.05
        KambiOutcome ox = new KambiOutcome();
        ox.setId(1002L);
        ox.setType("OT_DRAW");
        ox.setLabel("Draw");
        ox.setOdds(3200); // 3.20
        KambiOutcome o2 = new KambiOutcome();
        o2.setId(1003L);
        o2.setType("OT_TWO");
        o2.setLabel("Panathinaikos");
        o2.setOdds(3800); // 3.80
        bo1x2.setOutcomes(List.of(o1, ox, o2));
        betoffers.add(bo1x2);

        // 2. Total Goals Over/Under 2.5
        KambiBetOffer boTotal = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cTotal = new KambiBetOffer.KambiCriterion();
        cTotal.setLabel("Total Goals");
        cTotal.setEnglishLabel("Total Goals");
        boTotal.setCriterion(cTotal);
        KambiOutcome oOver = new KambiOutcome();
        oOver.setId(2001L);
        oOver.setType("OT_OVER");
        oOver.setLine(2.5);
        oOver.setLabel("Over 2.5");
        oOver.setOdds(1950);
        KambiOutcome oUnder = new KambiOutcome();
        oUnder.setId(2002L);
        oUnder.setType("OT_UNDER");
        oUnder.setLine(2.5);
        oUnder.setLabel("Under 2.5");
        oUnder.setOdds(1850);
        boTotal.setOutcomes(List.of(oOver, oUnder));
        betoffers.add(boTotal);

        // 3. Handicap
        KambiBetOffer boHandicap = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cHandicap = new KambiBetOffer.KambiCriterion();
        cHandicap.setLabel("Handicap");
        cHandicap.setEnglishLabel("Handicap");
        boHandicap.setCriterion(cHandicap);
        KambiOutcome oH1 = new KambiOutcome();
        oH1.setId(3001L);
        oH1.setType("OT_ONE");
        oH1.setLine(-1.0);
        oH1.setLabel("Olympiacos -1.0");
        oH1.setOdds(2800);
        KambiOutcome oH2 = new KambiOutcome();
        oH2.setId(3002L);
        oH2.setType("OT_TWO");
        oH2.setLine(1.0);
        oH2.setLabel("Panathinaikos 1.0");
        oH2.setOdds(1450);
        boHandicap.setOutcomes(List.of(oH1, oH2));
        betoffers.add(boHandicap);

        // 4. Both Teams to Score
        KambiBetOffer boBtts = new KambiBetOffer();
        KambiBetOffer.KambiCriterion cBtts = new KambiBetOffer.KambiCriterion();
        cBtts.setLabel("Both Teams to Score");
        cBtts.setEnglishLabel("Both Teams to Score");
        boBtts.setCriterion(cBtts);
        KambiOutcome bttsYes = new KambiOutcome();
        bttsYes.setId(4001L);
        bttsYes.setType("OT_YES");
        bttsYes.setLabel("Yes");
        bttsYes.setOdds(1900);
        KambiOutcome bttsNo = new KambiOutcome();
        bttsNo.setId(4002L);
        bttsNo.setType("OT_NO");
        bttsNo.setLabel("No");
        bttsNo.setOdds(1800);
        boBtts.setOutcomes(List.of(bttsYes, bttsNo));
        betoffers.add(boBtts);

        OddsUpdateRequest request = oddsMapper.mapToOddsUpdateRequest(matchCache, betoffers);

        assertNotNull(request);
        assertEquals("stoiximan", request.getBookmaker());
        assertEquals("10203040", request.getExternalEventId());
        assertEquals("Football", request.getSportName());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertEquals("Super League Greece", request.getLeagueName());
        assertEquals("Olympiacos", request.getTeam1());
        assertEquals("Panathinaikos", request.getTeam2());

        List<OddItem> odds = request.getOdds();
        assertNotNull(odds);
        assertEquals(9, odds.size());

        // Check 1X2 outcome
        OddItem itemWin1 = odds.stream().filter(o -> "1001".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemWin1);
        assertEquals(2.05, itemWin1.getValue());
        assertTrue(itemWin1.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) itemWin1.getBetType()).outcome());

        // Check Over 2.5
        OddItem itemOver = odds.stream().filter(o -> "2001".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemOver);
        assertEquals(1.95, itemOver.getValue());
        assertTrue(itemOver.getBetType() instanceof TotalBet);
        assertEquals(TotalBet.Direction.OVER, ((TotalBet) itemOver.getBetType()).direction());
        assertEquals(2.5, ((TotalBet) itemOver.getBetType()).param());

        // Check Both Teams to Score
        OddItem itemBttsYes = odds.stream().filter(o -> "4001".equals(o.getFactorId())).findFirst().orElse(null);
        assertNotNull(itemBttsYes);
        assertEquals(1.90, itemBttsYes.getValue());
        assertTrue(itemBttsYes.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet btts = (BinaryMarketBet) itemBttsYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, btts.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, btts.outcome());
    }
}
