package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@Order(60)
public class WplayBttsHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(String marketName, WplayMarketContext context) {
        if (marketName == null) return false;
        if (isStats(marketName)) return false;
        if (context != null && isEsports(context.getSportType())) return false;

        String m = marketName.toLowerCase(Locale.ROOT);
        return m.contains("ambos equipos marcarán")
                || m.contains("ambos equipos anotan")
                || m.contains("ambos marcarán")
                || m.contains("marcan ambos equipos")
                || m.contains("both teams to score")
                || m.contains("both to score")
                || m.contains("btts");
    }

    @Override
    public void handle(WplayMarketDto market, WplayMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null) return;

        String marketName = market.getName();
        BetScope scope = resolveScope(marketName, context != null ? context.getSportType() : null);

        List<WplayOutcomeDto> outcomes = market.getOutcomes();
        for (int i = 0; i < outcomes.size(); i++) {
            WplayOutcomeDto outcome = outcomes.get(i);
            if (outcome == null || outcome.getEffectiveOdds() <= 1.0) continue;

            String outName = outcome.getName() != null ? outcome.getName().trim() : "";
            String lower = outName.toLowerCase(Locale.ROOT);

            BinaryMarketBet.Outcome outcomeEnum = null;
            if (lower.equals("sí") || lower.equals("si") || lower.equals("yes") || lower.contains("sí") || lower.contains("si") || lower.contains("yes")) {
                outcomeEnum = BinaryMarketBet.Outcome.YES;
            } else if (lower.equals("no") || lower.contains("no")) {
                outcomeEnum = BinaryMarketBet.Outcome.NO;
            } else if (outcomes.size() == 2) {
                outcomeEnum = (i == 0) ? BinaryMarketBet.Outcome.YES : BinaryMarketBet.Outcome.NO;
            }

            if (outcomeEnum != null) {
                BinaryMarketBet betType = new BinaryMarketBet(
                        scope,
                        BetSubject.MATCH,
                        BinaryMarketBet.MarketType.BTTS,
                        outcomeEnum,
                        StatType.MATCH
                );
                addOddItem(items, outcome, marketName, betType);
            }
        }
    }
}
