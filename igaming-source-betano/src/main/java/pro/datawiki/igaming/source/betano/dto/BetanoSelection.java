package pro.datawiki.igaming.source.betano.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetanoSelection {
    private String id;
    private String name;
    
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("odds")
    private Double odds;

    @JsonProperty("decimalOdds")
    private Double decimalOdds;

    private Double handicap;
    
    @JsonProperty("line")
    private Double line;

    @JsonProperty("type")
    private String type;

    @JsonProperty("code")
    private String code;

    public Double getResolvedPrice() {
        if (price != null) return price;
        if (odds != null) return odds;
        if (decimalOdds != null) return decimalOdds;
        return null;
    }

    public String getResolvedName() {
        if (name != null && !name.isBlank()) return name;
        if (code != null && !code.isBlank()) return code;
        if (type != null && !type.isBlank()) return type;
        return "";
    }

    public Double getResolvedHandicap() {
        if (handicap != null) return handicap;
        return line;
    }
}
