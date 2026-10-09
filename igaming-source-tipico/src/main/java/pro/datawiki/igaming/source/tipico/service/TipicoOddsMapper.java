package pro.datawiki.igaming.source.tipico.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.tipico.dto.TipicoEvent;
import pro.datawiki.igaming.source.tipico.dto.TipicoMarket;
import pro.datawiki.igaming.source.tipico.dto.TipicoOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class TipicoOddsMapper {

    private final SportNormalizationService sportNormalizationService;
    private static final Pattern TOTAL_PARAM_PATTERN = Pattern.compile("[-+]?\\d*\\.?\\d+");

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchCache cached, TipicoEvent event) {
        if (cached == null || event == null) {
            return null;
        }

        List<TipicoMarket> markets = event.getMarkets();
        if (markets == null || markets.isEmpty()) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("tipico");
        request.setRegions(List.of(BookmakerRegion.GLOBAL, BookmakerRegion.EU));
        request.setExternalEventId(String.valueOf(event.getResolvedId()));
        request.setSportName(cached.getSportName() != null ? cached.getSportName() : event.getResolvedSport());

        SportType sportType = sportNormalizationService != null ? sportNormalizationService.normalize(request.getSportName()) : SportType.FOOTBALL;
        request.setSportType(sportType);
        request.setLeagueName(cached.getLeagueName() != null ? cached.getLeagueName() : event.getResolvedLeague());
        request.setTeam1(cached.getTeam1());
        request.setTeam2(cached.getTeam2());
        request.setIsLive(event.getResolvedIsLive() != null ? event.getResolvedIsLive() : Boolean.TRUE.equals(cached.getIsLive()));
        request.setStartTime(cached.getStartTime());
        request.setEventUrl("https://sports.tipico.de/de/events/" + event.getResolvedId());

        List<OddItem> oddItems = new ArrayList<>();

        for (TipicoMarket market : markets) {
            List<TipicoOutcome> outcomes = market.getResolvedOutcomes();
            if (outcomes == null || outcomes.isEmpty()) {
                continue;
            }

            String marketName = market.getResolvedName().toLowerCase();
            String marketType = market.getResolvedType().toUpperCase();

            if (is1X2Market(marketName, marketType)) {
                map1X2(market, outcomes, oddItems, cached);
            } else if (isTotalMarket(marketName, marketType)) {
                mapTotals(market, outcomes, oddItems);
            } else if (isHandicapMarket(marketName, marketType)) {
                mapHandicap(market, outcomes, oddItems, cached);
            } else if (isBttsMarket(marketName, marketType)) {
                mapBtts(market, outcomes, oddItems);
            } else if (isDoubleChanceMarket(marketName, marketType)) {
                mapDoubleChance(market, outcomes, oddItems);
            }
        }

        if (oddItems.isEmpty()) {
            return null;
        }

        request.setOdds(oddItems);
        return request;
    }

    private boolean is1X2Market(String name, String type) {
        return type.contains("1X2") || type.contains("MATCH_RESULT") || type.contains("WINNER")
                || name.contains("1x2") || name.contains("dreiweg") || name.contains("match result")
                || name.contains("sieger") || name.contains("gewinner") || name.equals("winner");
    }

    private void map1X2(TipicoMarket market, List<TipicoOutcome> outcomes, List<OddItem> oddItems, MatchCache cached) {
        boolean hasDraw = outcomes.stream().anyMatch(o -> {
            String n = o.getResolvedName().toLowerCase();
            return n.equals("x") || n.equals("unentschieden") || n.equals("draw") || n.equals("tie");
        });

        for (TipicoOutcome outcome : outcomes) {
            Double price = outcome.getResolvedPrice();
            if (price == null || price <= 1.0) continue;

            String name = outcome.getResolvedName().trim();
            String code = outcome.getCode() != null ? outcome.getCode().trim().toUpperCase() : "";

            if (code.equals("1") || name.equalsIgnoreCase("1") || (cached.getTeam1() != null && name.equalsIgnoreCase(cached.getTeam1())) || name.equalsIgnoreCase("Heim")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null)));
            } else if (code.equals("X") || name.equalsIgnoreCase("X") || name.equalsIgnoreCase("Unentschieden") || name.equalsIgnoreCase("Draw")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null)));
            } else if (code.equals("2") || name.equalsIgnoreCase("2") || (cached.getTeam2() != null && name.equalsIgnoreCase(cached.getTeam2())) || name.equalsIgnoreCase("Auswärts")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null)));
            }
        }
    }

    private boolean isTotalMarket(String name, String type) {
        return type.contains("TOTAL") || type.contains("OVER_UNDER") || name.contains("über/unter")
                || name.contains("over/under") || name.contains("tore") || name.contains("total");
    }

    private void mapTotals(TipicoMarket market, List<TipicoOutcome> outcomes, List<OddItem> oddItems) {
        Double marketTotal = parseTotalValue(market.getResolvedName());

        for (TipicoOutcome outcome : outcomes) {
            Double price = outcome.getResolvedPrice();
            if (price == null || price <= 1.0) continue;

            Double total = outcome.getResolvedHandicap();
            if (total == null) {
                total = parseTotalValue(outcome.getResolvedName());
            }
            if (total == null) {
                total = marketTotal;
            }
            if (total == null) continue;

            String name = outcome.getResolvedName().toLowerCase();
            if (name.contains("über") || name.contains("over") || name.startsWith(">") || name.startsWith("o ")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, total, false, null)));
            } else if (name.contains("unter") || name.contains("under") || name.startsWith("<") || name.startsWith("u ")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, total, false, null)));
            }
        }
    }

    private boolean isHandicapMarket(String name, String type) {
        return type.contains("HANDICAP") || type.contains("SPREAD")
                || name.contains("handicap") || name.contains("spread");
    }

    private void mapHandicap(TipicoMarket market, List<TipicoOutcome> outcomes, List<OddItem> oddItems, MatchCache cached) {
        for (TipicoOutcome outcome : outcomes) {
            Double price = outcome.getResolvedPrice();
            if (price == null || price <= 1.0) continue;

            Double hcap = outcome.getResolvedHandicap();
            if (hcap == null) {
                hcap = parseTotalValue(outcome.getResolvedName());
            }
            if (hcap == null) continue;

            String name = outcome.getResolvedName().trim();
            if (name.equalsIgnoreCase("1") || (cached.getTeam1() != null && name.toLowerCase().contains(cached.getTeam1().toLowerCase())) || name.toLowerCase().startsWith("heim")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, hcap, false, null)));
            } else if (name.equalsIgnoreCase("2") || (cached.getTeam2() != null && name.toLowerCase().contains(cached.getTeam2().toLowerCase())) || name.toLowerCase().startsWith("auswärts")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, hcap, false, null)));
            }
        }
    }

    private boolean isBttsMarket(String name, String type) {
        return type.contains("BTTS") || name.contains("beide teams treffen") || name.contains("both teams to score") || name.contains("btts");
    }

    private void mapBtts(TipicoMarket market, List<TipicoOutcome> outcomes, List<OddItem> oddItems) {
        for (TipicoOutcome outcome : outcomes) {
            Double price = outcome.getResolvedPrice();
            if (price == null || price <= 1.0) continue;

            String name = outcome.getResolvedName().toLowerCase().trim();
            if (name.equals("ja") || name.equals("yes") || name.equals("y")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, null)));
            } else if (name.equals("nein") || name.equals("no") || name.equals("n")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, null)));
            }
        }
    }

    private boolean isDoubleChanceMarket(String name, String type) {
        return type.contains("DOUBLE_CHANCE") || name.contains("doppelte chance") || name.contains("double chance");
    }

    private void mapDoubleChance(TipicoMarket market, List<TipicoOutcome> outcomes, List<OddItem> oddItems) {
        for (TipicoOutcome outcome : outcomes) {
            Double price = outcome.getResolvedPrice();
            if (price == null || price <= 1.0) continue;

            String name = outcome.getResolvedName().trim().toUpperCase();
            if (name.equals("1X") || name.contains("HEIM ODER UNENTSCHIEDEN") || name.contains("1 ODER X")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_1X, null)));
            } else if (name.equals("12") || name.contains("HEIM ODER AUSWÄRTS") || name.contains("1 ODER 2")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_12, null)));
            } else if (name.equals("X2") || name.contains("UNENTSCHIEDEN ODER AUSWÄRTS") || name.contains("X ODER 2")) {
                oddItems.add(createOddItem(outcome, market.getResolvedName(),
                        new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DC_X2, null)));
            }
        }
    }

    private OddItem createOddItem(TipicoOutcome outcome, String groupName, pro.datawiki.igaming.dto.BetType betType) {
        OddItem item = new OddItem();
        item.setFactorId(outcome.getId() != null ? String.valueOf(outcome.getId()) : null);
        item.setGroupName(groupName);
        item.setName(outcome.getResolvedName());
        item.setValue(outcome.getResolvedPrice());
        item.setBetType(betType);
        return item;
    }

    private Double parseTotalValue(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher matcher = TOTAL_PARAM_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
