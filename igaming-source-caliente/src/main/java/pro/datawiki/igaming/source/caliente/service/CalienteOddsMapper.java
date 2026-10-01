package pro.datawiki.igaming.source.caliente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.caliente.dto.CalienteEventDto;
import pro.datawiki.igaming.source.caliente.dto.CalienteMarketDto;
import pro.datawiki.igaming.source.caliente.service.handler.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main Odds Mapper for Caliente Sportsbook (Mexico).
 * Uses OOP Strategy / Handler pattern to delegate market mapping to specialized handlers.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CalienteOddsMapper extends AbstractBetTypeMapper {

    private static final Pattern PRICE_DEC_PATTERN = Pattern.compile(
            "<button[^>]*class=\"[^\"]*price[^\"]*\"[^>]*>.*?<span class=\"seln-name\">([^<]+)</span>.*?<span class=\"price dec\"[^>]*>([0-9.]+)</span>",
            Pattern.DOTALL
    );

    private static final Pattern SIMPLE_PRICE_PATTERN = Pattern.compile(
            "<span class=\"price dec\"[^>]*>([0-9.]+)</span>"
    );

    private final List<CalienteMarketHandler> marketHandlers;

    @Autowired(required = false)
    private SportNormalizationService sportNormalizationService;

    public CalienteOddsMapper() {
        this(List.of(
                new MatchResultMarketHandler(),
                new DoubleChanceMarketHandler(),
                new TotalMarketHandler(),
                new HandicapMarketHandler()
        ));
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "caliente".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String market, String outcome, Double param) {
        return null;
    }

    /**
     * Maps a CalienteEventDto into a standardized OddsUpdateRequest.
     *
     * @param event Caliente event DTO
     * @return OddsUpdateRequest or null if mandatory event data is missing
     */
    public OddsUpdateRequest mapToOddsUpdateRequest(CalienteEventDto event) {
        if (event == null) {
            return null;
        }

        String homeTeam = event.getHomeTeam();
        String awayTeam = event.getAwayTeam();
        if ((homeTeam == null || homeTeam.isBlank()) && (awayTeam == null || awayTeam.isBlank())) {
            String rawTitle = event.getName() != null ? event.getName() : event.getTitle();
            if (rawTitle != null) {
                String[] parts = rawTitle.contains(" vs ") ? rawTitle.split(" vs ", 2) :
                                 rawTitle.contains(" v ") ? rawTitle.split(" v ", 2) :
                                 rawTitle.contains(" - ") ? rawTitle.split(" - ", 2) : null;
                if (parts != null && parts.length == 2) {
                    homeTeam = parts[0].trim();
                    awayTeam = parts[1].trim();
                }
            }
        }

        if (homeTeam == null || homeTeam.isBlank() || awayTeam == null || awayTeam.isBlank()) {
            log.debug("Skipping Caliente event without valid participants: id={}", event.getId());
            return null;
        }

        SportType sportType = resolveSportType(event.getSportName(), event.getLeagueName());
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("caliente");
        request.setRegions(List.of(BookmakerRegion.INT, BookmakerRegion.LATAM));
        request.setExternalEventId(event.getId() != null ? event.getId() : (homeTeam + "_" + awayTeam));
        request.setSportName(event.getSportName() != null ? event.getSportName() : sportType.name());
        request.setSportType(sportType);
        request.setLeagueName(event.getLeagueName() != null ? event.getLeagueName() : "General");
        request.setTeam1(homeTeam);
        request.setTeam2(awayTeam);
        request.setIsLive(Boolean.TRUE.equals(event.getIsLive()));
        request.setStartTime(event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis());
        request.setEventUrl(event.getEventUrl());

        List<OddItem> items = new ArrayList<>();
        if (event.getMarkets() != null && !event.getMarkets().isEmpty()) {
            for (CalienteMarketDto market : event.getMarkets()) {
                if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
                    continue;
                }
                marketHandlers.stream()
                        .filter(h -> h.supports(market, sportType))
                        .findFirst()
                        .ifPresent(h -> {
                            try {
                                h.handle(market, event, sportType, items);
                            } catch (Exception e) {
                                log.debug("Error mapping market '{}' for Caliente event {}: {}",
                                        market.getEffectiveName(), event.getId(), e.getMessage());
                            }
                        });
            }
        }

        request.setOdds(items);
        return request;
    }

    /**
     * Backward-compatible Playtech HTML parser for Caliente.
     */
    public OddsUpdateRequest mapHtmlToOddsUpdateRequest(MatchCache cache, String html) {
        if (cache == null || html == null) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("caliente");
        request.setExternalEventId(cache.getExternalId());
        request.setSportName(cache.getSportName() != null ? cache.getSportName() : "Soccer");
        request.setSportType(resolveSportType(cache.getSportName(), cache.getLeagueName()));
        request.setLeagueName(cache.getLeagueName() != null ? cache.getLeagueName() : "Caliente");
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
                        item.setFactorId("caliente_" + outcomeName.replaceAll("[^a-zA-Z0-9_+.-]", "_"));
                        item.setName(outcomeName);
                        item.setValue(price);
                        item.setBetType(betType);
                        odds.add(item);
                    }
                }
            } catch (NumberFormatException ignored) {}
        }

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

    private OddItem createOdd(String name, Double val, BetType bt) {
        OddItem item = new OddItem();
        item.setFactorId("caliente_" + name);
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
        if (lower.equals("1") || lower.startsWith("1 ") || lower.startsWith("local")) {
            return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN1, StatType.NONE);
        }
        if (lower.equals("2") || lower.startsWith("2 ") || lower.startsWith("visitante")) {
            return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.WIN2, StatType.NONE);
        }
        return null;
    }

    private SportType resolveSportType(String rawSport, String rawLeague) {
        SportType sportType = resolveSportType(rawSport);
        if ((sportType == SportType.UNKNOWN || sportType == SportType.ESPORTS) && rawLeague != null && !rawLeague.isBlank()) {
            SportType leagueSport = resolveSportType(rawLeague);
            if (leagueSport != SportType.UNKNOWN) {
                return leagueSport;
            }
        }
        return sportType;
    }

    private SportType resolveSportType(String rawSport) {
        if (rawSport == null || rawSport.isBlank()) {
            return SportType.FOOTBALL;
        }
        if (sportNormalizationService != null) {
            try {
                SportType normalized = sportNormalizationService.normalize(rawSport);
                if (normalized != null && normalized != SportType.UNKNOWN) {
                    return normalized;
                }
            } catch (Exception ignored) {}
        }

        String upper = rawSport.toUpperCase();
        if (upper.contains("SOCCER") || upper.contains("FOOTBALL") || upper.contains("FÚTBOL") || upper.contains("FUTBOL")) return SportType.FOOTBALL;
        if (upper.contains("BASKETBALL") || upper.contains("BALONCESTO") || upper.contains("BÁSQUETBOL") || upper.contains("BASQUETBOL")) return SportType.BASKETBALL;
        if (upper.contains("TENNIS") && !upper.contains("TABLE") && !upper.contains("MESA")) return SportType.TENNIS;
        if (upper.contains("TABLE TENNIS") || upper.contains("TENIS DE MESA") || upper.contains("PING PONG")) return SportType.TABLE_TENNIS;
        if (upper.contains("ICE HOCKEY") || upper.contains("HOCKEY") || upper.contains("HOCKEY SOBRE HIELO")) return SportType.HOCKEY;
        if (upper.contains("VOLLEYBALL") || upper.contains("VOLEIBOL") || upper.contains("VÓLEIBOL")) return SportType.VOLLEYBALL;
        if (upper.contains("BASEBALL") || upper.contains("BÉISBOL") || upper.contains("BEISBOL")) return SportType.BASEBALL;
        if (upper.contains("AMERICAN FOOTBALL") || upper.contains("NFL") || upper.contains("FÚTBOL AMERICANO") || upper.contains("FUTBOL AMERICANO")) return SportType.AMERICAN_FOOTBALL;
        if (upper.contains("HANDBALL") || upper.contains("BALONMANO")) return SportType.HANDBALL;
        if (upper.contains("MMA") || upper.contains("UFC") || upper.contains("BOXING") || upper.contains("BOXEO")) return SportType.MMA;
        if (upper.contains("CS2") || upper.contains("CS:GO") || upper.contains("CSGO") || upper.contains("COUNTER-STRIKE") || upper.contains("COUNTER STRIKE")) return SportType.CS2;
        if (upper.contains("DOTA")) return SportType.DOTA2;
        if (upper.contains("LEAGUE OF LEGENDS") || upper.contains("LOL")) return SportType.LEAGUE_OF_LEGENDS;
        if (upper.contains("VALORANT")) return SportType.VALORANT;
        if (upper.contains("RAINBOW SIX") || upper.contains("R6")) return SportType.RAINBOW_SIX;
        if (upper.contains("ROCKET LEAGUE")) return SportType.ROCKET_LEAGUE;
        if (upper.contains("CALL OF DUTY") || upper.contains("COD")) return SportType.CALL_OF_DUTY;
        if (upper.contains("OVERWATCH")) return SportType.OVERWATCH;
        if (upper.contains("PUBG")) return SportType.PUBG;
        if (upper.contains("STARCRAFT")) return SportType.STARCRAFT;
        if (upper.contains("MOBILE LEGENDS") || upper.contains("MLBB")) return SportType.MOBILE_LEGENDS;
        if (upper.contains("ESPORTS") || upper.contains("E-SPORTS") || upper.contains("DEPORTES ELECTRÓNICOS")) return SportType.ESPORTS;

        return SportType.FOOTBALL;
    }
}
