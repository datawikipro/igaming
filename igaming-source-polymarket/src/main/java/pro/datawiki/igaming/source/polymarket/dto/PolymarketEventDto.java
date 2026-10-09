package pro.datawiki.igaming.source.polymarket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolymarketEventDto {
    private String id;
    private String ticker;
    private String slug;
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private Boolean active;
    private Boolean closed;
    private List<PolymarketTagDto> tags;
    private List<PolymarketMarketDto> markets;
}
