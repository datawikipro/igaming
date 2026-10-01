package pro.datawiki.igaming.source.draftkings.service.mapper;

import pro.datawiki.igaming.dto.BetType;

public interface DraftKingsMarketHandler {
    boolean supports(DraftKingsMarketContext ctx);
    BetType map(DraftKingsMarketContext ctx);
}
