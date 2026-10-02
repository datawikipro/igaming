package pro.datawiki.igaming.source.betesporte.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Esports markets (CS2, Dota 2, LoL, Valorant, etc.):
 * - Match Winner (Moneyline / 1X2 / Vencedor da Partida / Resultado Final)
 * - Map Winner (Map 1, Map 2, ... / Vencedor do Mapa 1..5)
 * - Total Maps & Map Handicap
 * - Total Rounds & Round Handicap
 * - Total Kills & Kill Handicap
 * - First Blood (Primeiro Abate / Primeiro Sangue)
 */
@Component
@Order(20)
public class EsportsMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) {
            return true;
        }
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("MAP ") || mName.contains("MAPS") || mName.contains("MAPA") ||
               mName.contains("ROUND") || mName.contains("ROUNDS") || mName.contains("RODADA") ||
               mName.contains("GAME ") || mName.contains("GAMES") ||
               mName.contains("KILL") || mName.contains("ABATE") ||
               mName.contains("FIRST BLOOD") || mName.contains("1ST BLOOD") ||
               mName.contains("PRIMEIRO ABATE") || mName.contains("PRIMEIRO SANGUE") ||
               mName.contains("PRIMEIRA MORTE");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        String context = mName + " " + (market.getPeriod() != null ? market.getPeriod().toUpperCase() : "");
        BetScope scope = resolveScope(context);

        if (isFirstBlood(mName)) {
            handleFirstBlood(market, event, scope, items);
        } else if (isRoundHandicap(mName)) {
            handleRoundHandicap(market, event, scope, items);
        } else if (isTotalRounds(mName)) {
            handleTotalRounds(market, scope, items);
        } else if (isKillHandicap(mName)) {
            handleKillHandicap(market, event, scope, items);
        } else if (isTotalKills(mName)) {
            handleTotalKills(market, scope, items);
        } else if (isMapHandicap(mName, scope, sportType)) {
            handleMapHandicap(market, event, scope, items);
        } else if (isTotalMaps(mName, scope, sportType)) {
            handleTotalMaps(market, scope, items);
        } else if (scope != BetScope.FULL_MATCH && isWinnerMarket(mName)) {
            handleMapWinner(market, event, scope, items);
        } else if (isMatchWinner(mName)) {
            handleMatchWinner(market, event, scope, items);
        }
    }

    private boolean isFirstBlood(String mName) {
        return mName.contains("FIRST BLOOD") || mName.contains("FIRST KILL") || mName.contains("1ST BLOOD") ||
               mName.contains("PRIMEIRO ABATE") || mName.contains("PRIMEIRO SANGUE") || mName.contains("PRIMEIRA MORTE");
    }

    private boolean isRoundHandicap(String mName) {
        return mName.contains("ROUND HANDICAP") ||
               mName.contains("HANDICAP DE ROUND") ||
               mName.contains("HANDICAP DE RODADA") ||
               ((mName.contains("ROUND") || mName.contains("RODADA")) &&
                (mName.contains("HANDICAP") || mName.contains("DESVANTAGEM") || mName.contains("SPREAD")));
    }

    private boolean isTotalRounds(String mName) {
        return mName.contains("TOTAL ROUND") || mName.contains("ROUND TOTAL") ||
               mName.contains("TOTAL DE ROUND") || mName.contains("TOTAL DE RODADA") ||
               ((mName.contains("ROUND") || mName.contains("ROUNDS") || mName.contains("RODADA") || mName.contains("RODADAS")) &&
                (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("MAIS/MENOS") ||
                 mName.contains("ACIMA/ABAIXO") || mName.contains("O/U") || mName.contains("MAIS") || mName.contains("MENOS")));
    }

    private boolean isKillHandicap(String mName) {
        return mName.contains("KILL HANDICAP") ||
               mName.contains("HANDICAP DE KILL") ||
               mName.contains("HANDICAP DE ABATE") ||
               ((mName.contains("KILL") || mName.contains("ABATE")) &&
                (mName.contains("HANDICAP") || mName.contains("DESVANTAGEM") || mName.contains("SPREAD")));
    }

    private boolean isTotalKills(String mName) {
        return mName.contains("TOTAL KILL") || mName.contains("KILL TOTAL") ||
               mName.contains("TOTAL DE KILL") || mName.contains("TOTAL DE ABATE") ||
               ((mName.contains("KILL") || mName.contains("KILLS") || mName.contains("ABATE") || mName.contains("ABATES")) &&
                (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("MAIS/MENOS") ||
                 mName.contains("ACIMA/ABAIXO") || mName.contains("O/U")));
    }

    private boolean isMapHandicap(String mName, BetScope scope, SportType sportType) {
        if (mName.contains("ROUND") || mName.contains("RODADA") || mName.contains("KILL") || mName.contains("ABATE")) {
            return false;
        }
        return mName.contains("MAP HANDICAP") || mName.contains("GAME HANDICAP") ||
               mName.contains("HANDICAP DE MAPA") || mName.contains("DESVANTAGEM DE MAPA") ||
               ((mName.contains("MAP") || mName.contains("MAPA") || mName.contains("GAME")) &&
                (mName.contains("HANDICAP") || mName.contains("DESVANTAGEM") || mName.contains("SPREAD"))) ||
               (scope == BetScope.FULL_MATCH && isEsports(sportType) &&
                (mName.contains("HANDICAP") || mName.contains("DESVANTAGEM") || mName.contains("SPREAD")));
    }

    private boolean isTotalMaps(String mName, BetScope scope, SportType sportType) {
        if (mName.contains("ROUND") || mName.contains("RODADA") || mName.contains("KILL") || mName.contains("ABATE")) {
            return false;
        }
        return mName.contains("TOTAL MAP") || mName.contains("MAP TOTAL") ||
               mName.contains("TOTAL DE MAPA") || mName.contains("TOTAL DE MAPAS") ||
               mName.contains("TOTAL GAME") || mName.contains("GAME TOTAL") ||
               ((mName.contains("MAP") || mName.contains("MAPA") || mName.contains("GAME")) &&
                (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("MAIS/MENOS") ||
                 mName.contains("ACIMA/ABAIXO") || mName.contains("O/U"))) ||
               (scope == BetScope.FULL_MATCH && isEsports(sportType) &&
                (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("MAIS/MENOS") || mName.contains("ACIMA/ABAIXO")));
    }

    private boolean isWinnerMarket(String mName) {
        return mName.contains("WINNER") || mName.contains("VENCEDOR") ||
               mName.contains("RESULT") || mName.contains("RESULTADO") ||
               mName.contains("MONEYLINE") || mName.contains("1X2") || mName.contains("GANHADOR");
    }

    private boolean isMatchWinner(String mName) {
        return mName.contains("MATCH WINNER") || mName.contains("VENCEDOR DO ENCONTRO") ||
               mName.contains("VENCEDOR DA PARTIDA") || mName.contains("VENCEDOR") ||
               mName.contains("WINNER") || mName.contains("MONEYLINE") ||
               mName.contains("RESULTADO FINAL") || mName.contains("1X2") ||
               mName.contains("MATCH ODDS");
    }

    private void handleMatchWinner(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE")) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "esports_match_winner", oName, odds, betType);
            }
        }
    }

    private void handleMapWinner(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = "esports_" + scope.name().toLowerCase() + "_winner";
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE")) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleTotalMaps(BetesporteMarketDto market, BetScope scope, List<OddItem> items) {
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isOver(upper)) {
                betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.MAPS, false, points);
            } else if (isUnder(upper)) {
                betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.MAPS, false, points);
            }

            if (betType != null) {
                addOddItem(items, "esports_total_maps", oName, odds, betType);
            }
        }
    }

    private void handleMapHandicap(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = mapHandicapRecord("1", scope, StatType.MAPS, false, hdp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = mapHandicapRecord("2", scope, StatType.MAPS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, "esports_map_handicap", oName, odds, betType);
            }
        }
    }

    private void handleTotalRounds(BetesporteMarketDto market, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_total_rounds" : ("esports_" + scope.name().toLowerCase() + "_total_rounds");
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isOver(upper)) {
                betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.ROUNDS, false, points);
            } else if (isUnder(upper)) {
                betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.ROUNDS, false, points);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleRoundHandicap(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_round_handicap" : ("esports_" + scope.name().toLowerCase() + "_round_handicap");
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = mapHandicapRecord("1", scope, StatType.ROUNDS, false, hdp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = mapHandicapRecord("2", scope, StatType.ROUNDS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleTotalKills(BetesporteMarketDto market, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_total_kills" : ("esports_" + scope.name().toLowerCase() + "_total_kills");
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isOver(upper)) {
                betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.KILLS, false, points);
            } else if (isUnder(upper)) {
                betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.KILLS, false, points);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleKillHandicap(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_kill_handicap" : ("esports_" + scope.name().toLowerCase() + "_kill_handicap");
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = mapHandicapRecord("1", scope, StatType.KILLS, false, hdp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = mapHandicapRecord("2", scope, StatType.KILLS, false, hdp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleFirstBlood(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = (scope == BetScope.FULL_MATCH) ? "esports_first_blood" : ("esports_" + scope.name().toLowerCase() + "_first_blood");
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BinaryMarketBet.Outcome fbOutcome = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                fbOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                fbOutcome = BinaryMarketBet.Outcome.TEAM2;
            } else if ("YES".equals(upper) || upper.startsWith("YES") || "SIM".equals(upper) || upper.startsWith("SIM") || "Y".equals(upper) || "S".equals(upper)) {
                fbOutcome = BinaryMarketBet.Outcome.YES;
            } else if ("NO".equals(upper) || upper.startsWith("NO") || "NAO".equals(upper) || "NÃO".equals(upper) || upper.startsWith("NÃO") || upper.startsWith("NAO") || "N".equals(upper)) {
                fbOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (fbOutcome != null) {
                BetType betType = new BinaryMarketBet(scope, BetSubject.MATCH,
                        BinaryMarketBet.MarketType.FIRST_BLOOD, fbOutcome, StatType.MATCH);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
