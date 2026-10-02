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
 * Handler for Cards / Yellow Cards (Cartões / Cartões Amarelos) statistical markets.
 */
@Component
@Order(30)
public class CardsMarketHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT")) return false;

        return mName.contains("CARD") ||
               mName.contains("CARTAO") ||
               mName.contains("CARTÃO") ||
               mName.contains("CARTOES") ||
               mName.contains("CARTÕES") ||
               mName.contains("BOOKING") ||
               mName.contains("YELLOW") ||
               mName.contains("AMAREL");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        StatType statType = (mName.contains("YELLOW") || mName.contains("AMAREL")) ? StatType.YELLOW_CARDS : StatType.CARDS;

        boolean isHandicap = mName.contains("HANDICAP") || mName.contains("SPREAD") || mName.contains("DESVANTAGEM");
        boolean isTotal = mName.contains("TOTAL") || mName.contains("OVER/UNDER") || mName.contains("OVER / UNDER") ||
                          mName.contains("MAIS/MENOS") || mName.contains("ACIMA/ABAIXO") || mName.contains("MAIS DE") || mName.contains("MENOS DE");
        boolean isResult = !isHandicap && !isTotal && (mName.contains("1X2") || mName.contains("RESULT") || mName.contains("VENCEDOR") ||
                          mName.contains("MAIS CART") || mName.contains("MOST CARD"));

        if (!isHandicap && !isTotal && !isResult) {
            boolean hasOverUnder = market.getOutcomes().stream().anyMatch(o -> isOver(o.getName()) || isUnder(o.getName()));
            if (hasOverUnder) {
                isTotal = true;
            } else {
                isResult = true;
            }
        }

        if (isTotal) {
            handleTotals(market, event, scope, statType, items);
        } else if (isHandicap) {
            handleHandicaps(market, event, scope, statType, items);
        } else {
            handleResult(market, event, scope, statType, items);
        }
    }

    private void handleTotals(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, StatType statType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetSubject subject = resolveSubject(mName, event);
        String prefix = (statType == StatType.YELLOW_CARDS) ? "yellow_cards_total" : "cards_total";
        String baseGroup = (subject == BetSubject.MATCH) ? prefix : (prefix + "_" + subject.name().toLowerCase());
        String group = formatGroupName(baseGroup, scope);

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

    private void handleHandicaps(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, StatType statType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");
        String prefix = (statType == StatType.YELLOW_CARDS) ? "yellow_cards" : "cards";
        String group = formatGroupName(isAsian ? prefix + "_asian_handicap" : prefix + "_handicap", scope);

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

    private void handleResult(BetesporteMarketDto market, BetesporteEventDto event, BetScope scope, StatType statType, List<OddItem> items) {
        String prefix = (statType == StatType.YELLOW_CARDS) ? "yellow_cards_1x2" : "cards_1x2";
        String group = formatGroupName(prefix, scope);

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
            } else if ("X".equals(upper) || upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE")) {
                betType = map1X2Record("X", scope, statType);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
