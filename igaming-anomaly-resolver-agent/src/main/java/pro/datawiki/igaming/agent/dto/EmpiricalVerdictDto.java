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
public class EmpiricalVerdictDto {
    private EmpiricalVerdictType verdictType;
    private Long winnerMatchId;
    private Long loserMatchId;
    private Long canonicalSofaId;
    private String teamAliasRaw;
    private Long canonicalTeamId;
    private String rationale;
    private Double confidence;
    private String recommendedAction;
    private String workerPod;
    private Double durationSeconds;
}
