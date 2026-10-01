package pro.datawiki.igaming.analytics.dto.crawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Representation of an individual bookmaker's crawler ingestion pipeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerPipelineDto implements Serializable {
    private String bookmakerCode;
    private String bookmakerName;
    private String region;            // RU_CUPIS, EU_OFFSHORE, US, LATAM
    private String routingType;       // DIRECT_SPB, OUTLINE_EU_NL, OUTLINE_US
    private String stealthProfile;    // BASIC, HEADLESS_STEALTH, XVFB_HEADED
    private int activeMatches;
    private int minThreshold;         // Default: 500 (Rule 8)
    private String thresholdStatus;   // PASS, WARN, FAIL
    private double eventsPerSecond;
    private long avgLatencyMs;
    private String lastIngestTimestamp;
    private String status;            // RUNNING, DEGRADED, STALLED, OFFLINE
    private int activeLeaguesCount;
    private long errorCount;
}
