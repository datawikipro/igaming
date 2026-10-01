package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

@Component
@Order(60)
public class AtgDrawNoBetHandler extends AbstractAtgMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase(Locale.ROOT);
        return m.contains("DRAW NO BET") || m.contains("DNB") || m.contains("OAVGJORT INGET SPEL");
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

        if ("OT_ONE".equals(type) || "1".equals(label) || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
            addHandicap(items, outcome, marketName, runnerName, odds, scope, HandicapBet.Outcome.TEAM1, 0.0, false, null);
        } else if ("OT_TWO".equals(type) || "2".equals(label) || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
            addHandicap(items, outcome, marketName, runnerName, odds, scope, HandicapBet.Outcome.TEAM2, 0.0, false, null);
        }
    }
}
