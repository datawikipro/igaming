package pro.datawiki.igaming.source.fanduel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.fanduel.dto.FanDuelEventGroupResponse;
import pro.datawiki.igaming.source.fanduel.service.mapper.FanDuelMarketContext;
import pro.datawiki.igaming.source.fanduel.service.mapper.FanDuelMarketHandler;
import pro.datawiki.igaming.source.fanduel.service.mapper.FanDuelScopeResolver;
import pro.datawiki.igaming.source.fanduel.service.mapper.FanDuelStatTypeResolver;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class FanDuelOddsMapper extends AbstractBetTypeMapper {

    private static final Pattern LINE_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;
    private final FanDuelScopeResolver scopeResolver;
    private final FanDuelStatTypeResolver statTypeResolver;
    private final List<FanDuelMarketHandler> marketHandlers;

    public FanDuelOddsMapper(UnmappedBetService unmappedBetService,
                             SportNormalizationService sportNormalizationService,
                             @Lazy BetTypeResolverService betTypeResolver,
                             FanDuelScopeResolver scopeResolver,
                             FanDuelStatTypeResolver statTypeResolver,
                             List<FanDuelMarketHandler> marketHandlers) {
        this.unmappedBetService = unmappedBetService;
        this.sportNormalizationService = sportNormalizationService;
        this.betTypeResolver = betTypeResolver;
        this.scopeResolver = scopeResolver;
        this.statTypeResolver = statTypeResolver;

        List<FanDuelMarketHandler> sorted = new ArrayList<>(marketHandlers != null ? marketHandlers : List.of());
        AnnotationAwareOrderComparator.sort(sorted);
        this.marketHandlers = Collections.unmodifiableList(sorted);
    }

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "fanduel".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public List<FanDuelMarketHandler> getMarketHandlers() {
        return marketHandlers;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(FanDuelEventGroupResponse.FanDuelEvent event,
                                                    FanDuelEventGroupResponse response,
                                                    String sportName,
                                                    String leagueName) {
        if (event == null) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("fanduel");
        request.setRegions(List.of(BookmakerRegion.US, BookmakerRegion.GLOBAL));
        request.setExternalEventId(String.valueOf(event.getEventId()));

        if (event.getOpenDate() != null) {
            try {
                request.setStartTime(Instant.parse(event.getOpenDate()).toEpochMilli());
            } catch (Exception e) {
                log.warn("Failed to parse start date '{}'", event.getOpenDate());
            }
        }

        request.setSportName(sportName);
        SportType sportType = sportNormalizationService != null ? sportNormalizationService.normalize(sportName) : SportType.FOOTBALL;
        request.setSportType(sportType);
        request.setLeagueName(leagueName);

        String team1 = null;
        String team2 = null;
        if (event.getName() != null) {
            String[] parts = event.getName().split(" @ ");
            if (parts.length == 2) {
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else {
                parts = event.getName().split(" vs ");
                if (parts.length == 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                } else {
                    parts = event.getName().split(" v ");
                    if (parts.length == 2) {
                        team1 = parts[0].trim();
                        team2 = parts[1].trim();
                    } else {
                        parts = event.getName().split(" - ");
                        if (parts.length == 2) {
                            team1 = parts[0].trim();
                            team2 = parts[1].trim();
                        }
                    }
                }
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive(Boolean.TRUE.equals(event.getInPlay()) || "Started".equalsIgnoreCase(event.getEventStatus()));
        request.setEventUrl("https://sportsbook.fanduel.com/event/" + event.getEventId());

        List<OddItem> oddsList = new ArrayList<>();
        if (response != null && response.getAttachments() != null) {
            FanDuelEventGroupResponse.Attachments att = response.getAttachments();
            if (att.getMarkets() != null) {
                for (FanDuelEventGroupResponse.FanDuelMarket market : att.getMarkets().values()) {
                    if (market.getEventId() != null && market.getEventId().equals(event.getEventId())) {
                        if (market.getRunners() != null && !market.getRunners().isEmpty()) {
                            for (FanDuelEventGroupResponse.FanDuelRunner runner : market.getRunners()) {
                                processRunnerOutcome(event, market.getMarketName(), market, runner, sportType, sportName, leagueName, team1, team2, oddsList);
                            }
                        } else if (att.getSelections() != null) {
                            for (FanDuelEventGroupResponse.FanDuelSelection selection : att.getSelections().values()) {
                                if (market.getMarketId().equals(selection.getMarketId())) {
                                    processOutcome(event, market.getMarketName(), market, selection, sportType, sportName, leagueName, team1, team2, oddsList);
                                }
                            }
                        }
                    }
                }
            }
        }
        request.setOdds(oddsList);
        return request;
    }

    private void processOutcome(FanDuelEventGroupResponse.FanDuelEvent event,
                                String marketName,
                                FanDuelEventGroupResponse.FanDuelMarket market,
                                FanDuelEventGroupResponse.FanDuelSelection selection,
                                SportType sportType,
                                String sportName,
                                String leagueName,
                                String team1,
                                String team2,
                                List<OddItem> oddsList) {
        if (selection == null || "SUSPENDED".equalsIgnoreCase(selection.getStatus())) return;

        Double decimalOdds = selection.getTrueOdds();
        if (decimalOdds == null && selection.getPrice() != null) {
            decimalOdds = selection.getPrice().getDecimal();
            if (decimalOdds == null && selection.getPrice().getAmerican() != null) {
                decimalOdds = americanToDecimal(selection.getPrice().getAmerican());
            }
        }
        if (decimalOdds == null || decimalOdds <= 1.0) return;

        String runnerName = selection.getName();
        if (runnerName == null) runnerName = "Outcome " + selection.hashCode();

        Double line = resolveLine(selection.getHandicap(), market, runnerName);
        BetScope scope = scopeResolver != null ? scopeResolver.resolve(marketName) : BetScope.FULL_MATCH;
        StatType statType = statTypeResolver != null ? statTypeResolver.resolve(marketName, sportType) : StatType.MATCH;

        FanDuelMarketContext ctx = FanDuelMarketContext.builder()
                .event(event)
                .market(market)
                .marketName(marketName)
                .runnerName(runnerName)
                .selectionId(selection.getSelectionId())
                .sportType(sportType)
                .sportName(sportName)
                .leagueName(leagueName)
                .team1(team1)
                .team2(team2)
                .decimalOdds(decimalOdds)
                .line(line)
                .scope(scope)
                .statType(statType)
                .build();

        BetType betType = matchHandlers(ctx);

        if (betType == null && betTypeResolver != null) {
            String mUpper = marketName != null ? marketName.toUpperCase() : "";
            betType = betTypeResolver.resolve("fanduel", sportType, mUpper, runnerName.toUpperCase(), line != null ? line : 0.0);
        }

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            logUnmapped(event, sportName, marketName, runnerName);
            return;
        }

        OddItem item = new OddItem();
        String safeRunner = runnerName.replaceAll("[^a-zA-Z0-9_+.-]", "_");
        String safeLine = line != null ? ("_" + line) : "";
        String factorId = market.getMarketId() + "_" + (selection.getSelectionId() != null ? selection.getSelectionId() : (safeRunner + safeLine));

        item.setFactorId(factorId);
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(decimalOdds);
        item.setBetType(betType);

        oddsList.add(item);
    }

    private void processRunnerOutcome(FanDuelEventGroupResponse.FanDuelEvent event,
                                      String marketName,
                                      FanDuelEventGroupResponse.FanDuelMarket market,
                                      FanDuelEventGroupResponse.FanDuelRunner runner,
                                      SportType sportType,
                                      String sportName,
                                      String leagueName,
                                      String team1,
                                      String team2,
                                      List<OddItem> oddsList) {
        if (runner == null || "SUSPENDED".equalsIgnoreCase(runner.getRunnerStatus())) return;

        Double decimalOdds = null;
        if (runner.getWinRunnerOdds() != null) {
            if (runner.getWinRunnerOdds().getTrueOdds() != null &&
                    runner.getWinRunnerOdds().getTrueOdds().getDecimalOdds() != null) {
                decimalOdds = runner.getWinRunnerOdds().getTrueOdds().getDecimalOdds().getDecimalOdds();
            }
            if (decimalOdds == null && runner.getWinRunnerOdds().getAmericanDisplayOdds() != null) {
                Integer am = runner.getWinRunnerOdds().getAmericanDisplayOdds().getAmericanOddsInt();
                if (am == null) am = runner.getWinRunnerOdds().getAmericanDisplayOdds().getAmericanOdds();
                if (am != null && am != 0) {
                    decimalOdds = americanToDecimal(am);
                }
            }
        }
        if (decimalOdds == null || decimalOdds <= 1.0) return;

        String runnerName = runner.getRunnerName();
        if (runnerName == null) runnerName = "Outcome " + runner.getSelectionId();

        Double line = resolveLine(runner.getHandicap(), market, runnerName);
        BetScope scope = scopeResolver != null ? scopeResolver.resolve(marketName) : BetScope.FULL_MATCH;
        StatType statType = statTypeResolver != null ? statTypeResolver.resolve(marketName, sportType) : StatType.MATCH;

        FanDuelMarketContext ctx = FanDuelMarketContext.builder()
                .event(event)
                .market(market)
                .marketName(marketName)
                .runnerName(runnerName)
                .selectionId(runner.getSelectionId() != null ? String.valueOf(runner.getSelectionId()) : null)
                .sportType(sportType)
                .sportName(sportName)
                .leagueName(leagueName)
                .team1(team1)
                .team2(team2)
                .decimalOdds(decimalOdds)
                .line(line)
                .scope(scope)
                .statType(statType)
                .build();

        BetType betType = matchHandlers(ctx);

        if (betType == null && betTypeResolver != null) {
            String mUpper = marketName != null ? marketName.toUpperCase() : "";
            betType = betTypeResolver.resolve("fanduel", sportType, mUpper, runnerName.toUpperCase(), line != null ? line : 0.0);
        }

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            logUnmapped(event, sportName, marketName, runnerName);
            return;
        }

        OddItem item = new OddItem();
        String safeRunner = runnerName.replaceAll("[^a-zA-Z0-9_+.-]", "_");
        String safeLine = line != null ? ("_" + line) : "";
        String factorId = market.getMarketId() + "_" + (runner.getSelectionId() != null ? runner.getSelectionId() : (safeRunner + safeLine));

        item.setFactorId(factorId);
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(decimalOdds);
        item.setBetType(betType);

        oddsList.add(item);
    }

    private BetType matchHandlers(FanDuelMarketContext ctx) {
        if (marketHandlers == null) return null;
        for (FanDuelMarketHandler handler : marketHandlers) {
            if (handler.supports(ctx)) {
                BetType mapped = handler.map(ctx);
                if (mapped != null) {
                    return mapped;
                }
            }
        }
        return null;
    }

    public static Double resolveLine(Double outcomeHandicap, FanDuelEventGroupResponse.FanDuelMarket market, String runnerName) {
        if (outcomeHandicap != null && outcomeHandicap != 0.0) {
            return outcomeHandicap;
        }
        if (market != null) {
            if (market.getTotalPoints() != null && market.getTotalPoints() != 0.0) {
                return market.getTotalPoints();
            }
            if (market.getHandicap() != null && market.getHandicap() != 0.0) {
                return market.getHandicap();
            }
        }
        Double fromRunner = parseLineFromRunner(runnerName);
        if (fromRunner != null) {
            return fromRunner;
        }
        return outcomeHandicap != null ? outcomeHandicap : 0.0;
    }

    public static Double parseLineFromRunner(String runnerName) {
        if (runnerName == null || runnerName.isBlank()) return null;
        String[] parts = runnerName.trim().split("\\s+");
        if (parts.length > 0) {
            String last = parts[parts.length - 1].replace("+", "").trim();
            try {
                return Double.parseDouble(last);
            } catch (Exception ignored) {}
        }
        Matcher matcher = LINE_PATTERN.matcher(runnerName);
        Double lastMatch = null;
        while (matcher.find()) {
            try {
                lastMatch = Double.parseDouble(matcher.group(1));
            } catch (Exception ignored) {}
        }
        return lastMatch;
    }

    public static double americanToDecimal(int am) {
        if (am == 0) return 0.0;
        if (am > 0) {
            return Math.round(((am / 100.0) + 1.0) * 1000.0) / 1000.0;
        } else {
            return Math.round(((100.0 / Math.abs(am)) + 1.0) * 1000.0) / 1000.0;
        }
    }

    public static double americanToDecimal(String americanStr) {
        if (americanStr == null || americanStr.isBlank()) return 0.0;
        try {
            int am = Integer.parseInt(americanStr.replace("+", "").trim());
            return americanToDecimal(am);
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    private void logUnmapped(FanDuelEventGroupResponse.FanDuelEvent event, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED FANDUEL MARKET: Event={}, Sport={}, Market={}, Runner={}",
                event != null ? event.getEventId() : "null", sportName, marketName, runnerName);
        if (unmappedBetService != null && event != null) {
            unmappedBetService.saveAndNotify("fanduel", sportName, runnerName, marketName, String.valueOf(event.getEventId()));
        }
    }
}
