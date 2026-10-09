package pro.datawiki.igaming.source.mozzartbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MozzartMarket {
    private Long id;
    private String name;
    private String code;
    @JsonProperty("subgameId")
    private Long subgameId;
    @JsonProperty("param")
    private Double param;
    @JsonProperty("games")
    private List<MozzartOutcome> outcomes;
    @JsonProperty("odds")
    private List<MozzartOutcome> odds;

    public List<MozzartOutcome> getResolvedOutcomes() {
        if (outcomes != null && !outcomes.isEmpty()) return outcomes;
        return odds;
    }
}
