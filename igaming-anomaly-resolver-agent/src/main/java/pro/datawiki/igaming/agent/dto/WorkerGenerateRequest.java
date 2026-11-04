package pro.datawiki.igaming.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerGenerateRequest {
    private String prompt;
    private String model;
    private Integer timeout;
}
