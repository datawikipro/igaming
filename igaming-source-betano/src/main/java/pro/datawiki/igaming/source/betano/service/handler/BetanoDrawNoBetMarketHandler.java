package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Handler for Draw No Bet (DNB / Tie No Bet / Empate Anula) markets for Betano.
 * Draw No Bet maps to Handicap 0.0 for TEAM1 or TEAM2.
 */
@Component
public class BetanoDrawNoBetMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName();
        if (isStatsMarket(mName)) return false;
        return mName.contains("DRAW NO BET") ||
               mName.contains("DNB") ||
               mName.contains("TIE NO BET") ||
               mName.contains("EMPATE ANULA") ||
               mName.contains("EMPATE NO VALIDO");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("draw_no_bet", scope);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (isTeam1(upper, event) || "OT_ONE".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = new HandicapBet(scope, HandicapBet.Outcome.TEAM1, 0.0, false, StatType.MATCH);
            } else if (isTeam2(upper, event) || "OT_TWO".equalsIgnoreCase(outcome.getOutcomeType())) {
                betType = new HandicapBet(scope, HandicapBet.Outcome.TEAM2, 0.0, false, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
