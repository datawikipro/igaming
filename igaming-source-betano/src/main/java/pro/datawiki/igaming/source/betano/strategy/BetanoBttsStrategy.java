package pro.datawiki.igaming.source.betano.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.source.betano.dto.BetanoMarket;
import pro.datawiki.igaming.source.betano.dto.BetanoSelection;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class BetanoBttsStrategy extends AbstractBetanoStrategy {

    @Override
    public boolean supports(BetanoMarket market) {
        String name = market.getResolvedName().toLowerCase(Locale.ROOT);
        String type = market.getResolvedType().toUpperCase(Locale.ROOT);

        return type.contains("BTTS") || name.contains("ambas as equipas marcam")
                || name.contains("ambas marcam") || name.contains("both teams to score")
                || name.contains("ambas as equipes marcam") || name.contains("beide teams treffen");
    }

    @Override
    public List<OddItem> map(BetanoMarket market, SportType sportType, String team1, String team2, MatchCache cached) {
        List<OddItem> items = new ArrayList<>();
        List<BetanoSelection> selections = market.getResolvedSelections();
        if (selections == null || selections.isEmpty()) return items;

        BetScope scope = BetanoScopeResolver.resolve(market.getResolvedName(), market.getResolvedType(), sportType);

        for (BetanoSelection selection : selections) {
            String name = selection.getResolvedName().toLowerCase(Locale.ROOT).trim();
            BinaryMarketBet.Outcome outcome = null;

            if (name.equals("sim") || name.equals("yes") || name.equals("s") || name.equals("y") || name.equals("ja") || name.equals("j")) {
                outcome = BinaryMarketBet.Outcome.YES;
            } else if (name.equals("não") || name.equals("nao") || name.equals("no") || name.equals("n") || name.equals("nein")) {
                outcome = BinaryMarketBet.Outcome.NO;
            }

            if (outcome != null) {
                OddItem item = createOddItem(selection, market.getResolvedName(),
                        new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, outcome, null));
                if (item != null) items.add(item);
            }
        }
        return items;
    }
}
