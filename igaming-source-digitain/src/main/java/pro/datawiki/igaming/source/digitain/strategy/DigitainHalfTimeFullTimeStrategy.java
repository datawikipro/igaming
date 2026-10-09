package pro.datawiki.igaming.source.digitain.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DigitainHalfTimeFullTimeStrategy extends AbstractDigitainStrategy {

    @Override
    public boolean supports(DigitainStakeGroupData group) {
        String name = ((group.getNameEn() != null ? group.getNameEn() : "") + " " + (group.getNameRu() != null ? group.getNameRu() : ""))
                .toLowerCase(Locale.ROOT);

        return name.contains("тайм/матч") || name.contains("тайм - матч")
                || name.contains("half time / full time") || name.contains("half time/full time")
                || name.contains("ht/ft") || name.contains("исход 1 тайма и всего матча");
    }

    @Override
    public List<OddItem> map(DigitainStakeGroupData group, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        if (group.getStakes() == null || group.getStakes().isEmpty()) return items;

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            String desc = stake.getNameEn() != null ? stake.getNameEn() : stake.getNameRu();
            HalfTimeFullTimeBet.Outcome htftOutcome = parseOutcome(desc, team1, team2);

            if (htftOutcome != null) {
                OddItem item = createOddItem(stake, group.getNameEn() != null ? group.getNameEn() : group.getNameRu(),
                        new HalfTimeFullTimeBet(htftOutcome), null);
                if (item != null) items.add(item);
            }
        }
        return items;
    }

    private HalfTimeFullTimeBet.Outcome parseOutcome(String desc, String team1, String team2) {
        if (desc == null || desc.isBlank()) return null;
        String normalized = desc.toUpperCase(Locale.ROOT)
                .replace("/", " - ")
                .replace(" / ", " - ");

        if (team1 != null && !team1.isBlank()) {
            normalized = normalized.replace(team1.toUpperCase(Locale.ROOT), "1");
        }
        if (team2 != null && !team2.isBlank()) {
            normalized = normalized.replace(team2.toUpperCase(Locale.ROOT), "2");
        }
        normalized = normalized.replace("HOME", "1")
                .replace("AWAY", "2")
                .replace("П1", "1")
                .replace("П2", "2")
                .replace("DRAW", "X")
                .replace("НИЧЬЯ", "X")
                .replace("Н", "X")
                .replace("TIE", "X")
                .replaceAll("\\s+", "");

        return switch (normalized) {
            case "1-1", "1/1", "11" -> HalfTimeFullTimeBet.Outcome.W1_W1;
            case "1-X", "1/X", "1X" -> HalfTimeFullTimeBet.Outcome.W1_X;
            case "1-2", "1/2", "12" -> HalfTimeFullTimeBet.Outcome.W1_W2;
            case "X-1", "X/1", "X1" -> HalfTimeFullTimeBet.Outcome.X_W1;
            case "X-X", "X/X", "XX" -> HalfTimeFullTimeBet.Outcome.X_X;
            case "X-2", "X/2", "X2" -> HalfTimeFullTimeBet.Outcome.X_W2;
            case "2-1", "2/1", "21" -> HalfTimeFullTimeBet.Outcome.W2_W1;
            case "2-X", "2/X", "2X" -> HalfTimeFullTimeBet.Outcome.W2_X;
            case "2-2", "2/2", "22" -> HalfTimeFullTimeBet.Outcome.W2_W2;
            default -> null;
        };
    }
}
