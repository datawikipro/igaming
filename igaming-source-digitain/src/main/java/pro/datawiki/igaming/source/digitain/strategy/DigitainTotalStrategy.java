package pro.datawiki.igaming.source.digitain.strategy;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DigitainTotalStrategy extends AbstractDigitainStrategy {

    @Override
    public boolean supports(DigitainStakeGroupData group, String team1, String team2) {
        Long groupId = group.getId();
        String name = ((group.getNameEn() != null ? group.getNameEn() : "") + " " + (group.getNameRu() != null ? group.getNameRu() : ""))
                .toLowerCase(Locale.ROOT);

        // Exclude team totals
        if (name.contains("индивидуальный") || name.contains("команда 1") || name.contains("команда 2")
                || name.contains("team 1") || name.contains("team 2") || name.contains("1-го") || name.contains("2-го")
                || (team1 != null && name.contains(team1.toLowerCase(Locale.ROOT)))
                || (team2 != null && name.contains(team2.toLowerCase(Locale.ROOT)))) {
            return false;
        }

        if (groupId != null && groupId == 3L) {
            return true;
        }

        return name.contains("total") || name.contains("тотал");
    }

    @Override
    public boolean supports(DigitainStakeGroupData group) {
        return supports(group, null, null);
    }

    @Override
    public List<OddItem> map(DigitainStakeGroupData group, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        if (group.getStakes() == null || group.getStakes().isEmpty()) return items;

        BetScope scope = DigitainScopeResolver.resolve(group.getNameEn(), group.getNameRu(), sportType);
        StatType statType = (sportType == SportType.MMA || sportType == SportType.BOXING)
                ? StatType.ROUNDS_TIME
                : null;

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double arg = stake.getArgument();
            if (arg == null) {
                arg = parseTotalValue(stake.getNameEn() != null ? stake.getNameEn() : stake.getNameRu());
            }
            if (arg == null) continue;

            String nameEn = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
            String nameRu = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
            TotalBet.Direction direction = null;

            if ("Over".equalsIgnoreCase(nameEn) || "Больше".equalsIgnoreCase(nameRu) || "Б".equalsIgnoreCase(nameRu) || "O".equalsIgnoreCase(nameEn)) {
                direction = TotalBet.Direction.OVER;
            } else if ("Under".equalsIgnoreCase(nameEn) || "Меньше".equalsIgnoreCase(nameRu) || "М".equalsIgnoreCase(nameRu) || "U".equalsIgnoreCase(nameEn)) {
                direction = TotalBet.Direction.UNDER;
            }

            if (direction != null) {
                OddItem item = createOddItem(stake, group.getNameEn() != null ? group.getNameEn() : group.getNameRu(),
                        new TotalBet(scope, BetSubject.MATCH, direction, arg, false, statType), arg);
                if (item != null) items.add(item);
            }
        }
        return items;
    }
}
