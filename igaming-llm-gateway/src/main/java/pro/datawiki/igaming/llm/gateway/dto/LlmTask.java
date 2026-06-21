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
public class LlmTask {
    private UUID id;
    private String providerType;
    private String modelName;
    private String prompt;
    private String systemPrompt;
    private Double temperature;
    private Integer maxTokens;
    private String apiKey; // В шлюзе мы подставляем API-ключ прямо сюда перед отправкой воркеру!
    private String logicalType;
    private String urgency;
}
