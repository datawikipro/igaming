package pro.datawiki.igaming.source.bcgame.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BcgameOutcomeDto {
    private String id;
    private String name;
    private Double odds;
    private Double price;
    private Boolean active;
    private Double param;
    private Double handicap;
    private Double total;

    public Double getEffectiveOdds() {
        return odds != null ? odds : price;
    }

    public Double getEffectiveParam() {
        if (param != null) return param;
        if (handicap != null) return handicap;
        if (total != null) return total;
        return null;
    }
}
