package pro.datawiki.igaming.source.iddaa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.iddaa.dto.IddaaEventDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class IddaaOddsMapper {

    private static final String BOOKMAKER_NAME = "iddaa";

    public OddsUpdateRequest mapToOddsUpdate(IddaaEventDto event) {
        if (event == null || event.getId() == null) {
            return null;
        }

        List<OddItem> items = new ArrayList<>();
        if (event.getOdds() != null) {
            for (Map.Entry<String, Double> entry : event.getOdds().entrySet()) {
                if (entry.getValue() == null || entry.getValue() <= 1.0) continue;
                String factor = entry.getKey();
                if (factor == null) continue;

                OddItem item = new OddItem();
                item.setFactorId(factor);
                item.setName(factor);
                item.setValue(entry.getValue());

                switch (factor) {
                    case "1" -> item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, null));
                    case "X" -> item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null));
                    case "2" -> item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, null));
                    case "1X" -> item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null));
                    case "12" -> item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null));
                    case "X2" -> item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null));
                    case "GG_YES", "BTTS_YES" -> item.setBetType(new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null));
                    case "GG_NO", "BTTS_NO" -> item.setBetType(new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null));
                    default -> {
                        if (factor.startsWith("TB_")) {
                            try {
                                double line = Double.parseDouble(factor.substring(3));
                                item.setBetType(new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, line, false, null));
                            } catch (Exception ignored) {}
                        } else if (factor.startsWith("TM_")) {
                            try {
                                double line = Double.parseDouble(factor.substring(3));
                                item.setBetType(new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, line, false, null));
                            } catch (Exception ignored) {}
                        } else if (factor.startsWith("H1_")) {
                            try {
                                double line = Double.parseDouble(factor.substring(3));
                                item.setBetType(new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, line, false, null));
                            } catch (Exception ignored) {}
                        } else if (factor.startsWith("H2_")) {
                            try {
                                double line = Double.parseDouble(factor.substring(3));
                                item.setBetType(new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, line, false, null));
                            } catch (Exception ignored) {}
                        }
                    }
                }
                if (item.getBetType() != null) {
                    items.add(item);
                }
            }
        }

        if (items.isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker(BOOKMAKER_NAME);
        request.setExternalEventId(event.getId());
        request.setSportName(event.getSport() != null ? event.getSport() : "Soccer");
        request.setLeagueName(event.getLeagueName() != null ? event.getLeagueName() : "General");
        request.setTeam1(event.getHomeTeam() != null ? event.getHomeTeam() : "Home");
        request.setTeam2(event.getAwayTeam() != null ? event.getAwayTeam() : "Away");
        request.setStartTime(event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis() + 7200000L);
        request.setIsLive(event.isLive());
        request.setEventUrl("https://www.iddaa.com/program/mac-detay/" + event.getId());
        request.setOdds(items);

        return request;
    }
}
