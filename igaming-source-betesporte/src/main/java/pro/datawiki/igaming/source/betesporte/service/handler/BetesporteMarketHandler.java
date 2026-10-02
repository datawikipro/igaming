package pro.datawiki.igaming.source.betesporte.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteStakeGroupData;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.List;

public interface BetesporteMarketHandler {
    boolean supports(BetesporteStakeGroupData group, SportType sportType);
    void handle(BetesporteStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items);
}
