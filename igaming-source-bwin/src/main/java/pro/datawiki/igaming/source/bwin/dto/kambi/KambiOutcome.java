package pro.datawiki.igaming.source.bwin.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiOutcome {
    private Long id;
    private String label;
    private Integer odds; // e.g. 1500 for 1.50
    private String type; // e.g. "OT_ONE", "OT_DRAW", "OT_TWO", "OT_OVER", "OT_UNDER"
    private Double line; // e.g. 2.5
    private String participant; // e.g. "HOME", "AWAY"
}
