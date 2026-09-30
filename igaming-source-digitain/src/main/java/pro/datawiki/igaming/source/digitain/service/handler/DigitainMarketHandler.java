package pro.datawiki.igaming.source.digitain.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.List;

public interface DigitainMarketHandler {
    boolean supports(DigitainStakeGroupData group, SportType sportType);
    void handle(DigitainStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items);
}
