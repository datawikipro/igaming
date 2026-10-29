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
public class ResolveTournamentMappingRequest implements Serializable {
    private Long queueItemId;
    private String externalSystemCode;
    private String rawKey;
    private String sourceId;
    private Long canonicalTournamentId;
    private String rawName;
    private String resolutionNotes;
}
