package pro.datawiki.igaming.source.bwin.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainOption;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainOptionMarket;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base abstract class for Bwin market handlers providing helper methods.
 */
public abstract class AbstractBwinMarketHandler extends AbstractBetTypeMapper implements BwinMarketHandler {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("([+-]?\\d+(?:[.,]\\d+)?)");

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "bwin".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String market, String outcome, Double param) {
        return null;
    }

    protected String getMarketName(EntainOptionMarket market) {
        if (market != null && market.getName() != null && market.getName().getValue() != null) {
            return market.getName().getValue().trim();
        }
        return "";
    }

    protected String getOptionName(EntainOption option) {
        if (option != null && option.getName() != null && option.getName().getValue() != null) {
            return option.getName().getValue().trim();
        }
        return "";
    }

    protected Double getOptionOdds(EntainOption option) {
        if (option != null && option.getPrice() != null && option.getPrice().getOdds() != null) {
            return option.getPrice().getOdds();
        }
        return null;
    }

    protected void addOddItem(List<OddItem> items, EntainOption option, String groupName, String outcomeName, Double odds, BetType betType) {
        if (odds == null || odds <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        item.setFactorId(option != null && option.getId() != null ? String.valueOf(option.getId()) : (groupName + "_" + outcomeName));
        item.setGroupName(groupName);
        item.setName(outcomeName);
        item.setValue(odds);
        item.setBetType(betType);
        items.add(item);
    }

    protected Double extractNumber(String text, Double fallback) {
        if (fallback != null) return fallback;
        if (text == null) return null;
        Matcher m = NUMERIC_PATTERN.matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1).replace(",", "."));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    protected BetScope resolveScope(String text) {
        if (text == null) return BetScope.FULL_MATCH;
        String upper = text.toUpperCase();

        if (upper.contains("HALF TIME / FULL TIME") || upper.contains("HALF TIME/FULL TIME") ||
            upper.contains("HALF-TIME / FULL-TIME") || upper.contains("HT/FT") || upper.contains("HT / FT")) {
            return BetScope.FULL_MATCH;
        }

        if (upper.contains("1ST HALF") || upper.contains("FIRST HALF") || upper.contains("1. HALF") ||
            upper.contains("HT1") || upper.contains("HALF TIME") || upper.contains("HALF-TIME") ||
            upper.contains("1ST H") || upper.matches(".*\\b1H\\b.*")) {
            return BetScope.HALF_1;
        }
        if (upper.contains("2ND HALF") || upper.contains("SECOND HALF") || upper.contains("2. HALF") ||
            upper.contains("HT2") || upper.contains("2ND H") || upper.matches(".*\\b2H\\b.*")) {
            return BetScope.HALF_2;
        }
        if (upper.contains("MAP 1") || upper.contains("1ST MAP") || upper.contains("GAME 1") || upper.contains("1ST GAME")) {
            return BetScope.MAP_1;
        }
        if (upper.contains("MAP 2") || upper.contains("2ND MAP") || upper.contains("GAME 2") || upper.contains("2ND GAME")) {
            return BetScope.MAP_2;
        }
        if (upper.contains("MAP 3") || upper.contains("3RD MAP") || upper.contains("GAME 3") || upper.contains("3RD GAME")) {
            return BetScope.MAP_3;
        }
        if (upper.contains("MAP 4") || upper.contains("4TH MAP") || upper.contains("GAME 4") || upper.contains("4TH GAME")) {
            return BetScope.MAP_4;
        }
        if (upper.contains("MAP 5") || upper.contains("5TH MAP") || upper.contains("GAME 5") || upper.contains("5TH GAME")) {
            return BetScope.MAP_5;
        }
        if (upper.contains("1ST PERIOD") || upper.contains("FIRST PERIOD") || upper.contains("1. PERIOD")) {
            return BetScope.PERIOD_1;
        }
        if (upper.contains("2ND PERIOD") || upper.contains("SECOND PERIOD") || upper.contains("2. PERIOD")) {
            return BetScope.PERIOD_2;
        }
        if (upper.contains("3RD PERIOD") || upper.contains("THIRD PERIOD") || upper.contains("3. PERIOD")) {
            return BetScope.PERIOD_3;
        }
        if (upper.contains("1ST SET") || upper.contains("FIRST SET")) {
            return BetScope.SET_1;
        }
        if (upper.contains("2ND SET") || upper.contains("SECOND SET")) {
            return BetScope.SET_2;
        }
        if (upper.contains("3RD SET") || upper.contains("THIRD SET")) {
            return BetScope.SET_3;
        }
        if (upper.contains("1ST QUARTER") || upper.contains("FIRST QUARTER") || upper.matches(".*\\bQ1\\b.*")) {
            return BetScope.QUARTER_1;
        }
        if (upper.contains("2ND QUARTER") || upper.contains("SECOND QUARTER") || upper.matches(".*\\bQ2\\b.*")) {
            return BetScope.QUARTER_2;
        }
        if (upper.contains("3RD QUARTER") || upper.contains("THIRD QUARTER") || upper.matches(".*\\bQ3\\b.*")) {
            return BetScope.QUARTER_3;
        }
        if (upper.contains("4TH QUARTER") || upper.contains("FOURTH QUARTER") || upper.matches(".*\\bQ4\\b.*")) {
            return BetScope.QUARTER_4;
        }

        return BetScope.FULL_MATCH;
    }
}
