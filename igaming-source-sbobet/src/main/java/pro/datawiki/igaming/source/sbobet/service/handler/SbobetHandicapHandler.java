package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.List;

@Component
@Order(80)
public class SbobetHandicapHandler extends AbstractSbobetMarketHandler {

    @Override
    public boolean supports(String marketKey) {
        return "handicaps".equalsIgnoreCase(marketKey) || "handicaps_half1".equalsIgnoreCase(marketKey);
    }

    @Override
    public boolean supports(String marketKey, pro.datawiki.igaming.dto.SportType sportType) {
        if (SbobetEsportsHandler.isEsports(sportType)) {
            return false;
        }
        return supports(marketKey);
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null || !marketNode.isArray()) return;

        for (JsonNode hdpNode : marketNode) {
            boolean isHalf1 = hdpNode.path("isHalf1").asBoolean(false);
            BetScope scope = isHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
            String groupName = isHalf1 ? "handicap_half_1" : "handicap";
            double hdp = hdpNode.path("hdp").asDouble();

            addOddItem(items, groupName, "HOME (" + hdp + ")", hdpNode.path("home").asDouble(),
                    new HandicapBet(scope, HandicapBet.Outcome.TEAM1, hdp, true, StatType.MATCH));
            addOddItem(items, groupName, "AWAY (" + (-hdp) + ")", hdpNode.path("away").asDouble(),
                    new HandicapBet(scope, HandicapBet.Outcome.TEAM2, -hdp, true, StatType.MATCH));
        }
    }
}
