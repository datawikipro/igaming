package pro.datawiki.igaming.mdm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveTeamMappingRequest implements Serializable {
    private Long queueItemId;
    private String externalSystemCode;
    private String rawKey;
    private String sourceId;
    private Long canonicalTeamId;
    private Long tournamentId; // optional: to establish league membership in mdm_tournament_team
    private String rawName;
    private String resolutionNotes;
}
