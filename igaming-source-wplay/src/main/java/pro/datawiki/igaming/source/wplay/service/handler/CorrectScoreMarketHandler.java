package pro.datawiki.igaming.source.wplay.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for Correct Score / Exact Score markets:
 * - Marcador exacto / Resultado exacto / Marcador correcto (Spanish)
 * - Correct Score / Exact Score (English)
 */
@Component
@Order(60)
public class CorrectScoreMarketHandler extends AbstractWplayMarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)\\s*[-:xX/]\\s*(\\d+)");

    @Override
    public boolean supports(WplayMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESQUINA") || mName.contains("CÓRNER") ||
            mName.contains("CARD") || mName.contains("TARJETA") || mName.contains("AMARILLA")) {
            return false;
        }

        return mName.contains("CORRECT SCORE") ||
               mName.contains("EXACT SCORE") ||
               mName.contains("MARCADOR EXACTO") ||
               mName.contains("MARCADOR CORRECTO") ||
               mName.contains("RESULTADO EXACTO") ||
               mName.contains("RESULTADO CORRECTO") ||
               mName.contains("SCORE EXACTO");
    }

    @Override
    public void handle(WplayMarketDto market, WplayEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        String period = market.getPeriod() != null ? market.getPeriod().toUpperCase() : "";
        BetScope scope = resolveScope(mName + " " + period);
        String group = formatGroupName("correct_score", scope);

        for (WplayOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            CorrectScoreBet betType = null;
            if (upper.contains("OTHER") || upper.contains("ANY OTHER") || upper.equals("AOS") ||
                upper.contains("OTRO") || upper.contains("OTROS") || upper.contains("CUALQUIER OTRO")) {
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
