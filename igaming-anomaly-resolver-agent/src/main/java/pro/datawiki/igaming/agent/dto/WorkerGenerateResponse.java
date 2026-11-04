package pro.datawiki.igaming.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkerGenerateResponse {
    private String status; // "ok", "error", "timeout"
    private String response;
    private String error;
    private Double durationSeconds;
    private String pod;
}
