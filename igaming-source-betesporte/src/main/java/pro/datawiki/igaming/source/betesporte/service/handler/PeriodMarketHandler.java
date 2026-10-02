package pro.datawiki.igaming.source.betesporte.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;

/**
 * Dedicated handler for Halves, Periods, and Quarters (1st Half / 1º Tempo, 2nd Half / 2º Tempo, Quarters, etc.)
 * when scope is not FULL_MATCH.
 */
@Component
@Order(95)
public class PeriodMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CANTO") ||
            mName.contains("CARD") || mName.contains("CART") || mName.contains("AMAREL")) {
            return false;
        }
        if (mName.contains("BOTH TEAMS") || mName.contains("BTTS") ||
            (mName.contains("AMBAS") && (mName.contains("MARC") || mName.contains("SCORE"))) ||
            (mName.contains("AMBOS") && (mName.contains("MARC") || mName.contains("SCORE")))) {
            return false;
        }
        if (mName.contains("CORRECT SCORE") || mName.contains("EXACT SCORE") ||
            mName.contains("RESULTADO EXATO") || mName.contains("PLACAR EXATO") ||
            mName.contains("RESULTADO CORRETO") || mName.contains("PLACAR CORRETO")) {
            return false;
        }
        if (mName.contains("HALF TIME / FULL TIME") || mName.contains("HT/FT") || mName.contains("HT / FT") ||
            mName.contains("DOUBLE RESULT") || mName.contains("INTERVALO / FINAL") || mName.contains("INTERVALO/FINAL")) {
            return false;
        }
        if (mName.contains("DOUBLE CHANCE") || mName.contains("DUPLA CHANCE") || mName.contains("CHANCE DUPLA")) {
            return false;
        }
        if (mName.contains("DRAW NO BET") || mName.contains("DNB") || mName.contains("EMPATE ANULA")) {
            return false;
        }

        String period = market.getPeriod() != null ? market.getPeriod().toUpperCase() : "";
        BetScope scope = resolveScope(mName + " " + period);
        return scope != BetScope.FULL_MATCH;
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        String period = market.getPeriod() != null ? market.getPeriod().toUpperCase() : "";
        BetScope scope = resolveScope(mName + " " + period);
        String scopeSuffix = "_" + scope.name().toLowerCase();

        if (mName.contains("HANDICAP") || mName.contains("SPREAD") || mName.contains("DESVANTAGEM")) {
            handlePeriodHandicap(market, event, scope, scopeSuffix, items);
        } else if (mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("OVER / UNDER") ||
                   mName.contains("MAIS/MENOS") || mName.contains("MAIS / MENOS") ||
                   mName.contains("ACIMA/ABAIXO") || mName.contains("ACIMA / ABAIXO") ||
                   mName.contains("GOLS") || mName.contains("PONTOS")) {
            handlePeriodTotal(market, event, scope, scopeSuffix, items);
        } else if (mName.contains("RESULT") || mName.contains("WINNER") || mName.contains("1X2") ||
                   mName.contains("MONEYLINE") || mName.contains("VENCEDOR") ||
                   mName.contains("RESULTADO") || mName.contains("QUEM VENCE")) {
            handlePeriodResult(market, event, scope, scopeSuffix, items);
        } else {
            handleMixedPeriodOutcomes(market, event, scope, scopeSuffix, items);
        }
    }

    private void handlePeriodResult(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, String scopeSuffix, List<OddItem> items) {
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("1 ") ||
                upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = map1X2Record("1", scope, StatType.MATCH);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("2 ") ||
                       upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = map1X2Record("2", scope, StatType.MATCH);
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE")) {
                betType = map1X2Record("X", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "period_result" + scopeSuffix, oName, odds, betType);
            }
        }
    }

    private void handlePeriodTotal(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, String scopeSuffix, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetSubject marketSubject = resolveSubject(mName, event);

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

            BetType betType = null;
            if (isOver(upper)) {
                betType = mapTotalRecord("OVER", scope, outcomeSubject, StatType.MATCH, false, points);
            } else if (isUnder(upper)) {
                betType = mapTotalRecord("UNDER", scope, outcomeSubject, StatType.MATCH, false, points);
            }

            if (betType != null) {
                String group = (outcomeSubject == BetSubject.MATCH) ?
                        ("period_total" + scopeSuffix) :
                        ("period_total_" + outcomeSubject.name().toLowerCase() + scopeSuffix);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handlePeriodHandicap(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, String scopeSuffix, List<OddItem> items) {
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
            if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("1 ") ||
                upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = mapHandicapRecord("1", scope, StatType.MATCH, outcomeAsian, hdp);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("2 ") ||
                       upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = mapHandicapRecord("2", scope, StatType.MATCH, outcomeAsian, hdp);
            }

            if (betType != null) {
                addOddItem(items, "period_handicap" + scopeSuffix, oName, odds, betType);
            }
        }
    }

    private void handleMixedPeriodOutcomes(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, String scopeSuffix, List<OddItem> items) {
        boolean hasOverUnder = market.getOutcomes().stream().anyMatch(o -> o.getName() != null && (isOver(o.getName()) || isUnder(o.getName())));
        if (hasOverUnder) {
            handlePeriodTotal(market, event, scope, scopeSuffix, items);
            return;
        }

        boolean hasHandicap = market.getOutcomes().stream().anyMatch(o -> o.getHandicap() != null || (o.getName() != null && o.getName().matches(".*[+-]\\d+.*")));
        if (hasHandicap) {
            handlePeriodHandicap(market, event, scope, scopeSuffix, items);
            return;
        }

        handlePeriodResult(market, event, scope, scopeSuffix, items);
    }
}
