package pro.datawiki.igaming.source.atg.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

public abstract class AbstractAtgMarketHandler implements AtgMarketHandler {

    protected double extractDecimalOdds(KambiOutcome outcome) {
        if (outcome == null || outcome.getOdds() == null) {
            return 0.0;
        }
        return outcome.getOdds() / 1000.0;
    }

    protected BetScope resolveScope(String marketName) {
        if (marketName == null) {
            return BetScope.FULL_MATCH;
        }
        String m = marketName.toUpperCase(Locale.ROOT);
        if (m.contains("1ST HALF") || m.contains("FIRST HALF") || m.contains("HALF TIME") 
                || m.contains("HT1") || m.contains("1H") || m.contains("HALF 1")) {
            return BetScope.HALF_1;
        }
        if (m.contains("2ND HALF") || m.contains("SECOND HALF") 
                || m.contains("HT2") || m.contains("2H") || m.contains("HALF 2")) {
            return BetScope.HALF_2;
        }
        if (m.contains("1ST PERIOD") || m.contains("PERIOD 1") || m.contains("1P")) {
            return BetScope.PERIOD_1;
        }
        if (m.contains("2ND PERIOD") || m.contains("PERIOD 2") || m.contains("2P")) {
            return BetScope.PERIOD_2;
        }
        if (m.contains("3RD PERIOD") || m.contains("PERIOD 3") || m.contains("3P")) {
            return BetScope.PERIOD_3;
        }
        if (m.contains("1ST QUARTER") || m.contains("QUARTER 1") || m.contains("1Q")) {
            return BetScope.QUARTER_1;
        }
        if (m.contains("2ND QUARTER") || m.contains("QUARTER 2") || m.contains("2Q")) {
            return BetScope.QUARTER_2;
        }
        if (m.contains("3RD QUARTER") || m.contains("QUARTER 3") || m.contains("3Q")) {
            return BetScope.QUARTER_3;
        }
        if (m.contains("4TH QUARTER") || m.contains("QUARTER 4") || m.contains("4Q")) {
            return BetScope.QUARTER_4;
        }
        if (m.contains("1ST SET") || m.contains("SET 1")) {
            return BetScope.SET_1;
        }
        if (m.contains("2ND SET") || m.contains("SET 2")) {
            return BetScope.SET_2;
        }
        if (m.contains("3RD SET") || m.contains("SET 3")) {
            return BetScope.SET_3;
        }
        return BetScope.FULL_MATCH;
    }

    protected void addOddItem(List<OddItem> items, KambiOutcome outcome, String groupName, 
                              String name, double odds, BetType betType) {
        if (odds <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        item.setFactorId(outcome != null && outcome.getId() != null ? String.valueOf(outcome.getId()) : (groupName + "_" + name.replace(" ", "_")));
        item.setGroupName(groupName);
        item.setName(name);
        item.setValue(odds);
        item.setBetType(betType);
        items.add(item);
    }

    protected void addMatchResult(List<OddItem> items, KambiOutcome outcome, String groupName, String name,
                                  double odds, BetScope scope, MatchResultBet.Outcome resultOutcome, StatType statType) {
        addOddItem(items, outcome, groupName, name, odds, new MatchResultBet(scope, resultOutcome, statType));
    }

    protected void addTotal(List<OddItem> items, KambiOutcome outcome, String groupName, String name,
                            double odds, BetScope scope, BetSubject subject, TotalBet.Direction direction,
                            double limit, boolean isAsian, StatType statType) {
        addOddItem(items, outcome, groupName, name, odds, new TotalBet(scope, subject, direction, limit, isAsian, statType));
    }

    protected void addHandicap(List<OddItem> items, KambiOutcome outcome, String groupName, String name,
                              double odds, BetScope scope, HandicapBet.Outcome handicapOutcome, double hdp,
                              boolean isAsian, StatType statType) {
        addOddItem(items, outcome, groupName, name, odds, new HandicapBet(scope, handicapOutcome, hdp, isAsian, statType));
    }

    protected void addBinary(List<OddItem> items, KambiOutcome outcome, String groupName, String name,
                             double odds, BetScope scope, BetSubject subject, BinaryMarketBet.MarketType marketType,
                             BinaryMarketBet.Outcome binaryOutcome, StatType statType) {
        addOddItem(items, outcome, groupName, name, odds, new BinaryMarketBet(scope, subject, marketType, binaryOutcome, statType));
    }
}
