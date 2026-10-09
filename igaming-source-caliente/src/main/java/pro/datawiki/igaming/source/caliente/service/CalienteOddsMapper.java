package pro.datawiki.igaming.source.caliente.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.*;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.caliente.dto.CalienteEventDto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CalienteOddsMapper {

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cache, CalienteEventDto event) {
        List<OddItem> items = mapOdds(event);
        if (items.isEmpty()) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("caliente");
        request.setExternalEventId(cache.getExternalId());
        request.setOdds(items);
        return request;
    }

    public List<OddItem> mapOdds(CalienteEventDto event) {
        List<OddItem> items = new ArrayList<>();
        if (event == null || event.getMarkets() == null) {
            return items;
        }

        for (CalienteEventDto.CalienteMarketDto market : event.getMarkets()) {
            if (market.getOutcomes() == null) continue;
            String marketName = market.getName() != null ? market.getName().toLowerCase() : "";

            for (CalienteEventDto.CalienteOutcomeDto outcome : market.getOutcomes()) {
                if (outcome.getOdds() == null || outcome.getOdds() <= 1.0) continue;
                String outcomeName = outcome.getName() != null ? outcome.getName().toLowerCase() : "";

                OddItem item = new OddItem();
                item.setFactorId(outcome.getId() != null ? outcome.getId() : outcome.getName());
                item.setGroupName(market.getName());
                item.setName(outcome.getName());
                item.setValue(outcome.getOdds());

                // 1X2 Match Winner (supporting multi-language French, Spanish, English)
                if (marketName.contains("1x2") || marketName.contains("winner") || marketName.contains("vainqueur") || marketName.contains("rГ©sultat") || marketName.contains("ganador") || marketName.contains("resultado")) {
                    if (outcomeName.contains("1") || outcomeName.contains("home") || outcomeName.contains("domicile") || outcomeName.contains("local")) {
                        item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, null));
                        items.add(item);
                    } else if (outcomeName.contains("x") || outcomeName.contains("draw") || outcomeName.contains("nul") || outcomeName.contains("empate")) {
                        item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null));
                        items.add(item);
                    } else if (outcomeName.contains("2") || outcomeName.contains("away") || outcomeName.contains("extГ©rieur") || outcomeName.contains("visitante")) {
                        item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, null));
                        items.add(item);
                    }
                } 
                // Totals
                else if (marketName.contains("total") || marketName.contains("plus/moins") || marketName.contains("mГЎs/menos") || marketName.contains("over") || marketName.contains("under")) {
                    double line = outcome.getLine() != null ? outcome.getLine() : (market.getLine() != null ? market.getLine() : 2.5);
                    if (outcomeName.contains("over") || outcomeName.contains("plus") || outcomeName.contains("mГЎs") || outcomeName.contains("+")) {
                        item.setBetType(new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, line, false, null));
                        items.add(item);
                    } else if (outcomeName.contains("under") || outcomeName.contains("moins") || outcomeName.contains("menos") || outcomeName.contains("-")) {
                        item.setBetType(new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, line, false, null));
                        items.add(item);
                    }
                } 
                // Both teams to score
                else if (marketName.contains("both teams") || marketName.contains("btts") || marketName.contains("les deux Г©quipes") || marketName.contains("ambos equipos")) {
                    if (outcomeName.contains("yes") || outcomeName.contains("oui") || outcomeName.contains("sГ­") || outcomeName.contains("si")) {
                        item.setBetType(new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null));
                        items.add(item);
                    } else if (outcomeName.contains("no") || outcomeName.contains("non")) {
                        item.setBetType(new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null));
                        items.add(item);
                    }
                }
            }
        }

        return items;
    }
}