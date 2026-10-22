package pro.datawiki.igaming.source.matchbook.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.BookmakerRegion;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.core.mapper.rule.LineParamParser;
import pro.datawiki.igaming.source.matchbook.dto.MatchbookEvent;
import pro.datawiki.igaming.source.matchbook.dto.MatchbookMarket;
import pro.datawiki.igaming.source.matchbook.dto.MatchbookRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchbookOddsMapper extends AbstractBetTypeMapper {

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "matchbook".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(MatchbookEvent event) {
        if (event == null || event.getId() == null) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("matchbook");
        request.setRegions(List.of(BookmakerRegion.GB, BookmakerRegion.EU, BookmakerRegion.GLOBAL));
        request.setExternalEventId(String.valueOf(event.getId()));

        SportType sportType = resolveSportType(event.getSportId());
        request.setSportType(sportType);
        request.setSportName(sportType.name());
        request.setLeagueName("Matchbook Exchange");

        // Parse teams from "Team1 vs Team2" or "Team1 @ Team2"
        String team1 = "Home Team";
        String team2 = "Away Team";
        if (event.getName() != null) {
            String name = event.getName();
            if (name.contains(" vs ")) {
                String[] parts = name.split(" vs ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (name.contains(" @ ")) {
                String[] parts = name.split(" @ ", 2);
                team2 = parts[0].trim();
                team1 = parts[1].trim();
            } else if (name.contains(" - ")) {
                String[] parts = name.split(" - ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive(Boolean.TRUE.equals(event.getInRunningFlag()));
        if (event.getStart() != null) {
            try {
                request.setStartTime(Instant.parse(event.getStart()).toEpochMilli());
            } catch (Exception e) {
                request.setStartTime(Instant.now().toEpochMilli());
            }
        } else {
            request.setStartTime(Instant.now().toEpochMilli());
        }

        List<OddItem> items = new ArrayList<>();

        if (event.getMarkets() != null) {
            for (MatchbookMarket market : event.getMarkets()) {
                if (market == null || market.getRunners() == null || market.getRunners().isEmpty()) {
                    continue;
                }

                String mType = market.getMarketType() != null ? market.getMarketType().toLowerCase() : "";
                String mName = market.getName() != null ? market.getName().toLowerCase() : "";

                if (mType.contains("one_x_two") || mName.contains("match odds") || mName.contains("full time result")) {
                    map1X2(items, market, team1, team2);
                } else if (mType.contains("money_line") || mName.contains("money line") || mName.contains("match winner")) {
                    mapMoneyline(items, market, team1, team2);
                } else if (mType.contains("total") || mType.contains("over_under") || mName.contains("over/under") || mName.contains("total")) {
                    mapTotals(items, market);
                } else if (mType.contains("handicap") || mName.contains("handicap") || mName.contains("spread")) {
                    mapHandicap(items, market, team1, team2);
                }
            }
        }

        request.setOdds(items);
        return request;
    }

    private void map1X2(List<OddItem> items, MatchbookMarket market, String team1, String team2) {
        for (MatchbookRunner runner : market.getRunners()) {
            Double backOdds = runner.getBestBackOdds();
            if (backOdds == null || backOdds <= 1.0) continue;

            String rName = runner.getName() != null ? runner.getName().trim() : "";
            BetType betType = null;

            if (rName.equalsIgnoreCase("draw") || rName.equalsIgnoreCase("the draw") || rName.equalsIgnoreCase("tie")) {
                betType = map1X2Record("X", BetScope.FULL_MATCH, StatType.MATCH);
            } else if (isMatch(rName, team1) || rName.equalsIgnoreCase("1")) {
                betType = map1X2Record("1", BetScope.FULL_MATCH, StatType.MATCH);
            } else if (isMatch(rName, team2) || rName.equalsIgnoreCase("2")) {
                betType = map1X2Record("2", BetScope.FULL_MATCH, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "moneyline", String.valueOf(runner.getId()), rName, backOdds, betType);
            }
        }
    }

    private void mapMoneyline(List<OddItem> items, MatchbookMarket market, String team1, String team2) {
        for (MatchbookRunner runner : market.getRunners()) {
            Double backOdds = runner.getBestBackOdds();
            if (backOdds == null || backOdds <= 1.0) continue;

            String rName = runner.getName() != null ? runner.getName().trim() : "";
            BetType betType = null;

            if (isMatch(rName, team1) || rName.equalsIgnoreCase("1")) {
                betType = map1X2Record("1", BetScope.FULL_MATCH, StatType.MATCH);
            } else if (isMatch(rName, team2) || rName.equalsIgnoreCase("2")) {
                betType = map1X2Record("2", BetScope.FULL_MATCH, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "moneyline", String.valueOf(runner.getId()), rName, backOdds, betType);
            }
        }
    }

    private void mapTotals(List<OddItem> items, MatchbookMarket market) {
        Double line = LineParamParser.parseTotalParam(market.getName());

        for (MatchbookRunner runner : market.getRunners()) {
            Double backOdds = runner.getBestBackOdds();
            if (backOdds == null || backOdds <= 1.0) continue;

            String rName = runner.getName() != null ? runner.getName().trim().toLowerCase() : "";
            Double runnerLine = LineParamParser.parseTotalParam(rName);
            Double finalLine = runnerLine != null ? runnerLine : line;
            if (finalLine == null) continue;

            BetType betType = null;
            if (rName.startsWith("over") || rName.startsWith("o ")) {
                betType = mapTotalRecord("OVER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, finalLine);
            } else if (rName.startsWith("under") || rName.startsWith("u ")) {
                betType = mapTotalRecord("UNDER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, finalLine);
            }

            if (betType != null) {
                String fullLabel = runner.getName() + " (" + finalLine + ")";
                addOddItem(items, "total", String.valueOf(runner.getId()), fullLabel, backOdds, betType);
            }
        }
    }

    private void mapHandicap(List<OddItem> items, MatchbookMarket market, String team1, String team2) {
        Double line = LineParamParser.parseHandicapParam(market.getName());

        for (MatchbookRunner runner : market.getRunners()) {
            Double backOdds = runner.getBestBackOdds();
            if (backOdds == null || backOdds <= 1.0) continue;

            String rName = runner.getName() != null ? runner.getName().trim() : "";
            Double runnerLine = LineParamParser.parseHandicapParam(rName);
            Double finalLine = runnerLine != null ? runnerLine : line;
            if (finalLine == null) continue;

            BetType betType = null;
            if (isMatch(rName, team1) || rName.startsWith("1")) {
                betType = mapHandicapRecord("1", BetScope.FULL_MATCH, StatType.MATCH, false, finalLine);
            } else if (isMatch(rName, team2) || rName.startsWith("2")) {
                betType = mapHandicapRecord("2", BetScope.FULL_MATCH, StatType.MATCH, false, finalLine);
            }

            if (betType != null) {
                String fullLabel = rName + " (" + (finalLine > 0 ? "+" + finalLine : finalLine) + ")";
                addOddItem(items, "handicap", String.valueOf(runner.getId()), fullLabel, backOdds, betType);
            }
        }
    }

    private void addOddItem(List<OddItem> items, String groupName, String outcomeId, String rawName, double value, BetType betType) {
        // Golden Rule #9: NO YIELD CAP! No silent drop of odds!
        if (value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        item.setFactorId(outcomeId != null ? outcomeId : (groupName + "_" + rawName.replaceAll("[^a-zA-Z0-9_+.-]", "_")));
        item.setGroupName(groupName);
        item.setName(rawName);
        item.setValue(Math.round(value * 1000.0) / 1000.0);
        item.setBetType(betType);
        items.add(item);
    }

    private boolean isMatch(String name, String team) {
        if (name == null || team == null) return false;
        String n = name.trim().toLowerCase();
        String t = team.trim().toLowerCase();
        return n.contains(t) || t.contains(n);
    }

    private Double extractParam(String text) {
        return LineParamParser.parseGeneralParam(text);
    }

    public SportType resolveSportType(Integer sportId) {
        if (sportId == null) return SportType.FOOTBALL;
        return switch (sportId) {
            case 1 -> SportType.AMERICAN_FOOTBALL;
            case 2, 24 -> SportType.FOOTBALL;
            case 3 -> SportType.BASEBALL;
            case 4 -> SportType.BASKETBALL;
            case 5 -> SportType.BOXING;
            case 6 -> SportType.CRICKET;
            case 9 -> SportType.TENNIS;
            case 15 -> SportType.HOCKEY;
            default -> SportType.FOOTBALL;
        };
    }
}
