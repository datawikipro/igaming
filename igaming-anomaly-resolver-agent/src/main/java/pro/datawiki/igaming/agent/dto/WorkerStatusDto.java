package pro.datawiki.igaming.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkerStatusDto {
    private Integer podIndex;
    private String podName;
    private String email;
    private String status; // "IDLE", "BUSY", etc.
    private Double geminiQuota;
    private Double gemini5hQuota;
    private Double geminiWeeklyQuota;
    private Double claudeQuota;
    private Boolean hasEnoughTokens;
    private String quotaMessage;
    private String lastError;
}
