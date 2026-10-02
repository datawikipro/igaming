package pro.datawiki.igaming.source.betesporte.service;

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
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetesporteOddsMapperTest {

    private BetesporteOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BetesporteOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("betesporte", SportType.FOOTBALL));
        assertTrue(mapper.supports("BETESPORTE", SportType.ESPORTS));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-100")
                .sportName("Futebol")
                .leagueName("Brasileirão Série A")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .isLive(false)
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Resultado Final")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Flamengo").decimal(2.10).build(),
                                        BetesporteOutcomeDto.builder().name("Empate").decimal(3.30).build(),
                                        BetesporteOutcomeDto.builder().name("Palmeiras").decimal(3.40).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("betesporte", request.getBookmaker());
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
    void testMatchResultPeriod() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-1002")
                .homeTeam("Corinthians")
                .awayTeam("Santos")
                .sportName("Futebol")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("1° Tempo - Resultado")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("1").decimal(2.80).build(),
                                        BetesporteOutcomeDto.builder().name("X").decimal(2.05).build(),
                                        BetesporteOutcomeDto.builder().name("2").decimal(3.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest update = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(update);
        List<OddItem> odds = update.getOdds();
        assertEquals(3, odds.size());

        OddItem item1T = odds.get(0);
        MatchResultBet bet1T = (MatchResultBet) item1T.getBetType();
        assertEquals(BetScope.HALF_1, bet1T.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, bet1T.outcome());
    }

    @Test
    void testDoubleChance() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-101")
                .sportName("Soccer")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Dupla Chance")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("1X").decimal(1.35).build(),
                                        BetesporteOutcomeDto.builder().name("12").decimal(1.30).build(),
                                        BetesporteOutcomeDto.builder().name("X2").decimal(1.65).build()
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
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-102")
                .sportName("Futebol")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Total de Gols Mais/Menos")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais de 2.5").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("Menos de 2.5").decimal(1.95).build()
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
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-103")
                .sportName("Soccer")
                .homeTeam("Liverpool")
                .awayTeam("Manchester City")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Total Casa - Mais/Menos")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 1.5").decimal(1.70).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 1.5").decimal(2.10).build()
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
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-104")
                .sportName("Basquete")
                .homeTeam("LA Lakers")
                .awayTeam("Golden State")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Handicap Asiatico")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("LA Lakers (-4.5)").decimal(1.90).build(),
                                        BetesporteOutcomeDto.builder().name("Golden State (+4.5)").decimal(1.90).build()
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
    void testEsportsCS2Markets() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("cs2-1")
                .sportName("CS2")
                .leagueName("ESL Pro League")
                .homeTeam("Natus Vincere")
                .awayTeam("FaZe Clan")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Vencedor da Partida")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Natus Vincere").decimal(1.80).build(),
                                        BetesporteOutcomeDto.builder().name("FaZe Clan").decimal(2.00).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Total de Mapas")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais de 2.5").decimal(1.90).build(),
                                        BetesporteOutcomeDto.builder().name("Menos de 2.5").decimal(1.90).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Mapa - Total de Rounds")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais de 21.5").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("Menos de 21.5").decimal(1.85).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Mapa - Handicap de Round")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Natus Vincere (-2.5)").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("FaZe Clan (+2.5)").decimal(1.85).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(req);
        assertEquals(SportType.CS2, req.getSportType());
        assertEquals(8, req.getOdds().size());

        // Winner
        OddItem w1 = req.getOdds().stream().filter(o -> "Natus Vincere".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.80, w1.getValue());
        assertTrue(w1.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.WIN1, ((MatchResultBet) w1.getBetType()).outcome());

        // Total maps
        OddItem totalMaps = req.getOdds().stream().filter(o -> "esports_total_maps".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        TotalBet tbMaps = (TotalBet) totalMaps.getBetType();
        assertEquals(StatType.MAPS, tbMaps.statType());
        assertEquals(2.5, tbMaps.param());

        // Total rounds map 1
        OddItem totalRounds = req.getOdds().stream().filter(o -> o.getGroupName().contains("map_1_total_rounds") && o.getName().contains("Mais")).findFirst().orElseThrow();
        TotalBet tbRounds = (TotalBet) totalRounds.getBetType();
        assertEquals(StatType.ROUNDS, tbRounds.statType());
        assertEquals(BetScope.MAP_1, tbRounds.scope());
        assertEquals(21.5, tbRounds.param());

        // Round handicap map 1
        OddItem hdpRounds = req.getOdds().stream().filter(o -> o.getGroupName().contains("map_1_round_handicap") && o.getName().contains("Natus")).findFirst().orElseThrow();
        HandicapBet hbRounds = (HandicapBet) hdpRounds.getBetType();
        assertEquals(StatType.ROUNDS, hbRounds.statType());
        assertEquals(BetScope.MAP_1, hbRounds.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, hbRounds.outcome());
        assertEquals(-2.5, hbRounds.param());
    }

    @Test
    void testEsportsDota2FirstBloodAndMapWinner() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("dota-1")
                .sportName("Dota 2")
                .homeTeam("Team Spirit")
                .awayTeam("Team Liquid")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("1º Mapa - Vencedor")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Team Spirit").decimal(1.75).build(),
                                        BetesporteOutcomeDto.builder().name("Team Liquid").decimal(2.05).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Mapa - Primeiro Abate")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Team Spirit").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("Team Liquid").decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(req);
        assertEquals(SportType.DOTA2, req.getSportType());
        assertEquals(4, req.getOdds().size());

        OddItem map1Winner = req.getOdds().stream().filter(o -> "esports_map_1_winner".equals(o.getGroupName()) && o.getName().contains("Spirit")).findFirst().orElseThrow();
        MatchResultBet mrb = (MatchResultBet) map1Winner.getBetType();
        assertEquals(BetScope.MAP_1, mrb.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mrb.outcome());

        OddItem fb = req.getOdds().stream().filter(o -> "esports_map_1_first_blood".equals(o.getGroupName()) && o.getName().contains("Spirit")).findFirst().orElseThrow();
        BinaryMarketBet bmb = (BinaryMarketBet) fb.getBetType();
        assertEquals(BetScope.MAP_1, bmb.scope());
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, bmb.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmb.outcome());
    }

    @Test
    void testEsportsLoLAndValorant() {
        BetesporteEventDto lolEvent = BetesporteEventDto.builder()
                .id("lol-1")
                .sportName("League of Legends")
                .homeTeam("T1")
                .awayTeam("Gen.G")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("1º Mapa - Total de Abates")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais de 25.5").decimal(1.90).build(),
                                        BetesporteOutcomeDto.builder().name("Menos de 25.5").decimal(1.90).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Mapa - Handicap de Abates")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("T1 (-4.5)").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("Gen.G (+4.5)").decimal(1.85).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest lolReq = mapper.mapToOddsUpdateRequest(lolEvent);
        assertNotNull(lolReq);
        assertEquals(SportType.LEAGUE_OF_LEGENDS, lolReq.getSportType());
        assertEquals(4, lolReq.getOdds().size());

        OddItem totalKills = lolReq.getOdds().stream().filter(o -> o.getGroupName().contains("total_kills")).findFirst().orElseThrow();
        TotalBet tbKills = (TotalBet) totalKills.getBetType();
        assertEquals(StatType.KILLS, tbKills.statType());
        assertEquals(25.5, tbKills.param());

        OddItem killHdp = lolReq.getOdds().stream().filter(o -> o.getGroupName().contains("kill_handicap") && o.getName().contains("T1")).findFirst().orElseThrow();
        HandicapBet hbKills = (HandicapBet) killHdp.getBetType();
        assertEquals(StatType.KILLS, hbKills.statType());
        assertEquals(-4.5, hbKills.param());

        // Valorant
        BetesporteEventDto valEvent = BetesporteEventDto.builder()
                .id("val-1")
                .sportName("Valorant")
                .homeTeam("Sentinels")
                .awayTeam("Fnatic")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Handicap de Mapa")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Sentinels (+1.5)").decimal(1.65).build(),
                                        BetesporteOutcomeDto.builder().name("Fnatic (-1.5)").decimal(2.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest valReq = mapper.mapToOddsUpdateRequest(valEvent);
        assertNotNull(valReq);
        assertEquals(SportType.VALORANT, valReq.getSportType());
        assertEquals(2, valReq.getOdds().size());
        OddItem mapHdp = valReq.getOdds().get(0);
        HandicapBet hbMap = (HandicapBet) mapHdp.getBetType();
        assertEquals(StatType.MAPS, hbMap.statType());
        assertEquals(1.5, hbMap.param());
    }

    @Test
    void testCornersMarkets() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("corners-1")
                .sportName("Futebol")
                .leagueName("Premier League")
                .homeTeam("Chelsea")
                .awayTeam("Arsenal")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Total de Escanteios")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais de 9.5").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("Menos de 9.5").decimal(1.95).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Tempo - Total de Escanteios")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 4.5").decimal(1.90).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 4.5").decimal(1.90).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Escanteios Casa - Total")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais de 5.5").decimal(2.10).build(),
                                        BetesporteOutcomeDto.builder().name("Menos de 5.5").decimal(1.70).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Handicap de Escanteios")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Chelsea (-1.5)").decimal(1.90).build(),
                                        BetesporteOutcomeDto.builder().name("Arsenal (+1.5)").decimal(1.90).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Vencedor dos Escanteios")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Chelsea").decimal(1.75).build(),
                                        BetesporteOutcomeDto.builder().name("Empate").decimal(7.50).build(),
                                        BetesporteOutcomeDto.builder().name("Arsenal").decimal(2.50).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Primeiro Escanteio")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Chelsea").decimal(1.80).build(),
                                        BetesporteOutcomeDto.builder().name("Arsenal").decimal(1.95).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Dupla Chance de Escanteios")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("1X").decimal(1.25).build(),
                                        BetesporteOutcomeDto.builder().name("12").decimal(1.15).build(),
                                        BetesporteOutcomeDto.builder().name("X2").decimal(1.60).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Escanteios - Empate Anula Aposta")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Chelsea").decimal(1.50).build(),
                                        BetesporteOutcomeDto.builder().name("Arsenal").decimal(2.40).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Total de Escanteios - Par/Ímpar")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Par").decimal(1.90).build(),
                                        BetesporteOutcomeDto.builder().name("Ímpar").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(req);
        List<OddItem> odds = req.getOdds();
        assertFalse(odds.isEmpty());

        // Match total corners
        OddItem matchTotal = odds.stream().filter(o -> "corners_total".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        TotalBet tbMatch = (TotalBet) matchTotal.getBetType();
        assertEquals(StatType.CORNERS, tbMatch.statType());
        assertEquals(9.5, tbMatch.param());
        assertEquals(BetSubject.MATCH, tbMatch.subject());
        assertEquals(BetScope.FULL_MATCH, tbMatch.scope());

        // 1st half total corners
        OddItem h1Total = odds.stream().filter(o -> o.getGroupName().contains("half_1") && o.getName().contains("Mais")).findFirst().orElseThrow();
        TotalBet tbH1 = (TotalBet) h1Total.getBetType();
        assertEquals(StatType.CORNERS, tbH1.statType());
        assertEquals(4.5, tbH1.param());
        assertEquals(BetScope.HALF_1, tbH1.scope());

        // Team total corners
        OddItem teamTotal = odds.stream().filter(o -> "corners_total_team1".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        TotalBet tbTeam = (TotalBet) teamTotal.getBetType();
        assertEquals(StatType.CORNERS, tbTeam.statType());
        assertEquals(5.5, tbTeam.param());
        assertEquals(BetSubject.TEAM1, tbTeam.subject());

        // Corner Handicap
        OddItem cornerHdp = odds.stream().filter(o -> "corners_handicap".equals(o.getGroupName()) && o.getName().contains("Chelsea")).findFirst().orElseThrow();
        HandicapBet hb = (HandicapBet) cornerHdp.getBetType();
        assertEquals(StatType.CORNERS, hb.statType());
        assertEquals(-1.5, hb.param());
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());

        // Corner 1X2
        OddItem cornerDraw = odds.stream().filter(o -> "corners_1x2".equals(o.getGroupName()) && "Empate".equals(o.getName())).findFirst().orElseThrow();
        MatchResultBet mrb = (MatchResultBet) cornerDraw.getBetType();
        assertEquals(StatType.CORNERS, mrb.statType());
        assertEquals(MatchResultBet.Outcome.DRAW, mrb.outcome());

        // First corner
        OddItem firstCorner = odds.stream().filter(o -> "corners_first".equals(o.getGroupName()) && "Chelsea".equals(o.getName())).findFirst().orElseThrow();
        BinaryMarketBet bmb = (BinaryMarketBet) firstCorner.getBetType();
        assertEquals(StatType.CORNERS, bmb.statType());
        assertEquals(BinaryMarketBet.MarketType.FIRST_CORNER, bmb.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmb.outcome());

        // Double chance
        OddItem dc = odds.stream().filter(o -> "corners_double_chance".equals(o.getGroupName()) && "1X".equals(o.getName())).findFirst().orElseThrow();
        MatchResultBet dcBet = (MatchResultBet) dc.getBetType();
        assertEquals(StatType.CORNERS, dcBet.statType());
        assertEquals(MatchResultBet.Outcome.DC_1X, dcBet.outcome());

        // Draw No Bet
        OddItem dnb = odds.stream().filter(o -> "corners_draw_no_bet".equals(o.getGroupName()) && "Chelsea".equals(o.getName())).findFirst().orElseThrow();
        HandicapBet dnbBet = (HandicapBet) dnb.getBetType();
        assertEquals(StatType.CORNERS, dnbBet.statType());
        assertEquals(0.0, dnbBet.param());
        assertEquals(HandicapBet.Outcome.TEAM1, dnbBet.outcome());

        // Odd / Even
        OddItem oddEven = odds.stream().filter(o -> "corners_odd_even".equals(o.getGroupName()) && "Par".equals(o.getName())).findFirst().orElseThrow();
        BinaryMarketBet oeBet = (BinaryMarketBet) oddEven.getBetType();
        assertEquals(StatType.CORNERS, oeBet.statType());
        assertEquals(BinaryMarketBet.MarketType.ODD_EVEN, oeBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.EVEN, oeBet.outcome());
    }

    @Test
    void testCardsMarkets() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("cards-1")
                .sportName("Futebol")
                .leagueName("La Liga")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Total de Cartões")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais de 4.5").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("Menos de 4.5").decimal(1.95).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Tempo - Total de Cartões")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 1.5").decimal(1.75).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 1.5").decimal(2.05).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Cartões Casa - Total")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais de 2.5").decimal(2.20).build(),
                                        BetesporteOutcomeDto.builder().name("Menos de 2.5").decimal(1.65).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Handicap de Cartões")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Real Madrid (-0.5)").decimal(1.90).build(),
                                        BetesporteOutcomeDto.builder().name("Barcelona (+0.5)").decimal(1.90).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Vencedor dos Cartões")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Real Madrid").decimal(2.10).build(),
                                        BetesporteOutcomeDto.builder().name("Empate").decimal(4.50).build(),
                                        BetesporteOutcomeDto.builder().name("Barcelona").decimal(2.60).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Cartão Vermelho")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Sim").decimal(3.50).build(),
                                        BetesporteOutcomeDto.builder().name("Não").decimal(1.30).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(req);
        List<OddItem> odds = req.getOdds();
        assertFalse(odds.isEmpty());

        // Match total cards
        OddItem matchTotal = odds.stream().filter(o -> "cards_total".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        TotalBet tbMatch = (TotalBet) matchTotal.getBetType();
        assertEquals(StatType.YELLOW_CARDS, tbMatch.statType());
        assertEquals(4.5, tbMatch.param());
        assertEquals(BetSubject.MATCH, tbMatch.subject());

        // 1st half total cards
        OddItem h1Total = odds.stream().filter(o -> o.getGroupName().contains("half_1") && o.getName().contains("Mais")).findFirst().orElseThrow();
        TotalBet tbH1 = (TotalBet) h1Total.getBetType();
        assertEquals(StatType.YELLOW_CARDS, tbH1.statType());
        assertEquals(1.5, tbH1.param());
        assertEquals(BetScope.HALF_1, tbH1.scope());

        // Team total cards
        OddItem teamTotal = odds.stream().filter(o -> "cards_total_team1".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        TotalBet tbTeam = (TotalBet) teamTotal.getBetType();
        assertEquals(StatType.YELLOW_CARDS, tbTeam.statType());
        assertEquals(2.5, tbTeam.param());
        assertEquals(BetSubject.TEAM1, tbTeam.subject());

        // Handicap cards
        OddItem hdpCards = odds.stream().filter(o -> "cards_handicap".equals(o.getGroupName()) && o.getName().contains("Real Madrid")).findFirst().orElseThrow();
        HandicapBet hb = (HandicapBet) hdpCards.getBetType();
        assertEquals(StatType.YELLOW_CARDS, hb.statType());
        assertEquals(-0.5, hb.param());
        assertEquals(HandicapBet.Outcome.TEAM1, hb.outcome());

        // 1X2 cards
        OddItem w1 = odds.stream().filter(o -> "cards_1x2".equals(o.getGroupName()) && "Real Madrid".equals(o.getName())).findFirst().orElseThrow();
        MatchResultBet mrb = (MatchResultBet) w1.getBetType();
        assertEquals(StatType.YELLOW_CARDS, mrb.statType());
        assertEquals(MatchResultBet.Outcome.WIN1, mrb.outcome());

        // Red card
        OddItem rcYes = odds.stream().filter(o -> "cards_red_card".equals(o.getGroupName()) && "Sim".equals(o.getName())).findFirst().orElseThrow();
        BinaryMarketBet rcBet = (BinaryMarketBet) rcYes.getBetType();
        assertEquals(StatType.YELLOW_CARDS, rcBet.statType());
        assertEquals(BinaryMarketBet.MarketType.RED_CARD, rcBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, rcBet.outcome());
    }

    @Test
    void testBothTeamsToScoreMarkets() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("btts-1")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Fluminense")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Ambas Marcam")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Sim").decimal(1.95).build(),
                                        BetesporteOutcomeDto.builder().name("Não").decimal(1.85).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Tempo - Ambas as equipes marcam")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Sim").decimal(4.20).build(),
                                        BetesporteOutcomeDto.builder().name("Não").decimal(1.20).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Ambas Marcam em Ambos os Tempos")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Sim").decimal(12.00).build(),
                                        BetesporteOutcomeDto.builder().name("Não").decimal(1.05).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(req);
        assertEquals(6, req.getOdds().size());

        // Full match BTTS Yes
        OddItem bttsYes = req.getOdds().stream().filter(o -> "btts".equals(o.getGroupName()) && "Sim".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.95, bttsYes.getValue());
        BinaryMarketBet betYes = (BinaryMarketBet) bttsYes.getBetType();
        assertEquals(BetScope.FULL_MATCH, betYes.scope());
        assertEquals(BinaryMarketBet.MarketType.BTTS, betYes.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, betYes.outcome());

        // Full match BTTS No
        OddItem bttsNo = req.getOdds().stream().filter(o -> "btts".equals(o.getGroupName()) && "Não".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.85, bttsNo.getValue());
        BinaryMarketBet betNo = (BinaryMarketBet) bttsNo.getBetType();
        assertEquals(BinaryMarketBet.Outcome.NO, betNo.outcome());

        // 1st Half BTTS
        OddItem bttsH1 = req.getOdds().stream().filter(o -> "btts_half_1".equals(o.getGroupName()) && "Sim".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(4.20, bttsH1.getValue());
        BinaryMarketBet betH1 = (BinaryMarketBet) bttsH1.getBetType();
        assertEquals(BetScope.HALF_1, betH1.scope());
        assertEquals(BinaryMarketBet.MarketType.BTTS, betH1.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, betH1.outcome());

        // Both Halves BTTS
        OddItem bttsBoth = req.getOdds().stream().filter(o -> "btts_both_halves".equals(o.getGroupName()) && "Sim".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(12.00, bttsBoth.getValue());
        BinaryMarketBet betBoth = (BinaryMarketBet) bttsBoth.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BOTH_HALVES_BTTS, betBoth.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, betBoth.outcome());
    }

    @Test
    void testDrawNoBetMarkets() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("dnb-1")
                .sportName("Futebol")
                .homeTeam("Palmeiras")
                .awayTeam("Corinthians")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Empate Anula Aposta")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Palmeiras").decimal(1.55).build(),
                                        BetesporteOutcomeDto.builder().name("Corinthians").decimal(2.45).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Tempo - Empate Anula Aposta")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("1").decimal(1.70).build(),
                                        BetesporteOutcomeDto.builder().name("2").decimal(2.10).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(req);
        assertEquals(4, req.getOdds().size());

        // Full Match DNB
        OddItem dnb1 = req.getOdds().stream().filter(o -> "draw_no_bet".equals(o.getGroupName()) && "Palmeiras".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.55, dnb1.getValue());
        HandicapBet hb1 = (HandicapBet) dnb1.getBetType();
        assertEquals(BetScope.FULL_MATCH, hb1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(0.0, hb1.param());

        OddItem dnb2 = req.getOdds().stream().filter(o -> "draw_no_bet".equals(o.getGroupName()) && "Corinthians".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(2.45, dnb2.getValue());
        HandicapBet hb2 = (HandicapBet) dnb2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(0.0, hb2.param());

        // 1st Half DNB
        OddItem dnbH1_1 = req.getOdds().stream().filter(o -> "draw_no_bet_half_1".equals(o.getGroupName()) && "1".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(1.70, dnbH1_1.getValue());
        HandicapBet hbH1_1 = (HandicapBet) dnbH1_1.getBetType();
        assertEquals(BetScope.HALF_1, hbH1_1.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, hbH1_1.outcome());
        assertEquals(0.0, hbH1_1.param());
    }

    @Test
    void testCorrectScoreMarkets() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("cs-1")
                .sportName("Futebol")
                .homeTeam("Barcelona")
                .awayTeam("Real Madrid")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Resultado Exato")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("1:0").decimal(6.50).build(),
                                        BetesporteOutcomeDto.builder().name("2 - 1").decimal(8.50).build(),
                                        BetesporteOutcomeDto.builder().name("0x0").decimal(7.00).build(),
                                        BetesporteOutcomeDto.builder().name("Qualquer Outro Resultado").decimal(15.00).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Tempo - Placar Exato")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("0-0").decimal(2.60).build(),
                                        BetesporteOutcomeDto.builder().name("1-0").decimal(3.80).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(req);
        assertEquals(6, req.getOdds().size());

        // 1:0
        OddItem cs10 = req.getOdds().stream().filter(o -> "correct_score".equals(o.getGroupName()) && "1:0".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(6.50, cs10.getValue());
        CorrectScoreBet bet10 = (CorrectScoreBet) cs10.getBetType();
        assertEquals(1, bet10.score1());
        assertEquals(0, bet10.score2());
        assertFalse(bet10.isAnyOtherScore());
        assertEquals(BetScope.FULL_MATCH, bet10.scope());

        // 2 - 1
        OddItem cs21 = req.getOdds().stream().filter(o -> "correct_score".equals(o.getGroupName()) && "2 - 1".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(8.50, cs21.getValue());
        CorrectScoreBet bet21 = (CorrectScoreBet) cs21.getBetType();
        assertEquals(2, bet21.score1());
        assertEquals(1, bet21.score2());

        // Any other score
        OddItem csOther = req.getOdds().stream().filter(o -> "correct_score".equals(o.getGroupName()) && o.getName().contains("Qualquer")).findFirst().orElseThrow();
        assertEquals(15.00, csOther.getValue());
        CorrectScoreBet betOther = (CorrectScoreBet) csOther.getBetType();
        assertTrue(betOther.isAnyOtherScore());

        // 1st Half Correct Score 1-0
        OddItem csH1 = req.getOdds().stream().filter(o -> "correct_score_half_1".equals(o.getGroupName()) && "1-0".equals(o.getName())).findFirst().orElseThrow();
        assertEquals(3.80, csH1.getValue());
        CorrectScoreBet betH1 = (CorrectScoreBet) csH1.getBetType();
        assertEquals(BetScope.HALF_1, betH1.scope());
        assertEquals(1, betH1.score1());
        assertEquals(0, betH1.score2());
    }

    @Test
    void testHalfTimeFullTimeMarkets() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("htft-1")
                .sportName("Futebol")
                .homeTeam("Liverpool")
                .awayTeam("Manchester City")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Intervalo / Final")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("1/1").decimal(2.60).build(),
                                        BetesporteOutcomeDto.builder().name("1/X").decimal(15.00).build(),
                                        BetesporteOutcomeDto.builder().name("1/2").decimal(30.00).build(),
                                        BetesporteOutcomeDto.builder().name("X/1").decimal(4.50).build(),
                                        BetesporteOutcomeDto.builder().name("X/X").decimal(5.00).build(),
                                        BetesporteOutcomeDto.builder().name("X/2").decimal(6.50).build(),
                                        BetesporteOutcomeDto.builder().name("2/1").decimal(28.00).build(),
                                        BetesporteOutcomeDto.builder().name("2/X").decimal(15.00).build(),
                                        BetesporteOutcomeDto.builder().name("2/2").decimal(4.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest req = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(req);
        assertEquals(9, req.getOdds().size());

        OddItem w1w1 = req.getOdds().stream().filter(o -> "1/1".equals(o.getName())).findFirst().orElseThrow();
        HalfTimeFullTimeBet b1 = (HalfTimeFullTimeBet) w1w1.getBetType();
        assertEquals(HalfTimeFullTimeBet.Outcome.W1_W1, b1.outcome());

        OddItem w1x = req.getOdds().stream().filter(o -> "1/X".equals(o.getName())).findFirst().orElseThrow();
        HalfTimeFullTimeBet b2 = (HalfTimeFullTimeBet) w1x.getBetType();
        assertEquals(HalfTimeFullTimeBet.Outcome.W1_X, b2.outcome());

        OddItem xx = req.getOdds().stream().filter(o -> "X/X".equals(o.getName())).findFirst().orElseThrow();
        HalfTimeFullTimeBet b3 = (HalfTimeFullTimeBet) xx.getBetType();
        assertEquals(HalfTimeFullTimeBet.Outcome.X_X, b3.outcome());

        OddItem w2w2 = req.getOdds().stream().filter(o -> "2/2".equals(o.getName())).findFirst().orElseThrow();
        HalfTimeFullTimeBet b4 = (HalfTimeFullTimeBet) w2w2.getBetType();
        assertEquals(HalfTimeFullTimeBet.Outcome.W2_W2, b4.outcome());
    }

    @Test
    void testPeriodMarketsComprehensive() {
        BetesporteEventDto footballEvent = BetesporteEventDto.builder()
                .id("period-fb-1")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("1º Tempo - Total de Gols")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais de 1.5").decimal(2.15).build(),
                                        BetesporteOutcomeDto.builder().name("Menos de 1.5").decimal(1.65).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Tempo - Handicap Asiatico")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Flamengo (-0.5)").decimal(2.05).build(),
                                        BetesporteOutcomeDto.builder().name("Palmeiras (+0.5)").decimal(1.75).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("2º Tempo - Resultado")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("1").decimal(2.30).build(),
                                        BetesporteOutcomeDto.builder().name("X").decimal(2.40).build(),
                                        BetesporteOutcomeDto.builder().name("2").decimal(3.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest fbReq = mapper.mapToOddsUpdateRequest(footballEvent);
        assertNotNull(fbReq);
        assertEquals(7, fbReq.getOdds().size());

        // 1st Half Total
        OddItem h1Over = fbReq.getOdds().stream().filter(o -> "period_total_half_1".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        TotalBet tbH1 = (TotalBet) h1Over.getBetType();
        assertEquals(BetScope.HALF_1, tbH1.scope());
        assertEquals(1.5, tbH1.param());
        assertEquals(TotalBet.Direction.OVER, tbH1.direction());

        // 1st Half Handicap
        OddItem h1Hdp = fbReq.getOdds().stream().filter(o -> "period_handicap_half_1".equals(o.getGroupName()) && o.getName().contains("Flamengo")).findFirst().orElseThrow();
        HandicapBet hbH1 = (HandicapBet) h1Hdp.getBetType();
        assertEquals(BetScope.HALF_1, hbH1.scope());
        assertEquals(-0.5, hbH1.param());
        assertEquals(HandicapBet.Outcome.TEAM1, hbH1.outcome());

        // 2nd Half Result
        OddItem h2W1 = fbReq.getOdds().stream().filter(o -> "period_result_half_2".equals(o.getGroupName()) && "1".equals(o.getName())).findFirst().orElseThrow();
        MatchResultBet mrbH2 = (MatchResultBet) h2W1.getBetType();
        assertEquals(BetScope.HALF_2, mrbH2.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mrbH2.outcome());

        // Basketball Quarters
        BetesporteEventDto bballEvent = BetesporteEventDto.builder()
                .id("period-bb-1")
                .sportName("Basquete")
                .homeTeam("LA Lakers")
                .awayTeam("Golden State")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("1° Quarto - Total de Pontos")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 54.5").decimal(1.90).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 54.5").decimal(1.90).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1° Quarto - Handicap")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("LA Lakers (-2.5)").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("Golden State (+2.5)").decimal(1.85).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest bbReq = mapper.mapToOddsUpdateRequest(bballEvent);
        assertNotNull(bbReq);
        assertEquals(4, bbReq.getOdds().size());

        // Q1 Total
        OddItem q1Total = bbReq.getOdds().stream().filter(o -> "period_total_quarter_1".equals(o.getGroupName()) && o.getName().contains("Mais")).findFirst().orElseThrow();
        TotalBet tbQ1 = (TotalBet) q1Total.getBetType();
        assertEquals(BetScope.QUARTER_1, tbQ1.scope());
        assertEquals(54.5, tbQ1.param());

        // Q1 Handicap
        OddItem q1Hdp = bbReq.getOdds().stream().filter(o -> "period_handicap_quarter_1".equals(o.getGroupName()) && o.getName().contains("Lakers")).findFirst().orElseThrow();
        HandicapBet hbQ1 = (HandicapBet) q1Hdp.getBetType();
        assertEquals(BetScope.QUARTER_1, hbQ1.scope());
        assertEquals(-2.5, hbQ1.param());
        assertEquals(HandicapBet.Outcome.TEAM1, hbQ1.outcome());
    }

    @Test
    void testInvalidOrEmptyEvent() {
        assertNull(mapper.mapToOddsUpdateRequest(null));
        assertNull(mapper.mapToOddsUpdateRequest(BetesporteEventDto.builder().id("1").build()));
    }
}
