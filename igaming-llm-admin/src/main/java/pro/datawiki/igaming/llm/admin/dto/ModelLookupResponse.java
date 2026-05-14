package pro.datawiki.igaming.llm.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ на запрос gateway: "какой провайдер обслуживает модель X?"
 * GET /api/v1/admin/models/lookup?modelId=deepseek-chat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelLookupResponse {
    private Long modelId;
    private String modelName;       // "deepseek-chat"
    private String displayName;     // "DeepSeek Chat"
    private Long providerId;
    private String providerName;    // "deepseek"
    private String providerDisplay; // "DeepSeek AI"
}
