package pro.datawiki.igaming.analytics.dto.crawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Configuration of threshold rules for line volume monitoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdRuleDto implements Serializable {
    private String bookmakerCode;         // "GLOBAL" or specific code
    private int minMatchesThreshold;       // Default: 500
    private int warningMatchesThreshold;   // Default: 300
    private int maxStaleSeconds;          // Default: 180
    private String updatedAt;
}
