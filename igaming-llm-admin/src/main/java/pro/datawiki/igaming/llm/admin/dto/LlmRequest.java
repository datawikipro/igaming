package pro.datawiki.igaming.llm.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {
    private String prompt;
    private String systemPrompt;
    private String model;           // e.g. "deepseek-chat", "gemini-3-flash"
    private String providerType;    // e.g. "deepseek", "gemini"
    private Double temperature;
    private Integer maxTokens;
    private String userId;

    @Builder.Default
    private boolean permanent = false;

    @Builder.Default
    private int ttlHours = 24;

    private Map<String, Object> metadata;

    private String logicalType;
    private Boolean useSearch;
    private String urgency;
}
