package pro.datawiki.igaming.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.dto.BookmakerLeagueStatDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Detailed Golden Rule #8 threshold compliance assessment for a bookmaker.
 * Threshold is fixed at >= 500 active matches.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmakerThresholdDto {
    private String id;
    private String name;
    private String logoEmoji;
    private String engine;
    private String proxyRoute;
    private Set<String> regions;
    private Boolean isOnline;
    private Long lastSeen;
    private Long matchesCount;
    private Long targetThreshold;        // 500 (Golden Rule #8)
    private Double thresholdProgress;    // 0.0 - 100.0%
    private String complianceStatus;     // COMPLIANT (>=500), DEGRADED (1-499), DEFECT (0)
    private Long oddsCount;
    private Double delayMinutes;
    private String freshnessStatus;      // FRESH (<1m), NORMAL (1-5m), DELAYED (5-15m), STALE (>15m)
    private String healthStatus;         // HEALTHY, DEGRADED, OFFLINE
    private String healthMessage;
    private Map<String, Long> sportBreakdown;
    private List<BookmakerLeagueStatDto> topLeagues;
}
