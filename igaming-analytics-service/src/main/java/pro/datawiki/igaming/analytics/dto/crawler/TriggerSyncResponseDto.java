package pro.datawiki.igaming.analytics.dto.crawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Result of on-demand sync trigger for a bookmaker.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerSyncResponseDto implements Serializable {
    private String bookmakerCode;
    private boolean triggered;
    private String message;
    private String dispatchedAt;
}
