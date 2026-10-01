package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@Order(30)
public class WplayTotalHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(String marketName, WplayMarketContext context) {
        if (marketName == null) return false;
        if (isStats(marketName)) return false;
        if (context != null && isEsports(context.getSportType())) return false;

        String m = marketName.toLowerCase(Locale.ROOT);
        return m.contains("total") || m.contains("más/menos") || m.contains("mas/menos")
                || m.contains("over/under") || m.contains("goles más/menos")
                || m.contains("línea de total");
    }

    @Override
    public void handle(WplayMarketDto market, WplayMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null) return;

        String marketName = market.getName();
        String mLower = marketName.toLowerCase(Locale.ROOT);
        BetScope scope = resolveScope(marketName, context != null ? context.getSportType() : null);

        BetSubject subject = BetSubject.MATCH;
        if (context != null) {
            if (context.getHomeTeam() != null && mLower.contains(context.getHomeTeam().toLowerCase(Locale.ROOT))) {
                subject = BetSubject.TEAM1;
            } else if (context.getAwayTeam() != null && mLower.contains(context.getAwayTeam().toLowerCase(Locale.ROOT))) {
                subject = BetSubject.TEAM2;
            }
        }
        if (mLower.contains("equipo 1") || mLower.contains("local") || mLower.contains("home") || mLower.contains("anfitrión")) {
            subject = BetSubject.TEAM1;
        } else if (mLower.contains("equipo 2") || mLower.contains("visitante") || mLower.contains("away")) {
            subject = BetSubject.TEAM2;
        }

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null || outcome.getEffectiveOdds() <= 1.0) continue;

            String outName = outcome.getName() != null ? outcome.getName().trim() : "";
            String outLower = outName.toLowerCase(Locale.ROOT);
            Double param = resolveParam(outcome, marketName);
            if (param == null) continue;

            if (outLower.contains("más") || outLower.contains("mas") || outLower.startsWith("over") || outLower.startsWith(">") || outLower.startsWith("+")) {
                addOddItem(items, outcome, marketName, new TotalBet(scope, subject, TotalBet.Direction.OVER, param, false, StatType.NONE));
            } else if (outLower.contains("menos") || outLower.startsWith("under") || outLower.startsWith("<") || outLower.startsWith("-")) {
                addOddItem(items, outcome, marketName, new TotalBet(scope, subject, TotalBet.Direction.UNDER, param, false, StatType.NONE));
            }
        }
    }
}
