package pro.datawiki.igaming.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Diagnostic result from probing a bookmaker crawler.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerProbeResultDto {
    private String bookmakerId;
    private String status;               // SUCCESS, DEGRADED, FAILED
    private Long responseTimeMs;
    private Long timestamp;
    private String message;
    private Long currentMatches;
    private Long currentOdds;
}
