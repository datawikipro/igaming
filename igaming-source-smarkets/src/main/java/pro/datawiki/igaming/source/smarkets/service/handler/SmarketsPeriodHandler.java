package pro.datawiki.igaming.source.smarkets.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContract;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsMarket;

import java.util.List;

@Component
@Order(40)
public class SmarketsPeriodHandler extends AbstractSmarketsMarketHandler {

    @Override
    public boolean supports(SmarketsMarketContext context) {
        if (context.getMarket() == null) return false;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";

        if (mName.contains("corner") || mName.contains("card") || mName.contains("booking") ||
            mName.contains("map") || mName.contains("round") || mName.contains("kill") ||
            mName.contains("half time / full time") || mName.contains("half-time / full-time") ||
            mName.contains("halftime/fulltime") || mName.contains("ht/ft") || mName.contains("ht / ft") ||
            mName.contains("double result")) {
            return false;
        }

        if (isEsports(context.getSportType())) {
            return false;
        }

        BetScope scope = resolveScope(mName);
        return scope != BetScope.FULL_MATCH;
    }

    @Override
    public void handle(SmarketsMarketContext context, List<OddItem> items) {
        if (context.getContracts() == null) return;
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";
        String mtName = market.getMarketType() != null && market.getMarketType().getName() != null ?
                market.getMarketType().getName().toLowerCase() : "";
        String marketParam = market.getMarketType() != null ? market.getMarketType().getParam() : null;
        Double defaultParam = extractParam(marketParam, market.getName());

        BetScope scope = resolveScope(mName);
        String team1 = context.getTeam1();
        String team2 = context.getTeam2();

        // 1. Double chance in period/half
        if (mName.contains("double chance") || mtName.contains("double_chance")) {
            handlePeriodDoubleChance(context, scope, team1, team2, items);
            return;
        }

        // 2. Handicap in period/half
        if (mName.contains("handicap") || mtName.contains("handicap") || mName.contains("spread")) {
            handlePeriodHandicap(context, scope, team1, team2, defaultParam, items);
            return;
        }

        // 3. Totals in period/half
        if (mName.contains("over/under") || mName.contains("total") || mtName.contains("over_under")) {
            handlePeriodTotal(context, scope, team1, team2, defaultParam, items);
            return;
        }

        // 4. Winner / 1X2 in period/half
        handlePeriodWinner(context, scope, team1, team2, items);
    }

