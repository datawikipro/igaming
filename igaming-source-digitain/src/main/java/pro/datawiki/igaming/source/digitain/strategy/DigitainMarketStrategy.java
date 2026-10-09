package pro.datawiki.igaming.source.digitain.strategy;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.List;

public interface DigitainMarketStrategy {

    default boolean supports(DigitainStakeGroupData group, String team1, String team2) {
        return supports(group);
    }

    boolean supports(DigitainStakeGroupData group);

    List<OddItem> map(DigitainStakeGroupData group, SportType sportType, String team1, String team2, MatchCache cached);
}
