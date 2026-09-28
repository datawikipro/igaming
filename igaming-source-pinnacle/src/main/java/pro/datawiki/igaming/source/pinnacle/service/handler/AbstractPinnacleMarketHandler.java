package pro.datawiki.igaming.source.pinnacle.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;

import java.util.List;

public abstract class AbstractPinnacleMarketHandler implements PinnacleMarketHandler {

    public static double americanToDecimal(double american) {
        if (american > 0) {
            return Math.round(((american / 100.0) + 1.0) * 1000.0) / 1000.0;
        } else if (american < 0) {
            return Math.round(((100.0 / Math.abs(american)) + 1.0) * 1000.0) / 1000.0;
        }
        return 1.0;
    }

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
}