    private void handlePeriodDoubleChance(SmarketsMarketContext context, BetScope scope, String team1, String team2, List<OddItem> items) {
        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            MatchResultBet.Outcome outcome = resolveDoubleChanceOutcome(cName, team1, team2);

            if (outcome != null) {
                MatchResultBet betType = new MatchResultBet(scope, outcome, StatType.MATCH);
                String groupName = "double_chance_" + scope.name().toLowerCase();
                addOddItem(items, groupName, contract.getId(), cName, backOdds, betType);
            }
        }
    }

    private void handlePeriodHandicap(SmarketsMarketContext context, BetScope scope, String team1, String team2, Double defaultParam, List<OddItem> items) {
        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            Double param = extractParam(null, cName);
            if (param == null) param = defaultParam;
            if (param == null) continue;

            HandicapBet.Outcome outcome = resolveHandicapOutcome(cName, team1, team2);
            if (outcome != null) {
                boolean isAsian = (param % 0.5 != 0);
                HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, StatType.MATCH);
                String groupName = "handicap_" + scope.name().toLowerCase();
                addOddItem(items, groupName, contract.getId(), cName + " (" + param + ")", backOdds, betType);
            }
        }
    }

    private void handlePeriodTotal(SmarketsMarketContext context, BetScope scope, String team1, String team2, Double defaultParam, List<OddItem> items) {
        SmarketsMarket market = context.getMarket();
        String mName = market.getName() != null ? market.getName().toLowerCase() : "";

        BetSubject subject = BetSubject.MATCH;
        if (mName.contains("home") || isMatch(mName, team1) || mName.contains("team 1")) {
            subject = BetSubject.TEAM1;
        } else if (mName.contains("away") || isMatch(mName, team2) || mName.contains("team 2")) {
            subject = BetSubject.TEAM2;
        }

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            BetSubject contractSubject = subject;
            if (contractSubject == BetSubject.MATCH) {
                if (cName.toLowerCase().contains("home") || isMatch(cName, team1)) {
                    contractSubject = BetSubject.TEAM1;
                } else if (cName.toLowerCase().contains("away") || isMatch(cName, team2)) {
                    contractSubject = BetSubject.TEAM2;
                }
            }

            Double param = extractParam(null, cName);
            if (param == null) param = defaultParam;
            if (param == null) continue;

            TotalBet.Direction dir = resolveTotalDirection(cName);
            if (dir != null) {
                boolean isAsian = (param % 0.5 != 0);
                TotalBet betType = new TotalBet(scope, contractSubject, dir, param, isAsian, StatType.MATCH);
                String groupName = "total";
                if (contractSubject != BetSubject.MATCH) {
                    groupName += "_" + contractSubject.name().toLowerCase();
                }
                groupName += "_" + scope.name().toLowerCase();
                addOddItem(items, groupName, contract.getId(), cName + " (" + param + ")", backOdds, betType);
            }
        }
    }

    private void handlePeriodWinner(SmarketsMarketContext context, BetScope scope, String team1, String team2, List<OddItem> items) {
        boolean hasDraw = context.getContracts().stream().anyMatch(c -> isDraw(c.getName()));

        for (SmarketsContract contract : context.getContracts()) {
            Double backOdds = context.getBestBackOdds(contract);
            if (backOdds == null || backOdds <= 1.0) continue;

            String cName = contract.getName() != null ? contract.getName().trim() : "";
            MatchResultBet.Outcome outcome = null;

            if (isDraw(cName)) {
                outcome = MatchResultBet.Outcome.DRAW;
            } else if (isMatch(cName, team1) || cName.startsWith("1") || cName.toLowerCase().contains("home")) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            } else if (isMatch(cName, team2) || cName.startsWith("2") || cName.toLowerCase().contains("away")) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            }

            if (outcome != null) {
                MatchResultBet betType = new MatchResultBet(scope, outcome, StatType.MATCH);
                String groupName = "match_result_" + scope.name().toLowerCase();
                addOddItem(items, groupName, contract.getId(), cName, backOdds, betType);
            }
        }
    }

    private boolean isDraw(String name) {
        if (name == null) return false;
        String n = name.trim().toLowerCase();
        return n.equals("draw") || n.equals("tie") || n.equals("x");
    }

    private MatchResultBet.Outcome resolveDoubleChanceOutcome(String cName, String team1, String team2) {
        if (cName == null) return null;
        String upper = cName.toUpperCase().trim();

        if (upper.equals("1X") || upper.equals("1/X") || upper.contains("HOME OR DRAW") || upper.contains("DRAW OR HOME") ||
            (isMatch(upper, team1) && (upper.contains("DRAW") || upper.contains("X")))) {
            return MatchResultBet.Outcome.DC_1X;
        }
        if (upper.equals("12") || upper.equals("1/2") || upper.contains("HOME OR AWAY") || upper.contains("AWAY OR HOME") ||
            (isMatch(upper, team1) && isMatch(upper, team2))) {
            return MatchResultBet.Outcome.DC_12;
        }
        if (upper.equals("X2") || upper.equals("2X") || upper.equals("X/2") || upper.contains("DRAW OR AWAY") || upper.contains("AWAY OR DRAW") ||
            (isMatch(upper, team2) && (upper.contains("DRAW") || upper.contains("X")))) {
            return MatchResultBet.Outcome.DC_X2;
        }
        return null;
    }

    private HandicapBet.Outcome resolveHandicapOutcome(String cName, String team1, String team2) {
        if (cName == null) return null;
        if (isMatch(cName, team1) || cName.startsWith("1") || cName.toLowerCase().contains("home")) {
            return HandicapBet.Outcome.TEAM1;
        }
        if (isMatch(cName, team2) || cName.startsWith("2") || cName.toLowerCase().contains("away")) {
            return HandicapBet.Outcome.TEAM2;
        }
        if (isDraw(cName)) {
            return HandicapBet.Outcome.DRAW;
        }
        return null;
    }

    private TotalBet.Direction resolveTotalDirection(String cName) {
        if (cName == null) return null;
        String upper = cName.toUpperCase().trim();
        if (upper.startsWith("OVER") || upper.startsWith("O ") || upper.equals("OVER") || upper.equals("O")) {
            return TotalBet.Direction.OVER;
        }
        if (upper.startsWith("UNDER") || upper.startsWith("U ") || upper.equals("UNDER") || upper.equals("U")) {
            return TotalBet.Direction.UNDER;
        }
        return null;
    }
}
