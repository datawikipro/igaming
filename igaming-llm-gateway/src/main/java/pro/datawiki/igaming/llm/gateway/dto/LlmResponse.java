package pro.datawiki.igaming.llm.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {
    private String text;
    private String model;
    private String provider;
    private Integer tokensUsed;
    private String finishReason;
    private LlmMetadataDto metadata;
}
