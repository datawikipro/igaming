package pro.datawiki.igaming.analyst.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelVersionDto {

    @JsonProperty("version_code")
    private String versionCode;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("ideology_version")
    private String ideologyVersion;

    @JsonProperty("vector_version")
    private String vectorVersion;

    @JsonProperty("loader_version")
    private String loaderVersion;

    @JsonProperty("iteration_number")
    private Integer iterationNumber;

    @JsonProperty("release_date")
    private LocalDateTime releaseDate;

    @JsonProperty("is_active")
    private boolean isActive;

    @JsonProperty("description")
    private String description;

    @JsonProperty("changelog")
    private String changelog;

    // Live Aggregated Performance Metrics
    @JsonProperty("total_picks")
    private Long totalPicks;

    @JsonProperty("won_count")
    private Long wonCount;

    @JsonProperty("lost_count")
    private Long lostCount;

    @JsonProperty("void_count")
    private Long voidCount;

    @JsonProperty("winrate_pct")
    private Double winratePct;

    @JsonProperty("total_pnl_rub")
    private BigDecimal totalPnlRub;

    @JsonProperty("roi_pct")
    private Double roiPct;
}
