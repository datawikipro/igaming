package pro.datawiki.igaming.source.bwin.dto.entain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntainOptionMarket {
    private Long id;
    private EntainValueObject name;
    private List<EntainOption> options;
}
