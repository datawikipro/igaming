package pro.datawiki.igaming.source.betano.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betano.dto.BetanoMarket;
import pro.datawiki.igaming.source.betano.dto.BetanoSelection;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class BetanoTotalStrategy extends AbstractBetanoStrategy {

    @Override
    public boolean supports(BetanoMarket market, String team1, String team2) {
        String name = market.getResolvedName().toLowerCase(Locale.ROOT);
        String type = market.getResolvedType().toUpperCase(Locale.ROOT);

        // Exclude team totals
        if (name.contains("equipa") || name.contains("time")
                || (team1 != null && name.contains(team1.toLowerCase(Locale.ROOT)))
                || (team2 != null && name.contains(team2.toLowerCase(Locale.ROOT)))) {
            return false;
        }

        return type.contains("TOTAL") || type.contains("OVER_UNDER") || type.contains("OVERUNDER") || type.equals("OU")
                || name.contains("total") || name.contains("mais/menos") || name.contains("über/unter")
                || name.contains("over/under") || name.contains("total de gols") || name.contains("tore mehr/weniger")
                || name.contains("total de pontos") || name.contains("total de sets");
    }

    @Override
    public boolean supports(BetanoMarket market) {
        return supports(market, null, null);
    }

    @Override
    public List<OddItem> map(BetanoMarket market, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        List<BetanoSelection> selections = market.getResolvedSelections();
        if (selections == null || selections.isEmpty()) return items;

        BetScope scope = BetanoScopeResolver.resolve(market.getResolvedName(), market.getResolvedType(), sportType);
        StatType statType = (sportType == SportType.MMA || sportType == SportType.BOXING)
                ? StatType.ROUNDS_TIME
                : null;
        Double marketTotal = parseTotalValue(market.getResolvedName());

        for (BetanoSelection selection : selections) {
            Double total = selection.getResolvedHandicap();
            if (total == null) {
                total = parseTotalValue(selection.getResolvedName());
            }
            if (total == null) {
                total = marketTotal;
            }
            if (total == null) continue;

            String name = selection.getResolvedName().toLowerCase(Locale.ROOT);
            TotalBet.Direction direction = null;
            if (name.contains("mais") || name.contains("over") || name.contains("über") || name.contains("mehr") || name.startsWith(">") || name.startsWith("o ")) {
                direction = TotalBet.Direction.OVER;
            } else if (name.contains("menos") || name.contains("under") || name.contains("unter") || name.contains("weniger") || name.startsWith("<") || name.startsWith("u ")) {
                direction = TotalBet.Direction.UNDER;
            }

            if (direction != null) {
                OddItem item = createOddItem(selection, market.getResolvedName(),
                        new TotalBet(scope, BetSubject.MATCH, direction, total, false, statType));
                if (item != null) items.add(item);
            }
        }
        return items;
    }
}
