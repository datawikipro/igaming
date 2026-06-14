package pro.datawiki.igaming.source.bwin.dto.entain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntainPrice {
    private Double odds;
}
