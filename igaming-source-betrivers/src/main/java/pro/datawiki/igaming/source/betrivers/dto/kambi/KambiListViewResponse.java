package pro.datawiki.igaming.source.betrivers.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiListViewResponse {
    private List<KambiListViewItemDto> events;
}
