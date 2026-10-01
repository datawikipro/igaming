package pro.datawiki.igaming.source.wplay.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base abstract class for Wplay market handlers providing localization (Spanish/English) and mapping helpers.
 */
public abstract class AbstractWplayMarketHandler extends AbstractBetTypeMapper implements WplayMarketHandler {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "wplay".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String market, String outcome, Double param) {
        return null;
    }

    /**
     * Adds an OddItem to the items list with validation and standard factorId formatting.
     */
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

    /**
     * Extracts decimal/integer number from text, replacing comma with period.
     */
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

    /**
     * Checks if text represents Over / Más.
     */
    protected boolean isOver(String text) {
        if (text == null) return false;
        String upper = text.toUpperCase();
        return upper.startsWith("OVER") || upper.startsWith("O ") || upper.contains(" OVER ") || upper.endsWith(" OVER") ||
               upper.startsWith("MÁS") || upper.startsWith("MAS") || upper.contains(" MÁS ") || upper.contains(" MAS ") ||
               upper.endsWith(" MÁS") || upper.endsWith(" MAS") || upper.startsWith("MÁS DE") || upper.startsWith("MAS DE") ||
               upper.startsWith("ACIMA") || upper.startsWith("+");
    }

    /**
     * Checks if text represents Under / Menos.
     */
    protected boolean isUnder(String text) {
        if (text == null) return false;
        String upper = text.toUpperCase();
        return upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.endsWith(" UNDER") ||
               upper.startsWith("MENOS") || upper.contains(" MENOS ") || upper.endsWith(" MENOS") ||
               upper.startsWith("MENOS DE") || upper.startsWith("ABAIXO") || upper.startsWith("-");
    }

    /**
     * Checks if outcome corresponds to Team 1 (Home, Local, 1).
     */
    protected boolean isTeam1(String outcomeName, WplayEventDto event) {
        if (outcomeName == null || outcomeName.isBlank()) return false;
        String upper = outcomeName.trim().toUpperCase();
        if (event != null && event.getHomeTeam() != null && !event.getHomeTeam().isBlank()) {
            String home = event.getHomeTeam().trim().toUpperCase();
            if (upper.equals(home) || upper.contains(home) || home.contains(upper)) {
                return true;
            }
        }
        return "1".equals(upper) || upper.startsWith("1 ") || upper.startsWith("1 (") || upper.startsWith("1-") ||
               upper.startsWith("HOME") || upper.startsWith("LOCAL") || upper.startsWith("CASA") ||
               upper.startsWith("TEAM 1") || upper.startsWith("TEAM1") || upper.startsWith("EQUIPO 1") || upper.startsWith("EQUIPO1");
    }

    /**
     * Checks if outcome corresponds to Team 2 (Away, Visitante, 2).
     */
    protected boolean isTeam2(String outcomeName, WplayEventDto event) {
        if (outcomeName == null || outcomeName.isBlank()) return false;
        String upper = outcomeName.trim().toUpperCase();
        if (event != null && event.getAwayTeam() != null && !event.getAwayTeam().isBlank()) {
            String away = event.getAwayTeam().trim().toUpperCase();
            if (upper.equals(away) || upper.contains(away) || away.contains(upper)) {
                return true;
            }
        }
        return "2".equals(upper) || upper.startsWith("2 ") || upper.startsWith("2 (") || upper.startsWith("2-") ||
               upper.startsWith("AWAY") || upper.startsWith("VISITANTE") || upper.startsWith("FORA") ||
               upper.startsWith("TEAM 2") || upper.startsWith("TEAM2") || upper.startsWith("EQUIPO 2") || upper.startsWith("EQUIPO2");
    }

    /**
     * Checks if outcome corresponds to Draw / Empate.
     */
    protected boolean isDraw(String outcomeName) {
        if (outcomeName == null || outcomeName.isBlank()) return false;
        String upper = outcomeName.trim().toUpperCase();
        return "X".equals(upper) || upper.contains("DRAW") || upper.contains("EMPATE") || upper.contains("TIE");
    }

    /**
     * Suffixes group name with scope when scope is not FULL_MATCH.
     */
    protected String formatGroupName(String baseGroup, BetScope scope) {
        if (scope == null || scope == BetScope.FULL_MATCH) {
            return baseGroup;
        }
        return baseGroup + "_" + scope.name().toLowerCase();
    }

    /**
     * Resolves the BetScope from text (Spanish and English).
     */
    protected BetScope resolveScope(String text) {
        if (text == null) return BetScope.FULL_MATCH;
        String upper = text.toUpperCase();

        if (upper.contains("HALF TIME / FULL TIME") || upper.contains("HALF TIME/FULL TIME") ||
            upper.contains("HALF-TIME / FULL-TIME") || upper.contains("HT/FT") || upper.contains("HT / FT") ||
            upper.contains("DESCANSO / FINAL") || upper.contains("DESCANSO/FINAL") ||
            upper.contains("MEDIO TIEMPO / TIEMPO COMPLETO") || upper.contains("MEDIO TIEMPO/TIEMPO COMPLETO")) {
            return BetScope.FULL_MATCH;
        }

        // Halves (Spanish & English)
        if (upper.contains("1ST HALF") || upper.contains("FIRST HALF") || upper.contains("1. HALF") ||
            upper.contains("HT1") || upper.contains("HALF TIME") || upper.contains("HALF-TIME") ||
            upper.contains("1ST H") || upper.matches(".*\\b1H\\b.*") ||
            upper.contains("1ER TIEMPO") || upper.contains("1° TIEMPO") || upper.contains("1º TIEMPO") ||
            upper.contains("PRIMER TIEMPO") || upper.contains("1RA MITAD") || upper.contains("1ª PARTE") ||
            upper.contains("1T") || upper.matches(".*\\b1T\\b.*")) {
            return BetScope.HALF_1;
        }
        if (upper.contains("2ND HALF") || upper.contains("SECOND HALF") || upper.contains("2. HALF") ||
            upper.contains("HT2") || upper.contains("2ND H") || upper.matches(".*\\b2H\\b.*") ||
            upper.contains("2DO TIEMPO") || upper.contains("2° TIEMPO") || upper.contains("2º TIEMPO") ||
            upper.contains("SEGUNDO TIEMPO") || upper.contains("2DA MITAD") || upper.contains("2ª PARTE") ||
            upper.contains("2T") || upper.matches(".*\\b2T\\b.*")) {
            return BetScope.HALF_2;
        }

        // Esports Maps
        if (upper.contains("MAP 1") || upper.contains("1ST MAP") || upper.contains("MAPA 1") || upper.contains("1ER MAPA") ||
            upper.contains("GAME 1") || upper.contains("1ST GAME") || upper.contains("JUEGO 1") || upper.contains("1ER JUEGO")) {
            return BetScope.MAP_1;
        }
        if (upper.contains("MAP 2") || upper.contains("2ND MAP") || upper.contains("MAPA 2") || upper.contains("2DO MAPA") ||
            upper.contains("GAME 2") || upper.contains("2ND GAME") || upper.contains("JUEGO 2") || upper.contains("2DO JUEGO")) {
            return BetScope.MAP_2;
        }
        if (upper.contains("MAP 3") || upper.contains("3RD MAP") || upper.contains("MAPA 3") || upper.contains("3ER MAPA") ||
            upper.contains("GAME 3") || upper.contains("3RD GAME") || upper.contains("JUEGO 3") || upper.contains("3ER JUEGO")) {
            return BetScope.MAP_3;
        }
        if (upper.contains("MAP 4") || upper.contains("4TH MAP") || upper.contains("MAPA 4") || upper.contains("4TO MAPA") ||
            upper.contains("GAME 4") || upper.contains("4TH GAME") || upper.contains("JUEGO 4") || upper.contains("4TO JUEGO")) {
            return BetScope.MAP_4;
        }
        if (upper.contains("MAP 5") || upper.contains("5TH MAP") || upper.contains("MAPA 5") || upper.contains("5TO MAPA") ||
            upper.contains("GAME 5") || upper.contains("5TH GAME") || upper.contains("JUEGO 5") || upper.contains("5TO JUEGO")) {
            return BetScope.MAP_5;
        }
        if (upper.contains("MAP 6") || upper.contains("6TH MAP") || upper.contains("MAPA 6") || upper.contains("6TO MAPA") ||
            upper.contains("GAME 6") || upper.contains("6TH GAME")) {
            return BetScope.MAP_6;
        }
        if (upper.contains("MAP 7") || upper.contains("7TH MAP") || upper.contains("MAPA 7") || upper.contains("7MO MAPA") ||
            upper.contains("GAME 7") || upper.contains("7TH GAME")) {
            return BetScope.MAP_7;
        }

        // Esports Rounds
        if (upper.contains("ROUND 1") || upper.contains("1ST ROUND") || upper.contains("RONDA 1") || upper.contains("1RA RONDA") ||
            upper.contains("1° RONDA") || upper.contains("1º RONDA")) {
            return BetScope.ROUND_1;
        }
        if (upper.contains("ROUND 2") || upper.contains("2ND ROUND") || upper.contains("RONDA 2") || upper.contains("2DA RONDA") ||
            upper.contains("2° RONDA") || upper.contains("2º RONDA")) {
            return BetScope.ROUND_2;
        }
        if (upper.contains("ROUND 3") || upper.contains("3RD ROUND") || upper.contains("RONDA 3") || upper.contains("3RA RONDA") ||
            upper.contains("3° RONDA") || upper.contains("3º RONDA")) {
            return BetScope.ROUND_3;
        }
        if (upper.contains("ROUND 4") || upper.contains("4TH ROUND") || upper.contains("RONDA 4") || upper.contains("4TA RONDA") ||
            upper.contains("4° RONDA") || upper.contains("4º RONDA")) {
            return BetScope.ROUND_4;
        }
        if (upper.contains("ROUND 5") || upper.contains("5TH ROUND") || upper.contains("RONDA 5") || upper.contains("5TA RONDA") ||
            upper.contains("5° RONDA") || upper.contains("5º RONDA")) {
            return BetScope.ROUND_5;
        }

        // Quarters
        if (upper.contains("QUARTER 1") || upper.contains("1ST QUARTER") || upper.contains("CUARTO 1") || upper.contains("1ER CUARTO") ||
            upper.contains("1° CUARTO") || upper.contains("1º CUARTO") || upper.matches(".*\\b1Q\\b.*")) {
            return BetScope.QUARTER_1;
        }
        if (upper.contains("QUARTER 2") || upper.contains("2ND QUARTER") || upper.contains("CUARTO 2") || upper.contains("2DO CUARTO") ||
            upper.contains("2° CUARTO") || upper.contains("2º CUARTO") || upper.matches(".*\\b2Q\\b.*")) {
            return BetScope.QUARTER_2;
        }
        if (upper.contains("QUARTER 3") || upper.contains("3RD QUARTER") || upper.contains("CUARTO 3") || upper.contains("3ER CUARTO") ||
            upper.contains("3° CUARTO") || upper.contains("3º CUARTO") || upper.matches(".*\\b3Q\\b.*")) {
            return BetScope.QUARTER_3;
        }
        if (upper.contains("QUARTER 4") || upper.contains("4TH QUARTER") || upper.contains("CUARTO 4") || upper.contains("4TO CUARTO") ||
            upper.contains("4° CUARTO") || upper.contains("4º CUARTO") || upper.matches(".*\\b4Q\\b.*")) {
            return BetScope.QUARTER_4;
        }

        // Periods / Sets
        if (upper.contains("PERIOD 1") || upper.contains("1ST PERIOD") || upper.contains("PERIODO 1") || upper.contains("1ER PERIODO") ||
            upper.contains("SET 1") || upper.contains("1ST SET") || upper.contains("1ER SET") || upper.contains("1° SET") || upper.contains("1º SET")) {
            return BetScope.PERIOD_1;
        }
        if (upper.contains("PERIOD 2") || upper.contains("2ND PERIOD") || upper.contains("PERIODO 2") || upper.contains("2DO PERIODO") ||
            upper.contains("SET 2") || upper.contains("2ND SET") || upper.contains("2DO SET") || upper.contains("2° SET") || upper.contains("2º SET")) {
            return BetScope.PERIOD_2;
        }
        if (upper.contains("PERIOD 3") || upper.contains("3RD PERIOD") || upper.contains("PERIODO 3") || upper.contains("3ER PERIODO") ||
            upper.contains("SET 3") || upper.contains("3RD SET") || upper.contains("3ER SET") || upper.contains("3° SET") || upper.contains("3º SET")) {
            return BetScope.PERIOD_3;
        }
        if (upper.contains("SET 4") || upper.contains("4TH SET") || upper.contains("4TO SET")) {
            return BetScope.SET_4;
        }
        if (upper.contains("SET 5") || upper.contains("5TH SET") || upper.contains("5TO SET")) {
            return BetScope.SET_5;
        }

        return BetScope.FULL_MATCH;
    }

    /**
     * Checks if sportType is an esports discipline.
     */
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
