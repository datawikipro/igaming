package pro.datawiki.igaming.source.digitain.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DigitainTotalHandler extends AbstractDigitainMarketHandler {

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern NUMERIC_PARAM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^(?i)(?:team\\s*[12]|home|away|команда\\s*[12]|h[12]|ф[12]|[12])\\b\\s*");
    private static final Pattern TEAM1_PATTERN = Pattern.compile("(?i)(team\\s*1|home|команда\\s*1|1-я\\s*команда|1-й\\s*команд|первой\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?1|ит\\s*1|ит1)");
    private static final Pattern TEAM2_PATTERN = Pattern.compile("(?i)(team\\s*2|away|команда\\s*2|2-я\\s*команда|2-й\\s*команд|второй\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?2|ит\\s*2|ит2)");

    @Override
    public boolean supports(DigitainStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }
        Long id = group.getId();
        String name = getGroupName(group).toLowerCase();

        // Exclude markets handled by specialized handlers
        if (name.contains("corner") || name.contains("углов")
                || name.contains("card") || name.contains("карточ") || name.contains("booking")
                || name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ")
                || name.contains("handicap") || name.contains("фора")
                || name.contains("match result") || name.contains("1x2")
                || name.contains("double chance") || name.contains("двойной шанс")
                || name.contains("both teams") || name.contains("обе забьют")
                || name.contains("correct score") || name.contains("точный счет")) {
            return false;
        }

        // Known total group IDs: 3 (Full Match Total), 6 (1st Half Total), 9 (2nd Half Total)
        if (id != null && (id == 3L || id == 6L || id == 9L)) {
            return true;
        }

        return name.contains("total") || name.contains("тотал") || name.contains("over/under") || name.contains("o/u");
    }

    @Override
    public void handle(DigitainStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) {
                continue;
            }

            Double param = resolveParam(stake, group);
            if (param == null || param < 0) {
                continue;
            }

            TotalBet.Direction direction = resolveDirection(stake);
            if (direction == null) {
                continue;
            }

            BetSubject subject = resolveSubject(group, stake, match);
            boolean isAsian = isQuarterAsian(param);

            TotalBet betType = new TotalBet(scope, subject, direction, param, isAsian, StatType.MATCH);
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    private TotalBet.Direction resolveDirection(DigitainStakeData stake) {
        String en = stake.getNameEn() != null ? stake.getNameEn().trim().toUpperCase() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim().toUpperCase() : "";

        // Check OVER
        if (en.equals("OVER") || ru.equals("БОЛЬШЕ") || ru.equals("Б") || en.equals("O")
                || en.startsWith("OVER") || ru.startsWith("БОЛЬШЕ") || ru.startsWith("ТБ")
                || en.contains("OVER") || ru.contains("БОЛЬШЕ")) {
            return TotalBet.Direction.OVER;
        }

        // Check UNDER
        if (en.equals("UNDER") || ru.equals("МЕНЬШЕ") || ru.equals("М") || en.equals("U")
                || en.startsWith("UNDER") || ru.startsWith("МЕНЬШЕ") || ru.startsWith("ТМ")
                || en.contains("UNDER") || ru.contains("МЕНЬШЕ")) {
            return TotalBet.Direction.UNDER;
        }

        // Check EXACT
        if (en.equals("EXACT") || ru.equals("РОВНО") || en.startsWith("EXACT") || ru.startsWith("РОВНО")) {
            return TotalBet.Direction.EXACT;
        }

        return null;
    }

    private BetSubject resolveSubject(DigitainStakeGroupData group, DigitainStakeData stake, MatchCache match) {
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

    private Double resolveParam(DigitainStakeData stake, DigitainStakeGroupData group) {
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

    private Double extractNumericParam(String text) {
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
