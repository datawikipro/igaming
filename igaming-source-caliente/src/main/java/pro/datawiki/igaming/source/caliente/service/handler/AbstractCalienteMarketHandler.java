package pro.datawiki.igaming.source.caliente.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.source.caliente.dto.CalienteEventDto;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base abstract class for Caliente market handlers providing helper methods and Mexican Spanish/English localization.
 */
public abstract class AbstractCalienteMarketHandler extends AbstractBetTypeMapper implements CalienteMarketHandler {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "caliente".equalsIgnoreCase(bookmaker);
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
               upper.startsWith("MÁS") || upper.startsWith("MAS") || upper.startsWith("ALTAS") || upper.startsWith("ALTA") ||
               upper.contains(" MÁS ") || upper.contains(" MAS ") || upper.contains(" ALTAS ") || upper.contains(" ALTA ") ||
               upper.endsWith(" MÁS") || upper.endsWith(" MAS") || upper.endsWith(" ALTAS") || upper.endsWith(" ALTA") ||
               upper.startsWith("MAIS") || upper.startsWith("ACIMA") || upper.startsWith("+");
    }

    protected boolean isUnder(String text) {
        if (text == null) return false;
        String upper = text.toUpperCase();
        return upper.startsWith("UNDER") || upper.startsWith("U ") || upper.contains(" UNDER ") || upper.endsWith(" UNDER") ||
               upper.startsWith("MENOS") || upper.startsWith("BAJAS") || upper.startsWith("BAJA") ||
               upper.contains(" MENOS ") || upper.contains(" BAJAS ") || upper.contains(" BAJA ") ||
               upper.endsWith(" MENOS") || upper.endsWith(" BAJAS") || upper.endsWith(" BAJA") ||
               upper.startsWith("ABAIXO") || upper.startsWith("-");
    }

    protected boolean isTeam1(String outcomeName, CalienteEventDto event) {
        if (outcomeName == null || outcomeName.isBlank()) return false;
        String upper = outcomeName.trim().toUpperCase();
        if (event != null && event.getHomeTeam() != null && !event.getHomeTeam().isBlank()) {
            String home = event.getHomeTeam().trim().toUpperCase();
            if (upper.equals(home) || upper.contains(home) || home.contains(upper)) {
                return true;
            }
        }
        return "1".equals(upper) || upper.startsWith("1 ") || upper.startsWith("1 (") || upper.startsWith("1-") ||
               upper.startsWith("LOCAL") || upper.startsWith("HOME") || upper.startsWith("CASA") ||
               upper.startsWith("TEAM 1") || upper.startsWith("TEAM1") || upper.startsWith("EQUIPO 1");
    }

    protected boolean isTeam2(String outcomeName, CalienteEventDto event) {
        if (outcomeName == null || outcomeName.isBlank()) return false;
        String upper = outcomeName.trim().toUpperCase();
        if (event != null && event.getAwayTeam() != null && !event.getAwayTeam().isBlank()) {
            String away = event.getAwayTeam().trim().toUpperCase();
            if (upper.equals(away) || upper.contains(away) || away.contains(upper)) {
                return true;
            }
        }
        return "2".equals(upper) || upper.startsWith("2 ") || upper.startsWith("2 (") || upper.startsWith("2-") ||
               upper.startsWith("VISITANTE") || upper.startsWith("AWAY") || upper.startsWith("FORA") ||
               upper.startsWith("TEAM 2") || upper.startsWith("TEAM2") || upper.startsWith("EQUIPO 2");
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
            upper.contains("DESCANSO / FINAL") || upper.contains("DESCANSO/FINAL") ||
            upper.contains("MEDIO TIEMPO / TIEMPO COMPLETO") || upper.contains("MEDIO TIEMPO/TIEMPO COMPLETO") ||
            upper.contains("HT/FT") || upper.contains("HT / FT")) {
            return BetScope.FULL_MATCH;
        }

        // Halves (Spanish & English)
        if (upper.contains("1ER TIEMPO") || upper.contains("PRIMER TIEMPO") || upper.contains("1. TIEMPO") ||
            upper.contains("1° TIEMPO") || upper.contains("1º TIEMPO") || upper.contains("1RA MITAD") ||
            upper.contains("PRIMERA MITAD") || upper.contains("1ST HALF") || upper.contains("FIRST HALF") ||
            upper.contains("1. HALF") || upper.contains("HT1") || upper.contains("1T") || upper.matches(".*\\b1H\\b.*")) {
            return BetScope.HALF_1;
        }
        if (upper.contains("2DO TIEMPO") || upper.contains("SEGUNDO TIEMPO") || upper.contains("2. TIEMPO") ||
            upper.contains("2° TIEMPO") || upper.contains("2º TIEMPO") || upper.contains("2DA MITAD") ||
            upper.contains("SEGUNDA MITAD") || upper.contains("2ND HALF") || upper.contains("SECOND HALF") ||
            upper.contains("2. HALF") || upper.contains("HT2") || upper.contains("2T") || upper.matches(".*\\b2H\\b.*")) {
            return BetScope.HALF_2;
        }

        // Esports Maps / Games
        if (upper.contains("MAPA 1") || upper.contains("1ER MAPA") || upper.contains("1° MAPA") || upper.contains("1º MAPA") ||
            upper.contains("MAP 1") || upper.contains("1ST MAP") || upper.contains("GAME 1") || upper.contains("1ST GAME") ||
            upper.contains("PRIMER MAPA") || upper.contains("MAP 01") || upper.contains("MAPA 01")) {
            return BetScope.MAP_1;
        }
        if (upper.contains("MAPA 2") || upper.contains("2DO MAPA") || upper.contains("2° MAPA") || upper.contains("2º MAPA") ||
            upper.contains("MAP 2") || upper.contains("2ND MAP") || upper.contains("GAME 2") || upper.contains("2ND GAME") ||
            upper.contains("SEGUNDO MAPA") || upper.contains("MAP 02") || upper.contains("MAPA 02")) {
            return BetScope.MAP_2;
        }
        if (upper.contains("MAPA 3") || upper.contains("3ER MAPA") || upper.contains("3° MAPA") || upper.contains("3º MAPA") ||
            upper.contains("MAP 3") || upper.contains("3RD MAP") || upper.contains("GAME 3") || upper.contains("3RD GAME") ||
            upper.contains("TERCER MAPA") || upper.contains("MAP 03") || upper.contains("MAPA 03")) {
            return BetScope.MAP_3;
        }
        if (upper.contains("MAPA 4") || upper.contains("4TO MAPA") || upper.contains("4° MAPA") || upper.contains("4º MAPA") ||
            upper.contains("MAP 4") || upper.contains("4TH MAP") || upper.contains("GAME 4") || upper.contains("4TH GAME") ||
            upper.contains("CUARTO MAPA") || upper.contains("MAP 04") || upper.contains("MAPA 04")) {
            return BetScope.MAP_4;
        }
        if (upper.contains("MAPA 5") || upper.contains("5TO MAPA") || upper.contains("5° MAPA") || upper.contains("5º MAPA") ||
            upper.contains("MAP 5") || upper.contains("5TH MAP") || upper.contains("GAME 5") || upper.contains("5TH GAME") ||
            upper.contains("QUINTO MAPA") || upper.contains("MAP 05") || upper.contains("MAPA 05")) {
            return BetScope.MAP_5;
        }
        if (upper.contains("MAPA 6") || upper.contains("6TO MAPA") || upper.contains("MAP 6") || upper.contains("6TH MAP")) {
            return BetScope.MAP_6;
        }
        if (upper.contains("MAPA 7") || upper.contains("7MO MAPA") || upper.contains("MAP 7") || upper.contains("7TH MAP")) {
            return BetScope.MAP_7;
        }

        // Rounds
        if (upper.contains("RONDA 1") || upper.contains("1RA RONDA") || upper.contains("ROUND 1") || upper.contains("1ST ROUND") ||
            upper.contains("1° ROUND") || upper.contains("1º ROUND")) {
            return BetScope.ROUND_1;
        }
        if (upper.contains("RONDA 2") || upper.contains("2DA RONDA") || upper.contains("ROUND 2") || upper.contains("2ND ROUND") ||
            upper.contains("2° ROUND") || upper.contains("2º ROUND")) {
            return BetScope.ROUND_2;
        }
        if (upper.contains("RONDA 3") || upper.contains("3RA RONDA") || upper.contains("ROUND 3") || upper.contains("3RD ROUND") ||
            upper.contains("3° ROUND") || upper.contains("3º ROUND")) {
            return BetScope.ROUND_3;
        }
        if (upper.contains("RONDA 4") || upper.contains("4TA RONDA") || upper.contains("ROUND 4") || upper.contains("4TH ROUND")) {
            return BetScope.ROUND_4;
        }
        if (upper.contains("RONDA 5") || upper.contains("5TA RONDA") || upper.contains("ROUND 5") || upper.contains("5TH ROUND")) {
            return BetScope.ROUND_5;
        }

        // Quarters
        if (upper.contains("1ER CUARTO") || upper.contains("PRIMER CUARTO") || upper.contains("1° CUARTO") || upper.contains("1º CUARTO") ||
            upper.contains("CUARTO 1") || upper.contains("QUARTER 1") || upper.contains("1ST QUARTER") || upper.matches(".*\\b1Q\\b.*")) {
            return BetScope.QUARTER_1;
        }
        if (upper.contains("2DO CUARTO") || upper.contains("SEGUNDO CUARTO") || upper.contains("2° CUARTO") || upper.contains("2º CUARTO") ||
            upper.contains("CUARTO 2") || upper.contains("QUARTER 2") || upper.contains("2ND QUARTER") || upper.matches(".*\\b2Q\\b.*")) {
            return BetScope.QUARTER_2;
        }
        if (upper.contains("3ER CUARTO") || upper.contains("TERCER CUARTO") || upper.contains("3° CUARTO") || upper.contains("3º CUARTO") ||
            upper.contains("CUARTO 3") || upper.contains("QUARTER 3") || upper.contains("3RD QUARTER") || upper.matches(".*\\b3Q\\b.*")) {
            return BetScope.QUARTER_3;
        }
        if (upper.contains("4TO CUARTO") || upper.contains("CUARTO CUARTO") || upper.contains("4° CUARTO") || upper.contains("4º CUARTO") ||
            upper.contains("CUARTO 4") || upper.contains("QUARTER 4") || upper.contains("4TH QUARTER") || upper.matches(".*\\b4Q\\b.*")) {
            return BetScope.QUARTER_4;
        }

        // Sets
        if (upper.contains("1ER SET") || upper.contains("PRIMER SET") || upper.contains("1° SET") || upper.contains("1º SET") ||
            upper.contains("SET 1") || upper.contains("1ST SET")) {
            return BetScope.SET_1;
        }
        if (upper.contains("2DO SET") || upper.contains("SEGUNDO SET") || upper.contains("2° SET") || upper.contains("2º SET") ||
            upper.contains("SET 2") || upper.contains("2ND SET")) {
            return BetScope.SET_2;
        }
        if (upper.contains("3ER SET") || upper.contains("TERCER SET") || upper.contains("3° SET") || upper.contains("3º SET") ||
            upper.contains("SET 3") || upper.contains("3RD SET")) {
            return BetScope.SET_3;
        }
        if (upper.contains("4TO SET") || upper.contains("SET 4") || upper.contains("4TH SET")) {
            return BetScope.SET_4;
        }
        if (upper.contains("5TO SET") || upper.contains("SET 5") || upper.contains("5TH SET")) {
            return BetScope.SET_5;
        }

        // Periods
        if (upper.contains("1ER PERIODO") || upper.contains("1ER PERÍODO") || upper.contains("PRIMER PERIODO") ||
            upper.contains("PERIOD 1") || upper.contains("1ST PERIOD") || upper.contains("PERIODO 1")) {
            return BetScope.PERIOD_1;
        }
        if (upper.contains("2DO PERIODO") || upper.contains("2DO PERÍODO") || upper.contains("SEGUNDO PERIODO") ||
            upper.contains("PERIOD 2") || upper.contains("2ND PERIOD") || upper.contains("PERIODO 2")) {
            return BetScope.PERIOD_2;
        }
        if (upper.contains("3ER PERIODO") || upper.contains("3ER PERÍODO") || upper.contains("TERCER PERIODO") ||
            upper.contains("PERIOD 3") || upper.contains("3RD PERIOD") || upper.contains("PERIODO 3")) {
            return BetScope.PERIOD_3;
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
