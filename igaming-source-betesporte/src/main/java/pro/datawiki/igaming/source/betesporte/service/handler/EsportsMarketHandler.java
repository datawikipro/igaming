package pro.datawiki.igaming.source.betesporte.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;

/**
 * Specialized handler for Esports disciplines: CS2, Dota 2, League of Legends, Valorant.
 * Handles match/map winners, map/round totals, map/round handicaps, and First Blood.
 */
@Component
@Order(10)
public class EsportsMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (!isEsports(sportType)) return false;
        if (market == null || market.getEffectiveName() == null) return false;
        return true;
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        // 1. First Blood
        if (mName.contains("FIRST BLOOD") || mName.contains("1ST BLOOD") || mName.contains("PRIMEIRO BLOOD") || mName.contains("FIRST KILL")) {
            handleFirstBlood(market, event, scope, items);
            return;
        }

        // 2. Total Maps (Match Scope)
        if (mName.contains("MAP") && (mName.contains("TOTAL") || isTotal(mName)) && !mName.contains("ROUND") && !mName.contains("RODADA") && scope == BetScope.FULL_MATCH) {
            handleTotals(market, event, scope, BetSubject.MATCH, StatType.MAPS, "maps_total", items);
            return;
        }

        // 3. Map Handicap (Match Scope)
        if (mName.contains("MAP") && (mName.contains("HANDICAP") || mName.contains("SPREAD") || mName.contains("DESVANTAGEM")) && !mName.contains("ROUND") && !mName.contains("RODADA") && scope == BetScope.FULL_MATCH) {
            handleHandicaps(market, event, scope, StatType.MAPS, "maps_handicap", items);
            return;
        }

        // 4. Total Rounds
        if ((mName.contains("ROUND") || mName.contains("RODADA")) && (mName.contains("TOTAL") || isTotal(mName))) {
            String group = formatGroupName("rounds_total", scope);
            handleTotals(market, event, scope, BetSubject.MATCH, StatType.ROUNDS, group, items);
            return;
        }

        // 5. Round Handicap
        if ((mName.contains("ROUND") || mName.contains("RODADA")) && (mName.contains("HANDICAP") || mName.contains("SPREAD") || mName.contains("DESVANTAGEM"))) {
            String group = formatGroupName("rounds_handicap", scope);
            handleHandicaps(market, event, scope, StatType.ROUNDS, group, items);
            return;
        }

        // 6. Total Kills
        if ((mName.contains("KILL") || mName.contains("ABATE")) && (mName.contains("TOTAL") || isTotal(mName))) {
            BetSubject subject = resolveSubject(mName, event);
            String prefix = (subject == BetSubject.MATCH) ? "kills_total" : ("kills_total_" + subject.name().toLowerCase());
            String group = formatGroupName(prefix, scope);
            handleTotals(market, event, scope, subject, StatType.KILLS, group, items);
            return;
        }

        // 7. Map Winner / Match Winner / 1X2 / Moneyline
        if (mName.contains("WINNER") || mName.contains("VENCEDOR") || mName.contains("RESULT") || mName.contains("1X2") ||
            mName.contains("MONEYLINE") || mName.contains("MAPA") || mName.contains("MAP") || mName.contains("PARTIDA")) {
            handleWinner(market, event, scope, items);
            return;
        }

        // 8. General Totals Fallback
        if (mName.contains("TOTAL") || isTotal(mName)) {
            BetSubject subject = resolveSubject(mName, event);
            String group = formatGroupName((subject == BetSubject.MATCH) ? "total" : ("total_" + subject.name().toLowerCase()), scope);
            handleTotals(market, event, scope, subject, StatType.MATCH, group, items);
            return;
        }

        // 9. General Handicaps Fallback
        if (mName.contains("HANDICAP") || mName.contains("SPREAD") || mName.contains("DESVANTAGEM")) {
            String group = formatGroupName("handicap", scope);
            handleHandicaps(market, event, scope, StatType.MATCH, group, items);
        }
    }

    private boolean isTotal(String name) {
        return name.contains("OVER/UNDER") || name.contains("OVER / UNDER") ||
               name.contains("MAIS/MENOS") || name.contains("ACIMA/ABAIXO") ||
               name.contains("MAIS DE") || name.contains("MENOS DE");
    }

    private void handleFirstBlood(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        String group = formatGroupName("first_blood", scope);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome fbOutcome = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                fbOutcome = BinaryMarketBet.Outcome.TEAM1;
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                fbOutcome = BinaryMarketBet.Outcome.TEAM2;
            }

            if (fbOutcome != null) {
                BetType betType = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.FIRST_BLOOD, fbOutcome, StatType.FIRST_BLOOD);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleWinner(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, List<OddItem> items) {
        StatType statType = (scope == BetScope.FULL_MATCH) ? StatType.MATCH : StatType.MAPS;
        String group = (scope == BetScope.FULL_MATCH) ? "moneyline" : formatGroupName("map_winner", scope);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || "1".equals(upper) || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = map1X2Record("1", scope, statType);
            } else if (isTeam2(upper, event) || "2".equals(upper) || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = map1X2Record("2", scope, statType);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleTotals(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, BetSubject subject, StatType statType, String group, List<OddItem> items) {
        String mName = market.getEffectiveName();
        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), mName);
            if (points == null) continue;

            BetType betType = null;
            if (isOver(oName)) {
                betType = mapTotalRecord("OVER", scope, subject, statType, false, points);
            } else if (isUnder(oName)) {
                betType = mapTotalRecord("UNDER", scope, subject, statType, false, points);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private void handleHandicaps(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, StatType statType, String group, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hcp = extractNumber(oName, outcome.getHandicap(), mName);
            if (hcp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = mapHandicapRecord("1", scope, statType, isAsian, hcp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = mapHandicapRecord("2", scope, statType, isAsian, hcp);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
