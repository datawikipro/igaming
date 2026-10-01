package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;

@Component
@Order(10)
public class BetanoEsportsHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (!isEsports(sportType)) return false;
        if (marketName == null) return false;
        String mUpper = marketName.toUpperCase();
        return mUpper.contains("MAP") || mUpper.contains("ROUND") || mUpper.contains("KILL")
                || mUpper.contains("BLOOD") || mUpper.contains("WINNER") || mUpper.contains("TOTAL")
                || mUpper.contains("HANDICAP") || mUpper.contains("SPREAD") || mUpper.contains("1X2")
                || mUpper.contains("MONEYLINE") || mUpper.contains("OVER/UNDER");
    }

    @Override
    public void handle(MatchCache match, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        String mUpper = marketName.toUpperCase();
        BetScope scope = resolveScope(marketName);
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());

        // 1. Total Maps (Match Scope)
        if (mUpper.contains("MAP") && (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER"))
                && !mUpper.contains("ROUND") && scope == BetScope.FULL_MATCH) {
            handleTotals(betOffer, items, "maps_total", scope, BetSubject.MATCH, StatType.MAPS);
            return;
        }

        // 2. Map Handicap (Match Scope)
        if (mUpper.contains("MAP") && (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD"))
                && !mUpper.contains("ROUND") && scope == BetScope.FULL_MATCH) {
            handleHandicaps(betOffer, items, "maps_handicap", scope, StatType.MAPS);
            return;
        }

        // 3. Total Rounds (Map Scope or Match Scope)
        if (mUpper.contains("ROUND") && (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER"))) {
            handleTotals(betOffer, items, "rounds_total" + scopeSuffix, scope, BetSubject.MATCH, StatType.ROUNDS);
            return;
        }

        // 4. Round Handicap (Map Scope or Match Scope)
        if (mUpper.contains("ROUND") && (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD"))) {
            handleHandicaps(betOffer, items, "rounds_handicap" + scopeSuffix, scope, StatType.ROUNDS);
            return;
        }

        // 5. Total Kills
        if (mUpper.contains("KILL") && (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER"))) {
            BetSubject subject = determineTotalSubject(marketName, match != null ? match.getTeam1() : null, match != null ? match.getTeam2() : null);
            handleTotals(betOffer, items, "kills_total" + scopeSuffix, scope, subject, StatType.KILLS);
            return;
        }

        // 6. First Blood
        if (mUpper.contains("FIRST BLOOD") || mUpper.contains("1ST BLOOD") || mUpper.contains("PRIMEIRO BLOOD")) {
            handleFirstBlood(betOffer, items, "first_blood" + scopeSuffix, scope);
            return;
        }

        // 7. Map Winner / Match Winner / 1X2 / Moneyline
        if (mUpper.contains("WINNER") || mUpper.contains("RESULT") || mUpper.contains("MONEYLINE") ||
                mUpper.contains("1X2") || mUpper.contains("MAP") || mUpper.contains("TO WIN") || mUpper.contains("VENCEDOR")) {
            handleWinner(betOffer, items, (scope == BetScope.FULL_MATCH ? "moneyline" : "map_winner" + scopeSuffix), scope);
            return;
        }

        // 8. General Esports Totals (fallback)
        if (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER")) {
            handleTotals(betOffer, items, "total" + scopeSuffix, scope, BetSubject.MATCH, StatType.MATCH);
            return;
        }

        // 9. General Esports Handicaps (fallback)
        if (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD")) {
            handleHandicaps(betOffer, items, "handicap" + scopeSuffix, scope, StatType.MATCH);
        }
    }

    private void handleTotals(KambiBetOffer betOffer, List<OddItem> items, String groupName, BetScope scope, BetSubject subject, StatType statType) {
        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            Double line = outcome.getLine();
            if (line == null) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";

            if ("OT_OVER".equals(type) || label.contains("OVER") || label.contains("MAIS DE")) {
                addOddItem(items, outcome, groupName, "OVER (" + line + ")", decimal,
                        new TotalBet(scope, subject, TotalBet.Direction.OVER, line, false, statType));
            } else if ("OT_UNDER".equals(type) || label.contains("UNDER") || label.contains("MENOS DE")) {
                addOddItem(items, outcome, groupName, "UNDER (" + line + ")", decimal,
                        new TotalBet(scope, subject, TotalBet.Direction.UNDER, line, false, statType));
            }
        }
    }

    private void handleHandicaps(KambiBetOffer betOffer, List<OddItem> items, String groupName, BetScope scope, StatType statType) {
        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            Double line = outcome.getLine();
            if (line == null) line = 0.0;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";

            if ("OT_ONE".equals(type) || "1".equals(label)) {
                addOddItem(items, outcome, groupName, "HOME (" + line + ")", decimal,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM1, line, false, statType));
            } else if ("OT_TWO".equals(type) || "2".equals(label)) {
                addOddItem(items, outcome, groupName, "AWAY (" + line + ")", decimal,
                        new HandicapBet(scope, HandicapBet.Outcome.TEAM2, line, false, statType));
            }
        }
    }

    private void handleWinner(KambiBetOffer betOffer, List<OddItem> items, String groupName, BetScope scope) {
        boolean hasDraw = betOffer.getOutcomes().stream().anyMatch(o -> {
            String t = o.getType() != null ? o.getType().toUpperCase() : "";
            String l = o.getLabel() != null ? o.getLabel().toUpperCase() : "";
            return "OT_DRAW".equals(t) || l.contains("DRAW") || l.contains("EMPATE") || l.contains("TIE") || "X".equals(l);
        });

        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";

            if ("OT_ONE".equals(type) || "1".equals(label)) {
                addOddItem(items, outcome, groupName, "HOME", decimal,
                        hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, StatType.MATCH)
                                : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH));
            } else if ("OT_DRAW".equals(type) || label.contains("DRAW") || label.contains("EMPATE") || label.contains("TIE") || "X".equals(label)) {
                addOddItem(items, outcome, groupName, "DRAW", decimal,
                        new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH));
            } else if ("OT_TWO".equals(type) || "2".equals(label)) {
                addOddItem(items, outcome, groupName, "AWAY", decimal,
                        hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, StatType.MATCH)
                                : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH));
            }
        }
    }

    private void handleFirstBlood(KambiBetOffer betOffer, List<OddItem> items, String groupName, BetScope scope) {
        for (KambiOutcome outcome : betOffer.getOutcomes()) {
            Double decimal = extractDecimalOdds(outcome);
            if (decimal == null || decimal <= 1.0) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";

            if ("OT_ONE".equals(type) || "1".equals(label)) {
                addOddItem(items, outcome, groupName, "HOME", decimal,
                        new BinaryMarketBet(scope, BetSubject.TEAM1, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD));
            } else if ("OT_TWO".equals(type) || "2".equals(label)) {
                addOddItem(items, outcome, groupName, "AWAY", decimal,
                        new BinaryMarketBet(scope, BetSubject.TEAM2, BinaryMarketBet.MarketType.FIRST_BLOOD, BinaryMarketBet.Outcome.YES, StatType.FIRST_BLOOD));
            }
        }
    }
}
