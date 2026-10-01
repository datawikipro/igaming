package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.Locale;

@Slf4j
@Component
@Order(10)
public class WplayStatsCornersHandler extends AbstractWplayStatsHandler {

    @Override
    protected StatType getStatType() {
        return StatType.CORNERS;
    }

    @Override
    protected String getStatPrefix() {
        return "corners";
    }

    @Override
    public boolean supports(String marketName, WplayMarketContext context) {
        if (marketName == null) return false;
        String m = marketName.toLowerCase(Locale.ROOT);
        return m.contains("corner") || m.contains("córner") || m.contains("esquina");
    }
}
