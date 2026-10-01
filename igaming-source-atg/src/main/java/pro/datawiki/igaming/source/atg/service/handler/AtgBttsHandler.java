package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

@Component
@Order(30)
public class AtgBttsHandler extends AbstractAtgMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase(Locale.ROOT);
        if (m.contains("CORNER") || m.contains("HÖRN") || m.contains("CARD") || m.contains("KORT") 
                || m.contains("BOOKING") || m.contains("ESPORT")) {
            return false;
        }
        return m.contains("BOTH TEAMS TO SCORE") || m.contains("BOTH TEAMS") || m.contains("BTTS") 
                || m.contains("BÅDA LAGEN GÖR MÅL") || m.contains("BADA LAGEN GOR MAL")
                || m.contains("BÅDA LAGEN ATT GÖRA MÅL") || m.contains("BÅDA LAGEN");
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

        BinaryMarketBet.Outcome binaryOutcome = resolveBttsOutcome(type, upperLabel, upperEng);
        if (binaryOutcome != null) {
            addBinary(items, outcome, marketName, runnerName, odds, scope, BetSubject.MATCH,
                    BinaryMarketBet.MarketType.BTTS, binaryOutcome, StatType.MATCH);
        }
    }

    private BinaryMarketBet.Outcome resolveBttsOutcome(String type, String upperLabel, String upperEng) {
        if ("OT_YES".equals(type)) {
            return BinaryMarketBet.Outcome.YES;
        } else if ("OT_NO".equals(type)) {
            return BinaryMarketBet.Outcome.NO;
        }

        String combined = (upperLabel + " " + upperEng).trim();
        if (combined.equals("YES") || combined.equals("JA") || combined.contains("BTTS_YES") 
                || combined.contains("YES (BTTS)") || combined.contains("JA (BTTS)")
                || combined.contains("BÅDA LAGEN GÖR MÅL - JA") || combined.contains("BÅDA GÖR MÅL")
                || combined.equals("1") || combined.equals("TRUE")
                || upperLabel.equals("YES") || upperLabel.equals("JA")
                || upperEng.equals("YES")) {
            return BinaryMarketBet.Outcome.YES;
        }

        if (combined.equals("NO") || combined.equals("NEJ") || combined.contains("BTTS_NO")
                || combined.contains("NO (BTTS)") || combined.contains("NEJ (BTTS)")
                || combined.contains("BÅDA LAGEN GÖR MÅL - NEJ") || combined.contains("INTE BÅDA LAGEN")
                || combined.equals("0") || combined.equals("FALSE")
                || upperLabel.equals("NO") || upperLabel.equals("NEJ")
                || upperEng.equals("NO")) {
            return BinaryMarketBet.Outcome.NO;
        }

        return null;
    }
}
