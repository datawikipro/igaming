package pro.datawiki.igaming.source.bcgame.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.source.bcgame.dto.BcgameMarketDto;

import java.util.List;

public interface BcgameMarketHandler {
    boolean supports(String marketName, BcgameMarketContext context);
    void handle(BcgameMarketDto market, BcgameMarketContext context, List<OddItem> items);
}
