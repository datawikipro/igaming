package pro.datawiki.igaming.llm.gateway.dto.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderConfigDto {
    private Long id;
    private String name;
    private boolean active;
    private List<KeyConfigDto> keys;
}
