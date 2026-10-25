package pro.datawiki.igaming.mdm.dto;

import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnmappedAnomalyEvent implements Serializable {

    private String externalSystemCode;

    /**
     * SPORT, TOURNAMENT, TEAM, MATCH
     */
    private String entityType;

    private String rawKey;
    private String rawName;
    private String rawCategory;
    private String rawParentKey;

    @Builder.Default
    private OffsetDateTime detectedAt = OffsetDateTime.now();

    private String details;
}
