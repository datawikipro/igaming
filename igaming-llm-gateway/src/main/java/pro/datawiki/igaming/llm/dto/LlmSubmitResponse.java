package pro.datawiki.igaming.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Ответ на POST /api/v1/llm/submit — возвращает ID задачи для async polling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmSubmitResponse {
    private UUID taskId;
    private String status;  // "PENDING" или "COMPLETED" (если кеш)
    private boolean cached;
    private String text;    // заполнено только если cached=true
    private Integer tokensUsed;
    private String finishReason;
}
