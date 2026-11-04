package pro.datawiki.igaming.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditCollisionResponse {
    private boolean success;
    private Long matchId1;
    private Long matchId2;
    private EmpiricalVerdictType verdictType;
    private Long winnerMatchId;
    private Long loserMatchId;
    private String rationale;
    private String workerPod;
    private Double durationSeconds;
    private String remediationStatus;
}
