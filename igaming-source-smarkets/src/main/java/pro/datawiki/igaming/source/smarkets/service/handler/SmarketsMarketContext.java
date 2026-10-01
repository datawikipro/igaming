package pro.datawiki.igaming.source.smarkets.service.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContract;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContractQuotes;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsEvent;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsMarket;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmarketsMarketContext {
    private SmarketsEvent event;
    private SmarketsMarket market;
    private List<SmarketsContract> contracts;
    private Map<String, SmarketsContractQuotes> quotesByContractId;
    private SportType sportType;
    private String team1;
    private String team2;
    private Boolean isLive;

    public Double getBestBackOdds(SmarketsContract contract) {
        if (contract == null || contract.getId() == null || quotesByContractId == null) {
            return null;
        }
        SmarketsContractQuotes quotes = quotesByContractId.get(contract.getId());
        return quotes != null ? quotes.getBestBackOdds() : null;
    }
}
