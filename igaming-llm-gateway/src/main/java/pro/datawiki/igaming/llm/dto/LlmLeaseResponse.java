package pro.datawiki.igaming.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmLeaseResponse {
    private String name;
    private String endpointUrl;
    private String providerType;
    private String modelName;
    private String apiKey;
}
