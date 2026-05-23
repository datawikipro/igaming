package pro.datawiki.igaming.llm.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskGroupCount {
    private Long modelId;
    private String modelName;
    private String displayName;
    private String providerType;
    private String status;
    private long count;
}
