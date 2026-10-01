package pro.datawiki.igaming.source.bet7k.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kEventDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kMarketDto;
import pro.datawiki.igaming.source.bet7k.dto.Bet7kOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dedicated handler for Correct Score / Exact Score markets:
 * - Resultado Exato / Placar Exato (Portuguese)
 * - Correct Score / Exact Score (English)
 */
@Component
@Order(60)
public class CorrectScoreMarketHandler extends AbstractBet7kMarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)\\s*[-:xX/]\\s*(\\d+)");

    @Override
    public boolean supports(Bet7kMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CANTO") ||
            mName.contains("CARD") || mName.contains("CART") || mName.contains("AMAREL")) {
            return false;
        }

        return mName.contains("CORRECT SCORE") ||
               mName.contains("EXACT SCORE") ||
               mName.contains("RESULTADO EXATO") ||
               mName.contains("PLACAR EXATO") ||
               mName.contains("SCORE EXATO");
    }

    @Override
    public void handle(Bet7kMarketDto market, Bet7kEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        String period = market.getPeriod() != null ? market.getPeriod().toUpperCase() : "";
        BetScope scope = resolveScope(mName + " " + period);
        String group = formatGroupName("correct_score", scope);

        for (Bet7kOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            CorrectScoreBet betType = null;
            if (upper.contains("OTHER") || upper.contains("ANY OTHER") || upper.equals("AOS") ||
                upper.contains("OUTRO") || upper.contains("OUTROS") || upper.contains("QUALQUER OUTRO")) {
                betType = new CorrectScoreBet(scope, 0, 0, true);
            } else {
                Matcher matcher = SCORE_PATTERN.matcher(oName);
                if (matcher.find()) {
                    try {
                        int score1 = Integer.parseInt(matcher.group(1));
                        int score2 = Integer.parseInt(matcher.group(2));

                        if (event != null && event.getHomeTeam() != null && event.getAwayTeam() != null) {
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
