package pro.datawiki.igaming.source.wplay.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;

import java.util.List;

public interface WplayMarketHandler {
    boolean supports(String marketName, WplayMarketContext context);
    void handle(WplayMarketDto market, WplayMarketContext context, List<OddItem> items);
}
