package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

@Component
@Order(30)
public class AtgHandicapHandler extends AbstractAtgMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase(Locale.ROOT);
        if (m.contains("CORNER") || m.contains("HÖRN") || m.contains("CARD") || m.contains("KORT") || m.contains("BOOKING")
                || m.contains("MAP") || m.contains("ROUND") || m.contains("DRAW NO BET") || m.contains("DNB")
                || m.contains("OAVGJORT INGET SPEL") || m.contains("3-WAY") || m.contains("3 WAY")
                || m.contains("3-VÄGS") || m.contains("3 VÄGS") || m.contains("THREE-WAY")
                || m.contains("TREVÄGS") || m.contains("EUROPEAN") || m.contains("EUROPEISKT")) {
            return false;
        }
        if (betOffer != null && betOffer.getOutcomes() != null) {
            boolean hasDraw = betOffer.getOutcomes().stream().anyMatch(o ->
                    "OT_DRAW".equalsIgnoreCase(o.getType())
                            || "OT_CROSS".equalsIgnoreCase(o.getType())
                            || (o.getLabel() != null && (
                            o.getLabel().equalsIgnoreCase("Draw")
                                    || o.getLabel().equalsIgnoreCase("X")
                                    || o.getLabel().equalsIgnoreCase("Oavgjort")
                                    || o.getLabel().equalsIgnoreCase("Lika")
                                    || o.getLabel().equalsIgnoreCase("Tie"))));
            if (hasDraw) {
                return false;
            }
        }
        return m.contains("HANDICAP") || m.contains("SPREAD") || m.contains("ASIAN");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                       String marketName, SportType sportType, List<OddItem> items) {
        double odds = extractDecimalOdds(outcome);
        if (odds <= 1.0) return;

        BetScope scope = resolveScope(marketName);
        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase(Locale.ROOT) : "";
        Double line = outcome.getLine() != null ? outcome.getLine() : 0.0;

        String runnerName = outcome.getLabel() != null ? outcome.getLabel() : "Outcome " + outcome.getId();

        if ("OT_ONE".equals(type) || "1".equals(label) || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
            addHandicap(items, outcome, marketName, runnerName, odds, scope, HandicapBet.Outcome.TEAM1, line, false, null);
        } else if ("OT_TWO".equals(type) || "2".equals(label) || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
            addHandicap(items, outcome, marketName, runnerName, odds, scope, HandicapBet.Outcome.TEAM2, line, false, null);
        }
    }
}
