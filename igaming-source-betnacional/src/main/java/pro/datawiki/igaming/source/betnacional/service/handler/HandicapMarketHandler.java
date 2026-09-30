package pro.datawiki.igaming.source.betnacional.service.handler;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalEventDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalMarketDto;
import pro.datawiki.igaming.source.betnacional.dto.BetnacionalOutcomeDto;

import java.util.List;

/**
 * Handler for Asian and European Handicap / Spread markets.
 */
@Component
public class HandicapMarketHandler extends AbstractBetnacionalMarketHandler {

    @Override
    public boolean supports(BetnacionalMarketDto market, SportType sportType) {
        if (isEsports(sportType)) return false;
        String mName = market.getEffectiveName().toUpperCase();
        if (mName.contains("CORNER") || mName.contains("ESCANT") || mName.contains("CARD") || mName.contains("CART")) return false;

        return mName.contains("HANDICAP") ||
               mName.contains("SPREAD") ||
               mName.contains("DESVANTAGEM") ||
               mName.contains("PONTOS DE VANTAGEM");
    }

    @Override
    public void handle(BetnacionalMarketDto market, BetnacionalEventDto event, SportType sportType, List<OddItem> items) {
        String mName = market.getEffectiveName().toUpperCase();
        BetScope scope = resolveScope(mName);
        boolean isAsian = mName.contains("ASIAN") || mName.contains("ASIATICO") || mName.contains("ASIÁTICO");

        for (BetnacionalOutcomeDto outcome : market.getOutcomes()) {
            Double odds = outcome.getEffectiveOdds();
            if (odds == null || odds <= 1.0) continue;

            String oName = outcome.getName() != null ? outcome.getName().trim() : "";
            Double hcp = extractNumber(oName, outcome.getHandicap(), mName);
            if (hcp == null) continue;

            String upper = oName.toUpperCase();
            BetType betType = null;
            if (isTeam1(upper, event) || upper.startsWith("1") || upper.startsWith("HOME") || upper.startsWith("CASA")) {
                betType = mapHandicapRecord("1", scope, StatType.MATCH, isAsian, hcp);
            } else if (isTeam2(upper, event) || upper.startsWith("2") || upper.startsWith("AWAY") || upper.startsWith("FORA")) {
                betType = mapHandicapRecord("2", scope, StatType.MATCH, isAsian, hcp);
            }

            if (betType != null) {
                String group = formatGroupName(isAsian ? "asian_handicap" : "handicap", scope);
                addOddItem(items, group, oName, odds, betType);
            }
        }
    }

    private boolean isTeam1(String name, BetnacionalEventDto event) {
        if (event.getHomeTeam() != null && name.contains(event.getHomeTeam().toUpperCase())) return true;
        return name.startsWith("1") || name.startsWith("HOME") || name.startsWith("CASA");
    }

    private boolean isTeam2(String name, BetnacionalEventDto event) {
        if (event.getAwayTeam() != null && name.contains(event.getAwayTeam().toUpperCase())) return true;
        return name.startsWith("2") || name.startsWith("AWAY") || name.startsWith("FORA");
    }
}
