package pro.datawiki.igaming.source.betsson.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiEventsResponse {
    private List<KambiEventWrapper> events;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KambiEventWrapper {
        private KambiEvent event;
        private List<KambiBetOffer> betOffers;
    }
}
