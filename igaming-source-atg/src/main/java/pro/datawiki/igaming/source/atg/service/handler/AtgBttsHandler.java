package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

@Component
@Order(50)
public class AtgBttsHandler extends AbstractAtgMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase(Locale.ROOT);
        return m.contains("BOTH TEAMS TO SCORE") || m.contains("BOTH TEAMS") || m.contains("BTTS") || m.contains("BÅDA LAGEN GÖR MÅL");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                       String marketName, SportType sportType, List<OddItem> items) {
        double odds = extractDecimalOdds(outcome);
        if (odds <= 1.0) return;

        BetScope scope = resolveScope(marketName);
        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase(Locale.ROOT) : "";

        String runnerName = outcome.getLabel() != null ? outcome.getLabel() : "Outcome " + outcome.getId();

        if ("OT_YES".equals(type) || "YES".equalsIgnoreCase(label) || "JA".equalsIgnoreCase(label)) {
            addBinary(items, outcome, marketName, runnerName, odds, scope, BetSubject.MATCH,
                    BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null);
        } else if ("OT_NO".equals(type) || "NO".equalsIgnoreCase(label) || "NEJ".equalsIgnoreCase(label)) {
            addBinary(items, outcome, marketName, runnerName, odds, scope, BetSubject.MATCH,
                    BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null);
        }
    }
}
