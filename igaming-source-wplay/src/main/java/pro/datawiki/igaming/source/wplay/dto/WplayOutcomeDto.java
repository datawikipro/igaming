package pro.datawiki.igaming.source.wplay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WplayOutcomeDto {
    private String id;
    private String name;
    private Double price;
    private Double param;
    private String selectionType;

    public Double getEffectiveOdds() {
        return price != null ? price : 0.0;
    }

    public Double getEffectiveParam() {
        return param;
    }
}
