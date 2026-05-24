package pro.datawiki.igaming.llm.domain;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmTask {
    private UUID id;
    private String providerType;
    private Long modelId;
    private String modelName;
    private String promptHash;
    private String prompt;
    private String systemPrompt;
    private Double temperature;
    private Integer maxTokens;
    private String userId;
    private String metadata;
    private boolean permanent;
    private int ttlHours;
    private String status;
    private String resultText;
    private Integer tokensUsed;
    private String finishReason;
    private String errorMessage;
    private boolean cached;
    private String workerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
