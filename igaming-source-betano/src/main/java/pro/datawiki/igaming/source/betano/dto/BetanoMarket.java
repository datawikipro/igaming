package pro.datawiki.igaming.source.betano.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetanoMarket {
    private String id;
    private String name;
    
    @JsonProperty("type")
    private String type;

    @JsonProperty("marketType")
    private String marketType;

    @JsonProperty("selections")
    private List<BetanoSelection> selections;

    @JsonProperty("outcomes")
    private List<BetanoSelection> outcomes;

    public String getResolvedName() {
        if (name != null && !name.isBlank()) return name;
        return "";
    }

    public String getResolvedType() {
        if (marketType != null && !marketType.isBlank()) return marketType;
        if (type != null && !type.isBlank()) return type;
        return "";
    }

    public List<BetanoSelection> getResolvedSelections() {
        if (selections != null && !selections.isEmpty()) return selections;
        return outcomes;
    }
}
