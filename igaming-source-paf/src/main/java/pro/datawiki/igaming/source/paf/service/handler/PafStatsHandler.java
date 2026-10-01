package pro.datawiki.igaming.source.paf.service.handler;

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
@Order(20)
public class PafStatsHandler extends AbstractPafMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String mUpper = marketName.toUpperCase();
        return mUpper.contains("CORNER") || mUpper.contains("KULMAPOTKU") || mUpper.contains("HÖRN")
                || mUpper.contains("CARD") || mUpper.contains("KORTTI") || mUpper.contains("VARNING")
                || mUpper.contains("BOOKING") || mUpper.contains("YELLOW") || mUpper.contains("KELTAINEN");
    }

    @Override
    public void handle(MatchCache match, KambiBetOffer betOffer, SportType sportType, String marketName, List<OddItem> items) {
        if (betOffer.getOutcomes() == null || betOffer.getOutcomes().isEmpty()) return;

        String mUpper = marketName.toUpperCase();
        StatType statType = (mUpper.contains("CORNER") || mUpper.contains("KULMAPOTKU") || mUpper.contains("HÖRN"))
                ? StatType.CORNERS :
                ((mUpper.contains("YELLOW") || mUpper.contains("KELTAINEN")) ? StatType.YELLOW_CARDS : StatType.CARDS);

        BetScope scope = resolveScope(marketName);
        String statPrefix = statType == StatType.CORNERS ? "corners" : "cards";
        String scopeSuffix = scope == BetScope.FULL_MATCH ? "" : ("_" + scope.name().toLowerCase());

        boolean isTotal = mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("OVER / UNDER")
                || mUpper.contains("YLI/ALLE") || mUpper.contains("KOKONAIS") || mUpper.contains("ANTAL");
        boolean isHandicap = mUpper.contains("HANDICAP") || mUpper.contains("SPREAD") || mUpper.contains("TASOITUS");

        if (isTotal) {
            String team1 = match != null ? match.getTeam1() : null;
            String team2 = match != null ? match.getTeam2() : null;
            BetSubject subject = determineTotalSubject(marketName, team1, team2);
            String groupName = statPrefix + "_total" + (subject != BetSubject.MATCH ? "_" + subject.name().toLowerCase() : "") + scopeSuffix;

            for (KambiOutcome outcome : betOffer.getOutcomes()) {
                Double decimal = extractDecimalOdds(outcome);
                if (decimal == null || decimal <= 1.0) continue;

                Double line = normalizeLine(outcome.getLine());
                if (line == null) continue;

                String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
                String label = outcome.getLabel() != null ? outcome.getLabel() : "";

                if ("OT_OVER".equals(type) || label.toUpperCase().contains("OVER") || label.toUpperCase().contains("YLI")) {
                    addOddItem(items, outcome, groupName, "OVER (" + line + ")", decimal,
                            new TotalBet(scope, subject, TotalBet.Direction.OVER, line, false, statType));
                } else if ("OT_UNDER".equals(type) || label.toUpperCase().contains("UNDER") || label.toUpperCase().contains("ALLE")) {
                    addOddItem(items, outcome, groupName, "UNDER (" + line + ")", decimal,
                            new TotalBet(scope, subject, TotalBet.Direction.UNDER, line, false, statType));
                }
            }
        } else if (isHandicap) {
            String groupName = statPrefix + "_handicap" + scopeSuffix;

            for (KambiOutcome outcome : betOffer.getOutcomes()) {
                Double decimal = extractDecimalOdds(outcome);
                if (decimal == null || decimal <= 1.0) continue;

                Double line = normalizeLine(outcome.getLine());
                if (line == null) line = 0.0;

                String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
                String label = outcome.getLabel() != null ? outcome.getLabel() : "";

                if ("OT_ONE".equals(type) || "1".equals(label)) {
                    addOddItem(items, outcome, groupName, "HOME (" + line + ")", decimal,
                            new HandicapBet(scope, HandicapBet.Outcome.TEAM1, line, false, statType));
                } else if ("OT_TWO".equals(type) || "2".equals(label)) {
                    addOddItem(items, outcome, groupName, "AWAY (" + line + ")", decimal,
                            new HandicapBet(scope, HandicapBet.Outcome.TEAM2, line, false, statType));
                }
            }
        } else {
            // Most / Result / 1X2
            String groupName = statPrefix + "_1x2" + scopeSuffix;
            boolean hasDraw = betOffer.getOutcomes().stream().anyMatch(o -> {
                String t = o.getType() != null ? o.getType().toUpperCase() : "";
                String l = o.getLabel() != null ? o.getLabel().toUpperCase() : "";
                return "OT_DRAW".equals(t) || l.contains("DRAW") || l.contains("TASAPELI") || l.contains("LIKA") || l.contains("TIE") || "X".equals(l);
            });

            for (KambiOutcome outcome : betOffer.getOutcomes()) {
                Double decimal = extractDecimalOdds(outcome);
                if (decimal == null || decimal <= 1.0) continue;

                String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
                String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase() : "";

                if ("OT_ONE".equals(type) || label.contains("HOME") || label.contains("KOTI") || "1".equals(label)) {
                    addOddItem(items, outcome, groupName, "HOME", decimal,
                            hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, statType)
                                    : new MatchResultBet(scope, MatchResultBet.Outcome.WIN1_2WAY, statType));
                } else if ("OT_DRAW".equals(type) || label.contains("DRAW") || label.contains("TASAPELI") || label.contains("LIKA") || label.contains("TIE") || "X".equals(label)) {
                    addOddItem(items, outcome, groupName, "DRAW", decimal,
                            new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, statType));
                } else if ("OT_TWO".equals(type) || label.contains("AWAY") || label.contains("VIERAS") || "2".equals(label)) {
                    addOddItem(items, outcome, groupName, "AWAY", decimal,
                            hasDraw ? new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, statType)
                                    : new MatchResultBet(scope, MatchResultBet.Outcome.WIN2_2WAY, statType));
                }
            }
        }
    }
}
