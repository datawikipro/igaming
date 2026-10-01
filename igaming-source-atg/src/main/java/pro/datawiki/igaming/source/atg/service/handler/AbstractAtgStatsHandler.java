package pro.datawiki.igaming.source.atg.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractAtgStatsHandler extends AbstractAtgMarketHandler {

    private static final Pattern LINE_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");

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

        Double line = outcome.getLine();
        if (line != null && Math.abs(line) > 100.0) {
            line = line / 1000.0;
        }
        if (line == null || line == 0.0) {
            line = extractLine(outcome.getLabel());
        }

        String runnerName = outcome.getLabel() != null ? outcome.getLabel() : "Outcome " + outcome.getId();

        // 1. Double Chance on Stats (1X, 12, X2)
        if ("OT_ONE_CROSS".equals(type) || "1X".equals(label) || label.contains("ELLER OAVGJORT") || label.contains("OR DRAW")) {
            addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.DC_1X, statType);
            return;
        } else if ("OT_ONE_TWO".equals(type) || "12".equals(label)) {
            addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.DC_12, statType);
            return;
        } else if ("OT_CROSS_TWO".equals(type) || "X2".equals(label) || label.contains("OAVGJORT ELLER") || label.contains("DRAW OR")) {
            addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.DC_X2, statType);
            return;
        }

        // 2. Totals (Over / Under)
        boolean isTotalMarket = mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER")
                || mUpper.contains("ANTAL") || mUpper.contains("ÖVER/UNDER")
                || "OT_OVER".equals(type) || "OT_UNDER".equals(type)
                || label.startsWith("OVER") || label.startsWith("ÖVER") || label.startsWith("UNDER")
                || label.startsWith(">") || label.startsWith("<");

        if (isTotalMarket) {
            BetSubject subject = BetSubject.MATCH;
            if (mUpper.contains("HOME") || mUpper.contains("TEAM 1") || mUpper.contains("HEMMALAG")
                    || (event != null && event.getHomeName() != null && mUpper.contains(event.getHomeName().toUpperCase(Locale.ROOT)))) {
                subject = BetSubject.TEAM1;
            } else if (mUpper.contains("AWAY") || mUpper.contains("TEAM 2") || mUpper.contains("BORTALAG")
                    || (event != null && event.getAwayName() != null && mUpper.contains(event.getAwayName().toUpperCase(Locale.ROOT)))) {
                subject = BetSubject.TEAM2;
            }

            if ("OT_OVER".equals(type) || label.startsWith("OVER") || label.startsWith("ÖVER") || label.startsWith(">")) {
                addTotal(items, outcome, marketName, runnerName, odds, scope, subject, TotalBet.Direction.OVER, line, false, statType);
                return;
            } else if ("OT_UNDER".equals(type) || label.startsWith("UNDER") || label.startsWith("<")) {
                addTotal(items, outcome, marketName, runnerName, odds, scope, subject, TotalBet.Direction.UNDER, line, false, statType);
                return;
            }
        }

        // 3. Handicaps
        boolean isHandicapMarket = mUpper.contains("HANDICAP") || mUpper.contains("SPREAD") || mUpper.contains("HANDIKAPP")
                || mUpper.contains("ASIAN") || (line != 0.0 && !isTotalMarket);

        if (isHandicapMarket) {
            if ("OT_ONE".equals(type) || "1".equals(label) || "HOME".equalsIgnoreCase(outcome.getParticipant())
                    || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
                addHandicap(items, outcome, marketName, runnerName, odds, scope, HandicapBet.Outcome.TEAM1, line, false, statType);
                return;
            } else if ("OT_TWO".equals(type) || "2".equals(label) || "AWAY".equalsIgnoreCase(outcome.getParticipant())
                    || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
                addHandicap(items, outcome, marketName, runnerName, odds, scope, HandicapBet.Outcome.TEAM2, line, false, statType);
                return;
            }
        }

        // 4. 1X2 / Most / Match Result
        boolean is1X2Market = mUpper.contains("1X2") || mUpper.contains("RESULT") || mUpper.contains("MOST")
                || mUpper.contains("WINNER") || mUpper.contains("MEST") || mUpper.contains("3-VÄGS")
                || mUpper.contains("3-WAY") || !isTotalMarket;

        if (is1X2Market) {
            boolean hasDraw = betOffer != null && betOffer.getOutcomes() != null && betOffer.getOutcomes().stream()
                    .anyMatch(o -> "OT_DRAW".equalsIgnoreCase(o.getType())
                            || "OT_CROSS".equalsIgnoreCase(o.getType())
                            || (o.getLabel() != null && (o.getLabel().equalsIgnoreCase("Draw")
                                    || o.getLabel().equalsIgnoreCase("X")
                                    || o.getLabel().equalsIgnoreCase("Oavgjort")
                                    || o.getLabel().equalsIgnoreCase("Lika"))));

            if ("OT_ONE".equals(type) || "1".equals(label) || "HOME".equalsIgnoreCase(outcome.getParticipant())
                    || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
                MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                addMatchResult(items, outcome, marketName, runnerName, odds, scope, res, statType);
            } else if ("OT_DRAW".equals(type) || "OT_CROSS".equals(type) || "DRAW".equals(label) || "X".equals(label)
                    || "OAVGJORT".equals(label) || "LIKA".equals(label)) {
                addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.DRAW, statType);
            } else if ("OT_TWO".equals(type) || "2".equals(label) || "AWAY".equalsIgnoreCase(outcome.getParticipant())
                    || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
                MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                addMatchResult(items, outcome, marketName, runnerName, odds, scope, res, statType);
            }
        }
    }

    protected Double extractLine(String text) {
        if (text == null) return 0.0;
        Matcher m = LINE_PATTERN.matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (Exception ignored) {}
        }
        return 0.0;
    }
}
