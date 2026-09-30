package pro.datawiki.igaming.source.sport888.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiBetOffer;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiEvent;
import pro.datawiki.igaming.source.sport888.dto.kambi.KambiOutcome;

import java.util.List;

@Component
@Order(70)
public class Sport888TotalHandler extends AbstractSport888MarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        if (isEsports(sportType) || isStats(marketName)) return false;
        String mUpper = marketName.toUpperCase();
        return mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("OVER / UNDER");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        BetScope scope = resolveScope(marketName);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());

        String team1 = event != null ? event.getHomeName() : null;
        String team2 = event != null ? event.getAwayName() : null;
        BetSubject subject = determineTotalSubject(marketName, team1, team2);

        String subjectSuffix = subject != BetSubject.MATCH ? ("_" + subject.name().toLowerCase()) : "";
        String groupName = "total" + subjectSuffix + scopeSuffix;

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            Double line = outcome.getLine();
            if (line == null) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";

            if ("OT_OVER".equals(type) || label.contains("OVER")) {
                addOddItem(items, outcome, groupName, "OVER (" + line + ")", decimal,
                        new TotalBet(scope, subject, TotalBet.Direction.OVER, line, false, StatType.MATCH));
            } else if ("OT_UNDER".equals(type) || label.contains("UNDER")) {
                addOddItem(items, outcome, groupName, "UNDER (" + line + ")", decimal,
                        new TotalBet(scope, subject, TotalBet.Direction.UNDER, line, false, StatType.MATCH));
            }
        }
    }
}
