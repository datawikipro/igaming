package pro.datawiki.igaming.source.norsktipping.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.norsktipping.dto.NorskTippingEventDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NorskTippingOddsMapper {

    private static final String BOOKMAKER_NAME = "norsktipping";

    public OddsUpdateRequest mapToOddsUpdate(NorskTippingEventDto event) {
        if (event == null || event.getId() == null) {
            return null;
        }

        List<OddItem> items = new ArrayList<>();
        if (event.getOdds() != null) {
            for (Map.Entry<String, Double> entry : event.getOdds().entrySet()) {
                if (entry.getValue() == null || entry.getValue() <= 1.0) continue;
                String factor = mapFactor(entry.getKey());
                if (factor == null) continue;

                OddItem item = new OddItem();
                item.setFactorId(factor);
                item.setName(entry.getKey());
                item.setValue(entry.getValue());

                switch (factor) {
                    case "1" -> item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, null));
                    case "X" -> item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null));
                    case "2" -> item.setBetType(new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, null));
                    case "TB_2.5" -> item.setBetType(new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, 2.5, false, null));
                    case "TM_2.5" -> item.setBetType(new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, 2.5, false, null));
                    default -> {}
                }
                items.add(item);
            }
        }

        if (items.isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker(BOOKMAKER_NAME);
        request.setExternalEventId(event.getId());
        request.setEventUrl("https://www.norsk-tipping.no/oddsen/event/" + event.getId());
        request.setSportName(event.getSport() != null ? event.getSport() : "Soccer");
        request.setLeagueName("National League");
        request.setTeam1(event.getHomeTeam() != null ? event.getHomeTeam() : "Home");
        request.setTeam2(event.getAwayTeam() != null ? event.getAwayTeam() : "Away");
        request.setStartTime(event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis() + 7200000L);
        request.setIsLive(event.isLive());
        request.setOdds(items);

        return request;
    }

    private String mapFactor(String raw) {
        if (raw == null) return null;
        return switch (raw.toUpperCase()) {
            case "1", "HOME", "W1" -> "1";
            case "X", "DRAW" -> "X";
            case "2", "AWAY", "W2" -> "2";
            case "1X" -> "1X";
            case "12" -> "12";
            case "X2" -> "X2";
            case "OVER_2.5", "TO_2.5", "TB_2.5" -> "TB_2.5";
            case "UNDER_2.5", "TU_2.5", "TM_2.5" -> "TM_2.5";
            default -> raw;
        };
    }
}
