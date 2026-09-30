package pro.datawiki.igaming.source.digitain.service.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DigitainMatchResultAndDoubleChanceHandlerTest {

    private DigitainMatchResultHandler matchResultHandler;
    private DigitainDoubleChanceHandler doubleChanceHandler;
    private MatchCache match;

    @BeforeEach
    void setUp() {
        matchResultHandler = new DigitainMatchResultHandler();
        doubleChanceHandler = new DigitainDoubleChanceHandler();

        match = new MatchCache();
        match.setId(100L);
        match.setTeam1("Arsenal");
        match.setTeam2("Chelsea");
        match.setSportName("Football");
    }

    @Test
    void testMatchResult3WayFullMatch() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .nameRu("Исход матча")
                .stakes(List.of(
                        DigitainStakeData.builder().id(11L).nameEn("1").nameRu("П1").factor(1.85).build(),
                        DigitainStakeData.builder().id(12L).nameEn("X").nameRu("Ничья").factor(3.60).build(),
                        DigitainStakeData.builder().id(13L).nameEn("2").nameRu("П2").factor(4.20).build()
                ))
                .build();

        assertTrue(matchResultHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        matchResultHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        OddItem w1 = items.get(0);
        assertEquals("11", w1.getFactorId());
        assertEquals(1.85, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        MatchResultBet b1 = (MatchResultBet) w1.getBetType();
        assertEquals(BetScope.FULL_MATCH, b1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, b1.outcome());
        assertEquals(StatType.MATCH, b1.statType());

        OddItem x = items.get(1);
        MatchResultBet bx = (MatchResultBet) x.getBetType();
        assertEquals(MatchResultBet.Outcome.DRAW, bx.outcome());

        OddItem w2 = items.get(2);
        MatchResultBet b2 = (MatchResultBet) w2.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN2, b2.outcome());
    }

    @Test
    void testMatchResult2WayMoneyline() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(702L)
                .nameEn("Winner")
                .nameRu("Победитель")
                .stakes(List.of(
                        DigitainStakeData.builder().id(21L).nameEn("Arsenal").factor(1.50).build(),
                        DigitainStakeData.builder().id(22L).nameEn("Chelsea").factor(2.60).build()
                ))
                .build();

        assertTrue(matchResultHandler.supports(group, SportType.BASKETBALL));

        List<OddItem> items = new ArrayList<>();
        matchResultHandler.handle(group, match, SportType.BASKETBALL, items);

        assertEquals(2, items.size());

        MatchResultBet b1 = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, b1.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, b1.outcome());

        MatchResultBet b2 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(BetScope.FULL_MATCH, b2.scope());
        assertEquals(MatchResultBet.Outcome.WIN2_2WAY, b2.outcome());
    }

    @Test
    void testHalfTimeMatchResult() {
        DigitainStakeGroupData group1 = DigitainStakeGroupData.builder()
                .id(4L)
                .nameEn("1st Half - 1X2")
                .nameRu("1-й тайм исход")
                .stakes(List.of(
                        DigitainStakeData.builder().id(31L).nameEn("1").factor(2.20).build(),
                        DigitainStakeData.builder().id(32L).nameEn("X").factor(2.10).build(),
                        DigitainStakeData.builder().id(33L).nameEn("2").factor(3.80).build()
                ))
                .build();

        assertTrue(matchResultHandler.supports(group1, SportType.FOOTBALL));

        List<OddItem> items1 = new ArrayList<>();
        matchResultHandler.handle(group1, match, SportType.FOOTBALL, items1);

        assertEquals(3, items1.size());
        assertEquals(BetScope.HALF_1, ((MatchResultBet) items1.get(0).getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) items1.get(0).getBetType()).outcome());

        DigitainStakeGroupData group2 = DigitainStakeGroupData.builder()
                .id(7L)
                .nameEn("2nd Half - 1X2")
                .nameRu("2-й тайм исход")
                .stakes(List.of(
                        DigitainStakeData.builder().id(41L).nameEn("1").factor(2.40).build(),
                        DigitainStakeData.builder().id(42L).nameEn("X").factor(2.20).build(),
                        DigitainStakeData.builder().id(43L).nameEn("2").factor(3.50).build()
                ))
                .build();

        assertTrue(matchResultHandler.supports(group2, SportType.FOOTBALL));

        List<OddItem> items2 = new ArrayList<>();
        matchResultHandler.handle(group2, match, SportType.FOOTBALL, items2);

        assertEquals(3, items2.size());
        assertEquals(BetScope.HALF_2, ((MatchResultBet) items2.get(0).getBetType()).scope());
    }

    @Test
    void testDoubleChanceFullMatch() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(992L)
                .nameEn("Double Chance")
                .nameRu("Двойной шанс")
                .stakes(List.of(
                        DigitainStakeData.builder().id(51L).nameEn("1X").factor(1.22).build(),
                        DigitainStakeData.builder().id(52L).nameEn("12").factor(1.30).build(),
                        DigitainStakeData.builder().id(53L).nameEn("X2").factor(1.95).build()
                ))
                .build();

        assertTrue(doubleChanceHandler.supports(group, SportType.FOOTBALL));
        assertFalse(matchResultHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        doubleChanceHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());

        MatchResultBet dc1x = (MatchResultBet) items.get(0).getBetType();
        assertEquals(BetScope.FULL_MATCH, dc1x.scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, dc1x.outcome());
        assertEquals(StatType.MATCH, dc1x.statType());

        MatchResultBet dc12 = (MatchResultBet) items.get(1).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_12, dc12.outcome());

        MatchResultBet dcX2 = (MatchResultBet) items.get(2).getBetType();
        assertEquals(MatchResultBet.Outcome.DC_X2, dcX2.outcome());
    }

    @Test
    void testDoubleChanceHalf1() {
        DigitainStakeGroupData group = DigitainStakeGroupData.builder()
                .id(993L)
                .nameEn("1st Half - Double Chance")
                .nameRu("1-й тайм - Двойной шанс")
                .stakes(List.of(
                        DigitainStakeData.builder().id(61L).nameRu("1X").factor(1.15).build(),
                        DigitainStakeData.builder().id(62L).nameRu("12").factor(1.45).build(),
                        DigitainStakeData.builder().id(63L).nameRu("Х2").factor(1.80).build()
                ))
                .build();

        assertTrue(doubleChanceHandler.supports(group, SportType.FOOTBALL));

        List<OddItem> items = new ArrayList<>();
        doubleChanceHandler.handle(group, match, SportType.FOOTBALL, items);

        assertEquals(3, items.size());
        assertEquals(BetScope.HALF_1, ((MatchResultBet) items.get(0).getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) items.get(0).getBetType()).outcome());
        assertEquals(BetScope.HALF_1, ((MatchResultBet) items.get(2).getBetType()).scope());
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) items.get(2).getBetType()).outcome());
    }

    @Test
    void testExclusionsAndInvalidFactors() {
        DigitainStakeGroupData cornerGroup = DigitainStakeGroupData.builder()
                .id(168L)
                .nameEn("Corners - 1X2")
                .stakes(List.of(DigitainStakeData.builder().nameEn("1").factor(1.90).build()))
                .build();

        assertFalse(matchResultHandler.supports(cornerGroup, SportType.FOOTBALL));
        assertFalse(doubleChanceHandler.supports(cornerGroup, SportType.FOOTBALL));

        DigitainStakeGroupData invalidFactorGroup = DigitainStakeGroupData.builder()
                .id(1L)
                .nameEn("Match Result")
                .stakes(List.of(
                        DigitainStakeData.builder().nameEn("1").factor(1.0).build(),
                        DigitainStakeData.builder().nameEn("X").factor(null).build(),
                        DigitainStakeData.builder().nameEn("2").factor(0.5).build()
                ))
                .build();

        List<OddItem> items = new ArrayList<>();
        matchResultHandler.handle(invalidFactorGroup, match, SportType.FOOTBALL, items);
        assertTrue(items.isEmpty());
    }
}
