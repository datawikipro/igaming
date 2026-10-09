package pro.datawiki.igaming.source.tipico.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipicoOutcome {
    private Long id;
    private String name;
    private String caption;
    
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("decimalOdds")
    private Double decimalOdds;

    @JsonProperty("fixedPrice")
    private Double fixedPrice;

    @JsonProperty("quote")
    private Double quote;

    private Double handicap;
    
    @JsonProperty("param")
    private Double param;

    @JsonProperty("code")
    private String code;

    @JsonProperty("type")
    private String type;

    public Double getResolvedPrice() {
        if (price != null) return price;
        if (decimalOdds != null) return decimalOdds;
        if (quote != null) return quote;
        if (fixedPrice != null) return fixedPrice;
        return null;
    }

    public String getResolvedName() {
        if (caption != null && !caption.isBlank()) return caption;
        if (name != null && !name.isBlank()) return name;
        if (code != null && !code.isBlank()) return code;
        return "";
    }

    public Double getResolvedHandicap() {
        if (handicap != null) return handicap;
        return param;
    }
}
