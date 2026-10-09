package pro.datawiki.igaming.source.digitain.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DigitainDoubleChanceStrategy extends AbstractDigitainStrategy {

    @Override
    public boolean supports(DigitainStakeGroupData group) {
        Long groupId = group.getId();
        String name = ((group.getNameEn() != null ? group.getNameEn() : "") + " " + (group.getNameRu() != null ? group.getNameRu() : ""))
                .toLowerCase(Locale.ROOT);

        if (groupId != null && groupId == 992L) {
            return true;
        }

        return name.contains("double chance") || name.contains("двойной шанс") || name.contains("двойной исход");
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
            String combined = (nameEn + " " + nameRu).toUpperCase(Locale.ROOT);
            MatchResultBet.Outcome outcome = null;

            if (combined.contains("1X") || combined.contains("1-X") || combined.contains("1Х") || combined.contains("1 ИЛИ X") || combined.contains("1 OR X")) {
                outcome = MatchResultBet.Outcome.DC_1X;
            } else if (combined.contains("12") || combined.contains("1-2") || combined.contains("1 ИЛИ 2") || combined.contains("1 OR 2")) {
                outcome = MatchResultBet.Outcome.DC_12;
            } else if (combined.contains("X2") || combined.contains("X-2") || combined.contains("Х2") || combined.contains("X ИЛИ 2") || combined.contains("X OR 2")) {
                outcome = MatchResultBet.Outcome.DC_X2;
            }

            if (outcome != null) {
                OddItem item = createOddItem(stake, group.getNameEn() != null ? group.getNameEn() : group.getNameRu(),
                        new MatchResultBet(scope, outcome, null), null);
                if (item != null) items.add(item);
            }
        }
        return items;
    }
}
