package pro.datawiki.igaming.source.apuestatotal.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

import java.util.List;

public interface ApuestatotalMarketHandler {
    boolean supports(ApuestatotalStakeGroupData group, SportType sportType);
    void handle(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items);
}
