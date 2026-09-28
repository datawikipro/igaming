package pro.datawiki.igaming.source.smarkets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmarketsMarketType {
    private String name;
    private String param;
}
