package pro.datawiki.igaming.playerfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;

/**
 * Result details from a headshot harvesting operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HarvestResultDto {

    private String playerName;
    private boolean success;
    private String avatarUrl;
    private AvatarSourceProvider provider;
    private String message;
    private long durationMs;
}
