package pro.datawiki.igaming.agent.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AgentStatsDto {
    private long totalEventsReceived;
    private long scheduleCollisionsProcessed;
    private long syntheticMatchesCancelled;
    private long groundTruthMatchesPromoted;
    private long unmappedTournamentsDetected;
    private long errorsCount;
    private Instant lastActiveTimestamp;
}
