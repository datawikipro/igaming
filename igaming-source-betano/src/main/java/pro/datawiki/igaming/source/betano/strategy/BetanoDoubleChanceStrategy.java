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
public class BetanoDoubleChanceStrategy extends AbstractBetanoStrategy {

    @Override
    public boolean supports(BetanoMarket market) {
        String name = market.getResolvedName().toLowerCase(Locale.ROOT);
        String type = market.getResolvedType().toUpperCase(Locale.ROOT);

        return type.contains("DOUBLE_CHANCE") || type.contains("DC") || name.contains("dupla chance")
                || name.contains("hipótese dupla") || name.contains("double chance") || name.contains("doppelte chance");
    }

    @Override
    public List<OddItem> map(BetanoMarket market, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        List<BetanoSelection> selections = market.getResolvedSelections();
        if (selections == null || selections.isEmpty()) return items;

        BetScope scope = BetanoScopeResolver.resolve(market.getResolvedName(), market.getResolvedType(), sportType);

        for (BetanoSelection selection : selections) {
            String name = selection.getResolvedName().trim().toUpperCase(Locale.ROOT);
            MatchResultBet.Outcome outcome = null;

            if (name.equals("1X") || name.contains("1 OU X") || name.contains("1 OR X") || name.contains("1 ODER X") || name.contains("1/X")) {
                outcome = MatchResultBet.Outcome.DC_1X;
            } else if (name.equals("12") || name.contains("1 OU 2") || name.contains("1 OR 2") || name.contains("1 ODER 2") || name.contains("1/2")) {
                outcome = MatchResultBet.Outcome.DC_12;
            } else if (name.equals("X2") || name.contains("X OU 2") || name.contains("X OR 2") || name.contains("X ODER 2") || name.contains("X/2")) {
                outcome = MatchResultBet.Outcome.DC_X2;
            }

            if (outcome != null) {
                OddItem item = createOddItem(selection, market.getResolvedName(),
                        new MatchResultBet(scope, outcome, null));
                if (item != null) items.add(item);
            }
        }
        return items;
    }
}
