package pro.datawiki.igaming.capture.sofascore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Live status report for asynchronous backfill jobs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackfillProgressDto {

    private boolean running;
    private String currentSport;
    private LocalDate currentDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> sports;
    private int totalDays;
    private int daysProcessed;
    private int eventsIngested;
    private int errorsCount;
    private Instant startedAt;
    private Long durationSeconds;
    private String lastMessage;
}
