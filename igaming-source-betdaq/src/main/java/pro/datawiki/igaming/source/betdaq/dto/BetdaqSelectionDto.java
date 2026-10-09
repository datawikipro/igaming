package pro.datawiki.igaming.source.betdaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetdaqSelectionDto {
    private String id;
    private String name;
    private String status;
    private Double resetCount;
    private List<BetdaqPriceDto> backPrices;
    private List<BetdaqPriceDto> layPrices;

    public Double getBestBackPrice() {
        if (backPrices != null && !backPrices.isEmpty()) {
            return backPrices.get(0).getEffectivePrice();
        }
        return null;
    }

    public Double getBestLayPrice() {
        if (layPrices != null && !layPrices.isEmpty()) {
            return layPrices.get(0).getEffectivePrice();
        }
        return null;
    }
}
