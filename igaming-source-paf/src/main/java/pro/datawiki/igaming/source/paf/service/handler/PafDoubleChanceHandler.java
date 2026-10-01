package pro.datawiki.igaming.source.paf.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;

@Component
@Order(60)
public class PafDoubleChanceHandler extends AbstractPafMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String mUpper = marketName.toUpperCase();
        return mUpper.contains("DOUBLE CHANCE") || mUpper.contains("TUPLAMAHDOLLISUUS") || mUpper.contains("DUBBELCHANS");
    }

    @Override
    public void handle(MatchCache match, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        BetScope scope = resolveScope(marketName);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
        String groupName = "double_chance" + scopeSuffix;

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";
            String rawName = outcome.getLabel() != null ? outcome.getLabel() : type;

            if ("OT_ONE_OR_DRAW".equals(type) || label.contains("1X") || label.contains("1 OR X") || label.contains("1 TAI X")) {
                addOddItem(items, outcome, groupName, rawName, decimal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, StatType.MATCH));
            } else if ("OT_ONE_OR_TWO".equals(type) || label.contains("12") || label.contains("1 OR 2") || label.contains("1 TAI 2")) {
                addOddItem(items, outcome, groupName, rawName, decimal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, StatType.MATCH));
            } else if ("OT_DRAW_OR_TWO".equals(type) || label.contains("X2") || label.contains("X OR 2") || label.contains("X TAI 2")) {
                addOddItem(items, outcome, groupName, rawName, decimal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, StatType.MATCH));
            }
        }
    }
}
