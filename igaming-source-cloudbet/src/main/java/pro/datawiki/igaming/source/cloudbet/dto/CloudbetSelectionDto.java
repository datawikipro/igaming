package pro.datawiki.igaming.source.cloudbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudbetSelectionDto {
    private String outcome;
    private Double price;
    private Double params;
    private Double line;
    private Double handicap;
    private String status;

    public Double getEffectivePrice() {
        return price;
    }

    public Double getEffectiveParam() {
        if (params != null) return params;
        if (line != null) return line;
        if (handicap != null) return handicap;
        return null;
    }
}
