package pro.datawiki.igaming.source.paf.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;

@Component
@Order(80)
public class PafTotalHandler extends AbstractPafMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        if (isEsports(sportType) || isStats(marketName)) return false;
        String mUpper = marketName.toUpperCase();
        return (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("OVER / UNDER")
                || mUpper.contains("YLI/ALLE") || mUpper.contains("KOKONAIS") || mUpper.contains("MAALIT")
                || mUpper.contains("GOALS") || mUpper.contains("PISTEET") || mUpper.contains("POINTS") || mUpper.contains("ANTAL"))
                && !mUpper.contains("CORNER") && !mUpper.contains("KULMAPOTKU") && !mUpper.contains("CARD")
                && !mUpper.contains("KORTTI") && !mUpper.contains("ROUND") && !mUpper.contains("KIERROS");
    }

    @Override
    public void handle(MatchCache match, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        BetScope scope = resolveScope(marketName);
        String team1 = match != null ? match.getTeam1() : null;
        String team2 = match != null ? match.getTeam2() : null;
        BetSubject subject = determineTotalSubject(marketName, team1, team2);

        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
        String subjectSuffix = subject != BetSubject.MATCH ? ("_" + subject.name().toLowerCase()) : "";
        String groupName = "total" + subjectSuffix + scopeSuffix;

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            Double line = normalizeLine(outcome.getLine());
            if (line == null) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";
            String rawName = outcome.getLabel() != null ? outcome.getLabel() : (type + " (" + line + ")");

            if ("OT_OVER".equals(type) || label.contains("OVER") || label.contains("YLI") || label.contains("ÖVER")) {
                addOddItem(items, outcome, groupName, rawName, decimal,
                        new TotalBet(scope, subject, TotalBet.Direction.OVER, line, false, StatType.MATCH));
            } else if ("OT_UNDER".equals(type) || label.contains("UNDER") || label.contains("ALLE")) {
                addOddItem(items, outcome, groupName, rawName, decimal,
                        new TotalBet(scope, subject, TotalBet.Direction.UNDER, line, false, StatType.MATCH));
            }
        }
    }
}
