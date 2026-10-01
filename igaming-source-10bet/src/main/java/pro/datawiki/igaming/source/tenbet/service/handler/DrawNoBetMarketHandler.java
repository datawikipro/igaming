package pro.datawiki.igaming.source.tenbet.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.tenbet.dto.TenBetEventDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetMarketDto;
import pro.datawiki.igaming.source.tenbet.dto.TenBetOutcomeDto;

import java.util.List;

/**
 * Dedicated OOP handler for Draw No Bet (DNB) markets.
 * Draw No Bet maps to Handicap 0.0 for TEAM1 or TEAM2.
 */
@Component
public class DrawNoBetMarketHandler extends AbstractTenBetMarketHandler {

    @Override
    public boolean supports(TenBetMarketDto market, SportType sportType) {
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        return mName.contains("DRAW NO BET") || mName.contains("DNB") || mName.contains("TIE NO BET");
    }

    @Override
    public void handle(TenBetMarketDto market, TenBetEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("draw_no_bet", scope);

        for (TenBetOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event)) {
                betType = new HandicapBet(scope, HandicapBet.Outcome.TEAM1, 0.0, false, StatType.MATCH);
            } else if (isTeam2(upper, event)) {
                betType = new HandicapBet(scope, HandicapBet.Outcome.TEAM2, 0.0, false, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private boolean isTeam1(String outcomeName, TenBetEventDto event) {
        if (event.getHomeTeam() != null) {
            String home = event.getHomeTeam().toUpperCase();
            if (outcomeName.contains(home) || (home.length() >= 3 && home.contains(outcomeName))) return true;
        }
        return "1".equals(outcomeName) || outcomeName.startsWith("HOME") || outcomeName.startsWith("TEAM 1") || outcomeName.startsWith("TEAM1");
    }

    private boolean isTeam2(String outcomeName, TenBetEventDto event) {
        if (event.getAwayTeam() != null) {
            String away = event.getAwayTeam().toUpperCase();
            if (outcomeName.contains(away) || (away.length() >= 3 && away.contains(outcomeName))) return true;
        }
        return "2".equals(outcomeName) || outcomeName.startsWith("AWAY") || outcomeName.startsWith("TEAM 2") || outcomeName.startsWith("TEAM2");
    }
}
