package pro.datawiki.igaming.analyst.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectedLineupDto implements Serializable {

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("formation")
    private String formation;

    @JsonProperty("starters")
    private List<ProjectedPlayerDto> starters;

    @JsonProperty("bench")
    private List<ProjectedPlayerDto> bench;

    @JsonProperty("confidence_pct")
    private Double confidencePct;

    @JsonProperty("contested_duels")
    private List<String> contestedDuels;
}
