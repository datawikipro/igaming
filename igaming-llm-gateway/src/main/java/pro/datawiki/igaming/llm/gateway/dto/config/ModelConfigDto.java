package pro.datawiki.igaming.llm.gateway.dto.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigDto {
    private Long id;
    private String modelId; // e.g. "gemini-1.5-flash"
    private String name;    // Human readable
    private boolean active;
    private Long providerId;
}
