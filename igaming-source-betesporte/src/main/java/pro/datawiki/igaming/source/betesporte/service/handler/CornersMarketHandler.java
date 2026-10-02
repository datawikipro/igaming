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
 * Dedicated handler for Corner statistics markets (Escanteios / Cantos):
 * - Match Total Corners (Over / Under, Alternate Lines, Escanteios Mais/Menos)
 * - Team Total Corners (Home / Away, Escanteios Casa / Fora)
 * - Corners 1X2 (Winner / Most Corners / Vencedor dos Escanteios / Mais Escanteios)
 * - Corners Handicap (Corner Spread / Handicap Asiático / Handicap Europeu)
 * - Corners Double Chance (Dupla Chance de Escanteios)
 * - Corners Draw No Bet (Empate Anula Aposta - Escanteios)
 * - Corners Odd / Even (Par / Ímpar de Escanteios)
 * - First Corner / Last Corner (Primeiro / Último Escanteio)
 * - Scopes: Full Match, 1st Half (1º Tempo), 2nd Half (2º Tempo)
 */
@Component
@Order(30)
public class CornersMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CARD") || mName.contains("CART") || mName.contains("AMAREL")) return false;

        return mName.contains("CORNER") ||
               mName.contains("ESCANT") ||
               mName.contains("CANTO");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        String period = market.getPeriod() != null ? market.getPeriod().toUpperCase() : "";
        BetScope scope = resolveScope(mName + " " + period);

        if (isFirstCorner(mName)) {
            handleFirstLastCorner(market, event, scope, BinaryMarketBet.MarketType.FIRST_CORNER, "corners_first", items);
        } else if (isLastCorner(mName)) {
            handleFirstLastCorner(market, event, scope, BinaryMarketBet.MarketType.LAST_CORNER, "corners_last", items);
        } else if (isDoubleChance(mName)) {
            handleCornersDoubleChance(market, event, scope, items);
        } else if (isDrawNoBet(mName)) {
            handleCornersDrawNoBet(market, event, scope, items);
        } else if (isOddEven(mName)) {
            handleCornersOddEven(market, event, scope, items);
        } else if (isHandicap(mName)) {
            handleCornersHandicap(market, event, scope, items);
        } else if (isTotal(mName)) {
            handleCornersTotal(market, event, scope, items);
        } else if (is1X2(mName)) {
            handleCorners1X2(market, event, scope, items);
        } else {
            handleMixedOutcomes(market, event, scope, items);
        }
    }

    private boolean isFirstCorner(String mName) {
        return mName.contains("FIRST CORNER") || mName.contains("1ST CORNER") ||
               mName.contains("PRIMEIRO ESCANTEIO") || mName.contains("1º ESCANTEIO") || mName.contains("1° ESCANTEIO") ||
               mName.contains("PRIMEIRO CANTO") || mName.contains("1º CANTO") || mName.contains("1° CANTO");
    }

    private boolean isLastCorner(String mName) {
        return mName.contains("LAST CORNER") ||
               mName.contains("ÚLTIMO ESCANTEIO") || mName.contains("ULTIMO ESCANTEIO") ||
               mName.contains("ÚLTIMO CANTO") || mName.contains("ULTIMO CANTO");
    }

    private boolean isDoubleChance(String mName) {
        return mName.contains("DOUBLE CHANCE") || mName.contains("DUPLA CHANCE") ||
               mName.contains("CHANCE DUPLA") || mName.contains("1X2 OU") || mName.contains("1X2 OR");
    }

    private boolean isDrawNoBet(String mName) {
        return mName.contains("DRAW NO BET") || mName.contains("DNB") ||
               mName.contains("EMPATE ANULA") || mName.contains("2-WAY") || mName.contains("2 WAY") ||
               mName.contains("TIE NO BET");
    }

    private boolean isOddEven(String mName) {
        return mName.contains("ODD/EVEN") || mName.contains("ODD / EVEN") || mName.contains("ODD OR EVEN") ||
               mName.contains("PAR/ÍMPAR") || mName.contains("PAR/IMPAR") ||
               mName.contains("PAR OU ÍMPAR") || mName.contains("PAR OU IMPAR");
    }

    private boolean isHandicap(String mName) {
        return mName.contains("HANDICAP") || mName.contains("SPREAD") || mName.contains("DESVANTAGEM");
    }

    private boolean isTotal(String mName) {
        return mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("OVER / UNDER") ||
               mName.contains("MAIS/MENOS") || mName.contains("MAIS / MENOS") ||
               mName.contains("ACIMA/ABAIXO") || mName.contains("ACIMA / ABAIXO") ||
               mName.contains("O/U") || mName.contains("MAIS") || mName.contains("MENOS") ||
               mName.contains("ACIMA") || mName.contains("ABAIXO");
    }

    private boolean is1X2(String mName) {
        return mName.contains("1X2") || mName.contains("WINNER") || mName.contains("VENCEDOR") ||
               mName.contains("RESULTADO") || mName.contains("MATCH") || mName.contains("PARTIDA") ||
               mName.contains("MOST") || mName.contains("MAIS ESCANTEIOS") ||
               mName.contains("MAIOR NÚMERO") || mName.contains("MAIOR NUMERO") ||
               mName.contains("QUEM TERÁ MAIS") || mName.contains("QUEM TERA MAIS") ||
               mName.contains("3-WAY") || mName.contains("3 WAY");
    }

    private void handleCornersTotal(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetSubject marketSubject = resolveSubject(mName, event);
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetSubject outcomeSubject = marketSubject;
            if (outcomeSubject == BetSubject.MATCH) {
                if (isTeam1(upper, event)) {
                    outcomeSubject = BetSubject.TEAM1;
                } else if (isTeam2(upper, event)) {
                    outcomeSubject = BetSubject.TEAM2;
                }
            }

            boolean outcomeAsian = isAsian || (Math.abs(points * 4) % 2 != 0);
            BetType betType = null;
            if (isOver(upper)) {
                betType = mapTotalRecord("OVER", scope, outcomeSubject, StatType.CORNERS, outcomeAsian, points);
            } else if (isUnder(upper)) {
                betType = mapTotalRecord("UNDER", scope, outcomeSubject, StatType.CORNERS, outcomeAsian, points);
            }

            if (betType != null) {
                String baseGroup = (outcomeSubject == BetSubject.MATCH) ? "corners_total" : ("corners_total_" + outcomeSubject.name().toLowerCase());
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCornersHandicap(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hdp = extractNumber(oName, outcome.getHandicap(), market.getEffectiveName());
            if (hdp == null) continue;

            String upper = oName.toUpperCase();
            boolean outcomeAsian = isAsian || (Math.abs(hdp * 4) % 2 != 0);

            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = mapHandicapRecord("1", scope, StatType.CORNERS, outcomeAsian, hdp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = mapHandicapRecord("2", scope, StatType.CORNERS, outcomeAsian, hdp);
            }

            if (betType != null) {
                String baseGroup = outcomeAsian ? "corners_asian_handicap" : "corners_handicap";
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCorners1X2(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("corners_1x2", scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            BetType betType = null;

            if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE") || upper.equals("IGUALDADE")) {
                betType = map1X2Record("X", scope, StatType.CORNERS);
            } else if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = map1X2Record("1", scope, StatType.CORNERS);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = map1X2Record("2", scope, StatType.CORNERS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleFirstLastCorner(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope,
                                       BinaryMarketBet.MarketType marketType, String baseGroup, List<OddItem> items) {
        String group = formatGroupName(baseGroup, scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bOutcome = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                bOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                bOutcome = BinaryMarketBet.Outcome.TEAM2;
            } else if ("X".equals(upper) || upper.contains("EMPATE") || upper.contains("DRAW") || upper.contains("TIE")) {
                bOutcome = BinaryMarketBet.Outcome.DRAW;
            } else if (upper.contains("NONE") || upper.contains("NEITHER") || upper.contains("NO CORNER") ||
                       upper.contains("NENHUM") || upper.contains("SEM ESCANTEIO") || upper.contains("SEM CANTO")) {
                bOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, marketType, bOutcome, StatType.CORNERS);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }

    private void handleCornersDoubleChance(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("corners_double_chance", scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.contains("1X") || (isTeam1(upper, event) && (upper.contains("EMPATE") || upper.contains("DRAW")))) {
                betType = map1X2DCRecord("1X", scope, StatType.CORNERS);
            } else if (upper.contains("12") || (isTeam1(upper, event) && isTeam2(upper, event))) {
                betType = map1X2DCRecord("12", scope, StatType.CORNERS);
            } else if (upper.contains("X2") || upper.contains("2X") || (isTeam2(upper, event) && (upper.contains("EMPATE") || upper.contains("DRAW")))) {
                betType = map1X2DCRecord("X2", scope, StatType.CORNERS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCornersDrawNoBet(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("corners_draw_no_bet", scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA") || upper.startsWith("TEAM 1")) {
                betType = mapHandicapRecord("1", scope, StatType.CORNERS, false, 0.0);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA") || upper.startsWith("TEAM 2")) {
                betType = mapHandicapRecord("2", scope, StatType.CORNERS, false, 0.0);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleCornersOddEven(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("corners_odd_even", scope);
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.contains("ODD") || upper.contains("ÍMPAR") || upper.contains("IMPAR")) {
                betType = mapOddEvenRecord("ODD", scope, StatType.CORNERS);
            } else if (upper.contains("EVEN") || upper.contains("PAR")) {
                betType = mapOddEvenRecord("EVEN", scope, StatType.CORNERS);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleMixedOutcomes(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        boolean hasOverUnder = market.getOutcomes().stream().anyMatch(o -> o.getName() != null && (isOver(o.getName()) || isUnder(o.getName())));
        if (hasOverUnder) {
            handleCornersTotal(market, event, scope, items);
            return;
        }
        boolean has1X2 = market.getOutcomes().stream().anyMatch(o -> o.getName() != null && (isTeam1(o.getName(), event) || isTeam2(o.getName(), event)));
        if (has1X2) {
            handleCorners1X2(market, event, scope, items);
        }
    }

    @Override
    protected BetSubject resolveSubject(String marketName, BetesporteEventDto event) {
        if (marketName == null) return BetSubject.MATCH;
        String upper = marketName.toUpperCase();
        if (event != null && event.getHomeTeam() != null && upper.contains(event.getHomeTeam().toUpperCase())) {
            return BetSubject.TEAM1;
        }
        if (event != null && event.getAwayTeam() != null && upper.contains(event.getAwayTeam().toUpperCase())) {
            return BetSubject.TEAM2;
        }
        if (upper.contains("HOME CORNER") || upper.contains("TEAM 1 CORNER") ||
            upper.contains("HOME TEAM CORNER") || upper.contains("CORNERS - HOME") ||
            upper.contains("TOTAL CASA") || upper.contains("CASA TOTAL") ||
            upper.contains("ESCANTEIOS CASA") || upper.contains("CASA ESCANTEIOS") ||
            upper.contains("CANTOS CASA") || upper.contains("TIME 1 ESCANTEIOS") ||
            upper.contains("EQUIPE 1 ESCANTEIOS")) {
            return BetSubject.TEAM1;
        }
        if (upper.contains("AWAY CORNER") || upper.contains("TEAM 2 CORNER") ||
            upper.contains("AWAY TEAM CORNER") || upper.contains("CORNERS - AWAY") ||
            upper.contains("TOTAL FORA") || upper.contains("FORA TOTAL") ||
            upper.contains("ESCANTEIOS FORA") || upper.contains("FORA ESCANTEIOS") ||
            upper.contains("CANTOS FORA") || upper.contains("TIME 2 ESCANTEIOS") ||
            upper.contains("EQUIPE 2 ESCANTEIOS")) {
            return BetSubject.TEAM2;
        }
        return BetSubject.MATCH;
    }
}
