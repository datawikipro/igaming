package pro.datawiki.igaming.llm.admin.dto;

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
    private String status;  // "PENDING" or "COMPLETED" (cache hit)
    private boolean cached;
    private String text;    // populated only if cached=true
    private Integer tokensUsed;
    private String finishReason;
}
