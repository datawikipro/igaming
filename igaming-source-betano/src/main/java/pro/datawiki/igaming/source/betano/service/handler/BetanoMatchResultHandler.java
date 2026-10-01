package pro.datawiki.igaming.source.betano.service.handler;

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
@Order(90)
public class BetanoMatchResultHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        if (isEsports(sportType) || isStats(marketName)) return false;
        String mUpper = marketName.toUpperCase();
        return mUpper.contains("MATCH") || mUpper.contains("RESULT") || mUpper.contains("MONEYLINE")
                || mUpper.contains("1X2") || mUpper.contains("WINNER") || mUpper.contains("TO WIN")
                || mUpper.contains("FULL TIME") || mUpper.contains("WHO WILL WIN") || mUpper.contains("VENCEDOR")
                || mUpper.contains("FINAL DO JOGO");
    }

    @Override
    public void handle(MatchCache match, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        BetScope scope = resolveScope(marketName);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());
        String groupName = (scope == BetScope.FULL_MATCH ? "1x2" : "result" + scopeSuffix);

        boolean hasDraw = betOffer.getOutcomes().stream().anyMatch(o -> {
            String t = o.getType() != null ? o.getType().toUpperCase() : "";
            String l = o.getLabel() != null ? o.getLabel().toUpperCase() : "";
            return "OT_DRAW".equals(t) || l.contains("DRAW") || l.contains("EMPATE") || l.contains("TIE") || "X".equals(l);
        });

        String home = match != null && match.getTeam1() != null ? match.getTeam1().trim().toUpperCase() : null;
        String away = match != null && match.getTeam2() != null ? match.getTeam2().trim().toUpperCase() : null;

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().trim().toUpperCase() : "";
            String rawName = outcome.getLabel() != null ? outcome.getLabel() : "HOME";

            if ("OT_ONE".equals(type) || "1".equals(label) || (home != null && label.contains(home))) {
                addOddItem(items, outcome, groupName, rawName, decimal,
                        hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, StatType.MATCH)
                                : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
            } else if ("OT_DRAW".equals(type) || label.contains("DRAW") || label.contains("EMPATE") || label.contains("TIE") || "X".equals(label)) {
                addOddItem(items, outcome, groupName, rawName, decimal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH));
            } else if ("OT_TWO".equals(type) || "2".equals(label) || (away != null && label.contains(away))) {
                addOddItem(items, outcome, groupName, rawName, decimal,
                        hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, StatType.MATCH)
                                : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
            }
        }
    }
}
