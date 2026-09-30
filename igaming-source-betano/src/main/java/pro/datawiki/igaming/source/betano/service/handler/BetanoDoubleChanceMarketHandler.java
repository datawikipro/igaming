package pro.datawiki.igaming.source.betano.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betano.dto.BetanoEventDto;
import pro.datawiki.igaming.source.betano.dto.BetanoMarketDto;
import pro.datawiki.igaming.source.betano.dto.BetanoOutcomeDto;

import java.util.List;

/**
 * Handler for Double Chance markets (1X, 12, X2) for Betano.
 */
@Component
public class BetanoDoubleChanceMarketHandler extends AbstractBetanoMarketHandler {

    @Override
    public boolean supports(BetanoMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName();
        if (isStatsMarket(mName)) return false;
        return mName.contains("DOUBLE CHANCE") ||
               mName.contains("1X2 OR") ||
               mName.contains("CHANCE DUPLA") ||
               mName.contains("DOBLE OPORTUNIDAD") ||
               mName.contains("DIPLI EFKAIRIA");
    }

    @Override
    public void handle(BetanoMarketDto market, BetanoEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        String group = formatGroupName("double_chance", scope);

        for (BetanoOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            String upper = oName.toUpperCase();
            String type = outcome.getOutcomeType() != null ? outcome.getOutcomeType().toUpperCase() : "";

            boolean isHome = (event.getHomeTeam() != null && upper.contains(event.getHomeTeam().toUpperCase())) ||
                             upper.contains("HOME") || upper.startsWith("1 ") || upper.endsWith(" 1") || upper.equals("1");
            boolean isAway = (event.getAwayTeam() != null && upper.contains(event.getAwayTeam().toUpperCase())) ||
                             upper.contains("AWAY") || upper.startsWith("2 ") || upper.endsWith(" 2") || upper.equals("2");
            boolean isDraw = upper.contains("DRAW") || upper.contains("TIE") || upper.contains("EMPATE") ||
                             upper.contains(" X") || upper.startsWith("X ") || upper.equals("X");

            BetType betType = null;
            if (upper.contains("1X") || upper.contains("1-X") || upper.contains("1/X") ||
                "OT_ONEX".equals(type) || "1X".equals(type) || (isHome && isDraw && !isAway)) {
                betType = map1X2DCRecord("1X", scope, StatType.MATCH);
            } else if (upper.contains("12") || upper.contains("1-2") || upper.contains("1/2") ||
                       "OT_ONETWO".equals(type) || "12".equals(type) || (isHome && isAway && !isDraw)) {
                betType = map1X2DCRecord("12", scope, StatType.MATCH);
            } else if (upper.contains("X2") || upper.contains("2X") || upper.contains("X-2") || upper.contains("X/2") ||
                       "OT_XTWO".equals(type) || "X2".equals(type) || "2X".equals(type) || (isAway && isDraw && !isHome)) {
                betType = map1X2DCRecord("X2", scope, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }
}
