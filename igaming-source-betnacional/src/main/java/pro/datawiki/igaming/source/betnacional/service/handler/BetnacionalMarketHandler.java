package pro.datawiki.igaming.source.betnacional.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalEventDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalMarketDto;

import java.util.List;

/**
 * Strategy interface for Betnacional market handlers.
 */
public interface BetnacionalMarketHandler {

    /**
     * Checks if this handler supports the given market and sport type.
     */
    boolean supports(BetnacionalMarketDto market, SportType sportType);

    /**
     * Maps the outcomes in the market to standardized OddItem instances.
     */
    void handle(BetnacionalMarketDto market, BetnacionalEventDto event, SportType sportType, List<OddItem> items);
}
