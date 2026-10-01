package pro.datawiki.igaming.source.smarkets.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.smarkets.dto.*;
import pro.datawiki.igaming.source.smarkets.service.handler.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SmarketsOddsMapper extends AbstractBetTypeMapper {

    private final List<SmarketsMarketHandler> handlers;

    @Autowired
    public SmarketsOddsMapper(List<SmarketsMarketHandler> handlers) {
        this.handlers = handlers != null ? handlers : List.of();
    }

    public SmarketsOddsMapper() {
        this.handlers = List.of(
                new SmarketsCornersHandler(),
                new SmarketsCardsHandler(),
                new SmarketsEsportsHandler(),
                new SmarketsPeriodHandler(),
                new SmarketsHalfTimeFullTimeHandler(),
                new SmarketsMatchResultHandler(),
                new SmarketsDoubleChanceHandler(),
                new SmarketsDrawNoBetHandler(),
                new SmarketsTotalHandler(),
                new SmarketsHandicapHandler(),
                new SmarketsBttsHandler(),
                new SmarketsCorrectScoreHandler()
        );
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "smarkets".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(SmarketsEvent event,
                                                    List<SmarketsMarket> markets,
                                                    Map<String, List<SmarketsContract>> contractsByMarketId,
                                                    Map<String, SmarketsContractQuotes> quotesByContractId) {
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("smarkets");
        request.setRegions(List.of(BookmakerRegion.EU, BookmakerRegion.GLOBAL));
        request.setExternalEventId(event.getId());

        SportType sportType = resolveSportType(event.getType());
        request.setSportType(sportType);
        request.setSportName(sportType.name());
        request.setLeagueName(event.getSlug() != null ? event.getSlug() : "Smarkets");

        // Parse teams
        String team1 = "Home Team";
        String team2 = "Away Team";
        if (event.getName() != null) {
            String name = event.getName();
            if (name.contains(" vs. ")) {
                String[] parts = name.split(" vs\\. ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (name.contains(" vs ")) {
                String[] parts = name.split(" vs ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (name.contains(" - ")) {
                String[] parts = name.split(" - ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (name.contains(" @ ")) {
                String[] parts = name.split(" @ ", 2);
                team2 = parts[0].trim();
                team1 = parts[1].trim();
            } else if (name.contains(" v ")) {
                String[] parts = name.split(" v ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive("live".equalsIgnoreCase(event.getState()));
        if (event.getStartDatetime() != null) {
            try {
                request.setStartTime(Instant.parse(event.getStartDatetime()).toEpochMilli());
            } catch (Exception e) {
                request.setStartTime(Instant.now().toEpochMilli());
            }
        } else {
            request.setStartTime(Instant.now().toEpochMilli());
        }

        List<OddItem> oddItems = new ArrayList<>();

        if (markets != null) {
            for (SmarketsMarket market : markets) {
                List<SmarketsContract> contracts = contractsByMarketId.get(market.getId());
                if (contracts == null || contracts.isEmpty()) {
                    continue;
                }

                SmarketsMarketContext context = SmarketsMarketContext.builder()
                        .event(event)
                        .market(market)
                        .contracts(contracts)
                        .quotesByContractId(quotesByContractId)
                        .sportType(sportType)
                        .team1(team1)
                        .team2(team2)
                        .isLive(request.getIsLive())
                        .build();

                for (SmarketsMarketHandler handler : handlers) {
                    if (handler.supports(context)) {
                        handler.handle(context, oddItems);
                        break;
                    }
                }
            }
        }

        request.setOdds(oddItems);
        return request;
    }

    public SportType resolveSportType(String type) {
        if (type == null) return SportType.FOOTBALL;
        String t = type.toLowerCase();
        if (t.contains("cs2") || t.contains("cs:go") || t.contains("counter-strike")) return SportType.CS2;
        if (t.contains("dota")) return SportType.DOTA2;
        if (t.contains("league_of_legends") || t.contains("lol")) return SportType.LEAGUE_OF_LEGENDS;
        if (t.contains("valorant")) return SportType.VALORANT;
        if (t.contains("esport")) return SportType.ESPORTS;
        if (t.contains("american")) return SportType.AMERICAN_FOOTBALL;
        if (t.contains("table_tennis")) return SportType.TABLE_TENNIS;
        if (t.contains("tennis")) return SportType.TENNIS;
        if (t.contains("basketball")) return SportType.BASKETBALL;
        if (t.contains("baseball")) return SportType.BASEBALL;
        if (t.contains("hockey")) return SportType.HOCKEY;
        if (t.contains("cricket")) return SportType.CRICKET;
        if (t.contains("mma") || t.contains("ufc")) return SportType.MMA;
        if (t.contains("boxing")) return SportType.BOXING;
        if (t.contains("darts")) return SportType.DARTS;
        if (t.contains("snooker")) return SportType.SNOOKER;
        if (t.contains("rugby_league")) return SportType.RUGBY_LEAGUE;
        if (t.contains("rugby")) return SportType.RUGBY_UNION;
        if (t.contains("football")) return SportType.FOOTBALL;
        return SportType.FOOTBALL;
    }
}
