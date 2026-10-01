package pro.datawiki.igaming.source.apuestatotal.service.handler;

import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractApuestatotalStatsHandler extends AbstractApuestatotalMarketHandler {

    protected abstract StatType getStatType();

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern NUMERIC_PARAM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^(?i)(?:handicap\\s*[12]|фора\\s*[12]|hándicap\\s*[12]|total|тотал|over/under|o/u|más/menos|menos/más|mais/menos|over|under|больше|меньше|más|menos|[12])\\b\\s*");

    private static final Pattern TEAM1_PATTERN = Pattern.compile("(?i)(team\\s*1|home|local|casa|equipo\\s*1|time\\s*1|команда\\s*1|1-я\\s*команда|1-й\\s*команд|первой\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?1|ит\\s*1|ит1)");
    private static final Pattern TEAM2_PATTERN = Pattern.compile("(?i)(team\\s*2|away|visitante|visita|fora|equipo\\s*2|time\\s*2|команда\\s*2|2-я\\s*команда|2-й\\s*команд|второй\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?2|ит\\s*2|ит2)");

    @Override
    public void handle(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        StatType statType = getStatType();
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);
        String groupNameLower = groupName.toLowerCase();

        boolean isTotalMarket = isTotal(groupNameLower, group.getId());
        boolean isHandicapMarket = isHandicap(groupNameLower, group.getId());
        boolean isResultMarket = isResult(groupNameLower, group.getId(), isTotalMarket, isHandicapMarket);

        for (ApuestatotalStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) {
                continue;
            }

            if (isTotalMarket) {
                handleTotal(group, stake, match, scope, statType, items);
            } else if (isHandicapMarket) {
                handleHandicap(group, stake, match, scope, statType, items);
            } else if (isResultMarket) {
                handleResult(group, stake, match, scope, statType, items);
            } else {
                // Infer from stake name or structure
                if (isStakeTotal(stake)) {
                    handleTotal(group, stake, match, scope, statType, items);
                } else if (isStakeHandicap(stake)) {
                    handleHandicap(group, stake, match, scope, statType, items);
                } else {
                    handleResult(group, stake, match, scope, statType, items);
                }
            }
        }
    }

    protected boolean isTotal(String nameLower, Long id) {
        if (id != null && (id == 166L || id == 188L)) return true;
        return nameLower.contains("total") || nameLower.contains("тотал") || nameLower.contains("over/under")
                || nameLower.contains("más/menos") || nameLower.contains("menos/más") || nameLower.contains("mais/menos");
    }

    protected boolean isHandicap(String nameLower, Long id) {
        if (id != null && (id == 167L || id == 189L)) return true;
        return nameLower.contains("handicap") || nameLower.contains("фора") || nameLower.contains("hándicap")
                || nameLower.contains("spread") || nameLower.contains("desventaja");
    }

    protected boolean isResult(String nameLower, Long id, boolean isTotal, boolean isHandicap) {
        if (isTotal || isHandicap) return false;
        if (id != null && (id == 168L || id == 187L)) return true;
        return nameLower.contains("winner") || nameLower.contains("1x2") || nameLower.contains("исход")
                || nameLower.contains("победитель") || nameLower.contains("ganador") || nameLower.contains("resultado")
                || nameLower.contains("double chance") || nameLower.contains("двойной шанс") || nameLower.contains("doble oportunidad");
    }

    protected boolean isStakeTotal(ApuestatotalStakeData stake) {
        if (stake == null) return false;
        String name = ((stake.getNameEn() != null ? stake.getNameEn() : "") + " "
                + (stake.getNameRu() != null ? stake.getNameRu() : "")).toUpperCase();
        return name.contains("OVER") || name.contains("UNDER") || name.contains("БОЛЬШЕ") || name.contains("МЕНЬШЕ")
                || name.contains("MÁS") || name.contains("MAS") || name.contains("MENOS")
                || name.startsWith("ТБ") || name.startsWith("ТМ");
    }

    protected boolean isStakeHandicap(ApuestatotalStakeData stake) {
        if (stake == null) return false;
        String name = ((stake.getNameEn() != null ? stake.getNameEn() : "") + " "
                + (stake.getNameRu() != null ? stake.getNameRu() : "")).toUpperCase();
        return name.contains("HANDICAP") || name.contains("HÁNDICAP") || name.contains("ФОРА")
                || name.startsWith("H1") || name.startsWith("H2") || name.startsWith("Ф1") || name.startsWith("Ф2");
    }

    protected void handleTotal(ApuestatotalStakeGroupData group, ApuestatotalStakeData stake, MatchCache match,
                               BetScope scope, StatType statType, List<OddItem> items) {
        Double param = resolveTotalParam(stake, group);
        if (param == null || param < 0) return;

        TotalBet.Direction direction = resolveTotalDirection(stake);
        if (direction == null) return;

        BetSubject subject = resolveTotalSubject(group, stake, match);
        boolean isAsian = isQuarterAsian(param);

        TotalBet betType = new TotalBet(scope, subject, direction, param, isAsian, statType);
        addOddItem(items, stake, getGroupName(group), betType, param);
    }

    protected void handleHandicap(ApuestatotalStakeGroupData group, ApuestatotalStakeData stake, MatchCache match,
                                  BetScope scope, StatType statType, List<OddItem> items) {
        Double param = resolveHandicapParam(stake, group);
        if (param == null) return;

        HandicapBet.Outcome outcome = resolveHandicapOutcome(stake, match);
        if (outcome == null) return;

        boolean isAsian = isQuarterAsian(param);
        HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, statType);
        addOddItem(items, stake, getGroupName(group), betType, param);
    }

    protected void handleResult(ApuestatotalStakeGroupData group, ApuestatotalStakeData stake, MatchCache match,
                                BetScope scope, StatType statType, List<OddItem> items) {
        MatchResultBet.Outcome dcOutcome = mapDoubleChanceOutcome(stake);
        if (dcOutcome != null) {
            MatchResultBet betType = new MatchResultBet(scope, dcOutcome, statType);
            addOddItem(items, stake, getGroupName(group), betType, null);
            return;
        }

        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);
        BetType betType = null;

        if (isTeam1ResultStake(stake, match)) {
            MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            betType = new MatchResultBet(scope, outcome, statType);
        } else if (isDrawStake(stake)) {
            betType = new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, statType);
        } else if (isTeam2ResultStake(stake, match)) {
            MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            betType = new MatchResultBet(scope, outcome, statType);
        }

        if (betType != null) {
            addOddItem(items, stake, getGroupName(group), betType, null);
        }
    }

    protected TotalBet.Direction resolveTotalDirection(ApuestatotalStakeData stake) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim().toUpperCase() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim().toUpperCase() : "";

        if (en.equals("OVER") || ru.equals("БОЛЬШЕ") || ru.equals("Б") || en.equals("O")
                || en.equals("MÁS") || en.equals("MAS") || en.equals("MAIS")
                || en.startsWith("OVER") || ru.startsWith("БОЛЬШЕ") || ru.startsWith("ТБ")
                || en.startsWith("MÁS DE") || en.startsWith("MAS DE")
                || en.contains("OVER") || ru.contains("БОЛЬШЕ") || en.contains("MÁS") || en.contains("MAS")) {
            return TotalBet.Direction.OVER;
        }

        if (en.equals("UNDER") || ru.equals("МЕНЬШЕ") || ru.equals("М") || en.equals("U")
                || en.equals("MENOS")
                || en.startsWith("UNDER") || ru.startsWith("МЕНЬШЕ") || ru.startsWith("ТМ")
                || en.startsWith("MENOS DE")
                || en.contains("UNDER") || ru.contains("МЕНЬШЕ") || en.contains("MENOS")) {
            return TotalBet.Direction.UNDER;
        }

        return null;
    }

    protected HandicapBet.Outcome resolveHandicapOutcome(ApuestatotalStakeData stake, MatchCache match) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
        String upperEn = en.toUpperCase();
        String upperRu = ru.toUpperCase();

        if (upperEn.equals("X") || upperRu.equals("X") || upperRu.equals("Х")
                || upperEn.equals("DRAW") || upperRu.equals("НИЧЬЯ") || upperEn.equals("EMPATE")
                || upperEn.startsWith("DRAW") || upperRu.startsWith("НИЧЬЯ") || upperEn.startsWith("EMPATE")) {
            return HandicapBet.Outcome.DRAW;
        }

        if (upperEn.equals("1") || upperEn.equals("H1") || upperRu.equals("Ф1")
                || upperEn.equals("HANDICAP 1") || upperEn.equals("HÁNDICAP 1") || upperEn.equals("HOME") || upperEn.equals("LOCAL") || upperEn.equals("CASA")
                || upperEn.startsWith("1 ") || upperRu.startsWith("1 ")
                || upperEn.startsWith("H1 ") || upperRu.startsWith("Ф1 ") || upperRu.startsWith("Ф1(")) {
            return HandicapBet.Outcome.TEAM1;
        }

        if (upperEn.equals("2") || upperEn.equals("H2") || upperRu.equals("Ф2")
                || upperEn.equals("HANDICAP 2") || upperEn.equals("HÁNDICAP 2") || upperEn.equals("AWAY") || upperEn.equals("VISITANTE") || upperEn.equals("VISITA") || upperEn.equals("FORA")
                || upperEn.startsWith("2 ") || upperRu.startsWith("2 ")
                || upperEn.startsWith("H2 ") || upperRu.startsWith("Ф2 ") || upperRu.startsWith("Ф2(")) {
            return HandicapBet.Outcome.TEAM2;
        }

        if (match != null) {
            if (match.getTeam1() != null && !match.getTeam1().isBlank()) {
                String t1 = match.getTeam1().trim();
                if (en.equalsIgnoreCase(t1) || ru.equalsIgnoreCase(t1)) return HandicapBet.Outcome.TEAM1;
            }
            if (match.getTeam2() != null && !match.getTeam2().isBlank()) {
                String t2 = match.getTeam2().trim();
                if (en.equalsIgnoreCase(t2) || ru.equalsIgnoreCase(t2)) return HandicapBet.Outcome.TEAM2;
            }
        }
        return null;
    }

    protected MatchResultBet.Outcome mapDoubleChanceOutcome(ApuestatotalStakeData stake) {
        if (stake == null) return null;
        String combined = ((stake.getNameEn() != null ? stake.getNameEn() : "") + " "
                + (stake.getNameRu() != null ? stake.getNameRu() : "")).toUpperCase();

        if (combined.contains("1X") || combined.contains("1-X") || combined.contains("1Х") || combined.contains("1 O X") || combined.contains("1 O EMPATE")) {
            return MatchResultBet.Outcome.DC_1X;
        }
        if (combined.contains("12") || combined.contains("1-2") || combined.contains("1 O 2")) {
            return MatchResultBet.Outcome.DC_12;
        }
        if (combined.contains("X2") || combined.contains("X-2") || combined.contains("2X") || combined.contains("Х2") || combined.contains("X O 2") || combined.contains("EMPATE O 2")) {
            return MatchResultBet.Outcome.DC_X2;
        }
        return null;
    }

    protected boolean isDrawStake(ApuestatotalStakeData s) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim().toUpperCase() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim().toUpperCase() : "";
        return "X".equals(en) || "X".equals(ru) || "Х".equals(en) || "Х".equals(ru)
                || "DRAW".equals(en) || "НИЧЬЯ".equals(ru) || "EMPATE".equals(en)
                || en.contains("DRAW") || ru.contains("НИЧЬЯ") || en.contains("EMPATE");
    }

    protected boolean isTeam1ResultStake(ApuestatotalStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("1".equalsIgnoreCase(en) || "Win1".equalsIgnoreCase(en) || "W1".equalsIgnoreCase(en)
                || "П1".equalsIgnoreCase(ru) || "P1".equalsIgnoreCase(en) || "Home".equalsIgnoreCase(en)
                || "Local".equalsIgnoreCase(en) || "Casa".equalsIgnoreCase(en)
                || "Победа 1".equalsIgnoreCase(ru) || "Победа1".equalsIgnoreCase(ru)
                || "Team 1".equalsIgnoreCase(en) || "Equipo 1".equalsIgnoreCase(en) || "Команда 1".equalsIgnoreCase(ru)) {
            return true;
        }
        if (match != null && match.getTeam1() != null && !match.getTeam1().isBlank()) {
            String t1 = match.getTeam1().trim();
            if (en.equalsIgnoreCase(t1) || ru.equalsIgnoreCase(t1)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isTeam2ResultStake(ApuestatotalStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("2".equalsIgnoreCase(en) || "Win2".equalsIgnoreCase(en) || "W2".equalsIgnoreCase(en)
                || "П2".equalsIgnoreCase(ru) || "P2".equalsIgnoreCase(en) || "Away".equalsIgnoreCase(en)
                || "Visitante".equalsIgnoreCase(en) || "Visita".equalsIgnoreCase(en) || "Fora".equalsIgnoreCase(en)
                || "Победа 2".equalsIgnoreCase(ru) || "Победа2".equalsIgnoreCase(ru)
                || "Team 2".equalsIgnoreCase(en) || "Equipo 2".equalsIgnoreCase(en) || "Команда 2".equalsIgnoreCase(ru)) {
            return true;
        }
        if (match != null && match.getTeam2() != null && !match.getTeam2().isBlank()) {
            String t2 = match.getTeam2().trim();
            if (en.equalsIgnoreCase(t2) || ru.equalsIgnoreCase(t2)) {
                return true;
            }
        }
        return false;
    }

    protected Double resolveTotalParam(ApuestatotalStakeData stake, ApuestatotalStakeGroupData group) {
        if (stake.getArgument() != null) {
            return stake.getArgument();
        }
        Double parsed = extractNumericParam(stake.getNameEn());
        if (parsed == null) {
            parsed = extractNumericParam(stake.getNameRu());
        }
        if (parsed == null) {
            parsed = extractNumericParam(getGroupName(group));
        }
        return parsed;
    }

    protected Double resolveHandicapParam(ApuestatotalStakeData stake, ApuestatotalStakeGroupData group) {
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

    protected BetSubject resolveTotalSubject(ApuestatotalStakeGroupData group, ApuestatotalStakeData stake, MatchCache match) {
        String stakeEn = stake.getNameEn() != null ? stake.getNameEn() : "";
        String stakeRu = stake.getNameRu() != null ? stake.getNameRu() : "";
        String stakeCombined = stakeEn + " " + stakeRu;

        boolean stakeT1 = TEAM1_PATTERN.matcher(stakeCombined).find();
        boolean stakeT2 = TEAM2_PATTERN.matcher(stakeCombined).find();

        if (stakeT1 && !stakeT2) {
            return BetSubject.TEAM1;
        }
        if (stakeT2 && !stakeT1) {
            return BetSubject.TEAM2;
        }

        String groupName = getGroupName(group);
        boolean groupT1 = TEAM1_PATTERN.matcher(groupName).find();
        boolean groupT2 = TEAM2_PATTERN.matcher(groupName).find();

        if (groupT1 && !groupT2) {
            return BetSubject.TEAM1;
        }
        if (groupT2 && !groupT1) {
            return BetSubject.TEAM2;
        }

        if (match != null) {
            String t1 = match.getTeam1() != null ? match.getTeam1().trim().toLowerCase() : "";
            String t2 = match.getTeam2() != null ? match.getTeam2().trim().toLowerCase() : "";

            String lowerGroup = groupName.toLowerCase();
            String lowerStake = stakeCombined.toLowerCase();

            boolean matchesT1 = !t1.isEmpty() && (lowerGroup.contains(t1) || lowerStake.contains(t1));
            boolean matchesT2 = !t2.isEmpty() && (lowerGroup.contains(t2) || lowerStake.contains(t2));

            if (matchesT1 && !matchesT2) {
                return BetSubject.TEAM1;
            }
            if (matchesT2 && !matchesT1) {
                return BetSubject.TEAM2;
            }
        }

        return BetSubject.MATCH;
    }

    protected Double extractNumericParam(String text) {
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

    protected Double extractSignedNumericParam(String text) {
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

    protected boolean isQuarterAsian(Double param) {
        if (param == null) return false;
        return Math.abs(param * 4 - Math.round(param * 4)) < 0.001
                && Math.abs(param * 2 - Math.round(param * 2)) > 0.001;
    }
}
