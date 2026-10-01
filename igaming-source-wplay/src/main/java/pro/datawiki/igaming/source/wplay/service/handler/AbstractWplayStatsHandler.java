package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.Locale;

@Slf4j
public abstract class AbstractWplayStatsHandler extends AbstractWplayMarketHandler {

    protected abstract StatType getStatType();
    protected abstract String getStatPrefix();

    @Override
    public void handle(WplayMarketDto market, WplayMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
            return;
        }

        String marketName = market.getName();
        String mLower = marketName != null ? marketName.toLowerCase(Locale.ROOT) : "";
        StatType statType = getStatType();
        BetScope scope = resolveScope(marketName, context != null ? context.getSportType() : null);

        boolean isTotal = mLower.contains("total") || mLower.contains("más/menos") || mLower.contains("mas/menos")
                || mLower.contains("over/under") || mLower.contains("más de") || mLower.contains("menos de");
        boolean isHandicap = mLower.contains("handicap") || mLower.contains("hándicap") || mLower.contains("spread");
        boolean isDoubleChance = mLower.contains("doble oportunidad") || mLower.contains("doble chance") || mLower.contains("double chance");

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null || outcome.getEffectiveOdds() <= 1.0) continue;

            String outName = outcome.getName() != null ? outcome.getName().trim() : "";
            String outLower = outName.toLowerCase(Locale.ROOT);
            Double odds = outcome.getEffectiveOdds();
            Double param = resolveParam(outcome, marketName);

            // Double chance
            if (isDoubleChance || outLower.equals("1x") || outLower.equals("12") || outLower.equals("x2")) {
                if (outLower.contains("1x") || outLower.contains("1 o x") || outLower.contains("1/x")) {
                    addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.DC_1X, statType));
                } else if (outLower.contains("12") || outLower.contains("1 o 2") || outLower.contains("1/2")) {
                    addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.DC_12, statType));
                } else if (outLower.contains("x2") || outLower.contains("x o 2") || outLower.contains("x/2")) {
                    addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.DC_X2, statType));
                }
                continue;
            }

            // Totals
            if (isTotal || outLower.contains("más de") || outLower.contains("mas de") || outLower.contains("menos de")
                    || outLower.startsWith("over") || outLower.startsWith("under") || outLower.startsWith("+") || outLower.startsWith("-")) {
                BetSubject subject = BetSubject.MATCH;
                if (context != null) {
                    if (context.getHomeTeam() != null && mLower.contains(context.getHomeTeam().toLowerCase(Locale.ROOT))) {
                        subject = BetSubject.TEAM1;
                    } else if (context.getAwayTeam() != null && mLower.contains(context.getAwayTeam().toLowerCase(Locale.ROOT))) {
                        subject = BetSubject.TEAM2;
                    }
                }
                if (mLower.contains("equipo 1") || mLower.contains("local") || mLower.contains("home")) {
                    subject = BetSubject.TEAM1;
                } else if (mLower.contains("equipo 2") || mLower.contains("visitante") || mLower.contains("away")) {
                    subject = BetSubject.TEAM2;
                }

                if (param != null) {
                    if (outLower.contains("más") || outLower.contains("mas") || outLower.startsWith("over") || outLower.startsWith(">")) {
                        addOddItem(items, outcome, marketName, new TotalBet(scope, subject, TotalBet.Direction.OVER, param, false, statType));
                        continue;
                    } else if (outLower.contains("menos") || outLower.startsWith("under") || outLower.startsWith("<")) {
                        addOddItem(items, outcome, marketName, new TotalBet(scope, subject, TotalBet.Direction.UNDER, param, false, statType));
                        continue;
                    }
                }
            }

            // Handicaps
            if (isHandicap && param != null) {
                if (isTeam1(outLower, context)) {
                    addOddItem(items, outcome, marketName, new HandicapBet(scope, HandicapBet.Outcome.TEAM1, param, false, statType));
                    continue;
                } else if (isTeam2(outLower, context)) {
                    addOddItem(items, outcome, marketName, new HandicapBet(scope, HandicapBet.Outcome.TEAM2, param, false, statType));
                    continue;
                }
            }

            // 1X2 / Winner
            if (outLower.contains("empate") || outLower.equals("x") || outLower.contains("draw")) {
                addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, statType));
            } else if (isTeam1(outLower, context)) {
                addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, statType));
            } else if (isTeam2(outLower, context)) {
                addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, statType));
            }
        }
    }

    protected boolean isTeam1(String outLower, WplayMarketContext context) {
        if (outLower.equals("1") || outLower.startsWith("1 ") || outLower.contains("local") || outLower.contains("equipo 1")) {
            return true;
        }
        if (context != null && context.getHomeTeam() != null) {
            String home = context.getHomeTeam().toLowerCase(Locale.ROOT);
            return outLower.contains(home) || home.contains(outLower);
        }
        return false;
    }

    protected boolean isTeam2(String outLower, WplayMarketContext context) {
        if (outLower.equals("2") || outLower.startsWith("2 ") || outLower.contains("visitante") || outLower.contains("equipo 2")) {
            return true;
        }
        if (context != null && context.getAwayTeam() != null) {
            String away = context.getAwayTeam().toLowerCase(Locale.ROOT);
            return outLower.contains(away) || away.contains(outLower);
        }
        return false;
    }
}
