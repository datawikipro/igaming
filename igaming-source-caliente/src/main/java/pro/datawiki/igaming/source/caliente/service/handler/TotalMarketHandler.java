package pro.datawiki.igaming.source.caliente.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.caliente.dto.CalienteEventDto;
import pro.datawiki.igaming.source.caliente.dto.CalienteMarketDto;
import pro.datawiki.igaming.source.caliente.dto.CalienteOutcomeDto;

import java.util.List;

/**
 * Handler for Over/Under totals (Match Totals, Team Totals) in Caliente.
 */
@Component
public class TotalMarketHandler extends AbstractCalienteMarketHandler {

    @Override
    public boolean supports(CalienteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESQUINA") || mName.contains("CARD") || mName.contains("TARJETA")) return false;

        return mName.contains("TOTAL") ||
               mName.contains("OVER/UNDER") ||
               mName.contains("OVER / UNDER") ||
               mName.contains("ALTAS/BAJAS") ||
               mName.contains("ALTAS / BAJAS") ||
               mName.contains("MÁS/MENOS") ||
               mName.contains("MÁS / MENOS") ||
               mName.contains("MAS/MENOS") ||
               mName.contains("GOALS O/U") ||
               mName.contains("POINTS O/U") ||
               mName.contains("GOLES MÁS/MENOS") ||
               mName.contains("TOTAL DE GOLES") ||
               mName.contains("TOTAL DE PUNTOS");
    }

    @Override
    public void handle(CalienteMarketDto market, CalienteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        BetSubject subject = resolveSubject(mName, event);

        for (CalienteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double points = extractNumber(oName, outcome.getHandicap(), mName);
            if (points == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isOver(upper)) {
                betType = mapTotalRecord("OVER", scope, subject, StatType.MATCH, false, points);
            } else if (isUnder(upper)) {
                betType = mapTotalRecord("UNDER", scope, subject, StatType.MATCH, false, points);
            }

            if (betType != null) {
                String baseGroup = (subject == BetSubject.MATCH) ? "total" : ("total_" + subject.name().toLowerCase());
                String group = formatGroupName(baseGroup, scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private BetSubject resolveSubject(String marketName, CalienteEventDto event) {
        if (event.getHomeTeam() != null && marketName.contains(event.getHomeTeam().toUpperCase())) {
            return BetSubject.TEAM1;
        }
        if (event.getAwayTeam() != null && marketName.contains(event.getAwayTeam().toUpperCase())) {
            return BetSubject.TEAM2;
        }
        if (marketName.contains("HOME TOTAL") || marketName.contains("TEAM 1 TOTAL") ||
            marketName.contains("TOTAL LOCAL") || marketName.contains("LOCAL TOTAL") ||
            marketName.contains("TOTAL EQUIPO 1") || marketName.contains("TOTAL DEL EQUIPO 1")) {
            return BetSubject.TEAM1;
        }
        if (marketName.contains("AWAY TOTAL") || marketName.contains("TEAM 2 TOTAL") ||
            marketName.contains("TOTAL VISITANTE") || marketName.contains("VISITANTE TOTAL") ||
            marketName.contains("TOTAL EQUIPO 2") || marketName.contains("TOTAL DEL EQUIPO 2")) {
            return BetSubject.TEAM2;
        }
        return BetSubject.MATCH;
    }
}
