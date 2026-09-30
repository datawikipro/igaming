package pro.datawiki.igaming.source.betway.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.source.betway.dto.BetwayEventDto;
import pro.datawiki.igaming.source.betway.dto.BetwayMarketDto;
import pro.datawiki.igaming.source.betway.dto.BetwayOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dedicated handler for Correct Score / Exact Score markets.
 */
@Component
public class CorrectScoreMarketHandler extends AbstractBetwayMarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)\\s*[-:]\\s*(\\d+)");

    @Override
    public boolean supports(BetwayMarketDto market, SportType sportType) {
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("CARD") || mName.contains("BOOKING")) return false;
        return mName.contains("CORRECT SCORE") || mName.contains("EXACT SCORE");
    }

    @Override
    public void handle(BetwayMarketDto market, BetwayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("correct_score", scope);

        for (BetwayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            CorrectScoreBet betType = null;
            if (upper.contains("OTHER") || upper.contains("ANY OTHER") || upper.equals("AOS")) {
                betType = new CorrectScoreBet(scope, 0, 0, true);
            } else {
                Matcher matcher = SCORE_PATTERN.matcher(oName);
                if (matcher.find()) {
                    try {
                        int score1 = Integer.parseInt(matcher.group(1));
                        int score2 = Integer.parseInt(matcher.group(2));

                        if (event.getAwayTeam() != null && event.getHomeTeam() != null) {
                            String homeUpper = event.getHomeTeam().toUpperCase();
                            String awayUpper = event.getAwayTeam().toUpperCase();
                            if (upper.startsWith(awayUpper) && !upper.startsWith(homeUpper)) {
                                int tmp = score1;
                                score1 = score2;
                                score2 = tmp;
                            }
                        }

                        betType = new CorrectScoreBet(scope, score1, score2, false);
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
