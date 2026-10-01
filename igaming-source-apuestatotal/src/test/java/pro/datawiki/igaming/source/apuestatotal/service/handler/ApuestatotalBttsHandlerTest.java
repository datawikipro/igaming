package pro.datawiki.igaming.source.apuestatotal.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApuestatotalBttsHandlerTest {

    private ApuestatotalBttsHandler handler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        handler = new ApuestatotalBttsHandler();

        match = new MatchCache();
        match.setId(101L);
        match.setTeam1("Alianza Lima");
        match.setTeam2("Universitario");
        match.setSportName("Football");
    }

    @Test
    void testBttsFullMatchEnglishYesNo() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(46L)
                .nameEn("Both Teams To Score")
                .nameRu("Обе команды забьют")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(461L).nameEn("Yes").factor(1.72).build(),
                        ApuestatotalStakeData.builder().id(462L).nameEn("No").factor(2.10).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        OddItem yesItem = items.get(0);
        assertEquals("461", yesItem.getFactorId());
        assertEquals("Both Teams To Score", yesItem.getGroupName());
        assertEquals("Yes", yesItem.getName());
        assertEquals(1.72, yesItem.getValue());
        assertTrue(yesItem.getBetType() instanceof BinaryMarketBet);

        BinaryMarketBet yesBet = (BinaryMarketBet) yesItem.getBetType();
        assertEquals(BetScope.FULL_MATCH, yesBet.scope());
        assertEquals(BetSubject.MATCH, yesBet.subject());
        assertEquals(BinaryMarketBet.MarketType.BTTS, yesBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, yesBet.outcome());
        assertEquals(StatType.MATCH, yesBet.statType());

        OddItem noItem = items.get(1);
        assertEquals("462", noItem.getFactorId());
        assertEquals(2.10, noItem.getValue());
        assertTrue(noItem.getBetType() instanceof BinaryMarketBet);

        BinaryMarketBet noBet = (BinaryMarketBet) noItem.getBetType();
        assertEquals(BetScope.FULL_MATCH, noBet.scope());
        assertEquals(BetSubject.MATCH, noBet.subject());
        assertEquals(BinaryMarketBet.MarketType.BTTS, noBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.NO, noBet.outcome());
        assertEquals(StatType.MATCH, noBet.statType());
    }

    @Test
    void testBttsSpanishAmbosAnotan() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("Ambos equipos anotan")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(463L).nameEn("Sí").factor(1.80).build(),
                        ApuestatotalStakeData.builder().id(464L).nameEn("No").factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) items.get(0).getBetType()).outcome());
        assertEquals(BinaryMarketBet.Outcome.NO, ((BinaryMarketBet) items.get(1).getBetType()).outcome());
    }

    @Test
    void testBttsRussianYesNo() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(46L)
                .nameRu("Обе забьют")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(465L).nameRu("Да").factor(1.85).build(),
                        ApuestatotalStakeData.builder().id(466L).nameRu("Нет").factor(1.95).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        BinaryMarketBet yesBet = (BinaryMarketBet) items.get(0).getBetType();
        assertEquals(BinaryMarketBet.Outcome.YES, yesBet.outcome());

        BinaryMarketBet noBet = (BinaryMarketBet) items.get(1).getBetType();
        assertEquals(BinaryMarketBet.Outcome.NO, noBet.outcome());
    }

    @Test
    void testBttsHalf1ById() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(10L)
                .nameEn("Both Teams To Score")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(101L).nameEn("Yes").factor(4.50).build(),
                        ApuestatotalStakeData.builder().id(102L).nameEn("No").factor(1.18).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        BinaryMarketBet yesBet = (BinaryMarketBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, yesBet.scope());
        assertEquals(BinaryMarketBet.Outcome.YES, yesBet.outcome());

        BinaryMarketBet noBet = (BinaryMarketBet) items.get(1).getBetType();
        assertEquals(BetScope.HALF_1, noBet.scope());
        assertEquals(BinaryMarketBet.Outcome.NO, noBet.outcome());
    }

    @Test
    void testBttsHalf1SpanishPrimerTiempo() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("1er tiempo - Ambos anotan")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(501L).nameEn("Sí").factor(4.20).build(),
                        ApuestatotalStakeData.builder().id(502L).nameEn("No").factor(1.22).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertEquals(BetScope.HALF_1, ((BinaryMarketBet) items.get(0).getBetType()).scope());
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) items.get(0).getBetType()).outcome());
        assertEquals(BetScope.HALF_1, ((BinaryMarketBet) items.get(1).getBetType()).scope());
        assertEquals(BinaryMarketBet.Outcome.NO, ((BinaryMarketBet) items.get(1).getBetType()).outcome());
    }

    @Test
    void testBttsHalf2SpanishSegundoTiempo() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .nameEn("2do tiempo - Ambos anotan")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(601L).nameEn("Si").factor(3.80).build(),
                        ApuestatotalStakeData.builder().id(602L).nameEn("No").factor(1.25).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());
        assertEquals(BetScope.HALF_2, ((BinaryMarketBet) items.get(0).getBetType()).scope());
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) items.get(0).getBetType()).outcome());
        assertEquals(BetScope.HALF_2, ((BinaryMarketBet) items.get(1).getBetType()).scope());
        assertEquals(BinaryMarketBet.Outcome.NO, ((BinaryMarketBet) items.get(1).getBetType()).outcome());
    }

    @Test
    void testSupportsExclusions() {
        assertFalse(handler.supports(ApuestatotalStakeGroupData.builder()
                .nameEn("Corners Both Teams To Score")
                .stakes(List.of(ApuestatotalStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        assertFalse(handler.supports(ApuestatotalStakeGroupData.builder()
                .nameEn("Yellow Cards Both Teams To Score")
                .stakes(List.of(ApuestatotalStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        assertFalse(handler.supports(ApuestatotalStakeGroupData.builder()
                .nameEn("Total Goals")
                .stakes(List.of(ApuestatotalStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        assertFalse(handler.supports(ApuestatotalStakeGroupData.builder()
                .nameEn("Handicap")
                .stakes(List.of(ApuestatotalStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        assertFalse(handler.supports(ApuestatotalStakeGroupData.builder()
                .nameEn("Double Chance")
                .stakes(List.of(ApuestatotalStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        assertFalse(handler.supports(ApuestatotalStakeGroupData.builder()
                .id(46L)
                .nameEn("Both Teams To Score")
                .stakes(List.of()).build(), SportType.FOOTBALL));

        assertFalse(handler.supports((ApuestatotalStakeGroupData) null, SportType.FOOTBALL));
    }

    @Test
    void testInvalidOddsAndNullFactors() {
        ApuestatotalStakeGroupData group = ApuestatotalStakeGroupData.builder()
                .id(46L)
                .nameEn("Both Teams To Score")
                .stakes(List.of(
                        ApuestatotalStakeData.builder().id(801L).nameEn("Yes").factor(1.0).build(),
                        ApuestatotalStakeData.builder().id(802L).nameEn("No").factor(null).build(),
                        ApuestatotalStakeData.builder().id(803L).nameEn("Unknown").factor(2.5).build(),
                        ApuestatotalStakeData.builder().id(804L).nameEn("Yes").factor(1.75).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(1, items.size());
        assertEquals("804", items.get(0).getFactorId());
        assertEquals(1.75, items.get(0).getValue());
        assertEquals(BinaryMarketBet.Outcome.YES, ((BinaryMarketBet) items.get(0).getBetType()).outcome());
    }
}
