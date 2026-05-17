package pro.datawiki.igaming.llm.admin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SupportedProviderResponse {
    private String name;
    private String displayName;
    private List<String> models;
}
