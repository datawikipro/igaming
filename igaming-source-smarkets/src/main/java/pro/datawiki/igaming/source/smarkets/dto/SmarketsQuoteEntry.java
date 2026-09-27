package pro.datawiki.igaming.source.smarkets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmarketsQuoteEntry {
    /**
     * Smarkets price in basis points (e.g., 5236 = 52.36% probability).
     * Decimal odds = 10000.0 / price.
     */
    private int price;

    /**
     * Quantity/stake available in pence.
     */
    private long quantity;

    public double getDecimalOdds() {
        if (price <= 0 || price >= 10000) {
            return 0.0;
        }
        return 10000.0 / price;
    }
}
