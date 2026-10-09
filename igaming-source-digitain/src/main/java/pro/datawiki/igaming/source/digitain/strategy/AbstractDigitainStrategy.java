package pro.datawiki.igaming.source.digitain.strategy;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractDigitainStrategy implements DigitainMarketStrategy {

    private static final Pattern TOTAL_PARAM_PATTERN = Pattern.compile("[-+]?\\d*\\.?\\d+");

    protected OddItem createOddItem(DigitainStakeData stake, String groupName, BetType betType, Double param) {
        Double factor = stake.getFactor();
        if (factor == null || factor <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return null;
        }

        OddItem item = new OddItem();
        item.setFactorId(stake.getId() != null ? String.valueOf(stake.getId()) : groupName + "_" + stake.getNameEn());
        item.setGroupName(groupName);
        item.setName(stake.getNameEn() != null ? stake.getNameEn() : stake.getNameRu());
        item.setValue(factor);
        item.setBetType(betType);
        return item;
    }

    protected Double parseTotalValue(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher matcher = TOTAL_PARAM_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
