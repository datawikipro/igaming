package pro.datawiki.igaming.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.dto.LoaderDelayDto;

import java.util.List;

/**
 * Aggregated operational metrics for the Ingestion Pipeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStatsDto {
    private Long matches;
    private Long teams;
    private Long normalizationRequests;
    private Long pendingNormalization;
    private Long unrecognizedOdds;
    private Long totalActiveOdds;
    private Integer totalBookmakers;
    private Integer onlineBookmakers;
    private Integer compliantBookmakers; // matches >= 500 (Golden Rule #8)
    private Integer degradedBookmakers;  // matches 1-499 or delay > 5m
    private Integer defectBookmakers;    // matches == 0
    private Double complianceRate;       // % compliant of online
    private String pipelineStatus;       // HEALTHY, DEGRADED, DOWN
    private String kafkaTopic;           // "odds.updates"
    private String kafkaStatus;          // "STREAMING", "CONNECTED"
    private Long timestamp;
    private List<LoaderDelayDto> loaderDelays;
}
