package pro.datawiki.igaming.llm.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompleteRequest {
    private UUID taskId;
    private String workerId;
    private String status;
    private String resultText;
    private Integer tokensUsed;
    private String finishReason;
    private String errorMessage;
}
