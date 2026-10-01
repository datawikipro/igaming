package pro.datawiki.igaming.source.wplay.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.Locale;

@Slf4j
@Component
@Order(15)
public class WplayStatsCardsHandler extends AbstractWplayStatsHandler {

    @Override
    protected StatType getStatType() {
        return StatType.YELLOW_CARDS;
    }

    @Override
    protected String getStatPrefix() {
        return "cards";
    }

    @Override
    public boolean supports(String marketName, WplayMarketContext context) {
        if (marketName == null) return false;
        String m = marketName.toLowerCase(Locale.ROOT);
        return m.contains("card") || m.contains("tarjeta") || m.contains("booking") || m.contains("amarilla");
    }
}
