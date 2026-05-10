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
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private String userId;
    private Map<String, Object> metadata;
}
