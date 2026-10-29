package pro.datawiki.igaming.mdm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnmappedQueueItemDto implements Serializable {
    private Long id;
    private String externalSystemCode;
    private String entityType;
    private String rawKey;
    private String sourceId;
    private String rawName;
    private Integer sportId;
    private String sportSlug;
    private Integer countryId;
    private String countryName;
    private String contextParentKey;
    private String contextParentName;
    private String status;
    private String resolutionNotes;
    private OffsetDateTime createdAt;
}
