package pro.datawiki.igaming.k8s.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmakerIncident {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("category")
    private String category; // BOOKMAKER_STALE, SUPER_ARB_10PLUS, LOW_DATA_VOLUME, HIGH_INGESTION_LAG, CPU_RAM_OVERLOAD, SOCIAL_MEDIA_STALE

    @JsonProperty("target")
    private String target; // bookmaker id, node name, etc.

    @JsonProperty("severity")
    private String severity; // CRITICAL, WARNING, INFO

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("currentValue")
    private String currentValue;

    @JsonProperty("thresholdValue")
    private String thresholdValue;

    @JsonProperty("status")
    private String status; // OPEN, RESOLVED

    @JsonProperty("planeIssueId")
    private String planeIssueId;

    @JsonProperty("planeIssueUrl")
    private String planeIssueUrl;

    @JsonProperty("aiTaskId")
    private Long aiTaskId;

    @JsonProperty("detectedAt")
    private Instant detectedAt;

    @JsonProperty("lastSeenAt")
    private Instant lastSeenAt;

    @JsonProperty("resolvedAt")
    private Instant resolvedAt;
}
