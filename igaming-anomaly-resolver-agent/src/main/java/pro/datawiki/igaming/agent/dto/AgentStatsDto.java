package pro.datawiki.igaming.agent.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AgentStatsDto {
    private long totalEventsReceived;
    private long scheduleCollisionsProcessed;
    private long empiricalAuditsCount;
    private long duplicateMergesCount;
    private long trashOutrightsCancelledCount;
    private long unmappedAliasesResolvedCount;
    private long syntheticMatchesCancelled;
    private long groundTruthMatchesPromoted;
    private long confirmedCollisionsCount;
    private long unmappedTournamentsDetected;
    private long errorsCount;
    private int planeWorkerPoolTotal;
    private int planeWorkerPoolHealthy;
    private Instant lastActiveTimestamp;
}
