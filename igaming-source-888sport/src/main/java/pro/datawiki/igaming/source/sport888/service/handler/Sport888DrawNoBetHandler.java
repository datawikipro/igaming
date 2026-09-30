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
@Order(50)
public class Sport888DrawNoBetHandler extends AbstractSport888MarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String mUpper = marketName.toUpperCase();
        return (mUpper.contains("DRAW NO BET") || mUpper.contains("DNB")) && !isStats(marketName);
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        BetScope scope = resolveScope(marketName);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
        String groupName = "draw_no_bet" + scopeSuffix;

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";

            if ("OT_ONE".equals(type) || "1".equals(label)) {
                addOddItem(items, outcome, groupName, "HOME", decimal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
            } else if ("OT_TWO".equals(type) || "2".equals(label)) {
                addOddItem(items, outcome, groupName, "AWAY", decimal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
            }
        }
    }
}
