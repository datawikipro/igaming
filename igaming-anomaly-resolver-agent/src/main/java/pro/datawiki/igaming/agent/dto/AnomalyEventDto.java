package pro.datawiki.igaming.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnomalyEventDto implements Serializable {

    private String externalSystemCode;
    private String entityType;
    private String rawKey;
    private String rawName;
    private String rawCategory;
    private String rawParentKey;
    private OffsetDateTime detectedAt;
    private String details;
}
