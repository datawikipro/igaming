package pro.datawiki.igaming.source.draftkings.service.mapper;

import lombok.Builder;
import lombok.Getter;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.draftkings.dto.DraftKingsEventGroupResponse;

@Getter
@Builder
public class DraftKingsMarketContext {
    private final DraftKingsEventGroupResponse.DraftKingsEvent event;
    private final String categoryName;
    private final String marketName;
    private final DraftKingsEventGroupResponse.DraftKingsOffer offer;
    private final DraftKingsEventGroupResponse.DraftKingsOutcome outcome;
    private final SportType sportType;
    private final String sportName;
    private final String leagueName;
    private final String team1;
    private final String team2;
    private final double decimalOdds;
    private final Double line;
    private final String runnerName;
    private final BetScope scope;
    private final StatType statType;
}
