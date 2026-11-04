package pro.datawiki.igaming.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditCollisionRequest {
    private Long matchId1;
    private Long matchId2;
    private String conflictTeam;
    private String details;
}
