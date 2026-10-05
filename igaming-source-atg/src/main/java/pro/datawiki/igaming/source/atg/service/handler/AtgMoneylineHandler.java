package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

@Component
@Order(50)
public class AtgMoneylineHandler extends AbstractAtgMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase(Locale.ROOT);
        if (m.contains("CORNER") || m.contains("HÖRN") || m.contains("CARD") || m.contains("KORT") || m.contains("BOOKING")
                || m.contains("DOUBLE CHANCE") || m.contains("DUBBELCHANS") || m.contains("DRAW NO BET") || m.contains("DNB") || m.contains("OAVGJORT INGET SPEL")
                || m.contains("BOTH TEAMS") || m.contains("BTTS") || m.contains("BÅDA LAGEN")
                || m.contains("TOTAL") || m.contains("ANTAL") || m.contains("HANDICAP") || m.contains("HANDIKAPP") || m.contains("SPREAD")
                || m.contains("OVER/UNDER") || m.contains("ÖVER/UNDER") || m.contains("ROUND") || m.contains("MAP")) {
            return false;
        }
        return m.contains("RESULT") || m.contains("MONEYLINE") || m.contains("1X2") 
                || m.contains("WINNER") || m.contains("MATCH") || m.endsWith("FULL TIME");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                       String marketName, SportType sportType, List<OddItem> items) {
        double odds = extractDecimalOdds(outcome);
        if (odds <= 1.0) return;

        BetScope scope = resolveScope(marketName);
        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String label = outcome.getLabel() != null ? outcome.getLabel().toUpperCase(Locale.ROOT) : "";

        boolean hasDraw = betOffer != null && betOffer.getOutcomes() != null && betOffer.getOutcomes().stream()
                .anyMatch(o -> "OT_DRAW".equalsIgnoreCase(o.getType()) 
                        || "OT_CROSS".equalsIgnoreCase(o.getType())
                        || (o.getLabel() != null && (o.getLabel().equalsIgnoreCase("Draw") || o.getLabel().equalsIgnoreCase("X"))));

        String runnerName = outcome.getLabel() != null ? outcome.getLabel() : "Outcome " + outcome.getId();

        if ("OT_ONE".equals(type) || "1".equals(label) || (event != null && event.getHomeName() != null && label.equalsIgnoreCase(event.getHomeName().toUpperCase(Locale.ROOT)))) {
            MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            addMatchResult(items, outcome, marketName, runnerName, odds, scope, res, null);
        } else if ("OT_DRAW".equals(type) || "OT_CROSS".equals(type) || "DRAW".equals(label) || "X".equals(label)) {
            addMatchResult(items, outcome, marketName, runnerName, odds, scope, MatchResultBet.Outcome.DRAW, null);
        } else if ("OT_TWO".equals(type) || "2".equals(label) || (event != null && event.getAwayName() != null && label.equalsIgnoreCase(event.getAwayName().toUpperCase(Locale.ROOT)))) {
            MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            addMatchResult(items, outcome, marketName, runnerName, odds, scope, res, null);
        }
    }
}
