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
@Order(50)
public class BwinDoubleChanceHandler extends AbstractBwinMarketHandler {

    @Override
    public boolean supports(EntainOptionMarket market, SportType sportType) {
        String name = getMarketName(market).toUpperCase();
        if (name.contains("CORNER") || name.contains("CARD") || name.contains("BOOKING")) {
            return false;
        }
        return name.contains("DOUBLE CHANCE");
    }

    @Override
    public void handle(EntainFixture fixture, EntainOptionMarket market, SportType sportType, List<OddItem> items) {
        if (market.getOptions() == null) return;

        String marketName = getMarketName(market);
        BetScope scope = resolveScope(marketName);

        for (EntainOption option : market.getOptions()) {
            Double odds = getOptionOdds(option);
            if (odds == null) continue;

            String optName = getOptionName(option);
            String oUpper = optName.toUpperCase().replace(" ", "");

            MatchResultBet.Outcome outcome = null;
            if (oUpper.contains("1X") || oUpper.contains("1ORX") || (oUpper.contains("HOME") && oUpper.contains("DRAW"))) {
                outcome = MatchResultBet.Outcome.DC_1X;
            } else if (oUpper.contains("12") || oUpper.contains("1OR2") || (oUpper.contains("HOME") && oUpper.contains("AWAY"))) {
                outcome = MatchResultBet.Outcome.DC_12;
            } else if (oUpper.contains("X2") || oUpper.contains("2X") || oUpper.contains("XOR2") || (oUpper.contains("DRAW") && oUpper.contains("AWAY"))) {
                outcome = MatchResultBet.Outcome.DC_X2;
            }

            if (outcome != null) {
                MatchResultBet betType = new MatchResultBet(scope, outcome, StatType.MATCH);
                addOddItem(items, option, marketName, optName, odds, betType);
            }
        }
    }
}
