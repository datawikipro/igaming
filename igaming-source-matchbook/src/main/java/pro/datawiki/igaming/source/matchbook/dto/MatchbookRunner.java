package pro.datawiki.igaming.source.matchbook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchbookRunner {
    private Long id;
    private String name;
    private String status;
    private List<MatchbookPrice> prices = new ArrayList<>();

    public Double getBestBackOdds() {
        if (prices == null) return null;
        return prices.stream()
                .filter(p -> "back".equalsIgnoreCase(p.getSide()) && p.getDecimalOdds() != null && p.getDecimalOdds() > 1.0)
                .map(MatchbookPrice::getDecimalOdds)
                .max(Double::compareTo)
                .orElse(null);
    }

    public Double getBestLayOdds() {
        if (prices == null) return null;
        return prices.stream()
                .filter(p -> "lay".equalsIgnoreCase(p.getSide()) && p.getDecimalOdds() != null && p.getDecimalOdds() > 1.0)
                .map(MatchbookPrice::getDecimalOdds)
                .min(Double::compareTo)
                .orElse(null);
    }
}
