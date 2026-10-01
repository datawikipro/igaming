package pro.datawiki.igaming.source.bwin.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainFixture;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainOption;
import pro.datawiki.igaming.source.bwin.dto.entain.EntainOptionMarket;

import java.util.List;

@Component
@Order(40)
public class BwinMatchResultHandler extends AbstractBwinMarketHandler {

    @Override
    public boolean supports(EntainOptionMarket market, SportType sportType) {
        String name = getMarketName(market).toUpperCase();
        if (name.contains("CORNER") || name.contains("CARD") || name.contains("BOOKING")) {
            return false;
        }
        if (name.contains("DOUBLE CHANCE") || name.contains("DRAW NO BET") || name.contains("DNB")) {
            return false;
        }
        if (name.contains("MAP") || name.contains("ROUND") || name.contains("FIRST BLOOD")) {
            return false;
        }
        return name.contains("MATCH RESULT") || name.contains("1X2")
                || name.contains("MONEYLINE") || name.contains("HEAD TO HEAD")
                || name.contains("HEAD-TO-HEAD") || name.contains("WHO WILL WIN")
                || name.endsWith(" WINNER") || name.equals("WINNER");
    }

    @Override
    public void handle(EntainFixture fixture, EntainOptionMarket market, SportType sportType, List<OddItem> items) {
        if (market.getOptions() == null) return;

        String marketName = getMarketName(market);
        BetScope scope = resolveScope(marketName);

        boolean hasDraw = market.getOptions().stream().anyMatch(o -> {
            String on = getOptionName(o).toUpperCase();
            return "X".equals(on) || on.contains("DRAW") || on.contains("TIE") || on.equals("EMPATE");
        });

        for (EntainOption option : market.getOptions()) {
            Double odds = getOptionOdds(option);
            if (odds == null) continue;

            String optName = getOptionName(option);
            String oUpper = optName.toUpperCase();

            MatchResultBet.Outcome outcome = null;
            if ("1".equals(oUpper) || oUpper.contains("1") && !oUpper.contains("X") || oUpper.contains("HOME")) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            } else if ("X".equals(oUpper) || oUpper.contains("DRAW") || oUpper.contains("TIE") || oUpper.equals("EMPATE")) {
                outcome = MatchResultBet.Outcome.DRAW;
            } else if ("2".equals(oUpper) || oUpper.contains("2") && !oUpper.contains("X") || oUpper.contains("AWAY")) {
                outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            }

            if (outcome != null) {
                MatchResultBet betType = new MatchResultBet(scope, outcome, StatType.MATCH);
                addOddItem(items, option, marketName, optName, odds, betType);
            }
        }
    }
}
