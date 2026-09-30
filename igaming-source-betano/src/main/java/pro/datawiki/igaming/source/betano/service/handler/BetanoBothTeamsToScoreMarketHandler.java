package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Handler for Both Teams to Score (BTTS: Yes/No / Ambas Marcam) markets for Betano.
 */
@Component
public class BetanoBothTeamsToScoreMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("BOTH TEAMS TO SCORE") ||
               mName.contains("BOTH TEAMS SCORE") ||
               mName.contains("BTTS") ||
               mName.contains("GG/NG") ||
               mName.contains("GOAL / NO GOAL") ||
               mName.contains("AMBAS MARCAM");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("both_teams_to_score", scope);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BinaryMarketBet.Outcome bOutcome = null;
            if (upper.equals("YES") || upper.equals("SIM") || upper.equals("SI") || upper.equals("Y") ||
                upper.equals("GG") || upper.contains("BOTH TEAMS TO SCORE - YES") || "OT_YES".equalsIgnoreCase(outcome.getOutcomeType())) {
                bOutcome = BinaryMarketBet.Outcome.YES;
            } else if (upper.equals("NO") || upper.equals("NAO") || upper.equals("NÃO") || upper.equals("N") ||
                       upper.equals("NG") || upper.contains("BOTH TEAMS TO SCORE - NO") || "OT_NO".equalsIgnoreCase(outcome.getOutcomeType())) {
                bOutcome = BinaryMarketBet.Outcome.NO;
            }

            if (bOutcome != null) {
                BinaryMarketBet bet = new BinaryMarketBet(scope, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, bOutcome, StatType.MATCH);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }
}
