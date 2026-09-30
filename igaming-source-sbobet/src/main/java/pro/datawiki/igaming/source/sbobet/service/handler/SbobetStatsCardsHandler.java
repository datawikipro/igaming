package pro.datawiki.igaming.source.sbobet.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.market.StatType;

@Component
public class SbobetStatsCardsHandler extends AbstractSbobetStatsHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String lower = marketKey.toLowerCase();
        return lower.contains("card") || lower.contains("yellow_card") || lower.startsWith("cards");
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
