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
public class LlmSubmitResponse {
    private UUID taskId;
    private String status;
    private boolean cached;
    private String text;
    private Integer tokensUsed;
    private String finishReason;
}
