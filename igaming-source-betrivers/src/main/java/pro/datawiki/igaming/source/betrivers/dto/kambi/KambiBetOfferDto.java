package pro.datawiki.igaming.source.betrivers.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiBetOfferDto {
    private Long id;
    private Long eventId;
    private KambiCriterionDto criterion;
    private KambiBetOfferTypeDto betOfferType;
    private List<KambiOutcomeDto> outcomes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KambiCriterionDto {
        private Long id;
        private String label;
        private String englishLabel;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KambiBetOfferTypeDto {
        private Long id;
        private String name;
    }
}
