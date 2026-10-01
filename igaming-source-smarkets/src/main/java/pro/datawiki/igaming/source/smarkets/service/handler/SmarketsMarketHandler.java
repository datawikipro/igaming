package pro.datawiki.igaming.source.smarkets.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContract;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsContractQuotes;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsEvent;
import pro.datawiki.igaming.source.smarkets.dto.SmarketsMarket;

import java.util.List;
import java.util.Map;

public interface SmarketsMarketHandler {

    /**
     * Checks if this handler supports the given market and sport.
     */
    boolean supports(SmarketsMarket market, SportType sportType);

    /**
     * Processes market contracts and quotes and populates oddItems.
     */
    void handle(SmarketsEvent event,
                SmarketsMarket market,
                List<SmarketsContract> contracts,
                Map<String, SmarketsContractQuotes> quotesByContractId,
                SportType sportType,
                String team1,
                String team2,
                List<OddItem> items);
}
