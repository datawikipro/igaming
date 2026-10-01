package pro.datawiki.igaming.source.wplay.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;

/**
 * Handler for Over/Under totals (Match Totals, Team Totals, Asian Totals):
 * - Total / Más de / Menos de / Total de goles / Goles totales (Spanish)
 * - Total / Over / Under / Goals O/U / Points O/U (English)
 */
@Component
@Order(100)
public class TotalMarketHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESQUINA") || mName.contains("CÓRNER") ||
            mName.contains("CARD") || mName.contains("TARJETA") || mName.contains("AMARILLA")) {
            return false;
        }

        return mName.contains("TOTAL") ||
               mName.contains("OVER/UNDER") ||
               mName.contains("OVER / UNDER") ||
               mName.contains("MÁS DE / MENOS DE") ||
               mName.contains("MAS DE / MENOS DE") ||
               mName.contains("MÁS / MENOS") ||
               mName.contains("MAS / MENOS") ||
               mName.contains("MÁS/MENOS") ||
               mName.contains("MAS/MENOS") ||
               mName.contains("GOALS O/U") ||
               mName.contains("POINTS O/U") ||
               mName.contains("TOTAL GOALS") ||
               mName.contains("TOTAL POINTS") ||
               mName.contains("TOTAL DE GOLES") ||
               mName.contains("TOTAL PUNTOS") ||
               mName.contains("PUNTOS TOTALES") ||
               mName.contains("GOLES MÁS/MENOS") ||
               mName.contains("GOLES MAS/MENOS");
    }

    @Override
    public void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        BetSubject subject = resolveSubject(mName, event);
        boolean isMarketAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), mName);
            if (points == null) continue;

            String upper = oName.toUpperCase();
            boolean isAsian = isMarketAsian || isQuarterAsian(points);

            BetType betType = null;
            if (isOver(upper)) {
                betType = mapTotalRecord("OVER", scope, subject, StatType.MATCH, isAsian, points);
            } else if (isUnder(upper)) {
                betType = mapTotalRecord("UNDER", scope, subject, StatType.MATCH, isAsian, points);
            }

            if (betType != null) {
                String baseGroup = (subject == BetSubject.MATCH) ?
                        (isAsian ? "asian_total" : "total") :
                        ("total_" + subject.name().toLowerCase());
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
