package pro.datawiki.igaming.capture.sofascore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Task item transferred via Redis queues from SofaScore Crawler to Loader.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofaTaskDto implements Serializable {

    public enum TaskType {
        LIVE,
        PREMATCH,
        HISTORY,
        ON_DEMAND
    }

    private String taskId;
    private Long eventId;
    private String sport;
    private TaskType type;
    private Long matchDate;
    private String homeTeam;
    private String awayTeam;
    private Long tournamentId;
    private String tournamentName;
    private String categoryName;
    private Long homeTeamId;
    private Long awayTeamId;
    private Long aggregatorMatchId; // if matched to existing match_record
    private int priority;           // 0 = highest (ON_DEMAND / LIVE), 1 = PREMATCH, 2 = HISTORY
    private long queuedAt;
}
