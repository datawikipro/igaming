package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;

import java.util.Locale;

@Component
@Order(7)
public class AtgStatsCornersHandler extends AbstractAtgStatsHandler {

    @Override
    protected StatType getStatType() {
        return StatType.CORNERS;
    }

    @Override
    protected String getStatPrefix() {
        return "corners";
    }

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName != null && isCorners(marketName)) {
            return true;
        }
        if (betOffer != null && betOffer.getCriterion() != null) {
            if (betOffer.getCriterion().getEnglishLabel() != null && isCorners(betOffer.getCriterion().getEnglishLabel())) {
                return true;
            }
            if (betOffer.getCriterion().getLabel() != null && isCorners(betOffer.getCriterion().getLabel())) {
                return true;
            }
        }
        return false;
    }

    private boolean isCorners(String text) {
        String m = text.toUpperCase(Locale.ROOT);
        return m.contains("CORNER") || m.contains("HÖRN");
    }
}
