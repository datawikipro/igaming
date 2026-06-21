package pro.datawiki.igaming.llm.gateway.dto.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRuleConfigDto {
    private Long id;
    private String logicalType;
    private Boolean googleRequired;
    private String urgency;
    private boolean active;
    private ModelConfigDto targetModel;
}
