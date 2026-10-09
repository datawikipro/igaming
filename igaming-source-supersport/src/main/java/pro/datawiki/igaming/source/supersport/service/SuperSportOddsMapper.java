package pro.datawiki.igaming.source.supersport.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.*;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.supersport.dto.SuperSportEventDto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SuperSportOddsMapper {

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cache, SuperSportEventDto event) {
        List<OddItem> items = mapOdds(event);
        if (items.isEmpty()) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("supersport");
        request.setExternalEventId(cache.getExternalId());
        request.setOdds(items);
        return request;
    }

    public List<OddItem> mapOdds(SuperSportEventDto event) {
        List<OddItem> items = new ArrayList<>();
        if (event == null || event.getMarkets() == null) {
            return items;
        }

        for (SuperSportEventDto.SuperSportMarketDto market : event.getMarkets()) {
            if (market.getOutcomes() == null) continue;
            String marketName = market.getName() != null ? market.getName().toLowerCase() : "";

            for (SuperSportEventDto.SuperSportOutcomeDto outcome : market.getOutcomes()) {
                if (outcome.getOdds() == null || outcome.getOdds() <= 1.0) continue;
                String outcomeName = outcome.getName() != null ? outcome.getName().toLowerCase() : "";

                OddItem item = new OddItem();
                item.setFactorId(outcome.getId() != null ? outcome.getId() : outcome.getName());
                item.setGroupName(market.getName());
                item.setName(outcome.getName());
                item.setValue(outcome.getOdds());

                if (marketName.contains("1x2") || marketName.contains("winner") || marketName.contains("match result") || marketName.contains("stavka") || marketName.contains("ishod")) {
                    if (outcomeName.contains("1") || outcomeName.contains("home") || outcomeName.contains("domacin") || outcomeName.contains("domaci")) {
                        item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, null));
                        items.add(item);
                    } else if (outcomeName.contains("x") || outcomeName.contains("draw") || outcomeName.contains("remi") || outcomeName.contains("nerieseno")) {
                        item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null));
                        items.add(item);
                    } else if (outcomeName.contains("2") || outcomeName.contains("away") || outcomeName.contains("gost") || outcomeName.contains("hostia")) {
                        item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, null));
                        items.add(item);
                    }
                } else if (marketName.contains("total") || marketName.contains("over") || marketName.contains("under") || marketName.contains("manj") || marketName.contains("vis")) {
                    double line = outcome.getLine() != null ? outcome.getLine() : (market.getLine() != null ? market.getLine() : 2.5);
                    if (outcomeName.contains("over") || outcomeName.contains("vise") || outcomeName.contains("viac") || outcomeName.contains("vec")) {
                        item.setBetType(new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, line, false, null));
                        items.add(item);
                    } else if (outcomeName.contains("under") || outcomeName.contains("manje") || outcomeName.contains("menej") || outcomeName.contains("manj")) {
                        item.setBetType(new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, line, false, null));
                        items.add(item);
                    }
                } else if (marketName.contains("both teams") || marketName.contains("btts") || marketName.contains("oba tima")) {
                    if (outcomeName.contains("yes") || outcomeName.contains("da") || outcomeName.contains("ano")) {
                        item.setBetType(new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null));
                        items.add(item);
                    } else if (outcomeName.contains("no") || outcomeName.contains("ne") || outcomeName.contains("nie")) {
                        item.setBetType(new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null));
                        items.add(item);
                    }
                }
            }
        }

        return items;
    }
}
