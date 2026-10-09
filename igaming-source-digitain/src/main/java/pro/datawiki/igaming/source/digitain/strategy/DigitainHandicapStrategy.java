package pro.datawiki.igaming.source.digitain.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DigitainHandicapStrategy extends AbstractDigitainStrategy {

    @Override
    public boolean supports(DigitainStakeGroupData group) {
        Long groupId = group.getId();
        String name = ((group.getNameEn() != null ? group.getNameEn() : "") + " " + (group.getNameRu() != null ? group.getNameRu() : ""))
                .toLowerCase(Locale.ROOT);

        if (name.contains("draw no bet") || name.contains("ничья исключена") || name.contains("dnb")) {
            return false;
        }

        if (groupId != null && groupId == 2L) {
            return true;
        }

        return name.contains("handicap") || name.contains("фора");
    }

    @Override
    public List<OddItem> map(DigitainStakeGroupData group, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        if (group.getStakes() == null || group.getStakes().isEmpty()) return items;

        BetScope scope = DigitainScopeResolver.resolve(group.getNameEn(), group.getNameRu(), sportType);

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
            HandicapBet.Outcome outcome = null;

            if ("Handicap 1".equalsIgnoreCase(nameEn) || "Фора1".equalsIgnoreCase(nameRu) || "H1".equalsIgnoreCase(nameEn) || "Ф1".equalsIgnoreCase(nameRu)
                    || (team1 != null && (nameEn.equalsIgnoreCase(team1) || nameRu.equalsIgnoreCase(team1)))) {
                outcome = HandicapBet.Outcome.TEAM1;
            } else if ("Handicap 2".equalsIgnoreCase(nameEn) || "Фора2".equalsIgnoreCase(nameRu) || "H2".equalsIgnoreCase(nameEn) || "Ф2".equalsIgnoreCase(nameRu)
                    || (team2 != null && (nameEn.equalsIgnoreCase(team2) || nameRu.equalsIgnoreCase(team2)))) {
                outcome = HandicapBet.Outcome.TEAM2;
            }

            if (outcome != null) {
                OddItem item = createOddItem(stake, group.getNameEn() != null ? group.getNameEn() : group.getNameRu(),
                        new HandicapBet(scope, outcome, arg, false, null), arg);
                if (item != null) items.add(item);
            }
        }
        return items;
    }
}
