package pro.datawiki.igaming.source.smarkets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmarketsContractQuotes {
    /**
     * Bids represent BACK bets (people offering to buy at this price).
     */
    private List<SmarketsQuoteEntry> bids = new ArrayList<>();

    /**
     * Offers represent LAY bets (people offering to sell at this price).
     */
    private List<SmarketsQuoteEntry> offers = new ArrayList<>();

    public Double getBestBackOdds() {
        if (bids != null && !bids.isEmpty()) {
            Double maxOdds = bids.stream()
                    .filter(b -> b.getPrice() > 0 && b.getPrice() < 10000)
                    .map(SmarketsQuoteEntry::getDecimalOdds)
                    .max(Double::compareTo)
                    .orElse(null);
            if (maxOdds != null && maxOdds > 1.0) {
                return maxOdds;
            }
        }
        if (offers != null && !offers.isEmpty()) {
            return offers.stream()
                    .filter(o -> o.getPrice() > 0 && o.getPrice() < 10000)
                    .map(SmarketsQuoteEntry::getDecimalOdds)
                    .max(Double::compareTo)
                    .orElse(null);
        }
        return null;
    }

    public Double getBestLayOdds() {
        if (offers == null || offers.isEmpty()) {
            return null;
        }
        return offers.stream()
                .filter(o -> o.getPrice() > 0 && o.getPrice() < 10000)
                .map(SmarketsQuoteEntry::getDecimalOdds)
                .min(Double::compareTo)
                .orElse(null);
    }
}
