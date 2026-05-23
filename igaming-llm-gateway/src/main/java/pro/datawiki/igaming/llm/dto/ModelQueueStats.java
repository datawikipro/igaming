package pro.datawiki.igaming.llm.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelQueueStats {
    private Long modelId;
    private String modelName;
    private String displayName;
    private String providerType;
    private long pendingCount;
    private long processingCount;
    private long completedCount;
    private long failedCount;
    private long processedPerHour;
}
