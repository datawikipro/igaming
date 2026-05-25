package pro.datawiki.igaming.llm.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerRegistrationRequest {
    private String workerName;
    private String providerType;
    private String modelName;
    private String podIp;
    private String failedApiKey;
    private String failureReason;
}

