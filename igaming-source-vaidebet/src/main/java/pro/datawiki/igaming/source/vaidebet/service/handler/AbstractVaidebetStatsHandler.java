package pro.datawiki.igaming.source.vaidebet.service.handler;

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
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeData;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeGroupData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractVaidebetStatsHandler extends AbstractVaidebetMarketHandler {

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern NUMERIC_PARAM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^(?i)(?:handicap\\s*[12]|фора\\s*[12]|team\\s*[12]|команда\\s*[12]|time\\s*[12]|mandante|visitante|home|away|casa|fora|h[12]|ф[12]|[12])\\b\\s*");
    private static final Pattern TEAM1_PATTERN = Pattern.compile("(?i)(team\\s*1|home|casa|mandante|time\\s*1|equipe\\s*1|команда\\s*1|1-я\\s*команда|1-й\\s*команд|первой\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?1|ит\\s*1|ит1)");
    private static final Pattern TEAM2_PATTERN = Pattern.compile("(?i)(team\\s*2|away|fora|visitante|time\\s*2|equipe\\s*2|команда\\s*2|2-я\\s*команда|2-й\\s*команд|второй\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?2|ит\\s*2|ит2)");

    protected abstract StatType getStatType();

    @Override
    public void handle(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
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

    protected boolean isHandicapMarket(VaidebetStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && (id == 167L || id == 189L)) {
            return true;
        }
        if (lowerName.contains("handicap") || lowerName.contains("фора") || lowerName.contains("spread") || lowerName.contains("desvantagem")) {
            return true;
        }
        return isHandicapGroup(group);
    }

    protected boolean isTotalMarket(VaidebetStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && (id == 166L || id == 188L)) {
            return true;
        }
        if (lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("over/under") || lowerName.contains("o/u")
                || lowerName.contains("mais/menos") || lowerName.contains("acima/abaixo") || lowerName.contains("mais de") || lowerName.contains("menos de")) {
            return true;
        }
        return isTotalGroup(group);
    }

    protected boolean isResultMarket(VaidebetStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && (id == 168L || id == 187L)) {
            return true;
        }
        if (lowerName.contains("1x2") || lowerName.contains("result") || lowerName.contains("winner")
                || lowerName.contains("исход") || lowerName.contains("победител") || lowerName.contains("победа")
                || lowerName.contains("vencedor") || lowerName.contains("resultado final")
                || lowerName.contains("double chance") || lowerName.contains("двойной шанс")
                || lowerName.contains("dupla chance") || lowerName.contains("dupla hipótese")) {
            return true;
        }
        return isResultGroup(group);
    }

    private boolean isHandicapGroup(VaidebetStakeGroupData group) {
        if (group == null || group.getStakes() == null) return false;
        return group.getStakes().stream().anyMatch(s -> resolveHandicapOutcome(s, null) != null && s.getArgument() != null);
    }

    private boolean isTotalGroup(VaidebetStakeGroupData group) {
        if (group == null || group.getStakes() == null) return false;
        return group.getStakes().stream().anyMatch(s -> resolveTotalDirection(s) != null);
    }

    private boolean isResultGroup(VaidebetStakeGroupData group) {
        if (group == null || group.getStakes() == null) return false;
        return group.getStakes().stream().anyMatch(s -> isTeam1ResultStake(s, null) || isDrawStake(s) || isTeam2ResultStake(s, null) || resolveDoubleChanceOutcome(s) != null);
    }

    protected void handleHandicap(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
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

    protected void handleTotal(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
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

    protected void handleResult(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);
        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);

        for (VaidebetStakeData stake : group.getStakes()) {
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

    protected void handleMixed(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);
        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);

        for (VaidebetStakeData stake : group.getStakes()) {
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

    protected TotalBet.Direction resolveTotalDirection(VaidebetStakeData stake) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim().toUpperCase() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim().toUpperCase() : "";

        // Check OVER
        if (en.equals("OVER") || ru.equals("БОЛЬШЕ") || ru.equals("Б") || en.equals("O") || en.equals("MAIS")
                || en.equals("ACIMA") || ru.equals("ACIMA") || en.equals("MAIS DE") || en.equals("ACIMA DE")
                || en.startsWith("OVER") || ru.startsWith("БОЛЬШЕ") || ru.startsWith("ТБ") || en.startsWith("MAIS DE")
                || en.startsWith("ACIMA") || ru.startsWith("ACIMA") || en.startsWith("MAIS")
                || en.contains("OVER") || ru.contains("БОЛЬШЕ") || en.contains("MAIS") || en.contains("ACIMA") || ru.contains("ACIMA")) {
            return TotalBet.Direction.OVER;
        }

        // Check UNDER
        if (en.equals("UNDER") || ru.equals("МЕНЬШЕ") || ru.equals("М") || en.equals("U") || en.equals("MENOS")
                || en.equals("ABAIXO") || ru.equals("ABAIXO") || en.equals("MENOS DE") || en.equals("ABAIXO DE")
                || en.startsWith("UNDER") || ru.startsWith("МЕНЬШЕ") || ru.startsWith("ТМ") || en.startsWith("MENOS DE")
                || en.startsWith("ABAIXO") || ru.startsWith("ABAIXO") || en.startsWith("MENOS")
                || en.contains("UNDER") || ru.contains("МЕНЬШЕ") || en.contains("MENOS") || en.contains("ABAIXO") || ru.contains("ABAIXO")) {
            return TotalBet.Direction.UNDER;
        }

        // Check EXACT
        if (en.equals("EXACT") || ru.equals("РОВНО") || en.startsWith("EXACT") || ru.startsWith("РОВНО")
                || en.equals("EXATO") || en.startsWith("EXATO")) {
            return TotalBet.Direction.EXACT;
        }

        return null;
    }

    protected HandicapBet.Outcome resolveHandicapOutcome(VaidebetStakeData stake, MatchCache match) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
        String upperEn = en.toUpperCase();
        String upperRu = ru.toUpperCase();

        if (upperEn.equals("X") || upperRu.equals("X") || upperRu.equals("Х")
                || upperEn.equals("DRAW") || upperRu.equals("НИЧЬЯ") || upperEn.equals("EMPATE") || upperRu.equals("EMPATE")
                || upperEn.startsWith("DRAW") || upperRu.startsWith("НИЧЬЯ") || upperEn.startsWith("EMPATE") || upperRu.startsWith("EMPATE")
                || upperRu.startsWith("ФОРА Х") || upperRu.startsWith("ФОРА X")) {
            return HandicapBet.Outcome.DRAW;
        }

        if (upperEn.equals("1") || upperEn.equals("H1") || upperRu.equals("Ф1") || upperRu.equals("ФОРА 1") || upperRu.equals("ФОРА1")
                || upperEn.equals("HANDICAP 1") || upperEn.equals("HOME") || upperEn.equals("CASA")
                || upperEn.equals("MANDANTE") || upperRu.equals("MANDANTE")
                || upperEn.equals("TIME 1") || upperRu.equals("TIME 1")
                || upperEn.equals("EQUIPE 1") || upperRu.equals("EQUIPE 1")
                || upperEn.startsWith("1 ") || upperRu.startsWith("1 ")
                || upperEn.startsWith("H1 ") || upperRu.startsWith("Ф1 ") || upperRu.startsWith("Ф1(")
                || upperEn.startsWith("HANDICAP 1") || upperRu.startsWith("ФОРА 1") || upperRu.startsWith("ФОРА1")
                || upperEn.startsWith("MANDANTE") || upperRu.startsWith("MANDANTE")
                || upperEn.startsWith("HOME") || upperRu.startsWith("HOME")
                || upperEn.startsWith("CASA") || upperRu.startsWith("CASA")
                || upperEn.startsWith("TIME 1") || upperRu.startsWith("TIME 1")
                || upperEn.startsWith("EQUIPE 1") || upperRu.startsWith("EQUIPE 1")) {
            return HandicapBet.Outcome.TEAM1;
        }

        if (upperEn.equals("2") || upperEn.equals("H2") || upperRu.equals("Ф2") || upperRu.equals("ФОРА 2") || upperRu.equals("ФОРА2")
                || upperEn.equals("HANDICAP 2") || upperEn.equals("AWAY") || upperEn.equals("FORA")
                || upperEn.equals("VISITANTE") || upperRu.equals("VISITANTE")
                || upperEn.equals("TIME 2") || upperRu.equals("TIME 2")
                || upperEn.equals("EQUIPE 2") || upperRu.equals("EQUIPE 2")
                || upperEn.startsWith("2 ") || upperRu.startsWith("2 ")
                || upperEn.startsWith("H2 ") || upperRu.startsWith("Ф2 ") || upperRu.startsWith("Ф2(")
                || upperEn.startsWith("HANDICAP 2") || upperRu.startsWith("ФОРА 2") || upperRu.startsWith("ФОРА2")
                || upperEn.startsWith("VISITANTE") || upperRu.startsWith("VISITANTE")
                || upperEn.startsWith("AWAY") || upperRu.startsWith("AWAY")
                || upperEn.startsWith("FORA") || upperRu.startsWith("FORA")
                || upperEn.startsWith("TIME 2") || upperRu.startsWith("TIME 2")
                || upperEn.startsWith("EQUIPE 2") || upperRu.startsWith("EQUIPE 2")) {
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

    protected MatchResultBet.Outcome resolveDoubleChanceOutcome(VaidebetStakeData stake) {
        if (stake == null) return null;
        String nameEn = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
        String nameRu = stake.getNameRu() != null ? stake.getNameRu().trim() : "";

        // Try built-in mapper first
        BetType mapped = map1X2DCRecord(nameEn, BetScope.FULL_MATCH, getStatType());
        if (mapped == null && !nameRu.isBlank()) {
            mapped = map1X2DCRecord(nameRu, BetScope.FULL_MATCH, getStatType());
        }

        if (mapped instanceof MatchResultBet mrb) {
            if (mrb.outcome() == MatchResultBet.Outcome.DC_1X
                    || mrb.outcome() == MatchResultBet.Outcome.DC_12
                    || mrb.outcome() == MatchResultBet.Outcome.DC_X2) {
                return mrb.outcome();
            }
        }

        String combined = ((stake.getNameEn() != null ? stake.getNameEn() : "") + " "
                + (stake.getNameRu() != null ? stake.getNameRu() : "")).toUpperCase();

        if (combined.contains("1X") || combined.contains("1-X") || combined.contains("1Х") || combined.contains("1/X")
                || combined.contains("1 OR X") || combined.contains("1 ИЛИ X") || combined.contains("1 ИЛИ Х")
                || combined.contains("1 ИЛИ НИЧЬЯ") || combined.contains("1 OU EMPATE") || combined.contains("1 OU X")
                || combined.contains("MANDANTE OU EMPATE") || combined.contains("CASA OU EMPATE")
                || combined.contains("HOME/DRAW") || combined.contains("П1Х")) {
            return MatchResultBet.Outcome.DC_1X;
        }
        if (combined.contains("12") || combined.contains("1-2") || combined.contains("1/2")
                || combined.contains("1 OR 2") || combined.contains("1 ИЛИ 2") || combined.contains("1 OU 2")
                || combined.contains("MANDANTE OU VISITANTE") || combined.contains("CASA OU FORA")
                || combined.contains("HOME/AWAY")) {
            return MatchResultBet.Outcome.DC_12;
        }
        if (combined.contains("X2") || combined.contains("X-2") || combined.contains("2X") || combined.contains("Х2")
                || combined.contains("X/2") || combined.contains("Х/2") || combined.contains("2/X")
                || combined.contains("X OR 2") || combined.contains("НИЧЬЯ ИЛИ 2") || combined.contains("EMPATE OU 2")
                || combined.contains("EMPATE OU VISITANTE") || combined.contains("EMPATE OU FORA")
                || combined.contains("X ИЛИ 2") || combined.contains("Х ИЛИ 2") || combined.contains("X OU 2")
                || combined.contains("DRAW/AWAY") || combined.contains("ПХ2")) {
            return MatchResultBet.Outcome.DC_X2;
        }
        return null;
    }

    protected boolean isDrawStake(VaidebetStakeData s) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim().toUpperCase() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim().toUpperCase() : "";
        return "X".equals(en) || "X".equals(ru) || "Х".equals(en) || "Х".equals(ru)
                || "DRAW".equals(en) || "НИЧЬЯ".equals(ru) || "EMPATE".equals(en) || "EMPATE".equals(ru)
                || en.contains("DRAW") || ru.contains("НИЧЬЯ") || en.contains("EMPATE") || ru.contains("EMPATE");
    }

    protected boolean isTeam1ResultStake(VaidebetStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("1".equalsIgnoreCase(en) || "1".equalsIgnoreCase(ru) || "Win1".equalsIgnoreCase(en) || "W1".equalsIgnoreCase(en)
                || "П1".equalsIgnoreCase(ru) || "P1".equalsIgnoreCase(en) || "Home".equalsIgnoreCase(en)
                || "Casa".equalsIgnoreCase(en) || "Mandante".equalsIgnoreCase(en) || "Mandante".equalsIgnoreCase(ru)
                || "Победа 1".equalsIgnoreCase(ru) || "Победа1".equalsIgnoreCase(ru)
                || "Team 1".equalsIgnoreCase(en) || "Команда 1".equalsIgnoreCase(ru)
                || "Time 1".equalsIgnoreCase(en) || "Time 1".equalsIgnoreCase(ru)
                || "Equipe 1".equalsIgnoreCase(en) || "Equipe 1".equalsIgnoreCase(ru)) {
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

    protected boolean isTeam2ResultStake(VaidebetStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("2".equalsIgnoreCase(en) || "2".equalsIgnoreCase(ru) || "Win2".equalsIgnoreCase(en) || "W2".equalsIgnoreCase(en)
                || "П2".equalsIgnoreCase(ru) || "P2".equalsIgnoreCase(en) || "Away".equalsIgnoreCase(en)
                || "Fora".equalsIgnoreCase(en) || "Visitante".equalsIgnoreCase(en) || "Visitante".equalsIgnoreCase(ru)
                || "Победа 2".equalsIgnoreCase(ru) || "Победа2".equalsIgnoreCase(ru)
                || "Team 2".equalsIgnoreCase(en) || "Команда 2".equalsIgnoreCase(ru)
                || "Time 2".equalsIgnoreCase(en) || "Time 2".equalsIgnoreCase(ru)
                || "Equipe 2".equalsIgnoreCase(en) || "Equipe 2".equalsIgnoreCase(ru)) {
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

    protected Double resolveTotalParam(VaidebetStakeData stake, VaidebetStakeGroupData group) {
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

    protected Double resolveHandicapParam(VaidebetStakeData stake, VaidebetStakeGroupData group) {
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

    protected BetSubject resolveTotalSubject(VaidebetStakeGroupData group, VaidebetStakeData stake, MatchCache match) {
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
