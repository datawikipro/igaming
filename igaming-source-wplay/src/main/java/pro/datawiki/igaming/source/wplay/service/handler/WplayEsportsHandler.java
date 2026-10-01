package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@Order(5)
public class WplayEsportsHandler extends AbstractWplayMarketHandler {

    @Override
    public boolean supports(String marketName, WplayMarketContext context) {
        if (marketName == null) return false;
        if (context != null && isEsports(context.getSportType())) return true;

        String m = marketName.toLowerCase(Locale.ROOT);
        return m.contains("esport") || m.contains("mapa") || m.contains("map ")
                || m.contains("round") || m.contains("ronda") || m.contains("kill")
                || m.contains("asesinato") || m.contains("first blood") || m.contains("primera sangre");
    }

    @Override
    public void handle(WplayMarketDto market, WplayMarketContext context, List<OddItem> items) {
        if (market == null || market.getOutcomes() == null) return;

        String marketName = market.getName();
        String mLower = marketName.toLowerCase(Locale.ROOT);
        BetScope scope = resolveScope(marketName, context != null ? context.getSportType() : null);

        boolean isFirstBlood = mLower.contains("first blood") || mLower.contains("primera sangre");
        boolean isKills = mLower.contains("kill") || mLower.contains("asesinato");
        boolean isRounds = mLower.contains("round") || mLower.contains("ronda");
        boolean isMapsTotalOrHcap = mLower.contains("mapas") || mLower.contains("maps");
        boolean isTotal = mLower.contains("total") || mLower.contains("más/menos") || mLower.contains("mas/menos") || mLower.contains("over/under");
        boolean isHandicap = mLower.contains("handicap") || mLower.contains("hándicap") || mLower.contains("spread");

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            if (outcome == null || outcome.getEffectiveOdds() <= 1.0) continue;

            String outName = outcome.getName() != null ? outcome.getName().trim() : "";
            String outLower = outName.toLowerCase(Locale.ROOT);
            Double param = resolveParam(outcome, marketName);

            // First Blood
            if (isFirstBlood) {
                if (isTeam1(outLower, context)) {
                    addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, StatType.FIRST_BLOOD));
                } else if (isTeam2(outLower, context)) {
                    addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, StatType.FIRST_BLOOD));
                }
                continue;
            }

            // Totals
            if (isTotal && param != null) {
                StatType statType = StatType.MATCH;
                if (isRounds) statType = StatType.ROUNDS;
                else if (isMapsTotalOrHcap) statType = StatType.MAPS;
                else if (isKills) statType = StatType.KILLS;

                if (outLower.contains("más") || outLower.contains("mas") || outLower.startsWith("over") || outLower.startsWith(">")) {
                    addOddItem(items, outcome, marketName, new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, statType));
                    continue;
                } else if (outLower.contains("menos") || outLower.startsWith("under") || outLower.startsWith("<")) {
                    addOddItem(items, outcome, marketName, new TotalBet(scope, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, statType));
                    continue;
                }
            }

            // Handicaps
            if (isHandicap && param != null) {
                StatType statType = StatType.MATCH;
                if (isRounds) statType = StatType.ROUNDS;
                else if (isMapsTotalOrHcap) statType = StatType.MAPS;
                else if (isKills) statType = StatType.KILLS;

                if (isTeam1(outLower, context)) {
                    addOddItem(items, outcome, marketName, new HandicapBet(scope, HandicapBet.Outcome.TEAM1, param, false, statType));
                    continue;
                } else if (isTeam2(outLower, context)) {
                    addOddItem(items, outcome, marketName, new HandicapBet(scope, HandicapBet.Outcome.TEAM2, param, false, statType));
                    continue;
                }
            }

            // Map winner or Match winner
            StatType statType = scope == BetScope.FULL_MATCH ? StatType.NONE : StatType.MATCH;
            if (isTeam1(outLower, context)) {
                addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.WIN1, statType));
            } else if (isTeam2(outLower, context)) {
                addOddItem(items, outcome, marketName, new MatchResultBet(scope, MatchResultBet.Outcome.WIN2, statType));
            }
        }
    }

    private boolean isTeam1(String lower, WplayMarketContext context) {
        if (lower.equals("1") || lower.startsWith("1 ") || lower.contains("equipo 1") || lower.contains("team 1")) return true;
        if (context != null && context.getHomeTeam() != null) {
            String home = context.getHomeTeam().toLowerCase(Locale.ROOT);
            return lower.contains(home) || home.contains(lower);
        }
        return false;
    }

    private boolean isTeam2(String lower, WplayMarketContext context) {
        if (lower.equals("2") || lower.startsWith("2 ") || lower.contains("equipo 2") || lower.contains("team 2")) return true;
        if (context != null && context.getAwayTeam() != null) {
            String away = context.getAwayTeam().toLowerCase(Locale.ROOT);
            return lower.contains(away) || away.contains(lower);
        }
        return false;
    }
}
