package pro.datawiki.igaming.source.bovada.strategy;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.bovada.dto.BovadaMarketDto;

import java.util.List;

public interface BovadaMarketStrategy {
    default boolean supports(BovadaMarketDto market, String team1, String team2) {
        return supports(market);
    }
    boolean supports(BovadaMarketDto market);
    List<OddItem> map(BovadaMarketDto market, SportType sportType, String team1, String team2);
}
