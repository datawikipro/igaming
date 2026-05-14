package pro.datawiki.igaming.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Локальная копия ModelLookupResponse для gateway.
 * Зеркало pro.datawiki.igaming.llm.admin.dto.ModelLookupResponse,
 * чтобы не создавать зависимость на admin-jar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelLookupResponse {
    private Long modelId;
    private String modelName;
    private String displayName;
    private Long providerId;
    private String providerName;
    private String providerDisplay;
}
