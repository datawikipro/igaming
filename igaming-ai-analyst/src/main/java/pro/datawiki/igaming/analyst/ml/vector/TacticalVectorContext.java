package pro.datawiki.igaming.analyst.ml.vector;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Tactical Context DTO passed to sport-specific vector extractors.
 */
@Getter
@Builder
@ToString
public class TacticalVectorContext {
    private final Long teamId;
    private final String teamName;
    private final Long opponentId;
    private final String opponentName;
    private final long matchTimestamp;
    private final List<Map<String, Object>> recentMatches;
    private final List<Map<String, Object>> starters;
    private final List<Map<String, Object>> bench;
}
