package pro.datawiki.igaming.source.betesporte.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;

/**
 * Handler for period-specific markets (Halves, Quarters, Sets, Periods).
 * Maps 1X2, Double Chance, Totals, Handicaps, and Draw No Bet for individual periods.
 */
@Component
@Order(80)
public class PeriodMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CARD") || mName.contains("CART")) return false;
        if (mName.contains("HALF TIME / FULL TIME") || mName.contains("HT/FT") || mName.contains("INTERVALO / FINAL")) return false;
        if (mName.contains("BOTH TEAMS") || mName.contains("BTTS") || mName.contains("AMBAS MARCAM")) return false;
        if (mName.contains("CORRECT SCORE") || mName.contains("RESULTADO EXATO")) return false;

        BetScope scope = resolveScope(mName);
        if (scope == BetScope.FULL_MATCH) {
            return false;
        }

        return mName.contains("TEMPO") ||
               mName.contains("HALF") ||
               mName.contains("INTERVALO") ||
               mName.contains("QUARTO") ||
               mName.contains("QUARTER") ||
               mName.contains("SET") ||
               mName.contains("PERIODO") ||
               mName.contains("PERÍODO");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        boolean isTotal = mName.contains("TOTAL") || isOverUnder(mName);
        boolean isHandicap = mName.contains("HANDICAP") || mName.contains("SPREAD") || mName.contains("DESVANTAGEM");
        boolean isDnb = mName.contains("DRAW NO BET") || mName.contains("DNB") || mName.contains("EMPATE ANULA");
        boolean isDoubleChance = mName.contains("DOUBLE CHANCE") || mName.contains("DUPLA CHANCE") || mName.contains("CHANCE DUPLA");

        if (isTotal) {
            handleTotals(market, event, scope, items);
        } else if (isHandicap) {
            handleHandicaps(market, event, scope, items);
        } else if (isDnb) {
            handleDnb(market, event, scope, items);
        } else if (isDoubleChance) {
            handleDoubleChance(market, event, scope, items);
        } else {
            handle1X2(market, event, scope, items);
        }
    }

    private boolean isOverUnder(String name) {
        return name.contains("OVER/UNDER") || name.contains("OVER / UNDER") ||
               name.contains("MAIS/MENOS") || name.contains("ACIMA/ABAIXO") ||
               name.contains("MAIS DE") || name.contains("MENOS DE");
    }

    private void handle1X2(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("1x2", scope);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE")) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleDoubleChance(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("double_chance", scope);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.equals("1X") || upper.contains("1 OR X") || upper.contains("1 OU X")) {
                betType = map1X2DCRecord("1X", scope, StatType.MATCH);
            } else if (upper.equals("12") || upper.contains("1 OR 2") || upper.contains("1 OU 2")) {
                betType = map1X2DCRecord("12", scope, StatType.MATCH);
            } else if (upper.equals("X2") || upper.equals("2X") || upper.contains("X OR 2") || upper.contains("X OU 2")) {
                betType = map1X2DCRecord("X2", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleTotals(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetSubject subject = resolveSubject(mName, event);
        String baseGroup = (subject == BetSubject.MATCH) ? "total" : ("total_" + subject.name().toLowerCase());
        String group = formatGroupName(baseGroup, scope);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), mName);
            if (points == null) continue;

            BetType betType = null;
            if (isOver(oName)) {
                betType = mapTotalRecord("OVER", scope, subject, StatType.MATCH, false, points);
            } else if (isUnder(oName)) {
                betType = mapTotalRecord("UNDER", scope, subject, StatType.MATCH, false, points);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleHandicaps(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");
        String group = formatGroupName(isAsian ? "asian_handicap" : "handicap", scope);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hcp = extractNumber(oName, outcome.getHandicap(), mName);
            if (hcp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = mapHandicapRecord("1", scope, StatType.MATCH, isAsian, hcp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = mapHandicapRecord("2", scope, StatType.MATCH, isAsian, hcp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleDnb(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("draw_no_bet", scope);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = new HandicapBet(scope, HandicapBet.Outcome.TEAM1, 0.0, false, StatType.MATCH);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = new HandicapBet(scope, HandicapBet.Outcome.TEAM2, 0.0, false, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
