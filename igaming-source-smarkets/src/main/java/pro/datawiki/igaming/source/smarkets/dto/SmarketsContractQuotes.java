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
        // Priority 1: Available executable offers to back against
        // In offers, lower price = higher decimal odds. The best backer price is the lowest offer price.
        if (offers != null && !offers.isEmpty()) {
            SmarketsQuoteEntry bestOffer = offers.stream()
                    .filter(o -> o.getPrice() >= 20 && o.getPrice() < 10000 && (o.getQuantity() == 0 || o.getQuantity() > 0))
                    .min(java.util.Comparator.comparingInt(SmarketsQuoteEntry::getPrice))
                    .orElse(null);
            if (bestOffer != null) {
                double odds = bestOffer.getDecimalOdds();
                if (odds > 1.0 && odds <= 500.0) {
                    return odds;
                }
            }
        }
        // Priority 2: Market bids (indicative back price)
        // In bids, higher price = market consensus bid. The best bid is the highest bid price.
        if (bids != null && !bids.isEmpty()) {
            SmarketsQuoteEntry bestBid = bids.stream()
                    .filter(b -> b.getPrice() >= 20 && b.getPrice() < 10000)
                    .max(java.util.Comparator.comparingInt(SmarketsQuoteEntry::getPrice))
                    .orElse(null);
            if (bestBid != null) {
                double odds = bestBid.getDecimalOdds();
                if (odds > 1.0 && odds <= 500.0) {
                    return odds;
                }
            }
        }
        return null;
    }

    public Double getBestLayOdds() {
        if (bids != null && !bids.isEmpty()) {
            SmarketsQuoteEntry bestBid = bids.stream()
                    .filter(b -> b.getPrice() >= 20 && b.getPrice() < 10000)
                    .max(java.util.Comparator.comparingInt(SmarketsQuoteEntry::getPrice))
                    .orElse(null);
            if (bestBid != null) {
                return bestBid.getDecimalOdds();
            }
        }
        if (offers != null && !offers.isEmpty()) {
            SmarketsQuoteEntry bestOffer = offers.stream()
                    .filter(o -> o.getPrice() >= 20 && o.getPrice() < 10000)
                    .min(java.util.Comparator.comparingInt(SmarketsQuoteEntry::getPrice))
                    .orElse(null);
            if (bestOffer != null) {
                return bestOffer.getDecimalOdds();
            }
        }
        return null;
    }
}
