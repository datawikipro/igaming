package pro.datawiki.igaming.source.wplay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
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
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.wplay.dto.WplayEventDto;
import pro.datawiki.igaming.source.wplay.dto.WplayMarketDto;
import pro.datawiki.igaming.source.wplay.dto.WplayOutcomeDto;
import pro.datawiki.igaming.source.wplay.service.handler.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class WplayOddsMapper extends AbstractBetTypeMapper {

    private static final Pattern MARKET_BLOCK_PATTERN = Pattern.compile(
            "<div[^>]*class=\"[^\"]*(?:market-group|mkt-group|market-item|market|mkt)[^\"]*\"[^>]*>(.*?)</div>\\s*(?=<div[^>]*class=\"[^\"]*(?:market-group|mkt-group|market-item|market|mkt)[^\"]*\"|$)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MARKET_NAME_PATTERN = Pattern.compile(
            "<span[^>]*class=\"[^\"]*(?:market-title|market-name|header-title|title|name)[^\"]*\"[^>]*>([^<]+)</span>",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PRICE_DEC_PATTERN = Pattern.compile(
            "<button[^>]*class=\"[^\"]*price[^\"]*\"[^>]*>.*?<span class=\"seln-name\">([^<]+)</span>.*?<span class=\"price dec\"[^>]*>([0-9.]+)</span>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SIMPLE_PRICE_PATTERN = Pattern.compile(
            "<span class=\"price dec\"[^>]*>([0-9.]+)</span>",
            Pattern.CASE_INSENSITIVE
    );

    private final SportNormalizationService sportNormalizationService;
    private final UnmappedBetService unmappedBetService;
    private final List<WplayMarketHandler> marketHandlers;

    @Autowired
    public WplayOddsMapper(@Autowired(required = false) SportNormalizationService sportNormalizationService,
                            @Autowired(required = false) UnmappedBetService unmappedBetService,
                            @Autowired(required = false) List<WplayMarketHandler> marketHandlers) {
        this.sportNormalizationService = sportNormalizationService;
        this.unmappedBetService = unmappedBetService;

        if (marketHandlers == null || marketHandlers.isEmpty()) {
            List<WplayMarketHandler> defaults = new ArrayList<>(List.of(
                    new WplayEsportsHandler(),
                    new WplayStatsCornersHandler(),
                    new WplayStatsCardsHandler(),
                    new WplayTotalHandler(),
                    new WplayHandicapHandler(),
                    new WplayDoubleChanceHandler(),
                    new WplayBttsHandler(),
                    new WplayDrawNoBetHandler(),
                    new WplayCorrectScoreHandler(),
                    new WplayPeriodHandler(),
                    new WplayMatchResultHandler()
            ));
            AnnotationAwareOrderComparator.sort(defaults);
            this.marketHandlers = Collections.unmodifiableList(defaults);
        } else {
            List<WplayMarketHandler> sorted = new ArrayList<>(marketHandlers);
            AnnotationAwareOrderComparator.sort(sorted);
            this.marketHandlers = Collections.unmodifiableList(sorted);
        }
    }

    public WplayOddsMapper() {
        this(null, null, null);
    }

    public List<WplayMarketHandler> getMarketHandlers() {
        return marketHandlers;
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "wplay".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String market, String outcome, Double param) {
        return map(market, outcome, param, null);
    }

    public BetType map(String market, String outcome, Double param, SportType sportType) {
        if (market == null && outcome == null) return null;
        String mkt = (market != null && !market.isBlank()) ? market : "Resultado del partido";
        String out = (outcome != null) ? outcome : "";

        WplayMarketContext context = WplayMarketContext.builder()
                .sportName(sportType != null ? sportType.name() : "General")
                .sportType(sportType != null ? sportType : SportType.FOOTBALL)
                .build();

        WplayMarketDto marketDto = new WplayMarketDto();
        marketDto.setName(mkt);
        WplayOutcomeDto outcomeDto = new WplayOutcomeDto();
        outcomeDto.setName(out);
        outcomeDto.setPrice(2.0);
        outcomeDto.setParam(param);
        marketDto.setOutcomes(List.of(outcomeDto));

        List<OddItem> items = new ArrayList<>();
        for (WplayMarketHandler handler : marketHandlers) {
            if (handler.supports(mkt, context)) {
                try {
                    handler.handle(marketDto, context, items);
                    if (!items.isEmpty()) {
                        return items.get(0).getBetType();
                    }
                } catch (Exception e) {
                    log.warn("Error in map() for market '{}', outcome '{}': {}", mkt, out, e.getMessage());
                }
                break;
            }
        }

        return map1X2Record(out, BetScope.FULL_MATCH, StatType.NONE);
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cache, WplayEventDto event) {
        if (event == null) return null;

        String sportName = (cache != null && cache.getSportName() != null && !cache.getSportName().isBlank())
                ? cache.getSportName()
                : (event.getSportName() != null ? event.getSportName() : "Soccer");

        String leagueName = (cache != null && cache.getLeagueName() != null && !cache.getLeagueName().isBlank())
                ? cache.getLeagueName()
                : (event.getLeagueName() != null ? event.getLeagueName() : "Wplay Sports");

        String team1 = (cache != null && cache.getTeam1() != null && !cache.getTeam1().isBlank())
                ? cache.getTeam1()
                : event.getHomeTeam();

        String team2 = (cache != null && cache.getTeam2() != null && !cache.getTeam2().isBlank())
                ? cache.getTeam2()
                : event.getAwayTeam();

        Long startTime = (cache != null && cache.getStartTime() != null && cache.getStartTime() > 0)
                ? cache.getStartTime()
                : (event.getStartTime() != null ? event.getStartTime() : System.currentTimeMillis());

        boolean isLive = (cache != null && cache.getIsLive() != null)
                ? Boolean.TRUE.equals(cache.getIsLive())
                : Boolean.TRUE.equals(event.getIsLive());

        SportType sportType = (sportNormalizationService != null)
                ? sportNormalizationService.normalize(sportName)
                : resolveSportType(sportName);

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("wplay");
        request.setExternalEventId(event.getId() != null ? event.getId() : (cache != null ? cache.getExternalId() : ""));
        request.setSportName(sportName);
        request.setSportType(sportType);
        request.setLeagueName(leagueName);
        request.setTeam1(team1);
        request.setTeam2(team2);
        request.setIsLive(isLive);
        request.setStartTime(startTime);
        request.setRegions(List.of(BookmakerRegion.LATAM, BookmakerRegion.GLOBAL));

        WplayMarketContext context = WplayMarketContext.builder()
                .eventId(request.getExternalEventId())
                .sportName(sportName)
                .sportType(sportType)
                .leagueName(leagueName)
                .homeTeam(team1)
                .awayTeam(team2)
                .isLive(isLive)
                .build();

        List<OddItem> items = new ArrayList<>();
        if (event.getMarkets() != null) {
            for (WplayMarketDto market : event.getMarkets()) {
                if (market == null || market.getName() == null) continue;

                boolean handled = false;
                for (WplayMarketHandler handler : marketHandlers) {
                    if (handler.supports(market.getName(), context)) {
                        try {
                            handler.handle(market, context, items);
                        } catch (Exception e) {
                            log.warn("Error handling market '{}': {}", market.getName(), e.getMessage());
                        }
                        handled = true;
                        break;
                    }
                }

                if (!handled && unmappedBetService != null) {
                    unmappedBetService.saveAndNotify("wplay", sportName, market.getName(), market.getName(), request.getExternalEventId());
                }
            }
        }

        request.setOdds(items);
        return request;
    }

    public OddsUpdateRequest mapHtmlToOddsUpdateRequest(MatchCache cache, String html) {
        if (cache == null || html == null) return null;

        String sportName = cache.getSportName() != null ? cache.getSportName() : "Soccer";
        SportType sportType = (sportNormalizationService != null)
                ? sportNormalizationService.normalize(sportName)
                : resolveSportType(sportName);

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("wplay");
        request.setExternalEventId(cache.getExternalId());
        request.setSportName(sportName);
        request.setSportType(sportType);
        request.setLeagueName(cache.getLeagueName() != null ? cache.getLeagueName() : "Wplay");
        request.setTeam1(cache.getTeam1());
        request.setTeam2(cache.getTeam2());
        request.setIsLive(Boolean.TRUE.equals(cache.getIsLive()));
        request.setStartTime(cache.getStartTime() != null ? cache.getStartTime() : System.currentTimeMillis());
        request.setRegions(List.of(BookmakerRegion.LATAM, BookmakerRegion.GLOBAL));

        WplayMarketContext context = WplayMarketContext.builder()
                .eventId(cache.getExternalId())
                .sportName(sportName)
                .sportType(sportType)
                .leagueName(request.getLeagueName())
                .homeTeam(cache.getTeam1())
                .awayTeam(cache.getTeam2())
                .isLive(request.getIsLive())
                .build();

        List<OddItem> odds = new ArrayList<>();
        List<WplayMarketDto> extractedMarkets = extractMarketsFromHtml(html);

        if (!extractedMarkets.isEmpty()) {
            for (WplayMarketDto market : extractedMarkets) {
                boolean handled = false;
                for (WplayMarketHandler handler : marketHandlers) {
                    if (handler.supports(market.getName(), context)) {
                        try {
                            handler.handle(market, context, odds);
                        } catch (Exception e) {
                            log.warn("Error handling market '{}': {}", market.getName(), e.getMessage());
                        }
                        handled = true;
                        break;
                    }
                }

                if (!handled && unmappedBetService != null) {
                    unmappedBetService.saveAndNotify("wplay", sportName, market.getName(), market.getName(), cache.getExternalId());
                }
            }
        }

        // If no market blocks yielded odds, extract all buttons with price dec and fallback
        if (odds.isEmpty()) {
            List<WplayOutcomeDto> flatOutcomes = new ArrayList<>();
            Matcher m = PRICE_DEC_PATTERN.matcher(html);
            while (m.find()) {
                String outcomeName = m.group(1).trim();
                String priceStr = m.group(2).trim();
                try {
                    double price = Double.parseDouble(priceStr);
                    if (price > 1.0) {
                        flatOutcomes.add(new WplayOutcomeDto("wplay_" + outcomeName, outcomeName, price, null, null));
                    }
                } catch (NumberFormatException ignored) {}
            }

            if (!flatOutcomes.isEmpty()) {
                WplayMarketDto fallbackMarket = new WplayMarketDto("mkt_main", "Resultado del partido", "Main", flatOutcomes);
                for (WplayMarketHandler handler : marketHandlers) {
                    if (handler.supports(fallbackMarket.getName(), context)) {
                        handler.handle(fallbackMarket, context, odds);
                        break;
                    }
                }
            }
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

    private List<WplayMarketDto> extractMarketsFromHtml(String html) {
        List<WplayMarketDto> list = new ArrayList<>();
        Matcher blockMatcher = MARKET_BLOCK_PATTERN.matcher(html);
        int mktIndex = 1;

        while (blockMatcher.find()) {
            String blockHtml = blockMatcher.group(1);
            String marketName = "Resultado del partido";

            Matcher nameMatcher = MARKET_NAME_PATTERN.matcher(blockHtml);
            if (nameMatcher.find()) {
                marketName = nameMatcher.group(1).trim();
            }

            List<WplayOutcomeDto> outcomes = new ArrayList<>();
            Matcher priceMatcher = PRICE_DEC_PATTERN.matcher(blockHtml);
            while (priceMatcher.find()) {
                String outName = priceMatcher.group(1).trim();
                String priceStr = priceMatcher.group(2).trim();
                try {
                    double price = Double.parseDouble(priceStr);
                    if (price > 1.0) {
                        outcomes.add(new WplayOutcomeDto("wplay_" + outName, outName, price, null, null));
                    }
                } catch (NumberFormatException ignored) {}
            }

            if (!outcomes.isEmpty()) {
                list.add(new WplayMarketDto("mkt_" + (mktIndex++), marketName, "General", outcomes));
            }
        }

        return list;
    }

    private OddItem createOdd(String name, Double val, BetType bt) {
        OddItem item = new OddItem();
        item.setFactorId("wplay_" + name);
        item.setName(name);
        item.setValue(val);
        item.setBetType(bt);
        return item;
    }

    private SportType resolveSportType(String sportName) {
        if (sportName == null) return SportType.FOOTBALL;
        String s = sportName.toLowerCase(Locale.ROOT);
        if (s.contains("basket") || s.contains("baloncesto")) return SportType.BASKETBALL;
        if (s.contains("hockey") || s.contains("iceh")) return SportType.HOCKEY;
        if (s.contains("base") || s.contains("béisbol")) return SportType.BASEBALL;
        if (s.contains("tennis") || s.contains("tenis")) return SportType.TENNIS;
        if (s.contains("esport") || s.contains("cs2") || s.contains("dota") || s.contains("lol")) return SportType.ESPORTS;
        return SportType.FOOTBALL;
    }
}
