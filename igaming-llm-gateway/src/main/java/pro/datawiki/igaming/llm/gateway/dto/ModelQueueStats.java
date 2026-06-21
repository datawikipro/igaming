package pro.datawiki.igaming.llm.gateway.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelQueueStats {
    private String modelName;
    private String providerType;
    private long pendingCount;
    private long processingCount;
    private long completedCount;
    private long failedCount;
    private long processedPerHour;
}
