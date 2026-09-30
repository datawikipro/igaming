package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for Correct Score markets for Betano.
 */
@Component
public class BetanoCorrectScoreMarketHandler extends AbstractBetanoMarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)[-:](\\d+)");

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        return mName.contains("CORRECT SCORE") ||
               mName.contains("EXACT SCORE") ||
               mName.contains("PLACAR EXATO") ||
               mName.contains("SCORE EXATO");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("correct_score", scope);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Matcher m = SCORE_PATTERN.matcher(oName);
            if (m.find()) {
                int s1 = Integer.parseInt(m.group(1));
                int s2 = Integer.parseInt(m.group(2));
                BetType bet = new CorrectScoreBet(scope, s1, s2, false);
                addOddItem(items, group, oName, odds, bet);
            }
        }
    }
}
