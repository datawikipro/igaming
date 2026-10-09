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
public class DigitainMatchResultStrategy extends AbstractDigitainStrategy {

    @Override
    public boolean supports(DigitainStakeGroupData group) {
        Long groupId = group.getId();
        String name = ((group.getNameEn() != null ? group.getNameEn() : "") + " " + (group.getNameRu() != null ? group.getNameRu() : ""))
                .toLowerCase(Locale.ROOT);

        if (name.contains("double chance") || name.contains("двойной шанс") || name.contains("двойной исход")
                || name.contains("both teams to score") || name.contains("обе забьют")
                || name.contains("total") || name.contains("тотал")
                || name.contains("handicap") || name.contains("фора")
                || name.contains("draw no bet") || name.contains("ничья исключена")
                || name.contains("тайм/матч") || name.contains("ht/ft")) {
            return false;
        }

        if (groupId != null && (groupId == 1L || groupId == 702L)) {
            return true;
        }

        return name.contains("match result") || name.contains("1x2") || name.contains("исход")
                || name.contains("winner") || name.contains("победитель");
    }

    @Override
    public List<OddItem> map(DigitainStakeGroupData group, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        if (group.getStakes() == null || group.getStakes().isEmpty()) return items;

        BetScope scope = DigitainScopeResolver.resolve(group.getNameEn(), group.getNameRu(), sportType);

        boolean hasDraw = group.getStakes().stream()
                .anyMatch(s -> "X".equalsIgnoreCase(s.getNameEn()) || "X".equalsIgnoreCase(s.getNameRu())
                        || (s.getNameEn() != null && s.getNameEn().toLowerCase(Locale.ROOT).contains("draw"))
                        || (s.getNameRu() != null && s.getNameRu().toLowerCase(Locale.ROOT).contains("ничья")));

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            String nameEn = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
            String nameRu = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
            MatchResultBet.Outcome outcome = null;

            if ("1".equalsIgnoreCase(nameEn) || "Win1".equalsIgnoreCase(nameEn) || "П1".equalsIgnoreCase(nameRu)
                    || (team1 != null && (nameEn.equalsIgnoreCase(team1) || nameRu.equalsIgnoreCase(team1)))) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            } else if ("X".equalsIgnoreCase(nameEn) || "X".equalsIgnoreCase(nameRu) || "Draw".equalsIgnoreCase(nameEn) || "Ничья".equalsIgnoreCase(nameRu)) {
                outcome = MatchResultBet.Outcome.DRAW;
            } else if ("2".equalsIgnoreCase(nameEn) || "Win2".equalsIgnoreCase(nameEn) || "П2".equalsIgnoreCase(nameRu)
                    || (team2 != null && (nameEn.equalsIgnoreCase(team2) || nameRu.equalsIgnoreCase(team2)))) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
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
