package pro.datawiki.igaming.source.digitain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainMatchOddsData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import static org.junit.jupiter.api.Assertions.*;

class DigitainOddsMapperTest {

    private DigitainOddsMapper mapper;

    @BeforeEach
    void setUp() {
        SportNormalizationService sportNormalizationService = Mockito.mock(SportNormalizationService.class);
        Mockito.when(sportNormalizationService.normalize(Mockito.anyString())).thenReturn(SportType.FOOTBALL);
        mapper = new DigitainOddsMapper(sportNormalizationService);
    }

    @Test
    void testMapStandardMarkets_1X2TotalsHandicapBtts() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("5001");
        cache.setTeam1("Zenit");
        cache.setTeam2("Spartak");
        cache.setSportName("Football");
        cache.setLeagueName("RPL");
        cache.setStartTime(System.currentTimeMillis());
        cache.setIsLive(false);

        DigitainMatchOddsData oddsData = new DigitainMatchOddsData();
        oddsData.setMatchId(5001L);

        List<DigitainStakeGroupData> groups = new ArrayList<>();

        // Group 1: 1X2
        DigitainStakeGroupData g1X2 = new DigitainStakeGroupData();
        g1X2.setId(1L);
        g1X2.setNameEn("Match Result");
        g1X2.setNameRu("Исход матча");
        List<DigitainStakeData> s1X2 = List.of(
                DigitainStakeData.builder().id(101L).nameEn("Win1").nameRu("П1").factor(1.85).build(),
                DigitainStakeData.builder().id(102L).nameEn("Draw").nameRu("Ничья").factor(3.60).build(),
                DigitainStakeData.builder().id(103L).nameEn("Win2").nameRu("П2").factor(4.20).build()
        );
        g1X2.setStakes(s1X2);
        groups.add(g1X2);

        // Group 3: Total
        DigitainStakeGroupData gTotal = new DigitainStakeGroupData();
        gTotal.setId(3L);
        gTotal.setNameEn("Total");
        gTotal.setNameRu("Тотал");
        List<DigitainStakeData> sTotal = List.of(
                DigitainStakeData.builder().id(201L).nameEn("Over").nameRu("Больше").argument(2.5).factor(1.90).build(),
                DigitainStakeData.builder().id(202L).nameEn("Under").nameRu("Меньше").argument(2.5).factor(1.90).build()
        );
        gTotal.setStakes(sTotal);
        groups.add(gTotal);

        // Group 2: Handicap
        DigitainStakeGroupData gHcap = new DigitainStakeGroupData();
        gHcap.setId(2L);
        gHcap.setNameEn("Handicap");
        gHcap.setNameRu("Фора");
        List<DigitainStakeData> sHcap = List.of(
                DigitainStakeData.builder().id(301L).nameEn("Handicap 1").nameRu("Фора1").argument(-1.0).factor(2.40).build(),
                DigitainStakeData.builder().id(302L).nameEn("Handicap 2").nameRu("Фора2").argument(1.0).factor(1.58).build()
        );
        gHcap.setStakes(sHcap);
        groups.add(gHcap);

        // Group 46: BTTS
        DigitainStakeGroupData gBtts = new DigitainStakeGroupData();
        gBtts.setId(46L);
        gBtts.setNameEn("Both Teams To Score");
        gBtts.setNameRu("Обе забьют");
        List<DigitainStakeData> sBtts = List.of(
                DigitainStakeData.builder().id(401L).nameEn("Yes").nameRu("Да").factor(1.75).build(),
                DigitainStakeData.builder().id(402L).nameEn("No").nameRu("Нет").factor(2.05).build()
        );
        gBtts.setStakes(sBtts);
        groups.add(gBtts);

