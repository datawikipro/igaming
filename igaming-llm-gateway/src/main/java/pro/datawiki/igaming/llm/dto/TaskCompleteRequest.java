package pro.datawiki.igaming.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Воркер вызывает POST /api/v1/worker/complete с этим payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompleteRequest {
    private UUID taskId;
    private String workerId;
    private String status;         // "COMPLETED" или "FAILED"
    private String resultText;
    private Integer tokensUsed;
    private String finishReason;
    private String errorMessage;
}
