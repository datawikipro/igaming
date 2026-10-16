package pro.datawiki.igaming.capture.sofascore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Result metrics for a completed or ongoing historical backfill operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackfillResult {

    private String status; // COMPLETED, IN_PROGRESS, FAILED, INTERRUPTED
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> sports;
    private int totalDatesProcessed;
    private int totalEventsIngested;
    private int errorsCount;
    private long durationMs;
    private String message;
}
