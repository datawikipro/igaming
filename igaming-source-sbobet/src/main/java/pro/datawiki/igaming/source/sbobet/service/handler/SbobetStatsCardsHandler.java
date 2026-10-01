package pro.datawiki.igaming.source.sbobet.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.StatType;

@Component
public class SbobetStatsCardsHandler extends AbstractSbobetStatsHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String lower = marketKey.toLowerCase();
        return lower.contains("card") || lower.contains("yellow_card") || lower.startsWith("cards")
                || lower.contains("booking") || lower.startsWith("bookings");
    }

    @Override
    public boolean supports(String marketKey, SportType sportType) {
        if (sportType != null && sportType != SportType.FOOTBALL && sportType != SportType.UNKNOWN) {
            return false;
        }
        return supports(marketKey);
    }

    @Override
    protected StatType getStatType() {
        return StatType.YELLOW_CARDS;
    }

    @Override
    protected String getStatPrefix() {
        return "cards";
    }
}
