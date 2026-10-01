package pro.datawiki.igaming.source.atg.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

public abstract class AbstractAtgStatsHandler extends AbstractAtgMarketHandler {

    protected abstract StatType getStatType();
    protected abstract String getStatPrefix();

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                       String marketName, SportType sportType, List<OddItem> items) {
        double odds = extractDecimalOdds(outcome);
        if (odds <= 1.0) return;

        StatType statType = getStatType();
        BetScope scope = resolveScope(marketName);
        String mUpper = marketName != null ? marketName.toUpperCase(Locale.ROOT) : "";
        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase(Locale.ROOT) : "";
        Double line = outcome.getLine() != null ? outcome.getLine() : 0.0;

        String runnerName = outcome.getLabel() != null ? outcome.getLabel() : "Outcome " + outcome.getId();

        // 1. Totals
        if (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER")) {
            BetSubject subject = BetSubject.MATCH;
            if (mUpper.contains("HOME") || mUpper.contains("TEAM 1") || (event != null && event.getHomeName() != null && mUpper.contains(event.getHomeName().toUpperCase(Locale.ROOT)))) {
                subject = BetSubject.TEAM1;
            } else if (mUpper.contains("AWAY") || mUpper.contains("TEAM 2") || (event != null && event.getAwayName() != null && mUpper.contains(event.getAwayName().toUpperCase(Locale.ROOT)))) {
                subject = BetSubject.TEAM2;
            }

            if ("OT_OVER".equals(type) || label.startsWith("OVER") || label.startsWith(">")) {
                addTotal(items, outcome, marketName, runnerName, odds, scope, subject, TotalBet.Direction.OVER, line, false, statType);
            } else if ("OT_UNDER".equals(type) || label.startsWith("UNDER") || label.startsWith("<")) {
                addTotal(items, outcome, marketName, runnerName, odds, scope, subject, TotalBet.Direction.UNDER, line, false, statType);
            }
            return;
        }

        // 2. Handicaps
        if (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD")) {
            if ("OT_ONE".equals(type) || "1".equals(label) || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
                addHandicap(items, outcome, marketName, runnerName, odds, scope, HandicapBet.Outcome.TEAM1, line, false, statType);
            } else if ("OT_TWO".equals(type) || "2".equals(label) || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
                addHandicap(items, outcome, marketName, runnerName, odds, scope, HandicapBet.Outcome.TEAM2, line, false, statType);
            }
            return;
        }

        // 3. 1X2 / Most / Match Result
        if (mUpper.contains("1X2") || mUpper.contains("RESULT") || mUpper.contains("MOST") || mUpper.contains("WINNER")) {
            boolean hasDraw = betOffer != null && betOffer.getOutcomes() != null && betOffer.getOutcomes().stream()
                    .anyMatch(o -> "OT_DRAW".equalsIgnoreCase(o.getType())
                            || "OT_CROSS".equalsIgnoreCase(o.getType())
                            || (o.getLabel() != null && (o.getLabel().equalsIgnoreCase("Draw") || o.getLabel().equalsIgnoreCase("X"))));

            if ("OT_ONE".equals(type) || "1".equals(label) || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
                MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                addMatchResult(items, outcome, marketName, runnerName, odds, scope, res, statType);
            } else if ("OT_DRAW".equals(type) || "OT_CROSS".equals(type) || "DRAW".equals(label) || "X".equals(label)) {
                addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.DRAW, statType);
            } else if ("OT_TWO".equals(type) || "2".equals(label) || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
                MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                addMatchResult(items, outcome, marketName, runnerName, odds, scope, res, statType);
            }
        }
    }
}
