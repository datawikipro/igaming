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
                                        BetesporteOutcomeDto.builder().name("1X").odds(1.35).build(),
                                        BetesporteOutcomeDto.builder().name("12").odds(1.28).build(),
                                        BetesporteOutcomeDto.builder().name("X2").odds(1.55).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());
    }

    @Test
    void testTotalOverUnder() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-102")
                .sportName("Futebol")
                .homeTeam("Santos")
                .awayTeam("Corinthians")
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
        assertTrue(over.getBetType() instanceof TotalBet);
        TotalBet tbOver = (TotalBet) over.getBetType();
        assertEquals(TotalBet.Direction.OVER, tbOver.direction());
        assertEquals(2.5, tbOver.param());

        OddItem under = request.getOdds().stream().filter(o -> o.getName().contains("Menos")).findFirst().orElseThrow();
        assertTrue(under.getBetType() instanceof TotalBet);
        TotalBet tbUnder = (TotalBet) under.getBetType();
        assertEquals(TotalBet.Direction.UNDER, tbUnder.direction());
        assertEquals(2.5, tbUnder.param());
    }

    @Test
    void testTeamTotal() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-103")
                .sportName("Soccer")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Total Casa - Mais/Menos")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 1.5").decimal(1.70).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 1.5").decimal(2.15).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem item = request.getOdds().get(0);
        assertTrue(item.getBetType() instanceof TotalBet);
        assertEquals(BetSubject.TEAM1, ((TotalBet) item.getBetType()).subject());
    }

    @Test
    void testHandicap() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-104")
                .sportName("Futebol")
                .homeTeam("Liverpool")
                .awayTeam("Everton")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Handicap Asiatico")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Liverpool (-1.5)").decimal(2.05).build(),
                                        BetesporteOutcomeDto.builder().name("Everton (+1.5)").decimal(1.80).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem h1 = request.getOdds().stream().filter(o -> o.getName().contains("Liverpool")).findFirst().orElseThrow();
        assertTrue(h1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) h1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(-1.5, hb1.param());

        OddItem h2 = request.getOdds().stream().filter(o -> o.getName().contains("Everton")).findFirst().orElseThrow();
        assertTrue(h2.getBetType() instanceof HandicapBet);
        HandicapBet hb2 = (HandicapBet) h2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(1.5, hb2.param());
    }

    @Test
    void testHalvesScope() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-105")
                .sportName("Futebol")
                .homeTeam("PSG")
                .awayTeam("Marseille")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("1º Tempo - Resultado Final")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("PSG").decimal(2.20).build(),
                                        BetesporteOutcomeDto.builder().name("Empate").decimal(2.40).build(),
                                        BetesporteOutcomeDto.builder().name("Marseille").decimal(4.50).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());
        assertEquals(BetScope.HALF_1, ((MatchResultBet) request.getOdds().get(0).getBetType()).scope());
    }

    @Test
    void testBothTeamsToScore() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-btts")
                .sportName("Futebol")
                .homeTeam("Inter")
                .awayTeam("Milan")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Ambas Marcam")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Sim").decimal(1.75).build(),
                                        BetesporteOutcomeDto.builder().name("Não").decimal(2.05).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem yes = request.getOdds().stream().filter(o -> "Sim".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(yes.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbYes = (BinaryMarketBet) yes.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bmbYes.marketType());
        assertEquals(BinaryMarketBet.Outcome.YES, bmbYes.outcome());

        OddItem no = request.getOdds().stream().filter(o -> "Não".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(no.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbNo = (BinaryMarketBet) no.getBetType();
        assertEquals(BinaryMarketBet.MarketType.BTTS, bmbNo.marketType());
        assertEquals(BinaryMarketBet.Outcome.NO, bmbNo.outcome());
    }

    @Test
    void testDrawNoBet() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-dnb")
                .sportName("Futebol")
                .homeTeam("Benfica")
                .awayTeam("Porto")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Empate Anula Aposta")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Benfica").decimal(1.65).build(),
                                        BetesporteOutcomeDto.builder().name("Porto").decimal(2.25).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem t1 = request.getOdds().stream().filter(o -> "Benfica".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(t1.getBetType() instanceof HandicapBet);
        HandicapBet hb1 = (HandicapBet) t1.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM1, hb1.outcome());
        assertEquals(0.0, hb1.param());

        OddItem t2 = request.getOdds().stream().filter(o -> "Porto".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(t2.getBetType() instanceof HandicapBet);
        HandicapBet hb2 = (HandicapBet) t2.getBetType();
        assertEquals(HandicapBet.Outcome.TEAM2, hb2.outcome());
        assertEquals(0.0, hb2.param());
    }

    @Test
    void testCorrectScore() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-cs")
                .sportName("Futebol")
                .homeTeam("Bayern")
                .awayTeam("Dortmund")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Resultado Exato")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("2 - 1").decimal(8.50).build(),
                                        BetesporteOutcomeDto.builder().name("1 - 1").decimal(7.00).build(),
                                        BetesporteOutcomeDto.builder().name("Outro").decimal(4.20).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(3, request.getOdds().size());

        OddItem s21 = request.getOdds().stream().filter(o -> "2 - 1".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(s21.getBetType() instanceof CorrectScoreBet);
        CorrectScoreBet cs21 = (CorrectScoreBet) s21.getBetType();
        assertEquals(2, cs21.score1());
        assertEquals(1, cs21.score2());
        assertFalse(cs21.isAnyOtherScore());

        OddItem other = request.getOdds().stream().filter(o -> "Outro".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(other.getBetType() instanceof CorrectScoreBet);
        CorrectScoreBet csOther = (CorrectScoreBet) other.getBetType();
        assertTrue(csOther.isAnyOtherScore());
    }

    @Test
    void testHalfTimeFullTime() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-htft")
                .sportName("Futebol")
                .homeTeam("Chelsea")
                .awayTeam("Arsenal")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Intervalo / Final")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("1/1").decimal(4.50).build(),
                                        BetesporteOutcomeDto.builder().name("X/2").decimal(6.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem w1w1 = request.getOdds().stream().filter(o -> "1/1".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(w1w1.getBetType() instanceof HalfTimeFullTimeBet);
        assertEquals(HalfTimeFullTimeBet.Outcome.W1_W1, ((HalfTimeFullTimeBet) w1w1.getBetType()).outcome());

        OddItem xw2 = request.getOdds().stream().filter(o -> "X/2".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(xw2.getBetType() instanceof HalfTimeFullTimeBet);
        assertEquals(HalfTimeFullTimeBet.Outcome.X_W2, ((HalfTimeFullTimeBet) xw2.getBetType()).outcome());
    }

    @Test
    void testCornersMarket() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-corners")
                .sportName("Futebol")
                .homeTeam("Liverpool")
                .awayTeam("City")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Total de Escanteios - Mais/Menos")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 9.5").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 9.5").decimal(1.95).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Handicap de Escanteios")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Liverpool (-1.5)").decimal(1.90).build(),
                                        BetesporteOutcomeDto.builder().name("City (+1.5)").decimal(1.90).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Vencedor dos Escanteios")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Liverpool").decimal(1.70).build(),
                                        BetesporteOutcomeDto.builder().name("Empate").decimal(7.50).build(),
                                        BetesporteOutcomeDto.builder().name("City").decimal(2.40).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(7, request.getOdds().size());

        OddItem cornerOver = request.getOdds().stream().filter(o -> "Mais 9.5".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(cornerOver.getBetType() instanceof TotalBet);
        TotalBet tbCorner = (TotalBet) cornerOver.getBetType();
        assertEquals(StatType.CORNERS, tbCorner.statType());
        assertEquals(9.5, tbCorner.param());
        assertEquals(TotalBet.Direction.OVER, tbCorner.direction());

        OddItem cornerHcp = request.getOdds().stream().filter(o -> o.getName().contains("Liverpool (-1.5)")).findFirst().orElseThrow();
        assertTrue(cornerHcp.getBetType() instanceof HandicapBet);
        HandicapBet hbCorner = (HandicapBet) cornerHcp.getBetType();
        assertEquals(StatType.CORNERS, hbCorner.statType());
        assertEquals(-1.5, hbCorner.param());

        OddItem cornerWinner = request.getOdds().stream().filter(o -> "Empate".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(cornerWinner.getBetType() instanceof MatchResultBet);
        assertEquals(StatType.CORNERS, ((MatchResultBet) cornerWinner.getBetType()).statType());
    }

    @Test
    void testCardsMarket() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-cards")
                .sportName("Futebol")
                .homeTeam("Atletico Madrid")
                .awayTeam("Real Madrid")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Total de Cartões Amarelos - Mais/Menos")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 4.5").decimal(1.80).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 4.5").decimal(2.00).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(2, request.getOdds().size());

        OddItem cardOver = request.getOdds().stream().filter(o -> "Mais 4.5".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(cardOver.getBetType() instanceof TotalBet);
        TotalBet tbCard = (TotalBet) cardOver.getBetType();
        assertEquals(StatType.YELLOW_CARDS, tbCard.statType());
        assertEquals(4.5, tbCard.param());
        assertEquals(TotalBet.Direction.OVER, tbCard.direction());
    }

    @Test
    void testEsportsCS2() {
        BetesporteEventDto event = BetesporteEventDto.builder()
                .id("ev-cs2")
                .sportName("CS2")
                .homeTeam("NAVI")
                .awayTeam("FaZe")
                .markets(List.of(
                        BetesporteMarketDto.builder()
                                .name("Vencedor da Partida")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("NAVI").decimal(1.70).build(),
                                        BetesporteOutcomeDto.builder().name("FaZe").decimal(2.15).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Total de Mapas")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 2.5").decimal(1.95).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 2.5").decimal(1.85).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("Handicap de Mapas")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("NAVI (-1.5)").decimal(2.80).build(),
                                        BetesporteOutcomeDto.builder().name("FaZe (+1.5)").decimal(1.45).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Mapa - Vencedor")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("NAVI").decimal(1.80).build(),
                                        BetesporteOutcomeDto.builder().name("FaZe").decimal(2.00).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Mapa - Total de Rounds")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("Mais 20.5").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("Menos 20.5").decimal(1.95).build()
                                ))
                                .build(),
                        BetesporteMarketDto.builder()
                                .name("1º Mapa - Primeiro Blood")
                                .outcomes(List.of(
                                        BetesporteOutcomeDto.builder().name("NAVI").decimal(1.85).build(),
                                        BetesporteOutcomeDto.builder().name("FaZe").decimal(1.95).build()
                                ))
                                .build()
                ))
                .build();

        OddsUpdateRequest request = mapper.mapToOddsUpdateRequest(event);
        assertNotNull(request);
        assertEquals(SportType.CS2, request.getSportType());
        assertEquals(12, request.getOdds().size());

        OddItem naviMatch = request.getOdds().stream().filter(o -> "NAVI".equals(o.getName()) && "moneyline".equals(o.getGroupName())).findFirst().orElseThrow();
        assertTrue(naviMatch.getBetType() instanceof MatchResultBet);
        assertEquals(BetScope.FULL_MATCH, ((MatchResultBet) naviMatch.getBetType()).scope());

        OddItem mapOver = request.getOdds().stream().filter(o -> "Mais 2.5".equals(o.getName()) && "maps_total".equals(o.getGroupName())).findFirst().orElseThrow();
        assertTrue(mapOver.getBetType() instanceof TotalBet);
        TotalBet tbMaps = (TotalBet) mapOver.getBetType();
        assertEquals(StatType.MAPS, tbMaps.statType());
        assertEquals(2.5, tbMaps.param());

        OddItem map1Winner = request.getOdds().stream().filter(o -> "NAVI".equals(o.getName()) && o.getGroupName().contains("map_winner")).findFirst().orElseThrow();
        assertTrue(map1Winner.getBetType() instanceof MatchResultBet);
        assertEquals(BetScope.MAP_1, ((MatchResultBet) map1Winner.getBetType()).scope());

        OddItem roundOver = request.getOdds().stream().filter(o -> "Mais 20.5".equals(o.getName())).findFirst().orElseThrow();
        assertTrue(roundOver.getBetType() instanceof TotalBet);
        TotalBet tbRounds = (TotalBet) roundOver.getBetType();
        assertEquals(StatType.ROUNDS, tbRounds.statType());
        assertEquals(20.5, tbRounds.param());
        assertEquals(BetScope.MAP_1, tbRounds.scope());

        OddItem fbNavi = request.getOdds().stream().filter(o -> "NAVI".equals(o.getName()) && o.getGroupName().contains("first_blood")).findFirst().orElseThrow();
        assertTrue(fbNavi.getBetType() instanceof BinaryMarketBet);
        BinaryMarketBet bmbFb = (BinaryMarketBet) fbNavi.getBetType();
        assertEquals(BinaryMarketBet.MarketType.FIRST_BLOOD, bmbFb.marketType());
        assertEquals(BinaryMarketBet.Outcome.TEAM1, bmbFb.outcome());
        assertEquals(BetScope.MAP_1, bmbFb.scope());
    }

    @Test
    void testInvalidEventOrMissingParticipants() {
        assertNull(mapper.mapToOddsUpdateRequest(null));

        BetesporteEventDto noTeams = BetesporteEventDto.builder().id("ev-invalid").build();
        assertNull(mapper.mapToOddsUpdateRequest(noTeams));
    }
}

