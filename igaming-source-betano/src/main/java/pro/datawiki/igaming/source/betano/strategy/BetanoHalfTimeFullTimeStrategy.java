package pro.datawiki.igaming.source.betano.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;
import pro.datawiki.igaming.source.betano.dto.BetanoMarket;
import pro.datawiki.igaming.source.betano.dto.BetanoSelection;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class BetanoHalfTimeFullTimeStrategy extends AbstractBetanoStrategy {

    @Override
    public boolean supports(BetanoMarket market) {
        String name = market.getResolvedName().toLowerCase(Locale.ROOT);
        String type = market.getResolvedType().toUpperCase(Locale.ROOT);

        return type.contains("HT_FT") || type.contains("HALF_TIME_FULL_TIME")
                || name.contains("intervalo/final") || name.contains("intervalo / final")
                || name.contains("intervalo / resultado final")
                || name.contains("half time / full time") || name.contains("ht/ft")
                || name.contains("duplo resultado");
    }

    @Override
    public List<OddItem> map(BetanoMarket market, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        List<BetanoSelection> selections = market.getResolvedSelections();
        if (selections == null || selections.isEmpty()) return items;

        for (BetanoSelection selection : selections) {
            String name = selection.getResolvedName();
            HalfTimeFullTimeBet.Outcome htftOutcome = parseOutcome(name, team1, team2);

            if (htftOutcome != null) {
                OddItem item = createOddItem(selection, market.getResolvedName(),
                        new HalfTimeFullTimeBet(htftOutcome));
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
                .replace("CASA", "1")
                .replace("AWAY", "2")
                .replace("FORA", "2")
                .replace("DRAW", "X")
                .replace("EMPATE", "X")
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
