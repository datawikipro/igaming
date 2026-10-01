package pro.datawiki.igaming.source.betnacional.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalEventDto;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base abstract class for Betnacional market handlers providing helper methods.
 */
public abstract class AbstractBetnacionalMarketHandler extends AbstractBetTypeMapper implements BetnacionalMarketHandler {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "betnacional".equalsIgnoreCase(bookmaker);
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

    protected Double extractNumber(String text, Double fallback) {
        if (fallback != null) return fallback;
        if (text == null) return null;
        String normalized = text.replace(',', '.');
        Matcher m = NUMERIC_PATTERN.matcher(normalized);
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

    protected boolean isOver(String text) {
        if (text == null) return false;
        String upper = text.toUpperCase();
        return upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ") || upper.endsWith(" OVER") ||
               upper.startsWith("MAIS") || upper.startsWith("ACIMA") || upper.contains(" MAIS ") || upper.contains(" ACIMA ") ||
               upper.endsWith(" MAIS") || upper.endsWith(" ACIMA") || upper.startsWith("+");
    }

    protected boolean isUnder(String text) {
        if (text == null) return false;
        String upper = text.toUpperCase();
        return upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.endsWith(" UNDER") ||
               upper.startsWith("MENOS") || upper.startsWith("ABAIXO") || upper.contains(" MENOS ") || upper.contains(" ABAIXO ") ||
               upper.endsWith(" MENOS") || upper.endsWith(" ABAIXO") || upper.startsWith("-");
    }

    protected boolean isTeam1(String outcomeName, BetnacionalEventDto event) {
        if (outcomeName == null || outcomeName.isBlank()) return false;
        String upper = outcomeName.trim().toUpperCase();
        if (event != null && event.getHomeTeam() != null && !event.getHomeTeam().isBlank()) {
            String home = event.getHomeTeam().trim().toUpperCase();
            if (upper.equals(home) || upper.contains(home) || home.contains(upper)) {
                return true;
            }
        }
        return "1".equals(upper) || upper.startsWith("1 ") || upper.startsWith("1 (") || upper.startsWith("1-") ||
               upper.startsWith("HOME") || upper.startsWith("CASA") ||
               upper.startsWith("TEAM 1") || upper.startsWith("TEAM1") || upper.startsWith("EQUIPE 1") || upper.startsWith("TIME 1");
    }

    protected boolean isTeam2(String outcomeName, BetnacionalEventDto event) {
        if (outcomeName == null || outcomeName.isBlank()) return false;
        String upper = outcomeName.trim().toUpperCase();
        if (event != null && event.getAwayTeam() != null && !event.getAwayTeam().isBlank()) {
            String away = event.getAwayTeam().trim().toUpperCase();
            if (upper.equals(away) || upper.contains(away) || away.contains(upper)) {
                return true;
            }
        }
        return "2".equals(upper) || upper.startsWith("2 ") || upper.startsWith("2 (") || upper.startsWith("2-") ||
               upper.startsWith("AWAY") || upper.startsWith("FORA") ||
               upper.startsWith("TEAM 2") || upper.startsWith("TEAM2") || upper.startsWith("EQUIPE 2") || upper.startsWith("TIME 2");
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
            upper.contains("INTERVALO / FINAL") || upper.contains("INTERVALO/FINAL")) {
            return BetScope.FULL_MATCH;
        }

        // Halves (English & Portuguese)
        if (upper.contains("1ST HALF") || upper.contains("FIRST HALF") || upper.contains("1. HALF") ||
            upper.contains("HT1") || upper.contains("HALF TIME") || upper.contains("HALF-TIME") ||
            upper.contains("1ST H") || upper.matches(".*\\b1H\\b.*") ||
            upper.contains("1° TEMPO") || upper.contains("1º TEMPO") || upper.contains("1. TEMPO") ||
            upper.contains("PRIMEIRO TEMPO") || upper.contains("1ER TEMPO") || upper.contains("1T")) {
            return BetScope.HALF_1;
        }
        if (upper.contains("2ND HALF") || upper.contains("SECOND HALF") || upper.contains("2. HALF") ||
            upper.contains("HT2") || upper.contains("2ND H") || upper.matches(".*\\b2H\\b.*") ||
            upper.contains("2° TEMPO") || upper.contains("2º TEMPO") || upper.contains("2. TEMPO") ||
            upper.contains("SEGUNDO TEMPO") || upper.contains("2DO TEMPO") || upper.contains("2T")) {
            return BetScope.HALF_2;
        }

        // Esports Maps / Games
        if (upper.contains("MAP 1") || upper.contains("1ST MAP") || upper.contains("GAME 1") || upper.contains("1ST GAME") || upper.contains("MAPA 1") ||
            upper.contains("1° MAPA") || upper.contains("1º MAPA") || upper.contains("1. MAPA") || upper.contains("PRIMEIRO MAPA") || upper.contains("MAP 01") || upper.contains("MAPA 01")) {
            return BetScope.MAP_1;
        }
        if (upper.contains("MAP 2") || upper.contains("2ND MAP") || upper.contains("GAME 2") || upper.contains("2ND GAME") || upper.contains("MAPA 2") ||
            upper.contains("2° MAPA") || upper.contains("2º MAPA") || upper.contains("2. MAPA") || upper.contains("SEGUNDO MAPA") || upper.contains("MAP 02") || upper.contains("MAPA 02")) {
            return BetScope.MAP_2;
        }
        if (upper.contains("MAP 3") || upper.contains("3RD MAP") || upper.contains("GAME 3") || upper.contains("3RD GAME") || upper.contains("MAPA 3") ||
            upper.contains("3° MAPA") || upper.contains("3º MAPA") || upper.contains("3. MAPA") || upper.contains("TERCEIRO MAPA") || upper.contains("MAP 03") || upper.contains("MAPA 03")) {
            return BetScope.MAP_3;
        }
        if (upper.contains("MAP 4") || upper.contains("4TH MAP") || upper.contains("GAME 4") || upper.contains("4TH GAME") || upper.contains("MAPA 4") ||
            upper.contains("4° MAPA") || upper.contains("4º MAPA") || upper.contains("4. MAPA") || upper.contains("QUARTO MAPA") || upper.contains("MAP 04") || upper.contains("MAPA 04")) {
            return BetScope.MAP_4;
        }
        if (upper.contains("MAP 5") || upper.contains("5TH MAP") || upper.contains("GAME 5") || upper.contains("5TH GAME") || upper.contains("MAPA 5") ||
            upper.contains("5° MAPA") || upper.contains("5º MAPA") || upper.contains("5. MAPA") || upper.contains("QUINTO MAPA") || upper.contains("MAP 05") || upper.contains("MAPA 05")) {
            return BetScope.MAP_5;
        }
        if (upper.contains("MAP 6") || upper.contains("6TH MAP") || upper.contains("GAME 6") || upper.contains("6TH GAME") || upper.contains("MAPA 6") ||
            upper.contains("6° MAPA") || upper.contains("6º MAPA") || upper.contains("6. MAPA")) {
            return BetScope.MAP_6;
        }
        if (upper.contains("MAP 7") || upper.contains("7TH MAP") || upper.contains("GAME 7") || upper.contains("7TH GAME") || upper.contains("MAPA 7") ||
            upper.contains("7° MAPA") || upper.contains("7º MAPA") || upper.contains("7. MAPA")) {
            return BetScope.MAP_7;
        }

        // Rounds
        if (upper.contains("ROUND 1") || upper.contains("1ST ROUND") || upper.contains("RODADA 1") ||
            upper.contains("1° ROUND") || upper.contains("1º ROUND") || upper.contains("1° RODADA") || upper.contains("1º RODADA")) {
            return BetScope.ROUND_1;
        }
        if (upper.contains("ROUND 2") || upper.contains("2ND ROUND") || upper.contains("RODADA 2") ||
            upper.contains("2° ROUND") || upper.contains("2º ROUND") || upper.contains("2° RODADA") || upper.contains("2º RODADA")) {
            return BetScope.ROUND_2;
        }
        if (upper.contains("ROUND 3") || upper.contains("3RD ROUND") || upper.contains("RODADA 3") ||
            upper.contains("3° ROUND") || upper.contains("3º ROUND") || upper.contains("3° RODADA") || upper.contains("3º RODADA")) {
            return BetScope.ROUND_3;
        }
        if (upper.contains("ROUND 4") || upper.contains("4TH ROUND") || upper.contains("RODADA 4") ||
            upper.contains("4° ROUND") || upper.contains("4º ROUND") || upper.contains("4° RODADA") || upper.contains("4º RODADA")) {
            return BetScope.ROUND_4;
        }
        if (upper.contains("ROUND 5") || upper.contains("5TH ROUND") || upper.contains("RODADA 5") ||
            upper.contains("5° ROUND") || upper.contains("5º ROUND") || upper.contains("5° RODADA") || upper.contains("5º RODADA")) {
            return BetScope.ROUND_5;
        }

        // Quarters
        if (upper.contains("QUARTER 1") || upper.contains("1ST QUARTER") || upper.contains("1° QUARTO") || upper.contains("1º QUARTO") || upper.contains("QUARTO 1") || upper.matches(".*\\b1Q\\b.*")) {
            return BetScope.QUARTER_1;
        }
        if (upper.contains("QUARTER 2") || upper.contains("2ND QUARTER") || upper.contains("2° QUARTO") || upper.contains("2º QUARTO") || upper.contains("QUARTO 2") || upper.matches(".*\\b2Q\\b.*")) {
            return BetScope.QUARTER_2;
        }
        if (upper.contains("QUARTER 3") || upper.contains("3RD QUARTER") || upper.contains("3° QUARTO") || upper.contains("3º QUARTO") || upper.contains("QUARTO 3") || upper.matches(".*\\b3Q\\b.*")) {
            return BetScope.QUARTER_3;
        }
        if (upper.contains("QUARTER 4") || upper.contains("4TH QUARTER") || upper.contains("4° QUARTO") || upper.contains("4º QUARTO") || upper.contains("QUARTO 4") || upper.matches(".*\\b4Q\\b.*")) {
            return BetScope.QUARTER_4;
        }

        // Sets
        if (upper.contains("SET 1") || upper.contains("1ST SET") || upper.contains("1° SET") || upper.contains("1º SET") || upper.contains("PRIMEIRO SET")) {
            return BetScope.SET_1;
        }
        if (upper.contains("SET 2") || upper.contains("2ND SET") || upper.contains("2° SET") || upper.contains("2º SET") || upper.contains("SEGUNDO SET")) {
            return BetScope.SET_2;
        }
        if (upper.contains("SET 3") || upper.contains("3RD SET") || upper.contains("3° SET") || upper.contains("3º SET") || upper.contains("TERCEIRO SET")) {
            return BetScope.SET_3;
        }
        if (upper.contains("SET 4") || upper.contains("4TH SET") || upper.contains("4° SET") || upper.contains("4º SET")) {
            return BetScope.SET_4;
        }
        if (upper.contains("SET 5") || upper.contains("5TH SET") || upper.contains("5° SET") || upper.contains("5º SET")) {
            return BetScope.SET_5;
        }

        // Periods
        if (upper.contains("PERIOD 1") || upper.contains("1ST PERIOD") || upper.contains("1° PERÍODO") || upper.contains("1º PERIODO") || upper.contains("PERÍODO 1") || upper.contains("PERIODO 1")) {
            return BetScope.PERIOD_1;
        }
        if (upper.contains("PERIOD 2") || upper.contains("2ND PERIOD") || upper.contains("2° PERÍODO") || upper.contains("2º PERIODO") || upper.contains("PERÍODO 2") || upper.contains("PERIODO 2")) {
            return BetScope.PERIOD_2;
        }
        if (upper.contains("PERIOD 3") || upper.contains("3RD PERIOD") || upper.contains("3° PERÍODO") || upper.contains("3º PERIODO") || upper.contains("PERÍODO 3") || upper.contains("PERIODO 3")) {
            return BetScope.PERIOD_3;
        }
        if (upper.contains("PERIOD 4") || upper.contains("4TH PERIOD") || upper.contains("4° PERÍODO") || upper.contains("4º PERIODO") || upper.contains("PERÍODO 4") || upper.contains("PERIODO 4")) {
            return BetScope.PERIOD_4;
        }
        if (upper.contains("PERIOD 5") || upper.contains("5TH PERIOD") || upper.contains("5° PERÍODO") || upper.contains("5º PERIODO") || upper.contains("PERÍODO 5") || upper.contains("PERIODO 5")) {
            return BetScope.PERIOD_5;
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
