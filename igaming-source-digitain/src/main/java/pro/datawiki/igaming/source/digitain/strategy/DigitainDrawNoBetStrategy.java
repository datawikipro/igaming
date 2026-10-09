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
public class DigitainDrawNoBetStrategy extends AbstractDigitainStrategy {

    @Override
    public boolean supports(DigitainStakeGroupData group) {
        String name = ((group.getNameEn() != null ? group.getNameEn() : "") + " " + (group.getNameRu() != null ? group.getNameRu() : ""))
                .toLowerCase(Locale.ROOT);

        return name.contains("draw no bet") || name.contains("ничья исключена")
                || name.contains("ничьей нет") || name.equals("dnb");
    }

    @Override
    public List<OddItem> map(DigitainStakeGroupData group, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        if (group.getStakes() == null || group.getStakes().isEmpty()) return items;

        BetScope scope = DigitainScopeResolver.resolve(group.getNameEn(), group.getNameRu(), sportType);

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            String nameEn = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
            String nameRu = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
            HandicapBet.Outcome outcome = null;

            if ("1".equalsIgnoreCase(nameEn) || "Win1".equalsIgnoreCase(nameEn) || "П1".equalsIgnoreCase(nameRu)
                    || (team1 != null && (nameEn.equalsIgnoreCase(team1) || nameRu.equalsIgnoreCase(team1)))) {
                outcome = HandicapBet.Outcome.TEAM1;
            } else if ("2".equalsIgnoreCase(nameEn) || "Win2".equalsIgnoreCase(nameEn) || "П2".equalsIgnoreCase(nameRu)
                    || (team2 != null && (nameEn.equalsIgnoreCase(team2) || nameRu.equalsIgnoreCase(team2)))) {
                outcome = HandicapBet.Outcome.TEAM2;
            }

            if (outcome != null) {
                OddItem item = createOddItem(stake, group.getNameEn() != null ? group.getNameEn() : group.getNameRu(),
                        new HandicapBet(scope, outcome, 0.0, false, null), 0.0);
                if (item != null) items.add(item);
            }
        }
        return items;
    }
}
