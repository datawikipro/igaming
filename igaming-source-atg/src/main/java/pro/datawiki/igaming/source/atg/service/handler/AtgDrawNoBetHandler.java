package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

@Component
@Order(35)
public class AtgDrawNoBetHandler extends AbstractAtgMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase(Locale.ROOT);
        if (m.contains("CORNER") || m.contains("HÖRN") || m.contains("CARD") || m.contains("KORT") 
                || m.contains("BOOKING") || m.contains("ESPORT")) {
            return false;
        }
        return m.contains("DRAW NO BET") || m.contains("DRAW_NO_BET") || m.contains("DNB") 
                || m.contains("OAVGJORT INGET SPEL") || m.contains("OAVGJORT - INGET SPEL")
                || m.contains("INSATSEN TILLBAKA VID OAVGJORT");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                       String marketName, SportType sportType, List<OddItem> items) {
        double odds = extractDecimalOdds(outcome);
        if (odds <= 1.0) return;

        BetScope scope = resolveScope(marketName);
        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String label = outcome.getLabel() != null ? outcome.getLabel().trim() : "";
        String eng = outcome.getEnglishLabel() != null ? outcome.getEnglishLabel().trim() : "";
        String upperLabel = label.toUpperCase(Locale.ROOT);
        String upperEng = eng.toUpperCase(Locale.ROOT);

        String runnerName = !label.isEmpty() ? label : (!eng.isEmpty() ? eng : "Outcome " + outcome.getId());

        HandicapBet.Outcome hdpOutcome = resolveDnbOutcome(event, outcome, type, upperLabel, upperEng);
        if (hdpOutcome != null) {
            addHandicap(items, outcome, marketName, runnerName, odds, scope, hdpOutcome, 0.0, false, StatType.MATCH);
        }
    }

    private HandicapBet.Outcome resolveDnbOutcome(KambiEvent event, KambiOutcome outcome, 
                                                 String type, String upperLabel, String upperEng) {
        if ("OT_ONE".equals(type) || "1".equals(upperLabel) || "1".equals(upperEng)
                || "HOME".equals(upperLabel) || "HOME".equals(upperEng)
                || "HEMMA".equals(upperLabel) || "HEMMA".equals(upperEng)
                || "TEAM 1".equals(upperLabel) || "TEAM 1".equals(upperEng)) {
            return HandicapBet.Outcome.TEAM1;
        }
        if ("OT_TWO".equals(type) || "2".equals(upperLabel) || "2".equals(upperEng)
                || "AWAY".equals(upperLabel) || "AWAY".equals(upperEng)
                || "BORTA".equals(upperLabel) || "BORTA".equals(upperEng)
                || "TEAM 2".equals(upperLabel) || "TEAM 2".equals(upperEng)) {
            return HandicapBet.Outcome.TEAM2;
        }

        if (event != null) {
            String home = event.getHomeName() != null ? event.getHomeName().trim().toUpperCase(Locale.ROOT) : "";
            String away = event.getAwayName() != null ? event.getAwayName().trim().toUpperCase(Locale.ROOT) : "";
            if (!home.isEmpty() && (upperLabel.contains(home) || upperEng.contains(home))) {
                return HandicapBet.Outcome.TEAM1;
            }
            if (!away.isEmpty() && (upperLabel.contains(away) || upperEng.contains(away))) {
                return HandicapBet.Outcome.TEAM2;
            }
        }

        if (outcome.getParticipant() != null && event != null) {
            String part = outcome.getParticipant().trim().toUpperCase(Locale.ROOT);
            String home = event.getHomeName() != null ? event.getHomeName().trim().toUpperCase(Locale.ROOT) : "";
            String away = event.getAwayName() != null ? event.getAwayName().trim().toUpperCase(Locale.ROOT) : "";
            if (!home.isEmpty() && part.contains(home)) {
                return HandicapBet.Outcome.TEAM1;
            }
            if (!away.isEmpty() && part.contains(away)) {
                return HandicapBet.Outcome.TEAM2;
            }
        }

        return null;
    }
}
