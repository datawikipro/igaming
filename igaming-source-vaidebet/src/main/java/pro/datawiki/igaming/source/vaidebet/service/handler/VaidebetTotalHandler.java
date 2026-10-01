package pro.datawiki.igaming.source.vaidebet.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeData;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeGroupData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(60)
public class VaidebetTotalHandler extends AbstractVaidebetMarketHandler {

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern NUMERIC_PARAM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^(?i)(?:team\\s*[12]|home|away|команда\\s*[12]|time\\s*[12]|mandante|visitante|h[12]|ф[12]|[12])\\b\\s*");
    private static final Pattern TEAM1_PATTERN = Pattern.compile("(?i)(team\\s*1|home|casa|mandante|time\\s*1|equipe\\s*1|команда\\s*1|1-я\\s*команда|1-й\\s*команд|первой\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?1|ит\\s*1|ит1)");
    private static final Pattern TEAM2_PATTERN = Pattern.compile("(?i)(team\\s*2|away|fora|visitante|time\\s*2|equipe\\s*2|команда\\s*2|2-я\\s*команда|2-й\\s*команд|второй\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?2|ит\\s*2|ит2)");

    @Override
    public boolean supports(VaidebetStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }
        Long id = group.getId();
        String name = getGroupName(group).toLowerCase();

        // Exclude markets handled by specialized handlers
        if (name.contains("corner") || name.contains("углов") || name.contains("escanteio")
                || name.contains("card") || name.contains("карточ") || name.contains("cartao") || name.contains("cartão") || name.contains("booking")
                || name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ") || name.contains("mapa ")
                || name.contains("handicap") || name.contains("фора")
                || name.contains("match result") || name.contains("1x2")
                || name.contains("double chance") || name.contains("двойной шанс") || name.contains("dupla chance")
                || name.contains("both teams") || name.contains("обе забьют") || name.contains("ambas marcam")
                || name.contains("correct score") || name.contains("точный счет") || name.contains("resultado correto")) {
            return false;
        }

        // Known total group IDs: 3 (Full Match Total), 6 (1st Half Total), 9 (2nd Half Total)
        if (id != null && (id == 3L || id == 6L || id == 9L)) {
            return true;
        }

        return name.contains("total") || name.contains("тотал") || name.contains("over/under") || name.contains("o/u")
                || name.contains("mais/menos") || name.contains("gols");
    }

    @Override
    public void handle(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
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

    private TotalBet.Direction resolveDirection(VaidebetStakeData stake) {
        String en = stake.getNameEn() != null ? stake.getNameEn().trim().toUpperCase() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim().toUpperCase() : "";

        // Check OVER
        if (en.equals("OVER") || ru.equals("БОЛЬШЕ") || ru.equals("Б") || en.equals("O") || en.equals("MAIS")
                || en.equals("ACIMA") || ru.equals("ACIMA") || en.equals("MAIS DE") || en.equals("ACIMA DE")
                || en.startsWith("OVER") || ru.startsWith("БОЛЬШЕ") || ru.startsWith("ТБ") || en.startsWith("MAIS DE")
                || en.startsWith("ACIMA") || ru.startsWith("ACIMA")
                || en.contains("OVER") || ru.contains("БОЛЬШЕ") || en.contains("MAIS") || en.contains("ACIMA") || ru.contains("ACIMA")) {
            return TotalBet.Direction.OVER;
        }

        // Check UNDER
        if (en.equals("UNDER") || ru.equals("МЕНЬШЕ") || ru.equals("М") || en.equals("U") || en.equals("MENOS")
                || en.equals("ABAIXO") || ru.equals("ABAIXO") || en.equals("MENOS DE") || en.equals("ABAIXO DE")
                || en.startsWith("UNDER") || ru.startsWith("МЕНЬШЕ") || ru.startsWith("ТМ") || en.startsWith("MENOS DE")
                || en.startsWith("ABAIXO") || ru.startsWith("ABAIXO")
                || en.contains("UNDER") || ru.contains("МЕНЬШЕ") || en.contains("MENOS") || en.contains("ABAIXO") || ru.contains("ABAIXO")) {
            return TotalBet.Direction.UNDER;
        }

        // Check EXACT
        if (en.equals("EXACT") || ru.equals("РОВНО") || en.startsWith("EXACT") || ru.startsWith("РОВНО") || en.equals("EXATO") || en.startsWith("EXATO")) {
            return TotalBet.Direction.EXACT;
        }

        return null;
    }

    private BetSubject resolveSubject(VaidebetStakeGroupData group, VaidebetStakeData stake, MatchCache match) {
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

    private Double resolveParam(VaidebetStakeData stake, VaidebetStakeGroupData group) {
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
