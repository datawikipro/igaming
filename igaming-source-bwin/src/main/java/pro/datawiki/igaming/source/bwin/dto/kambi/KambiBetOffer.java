package pro.datawiki.igaming.source.bwin.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiBetOffer {
    private Long id;
    private KambiCriterion criterion;
    private List<KambiOutcome> outcomes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KambiCriterion {
        private Long id;
        private String label; // e.g. "Match", "Total Goals"
        private String englishLabel;
    }
}
