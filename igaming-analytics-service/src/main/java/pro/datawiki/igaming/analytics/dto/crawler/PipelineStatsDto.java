package pro.datawiki.igaming.analytics.dto.crawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Aggregated summary statistics across all 52 ingestion pipelines.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStatsDto implements Serializable {
    private int totalPipelines;
    private int activePipelines;
    private int thresholdMetCount;       // activeMatches >= 500
    private int thresholdWarnCount;      // 300 <= activeMatches < 500
    private int thresholdFailCount;      // activeMatches < 300
    private int totalActiveMatches;
    private double overallIngestionRate; // events/sec
    private double rule8CompliancePercent;
}
