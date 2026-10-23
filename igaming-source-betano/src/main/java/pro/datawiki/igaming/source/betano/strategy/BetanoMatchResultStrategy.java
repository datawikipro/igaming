package pro.datawiki.igaming.source.betano.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.source.betano.dto.BetanoMarket;
import pro.datawiki.igaming.source.betano.dto.BetanoSelection;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class BetanoMatchResultStrategy extends AbstractBetanoStrategy {

    @Override
    public boolean supports(BetanoMarket market) {
        String name = market.getResolvedName().toLowerCase(Locale.ROOT);
        String type = market.getResolvedType().toUpperCase(Locale.ROOT);

        if (name.contains("dupla chance") || name.contains("double chance") || name.contains("doppelte chance")
                || name.contains("ambas marcam") || name.contains("both teams to score") || name.contains("beide teams")
                || name.contains("total") || name.contains("handicap") || name.contains("über/unter")
                || name.contains("empate anula") || name.contains("draw no bet") || name.contains("unentschieden keine")
                || name.contains("intervalo/final") || name.contains("half time/full time") || name.contains("halbzeit/endstand")) {
            return false;
        }

        return type.contains("1X2") || type.contains("MATCH_RESULT") || type.contains("WINNER")
                || type.equals("MR12") || type.equals("MR") || type.equals("MATCHRESULT") || type.equals("ML")
                || type.contains("MONEYLINE") || type.equals("MRES")
                || name.contains("match result") || name.contains("resultado final")
                || name.contains("endergebnis") || name.contains("spielergebnis") || name.contains("sieger")
                || name.contains("1x2") || name.contains("winner") || name.equals("vencedor")
                || name.contains("vencedor do encontro");
    }

    @Override
    public List<OddItem> map(BetanoMarket market, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        List<BetanoSelection> selections = market.getResolvedSelections();
        if (selections == null || selections.isEmpty()) return items;

        BetScope scope = BetanoScopeResolver.resolve(market.getResolvedName(), market.getResolvedType(), sportType);

        boolean hasDraw = selections.stream().anyMatch(s -> {
            String n = s.getResolvedName().toLowerCase(Locale.ROOT);
            return n.equals("x") || n.equals("empate") || n.equals("draw") || n.equals("tie") || n.equals("unentschieden");
        });

        for (BetanoSelection selection : selections) {
            String name = selection.getResolvedName().trim();
            String code = selection.getCode() != null ? selection.getCode().trim().toUpperCase(Locale.ROOT) : "";

            MatchResultBet.Outcome outcome = null;
            if (code.equals("1") || name.equalsIgnoreCase("1") || (team1 != null && name.equalsIgnoreCase(team1)) || name.equalsIgnoreCase("Home") || name.equalsIgnoreCase("Heim")) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            } else if (code.equals("X") || name.equalsIgnoreCase("X") || name.equalsIgnoreCase("Empate") || name.equalsIgnoreCase("Draw") || name.equalsIgnoreCase("Unentschieden")) {
                outcome = MatchResultBet.Outcome.DRAW;
            } else if (code.equals("2") || name.equalsIgnoreCase("2") || (team2 != null && name.equalsIgnoreCase(team2)) || name.equalsIgnoreCase("Away") || name.equalsIgnoreCase("Gast") || name.equalsIgnoreCase("Auswärts")) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            }

            if (outcome != null) {
                OddItem item = createOddItem(selection, market.getResolvedName(), new MatchResultBet(scope, outcome, null));
                if (item != null) items.add(item);
            }
        }
        return items;
    }
}
