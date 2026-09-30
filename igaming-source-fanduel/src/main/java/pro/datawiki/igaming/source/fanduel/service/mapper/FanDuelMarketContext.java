package pro.datawiki.igaming.source.fanduel.service.mapper;

import lombok.Builder;
import lombok.Getter;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.fanduel.dto.FanDuelEventGroupResponse;

@Getter
@Builder
public class FanDuelMarketContext {
    private final FanDuelEventGroupResponse.FanDuelEvent event;
    private final FanDuelEventGroupResponse.FanDuelMarket market;
    private final String marketName;
    private final String runnerName;
    private final String selectionId;
    private final SportType sportType;
    private final String sportName;
    private final String leagueName;
    private final String team1;
    private final String team2;
    private final double decimalOdds;
    private final Double line;
    private final BetScope scope;
    private final StatType statType;
}
