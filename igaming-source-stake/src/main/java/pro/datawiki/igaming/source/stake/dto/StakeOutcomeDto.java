package pro.datawiki.igaming.source.stake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StakeOutcomeDto {
    private String id;
    private String name;
    private Double odds;
    private Boolean active;
    private Double line;
    private Double handicap;
    private Double total;

    public Double getEffectiveOdds() {
        return odds;
    }

    public Double getEffectiveParam() {
        if (line != null) return line;
        if (handicap != null) return handicap;
        if (total != null) return total;
        return null;
    }
}
