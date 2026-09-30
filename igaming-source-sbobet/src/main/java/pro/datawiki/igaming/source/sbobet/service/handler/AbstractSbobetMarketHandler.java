package pro.datawiki.igaming.source.sbobet.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.List;

public abstract class AbstractSbobetMarketHandler implements SbobetMarketHandler {

    protected void addOddItem(List<OddItem> items, String groupName, String rawOutcomeName, double value, BetType betType) {
        if (value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        item.setFactorId(groupName + "_" + rawOutcomeName.replace(" ", "_"));
        item.setGroupName(groupName);
        item.setName(rawOutcomeName);
        item.setValue(value);
        item.setBetType(betType);
        items.add(item);
    }

    protected void addMatchResult(List<OddItem> items, String groupName, String name, double value,
                                  BetScope scope, MatchResultBet.Outcome outcome, StatType statType) {
        addOddItem(items, groupName, name, value, new MatchResultBet(scope, outcome, statType));
    }

    protected void addTotal(List<OddItem> items, String groupName, String name, double value,
                            BetScope scope, BetSubject subject, TotalBet.Direction direction,
                            double limit, boolean isAsian, StatType statType) {
        addOddItem(items, groupName, name, value, new TotalBet(scope, subject, direction, limit, isAsian, statType));
    }

    protected void addHandicap(List<OddItem> items, String groupName, String name, double value,
                               BetScope scope, HandicapBet.Outcome outcome, double hdp,
                               boolean isAsian, StatType statType) {
        addOddItem(items, groupName, name, value, new HandicapBet(scope, outcome, hdp, isAsian, statType));
    }

    protected void addBinary(List<OddItem> items, String groupName, String name, double value,
                             BetScope scope, BetSubject subject, BinaryMarketBet.MarketType marketType,
                             BinaryMarketBet.Outcome outcome, StatType statType) {
        addOddItem(items, groupName, name, value, new BinaryMarketBet(scope, subject, marketType, outcome, statType));
    }
}
