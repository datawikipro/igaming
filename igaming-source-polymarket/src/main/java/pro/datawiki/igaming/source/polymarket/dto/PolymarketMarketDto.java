package pro.datawiki.igaming.source.polymarket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolymarketMarketDto {
    private String id;
    private String question;
    private String slug;
    private String conditionId;
    private Object outcomes; // can be String JSON "[\"Yes\", \"No\"]" or List<String>
    private Object outcomePrices; // can be String JSON "[\"0.65\", \"0.35\"]" or List<String>
    private Boolean active;
    private Boolean closed;
    private Double volume;
    private String endDate;
    private String startDate;
}
