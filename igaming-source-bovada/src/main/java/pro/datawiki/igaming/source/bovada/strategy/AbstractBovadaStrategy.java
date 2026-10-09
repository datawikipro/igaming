package pro.datawiki.igaming.source.bovada.strategy;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.source.bovada.dto.BovadaOutcomeDto;
import pro.datawiki.igaming.source.bovada.dto.BovadaPriceDto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractBovadaStrategy implements BovadaMarketStrategy {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");

    protected Double parseDecimalPrice(BovadaPriceDto price) {
        if (price == null) return null;
        if (price.getDecimal() != null && !price.getDecimal().isBlank()) {
            try {
                return Double.parseDouble(price.getDecimal());
            } catch (NumberFormatException ignored) {}
        }
        if (price.getAmerican() != null && !price.getAmerican().isBlank()) {
            try {
                int american = Integer.parseInt(price.getAmerican().replace("+", "").trim());
                if (american > 0) {
                    return 1.0 + (american / 100.0);
                } else if (american < 0) {
                    return 1.0 + (100.0 / Math.abs(american));
                }
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    protected Double parseHandicap(BovadaOutcomeDto outcome) {
        if (outcome.getPrice() != null && outcome.getPrice().getHandicap() != null
                && !outcome.getPrice().getHandicap().isBlank()) {
            try {
                return Double.parseDouble(outcome.getPrice().getHandicap());
            } catch (NumberFormatException ignored) {}
        }
        if (outcome.getDescription() != null) {
            Matcher m = NUMERIC_PATTERN.matcher(outcome.getDescription());
            if (m.find()) {
                try {
                    return Double.parseDouble(m.group(1));
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    protected Double parseTotal(BovadaOutcomeDto outcome) {
        if (outcome.getPrice() != null && outcome.getPrice().getHandicap() != null
                && !outcome.getPrice().getHandicap().isBlank()) {
            try {
                return Math.abs(Double.parseDouble(outcome.getPrice().getHandicap()));
            } catch (NumberFormatException ignored) {}
        }
        if (outcome.getDescription() != null) {
            Matcher m = NUMERIC_PATTERN.matcher(outcome.getDescription());
            if (m.find()) {
                try {
                    return Math.abs(Double.parseDouble(m.group(1)));
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    protected OddItem createOddItem(String group, String name, Double value, BetType betType) {
        if (value == null || value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return null;
        }
        OddItem item = new OddItem();
        item.setFactorId(group + "_" + name.replaceAll("[^a-zA-Z0-9_+.-]", "_"));
        item.setGroupName(group);
        item.setName(name);
        item.setValue(value);
        item.setBetType(betType);
        return item;
    }
}
