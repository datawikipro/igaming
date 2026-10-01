package pro.datawiki.igaming.source.bet7k.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kEventDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kMarketDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Bet7kOddsMapperTest {

    private Bet7kOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new Bet7kOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("bet7k", SportType.FOOTBALL));
        assertTrue(mapper.supports("BET7K", SportType.ESPORTS));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-100")
                .sportName("Futebol")
                .leagueName("Brasileirão Série A")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .isLive(false)
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Resultado Final")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo").decimal(2.10).build(),
                                        Bet7kOutcomeDto.builder().name("Empate").decimal(3.30).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras").decimal(3.40).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("bet7k", request.getBookmaker());
        assertEquals("Flamengo", request.getTeam1());
        assertEquals("Palmeiras", request.getTeam2());
        assertEquals(SportType.FOOTBALL, request.getSportType());
        assertTrue(request.getRegions().contains(BookmakerRegion.LATAM));

        List<OddItem> odds = request.getOdds();
        assertEquals(3, odds.size());

        OddItem w1 = odds.stream().filter(o -> "Flamengo".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(2.10, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) w1.getBetType()).outcome());

        OddItem draw = odds.stream().filter(o -> "Empate".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(3.30, draw.getValue());
        assertTrue(draw.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) draw.getBetType()).outcome());

        OddItem w2 = odds.stream().filter(o -> "Palmeiras".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(3.40, w2.getValue());
        assertTrue(w2.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN2, ((MatchResultBet) w2.getBetType()).outcome());
    }

    @Test
    void testDoubleChance() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-101")
                .sportName("Soccer")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Dupla Chance")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("1X").decimal(1.35).build(),
                                        Bet7kOutcomeDto.builder().name("12").decimal(1.30).build(),
                                        Bet7kOutcomeDto.builder().name("X2").decimal(1.65).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream().filter(o -> "1X".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.35, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1X.getBetType()).outcome());
    }

    @Test
    void testTotals() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-102")
                .sportName("Futebol")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Total de Gols Mais/Menos")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 2.5").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 2.5").decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().stream().filter(o -> o.getName().contains("Mais")).findFirst().orElseThrow();
        assertEquals(1.85, over.getValue());
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet tbOver = (TotalBet) over.getBetType();
        assertEquals(2.5, tbOver.param());
        assertEquals(TotalBet.Direction.OVER, tbOver.direction());
        assertEquals(BetSubject.MATCH, tbOver.subject());

        OddItem under = request.getOdds().stream().filter(o -> o.getName().contains("Menos")).findFirst().orElseThrow();
        assertEquals(1.95, under.getValue());
        assertTrue(under.getBetType() instanceof TotalBet);
        TotalBet tbUnder = (TotalBet) under.getBetType();
        assertEquals(2.5, tbUnder.param());
        assertEquals(TotalBet.Direction.UNDER, tbUnder.direction());
    }

    @Test
    void testTeamTotals() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-103")
                .sportName("Soccer")
                .homeTeam("Liverpool")
                .awayTeam("Manchester City")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Total Casa - Mais/Menos")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais 1.5").decimal(1.70).build(),
                                        Bet7kOutcomeDto.builder().name("Menos 1.5").decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().stream().filter(o -> o.getName().contains("Mais")).findFirst().orElseThrow();
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet tb = (TotalBet) over.getBetType();
        assertEquals(BetSubject.TEAM1, tb.subject());
        assertEquals(1.5, tb.param());
    }

    @Test
    void testHandicap() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-104")
                .sportName("Basquete")
                .homeTeam("LA Lakers")
                .awayTeam("Golden State")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Handicap Asiatico")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("LA Lakers (-4.5)").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Golden State (+4.5)").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().stream().filter(o -> o.getName().contains("Lakers")).findFirst().orElseThrow();
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-4.5, hb1.param());
        assertTrue(hb1.isAsian());
    }

    @Test
    void testScopeResolutionFirstHalf() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-105")
                .sportName("Futebol")
                .homeTeam("Santos")
                .awayTeam("Corinthians")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("1° Tempo - Resultado")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Santos").decimal(2.50).build(),
                                        Bet7kOutcomeDto.builder().name("Empate").decimal(2.00).build(),
                                        Bet7kOutcomeDto.builder().name("Corinthians").decimal(3.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem w1 = request.getOdds().stream().filter(o -> "Santos".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        MatchResultBet mrb = (MatchResultBet) w1.getBetType();
        assertEquals(BetScope.HALF_1, mrb.scope());
    }

    @Test
    void testFallbackTitleParsing() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-106")
                .name("Inter Milan vs AC Milan")
                .sportName("Futebol")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("1X2")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Inter Milan").decimal(2.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("Inter Milan", request.getTeam1());
        assertEquals("AC Milan", request.getTeam2());
    }

    @Test
    void testNullAndEmptyInputs() {
        assertNull(mapper.mapToOddsUpdateRequest(null));

        Bet7kEventDto empty = new Bet7kEventDto();
        assertNull(mapper.mapToOddsUpdateRequest(empty));

        Bet7kEventDto onlyId = Bet7kEventDto.builder().id("123").build();
        assertNull(mapper.mapToOddsUpdateRequest(onlyId));
    }

    @Test
    void testCs2EsportsMarkets() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("cs2-100")
                .sportName("CS2")
                .leagueName("ESL Pro League")
                .homeTeam("Natus Vincere")
                .awayTeam("FaZe Clan")
                .isLive(false)
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Vencedor da Partida")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Natus Vincere").decimal(1.75).build(),
                                        Bet7kOutcomeDto.builder().name("FaZe Clan").decimal(2.10).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Total de Mapas")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 2.5").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 2.5").decimal(1.80).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Handicap de Mapas")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Natus Vincere (-1.5)").decimal(2.60).build(),
                                        Bet7kOutcomeDto.builder().name("FaZe Clan (+1.5)").decimal(1.45).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Mapa - Vencedor")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Natus Vincere").decimal(1.80).build(),
                                        Bet7kOutcomeDto.builder().name("FaZe Clan").decimal(1.95).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Mapa - Total de Rounds")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais 21.5").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Menos 21.5").decimal(1.85).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Mapa - Handicap de Rodadas")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Natus Vincere (-2.5)").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("FaZe Clan (+2.5)").decimal(1.80).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Mapa - Primeiro Sangue")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Natus Vincere").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("FaZe Clan").decimal(1.85).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.CS2, request.getSportType());
        assertEquals("Natus Vincere", request.getTeam1());
        assertEquals("FaZe Clan", request.getTeam2());

        List<OddItem> odds = request.getOdds();
        assertEquals(14, odds.size());

        // Match Winner
        OddItem w1 = odds.stream().filter(o -> "moneyline".equals(o.getGroupName()) && o.getName().contains("Natus")).findFirst().orElseThrow();
        assertEquals(1.75, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        MatchResultBet mrb1 = (MatchResultBet) w1.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, mrb1.outcome());
        assertEquals(BetScope.FULL_MATCH, mrb1.scope());
        assertEquals(StatType.MATCH, mrb1.statType());

        // Total Maps
        OddItem mapOver = odds.stream().filter(o -> "maps_total".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        assertTrue(mapOver.getBetType() instanceof TotalBet);
        TotalBet tbMaps = (TotalBet) mapOver.getBetType();
        assertEquals(2.5, tbMaps.param());
        assertEquals(StatType.MAPS, tbMaps.statType());
        assertEquals(TotalBet.Direction.OVER, tbMaps.direction());

        // Map Handicap
        OddItem mapHcp1 = odds.stream().filter(o -> "maps_handicap".equals(o.getGroupName()) && o.getName().contains("Natus")).findFirst().orElseThrow();
        assertTrue(mapHcp1.getBetType() instanceof HandicapBet);
        HandicapBet hbMaps = (HandicapBet) mapHcp1.getBetType();
        assertEquals(-1.5, hbMaps.param());
        assertEquals(StatType.MAPS, hbMaps.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, hbMaps.outcome());

        // 1st Map Winner
        OddItem map1W1 = odds.stream().filter(o -> "map_winner_map_1".equals(o.getGroupName()) && o.getName().contains("Natus")).findFirst().orElseThrow();
        assertTrue(map1W1.getBetType() instanceof MatchResultBet);
        MatchResultBet m1mrb = (MatchResultBet) map1W1.getBetType();
        assertEquals(BetScope.MAP_1, m1mrb.scope());
        assertEquals(MatchResultBet.Outcome.WIN1_2WAY, m1mrb.outcome());

        // 1st Map Total Rounds
        OddItem roundsOver = odds.stream().filter(o -> "rounds_total_map_1".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        assertTrue(roundsOver.getBetType() instanceof TotalBet);
        TotalBet tbRounds = (TotalBet) roundsOver.getBetType();
        assertEquals(BetScope.MAP_1, tbRounds.scope());
        assertEquals(21.5, tbRounds.param());
        assertEquals(StatType.ROUNDS, tbRounds.statType());

        // 1st Map Round Handicap
        OddItem roundHcp = odds.stream().filter(o -> "rounds_handicap_map_1".equals(o.getGroupName()) && o.getName().contains("Natus")).findFirst().orElseThrow();
        assertTrue(roundHcp.getBetType() instanceof HandicapBet);
        HandicapBet hbRounds = (HandicapBet) roundHcp.getBetType();
        assertEquals(BetScope.MAP_1, hbRounds.scope());
        assertEquals(-2.5, hbRounds.param());
        assertEquals(StatType.ROUNDS, hbRounds.statType());

        // 1st Map First Blood
        OddItem fb = odds.stream().filter(o -> "first_blood_map_1".equals(o.getGroupName()) && o.getName().contains("Natus")).findFirst().orElseThrow();
        assertTrue(fb.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmb = (BinaryMarketBet) fb.getBetType();
        assertEquals(BetScope.MAP_1, bmb.scope());
        assertEquals(BetSubject.TEAM1, bmb.subject());
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, bmb.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bmb.outcome());
        assertEquals(StatType.FIRST_BLOOD, bmb.statType());
    }

    @Test
    void testDota2EsportsMarkets() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("dota-200")
                .sportName("Dota 2")
                .leagueName("The International")
                .homeTeam("Team Spirit")
                .awayTeam("Team Liquid")
                .isLive(false)
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Match Winner")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Team Spirit").decimal(1.65).build(),
                                        Bet7kOutcomeDto.builder().name("Team Liquid").decimal(2.20).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Mapa 1 - Vencedor")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Team Spirit").decimal(1.70).build(),
                                        Bet7kOutcomeDto.builder().name("Team Liquid").decimal(2.10).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Mapa 1 - Total de Abates")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 48.5").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 48.5").decimal(1.85).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Mapa 1 - Handicap de Kills")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Team Spirit (-8.5)").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Team Liquid (+8.5)").decimal(1.80).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Primeiro Sangue")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Team Spirit").decimal(1.83).build(),
                                        Bet7kOutcomeDto.builder().name("Team Liquid").decimal(1.87).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.DOTA2, request.getSportType());

        List<OddItem> odds = request.getOdds();
        assertEquals(10, odds.size());

        // Match Winner
        OddItem w1 = odds.stream().filter(o -> "moneyline".equals(o.getGroupName()) && o.getName().contains("Spirit")).findFirst().orElseThrow();
        assertEquals(1.65, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);

        // Map 1 Winner
        OddItem m1w = odds.stream().filter(o -> "map_winner_map_1".equals(o.getGroupName()) && o.getName().contains("Spirit")).findFirst().orElseThrow();
        assertTrue(m1w.getBetType() instanceof MatchResultBet);
        assertEquals(BetScope.MAP_1, ((MatchResultBet) m1w.getBetType()).scope());

        // Map 1 Total Kills
        OddItem killsOver = odds.stream().filter(o -> "kills_total_map_1".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        assertTrue(killsOver.getBetType() instanceof TotalBet);
        TotalBet tbKills = (TotalBet) killsOver.getBetType();
        assertEquals(BetScope.MAP_1, tbKills.scope());
        assertEquals(48.5, tbKills.param());
        assertEquals(StatType.KILLS, tbKills.statType());

        // Map 1 Kill Handicap
        OddItem killsHcp = odds.stream().filter(o -> "kills_handicap_map_1".equals(o.getGroupName()) && o.getName().contains("Spirit")).findFirst().orElseThrow();
        assertTrue(killsHcp.getBetType() instanceof HandicapBet);
        HandicapBet hbKills = (HandicapBet) killsHcp.getBetType();
        assertEquals(BetScope.MAP_1, hbKills.scope());
        assertEquals(-8.5, hbKills.param());
        assertEquals(StatType.KILLS, hbKills.statType());

        // Full Match First Blood
        OddItem fb = odds.stream().filter(o -> "first_blood".equals(o.getGroupName()) && o.getName().contains("Liquid")).findFirst().orElseThrow();
        assertTrue(fb.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmb = (BinaryMarketBet) fb.getBetType();
        assertEquals(BetScope.FULL_MATCH, bmb.scope());
        assertEquals(BetSubject.TEAM2, bmb.subject());
        assertEquals(StatType.FIRST_BLOOD, bmb.statType());
    }

    @Test
    void testLeagueOfLegendsEsportsMarkets() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("lol-300")
                .sportName("League of Legends")
                .leagueName("Worlds 2024")
                .homeTeam("T1")
                .awayTeam("Gen.G")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Resultado Final")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("T1").decimal(1.95).build(),
                                        Bet7kOutcomeDto.builder().name("Gen.G").decimal(1.80).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Total de Mapas Mais/Menos")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais 3.5").decimal(1.40).build(),
                                        Bet7kOutcomeDto.builder().name("Menos 3.5").decimal(2.70).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Map 1 Winner")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("T1").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Gen.G").decimal(1.85).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1° Mapa - Total Kills")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Over 26.5").decimal(1.80).build(),
                                        Bet7kOutcomeDto.builder().name("Under 26.5").decimal(1.90).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1st Map First Blood")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("T1").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Gen.G").decimal(1.85).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.LEAGUE_OF_LEGENDS, request.getSportType());

        List<OddItem> odds = request.getOdds();
        assertEquals(10, odds.size());

        // LoL Total Maps
        OddItem totalMaps = odds.stream().filter(o -> "maps_total".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        assertEquals(3.5, ((TotalBet) totalMaps.getBetType()).param());
        assertEquals(StatType.MAPS, ((TotalBet) totalMaps.getBetType()).statType());

        // LoL Map 1 Kills Total
        OddItem killsTotal = odds.stream().filter(o -> "kills_total_map_1".equals(o.getGroupName()) && o.getName().contains("Over")).findFirst().orElseThrow();
        assertEquals(26.5, ((TotalBet) killsTotal.getBetType()).param());
        assertEquals(StatType.KILLS, ((TotalBet) killsTotal.getBetType()).statType());

        // LoL Map 1 First Blood
        OddItem fb = odds.stream().filter(o -> "first_blood_map_1".equals(o.getGroupName()) && o.getName().contains("T1")).findFirst().orElseThrow();
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, ((BinaryMarketBet) fb.getBetType()).marketType());
        assertEquals(BetScope.MAP_1, ((BinaryMarketBet) fb.getBetType()).scope());
    }

    @Test
    void testValorantEsportsMarkets() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("val-400")
                .sportName("Valorant")
                .leagueName("VCT Champions")
                .homeTeam("Sentinels")
                .awayTeam("Fnatic")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Vencedor")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Sentinels").decimal(2.10).build(),
                                        Bet7kOutcomeDto.builder().name("Fnatic").decimal(1.68).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Total de Mapas")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais 2.5").decimal(2.00).build(),
                                        Bet7kOutcomeDto.builder().name("Menos 2.5").decimal(1.72).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Handicap Asiatico de Mapas")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Sentinels (+1.5)").decimal(1.40).build(),
                                        Bet7kOutcomeDto.builder().name("Fnatic (-1.5)").decimal(2.75).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Mapa 2 - Total de Rodadas")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Acima de 21.5").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Abaixo de 21.5").decimal(1.85).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Mapa 2 - Handicap de Rounds")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Sentinels (+2.5)").decimal(1.80).build(),
                                        Bet7kOutcomeDto.builder().name("Fnatic (-2.5)").decimal(1.90).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Abate - Mapa 2")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Sentinels").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Fnatic").decimal(1.85).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.VALORANT, request.getSportType());

        List<OddItem> odds = request.getOdds();
        assertEquals(12, odds.size());

        // Map 2 Total Rounds
        OddItem rTot = odds.stream().filter(o -> "rounds_total_map_2".equals(o.getGroupName()) && o.getName().contains("Acima")).findFirst().orElseThrow();
        assertEquals(21.5, ((TotalBet) rTot.getBetType()).param());
        assertEquals(BetScope.MAP_2, ((TotalBet) rTot.getBetType()).scope());
        assertEquals(StatType.ROUNDS, ((TotalBet) rTot.getBetType()).statType());

        // Map 2 Round Handicap
        OddItem rHcp = odds.stream().filter(o -> "rounds_handicap_map_2".equals(o.getGroupName()) && o.getName().contains("Fnatic")).findFirst().orElseThrow();
        assertEquals(-2.5, ((HandicapBet) rHcp.getBetType()).param());
        assertEquals(BetScope.MAP_2, ((HandicapBet) rHcp.getBetType()).scope());
        assertEquals(StatType.ROUNDS, ((HandicapBet) rHcp.getBetType()).statType());

        // Map 2 First Blood (1º Abate)
        OddItem fb = odds.stream().filter(o -> "first_blood_map_2".equals(o.getGroupName()) && o.getName().contains("Sentinels")).findFirst().orElseThrow();
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, ((BinaryMarketBet) fb.getBetType()).marketType());
        assertEquals(BetScope.MAP_2, ((BinaryMarketBet) fb.getBetType()).scope());
    }

    @Test
    void testEsportsDoubleChanceAndDrawNoBet() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("bo2-500")
                .sportName("Dota 2")
                .homeTeam("Team Secret")
                .awayTeam("OG")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Resultado Final")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Team Secret").decimal(2.40).build(),
                                        Bet7kOutcomeDto.builder().name("Empate").decimal(2.10).build(),
                                        Bet7kOutcomeDto.builder().name("OG").decimal(3.50).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Dupla Chance")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("1X").decimal(1.30).build(),
                                        Bet7kOutcomeDto.builder().name("12").decimal(1.60).build(),
                                        Bet7kOutcomeDto.builder().name("X2").decimal(1.50).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);

        List<OddItem> odds = request.getOdds();
        assertEquals(6, odds.size());

        OddItem draw = odds.stream().filter(o -> "moneyline".equals(o.getGroupName()) && "Empate".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) draw.getBetType()).outcome());

        OddItem dc1X = odds.stream().filter(o -> "double_chance".equals(o.getGroupName()) && "1X".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1X.getBetType()).outcome());
    }

    @Test
    void testCornersTotalsMatchAndHalvesAndTeamAndAsian() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-corners-1")
                .sportName("Futebol")
                .leagueName("Brasileirão")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Total de Escanteios")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 9.5").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 9.5").decimal(1.95).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Tempo - Total de Escanteios")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 4.5").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 4.5").decimal(1.80).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Total de Escanteios - Flamengo")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 5.5").decimal(1.75).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 5.5").decimal(2.05).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Total Asiático de Escanteios")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 9.25").decimal(1.80).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 9.25").decimal(2.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(8, request.getOdds().size());

        OddItem over95 = request.getOdds().stream()
                .filter(o -> "corners_total".equals(o.getGroupName()) && o.getName().contains("Mais de 9.5"))
                .findFirst().orElseThrow();
        assertEquals(1.85, over95.getValue());
        assertTrue(over95.getBetType() instanceof TotalBet);
        TotalBet tbOver = (TotalBet) over95.getBetType();
        assertEquals(TotalBet.Direction.OVER, tbOver.direction());
        assertEquals(9.5, tbOver.param());
        assertEquals(BetScope.FULL_MATCH, tbOver.scope());
        assertEquals(BetSubject.MATCH, tbOver.subject());
        assertEquals(StatType.CORNERS, tbOver.statType());
        assertFalse(tbOver.isAsian());

        OddItem ht1Over = request.getOdds().stream()
                .filter(o -> "corners_total_half_1".equals(o.getGroupName()) && o.getName().contains("Mais de 4.5"))
                .findFirst().orElseThrow();
        assertEquals(1.90, ht1Over.getValue());
        TotalBet tbHt1 = (TotalBet) ht1Over.getBetType();
        assertEquals(BetScope.HALF_1, tbHt1.scope());
        assertEquals(StatType.CORNERS, tbHt1.statType());

        OddItem team1Over = request.getOdds().stream()
                .filter(o -> "corners_total_team1".equals(o.getGroupName()))
                .findFirst().orElseThrow();
        assertEquals(1.75, team1Over.getValue());
        TotalBet tbTeam1 = (TotalBet) team1Over.getBetType();
        assertEquals(BetSubject.TEAM1, tbTeam1.subject());
        assertEquals(StatType.CORNERS, tbTeam1.statType());

        OddItem asianOver = request.getOdds().stream()
                .filter(o -> "corners_total".equals(o.getGroupName()) && o.getName().contains("9.25"))
                .findFirst().orElseThrow();
        TotalBet tbAsian = (TotalBet) asianOver.getBetType();
        assertTrue(tbAsian.isAsian());
        assertEquals(9.25, tbAsian.param());
        assertEquals(StatType.CORNERS, tbAsian.statType());
    }

    @Test
    void testCornersHandicapMatchAndHalves() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-corners-2")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Handicap de Escanteios")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo (-1.5)").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras (+1.5)").decimal(1.90).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Tempo - Handicap Asiático de Escanteios")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo (-0.75)").decimal(2.05).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras (+0.75)").decimal(1.75).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem h1 = request.getOdds().stream()
                .filter(o -> "corners_handicap".equals(o.getGroupName()) && o.getName().contains("Flamengo"))
                .findFirst().orElseThrow();
        assertEquals(1.90, h1.getValue());
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-1.5, hb1.param());
        assertEquals(BetScope.FULL_MATCH, hb1.scope());
        assertEquals(StatType.CORNERS, hb1.statType());
        assertFalse(hb1.isAsian());

        OddItem h2 = request.getOdds().stream()
                .filter(o -> "corners_handicap".equals(o.getGroupName()) && o.getName().contains("Palmeiras"))
                .findFirst().orElseThrow();
        HandicapBet hb2 = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(1.5, hb2.param());

        OddItem ht1Asian = request.getOdds().stream()
                .filter(o -> "corners_asian_handicap_half_1".equals(o.getGroupName()) && o.getName().contains("Flamengo"))
                .findFirst().orElseThrow();
        HandicapBet hbAsian = (HandicapBet) ht1Asian.getBetType();
        assertTrue(hbAsian.isAsian());
        assertEquals(-0.75, hbAsian.param());
        assertEquals(BetScope.HALF_1, hbAsian.scope());
        assertEquals(StatType.CORNERS, hbAsian.statType());
    }

    @Test
    void testCorners1X2AndFirstLastCorner() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-corners-3")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Quem terá mais escanteios")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo").decimal(1.65).build(),
                                        Bet7kOutcomeDto.builder().name("Empate").decimal(7.50).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras").decimal(2.40).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Primeiro Escanteio")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo").decimal(1.70).build(),
                                        Bet7kOutcomeDto.builder().name("Nenhum").decimal(25.0).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras").decimal(2.10).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Último Escanteio")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo").decimal(1.80).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras").decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(8, request.getOdds().size());

        OddItem mostW1 = request.getOdds().stream()
                .filter(o -> "corners_1x2".equals(o.getGroupName()) && "Flamengo".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.65, mostW1.getValue());
        assertTrue(mostW1.getBetType() instanceof MatchResultBet);
        MatchResultBet mb1 = (MatchResultBet) mostW1.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, mb1.outcome());
        assertEquals(StatType.CORNERS, mb1.statType());

        OddItem mostX = request.getOdds().stream()
                .filter(o -> "corners_1x2".equals(o.getGroupName()) && "Empate".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) mostX.getBetType()).outcome());

        OddItem firstFlamengo = request.getOdds().stream()
                .filter(o -> "corners_first".equals(o.getGroupName()) && "Flamengo".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.70, firstFlamengo.getValue());
        assertTrue(firstFlamengo.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbFirst = (BinaryMarketBet) firstFlamengo.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_CORNER, bmbFirst.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmbFirst.outcome());
        assertEquals(StatType.CORNERS, bmbFirst.statType());

        OddItem firstNone = request.getOdds().stream()
                .filter(o -> "corners_first".equals(o.getGroupName()) && "Nenhum".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(BinaryMarketBet.Outcome.NO, ((BinaryMarketBet) firstNone.getBetType()).outcome());

        OddItem lastPalmeiras = request.getOdds().stream()
                .filter(o -> "corners_last".equals(o.getGroupName()) && "Palmeiras".equals(o.getName()))
                .findFirst().orElseThrow();
        BinaryMarketBet bmbLast = (BinaryMarketBet) lastPalmeiras.getBetType();
        assertEquals(BinaryMarketBet.MarketType.LAST_CORNER, bmbLast.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM2, bmbLast.outcome());
    }

    @Test
    void testCornersDoubleChanceDrawNoBetOddEven() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-corners-4")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Dupla Chance de Escanteios")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("1X").decimal(1.25).build(),
                                        Bet7kOutcomeDto.builder().name("12").decimal(1.40).build(),
                                        Bet7kOutcomeDto.builder().name("X2").decimal(1.60).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Empate Anula Aposta - Escanteios")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo").decimal(1.45).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras").decimal(2.65).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Escanteios Par/Ímpar")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Par").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Ímpar").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(7, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream()
                .filter(o -> "corners_double_chance".equals(o.getGroupName()) && "1X".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.25, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        MatchResultBet mbDc = (MatchResultBet) dc1X.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, mbDc.outcome());
        assertEquals(StatType.CORNERS, mbDc.statType());

        OddItem dnb1 = request.getOdds().stream()
                .filter(o -> "corners_draw_no_bet".equals(o.getGroupName()) && "Flamengo".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.45, dnb1.getValue());
        assertTrue(dnb1.getBetType() instanceof HandicapBet);
        HandicapBet hbDnb = (HandicapBet) dnb1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hbDnb.outcome());
        assertEquals(0.0, hbDnb.param());
        assertEquals(StatType.CORNERS, hbDnb.statType());

        OddItem oddCorners = request.getOdds().stream()
                .filter(o -> "corners_odd_even".equals(o.getGroupName()) && "Ímpar".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.90, oddCorners.getValue());
        assertTrue(oddCorners.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbOdd = (BinaryMarketBet) oddCorners.getBetType();
        assertEquals(BinaryMarketBet.MarketType.ODD_EVEN, bmbOdd.marketType());
        assertEquals(BinaryMarketBet.Outcome.ODD, bmbOdd.outcome());
        assertEquals(StatType.CORNERS, bmbOdd.statType());
    }

    @Test
    void testCardsTotalsMatchAndHalvesAndTeamAndAsian() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-cards-1")
                .sportName("Futebol")
                .leagueName("Brasileirão")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Total de Cartões Amarelos")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 4.5").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 4.5").decimal(1.95).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Tempo - Total de Cartões Amarelos")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 1.5").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 1.5").decimal(1.80).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Total de Cartões Amarelos - Flamengo")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 2.5").decimal(1.75).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 2.5").decimal(2.05).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Total de Cartões Amarelos - Palmeiras")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 2.5").decimal(1.80).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 2.5").decimal(1.90).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Total Asiático de Cartões")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais de 4.25").decimal(1.80).build(),
                                        Bet7kOutcomeDto.builder().name("Menos de 4.25").decimal(2.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(10, request.getOdds().size());

        // Match total
        OddItem over45 = request.getOdds().stream()
                .filter(o -> "cards_total".equals(o.getGroupName()) && o.getName().contains("Mais de 4.5"))
                .findFirst().orElseThrow();
        assertEquals(1.85, over45.getValue());
        assertTrue(over45.getBetType() instanceof TotalBet);
        TotalBet tbOver = (TotalBet) over45.getBetType();
        assertEquals(TotalBet.Direction.OVER, tbOver.direction());
        assertEquals(4.5, tbOver.param());
        assertEquals(BetScope.FULL_MATCH, tbOver.scope());
        assertEquals(BetSubject.MATCH, tbOver.subject());
        assertEquals(StatType.YELLOW_CARDS, tbOver.statType());
        assertFalse(tbOver.isAsian());

        // 1st Half total
        OddItem ht1Over = request.getOdds().stream()
                .filter(o -> "cards_total_half_1".equals(o.getGroupName()) && o.getName().contains("Mais de 1.5"))
                .findFirst().orElseThrow();
        assertEquals(1.90, ht1Over.getValue());
        TotalBet tbHt1 = (TotalBet) ht1Over.getBetType();
        assertEquals(BetScope.HALF_1, tbHt1.scope());
        assertEquals(StatType.YELLOW_CARDS, tbHt1.statType());

        // Team 1 total
        OddItem team1Over = request.getOdds().stream()
                .filter(o -> "cards_total_team1".equals(o.getGroupName()) && o.getName().contains("Mais de 2.5"))
                .findFirst().orElseThrow();
        assertEquals(1.75, team1Over.getValue());
        TotalBet tbTeam1 = (TotalBet) team1Over.getBetType();
        assertEquals(BetSubject.TEAM1, tbTeam1.subject());
        assertEquals(StatType.YELLOW_CARDS, tbTeam1.statType());

        // Team 2 total
        OddItem team2Over = request.getOdds().stream()
                .filter(o -> "cards_total_team2".equals(o.getGroupName()) && o.getName().contains("Mais de 2.5"))
                .findFirst().orElseThrow();
        assertEquals(1.80, team2Over.getValue());
        TotalBet tbTeam2 = (TotalBet) team2Over.getBetType();
        assertEquals(BetSubject.TEAM2, tbTeam2.subject());
        assertEquals(StatType.YELLOW_CARDS, tbTeam2.statType());

        // Asian total
        OddItem asianOver = request.getOdds().stream()
                .filter(o -> "cards_total".equals(o.getGroupName()) && o.getName().contains("4.25"))
                .findFirst().orElseThrow();
        TotalBet tbAsian = (TotalBet) asianOver.getBetType();
        assertTrue(tbAsian.isAsian());
        assertEquals(4.25, tbAsian.param());
        assertEquals(StatType.YELLOW_CARDS, tbAsian.statType());
    }

    @Test
    void testCardsHandicapMatchAndHalves() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-cards-2")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Handicap de Cartões")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo (-0.5)").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras (+0.5)").decimal(1.90).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Tempo - Handicap Asiático de Cartões")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo (-0.25)").decimal(2.05).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras (+0.25)").decimal(1.75).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem h1 = request.getOdds().stream()
                .filter(o -> "cards_handicap".equals(o.getGroupName()) && o.getName().contains("Flamengo"))
                .findFirst().orElseThrow();
        assertEquals(1.90, h1.getValue());
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-0.5, hb1.param());
        assertEquals(BetScope.FULL_MATCH, hb1.scope());
        assertEquals(StatType.YELLOW_CARDS, hb1.statType());
        assertFalse(hb1.isAsian());

        OddItem h2 = request.getOdds().stream()
                .filter(o -> "cards_handicap".equals(o.getGroupName()) && o.getName().contains("Palmeiras"))
                .findFirst().orElseThrow();
        HandicapBet hb2 = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(0.5, hb2.param());

        OddItem ht1Asian = request.getOdds().stream()
                .filter(o -> "cards_asian_handicap_half_1".equals(o.getGroupName()) && o.getName().contains("Flamengo"))
                .findFirst().orElseThrow();
        HandicapBet hbAsian = (HandicapBet) ht1Asian.getBetType();
        assertTrue(hbAsian.isAsian());
        assertEquals(-0.25, hbAsian.param());
        assertEquals(BetScope.HALF_1, hbAsian.scope());
        assertEquals(StatType.YELLOW_CARDS, hbAsian.statType());
    }

    @Test
    void testCards1X2AndRedCardAndFirstLastCard() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-cards-3")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Quem terá mais cartões")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo").decimal(1.80).build(),
                                        Bet7kOutcomeDto.builder().name("Empate").decimal(4.50).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras").decimal(2.20).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Cartão Vermelho")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Sim").decimal(3.20).build(),
                                        Bet7kOutcomeDto.builder().name("Não").decimal(1.30).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Primeiro Cartão")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo").decimal(1.75).build(),
                                        Bet7kOutcomeDto.builder().name("Nenhum").decimal(15.0).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras").decimal(2.05).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Último Cartão")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras").decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(10, request.getOdds().size());

        // 1X2 Most cards
        OddItem mostW1 = request.getOdds().stream()
                .filter(o -> "cards_1x2".equals(o.getGroupName()) && "Flamengo".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.80, mostW1.getValue());
        assertTrue(mostW1.getBetType() instanceof MatchResultBet);
        MatchResultBet mb1 = (MatchResultBet) mostW1.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, mb1.outcome());
        assertEquals(StatType.YELLOW_CARDS, mb1.statType());

        OddItem mostX = request.getOdds().stream()
                .filter(o -> "cards_1x2".equals(o.getGroupName()) && "Empate".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(MatchResultBet.Outcome.DRAW, ((MatchResultBet) mostX.getBetType()).outcome());

        // Red card Yes/No
        OddItem redSim = request.getOdds().stream()
                .filter(o -> "cards_red_card".equals(o.getGroupName()) && "Sim".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(3.20, redSim.getValue());
        assertTrue(redSim.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbRed = (BinaryMarketBet) redSim.getBetType();
        assertEquals(BinaryMarketBet.MarketType.RED_CARD, bmbRed.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bmbRed.outcome());
        assertEquals(StatType.YELLOW_CARDS, bmbRed.statType());

        OddItem redNao = request.getOdds().stream()
                .filter(o -> "cards_red_card".equals(o.getGroupName()) && "Não".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.30, redNao.getValue());
        assertEquals(BinaryMarketBet.Outcome.NO, ((BinaryMarketBet) redNao.getBetType()).outcome());

        // First Card
        OddItem firstFlamengo = request.getOdds().stream()
                .filter(o -> "cards_first".equals(o.getGroupName()) && "Flamengo".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.75, firstFlamengo.getValue());
        assertTrue(firstFlamengo.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbFirst = (BinaryMarketBet) firstFlamengo.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_CARD, bmbFirst.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmbFirst.outcome());
        assertEquals(StatType.YELLOW_CARDS, bmbFirst.statType());

        OddItem firstNone = request.getOdds().stream()
                .filter(o -> "cards_first".equals(o.getGroupName()) && "Nenhum".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(BinaryMarketBet.Outcome.NO, ((BinaryMarketBet) firstNone.getBetType()).outcome());

        // Last Card
        OddItem lastPalmeiras = request.getOdds().stream()
                .filter(o -> "cards_last".equals(o.getGroupName()) && "Palmeiras".equals(o.getName()))
                .findFirst().orElseThrow();
        BinaryMarketBet bmbLast = (BinaryMarketBet) lastPalmeiras.getBetType();
        assertEquals(BinaryMarketBet.MarketType.LAST_CARD, bmbLast.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM2, bmbLast.outcome());
    }

    @Test
    void testCardsDoubleChanceDrawNoBetOddEven() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-cards-4")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Dupla Chance de Cartões")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("1X").decimal(1.25).build(),
                                        Bet7kOutcomeDto.builder().name("12").decimal(1.40).build(),
                                        Bet7kOutcomeDto.builder().name("X2").decimal(1.60).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Empate Anula Aposta - Cartões")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Flamengo").decimal(1.50).build(),
                                        Bet7kOutcomeDto.builder().name("Palmeiras").decimal(2.50).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Cartões Par/Ímpar")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Par").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Ímpar").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(7, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream()
                .filter(o -> "cards_double_chance".equals(o.getGroupName()) && "1X".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.25, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        MatchResultBet mbDc = (MatchResultBet) dc1X.getBetType();
        assertEquals(MatchResultBet.Outcome.DC_1X, mbDc.outcome());
        assertEquals(StatType.YELLOW_CARDS, mbDc.statType());

        OddItem dnb1 = request.getOdds().stream()
                .filter(o -> "cards_draw_no_bet".equals(o.getGroupName()) && "Flamengo".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.50, dnb1.getValue());
        assertTrue(dnb1.getBetType() instanceof HandicapBet);
        HandicapBet hbDnb = (HandicapBet) dnb1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hbDnb.outcome());
        assertEquals(0.0, hbDnb.param());
        assertEquals(StatType.YELLOW_CARDS, hbDnb.statType());

        OddItem oddCards = request.getOdds().stream()
                .filter(o -> "cards_odd_even".equals(o.getGroupName()) && "Ímpar".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.90, oddCards.getValue());
        assertTrue(oddCards.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbOdd = (BinaryMarketBet) oddCards.getBetType();
        assertEquals(BinaryMarketBet.MarketType.ODD_EVEN, bmbOdd.marketType());
        assertEquals(BinaryMarketBet.Outcome.ODD, bmbOdd.outcome());
        assertEquals(StatType.YELLOW_CARDS, bmbOdd.statType());
    }

    @Test
    void testBothTeamsToScoreMatchAndHalves() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-btts-1")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Ambas Marcam")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Sim").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Não").decimal(1.95).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Tempo - Ambas as Equipes Marcam")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Sim").decimal(4.50).build(),
                                        Bet7kOutcomeDto.builder().name("Não").decimal(1.18).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("Ambas Marcam em Ambos os Tempos")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Sim").decimal(11.0).build(),
                                        Bet7kOutcomeDto.builder().name("Não").decimal(1.05).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(6, request.getOdds().size());

        // Match BTTS
        OddItem bttsYes = request.getOdds().stream()
                .filter(o -> "btts".equals(o.getGroupName()) && "Sim".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.85, bttsYes.getValue());
        assertTrue(bttsYes.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbYes = (BinaryMarketBet) bttsYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bmbYes.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bmbYes.outcome());
        assertEquals(BetScope.FULL_MATCH, bmbYes.scope());
        assertEquals(StatType.MATCH, bmbYes.statType());

        OddItem bttsNo = request.getOdds().stream()
                .filter(o -> "btts".equals(o.getGroupName()) && "Não".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.95, bttsNo.getValue());
        assertEquals(BinaryMarketBet.Outcome.NO, ((BinaryMarketBet) bttsNo.getBetType()).outcome());

        // 1st Half BTTS
        OddItem ht1Yes = request.getOdds().stream()
                .filter(o -> "btts_half_1".equals(o.getGroupName()) && "Sim".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(4.50, ht1Yes.getValue());
        BinaryMarketBet bmbHt1 = (BinaryMarketBet) ht1Yes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bmbHt1.marketType());
        assertEquals(BetScope.HALF_1, bmbHt1.scope());

        // Both Halves BTTS
        OddItem bothHalvesYes = request.getOdds().stream()
                .filter(o -> "btts_both_halves".equals(o.getGroupName()) && "Sim".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(11.0, bothHalvesYes.getValue());
        BinaryMarketBet bmbBothHalves = (BinaryMarketBet) bothHalvesYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BOTH_HALVES_BTTS, bmbBothHalves.marketType());
        assertEquals(BetScope.FULL_MATCH, bmbBothHalves.scope());
    }

    @Test
    void testDrawNoBet() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-dnb-1")
                .sportName("Futebol")
                .homeTeam("Chelsea")
                .awayTeam("Arsenal")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Empate Anula Aposta")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Chelsea").decimal(1.80).build(),
                                        Bet7kOutcomeDto.builder().name("Arsenal").decimal(2.00).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Tempo - Empate Anula")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Chelsea").decimal(1.70).build(),
                                        Bet7kOutcomeDto.builder().name("Arsenal").decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        // Match DNB
        OddItem dnb1 = request.getOdds().stream()
                .filter(o -> "draw_no_bet".equals(o.getGroupName()) && "Chelsea".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.80, dnb1.getValue());
        assertTrue(dnb1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) dnb1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(0.0, hb1.param());
        assertEquals(BetScope.FULL_MATCH, hb1.scope());
        assertEquals(StatType.MATCH, hb1.statType());

        OddItem dnb2 = request.getOdds().stream()
                .filter(o -> "draw_no_bet".equals(o.getGroupName()) && "Arsenal".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(2.00, dnb2.getValue());
        HandicapBet hb2 = (HandicapBet) dnb2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(0.0, hb2.param());

        // 1st Half DNB
        OddItem ht1Dnb = request.getOdds().stream()
                .filter(o -> "draw_no_bet_half_1".equals(o.getGroupName()) && "Chelsea".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.70, ht1Dnb.getValue());
        HandicapBet hbHt1 = (HandicapBet) ht1Dnb.getBetType();
        assertEquals(BetScope.HALF_1, hbHt1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, hbHt1.outcome());
        assertEquals(0.0, hbHt1.param());
    }

    @Test
    void testCorrectScore() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-cs-1")
                .sportName("Futebol")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Resultado Exato")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("2 - 1").decimal(9.50).build(),
                                        Bet7kOutcomeDto.builder().name("1 - 1").decimal(7.00).build(),
                                        Bet7kOutcomeDto.builder().name("0 - 2").decimal(12.0).build(),
                                        Bet7kOutcomeDto.builder().name("Qualquer Outro Resultado").decimal(15.0).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem cs21 = request.getOdds().stream()
                .filter(o -> "2 - 1".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(9.50, cs21.getValue());
        assertTrue(cs21.getBetType() instanceof CorrectScoreBet);
        CorrectScoreBet csBet21 = (CorrectScoreBet) cs21.getBetType();
        assertEquals(2, csBet21.score1());
        assertEquals(1, csBet21.score2());
        assertFalse(csBet21.isAnyOtherScore());
        assertEquals(BetScope.FULL_MATCH, csBet21.scope());

        OddItem cs11 = request.getOdds().stream()
                .filter(o -> "1 - 1".equals(o.getName()))
                .findFirst().orElseThrow();
        CorrectScoreBet csBet11 = (CorrectScoreBet) cs11.getBetType();
        assertEquals(1, csBet11.score1());
        assertEquals(1, csBet11.score2());

        OddItem cs02 = request.getOdds().stream()
                .filter(o -> "0 - 2".equals(o.getName()))
                .findFirst().orElseThrow();
        CorrectScoreBet csBet02 = (CorrectScoreBet) cs02.getBetType();
        assertEquals(0, csBet02.score1());
        assertEquals(2, csBet02.score2());

        OddItem csOther = request.getOdds().stream()
                .filter(o -> o.getName().contains("Outro"))
                .findFirst().orElseThrow();
        CorrectScoreBet csBetOther = (CorrectScoreBet) csOther.getBetType();
        assertTrue(csBetOther.isAnyOtherScore());
    }

    @Test
    void testHalfTimeFullTime() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-htft-1")
                .sportName("Futebol")
                .homeTeam("Liverpool")
                .awayTeam("Manchester City")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("Intervalo / Final")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Liverpool / Liverpool").decimal(3.50).build(),
                                        Bet7kOutcomeDto.builder().name("Empate / Liverpool").decimal(5.00).build(),
                                        Bet7kOutcomeDto.builder().name("Manchester City / Liverpool").decimal(26.0).build(),
                                        Bet7kOutcomeDto.builder().name("Empate / Empate").decimal(5.50).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(4, request.getOdds().size());

        OddItem htft1 = request.getOdds().stream()
                .filter(o -> "Liverpool / Liverpool".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(3.50, htft1.getValue());
        assertTrue(htft1.getBetType() instanceof HalfTimeFullTimeBet);
        HalfTimeFullTimeBet b1 = (HalfTimeFullTimeBet) htft1.getBetType();
        assertEquals(HalfTimeFullTimeBet.Outcome.W1_W1, b1.outcome());

        OddItem htft2 = request.getOdds().stream()
                .filter(o -> "Empate / Liverpool".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.X_W1, ((HalfTimeFullTimeBet) htft2.getBetType()).outcome());

        OddItem htft3 = request.getOdds().stream()
                .filter(o -> "Manchester City / Liverpool".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.W2_W1, ((HalfTimeFullTimeBet) htft3.getBetType()).outcome());

        OddItem htft4 = request.getOdds().stream()
                .filter(o -> "Empate / Empate".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(HalfTimeFullTimeBet.Outcome.X_X, ((HalfTimeFullTimeBet) htft4.getBetType()).outcome());
    }

    @Test
    void testPeriodMarketsHalvesAndQuarters() {
        Bet7kEventDto event = Bet7kEventDto.builder()
                .id("ev-period-1")
                .sportName("Basquete")
                .homeTeam("Golden State")
                .awayTeam("Boston Celtics")
                .markets(List.of(
                        Bet7kMarketDto.builder()
                                .name("1º Quarto - Vencedor")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Golden State").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Boston Celtics").decimal(1.95).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Quarto - Total de Pontos")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Mais 54.5").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Menos 54.5").decimal(1.90).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Quarto - Handicap")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Golden State (-1.5)").decimal(1.90).build(),
                                        Bet7kOutcomeDto.builder().name("Boston Celtics (+1.5)").decimal(1.90).build()
                                ))
                                .build(),
                        Bet7kMarketDto.builder()
                                .name("1º Tempo - Total")
                                .outcomes(List.of(
                                        Bet7kOutcomeDto.builder().name("Acima 112.5").decimal(1.85).build(),
                                        Bet7kOutcomeDto.builder().name("Abaixo 112.5").decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(8, request.getOdds().size());

        // 1Q Result
        OddItem q1w1 = request.getOdds().stream()
                .filter(o -> "period_result_quarter_1".equals(o.getGroupName()) && o.getName().contains("Golden State"))
                .findFirst().orElseThrow();
        assertEquals(1.85, q1w1.getValue());
        assertTrue(q1w1.getBetType() instanceof MatchResultBet);
        MatchResultBet mrbQ1 = (MatchResultBet) q1w1.getBetType();
        assertEquals(MatchResultBet.Outcome.WIN1, mrbQ1.outcome());
        assertEquals(BetScope.QUARTER_1, mrbQ1.scope());

        // 1Q Total
        OddItem q1Tot = request.getOdds().stream()
                .filter(o -> "period_total_quarter_1".equals(o.getGroupName()) && o.getName().contains("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.90, q1Tot.getValue());
        assertTrue(q1Tot.getBetType() instanceof TotalBet);
        TotalBet tbQ1 = (TotalBet) q1Tot.getBetType();
        assertEquals(54.5, tbQ1.param());
        assertEquals(BetScope.QUARTER_1, tbQ1.scope());
        assertEquals(TotalBet.Direction.OVER, tbQ1.direction());

        // 1Q Handicap
        OddItem q1Hcp = request.getOdds().stream()
                .filter(o -> "period_handicap_quarter_1".equals(o.getGroupName()) && o.getName().contains("Golden State"))
                .findFirst().orElseThrow();
        assertEquals(1.90, q1Hcp.getValue());
        assertTrue(q1Hcp.getBetType() instanceof HandicapBet);
        HandicapBet hbQ1 = (HandicapBet) q1Hcp.getBetType();
        assertEquals(-1.5, hbQ1.param());
        assertEquals(BetScope.QUARTER_1, hbQ1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, hbQ1.outcome());

        // 1T Total
        OddItem ht1Tot = request.getOdds().stream()
                .filter(o -> "period_total_half_1".equals(o.getGroupName()) && o.getName().contains("Acima"))
                .findFirst().orElseThrow();
        assertEquals(1.85, ht1Tot.getValue());
        TotalBet tbHt1 = (TotalBet) ht1Tot.getBetType();
        assertEquals(112.5, tbHt1.param());
        assertEquals(BetScope.HALF_1, tbHt1.scope());
        assertEquals(TotalBet.Direction.OVER, tbHt1.direction());
    }
}