        oddsData.setGroups(groups);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, oddsData);
        assertNotNull(request);
        assertEquals("digitain", request.getBookmaker());
        assertEquals("5001", request.getExternalEventId());
        assertEquals(9, request.getOdds().size());

        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet mrb && mrb.outcome() == MatchResultBet.Outcome.WIN1));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet mrb && mrb.outcome() == MatchResultBet.Outcome.DRAW));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof MatchResultBet mrb && mrb.outcome() == MatchResultBet.Outcome.WIN2));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet tb && tb.direction() == TotalBet.Direction.OVER && tb.param() == 2.5));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof TotalBet tb && tb.direction() == TotalBet.Direction.UNDER && tb.param() == 2.5));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet hb && hb.outcome() == HandicapBet.Outcome.TEAM1 && hb.param() == -1.0));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof HandicapBet hb && hb.outcome() == HandicapBet.Outcome.TEAM2 && hb.param() == 1.0));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet bmb && bmb.outcome() == BinaryMarketBet.Outcome.YES));
        assertTrue(request.getOdds().stream().anyMatch(o -> o.getBetType() instanceof BinaryMarketBet bmb && bmb.outcome() == BinaryMarketBet.Outcome.NO));
    }

    @Test
    void testMapPeriodScopes_1stHalfAnd2ndHalf() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("5002");
        cache.setTeam1("CSKA");
        cache.setTeam2("Lokomotiv");
        cache.setSportName("Football");

        DigitainMatchOddsData oddsData = new DigitainMatchOddsData();
        oddsData.setMatchId(5002L);

        List<DigitainStakeGroupData> groups = new ArrayList<>();

        // 1st Half 1X2
        DigitainStakeGroupData h1Group = new DigitainStakeGroupData();
        h1Group.setId(1010L);
        h1Group.setNameEn("1st Half Result");
        h1Group.setNameRu("Исход 1-й тайм");
        h1Group.setStakes(List.of(
                DigitainStakeData.builder().id(11L).nameEn("Win1").nameRu("П1").factor(2.50).build(),
                DigitainStakeData.builder().id(12L).nameEn("Draw").nameRu("Ничья").factor(2.10).build(),
                DigitainStakeData.builder().id(13L).nameEn("Win2").nameRu("П2").factor(3.80).build()
        ));
        groups.add(h1Group);

        // 1st Half Total
        DigitainStakeGroupData h1Total = new DigitainStakeGroupData();
        h1Total.setId(1011L);
        h1Total.setNameEn("1st Half Total");
        h1Total.setNameRu("Тотал 1-й тайм");
        h1Total.setStakes(List.of(
                DigitainStakeData.builder().id(21L).nameEn("Over").nameRu("Больше").argument(1.0).factor(2.05).build(),
                DigitainStakeData.builder().id(22L).nameEn("Under").nameRu("Меньше").argument(1.0).factor(1.75).build()
        ));
        groups.add(h1Total);

        oddsData.setGroups(groups);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, oddsData);
        assertNotNull(request);
        assertEquals(5, request.getOdds().size());

        assertTrue(request.getOdds().stream().allMatch(o -> {
            if (o.getBetType() instanceof MatchResultBet mrb) {
                return mrb.scope() == BetScope.HALF_1;
            } else if (o.getBetType() instanceof TotalBet tb) {
                return tb.scope() == BetScope.HALF_1;
            }
            return false;
        }));
    }

    @Test
    void testMapTeamTotalsAndDoubleChanceAndDnbAndHtFt() {
        MatchCache cache = new MatchCache();
        cache.setExternalId("5003");
        cache.setTeam1("Real Madrid");
        cache.setTeam2("Barcelona");
        cache.setSportName("Football");

        DigitainMatchOddsData oddsData = new DigitainMatchOddsData();
        oddsData.setMatchId(5003L);

        List<DigitainStakeGroupData> groups = new ArrayList<>();

        // Team 1 Total
        DigitainStakeGroupData t1Total = new DigitainStakeGroupData();
        t1Total.setId(2001L);
        t1Total.setNameEn("Team 1 Total");
        t1Total.setNameRu("Индивидуальный тотал 1-й команды");
        t1Total.setStakes(List.of(
                DigitainStakeData.builder().id(51L).nameEn("Over").nameRu("Больше").argument(1.5).factor(1.80).build()
        ));
        groups.add(t1Total);

        // Double Chance
        DigitainStakeGroupData dcGroup = new DigitainStakeGroupData();
        dcGroup.setId(992L);
        dcGroup.setNameEn("Double Chance");
        dcGroup.setNameRu("Двойной шанс");
        dcGroup.setStakes(List.of(
                DigitainStakeData.builder().id(61L).nameEn("1X").nameRu("1X").factor(1.35).build()
        ));
        groups.add(dcGroup);

        // Draw No Bet
        DigitainStakeGroupData dnbGroup = new DigitainStakeGroupData();
        dnbGroup.setId(2002L);
        dnbGroup.setNameEn("Draw No Bet");
        dnbGroup.setNameRu("Ничья исключена");
        dnbGroup.setStakes(List.of(
                DigitainStakeData.builder().id(71L).nameEn("Win1").nameRu("П1").factor(1.55).build()
        ));
        groups.add(dnbGroup);

        // HT/FT
        DigitainStakeGroupData htftGroup = new DigitainStakeGroupData();
        htftGroup.setId(2003L);
        htftGroup.setNameEn("Half Time / Full Time");
        htftGroup.setNameRu("Тайм/Матч");
        htftGroup.setStakes(List.of(
                DigitainStakeData.builder().id(81L).nameEn("1/1").nameRu("П1/П1").factor(3.10).build()
        ));
        groups.add(htftGroup);

        oddsData.setGroups(groups);

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(cache, oddsData);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        // Verify Team Total
        assertTrue(request.getOdds().stream().anyMatch(o ->
                o.getBetType() instanceof TotalBet tb
                        && tb.subject() == BetSubject.TEAM1
                        && tb.direction() == TotalBet.Direction.OVER
                        && tb.param() == 1.5));

        // Verify Double Chance
        assertTrue(request.getOdds().stream().anyMatch(o ->
                o.getBetType() instanceof MatchResultBet mrb
                        && mrb.outcome() == MatchResultBet.Outcome.DC_1X));

        // Verify Draw No Bet
        assertTrue(request.getOdds().stream().anyMatch(o ->
                o.getBetType() instanceof HandicapBet hb
                        && hb.outcome() == HandicapBet.Outcome.TEAM1
                        && hb.param() == 0.0));

        // Verify HT/FT
        assertTrue(request.getOdds().stream().anyMatch(o ->
                o.getBetType() instanceof HalfTimeFullTimeBet htft
                        && htft.outcome() == HalfTimeFullTimeBet.Outcome.W1_W1));
    }
}
