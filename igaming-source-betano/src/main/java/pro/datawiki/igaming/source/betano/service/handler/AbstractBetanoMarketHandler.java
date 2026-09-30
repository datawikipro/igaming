package pro.datawiki.igaming.source.betano.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base abstract class for Betano market handlers providing helper methods.
 */
public abstract class AbstractBetanoMarketHandler extends AbstractBetTypeMapper implements BetanoMarketHandler {

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "betano".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String market, String outcome, Double param) {
        return null;
    }

    protected void addOddItem(List<OddItem> items, String groupName, String outcomeName, Double value, BetType betType) {
        if (value == null || value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        String safeName = outcomeName != null ? outcomeName.trim() : "Outcome";
        String factorId = groupName + "_" + safeName.replaceAll("[^a-zA-Z0-9_+.-]", "_");
        item.setFactorId(factorId);
        item.setGroupName(groupName);
        item.setName(safeName);
        item.setValue(value);
        item.setBetType(betType);
        items.add(item);
    }

    private static final Pattern PARENTHESIS_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern SIGNED_OR_DECIMAL_PATTERN = Pattern.compile("([+-]\\d+(?:\\.\\d+)?|\\d+\\.\\d+)");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");

    protected Double extractNumber(String text, Double fallback) {
        if (fallback != null) {
            if (text != null && (text.contains("(-") || text.contains("(- ") || text.contains(" -")) && fallback > 0) {
                return -fallback;
            }
            return fallback;
        }
        if (text == null) return null;

        Matcher mParen = PARENTHESIS_PATTERN.matcher(text);
        if (mParen.find()) {
            try {
                return Double.parseDouble(mParen.group(1));
            } catch (NumberFormatException ignored) {}
        }

        Matcher mSigned = SIGNED_OR_DECIMAL_PATTERN.matcher(text);
        if (mSigned.find()) {
            try {
                return Double.parseDouble(mSigned.group(1));
            } catch (NumberFormatException ignored) {}
        }

        Matcher m = NUMERIC_PATTERN.matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    protected Double extractNumber(String text, Double fallback, String fallbackMarketName) {
        Double val = extractNumber(text, fallback);
        if (val != null) {
            return val;
        }
        return extractNumber(fallbackMarketName, null);
    }

    protected String formatGroupName(String baseGroup, BetScope scope) {
        if (scope == null || scope == BetScope.FULL_MATCH) {
            return baseGroup;
        }
        return baseGroup + "_" + scope.name().toLowerCase();
    }

    protected BetScope resolveScope(String text) {
        if (text == null) return BetScope.FULL_MATCH;
        String upper = text.toUpperCase();

        if (upper.contains("HALF TIME / FULL TIME") || upper.contains("HALF TIME/FULL TIME") ||
            upper.contains("HALF-TIME / FULL-TIME") || upper.contains("HT/FT") || upper.contains("HT / FT") ||
            upper.contains("INTERVALO / FINAL DO JOGO") || upper.contains("INTERVALO/FINAL")) {
            return BetScope.FULL_MATCH;
        }

        if (upper.contains("1ST HALF") || upper.contains("FIRST HALF") || upper.contains("1. HALF") ||
            upper.contains("HT1") || upper.contains("HALF TIME") || upper.contains("HALF-TIME") ||
            upper.contains("1ST H") || upper.matches(".*\\b1H\\b.*") ||
            upper.contains("1.º TEMPO") || upper.contains("1º TEMPO") || upper.contains("1ER TIEMPO") || upper.contains("PRIMER TIEMPO")) {
            return BetScope.HALF_1;
        }
        if (upper.contains("2ND HALF") || upper.contains("SECOND HALF") || upper.contains("2. HALF") ||
            upper.contains("HT2") || upper.contains("2ND H") || upper.matches(".*\\b2H\\b.*") ||
            upper.contains("2.º TEMPO") || upper.contains("2º TEMPO") || upper.contains("2DO TIEMPO") || upper.contains("SEGUNDO TIEMPO")) {
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
        if (upper.contains("MAP 6") || upper.contains("6TH MAP") || upper.contains("GAME 6") || upper.contains("6TH GAME")) {
            return BetScope.MAP_6;
        }
        if (upper.contains("MAP 7") || upper.contains("7TH MAP") || upper.contains("GAME 7") || upper.contains("7TH GAME")) {
            return BetScope.MAP_7;
        }
        if (upper.contains("ROUND 1") || upper.contains("1ST ROUND")) {
            return BetScope.ROUND_1;
        }
        if (upper.contains("ROUND 2") || upper.contains("2ND ROUND")) {
            return BetScope.ROUND_2;
        }
        if (upper.contains("ROUND 3") || upper.contains("3RD ROUND")) {
            return BetScope.ROUND_3;
        }
        if (upper.contains("ROUND 4") || upper.contains("4TH ROUND")) {
            return BetScope.ROUND_4;
        }
        if (upper.contains("ROUND 5") || upper.contains("5TH ROUND")) {
            return BetScope.ROUND_5;
        }
        if (upper.contains("PERIOD 1") || upper.contains("1ST PERIOD") || upper.contains("1.º PERÍODO") || upper.contains("1º PERÍODO")) {
            return BetScope.PERIOD_1;
        }
        if (upper.contains("PERIOD 2") || upper.contains("2ND PERIOD") || upper.contains("2.º PERÍODO") || upper.contains("2º PERÍODO")) {
            return BetScope.PERIOD_2;
        }
        if (upper.contains("PERIOD 3") || upper.contains("3RD PERIOD") || upper.contains("3.º PERÍODO") || upper.contains("3º PERÍODO")) {
            return BetScope.PERIOD_3;
        }
        if (upper.contains("QUARTER 1") || upper.contains("1ST QUARTER") || upper.contains("1.º QUARTO") || upper.contains("1º QUARTO")) {
            return BetScope.QUARTER_1;
        }
        if (upper.contains("QUARTER 2") || upper.contains("2ND QUARTER") || upper.contains("2.º QUARTO") || upper.contains("2º QUARTO")) {
            return BetScope.QUARTER_2;
        }
        if (upper.contains("QUARTER 3") || upper.contains("3RD QUARTER") || upper.contains("3.º QUARTO") || upper.contains("3º QUARTO")) {
            return BetScope.QUARTER_3;
        }
        if (upper.contains("QUARTER 4") || upper.contains("4TH QUARTER") || upper.contains("4.º QUARTO") || upper.contains("4º QUARTO")) {
            return BetScope.QUARTER_4;
        }
        if (upper.contains("SET 1") || upper.contains("1ST SET") || upper.contains("1.º SET") || upper.contains("1º SET")) {
            return BetScope.SET_1;
        }
        if (upper.contains("SET 2") || upper.contains("2ND SET") || upper.contains("2.º SET") || upper.contains("2º SET")) {
            return BetScope.SET_2;
        }
        if (upper.contains("SET 3") || upper.contains("3RD SET") || upper.contains("3.º SET") || upper.contains("3º SET")) {
            return BetScope.SET_3;
        }
        if (upper.contains("SET 4") || upper.contains("4TH SET") || upper.contains("4.º SET") || upper.contains("4º SET")) {
            return BetScope.SET_4;
        }
        if (upper.contains("SET 5") || upper.contains("5TH SET") || upper.contains("5.º SET") || upper.contains("5º SET")) {
            return BetScope.SET_5;
        }
        return BetScope.FULL_MATCH;
    }

    protected boolean isEsports(SportType sportType) {
        return sportType == SportType.CS2 ||
               sportType == SportType.DOTA2 ||
               sportType == SportType.LEAGUE_OF_LEGENDS ||
               sportType == SportType.VALORANT ||
               sportType == SportType.ESPORTS ||
               sportType == SportType.RAINBOW_SIX ||
               sportType == SportType.ROCKET_LEAGUE ||
               sportType == SportType.CALL_OF_DUTY ||
               sportType == SportType.OVERWATCH ||
               sportType == SportType.PUBG ||
               sportType == SportType.STARCRAFT ||
               sportType == SportType.MOBILE_LEGENDS;
    }
}
