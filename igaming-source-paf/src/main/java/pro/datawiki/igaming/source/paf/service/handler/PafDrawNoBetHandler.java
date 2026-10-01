package pro.datawiki.igaming.source.paf.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;

@Component
@Order(50)
public class PafDrawNoBetHandler extends AbstractPafMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String mUpper = marketName.toUpperCase();
        return mUpper.contains("DRAW NO BET") || mUpper.contains("DNB")
                || mUpper.contains("TASAPELI EI VETOA") || mUpper.contains("INGET SPEL VID OAVGJORT");
    }

    @Override
    public void handle(MatchCache match, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        BetScope scope = resolveScope(marketName);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
        String groupName = "draw_no_bet" + scopeSuffix;

        String home = match != null && match.getTeam1() != null ? match.getTeam1().trim().toUpperCase() : null;
        String away = match != null && match.getTeam2() != null ? match.getTeam2().trim().toUpperCase() : null;

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().trim().toUpperCase() : "";
            String rawName = outcome.getLabel() != null ? outcome.getLabel() : type;

            if ("OT_ONE".equals(type) || "1".equals(label) || (home != null && label.contains(home))) {
                addOddItem(items, outcome, groupName, rawName, decimal,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM1, 0.0, false, StatType.MATCH));
            } else if ("OT_TWO".equals(type) || "2".equals(label) || (away != null && label.contains(away))) {
                addOddItem(items, outcome, groupName, rawName, decimal,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM2, 0.0, false, StatType.MATCH));
            }
        }
    }
}
