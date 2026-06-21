package pro.datawiki.igaming.llm.gateway.dto.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyConfigDto {
    private Long id;
    private String label;
    private String apiKey;
    private boolean active;
}
