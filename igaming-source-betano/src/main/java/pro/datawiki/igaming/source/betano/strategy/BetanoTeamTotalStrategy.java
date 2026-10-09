package pro.datawiki.igaming.source.betano.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.betano.dto.BetanoMarket;
import pro.datawiki.igaming.source.betano.dto.BetanoSelection;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class BetanoTeamTotalStrategy extends AbstractBetanoStrategy {

    @Override
    public boolean supports(BetanoMarket market, String team1, String team2) {
        String name = market.getResolvedName().toLowerCase(Locale.ROOT);
        String type = market.getResolvedType().toUpperCase(Locale.ROOT);

        boolean isTotal = type.contains("TOTAL") || type.contains("OVER_UNDER")
                || name.contains("total") || name.contains("mais/menos") || name.contains("over/under");
        if (!isTotal) return false;

        return name.contains("equipa 1") || name.contains("equipa 2")
                || name.contains("time 1") || name.contains("time 2")
                || name.contains("da equipa") || name.contains("do time")
                || name.contains("casa -") || name.contains("fora -")
                || name.contains("home -") || name.contains("away -")
                || (team1 != null && name.contains(team1.toLowerCase(Locale.ROOT)))
                || (team2 != null && name.contains(team2.toLowerCase(Locale.ROOT)));
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

        String marketName = market.getResolvedName().toLowerCase(Locale.ROOT);
        BetSubject subject = resolveSubject(marketName, team1, team2);
        if (subject == null) return items;

        BetScope scope = BetanoScopeResolver.resolve(market.getResolvedName(), market.getResolvedType(), sportType);
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
            if (name.contains("mais") || name.contains("over") || name.startsWith(">") || name.startsWith("o ")) {
                direction = TotalBet.Direction.OVER;
            } else if (name.contains("menos") || name.contains("under") || name.startsWith("<") || name.startsWith("u ")) {
                direction = TotalBet.Direction.UNDER;
            }

            if (direction != null) {
                OddItem item = createOddItem(selection, market.getResolvedName(),
                        new TotalBet(scope, subject, direction, total, false, null));
                if (item != null) items.add(item);
            }
        }
        return items;
    }

    private BetSubject resolveSubject(String marketName, String team1, String team2) {
        if (marketName.contains("equipa 1") || marketName.contains("time 1")
                || marketName.contains("casa") || marketName.contains("home")
                || (team1 != null && marketName.contains(team1.toLowerCase(Locale.ROOT)))) {
            return BetSubject.TEAM1;
        }
        if (marketName.contains("equipa 2") || marketName.contains("time 2")
                || marketName.contains("fora") || marketName.contains("away")
                || (team2 != null && marketName.contains(team2.toLowerCase(Locale.ROOT)))) {
            return BetSubject.TEAM2;
        }
        return null;
    }
}
