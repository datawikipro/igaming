package pro.datawiki.igaming.source.bwin.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiEventDetailsResponse {
    private List<KambiEvent> events;
    private List<KambiBetOffer> betoffers;
}
