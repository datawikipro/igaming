package pro.datawiki.igaming.capture.sofascore.stats;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Canonical payload published to Kafka topic {@code sports.canonical.advanced-metrics}.
 * Encapsulates verified institutional analytics for Ice Hockey (KHL / NHL) and Basketball (NBA / EuroLeague).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CanonicalAdvancedMetricsEvent implements Serializable {

    public static final String TOPIC_NAME = "sports.canonical.advanced-metrics";

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("provider")
    private String provider; // e.g. "SOFASCORE", "NHL_API", "KHL_OFFICIAL", "NBA_API", "EUROLEAGUE_API"

    @JsonProperty("sport")
    private String sport; // "ICE_HOCKEY" or "BASKETBALL"

    @JsonProperty("league_name")
    private String leagueName; // "KHL", "NHL", "NBA", "EuroLeague"

    @JsonProperty("match_external_id")
    private String matchExternalId;

    @JsonProperty("match_start_time_epoch_ms")
    private Long matchStartTimeEpochMs;

    @JsonProperty("team1_name")
    private String team1Name;

    @JsonProperty("team2_name")
    private String team2Name;

    @JsonProperty("status")
    private String status; // "IN_PROGRESS", "FINISHED"

    @JsonProperty("hockey_metrics")
    private IceHockeyAdvancedMetrics hockeyMetrics;

    @JsonProperty("basketball_metrics")
    private BasketballAdvancedMetrics basketballMetrics;

    @JsonProperty("additional_metrics")
    private Map<String, Object> additionalMetrics;

    @JsonProperty("captured_at_epoch_ms")
    private Long capturedAtEpochMs;
}
