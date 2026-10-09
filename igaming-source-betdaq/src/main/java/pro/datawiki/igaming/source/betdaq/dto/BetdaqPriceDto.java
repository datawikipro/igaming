package pro.datawiki.igaming.source.betdaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetdaqPriceDto {
    private Double price;
    private Double stake;
    private Double availableAmount;

    public Double getEffectivePrice() {
        return price;
    }
}
