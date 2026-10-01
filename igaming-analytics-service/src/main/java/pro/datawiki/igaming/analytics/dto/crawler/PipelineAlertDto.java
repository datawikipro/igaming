package pro.datawiki.igaming.analytics.dto.crawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Operational alert for pipeline threshold violations and crawler degradation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineAlertDto implements Serializable {
    private String id;
    private String bookmakerCode;
    private String bookmakerName;
    private String severity; // CRITICAL, WARNING, INFO
    private String message;
    private String rule;
    private String timestamp;
}
