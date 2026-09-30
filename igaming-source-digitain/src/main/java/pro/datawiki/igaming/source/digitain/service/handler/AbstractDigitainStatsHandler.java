package pro.datawiki.igaming.source.digitain.service.handler;

import lombok.extern.slf4j.Slf4j;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractDigitainStatsHandler extends AbstractDigitainMarketHandler {

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern NUMERIC_PARAM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^(?i)(?:handicap\\s*[12]|фора\\s*[12]|team\\s*[12]|команда\\s*[12]|h[12]|ф[12]|[12])\\b\\s*");
    private static final Pattern TEAM1_PATTERN = Pattern.compile("(?i)(team\\s*1|home|команда\\s*1|1-я\\s*команда|1-й\\s*команд|первой\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?1|ит\\s*1|ит1)");
    private static final Pattern TEAM2_PATTERN = Pattern.compile("(?i)(team\\s*2|away|команда\\s*2|2-я\\s*команда|2-й\\s*команд|второй\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?2|ит\\s*2|ит2)");

    protected abstract StatType getStatType();

    @Override
    public void handle(DigitainStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        String lowerName = getGroupName(group).toLowerCase();

        if (isHandicapMarket(group, lowerName)) {
            handleHandicap(group, match, sportType, items);
        } else if (isTotalMarket(group, lowerName)) {
            handleTotal(group, match, sportType, items);
        } else if (isResultMarket(group, lowerName)) {
            handleResult(group, match, sportType, items);
        } else {
            handleMixed(group, match, sportType, items);
        }
    }

    protected boolean isHandicapMarket(DigitainStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && id == 167L) {
            return true;
        }
        if (lowerName.contains("handicap") || lowerName.contains("фора") || lowerName.contains("spread")) {
            return true;
        }
        return isHandicapGroup(group);
    }

    protected boolean isTotalMarket(DigitainStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && id == 166L) {
            return true;
        }
        if (lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("over/under") || lowerName.contains("o/u")) {
            return true;
        }
        return isTotalGroup(group);
    }

    protected boolean isResultMarket(DigitainStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && id == 168L) {
            return true;
        }
        if (lowerName.contains("1x2") || lowerName.contains("result") || lowerName.contains("winner")
                || lowerName.contains("исход") || lowerName.contains("победител") || lowerName.contains("победа")
                || lowerName.contains("double chance") || lowerName.contains("двойной шанс")) {
            return true;
        }
        return isResultGroup(group);
    }

    private boolean isHandicapGroup(DigitainStakeGroupData group) {
        if (group == null || group.getStakes() == null) return false;
        return group.getStakes().stream().anyMatch(s -> resolveHandicapOutcome(s, null) != null && s.getArgument() != null);
    }

    private boolean isTotalGroup(DigitainStakeGroupData group) {
        if (group == null || group.getStakes() == null) return false;
        return group.getStakes().stream().anyMatch(s -> resolveTotalDirection(s) != null);
    }

    private boolean isResultGroup(DigitainStakeGroupData group) {
        if (group == null || group.getStakes() == null) return false;
        return group.getStakes().stream().anyMatch(s -> isTeam1ResultStake(s, null) || isDrawStake(s) || isTeam2ResultStake(s, null) || resolveDoubleChanceOutcome(s) != null);
    }

    protected void handleHandicap(DigitainStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double param = resolveHandicapParam(stake, group);
            if (param == null) continue;

            HandicapBet.Outcome outcome = resolveHandicapOutcome(stake, match);
            if (outcome == null) continue;

            boolean isAsian = isQuarterAsian(param);
            HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, getStatType());
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    protected void handleTotal(DigitainStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double param = resolveTotalParam(stake, group);
            if (param == null || param < 0) continue;

            TotalBet.Direction direction = resolveTotalDirection(stake);
            if (direction == null) continue;

            BetSubject subject = resolveTotalSubject(group, stake, match);
            boolean isAsian = isQuarterAsian(param);
            TotalBet betType = new TotalBet(scope, subject, direction, param, isAsian, getStatType());
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    protected void handleResult(DigitainStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);
        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            BetType betType = null;
            MatchResultBet.Outcome dcOutcome = resolveDoubleChanceOutcome(stake);
            if (dcOutcome != null) {
                betType = new MatchResultBet(scope, dcOutcome, getStatType());
            } else if (isTeam1ResultStake(stake, match)) {
                MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                betType = new MatchResultBet(scope, outcome, getStatType());
            } else if (isDrawStake(stake)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, getStatType());
            } else if (isTeam2ResultStake(stake, match)) {
                MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                betType = new MatchResultBet(scope, outcome, getStatType());
            }

            if (betType != null) {
                addOddItem(items, stake, groupName, betType, null);
            }
        }
    }

    protected void handleMixed(DigitainStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);
        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            // Try Total
            TotalBet.Direction dir = resolveTotalDirection(stake);
            if (dir != null) {
                Double param = resolveTotalParam(stake, group);
                if (param != null && param >= 0) {
                    BetSubject subject = resolveTotalSubject(group, stake, match);
                    boolean isAsian = isQuarterAsian(param);
                    TotalBet betType = new TotalBet(scope, subject, dir, param, isAsian, getStatType());
                    addOddItem(items, stake, groupName, betType, param);
                    continue;
                }
            }

            // Try Handicap
            HandicapBet.Outcome hcOutcome = resolveHandicapOutcome(stake, match);
            if (hcOutcome != null) {
                Double param = resolveHandicapParam(stake, group);
                if (param != null) {
                    boolean isAsian = isQuarterAsian(param);
                    HandicapBet betType = new HandicapBet(scope, hcOutcome, param, isAsian, getStatType());
                    addOddItem(items, stake, groupName, betType, param);
                    continue;
                }
            }

            // Try Double Chance
            MatchResultBet.Outcome dcOutcome = resolveDoubleChanceOutcome(stake);
            if (dcOutcome != null) {
                MatchResultBet betType = new MatchResultBet(scope, dcOutcome, getStatType());
                addOddItem(items, stake, groupName, betType, null);
                continue;
            }

            // Try 1X2
            if (isTeam1ResultStake(stake, match)) {
                MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                addOddItem(items, stake, groupName, new MatchResultBet(scope, outcome, getStatType()), null);
            } else if (isDrawStake(stake)) {
                addOddItem(items, stake, groupName, new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, getStatType()), null);
            } else if (isTeam2ResultStake(stake, match)) {
                MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                addOddItem(items, stake, groupName, new MatchResultBet(scope, outcome, getStatType()), null);
            }
        }
    }

    protected TotalBet.Direction resolveTotalDirection(DigitainStakeData stake) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim().toUpperCase() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim().toUpperCase() : "";

        if (en.equals("OVER") || ru.equals("БОЛЬШЕ") || ru.equals("Б") || en.equals("O")
                || en.startsWith("OVER") || ru.startsWith("БОЛЬШЕ") || ru.startsWith("ТБ")
                || en.contains("OVER") || ru.contains("БОЛЬШЕ")) {
            return TotalBet.Direction.OVER;
        }

        if (en.equals("UNDER") || ru.equals("МЕНЬШЕ") || ru.equals("М") || en.equals("U")
                || en.startsWith("UNDER") || ru.startsWith("МЕНЬШЕ") || ru.startsWith("ТМ")
                || en.contains("UNDER") || ru.contains("МЕНЬШЕ")) {
            return TotalBet.Direction.UNDER;
        }

        if (en.equals("EXACT") || ru.equals("РОВНО") || en.startsWith("EXACT") || ru.startsWith("РОВНО")) {
            return TotalBet.Direction.EXACT;
        }

        return null;
    }

    protected HandicapBet.Outcome resolveHandicapOutcome(DigitainStakeData stake, MatchCache match) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
        String upperEn = en.toUpperCase();
        String upperRu = ru.toUpperCase();

        if (upperEn.equals("X") || upperRu.equals("X") || upperRu.equals("Х")
                || upperEn.equals("DRAW") || upperRu.equals("НИЧЬЯ")
                || upperEn.startsWith("DRAW") || upperRu.startsWith("НИЧЬЯ")
                || upperRu.startsWith("ФОРА Х") || upperRu.startsWith("ФОРА X")) {
            return HandicapBet.Outcome.DRAW;
        }

        if (upperEn.equals("1") || upperEn.equals("H1") || upperRu.equals("Ф1") || upperRu.equals("ФОРА 1") || upperRu.equals("ФОРА1")
                || upperEn.equals("HANDICAP 1") || upperEn.equals("HOME")
                || upperEn.startsWith("1 ") || upperRu.startsWith("1 ")
                || upperEn.startsWith("H1 ") || upperRu.startsWith("Ф1 ") || upperRu.startsWith("Ф1(")
                || upperEn.startsWith("HANDICAP 1") || upperRu.startsWith("ФОРА 1") || upperRu.startsWith("ФОРА1")) {
            return HandicapBet.Outcome.TEAM1;
        }

        if (upperEn.equals("2") || upperEn.equals("H2") || upperRu.equals("Ф2") || upperRu.equals("ФОРА 2") || upperRu.equals("ФОРА2")
                || upperEn.equals("HANDICAP 2") || upperEn.equals("AWAY")
                || upperEn.startsWith("2 ") || upperRu.startsWith("2 ")
                || upperEn.startsWith("H2 ") || upperRu.startsWith("Ф2 ") || upperRu.startsWith("Ф2(")
                || upperEn.startsWith("HANDICAP 2") || upperRu.startsWith("ФОРА 2") || upperRu.startsWith("ФОРА2")) {
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

    protected MatchResultBet.Outcome resolveDoubleChanceOutcome(DigitainStakeData stake) {
        if (stake == null) return null;
        String combined = ((stake.getNameEn() != null ? stake.getNameEn() : "") + " "
                + (stake.getNameRu() != null ? stake.getNameRu() : "")).toUpperCase();

        if (combined.contains("1X") || combined.contains("1-X") || combined.contains("1Х")) {
            return MatchResultBet.Outcome.DC_1X;
        }
        if (combined.contains("12") || combined.contains("1-2")) {
            return MatchResultBet.Outcome.DC_12;
        }
        if (combined.contains("X2") || combined.contains("X-2") || combined.contains("2X") || combined.contains("Х2")) {
            return MatchResultBet.Outcome.DC_X2;
        }
        return null;
    }

    protected boolean isDrawStake(DigitainStakeData s) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim().toUpperCase() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim().toUpperCase() : "";
        return "X".equals(en) || "X".equals(ru) || "Х".equals(en) || "Х".equals(ru)
                || "DRAW".equals(en) || "НИЧЬЯ".equals(ru)
                || en.contains("DRAW") || ru.contains("НИЧЬЯ");
    }

    protected boolean isTeam1ResultStake(DigitainStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("1".equalsIgnoreCase(en) || "Win1".equalsIgnoreCase(en) || "W1".equalsIgnoreCase(en)
                || "П1".equalsIgnoreCase(ru) || "P1".equalsIgnoreCase(en) || "Home".equalsIgnoreCase(en)
                || "Победа 1".equalsIgnoreCase(ru) || "Победа1".equalsIgnoreCase(ru)
                || "Team 1".equalsIgnoreCase(en) || "Команда 1".equalsIgnoreCase(ru)) {
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

    protected boolean isTeam2ResultStake(DigitainStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("2".equalsIgnoreCase(en) || "Win2".equalsIgnoreCase(en) || "W2".equalsIgnoreCase(en)
                || "П2".equalsIgnoreCase(ru) || "P2".equalsIgnoreCase(en) || "Away".equalsIgnoreCase(en)
                || "Победа 2".equalsIgnoreCase(ru) || "Победа2".equalsIgnoreCase(ru)
                || "Team 2".equalsIgnoreCase(en) || "Команда 2".equalsIgnoreCase(ru)) {
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

    protected Double resolveTotalParam(DigitainStakeData stake, DigitainStakeGroupData group) {
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

    protected Double resolveHandicapParam(DigitainStakeData stake, DigitainStakeGroupData group) {
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

    protected BetSubject resolveTotalSubject(DigitainStakeGroupData group, DigitainStakeData stake, MatchCache match) {
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
