package pro.datawiki.igaming.source.apuestatotal.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(70)
public class ApuestatotalHandicapHandler extends AbstractApuestatotalMarketHandler {

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern NUMERIC_PARAM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^(?i)(?:handicap\\s*[12]|фора\\s*[12]|hándicap\\s*[12]|h[12]|ф[12]|[12])\\b\\s*");

    @Override
    public boolean supports(ApuestatotalStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }
        Long id = group.getId();
        String name = getGroupName(group).toLowerCase();

        // Exclude markets handled by specialized handlers
        if (name.contains("corner") || name.contains("углов") || name.contains("córner") || name.contains("esquina")
                || name.contains("card") || name.contains("карточ") || name.contains("tarjeta") || name.contains("booking")
                || name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ") || name.contains("mapa ")
                || name.contains("total") || name.contains("тотал")
                || name.contains("match result") || name.contains("1x2")
                || name.contains("double chance") || name.contains("двойной шанс") || name.contains("doble oportunidad")
                || name.contains("both teams") || name.contains("обе забьют") || name.contains("ambos equipos anotan") || name.contains("ambos anotan")
                || name.contains("correct score") || name.contains("точный счет") || name.contains("marcador exacto")) {
            return false;
        }

        // Known handicap group IDs: 2 (Full Match Handicap), 5 (1st Half Handicap), 8 (2nd Half Handicap)
        if (id != null && (id == 2L || id == 5L || id == 8L)) {
            return true;
        }

        return name.contains("handicap") || name.contains("фора") || name.contains("hándicap") || name.contains("spread") || name.contains("desventaja");
    }

    @Override
    public void handle(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (ApuestatotalStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) {
                continue;
            }

            Double param = resolveHandicapParam(stake, group);
            if (param == null) {
                continue;
            }

            HandicapBet.Outcome outcome = resolveOutcome(stake, match);
            if (outcome == null) {
                continue;
            }

            boolean isAsian = isQuarterAsian(param);
            HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, StatType.MATCH);
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    private HandicapBet.Outcome resolveOutcome(ApuestatotalStakeData stake, MatchCache match) {
        String en = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
        String upperEn = en.toUpperCase();
        String upperRu = ru.toUpperCase();

        // 3-way handicap draw
        if (upperEn.equals("X") || upperRu.equals("X") || upperRu.equals("Х")
                || upperEn.equals("DRAW") || upperRu.equals("НИЧЬЯ") || upperEn.equals("EMPATE")
                || upperEn.startsWith("DRAW") || upperRu.startsWith("НИЧЬЯ") || upperEn.startsWith("EMPATE")
                || upperRu.startsWith("ФОРА Х") || upperRu.startsWith("ФОРА X")) {
            return HandicapBet.Outcome.DRAW;
        }

        // Team 1 handicap
        if (upperEn.equals("1") || upperEn.equals("H1") || upperRu.equals("Ф1") || upperRu.equals("ФОРА 1") || upperRu.equals("ФОРА1")
                || upperEn.equals("HANDICAP 1") || upperEn.equals("HÁNDICAP 1") || upperEn.equals("HOME") || upperEn.equals("LOCAL") || upperEn.equals("CASA")
                || upperEn.startsWith("1 ") || upperRu.startsWith("1 ")
                || upperEn.startsWith("H1 ") || upperRu.startsWith("Ф1 ") || upperRu.startsWith("Ф1(")
                || upperEn.startsWith("HANDICAP 1") || upperEn.startsWith("HÁNDICAP 1") || upperRu.startsWith("ФОРА 1") || upperRu.startsWith("ФОРА1")
                || upperEn.startsWith("HOME") || upperEn.startsWith("LOCAL") || upperEn.startsWith("CASA")) {
            return HandicapBet.Outcome.TEAM1;
        }

        // Team 2 handicap
        if (upperEn.equals("2") || upperEn.equals("H2") || upperRu.equals("Ф2") || upperRu.equals("ФОРА 2") || upperRu.equals("ФОРА2")
                || upperEn.equals("HANDICAP 2") || upperEn.equals("HÁNDICAP 2") || upperEn.equals("AWAY") || upperEn.equals("VISITANTE") || upperEn.equals("VISITA") || upperEn.equals("FORA")
                || upperEn.startsWith("2 ") || upperRu.startsWith("2 ")
                || upperEn.startsWith("H2 ") || upperRu.startsWith("Ф2 ") || upperRu.startsWith("Ф2(")
                || upperEn.startsWith("HANDICAP 2") || upperEn.startsWith("HÁNDICAP 2") || upperRu.startsWith("ФОРА 2") || upperRu.startsWith("ФОРА2")
                || upperEn.startsWith("AWAY") || upperEn.startsWith("VISITANTE") || upperEn.startsWith("VISITA") || upperEn.startsWith("FORA")) {
            return HandicapBet.Outcome.TEAM2;
        }

        if (match != null) {
            if (match.getTeam1() != null && !match.getTeam1().isBlank()) {
                String t1 = match.getTeam1().trim();
                if (en.equalsIgnoreCase(t1) || ru.equalsIgnoreCase(t1)
                        || upperEn.startsWith(t1.toUpperCase()) || upperRu.startsWith(t1.toUpperCase())) {
                    return HandicapBet.Outcome.TEAM1;
                }
            }
            if (match.getTeam2() != null && !match.getTeam2().isBlank()) {
                String t2 = match.getTeam2().trim();
                if (en.equalsIgnoreCase(t2) || ru.equalsIgnoreCase(t2)
                        || upperEn.startsWith(t2.toUpperCase()) || upperRu.startsWith(t2.toUpperCase())) {
                    return HandicapBet.Outcome.TEAM2;
                }
            }
        }

        return null;
    }

    private Double resolveHandicapParam(ApuestatotalStakeData stake, ApuestatotalStakeGroupData group) {
        Double param = stake.getArgument();
        String name = ((stake.getNameEn() != null ? stake.getNameEn() : "") + " "
                + (stake.getNameRu() != null ? stake.getNameRu() : "")).trim();

        Double parsedFromName = extractSignedNumericParam(name);
        if (parsedFromName != null) {
            if (param != null && param > 0 && parsedFromName < 0 && Math.abs(param - Math.abs(parsedFromName)) < 0.001) {
                return parsedFromName;
            }
            if (param == null) {
                return parsedFromName;
            }
        }
        if (param == null) {
            return extractSignedNumericParam(getGroupName(group));
        }
        return param;
    }

    private Double extractSignedNumericParam(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher parenMatcher = PAREN_PARAM_PATTERN.matcher(text);
        if (parenMatcher.find()) {
            try {
                return Double.parseDouble(parenMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        String stripped = PREFIX_PATTERN.matcher(text).replaceFirst("");
        Matcher numMatcher = NUMERIC_PARAM_PATTERN.matcher(stripped);
        if (numMatcher.find()) {
            try {
                return Double.parseDouble(numMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private boolean isQuarterAsian(Double param) {
        if (param == null) return false;
        return Math.abs(param * 4 - Math.round(param * 4)) < 0.001
                && Math.abs(param * 2 - Math.round(param * 2)) > 0.001;
    }
}
