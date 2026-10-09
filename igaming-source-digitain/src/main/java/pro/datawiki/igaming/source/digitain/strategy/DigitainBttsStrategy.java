package pro.datawiki.igaming.source.digitain.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DigitainBttsStrategy extends AbstractDigitainStrategy {

    @Override
    public boolean supports(DigitainStakeGroupData group) {
        Long groupId = group.getId();
        String name = ((group.getNameEn() != null ? group.getNameEn() : "") + " " + (group.getNameRu() != null ? group.getNameRu() : ""))
                .toLowerCase(Locale.ROOT);

        if (groupId != null && groupId == 46L) {
            return true;
        }

        return name.contains("both teams to score") || name.contains("обе забьют") || name.contains("обе команды забьют");
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
            BinaryMarketBet.Outcome outcome = null;

            if ("Yes".equalsIgnoreCase(nameEn) || "Да".equalsIgnoreCase(nameRu) || "Y".equalsIgnoreCase(nameEn)) {
                outcome = BinaryMarketBet.Outcome.YES;
            } else if ("No".equalsIgnoreCase(nameEn) || "Нет".equalsIgnoreCase(nameRu) || "N".equalsIgnoreCase(nameEn)) {
                outcome = BinaryMarketBet.Outcome.NO;
            }

            if (outcome != null) {
                OddItem item = createOddItem(stake, group.getNameEn() != null ? group.getNameEn() : group.getNameRu(),
                        new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, outcome, null), null);
                if (item != null) items.add(item);
            }
        }
        return items;
    }
}
