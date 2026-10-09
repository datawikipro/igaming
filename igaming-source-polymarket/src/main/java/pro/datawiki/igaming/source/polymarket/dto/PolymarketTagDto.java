package pro.datawiki.igaming.source.polymarket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolymarketTagDto {
    private String id;
    private String label;
    private String slug;
}
