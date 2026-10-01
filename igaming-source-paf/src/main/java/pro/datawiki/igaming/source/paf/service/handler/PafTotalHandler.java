package pro.datawiki.igaming.source.paf.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

@Component
@Order(20)
public class PafTotalHandler extends AbstractPafMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase(Locale.ROOT);
        if (m.contains("CORNER") || m.contains("HÖRN") || m.contains("KULMA")
                || m.contains("CARD") || m.contains("KORT") || m.contains("BOOKING") || m.contains("VARNING") || m.contains("VAROITUS")
                || m.contains("MAP") || m.contains("KART") || m.contains("ROUND") || m.contains("KIERROS")) {
            return false;
        }
        return m.contains("TOTAL") || m.contains("OVER/UNDER") || m.contains("ANTAL")
                || m.contains("ÖVER/UNDER") || m.contains("YLI/ALLE") || m.contains("YHTEENSÄ");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                       String marketName, SportType sportType, List<OddItem> items) {
        double odds = extractDecimalOdds(outcome);
        if (odds <= 1.0) return;

        BetScope scope = resolveScope(marketName);
        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase(Locale.ROOT) : "";
        Double line = outcome.getLine() != null ? outcome.getLine() : 0.0;
        if (Math.abs(line) > 100.0) {
            line = line / 1000.0;
        }

        BetSubject subject = BetSubject.MATCH;
        String mUpper = marketName.toUpperCase(Locale.ROOT);
        if (mUpper.contains("HOME") || mUpper.contains("TEAM 1") || mUpper.contains("HEMMALAG") || mUpper.contains("KOTI")
                || (event != null && event.getHomeName() != null && mUpper.contains(event.getHomeName().toUpperCase(Locale.ROOT)))) {
            subject = BetSubject.TEAM1;
        } else if (mUpper.contains("AWAY") || mUpper.contains("TEAM 2") || mUpper.contains("BORTALAG") || mUpper.contains("VIERAS")
                || (event != null && event.getAwayName() != null && mUpper.contains(event.getAwayName().toUpperCase(Locale.ROOT)))) {
            subject = BetSubject.TEAM2;
        }

        String runnerName = outcome.getLabel() != null ? outcome.getLabel() : "Outcome " + outcome.getId();

        if ("OT_OVER".equals(type) || label.startsWith("OVER") || label.startsWith("ÖVER") || label.startsWith("YLI") || label.startsWith(">")) {
            addTotal(items, outcome, marketName, runnerName, odds, scope, subject, TotalBet.Direction.OVER, line, false, null);
        } else if ("OT_UNDER".equals(type) || label.startsWith("UNDER") || label.startsWith("ALLE") || label.startsWith("<")) {
            addTotal(items, outcome, marketName, runnerName, odds, scope, subject, TotalBet.Direction.UNDER, line, false, null);
        }
    }
}
