package pro.datawiki.igaming.source.sport888.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiBetOffer;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiEvent;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiOutcome;

import java.util.List;

@Component
@Order(30)
public class Sport888DoubleChanceHandler extends AbstractSport888MarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String mUpper = marketName.toUpperCase();
        return (mUpper.contains("DOUBLE CHANCE") || mUpper.contains("DOUBLE_CHANCE")) && !isStats(marketName);
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        BetScope scope = resolveScope(marketName);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
        String groupName = "double_chance" + scopeSuffix;

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";

            if ("OT_ONE_X".equals(type) || label.contains("1X") || label.contains("HOME OR DRAW") || label.contains("HOME/DRAW")) {
                addOddItem(items, outcome, groupName, "1X", decimal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH));
            } else if ("OT_ONE_TWO".equals(type) || label.contains("12") || label.contains("HOME OR AWAY") || label.contains("HOME/AWAY")) {
                addOddItem(items, outcome, groupName, "12", decimal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, StatType.MATCH));
            } else if ("OT_X_TWO".equals(type) || label.contains("X2") || label.contains("DRAW OR AWAY") || label.contains("DRAW/AWAY")) {
                addOddItem(items, outcome, groupName, "X2", decimal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH));
            }
        }
    }
}
