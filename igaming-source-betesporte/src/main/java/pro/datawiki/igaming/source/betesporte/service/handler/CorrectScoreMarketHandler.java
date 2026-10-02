package pro.datawiki.igaming.source.betesporte.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteEventDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteMarketDto;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteOutcomeDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for Correct Score (Resultado Exato / Placar Exato) markets.
 */
@Component
@Order(60)
public class CorrectScoreMarketHandler extends AbstractBetesporteMarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)\\s*[-:xX/]\\s*(\\d+)");

    @Override
    public boolean supports(BetesporteMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CARD") || mName.contains("CART")) return false;

        return mName.contains("CORRECT SCORE") ||
               mName.contains("EXACT SCORE") ||
               mName.contains("RESULTADO EXATO") ||
               mName.contains("PLACAR EXATO") ||
               mName.contains("RESULTADO CORRETO");
    }

    @Override
    public void handle(BetesporteMarketDto market, BetesporteEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("correct_score", scope);

        for (BetesporteOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();

            BetType betType = null;
            if (upper.contains("OUTRO") || upper.contains("OTHER") || upper.contains("QUALQUER")) {
                betType = new CorrectScoreBet(scope, 0, 0, true);
            } else {
                Matcher matcher = SCORE_PATTERN.matcher(oName);
                if (matcher.find()) {
                    try {
                        int s1 = Integer.parseInt(matcher.group(1));
                        int s2 = Integer.parseInt(matcher.group(2));
                        betType = new CorrectScoreBet(scope, s1, s2, false);
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
