package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;

import java.util.Locale;

@Component
@Order(8)
public class AtgStatsCardsHandler extends AbstractAtgStatsHandler {

    @Override
    protected StatType getStatType() {
        return StatType.YELLOW_CARDS;
    }

    @Override
    protected String getStatPrefix() {
        return "cards";
    }

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName != null && isCards(marketName)) {
            return true;
        }
        if (betOffer != null && betOffer.getCriterion() != null) {
            if (betOffer.getCriterion().getEnglishLabel() != null && isCards(betOffer.getCriterion().getEnglishLabel())) {
                return true;
            }
            if (betOffer.getCriterion().getLabel() != null && isCards(betOffer.getCriterion().getLabel())) {
                return true;
            }
        }
        return false;
    }

    private boolean isCards(String text) {
        String m = text.toUpperCase(Locale.ROOT);
        return m.contains("CARD") || m.contains("BOOKING") || m.contains("KORT") || m.contains("VARNING");
    }
}
