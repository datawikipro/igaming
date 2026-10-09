package pro.datawiki.igaming.source.novig.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NovigOutcomeDto {
    private String id;
    private String description;
    private String type;

    /**
     * Probability between 0.0 and 1.0 (e.g. 0.52 = 52%).
     * Decimal odds = 1.0 / available.
     */
    private Double available;

    private Double altAvailable;
    private Double last;

    public Double getDecimalOdds() {
        if (available == null || available <= 0.0 || available >= 1.0) {
            return null;
        }
        return Math.round((1.0 / available) * 1000.0) / 1000.0;
    }
}
