package pro.datawiki.igaming.source.fanduel.service.mapper;

import pro.datawiki.igaming.dto.BetType;

public interface FanDuelMarketHandler {
    boolean supports(FanDuelMarketContext ctx);
    BetType map(FanDuelMarketContext ctx);
}
