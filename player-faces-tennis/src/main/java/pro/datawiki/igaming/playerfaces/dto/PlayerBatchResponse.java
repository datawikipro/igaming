package pro.datawiki.igaming.playerfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response payload returning resolved player face avatars for requested names.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerBatchResponse {

    private Map<String, PlayerFaceDto> players;
    private int totalRequested;
    private int totalResolved;
    private int totalGenerated;
}
