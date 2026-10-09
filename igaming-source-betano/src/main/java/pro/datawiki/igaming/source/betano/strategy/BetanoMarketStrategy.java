package pro.datawiki.igaming.source.betano.strategy;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betano.dto.BetanoMarket;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.List;

public interface BetanoMarketStrategy {

    default boolean supports(BetanoMarket market, String team1, String team2) {
        return supports(market);
    }

    boolean supports(BetanoMarket market);

    List<OddItem> map(BetanoMarket market, SportType sportType, String team1, String team2, MatchCache cached);
}
