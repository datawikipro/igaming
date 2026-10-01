package pro.datawiki.igaming.source.vaidebet.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeGroupData;

import java.util.List;

public interface VaidebetMarketHandler {
    boolean supports(VaidebetStakeGroupData group, SportType sportType);
    void handle(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items);
}
