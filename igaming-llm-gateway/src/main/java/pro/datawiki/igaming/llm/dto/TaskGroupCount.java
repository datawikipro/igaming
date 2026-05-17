package pro.datawiki.igaming.llm.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskGroupCount {
    private String modelName;
    private String providerType;
    private String status;
    private long count;
}
