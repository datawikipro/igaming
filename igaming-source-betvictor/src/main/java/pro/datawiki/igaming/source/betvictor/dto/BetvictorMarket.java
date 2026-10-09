package pro.datawiki.igaming.source.betvictor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetvictorMarket {
    private Long id;
    private String name;
    private String description;
    
    @JsonProperty("type")
    private String type;

    @JsonProperty("market_type")
    private String marketType;

    private String period;

    @JsonProperty("outcomes")
    private List<BetvictorOutcome> outcomes;

    public String getResolvedName() {
        if (description != null && !description.isBlank()) return description;
        if (name != null && !name.isBlank()) return name;
        return "";
    }

    public String getResolvedType() {
        if (marketType != null && !marketType.isBlank()) return marketType;
        if (type != null && !type.isBlank()) return type;
        return "";
    }
}
