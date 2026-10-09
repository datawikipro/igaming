package pro.datawiki.igaming.source.betano.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.source.betano.dto.BetanoMarket;
import pro.datawiki.igaming.source.betano.dto.BetanoSelection;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class BetanoDrawNoBetStrategy extends AbstractBetanoStrategy {

    @Override
    public boolean supports(BetanoMarket market) {
        String name = market.getResolvedName().toLowerCase(Locale.ROOT);
        String type = market.getResolvedType().toUpperCase(Locale.ROOT);

        return type.contains("DRAW_NO_BET") || type.contains("DNB")
                || name.contains("empate anula aposta") || name.contains("empate anula")
                || name.contains("draw no bet") || name.equals("dnb");
    }

    @Override
    public List<OddItem> map(BetanoMarket market, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        List<BetanoSelection> selections = market.getResolvedSelections();
        if (selections == null || selections.isEmpty()) return items;

        BetScope scope = BetanoScopeResolver.resolve(market.getResolvedName(), market.getResolvedType(), sportType);

        for (BetanoSelection selection : selections) {
            String name = selection.getResolvedName().trim();
            String nameLower = name.toLowerCase(Locale.ROOT);
            HandicapBet.Outcome outcome = null;

            if (name.equalsIgnoreCase("1") || nameLower.startsWith("home")
                    || (team1 != null && nameLower.contains(team1.toLowerCase(Locale.ROOT)))) {
                outcome = HandicapBet.Outcome.TEAM1;
            } else if (name.equalsIgnoreCase("2") || nameLower.startsWith("away")
                    || (team2 != null && nameLower.contains(team2.toLowerCase(Locale.ROOT)))) {
                outcome = HandicapBet.Outcome.TEAM2;
            }

            if (outcome != null) {
                OddItem item = createOddItem(selection, market.getResolvedName(),
                        new HandicapBet(scope, outcome, 0.0, false, null));
                if (item != null) items.add(item);
            }
        }
        return items;
    }
}
