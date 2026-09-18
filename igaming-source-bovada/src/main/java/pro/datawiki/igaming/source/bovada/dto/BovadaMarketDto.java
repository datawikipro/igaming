package pro.datawiki.igaming.source.bovada.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BovadaMarketDto {
    private String id;
    private String description;
    private String key;
    private BovadaPeriodDto period;
    private List<BovadaOutcomeDto> outcomes;
}
