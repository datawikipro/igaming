package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

@Component
@Order(40)
public class AtgDoubleChanceHandler extends AbstractAtgMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase(Locale.ROOT);
        return m.contains("DOUBLE CHANCE") || m.contains("DOUBLE_CHANCE") || m.contains(" 1X2 DC") || m.endsWith(" DC") || m.contains("DUBBELCHANS");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                       String marketName, SportType sportType, List<OddItem> items) {
        double odds = extractDecimalOdds(outcome);
        if (odds <= 1.0) return;

        BetScope scope = resolveScope(marketName);
        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase(Locale.ROOT) : "";
        String englishLabel = outcome.getEnglishLabel() != null ? outcome.getEnglishLabel().toUpperCase(Locale.ROOT) : "";

        String runnerName = outcome.getLabel() != null ? outcome.getLabel() : "Outcome " + outcome.getId();

        if ("OT_ONE_CROSS".equals(type) || label.contains("1X") || englishLabel.contains("1X")
                || label.contains("HOME OR DRAW") || englishLabel.contains("HOME OR DRAW")) {
            addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.DC_1X, null);
        } else if ("OT_ONE_TWO".equals(type) || label.contains("12") || englishLabel.contains("12")
                || label.contains("HOME OR AWAY") || englishLabel.contains("HOME OR AWAY")) {
            addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.DC_12, null);
        } else if ("OT_CROSS_TWO".equals(type) || label.contains("X2") || englishLabel.contains("X2")
                || label.contains("DRAW OR AWAY") || englishLabel.contains("DRAW OR AWAY")) {
            addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.DC_X2, null);
        }
    }
}
