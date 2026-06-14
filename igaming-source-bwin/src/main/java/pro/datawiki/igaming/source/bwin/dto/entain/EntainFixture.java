package pro.datawiki.igaming.source.bwin.dto.entain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntainFixture {
    private Long sourceId;
    private String startDate;
    private EntainValueObject name;
    private List<EntainOptionMarket> optionMarkets;
}
