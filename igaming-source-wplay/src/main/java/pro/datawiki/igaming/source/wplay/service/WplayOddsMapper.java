package pro.datawiki.igaming.source.wplay.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.service.handler.WplayMarketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main Wplay odds mapper.
 * Supports two modes:
 * 1. DTO-based: mapEventToOddsUpdateRequest(WplayEventDto) — uses chain of WplayMarketHandler.
 * 2. HTML-based (legacy): mapHtmlToOddsUpdateRequest(MatchCache, html) — regex-based HTML parsing.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WplayOddsMapper extends AbstractBetTypeMapper {

    /** Chain of market handlers (injected and ordered via @Order annotations). */
    private final List<WplayMarketHandler> handlers;

    private static final Pattern PRICE_DEC_PATTERN = Pattern.compile(
            "<button[^>]*class=\"[^\"]*price[^\"]*\"[^>]*>.*?<span class=\"seln-name\">([^<]+)</span>.*?<span class=\"price dec\"[^>]*>([0-9.]+)</span>",
            Pattern.DOTALL
    );

    private static final Pattern SIMPLE_PRICE_PATTERN = Pattern.compile(
            "<span class=\"price dec\"[^>]*>([0-9.]+)</span>"
    );

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "wplay".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return map1X2Record(o, BetScope.FULL_MATCH, StatType.NONE);
    }

    // -------------------------------------------------------------------------
    // DTO-based mapping (primary pipeline)
    // -------------------------------------------------------------------------

    /**
     * Maps a WplayEventDto (JSON API response) to a normalized OddsUpdateRequest
     * using the chain of WplayMarketHandler beans.
     *
     * @param event WplayEventDto parsed from the SBTech JSON API.
     * @return OddsUpdateRequest ready for Kafka / aggregator, or null if event is null.
     */
    public OddsUpdateRequest mapEventToOddsUpdateRequest(WplayEventDto event) {
        if (event == null) return null;

        SportType sportType = resolveSportType(event.getSportName());

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("wplay");
        request.setExternalEventId(event.getId());
        request.setSportName(event.getSportName() != null ? event.getSportName() : "Soccer");
        request.setSportType(sportType);
        request.setLeagueName(event.getLeagueName() != null ? event.getLeagueName() : "Wplay");
        request.setTeam1(event.getHomeTeam());
        request.setTeam2(event.getAwayTeam());
        request.setIsLive(Boolean.TRUE.equals(event.getIsLive()));
        request.setStartTime(event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis());
        request.setRegions(List.of(BookmakerRegion.LATAM, BookmakerRegion.GLOBAL));

        List<OddItem> odds = new ArrayList<>();
        for (WplayMarketDto market : event.getMarkets()) {
            if (market.getOutcomes() == null || market.getOutcomes().isEmpty()) continue;
            for (WplayMarketHandler handler : handlers) {
                if (handler.supports(market, sportType)) {
                    handler.handle(market, event, sportType, odds);
                    break; // First matching handler wins (chain-of-responsibility)
                }
            }
        }

        request.setOdds(odds);
        return request;
    }

    // -------------------------------------------------------------------------
    // HTML-based mapping (legacy, for backward compatibility)
    // -------------------------------------------------------------------------

    /**
     * Maps Wplay HTML fragment to an OddsUpdateRequest (legacy HTML-scraping path).
     *
     * @param cache  MatchCache entry (stores team names, event IDs, sport type).
     * @param html   Raw HTML of the betting market.
     * @return OddsUpdateRequest or null if cache/html is missing.
     */
    public OddsUpdateRequest mapHtmlToOddsUpdateRequest(MatchCache cache, String html) {
        if (cache == null || html == null) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("wplay");
        request.setExternalEventId(cache.getExternalId());
        request.setSportName(cache.getSportName() != null ? cache.getSportName() : "Soccer");
        request.setSportType(SportType.FOOTBALL);
        request.setLeagueName(cache.getLeagueName() != null ? cache.getLeagueName() : "Wplay");
        request.setTeam1(cache.getTeam1());
        request.setTeam2(cache.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cache.getIsLive()));
        request.setStartTime(cache.getStartTime() != null ? cache.getStartTime() : System.currentTimeMillis());
        request.setRegions(List.of(BookmakerRegion.LATAM, BookmakerRegion.GLOBAL));

        List<OddItem> odds = new ArrayList<>();
        Matcher m = PRICE_DEC_PATTERN.matcher(html);
        while (m.find()) {
            String outcomeName = m.group(1).trim();
            String priceStr = m.group(2).trim();
            try {
                double price = Double.parseDouble(priceStr);
                if (price > 1.0) {
                    BetType betType = resolveBetType(outcomeName, cache.getTeam1(), cache.getTeam2());
                    if (betType != null) {
                        OddItem item = new OddItem();
                        item.setFactorId("wplay_" + outcomeName);
                        item.setName(outcomeName);
                        item.setValue(price);
                        item.setBetType(betType);
                        odds.add(item);
                    }
                }
            } catch (NumberFormatException ignored) {}
        }

        // If named pattern did not yield odds, try simple decimal odds fallback
        if (odds.isEmpty()) {
            Matcher mSimple = SIMPLE_PRICE_PATTERN.matcher(html);
            List<Double> prices = new ArrayList<>();
            while (mSimple.find() && prices.size() < 3) {
                try {
                    double p = Double.parseDouble(mSimple.group(1).trim());
                    if (p > 1.0) prices.add(p);
                } catch (NumberFormatException ignored) {}
            }
            if (prices.size() >= 3) {
                odds.add(createOdd("1", prices.get(0), new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, StatType.NONE)));
                odds.add(createOdd("X", prices.get(1), new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, StatType.NONE)));
                odds.add(createOdd("2", prices.get(2), new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, StatType.NONE)));
            } else if (prices.size() == 2) {
                odds.add(createOdd("1", prices.get(0), new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, StatType.NONE)));
                odds.add(createOdd("2", prices.get(1), new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, StatType.NONE)));
            }
        }

        request.setOdds(odds);
        return request;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Resolves SportType from sport name string (Spanish/English).
     */
    private SportType resolveSportType(String sportName) {
        if (sportName == null) return SportType.FOOTBALL;
        String upper = sportName.toUpperCase();
        if (upper.contains("FÚTBOL") || upper.contains("FUTBOL") || upper.contains("SOCCER") || upper.contains("FOOTBALL")) {
            return SportType.FOOTBALL;
        }
        if (upper.contains("BALONCESTO") || upper.contains("BASKETBALL") || upper.contains("NBA")) {
            return SportType.BASKETBALL;
        }
        if (upper.contains("TENIS") || upper.contains("TENNIS")) {
            return SportType.TENNIS;
        }
        if (upper.contains("BÉISBOL") || upper.contains("BEISBOL") || upper.contains("BASEBALL")) {
            return SportType.BASEBALL;
        }
        if (upper.contains("HOCKEY")) {
            return SportType.ICE_HOCKEY;
        }
        if (upper.contains("VOLEIBOL") || upper.contains("VOLLEY") || upper.contains("VOLLEYBALL")) {
            return SportType.VOLLEYBALL;
        }
        if (upper.contains("RUGBY")) {
            return SportType.RUGBY;
        }
        if (upper.contains("CS2") || upper.contains("COUNTER-STRIKE") || upper.contains("COUNTER STRIKE")) {
            return SportType.CS2;
        }
        if (upper.contains("DOTA")) {
            return SportType.DOTA2;
        }
        if (upper.contains("LEAGUE OF LEGENDS") || upper.contains("LOL")) {
            return SportType.LEAGUE_OF_LEGENDS;
        }
        if (upper.contains("VALORANT")) {
            return SportType.VALORANT;
        }
        if (upper.contains("ESPORT") || upper.contains("E-SPORT")) {
            return SportType.ESPORTS;
        }
        return SportType.FOOTBALL;
    }

    private OddItem createOdd(String name, Double val, BetType bt) {
        OddItem item = new OddItem();
        item.setFactorId("wplay_" + name);
        item.setName(name);
        item.setValue(val);
        item.setBetType(bt);
        return item;
    }

    private BetType resolveBetType(String outcomeName, String team1, String team2) {
        String lower = outcomeName.toLowerCase();
        if (lower.contains("empate") || lower.contains("draw") || lower.equals("x")) {
            return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, StatType.NONE);
        }
        if (team1 != null && (lower.contains(team1.toLowerCase()) || team1.toLowerCase().contains(lower))) {
            return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, StatType.NONE);
        }
        if (team2 != null && (lower.contains(team2.toLowerCase()) || team2.toLowerCase().contains(lower))) {
            return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, StatType.NONE);
        }
        if (lower.equals("1") || lower.startsWith("1 ")) {
            return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, StatType.NONE);
        }
        if (lower.equals("2") || lower.startsWith("2 ")) {
            return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, StatType.NONE);
        }
        return null;
    }
}
