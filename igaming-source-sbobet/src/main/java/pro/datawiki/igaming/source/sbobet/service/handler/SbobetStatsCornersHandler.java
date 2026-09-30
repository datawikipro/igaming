package pro.datawiki.igaming.source.sbobet.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.market.StatType;

@Component
public class SbobetStatsCornersHandler extends AbstractSbobetStatsHandler {

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String lower = marketKey.toLowerCase();
        return lower.contains("corner") || lower.startsWith("corners");
    }

    @Override
    protected StatType getStatType() {
        return StatType.CORNERS;
    }

    @Override
    protected String getStatPrefix() {
        return "corners";
    }
}
