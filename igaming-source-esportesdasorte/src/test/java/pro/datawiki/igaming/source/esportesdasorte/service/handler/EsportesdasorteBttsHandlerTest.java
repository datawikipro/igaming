package pro.datawiki.igaming.source.esportesdasorte.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeData;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeGroupData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EsportesdasorteBttsHandlerTest {

    private EsportesdasorteBttsHandler handler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        handler = new EsportesdasorteBttsHandler();

        match = new MatchCache();
        match.setId(101L);
        match.setTeam1("Flamengo");
        match.setTeam2("Palmeiras");
        match.setSportName("Football");
    }

    @Test
    void testBttsFullMatchEnglishYesNo() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(46L)
                .nameEn("Both Teams To Score")
                .nameRu("Обе команды забьют")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(461L).nameEn("Yes").factor(1.72).build(),
                        EsportesdasorteStakeData.builder().id(462L).nameEn("No").factor(2.10).build()
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
    void testBttsFullMatchRussianYesNo() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(46L)
                .nameEn("")
                .nameRu("Обе забьют")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(463L).nameRu("Да").factor(1.85).build(),
                        EsportesdasorteStakeData.builder().id(464L).nameRu("Нет").factor(1.95).build()
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
    void testBttsPortuguesePtBrYesNo() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Ambas Marcam")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(465L).nameEn("Sim").factor(1.78).build(),
                        EsportesdasorteStakeData.builder().id(466L).nameEn("Não").factor(2.05).build()
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
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(10L)
                .nameEn("Both Teams To Score")
                .nameRu("Обе забьют")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(101L).nameEn("Yes").nameRu("Да").factor(4.50).build(),
                        EsportesdasorteStakeData.builder().id(102L).nameEn("No").nameRu("Нет").factor(1.18).build()
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
    void testBttsHalf1ByName() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("1st Half - Both Teams To Score")
                .nameRu("1-й тайм - Обе забьют")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(501L).nameEn("Yes").factor(4.20).build(),
                        EsportesdasorteStakeData.builder().id(502L).nameEn("No").factor(1.22).build()
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
    void testBttsHalf1PortugueseByName() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("1º Tempo - Ambas Marcam")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(511L).nameEn("Sim").factor(4.10).build(),
                        EsportesdasorteStakeData.builder().id(512L).nameEn("Não").factor(1.20).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        BinaryMarketBet yesBet = (BinaryMarketBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_1, yesBet.scope());
        assertEquals(BinaryMarketBet.Outcome.YES, yesBet.outcome());
    }

    @Test
    void testBttsHalf2ByName() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("2nd Half - Both Teams To Score")
                .nameRu("2-й тайм - Обе команды забьют")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(601L).nameEn("Yes").factor(3.80).build(),
                        EsportesdasorteStakeData.builder().id(602L).nameEn("No").factor(1.25).build()
                ))
                .build();

        assertTrue(handler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        handler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(2, items.size());

        BinaryMarketBet yesBet = (BinaryMarketBet) items.get(0).getBetType();
        assertEquals(BetScope.HALF_2, yesBet.scope());
        assertEquals(BinaryMarketBet.Outcome.YES, yesBet.outcome());

        BinaryMarketBet noBet = (BinaryMarketBet) items.get(1).getBetType();
        assertEquals(BetScope.HALF_2, noBet.scope());
        assertEquals(BinaryMarketBet.Outcome.NO, noBet.outcome());
    }

    @Test
    void testBttsCompoundStakeNames() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .nameEn("Both Teams to Score")
                .nameRu("Обе забьют")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(701L).nameEn("Both Teams to Score - Yes").nameRu("Обе забьют - Да").factor(1.90).build(),
                        EsportesdasorteStakeData.builder().id(702L).nameEn("Both Teams to Score - No").nameRu("Обе забьют - Нет").factor(1.90).build()
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
    void testSupportsExclusions() {
        // Corners
        assertFalse(handler.supports(EsportesdasorteStakeGroupData.builder()
                .nameEn("Corners Both Teams To Score")
                .stakes(List.of(EsportesdasorteStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        // Yellow Cards
        assertFalse(handler.supports(EsportesdasorteStakeGroupData.builder()
                .nameEn("Yellow Cards Both Teams To Score")
                .stakes(List.of(EsportesdasorteStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        // Total
        assertFalse(handler.supports(EsportesdasorteStakeGroupData.builder()
                .nameEn("Total Goals")
                .stakes(List.of(EsportesdasorteStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        // Handicap
        assertFalse(handler.supports(EsportesdasorteStakeGroupData.builder()
                .nameEn("Handicap")
                .stakes(List.of(EsportesdasorteStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        // Double chance
        assertFalse(handler.supports(EsportesdasorteStakeGroupData.builder()
                .nameEn("Double Chance")
                .stakes(List.of(EsportesdasorteStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        // Match result
        assertFalse(handler.supports(EsportesdasorteStakeGroupData.builder()
                .nameEn("Match Result 1X2")
                .stakes(List.of(EsportesdasorteStakeData.builder().factor(2.0).build())).build(), SportType.FOOTBALL));

        // Empty stakes
        assertFalse(handler.supports(EsportesdasorteStakeGroupData.builder()
                .id(46L)
                .nameEn("Both Teams To Score")
                .stakes(List.of()).build(), SportType.FOOTBALL));

        // Null group
        assertFalse(handler.supports((EsportesdasorteStakeGroupData) null, SportType.FOOTBALL));
    }

    @Test
    void testInvalidOddsAndNullFactors() {
        EsportesdasorteStakeGroupData group = EsportesdasorteStakeGroupData.builder()
                .id(46L)
                .nameEn("Both Teams To Score")
                .stakes(List.of(
                        EsportesdasorteStakeData.builder().id(801L).nameEn("Yes").factor(1.0).build(), // odd <= 1.0
                        EsportesdasorteStakeData.builder().id(802L).nameEn("No").factor(null).build(), // null odd
                        EsportesdasorteStakeData.builder().id(803L).nameEn("Unknown").factor(2.5).build(), // unknown outcome
                        EsportesdasorteStakeData.builder().id(804L).nameEn("Yes").factor(1.75).build() // valid
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
