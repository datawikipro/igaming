package pro.datawiki.igaming.source.caliente.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalienteOutcomeDto {

    private String id;
    private String name;
    private Double odds;
    private Double price;
    private Double decimal;
    private Double handicap;
    private String outcomeType;
    private String status;

    public Double getEffectiveOdds() {
        if (decimal != null && decimal > 1.0) return decimal;
        if (odds != null && odds > 1.0) return odds;
        if (price != null && price > 1.0) return price;
        return null;
    }
}
