package pro.datawiki.igaming.source.core.engine.xbet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.domain.MatchFactor;
import pro.datawiki.igaming.source.core.engine.xbet.dto.XbetFamilyEvent;
import pro.datawiki.igaming.source.core.engine.xbet.dto.XbetFamilyGame;
import pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetMainResultStrategy;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class XbetFamilyOddsProcessorTest {

    private XbetFamilyOddsProcessor processor;
    private BetTypeResolverService resolverService;
    private UnmappedBetService unmappedBetService;

    @BeforeEach
    void setUp() {
        resolverService = Mockito.mock(BetTypeResolverService.class);
        unmappedBetService = Mockito.mock(UnmappedBetService.class);

        processor = new XbetFamilyOddsProcessor(
                resolverService,
                unmappedBetService,
                List.of(new XbetMainResultStrategy())
        );

        when(resolverService.resolve(anyString(), any(), eq("W1"), eq("1"), any(), any()))
                .thenReturn(new MatchResultBet(pro.datawiki.igaming.dto.market.BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, StatType.MATCH));
        when(resolverService.resolve(anyString(), any(), eq("X"), eq("2"), any(), any()))
                .thenReturn(new MatchResultBet(pro.datawiki.igaming.dto.market.BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, StatType.MATCH));
        when(resolverService.resolve(anyString(), any(), eq("W2"), eq("3"), any(), any()))
                .thenReturn(new MatchResultBet(pro.datawiki.igaming.dto.market.BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, StatType.MATCH));
    }

    @Test
    void testPrecisionDeduplicationRugbySamoaFrance() {
        // Reproduces actual anomaly payload 332129 / 758636906
        // E contains:
        // {"T": 1, "G": 1, "C": 1.01, "P": null}
        // {"T": 1, "G": 1, "C": 1.001, "P": null}
        // {"T": 2, "G": 1, "C": 100.0, "P": null}
        // {"T": 3, "G": 1, "C": 15.0, "P": null}

        XbetFamilyGame game = new XbetFamilyGame();
        XbetFamilyEvent e1 = new XbetFamilyEvent();
        e1.setType(1);
        e1.setGroup(1);
        e1.setCoefficient(1.01);

        XbetFamilyEvent e2 = new XbetFamilyEvent();
        e2.setType(1);
        e2.setGroup(1);
        e2.setCoefficient(1.001);

        XbetFamilyEvent e3 = new XbetFamilyEvent();
        e3.setType(2);
        e3.setGroup(1);
        e3.setCoefficient(100.0);

        XbetFamilyEvent e4 = new XbetFamilyEvent();
        e4.setType(3);
        e4.setGroup(1);
        e4.setCoefficient(15.0);

        game.setEvents(List.of(e1, e2, e3, e4));

        MatchCache match = new MatchCache();
        match.setExternalId("758636906");
        match.setTeam1("Самоа");
        match.setTeam2("Франция");

        List<OddItem> odds = processor.processOdds(match, game, SportType.FOOTBALL, "1xbet");
        assertEquals(3, odds.size(), "Should have exactly 3 odds: WIN1, DRAW, WIN2");

        OddItem win1 = odds.stream().filter(o -> "WIN1".equals(o.getBetType().code())).findFirst().orElse(null);
        assertNotNull(win1, "WIN1 must not be dropped due to collision!");
        assertEquals(1.001, win1.getValue(), "Higher precision quote (1.001) should be chosen over rounded (1.01)");

        // Verify factors deduplication
        List<MatchFactor> factors = processor.buildFactors(game);
        assertEquals(3, factors.size(), "Should produce 3 factors");
        MatchFactor f1 = factors.stream().filter(f -> "1".equals(f.getFactorId())).findFirst().orElse(null);
        assertNotNull(f1);
        assertEquals(1.001, f1.getValue());
    }
}
