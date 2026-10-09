package pro.datawiki.igaming.source.betano.strategy;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.source.betano.dto.BetanoSelection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractBetanoStrategy implements BetanoMarketStrategy {

    private static final Pattern TOTAL_PARAM_PATTERN = Pattern.compile("[-+]?\\d*\\.?\\d+");

    protected OddItem createOddItem(BetanoSelection selection, String groupName, BetType betType) {
        Double price = selection.getResolvedPrice();
        if (price == null || price <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return null;
        }

        OddItem item = new OddItem();
        item.setFactorId(selection.getId() != null ? selection.getId() : groupName + "_" + selection.getResolvedName().replaceAll("[^a-zA-Z0-9_+.-]", "_"));
        item.setGroupName(groupName);
        item.setName(selection.getResolvedName());
        item.setValue(price);
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
