package pro.datawiki.igaming.source.sport888.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiBetOffer;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiEvent;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiOutcome;

import java.util.List;

@Component
@Order(80)
public class Sport888HandicapHandler extends AbstractSport888MarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        if (isEsports(sportType) || isStats(marketName)) return false;
        String mUpper = marketName.toUpperCase();
        return mUpper.contains("HANDICAP") || mUpper.contains("SPREAD") || mUpper.contains("RUN LINE") || mUpper.contains("PUCK LINE");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        BetScope scope = resolveScope(marketName);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
        String groupName = "handicap" + scopeSuffix;

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            Double line = outcome.getLine();
            if (line == null) line = 0.0;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";

            if ("OT_ONE".equals(type) || "1".equals(label)) {
                addOddItem(items, outcome, groupName, "HOME (" + line + ")", decimal,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM1, line, false, StatType.MATCH));
            } else if ("OT_TWO".equals(type) || "2".equals(label)) {
                addOddItem(items, outcome, groupName, "AWAY (" + line + ")", decimal,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM2, line, false, StatType.MATCH));
            }
        }
    }
}
