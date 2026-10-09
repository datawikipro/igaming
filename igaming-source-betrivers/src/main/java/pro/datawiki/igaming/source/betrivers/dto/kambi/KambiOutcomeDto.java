package pro.datawiki.igaming.source.betrivers.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiOutcomeDto {
    private Long id;
    private String label;
    private String englishLabel;
    private Integer odds; // e.g. 1950 for 1.95 decimal
    private String oddsAmerican; // e.g. "-105"
    private String type; // OT_ONE, OT_DRAW, OT_TWO, OT_OVER, OT_UNDER, OT_UNTYPED
    private Double line;
    private String status; // OPEN, SUSPENDED
}
