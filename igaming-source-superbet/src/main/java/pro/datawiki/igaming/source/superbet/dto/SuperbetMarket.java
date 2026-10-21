package pro.datawiki.igaming.source.superbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperbetMarket {
    private Long id;
    private String name;
    private String code;
    @JsonProperty("marketId")
    private Long marketId;
    @JsonProperty("specifiers")
    private Object specifiers;
    private List<SuperbetOutcome> odds;
    private List<SuperbetOutcome> outcomes;

    public List<SuperbetOutcome> getResolvedOutcomes() {
        if (odds != null && !odds.isEmpty()) {
            return odds;
        }
        if (outcomes != null && !outcomes.isEmpty()) {
            return outcomes;
        }
        return Collections.emptyList();
    }
}
