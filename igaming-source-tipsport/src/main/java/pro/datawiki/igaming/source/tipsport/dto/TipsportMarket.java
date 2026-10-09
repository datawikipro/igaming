package pro.datawiki.igaming.source.tipsport.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipsportMarket {
    private Long id;
    private String name;
    @JsonProperty("marketType")
    private String marketType;
    @JsonProperty("type")
    private String type;
    @JsonProperty("param")
    private Double param;
    @JsonProperty("outcomes")
    private List<TipsportOutcome> outcomes;
    @JsonProperty("opportunities")
    private List<TipsportOutcome> opportunities;

    public List<TipsportOutcome> getResolvedOutcomes() {
        if (outcomes != null && !outcomes.isEmpty()) return outcomes;
        return opportunities;
    }
}
