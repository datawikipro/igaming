package pro.datawiki.igaming.source.betnacional.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalEventDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalMarketDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalOutcomeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BetnacionalOddsMapperTest {

    private BetnacionalOddsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BetnacionalOddsMapper();
    }

    @Test
    void testSupports() {
        assertTrue(mapper.supports("betnacional", SportType.FOOTBALL));
        assertTrue(mapper.supports("BETNACIONAL", SportType.ESPORTS));
        assertFalse(mapper.supports("fonbet", SportType.FOOTBALL));
    }

    @Test
    void testMatchResult1X2() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-100")
                .sportName("Futebol")
                .leagueName("Brasileirão Série A")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .isLive(false)
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Resultado Final")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Flamengo").decimal(2.10).build(),
                                        BetnacionalOutcomeDto.builder().name("Empate").decimal(3.30).build(),
                                        BetnacionalOutcomeDto.builder().name("Palmeiras").decimal(3.40).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals("betnacional", request.getBookmaker());
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
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-101")
                .sportName("Soccer")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Dupla Chance")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("1X").decimal(1.30).build(),
                                        BetnacionalOutcomeDto.builder().name("12").decimal(1.35).build(),
                                        BetnacionalOutcomeDto.builder().name("X2").decimal(1.65).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem dc1X = request.getOdds().stream().filter(o -> o.getName().equals("1X")).findFirst().orElseThrow();
        assertEquals(1.30, dc1X.getValue());
        assertTrue(dc1X.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_1X, ((MatchResultBet) dc1X.getBetType()).outcome());

        OddItem dc12 = request.getOdds().stream().filter(o -> o.getName().equals("12")).findFirst().orElseThrow();
        assertEquals(1.35, dc12.getValue());
        assertTrue(dc12.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_12, ((MatchResultBet) dc12.getBetType()).outcome());

        OddItem dcX2 = request.getOdds().stream().filter(o -> o.getName().equals("X2")).findFirst().orElseThrow();
        assertEquals(1.65, dcX2.getValue());
        assertTrue(dcX2.getBetType() instanceof MatchResultBet);
        assertEquals(MatchResultBet.Outcome.DC_X2, ((MatchResultBet) dcX2.getBetType()).outcome());
    }

    @Test
    void testTotalOverUnder() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-102")
                .sportName("Futebol")
                .homeTeam("Liverpool")
                .awayTeam("Man City")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Total de Gols - Mais/Menos")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 2.5").decimal(1.80).handicap(2.5).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 2.5").decimal(2.00).handicap(2.5).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem over = request.getOdds().stream().filter(o -> o.getName().contains("Mais")).findFirst().orElseThrow();
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet overBet = (TotalBet) over.getBetType();
        assertEquals(TotalBet.Direction.OVER, overBet.direction());
        assertEquals(2.5, overBet.param());

        OddItem under = request.getOdds().stream().filter(o -> o.getName().contains("Menos")).findFirst().orElseThrow();
        assertTrue(under.getBetType() instanceof TotalBet);
        TotalBet underBet = (TotalBet) under.getBetType();
        assertEquals(TotalBet.Direction.UNDER, underBet.direction());
        assertEquals(2.5, underBet.param());
    }

    @Test
    void testHandicap() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-103")
                .sportName("Futebol")
                .homeTeam("Inter")
                .awayTeam("Milan")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Handicap Asiatico")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Inter (-0.5)").decimal(1.95).handicap(-0.5).build(),
                                        BetnacionalOutcomeDto.builder().name("Milan (+0.5)").decimal(1.85).handicap(0.5).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().stream().filter(o -> o.getName().contains("Inter")).findFirst().orElseThrow();
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet h1Bet = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, h1Bet.outcome());
        assertEquals(-0.5, h1Bet.param());

        OddItem h2 = request.getOdds().stream().filter(o -> o.getName().contains("Milan")).findFirst().orElseThrow();
        assertTrue(h2.getBetType() instanceof HandicapBet);
        HandicapBet h2Bet = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, h2Bet.outcome());
        assertEquals(0.5, h2Bet.param());
    }

    @Test
    void testEsportsCS2Markets() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-cs2-1")
                .sportName("CS2")
                .leagueName("ESL Pro League")
                .homeTeam("Natus Vincere")
                .awayTeam("FaZe Clan")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Vencedor do Encontro")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Natus Vincere").decimal(1.65).build(),
                                        BetnacionalOutcomeDto.builder().name("FaZe Clan").decimal(2.25).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("1º Mapa - Vencedor")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Natus Vincere").decimal(1.72).build(),
                                        BetnacionalOutcomeDto.builder().name("FaZe Clan").decimal(2.05).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Handicap de Mapas -1.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Natus Vincere (-1.5)").handicap(-1.5).decimal(2.60).build(),
                                        BetnacionalOutcomeDto.builder().name("FaZe Clan (+1.5)").handicap(1.5).decimal(1.45).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.CS2, request.getSportType());
        assertEquals(6, request.getOdds().size());

        OddItem matchWinner = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_match_winner") && o.getName().equals("Natus Vincere"))
                .findFirst().orElseThrow();
        assertEquals(1.65, matchWinner.getValue());
        MatchResultBet mwBet = (MatchResultBet) matchWinner.getBetType();
        assertEquals(BetScope.FULL_MATCH, mwBet.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mwBet.outcome());

        OddItem map1Winner = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_winner") && o.getName().equals("Natus Vincere"))
                .findFirst().orElseThrow();
        assertEquals(1.72, map1Winner.getValue());
        MatchResultBet mBet = (MatchResultBet) map1Winner.getBetType();
        assertEquals(BetScope.MAP_1, mBet.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mBet.outcome());

        OddItem mapHdc = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_handicap") && o.getName().contains("Natus"))
                .findFirst().orElseThrow();
        assertEquals(2.60, mapHdc.getValue());
        HandicapBet hBet = (HandicapBet) mapHdc.getBetType();
        assertEquals(-1.5, hBet.param());
        assertEquals(StatType.MAPS, hBet.statType());
    }

    @Test
    void testEsportsCS2RoundsAndHandicap() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-cs2-rounds")
                .sportName("CS:GO")
                .homeTeam("G2 Esports")
                .awayTeam("Vitality")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("1º Mapa Total de Rounds Mais/Menos 21.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 21.5").handicap(21.5).decimal(1.85).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 21.5").handicap(21.5).decimal(1.95).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Handicap de Rounds - 1º Mapa")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("G2 Esports (-2.5)").handicap(-2.5).decimal(2.10).build(),
                                        BetnacionalOutcomeDto.builder().name("Vitality (+2.5)").handicap(2.5).decimal(1.70).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.CS2, request.getSportType());
        assertEquals(4, request.getOdds().size());

        OddItem totalRounds = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_total_rounds") && o.getName().startsWith("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.85, totalRounds.getValue());
        TotalBet rBet = (TotalBet) totalRounds.getBetType();
        assertEquals(BetScope.MAP_1, rBet.scope());
        assertEquals(21.5, rBet.param());
        assertEquals(StatType.ROUNDS, rBet.statType());

        OddItem roundHdc = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_round_handicap") && o.getName().contains("G2"))
                .findFirst().orElseThrow();
        assertEquals(2.10, roundHdc.getValue());
        HandicapBet hBet = (HandicapBet) roundHdc.getBetType();
        assertEquals(BetScope.MAP_1, hBet.scope());
        assertEquals(-2.5, hBet.param());
        assertEquals(StatType.ROUNDS, hBet.statType());
    }

    @Test
    void testEsportsDota2Markets() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-dota2-1")
                .sportName("Dota 2")
                .leagueName("The International")
                .homeTeam("Team Spirit")
                .awayTeam("Team Liquid")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("1º Mapa - Vencedor")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Team Spirit").decimal(1.60).build(),
                                        BetnacionalOutcomeDto.builder().name("Team Liquid").decimal(2.30).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Mapa 1 Total de Kills Mais/Menos 48.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 48.5").handicap(48.5).decimal(1.90).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 48.5").handicap(48.5).decimal(1.90).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("1º Mapa - Primeiro Abate")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Team Spirit").decimal(1.80).build(),
                                        BetnacionalOutcomeDto.builder().name("Team Liquid").decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.DOTA2, request.getSportType());
        assertEquals(6, request.getOdds().size());

        OddItem g1Winner = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_winner") && o.getName().equals("Team Spirit"))
                .findFirst().orElseThrow();
        assertEquals(1.60, g1Winner.getValue());
        MatchResultBet mBet = (MatchResultBet) g1Winner.getBetType();
        assertEquals(BetScope.MAP_1, mBet.scope());
        assertEquals(MatchResultBet.Outcome.WIN1, mBet.outcome());

        OddItem killsOver = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_total_kills") && o.getName().startsWith("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.90, killsOver.getValue());
        TotalBet kBet = (TotalBet) killsOver.getBetType();
        assertEquals(BetScope.MAP_1, kBet.scope());
        assertEquals(48.5, kBet.param());
        assertEquals(StatType.KILLS, kBet.statType());

        OddItem fb = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_first_blood") && o.getName().equals("Team Spirit"))
                .findFirst().orElseThrow();
        assertEquals(1.80, fb.getValue());
        BinaryMarketBet fbBet = (BinaryMarketBet) fb.getBetType();
        assertEquals(BetScope.MAP_1, fbBet.scope());
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, fbBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, fbBet.outcome());
    }

    @Test
    void testEsportsLoLMarkets() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-lol-1")
                .sportName("Esports")
                .leagueName("League of Legends CBLOL")
                .homeTeam("LOUD")
                .awayTeam("paiN Gaming")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Total de Mapas Mais/Menos 2.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 2.5").handicap(2.5).decimal(2.00).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 2.5").handicap(2.5).decimal(1.75).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Mapa 1 Primeiro Sangue")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Sim").decimal(1.85).build(),
                                        BetnacionalOutcomeDto.builder().name("Não").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.LEAGUE_OF_LEGENDS, request.getSportType());
        assertEquals(4, request.getOdds().size());

        OddItem totalGames = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_total_maps") && o.getName().startsWith("Mais"))
                .findFirst().orElseThrow();
        assertEquals(2.00, totalGames.getValue());
        TotalBet gBet = (TotalBet) totalGames.getBetType();
        assertEquals(StatType.MAPS, gBet.statType());
        assertEquals(2.5, gBet.param());

        OddItem fbSim = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_first_blood") && o.getName().equals("Sim"))
                .findFirst().orElseThrow();
        assertEquals(1.85, fbSim.getValue());
        BinaryMarketBet fbBet = (BinaryMarketBet) fbSim.getBetType();
        assertEquals(BinaryMarketBet.Outcome.YES, fbBet.outcome());
    }

    @Test
    void testEsportsValorantMarkets() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-val-1")
                .sportName("Valorant")
                .leagueName("VCT Americas")
                .homeTeam("Sentinels")
                .awayTeam("Paper Rex")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Vencedor da Partida")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Sentinels").decimal(1.75).build(),
                                        BetnacionalOutcomeDto.builder().name("Paper Rex").decimal(2.10).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("1º Mapa Total de Rodadas Mais/Menos 22.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 22.5").handicap(22.5).decimal(1.80).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 22.5").handicap(22.5).decimal(1.95).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Handicap de Abates - 1º Mapa")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Sentinels (-3.5)").handicap(-3.5).decimal(1.95).build(),
                                        BetnacionalOutcomeDto.builder().name("Paper Rex (+3.5)").handicap(3.5).decimal(1.85).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.VALORANT, request.getSportType());
        assertEquals(6, request.getOdds().size());

        OddItem mw = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_match_winner") && o.getName().equals("Sentinels"))
                .findFirst().orElseThrow();
        assertEquals(1.75, mw.getValue());

        OddItem rounds = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_total_rounds") && o.getName().startsWith("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.80, rounds.getValue());
        TotalBet rBet = (TotalBet) rounds.getBetType();
        assertEquals(BetScope.MAP_1, rBet.scope());
        assertEquals(StatType.ROUNDS, rBet.statType());

        OddItem killsHdc = request.getOdds().stream()
                .filter(o -> o.getGroupName().equals("esports_map_1_kill_handicap") && o.getName().contains("Sentinels"))
                .findFirst().orElseThrow();
        assertEquals(1.95, killsHdc.getValue());
        HandicapBet hBet = (HandicapBet) killsHdc.getBetType();
        assertEquals(BetScope.MAP_1, hBet.scope());
        assertEquals(-3.5, hBet.param());
        assertEquals(StatType.KILLS, hBet.statType());
    }

    @Test
    void testCornersTotalsMatchHalfTeam() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-corners-1")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Total de Escanteios Mais/Menos 9.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 9.5").handicap(9.5).decimal(1.85).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 9.5").handicap(9.5).decimal(1.95).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("1º Tempo - Escanteios Mais/Menos 4.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 4.5").handicap(4.5).decimal(1.90).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 4.5").handicap(4.5).decimal(1.90).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Flamengo - Total de Escanteios Mais/Menos 5.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 5.5").handicap(5.5).decimal(1.75).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 5.5").handicap(5.5).decimal(2.05).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Palmeiras - Total de Escanteios Mais/Menos 4.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 4.5").handicap(4.5).decimal(2.10).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 4.5").handicap(4.5).decimal(1.70).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(8, request.getOdds().size());

        // Match total
        OddItem cTotalOver = request.getOdds().stream()
                .filter(o -> "corners_total".equals(o.getGroupName()) && o.getName().contains("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.85, cTotalOver.getValue());
        TotalBet cBet = (TotalBet) cTotalOver.getBetType();
        assertEquals(StatType.CORNERS, cBet.statType());
        assertEquals(BetScope.FULL_MATCH, cBet.scope());
        assertEquals(BetSubject.MATCH, cBet.subject());
        assertEquals(TotalBet.Direction.OVER, cBet.direction());
        assertEquals(9.5, cBet.param());

        OddItem cTotalUnder = request.getOdds().stream()
                .filter(o -> "corners_total".equals(o.getGroupName()) && o.getName().contains("Menos"))
                .findFirst().orElseThrow();
        assertEquals(1.95, cTotalUnder.getValue());
        TotalBet uBet = (TotalBet) cTotalUnder.getBetType();
        assertEquals(TotalBet.Direction.UNDER, uBet.direction());

        // Half 1 total
        OddItem h1Over = request.getOdds().stream()
                .filter(o -> "corners_total_half_1".equals(o.getGroupName()) && o.getName().contains("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.90, h1Over.getValue());
        TotalBet h1Bet = (TotalBet) h1Over.getBetType();
        assertEquals(StatType.CORNERS, h1Bet.statType());
        assertEquals(BetScope.HALF_1, h1Bet.scope());
        assertEquals(BetSubject.MATCH, h1Bet.subject());
        assertEquals(4.5, h1Bet.param());

        // Team 1 total
        OddItem t1Over = request.getOdds().stream()
                .filter(o -> "corners_total_team1".equals(o.getGroupName()) && o.getName().contains("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.75, t1Over.getValue());
        TotalBet t1Bet = (TotalBet) t1Over.getBetType();
        assertEquals(StatType.CORNERS, t1Bet.statType());
        assertEquals(BetSubject.TEAM1, t1Bet.subject());
        assertEquals(5.5, t1Bet.param());

        // Team 2 total
        OddItem t2Over = request.getOdds().stream()
                .filter(o -> "corners_total_team2".equals(o.getGroupName()) && o.getName().contains("Mais"))
                .findFirst().orElseThrow();
        assertEquals(2.10, t2Over.getValue());
        TotalBet t2Bet = (TotalBet) t2Over.getBetType();
        assertEquals(StatType.CORNERS, t2Bet.statType());
        assertEquals(BetSubject.TEAM2, t2Bet.subject());
        assertEquals(4.5, t2Bet.param());
    }

    @Test
    void testCornersHandicapsMatchAndHalves() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-corners-2")
                .sportName("Futebol")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Handicap de Escanteios")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Real Madrid (-1.5)").handicap(-1.5).decimal(1.90).build(),
                                        BetnacionalOutcomeDto.builder().name("Barcelona (+1.5)").handicap(1.5).decimal(1.90).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Handicap Asiático de Escanteios")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Real Madrid (-2.0)").handicap(-2.0).decimal(2.30).build(),
                                        BetnacionalOutcomeDto.builder().name("Barcelona (+2.0)").handicap(2.0).decimal(1.60).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("1º Tempo - Handicap de Escanteios")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Real Madrid (-0.5)").handicap(-0.5).decimal(1.80).build(),
                                        BetnacionalOutcomeDto.builder().name("Barcelona (+0.5)").handicap(0.5).decimal(2.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(6, request.getOdds().size());

        // Match handicap
        OddItem h1 = request.getOdds().stream()
                .filter(o -> "corners_handicap".equals(o.getGroupName()) && o.getName().contains("Real Madrid"))
                .findFirst().orElseThrow();
        assertEquals(1.90, h1.getValue());
        HandicapBet h1Bet = (HandicapBet) h1.getBetType();
        assertEquals(StatType.CORNERS, h1Bet.statType());
        assertEquals(BetScope.FULL_MATCH, h1Bet.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1Bet.outcome());
        assertEquals(-1.5, h1Bet.param());

        // Asian handicap
        OddItem ah1 = request.getOdds().stream()
                .filter(o -> "corners_asian_handicap".equals(o.getGroupName()) && o.getName().contains("Real Madrid"))
                .findFirst().orElseThrow();
        assertEquals(2.30, ah1.getValue());
        HandicapBet ah1Bet = (HandicapBet) ah1.getBetType();
        assertEquals(StatType.CORNERS, ah1Bet.statType());
        assertEquals(-2.0, ah1Bet.param());

        // Half 1 handicap
        OddItem h1Half = request.getOdds().stream()
                .filter(o -> "corners_handicap_half_1".equals(o.getGroupName()) && o.getName().contains("Real Madrid"))
                .findFirst().orElseThrow();
        assertEquals(1.80, h1Half.getValue());
        HandicapBet halfBet = (HandicapBet) h1Half.getBetType();
        assertEquals(StatType.CORNERS, halfBet.statType());
        assertEquals(BetScope.HALF_1, halfBet.scope());
        assertEquals(-0.5, halfBet.param());
    }

    @Test
    void testCorners1X2FirstLastAndSpecial() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-corners-3")
                .sportName("Soccer")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Vencedor dos Escanteios")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Arsenal").decimal(1.65).build(),
                                        BetnacionalOutcomeDto.builder().name("Empate").decimal(7.50).build(),
                                        BetnacionalOutcomeDto.builder().name("Chelsea").decimal(2.80).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Primeiro Escanteio")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Arsenal").decimal(1.70).build(),
                                        BetnacionalOutcomeDto.builder().name("Chelsea").decimal(2.10).build(),
                                        BetnacionalOutcomeDto.builder().name("Nenhum").decimal(15.0).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Último Escanteio")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Arsenal").decimal(1.80).build(),
                                        BetnacionalOutcomeDto.builder().name("Chelsea").decimal(2.00).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Dupla Chance - Escanteios")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("1X").decimal(1.25).build(),
                                        BetnacionalOutcomeDto.builder().name("12").decimal(1.15).build(),
                                        BetnacionalOutcomeDto.builder().name("X2").decimal(1.85).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Empate Anula - Escanteios")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Arsenal").decimal(1.40).build(),
                                        BetnacionalOutcomeDto.builder().name("Chelsea").decimal(2.75).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(13, request.getOdds().size());

        // 1X2
        OddItem c1 = request.getOdds().stream()
                .filter(o -> "corners_1x2".equals(o.getGroupName()) && "Arsenal".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.65, c1.getValue());
        MatchResultBet mr1 = (MatchResultBet) c1.getBetType();
        assertEquals(StatType.CORNERS, mr1.statType());
        assertEquals(MatchResultBet.Outcome.WIN1, mr1.outcome());

        OddItem cX = request.getOdds().stream()
                .filter(o -> "corners_1x2".equals(o.getGroupName()) && "Empate".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(7.50, cX.getValue());
        MatchResultBet mrX = (MatchResultBet) cX.getBetType();
        assertEquals(StatType.CORNERS, mrX.statType());
        assertEquals(MatchResultBet.Outcome.DRAW, mrX.outcome());

        OddItem c2 = request.getOdds().stream()
                .filter(o -> "corners_1x2".equals(o.getGroupName()) && "Chelsea".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(2.80, c2.getValue());
        MatchResultBet mr2 = (MatchResultBet) c2.getBetType();
        assertEquals(StatType.CORNERS, mr2.statType());
        assertEquals(MatchResultBet.Outcome.WIN2, mr2.outcome());

        // First Corner
        OddItem fc1 = request.getOdds().stream()
                .filter(o -> "corners_first".equals(o.getGroupName()) && "Arsenal".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.70, fc1.getValue());
        BinaryMarketBet b1 = (BinaryMarketBet) fc1.getBetType();
        assertEquals(StatType.CORNERS, b1.statType());
        assertEquals(BinaryMarketBet.MarketType.FIRST_CORNER, b1.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, b1.outcome());

        OddItem fcNone = request.getOdds().stream()
                .filter(o -> "corners_first".equals(o.getGroupName()) && "Nenhum".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(15.0, fcNone.getValue());
        BinaryMarketBet bNone = (BinaryMarketBet) fcNone.getBetType();
        assertEquals(BinaryMarketBet.Outcome.NO, bNone.outcome());

        // Last Corner
        OddItem lc1 = request.getOdds().stream()
                .filter(o -> "corners_last".equals(o.getGroupName()) && "Arsenal".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.80, lc1.getValue());
        BinaryMarketBet lb1 = (BinaryMarketBet) lc1.getBetType();
        assertEquals(BinaryMarketBet.MarketType.LAST_CORNER, lb1.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, lb1.outcome());

        // Double Chance
        OddItem dc1X = request.getOdds().stream()
                .filter(o -> "corners_double_chance".equals(o.getGroupName()) && "1X".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.25, dc1X.getValue());
        MatchResultBet dcBet = (MatchResultBet) dc1X.getBetType();
        assertEquals(StatType.CORNERS, dcBet.statType());
        assertEquals(MatchResultBet.Outcome.DC_1X, dcBet.outcome());

        // Draw No Bet
        OddItem dnb1 = request.getOdds().stream()
                .filter(o -> "corners_draw_no_bet".equals(o.getGroupName()) && "Arsenal".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.40, dnb1.getValue());
        HandicapBet dnbBet = (HandicapBet) dnb1.getBetType();
        assertEquals(StatType.CORNERS, dnbBet.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, dnbBet.outcome());
        assertEquals(0.0, dnbBet.param());
    }

    @Test
    void testCardsTotalsMatchHalfTeam() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-cards-1")
                .sportName("Futebol")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Total de Cartões Amarelos Mais/Menos 4.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 4.5").handicap(4.5).decimal(1.85).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 4.5").handicap(4.5).decimal(1.95).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("1º Tempo - Total de Cartões Mais/Menos 1.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 1.5").handicap(1.5).decimal(1.90).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 1.5").handicap(1.5).decimal(1.80).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("2º Tempo - Total de Cartões Mais/Menos 2.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 2.5").handicap(2.5).decimal(2.05).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 2.5").handicap(2.5).decimal(1.70).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Flamengo - Total de Cartões Mais/Menos 2.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 2.5").handicap(2.5).decimal(1.75).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 2.5").handicap(2.5).decimal(2.00).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Palmeiras - Total de Cartões Mais/Menos 2.5")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Mais 2.5").handicap(2.5).decimal(1.90).build(),
                                        BetnacionalOutcomeDto.builder().name("Menos 2.5").handicap(2.5).decimal(1.85).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(10, request.getOdds().size());

        // Match total
        OddItem cTotalOver = request.getOdds().stream()
                .filter(o -> "cards_total".equals(o.getGroupName()) && o.getName().contains("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.85, cTotalOver.getValue());
        TotalBet cBet = (TotalBet) cTotalOver.getBetType();
        assertEquals(StatType.YELLOW_CARDS, cBet.statType());
        assertEquals(BetScope.FULL_MATCH, cBet.scope());
        assertEquals(BetSubject.MATCH, cBet.subject());
        assertEquals(TotalBet.Direction.OVER, cBet.direction());
        assertEquals(4.5, cBet.param());

        OddItem cTotalUnder = request.getOdds().stream()
                .filter(o -> "cards_total".equals(o.getGroupName()) && o.getName().contains("Menos"))
                .findFirst().orElseThrow();
        assertEquals(1.95, cTotalUnder.getValue());
        TotalBet uBet = (TotalBet) cTotalUnder.getBetType();
        assertEquals(TotalBet.Direction.UNDER, uBet.direction());

        // Half 1 total
        OddItem h1Over = request.getOdds().stream()
                .filter(o -> "cards_total_half_1".equals(o.getGroupName()) && o.getName().contains("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.90, h1Over.getValue());
        TotalBet h1Bet = (TotalBet) h1Over.getBetType();
        assertEquals(StatType.YELLOW_CARDS, h1Bet.statType());
        assertEquals(BetScope.HALF_1, h1Bet.scope());
        assertEquals(BetSubject.MATCH, h1Bet.subject());
        assertEquals(1.5, h1Bet.param());

        // Half 2 total
        OddItem h2Over = request.getOdds().stream()
                .filter(o -> "cards_total_half_2".equals(o.getGroupName()) && o.getName().contains("Mais"))
                .findFirst().orElseThrow();
        assertEquals(2.05, h2Over.getValue());
        TotalBet h2Bet = (TotalBet) h2Over.getBetType();
        assertEquals(StatType.YELLOW_CARDS, h2Bet.statType());
        assertEquals(BetScope.HALF_2, h2Bet.scope());
        assertEquals(BetSubject.MATCH, h2Bet.subject());
        assertEquals(2.5, h2Bet.param());

        // Team 1 total
        OddItem t1Over = request.getOdds().stream()
                .filter(o -> "cards_total_team1".equals(o.getGroupName()) && o.getName().contains("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.75, t1Over.getValue());
        TotalBet t1Bet = (TotalBet) t1Over.getBetType();
        assertEquals(StatType.YELLOW_CARDS, t1Bet.statType());
        assertEquals(BetSubject.TEAM1, t1Bet.subject());
        assertEquals(2.5, t1Bet.param());

        // Team 2 total
        OddItem t2Over = request.getOdds().stream()
                .filter(o -> "cards_total_team2".equals(o.getGroupName()) && o.getName().contains("Mais"))
                .findFirst().orElseThrow();
        assertEquals(1.90, t2Over.getValue());
        TotalBet t2Bet = (TotalBet) t2Over.getBetType();
        assertEquals(StatType.YELLOW_CARDS, t2Bet.statType());
        assertEquals(BetSubject.TEAM2, t2Bet.subject());
        assertEquals(2.5, t2Bet.param());
    }

    @Test
    void testCardsHandicapsMatchAndHalves() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-cards-2")
                .sportName("Futebol")
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Handicap de Cartões")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Real Madrid (-0.5)").handicap(-0.5).decimal(1.85).build(),
                                        BetnacionalOutcomeDto.builder().name("Barcelona (+0.5)").handicap(0.5).decimal(1.95).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Handicap Asiático de Cartões")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Real Madrid (-1.0)").handicap(-1.0).decimal(2.40).build(),
                                        BetnacionalOutcomeDto.builder().name("Barcelona (+1.0)").handicap(1.0).decimal(1.55).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("1º Tempo - Handicap de Cartões")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Real Madrid (0.0)").handicap(0.0).decimal(1.80).build(),
                                        BetnacionalOutcomeDto.builder().name("Barcelona (0.0)").handicap(0.0).decimal(2.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(6, request.getOdds().size());

        // Match handicap
        OddItem h1 = request.getOdds().stream()
                .filter(o -> "cards_handicap".equals(o.getGroupName()) && o.getName().contains("Real Madrid"))
                .findFirst().orElseThrow();
        assertEquals(1.85, h1.getValue());
        HandicapBet h1Bet = (HandicapBet) h1.getBetType();
        assertEquals(StatType.YELLOW_CARDS, h1Bet.statType());
        assertEquals(BetScope.FULL_MATCH, h1Bet.scope());
        assertEquals(HandicapBet.Outcome.TEAM1, h1Bet.outcome());
        assertEquals(-0.5, h1Bet.param());

        // Asian handicap
        OddItem ah1 = request.getOdds().stream()
                .filter(o -> "cards_asian_handicap".equals(o.getGroupName()) && o.getName().contains("Real Madrid"))
                .findFirst().orElseThrow();
        assertEquals(2.40, ah1.getValue());
        HandicapBet ah1Bet = (HandicapBet) ah1.getBetType();
        assertEquals(StatType.YELLOW_CARDS, ah1Bet.statType());
        assertEquals(-1.0, ah1Bet.param());
        assertTrue(ah1Bet.isAsian());

        // Half 1 handicap
        OddItem h1Half = request.getOdds().stream()
                .filter(o -> "cards_handicap_half_1".equals(o.getGroupName()) && o.getName().contains("Real Madrid"))
                .findFirst().orElseThrow();
        assertEquals(1.80, h1Half.getValue());
        HandicapBet halfBet = (HandicapBet) h1Half.getBetType();
        assertEquals(StatType.YELLOW_CARDS, halfBet.statType());
        assertEquals(BetScope.HALF_1, halfBet.scope());
        assertEquals(0.0, halfBet.param());
    }

    @Test
    void testCards1X2AndRedCardAndSpecials() {
        BetnacionalEventDto event = BetnacionalEventDto.builder()
                .id("ev-cards-3")
                .sportName("Soccer")
                .homeTeam("Flamengo")
                .awayTeam("Palmeiras")
                .markets(List.of(
                        BetnacionalMarketDto.builder()
                                .name("Vencedor dos Cartões")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Flamengo").decimal(2.20).build(),
                                        BetnacionalOutcomeDto.builder().name("Empate").decimal(4.20).build(),
                                        BetnacionalOutcomeDto.builder().name("Palmeiras").decimal(2.60).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Cartão Vermelho")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Sim").decimal(3.50).build(),
                                        BetnacionalOutcomeDto.builder().name("Não").decimal(1.25).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Primeiro Cartão")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Flamengo").decimal(1.80).build(),
                                        BetnacionalOutcomeDto.builder().name("Palmeiras").decimal(2.05).build(),
                                        BetnacionalOutcomeDto.builder().name("Nenhum").decimal(12.0).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Dupla Chance - Cartões")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("1X").decimal(1.40).build(),
                                        BetnacionalOutcomeDto.builder().name("12").decimal(1.25).build(),
                                        BetnacionalOutcomeDto.builder().name("X2").decimal(1.55).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Empate Anula - Cartões")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Flamengo").decimal(1.65).build(),
                                        BetnacionalOutcomeDto.builder().name("Palmeiras").decimal(2.10).build()
                                ))
                                .build(),
                        BetnacionalMarketDto.builder()
                                .name("Total de Cartões Par/Ímpar")
                                .outcomes(List.of(
                                        BetnacionalOutcomeDto.builder().name("Par").decimal(1.90).build(),
                                        BetnacionalOutcomeDto.builder().name("Ímpar").decimal(1.90).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(15, request.getOdds().size());

        // 1X2
        OddItem c1 = request.getOdds().stream()
                .filter(o -> "cards_1x2".equals(o.getGroupName()) && "Flamengo".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(2.20, c1.getValue());
        MatchResultBet mr1 = (MatchResultBet) c1.getBetType();
        assertEquals(StatType.YELLOW_CARDS, mr1.statType());
        assertEquals(MatchResultBet.Outcome.WIN1, mr1.outcome());

        OddItem cX = request.getOdds().stream()
                .filter(o -> "cards_1x2".equals(o.getGroupName()) && "Empate".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(4.20, cX.getValue());
        MatchResultBet mrX = (MatchResultBet) cX.getBetType();
        assertEquals(StatType.YELLOW_CARDS, mrX.statType());
        assertEquals(MatchResultBet.Outcome.DRAW, mrX.outcome());

        OddItem c2 = request.getOdds().stream()
                .filter(o -> "cards_1x2".equals(o.getGroupName()) && "Palmeiras".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(2.60, c2.getValue());
        MatchResultBet mr2 = (MatchResultBet) c2.getBetType();
        assertEquals(StatType.YELLOW_CARDS, mr2.statType());
        assertEquals(MatchResultBet.Outcome.WIN2, mr2.outcome());

        // Red Card
        OddItem rcYes = request.getOdds().stream()
                .filter(o -> "cards_red_card".equals(o.getGroupName()) && "Sim".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(3.50, rcYes.getValue());
        BinaryMarketBet rcYesBet = (BinaryMarketBet) rcYes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.RED_CARD, rcYesBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, rcYesBet.outcome());
        assertEquals(StatType.YELLOW_CARDS, rcYesBet.statType());

        OddItem rcNo = request.getOdds().stream()
                .filter(o -> "cards_red_card".equals(o.getGroupName()) && "Não".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.25, rcNo.getValue());
        BinaryMarketBet rcNoBet = (BinaryMarketBet) rcNo.getBetType();
        assertEquals(BinaryMarketBet.MarketType.RED_CARD, rcNoBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.NO, rcNoBet.outcome());

        // First Card
        OddItem fc1 = request.getOdds().stream()
                .filter(o -> "cards_first".equals(o.getGroupName()) && "Flamengo".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.80, fc1.getValue());
        BinaryMarketBet b1 = (BinaryMarketBet) fc1.getBetType();
        assertEquals(StatType.YELLOW_CARDS, b1.statType());
        assertEquals(BinaryMarketBet.MarketType.FIRST_CARD, b1.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, b1.outcome());

        OddItem fcNone = request.getOdds().stream()
                .filter(o -> "cards_first".equals(o.getGroupName()) && "Nenhum".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(12.0, fcNone.getValue());
        BinaryMarketBet bNone = (BinaryMarketBet) fcNone.getBetType();
        assertEquals(BinaryMarketBet.Outcome.NO, bNone.outcome());

        // Double Chance
        OddItem dc1X = request.getOdds().stream()
                .filter(o -> "cards_double_chance".equals(o.getGroupName()) && "1X".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.40, dc1X.getValue());
        MatchResultBet dcBet = (MatchResultBet) dc1X.getBetType();
        assertEquals(StatType.YELLOW_CARDS, dcBet.statType());
        assertEquals(MatchResultBet.Outcome.DC_1X, dcBet.outcome());

        // Draw No Bet
        OddItem dnb1 = request.getOdds().stream()
                .filter(o -> "cards_draw_no_bet".equals(o.getGroupName()) && "Flamengo".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.65, dnb1.getValue());
        HandicapBet dnbBet = (HandicapBet) dnb1.getBetType();
        assertEquals(StatType.YELLOW_CARDS, dnbBet.statType());
        assertEquals(HandicapBet.Outcome.TEAM1, dnbBet.outcome());
        assertEquals(0.0, dnbBet.param());

        // Odd / Even
        OddItem odd = request.getOdds().stream()
                .filter(o -> "cards_odd_even".equals(o.getGroupName()) && "Ímpar".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.90, odd.getValue());
        BinaryMarketBet oddBet = (BinaryMarketBet) odd.getBetType();
        assertEquals(StatType.YELLOW_CARDS, oddBet.statType());
        assertEquals(BinaryMarketBet.MarketType.ODD_EVEN, oddBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.ODD, oddBet.outcome());

        OddItem even = request.getOdds().stream()
                .filter(o -> "cards_odd_even".equals(o.getGroupName()) && "Par".equals(o.getName()))
                .findFirst().orElseThrow();
        assertEquals(1.90, even.getValue());
        BinaryMarketBet evenBet = (BinaryMarketBet) even.getBetType();
        assertEquals(StatType.YELLOW_CARDS, evenBet.statType());
        assertEquals(BinaryMarketBet.MarketType.ODD_EVEN, evenBet.marketType());
        assertEquals(BinaryMarketBet.Outcome.EVEN, evenBet.outcome());
    }
}
