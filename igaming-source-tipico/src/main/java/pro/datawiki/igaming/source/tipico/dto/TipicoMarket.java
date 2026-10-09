package pro.datawiki.igaming.source.tipico.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipicoMarket {
    private Long id;
    private String name;
    private String title;
    private String caption;
    
    @JsonProperty("marketType")
    private String marketType;

    @JsonProperty("type")
    private String type;

    @JsonProperty("outcomes")
    private List<TipicoOutcome> outcomes;

    @JsonProperty("results")
    private List<TipicoOutcome> results;

    public String getResolvedName() {
        if (caption != null && !caption.isBlank()) return caption;
        if (title != null && !title.isBlank()) return title;
        if (name != null && !name.isBlank()) return name;
        return "";
    }

    public String getResolvedType() {
        if (marketType != null && !marketType.isBlank()) return marketType;
        if (type != null && !type.isBlank()) return type;
        return "";
    }

    public List<TipicoOutcome> getResolvedOutcomes() {
        if (outcomes != null && !outcomes.isEmpty()) return outcomes;
        return results;
    }
}
