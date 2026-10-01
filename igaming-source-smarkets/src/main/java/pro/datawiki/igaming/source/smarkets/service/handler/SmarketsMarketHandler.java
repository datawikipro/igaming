package pro.datawiki.igaming.source.smarkets.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import java.util.List;

public interface SmarketsMarketHandler {
    boolean supports(SmarketsMarketContext context);
    void handle(SmarketsMarketContext context, List<OddItem> items);
}
