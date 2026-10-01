package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;

@Component
@Order(25)
public class AtgDoubleChanceHandler extends AbstractAtgMarketHandler {

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null) return false;
        String m = marketName.toUpperCase(Locale.ROOT);
        if (m.contains("CORNER") || m.contains("HÖRN") || m.contains("CARD") || m.contains("KORT") 
                || m.contains("BOOKING") || m.contains("ESPORT")) {
            return false;
        }
        return m.contains("DOUBLE CHANCE") || m.contains("DOUBLE_CHANCE") 
                || m.contains(" 1X2 DC") || m.endsWith(" DC") || m.contains(" DC ")
                || m.contains("DUBBELCHANS") || m.contains("DUBBEL CHANS");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                       String marketName, SportType sportType, List<OddItem> items) {
        double odds = extractDecimalOdds(outcome);
        if (odds <= 1.0) return;

        BetScope scope = resolveScope(marketName);
        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String label = outcome.getLabel() != null ? outcome.getLabel().trim() : "";
        String englishLabel = outcome.getEnglishLabel() != null ? outcome.getEnglishLabel().trim() : "";
        String upperLabel = label.toUpperCase(Locale.ROOT);
        String upperEng = englishLabel.toUpperCase(Locale.ROOT);

        String runnerName = !label.isEmpty() ? label : (!englishLabel.isEmpty() ? englishLabel : "Outcome " + outcome.getId());

        MatchResultBet.Outcome dcOutcome = resolveDoubleChanceOutcome(event, type, label, englishLabel, upperLabel, upperEng, scope);
        if (dcOutcome != null) {
            addMatchResult(items, outcome, marketName, runnerName, odds, scope, dcOutcome, StatType.MATCH);
        }
    }

    private MatchResultBet.Outcome resolveDoubleChanceOutcome(KambiEvent event, String type, 
                                                              String label, String englishLabel, 
                                                              String upperLabel, String upperEng, 
                                                              BetScope scope) {
        // 1. Direct type matching from Kambi
        if ("OT_ONE_CROSS".equals(type) || "OT_1X".equals(type)) {
            return MatchResultBet.Outcome.DC_1X;
        } else if ("OT_ONE_TWO".equals(type) || "OT_12".equals(type)) {
            return MatchResultBet.Outcome.DC_12;
        } else if ("OT_CROSS_TWO".equals(type) || "OT_X2".equals(type) || "OT_TWO_CROSS".equals(type)) {
            return MatchResultBet.Outcome.DC_X2;
        }

        // 2. Built-in map1X2DCRecord
        BetType mapped = map1X2DCRecord(label, scope, StatType.MATCH);
        if (mapped == null && !englishLabel.isEmpty()) {
            mapped = map1X2DCRecord(englishLabel, scope, StatType.MATCH);
        }
        if (mapped instanceof MatchResultBet mrb && mrb.outcome() != null) {
            if (mrb.outcome() == MatchResultBet.Outcome.DC_1X 
                    || mrb.outcome() == MatchResultBet.Outcome.DC_12 
                    || mrb.outcome() == MatchResultBet.Outcome.DC_X2) {
                return mrb.outcome();
            }
        }

        // 3. String patterns (English + Swedish)
        String text = (upperLabel + " " + upperEng).trim();
        if (text.contains("1X") || text.contains("1-X") || text.contains("1/X") 
                || text.contains("1 OR X") || text.contains("1 ИЛИ X") 
                || text.contains("1 EL X") || text.contains("1 ELLER X") 
                || text.contains("HOME OR DRAW") || text.contains("HOME/DRAW") 
                || text.contains("HEMMA ELLER OAVGJORT") || text.contains("HEMMA ELLER KRYSS") 
                || text.contains("HEMMA/OAVGJORT")) {
            return MatchResultBet.Outcome.DC_1X;
        }
        if (text.contains("12") || text.contains("1-2") || text.contains("1/2") 
                || text.contains("1 OR 2") || text.contains("1 ИЛИ 2") 
                || text.contains("1 EL 2") || text.contains("1 ELLER 2") 
                || text.contains("HOME OR AWAY") || text.contains("HOME/AWAY") 
                || text.contains("HEMMA ELLER BORTA") || text.contains("HEMMA/BORTA")) {
            return MatchResultBet.Outcome.DC_12;
        }
        if (text.contains("X2") || text.contains("X-2") || text.contains("X/2") || text.contains("2X") 
                || text.contains("X OR 2") || text.contains("2 OR X") || text.contains("X ИЛИ 2") 
                || text.contains("X EL 2") || text.contains("X ELLER 2") || text.contains("2 EL X") || text.contains("2 ELLER X") 
                || text.contains("DRAW OR AWAY") || text.contains("DRAW/AWAY") || text.contains("AWAY OR DRAW") 
                || text.contains("OAVGJORT ELLER BORTA") || text.contains("KRYSS ELLER BORTA") 
                || text.contains("BORTA ELLER OAVGJORT")) {
            return MatchResultBet.Outcome.DC_X2;
        }

        // 4. Team-name based resolution
        if (event != null) {
            String home = event.getHomeName() != null ? event.getHomeName().trim().toUpperCase(Locale.ROOT) : "";
            String away = event.getAwayName() != null ? event.getAwayName().trim().toUpperCase(Locale.ROOT) : "";
            boolean hasHome = !home.isEmpty() && (upperLabel.contains(home) || upperEng.contains(home));
            boolean hasAway = !away.isEmpty() && (upperLabel.contains(away) || upperEng.contains(away));
            boolean hasDraw = text.contains("DRAW") || text.contains("OAVGJORT") || text.contains("KRYSS") 
                    || text.contains(" X ") || text.endsWith(" X") || text.startsWith("X ") || text.equals("X");

            if (hasHome && hasAway) {
                return MatchResultBet.Outcome.DC_12;
            } else if (hasHome && hasDraw) {
                return MatchResultBet.Outcome.DC_1X;
            } else if (hasAway && hasDraw) {
                return MatchResultBet.Outcome.DC_X2;
            }
        }

        return null;
    }
}
