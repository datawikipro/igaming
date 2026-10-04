package pro.datawiki.igaming.source.wplay.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;

/**
 * Handler for Cards / Yellow Cards markets (StatType.YELLOW_CARDS):
 * - Yellow Cards / Tarjetas amarillas / Tarjetas (Spanish/English)
 * Supports: 1X2 (team more cards), Totals, Handicap, Red Card (Yes/No).
 */
@Component
@Order(40)
public class CardsMarketHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("YELLOW CARD") ||
               mName.contains("TARJETA AMARILLA") ||
               mName.contains("TARJETAS AMARILLAS") ||
               mName.contains("CARD") && mName.contains("TARJETA") ||
               mName.equals("CARDS") ||
               mName.contains("TARJETAS") ||
               mName.contains("RED CARD") ||
               mName.contains("TARJETA ROJA") ||
               (mName.contains("CARD") && !mName.contains("CORNER"));
    }

    @Override
    public void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);

        boolean isRedCard = mName.contains("RED CARD") || mName.contains("TARJETA ROJA") || mName.contains("EXPULSIÓN") || mName.contains("EXPULSION");
        boolean isTotal = mName.contains("TOTAL") || mName.contains("OVER") || mName.contains("UNDER") ||
                          mName.contains("MÁS") || mName.contains("MAS") || mName.contains("MENOS") || mName.contains("O/U");
        boolean isHandicap = mName.contains("HANDICAP") || mName.contains("HÁNDICAP") || mName.contains("SPREAD");
        boolean is1x2 = mName.contains("1X2") || mName.contains("RESULTADO") || mName.contains("MÁS TARJETAS") || mName.contains("MAS TARJETAS");

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            if (isRedCard) {
                // Red Card: Yes/No (BinaryMarketBet)
                BetType betType = null;
                if (upper.contains("YES") || upper.contains("SÍ") || upper.contains("SI")) {
                    betType = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.RED_CARD, BinaryMarketBet.Outcome.YES, StatType.YELLOW_CARDS);
                } else if (upper.contains("NO")) {
                    betType = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.RED_CARD, BinaryMarketBet.Outcome.NO, StatType.YELLOW_CARDS);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("red_card", scope), oName, odds, betType);
                }
            } else if (isTotal) {
                Double points = extractNumber(oName, outcome.getHandicap(), mName);
                if (points == null) continue;
                BetType betType = null;
                if (isOver(upper)) {
                    betType = mapTotalRecord("OVER", scope, BetSubject.MATCH, StatType.YELLOW_CARDS, false, points);
                } else if (isUnder(upper)) {
                    betType = mapTotalRecord("UNDER", scope, BetSubject.MATCH, StatType.YELLOW_CARDS, false, points);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("cards_total", scope), oName, odds, betType);
                }
            } else if (isHandicap) {
                Double hcp = extractNumber(oName, outcome.getHandicap(), mName);
                if (hcp == null) continue;
                BetType betType = null;
                if (isTeam1(upper, event)) {
                    betType = mapHandicapRecord("1", scope, StatType.YELLOW_CARDS, false, hcp);
                } else if (isTeam2(upper, event)) {
                    betType = mapHandicapRecord("2", scope, StatType.YELLOW_CARDS, false, hcp);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("cards_handicap", scope), oName, odds, betType);
                }
            } else if (is1x2) {
                BetType betType = null;
                if (isTeam1(upper, event) || "1".equals(upper)) {
                    betType = map1X2Record("1", scope, StatType.YELLOW_CARDS);
                } else if (isTeam2(upper, event) || "2".equals(upper)) {
                    betType = map1X2Record("2", scope, StatType.YELLOW_CARDS);
                } else if (isDraw(upper)) {
                    betType = map1X2Record("X", scope, StatType.YELLOW_CARDS);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("cards_1x2", scope), oName, odds, betType);
                }
            } else {
                // Generic fallback: try 1X2 for team card winner markets
                BetType betType = null;
                if (isTeam1(upper, event) || "1".equals(upper)) {
                    betType = map1X2Record("1", scope, StatType.YELLOW_CARDS);
                } else if (isTeam2(upper, event) || "2".equals(upper)) {
                    betType = map1X2Record("2", scope, StatType.YELLOW_CARDS);
                } else if (isDraw(upper)) {
                    betType = map1X2Record("X", scope, StatType.YELLOW_CARDS);
                }
                if (betType != null) {
                    addOddItem(items, formatGroupName("cards_1x2", scope), oName, odds, betType);
                }
            }
        }
    }
}
