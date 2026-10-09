package pro.datawiki.igaming.source.novig.service;

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
import pro.datawiki.igaming.source.novig.dto.NovigEventDto;
import pro.datawiki.igaming.source.novig.dto.NovigMarketDto;
import pro.datawiki.igaming.source.novig.dto.NovigOutcomeDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class NovigOddsMapper extends AbstractBetTypeMapper {

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "novig".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(NovigEventDto event) {
        if (event == null || event.getId() == null) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("novig");
        request.setRegions(List.of(BookmakerRegion.US, BookmakerRegion.GLOBAL));
        request.setExternalEventId(event.getId());

        SportType sportType = resolveSportType(event.getLeague());
        request.setSportType(sportType);
        request.setSportName(sportType.name());
        request.setLeagueName(event.getLeague() != null ? event.getLeague() : "US Sports");

        // Parse teams from "Away @ Home" or "Team1 vs Team2"
        String team1 = "Home Team";
        String team2 = "Away Team";
        if (event.getDescription() != null) {
            String desc = event.getDescription();
            if (desc.contains(" @ ")) {
                String[] parts = desc.split(" @ ", 2);
                team2 = parts[0].trim(); // Away @ Home
                team1 = parts[1].trim();
            } else if (desc.contains(" vs ")) {
                String[] parts = desc.split(" vs ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else if (desc.contains(" - ")) {
                String[] parts = desc.split(" - ", 2);
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive("OPEN_INGAME".equalsIgnoreCase(event.getStatus()));
        if (event.getScheduledStart() != null) {
            try {
                request.setStartTime(Instant.parse(event.getScheduledStart()).toEpochMilli());
            } catch (Exception e) {
                request.setStartTime(Instant.now().toEpochMilli());
            }
        } else {
            request.setStartTime(Instant.now().toEpochMilli());
        }

        List<OddItem> items = new ArrayList<>();

        if (event.getMarkets() != null) {
            for (NovigMarketDto market : event.getMarkets()) {
                if (market == null || market.getOutcomes() == null || market.getOutcomes().isEmpty()) {
                    continue;
                }

                String mType = market.getType() != null ? market.getType().toUpperCase() : "";

                if (mType.contains("MONEYLINE") || mType.contains("ML") || mType.equals("WINNER")) {
                    mapMoneyline(items, market, team1, team2);
                } else if (mType.contains("SPREAD") || mType.contains("RUN_LINE") || mType.contains("PUCK_LINE")) {
                    mapSpread(items, market, team1, team2);
                } else if (mType.contains("TOTAL") && !mType.contains("TEAM_TOTAL")) {
                    mapTotal(items, market);
                }
            }
        }

        request.setOdds(items);
        return request;
    }

    private void mapMoneyline(List<OddItem> items, NovigMarketDto market, String team1, String team2) {
        for (NovigOutcomeDto outcome : market.getOutcomes()) {
            Double decimalOdds = outcome.getDecimalOdds();
            if (decimalOdds == null || decimalOdds <= 1.0) continue;

            String desc = outcome.getDescription() != null ? outcome.getDescription().trim() : "";
            BetType betType = null;

            if (isMatch(desc, team1) || desc.equalsIgnoreCase("Home") || desc.equals("1")) {
                betType = map1X2Record("1", BetScope.FULL_MATCH, StatType.MATCH);
            } else if (isMatch(desc, team2) || desc.equalsIgnoreCase("Away") || desc.equals("2")) {
                betType = map1X2Record("2", BetScope.FULL_MATCH, StatType.MATCH);
            } else if (desc.equalsIgnoreCase("Draw") || desc.equalsIgnoreCase("Tie") || desc.equals("X")) {
                betType = map1X2Record("X", BetScope.FULL_MATCH, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "moneyline", outcome.getId(), desc, decimalOdds, betType);
            }
        }
    }

    private void mapSpread(List<OddItem> items, NovigMarketDto market, String team1, String team2) {
        for (NovigOutcomeDto outcome : market.getOutcomes()) {
            Double decimalOdds = outcome.getDecimalOdds();
            if (decimalOdds == null || decimalOdds <= 1.0) continue;

            Double line = market.getStrike();
            String desc = outcome.getDescription() != null ? outcome.getDescription().trim() : "";
            BetType betType = null;

            if (isMatch(desc, team1) || desc.equalsIgnoreCase("Home") || desc.startsWith("1")) {
                betType = mapHandicapRecord("1", BetScope.FULL_MATCH, StatType.MATCH, false, line);
            } else if (isMatch(desc, team2) || desc.equalsIgnoreCase("Away") || desc.startsWith("2")) {
                betType = mapHandicapRecord("2", BetScope.FULL_MATCH, StatType.MATCH, false, line);
            }

            if (betType != null) {
                String fullLabel = desc + " (" + (line != null && line > 0 ? "+" + line : line) + ")";
                addOddItem(items, "spread", outcome.getId(), fullLabel, decimalOdds, betType);
            }
        }
    }

    private void mapTotal(List<OddItem> items, NovigMarketDto market) {
        for (NovigOutcomeDto outcome : market.getOutcomes()) {
            Double decimalOdds = outcome.getDecimalOdds();
            if (decimalOdds == null || decimalOdds <= 1.0) continue;

            Double line = market.getStrike();
            if (line == null) continue;

            String desc = outcome.getDescription() != null ? outcome.getDescription().trim().toLowerCase() : "";
            String type = outcome.getType() != null ? outcome.getType().toLowerCase() : "";
            BetType betType = null;

            if (desc.startsWith("over") || type.startsWith("over") || desc.startsWith("o ")) {
                betType = mapTotalRecord("OVER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, line);
            } else if (desc.startsWith("under") || type.startsWith("under") || desc.startsWith("u ")) {
                betType = mapTotalRecord("UNDER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, line);
            }

            if (betType != null) {
                String fullLabel = outcome.getDescription() + " (" + line + ")";
                addOddItem(items, "total", outcome.getId(), fullLabel, decimalOdds, betType);
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

    public SportType resolveSportType(String league) {
        if (league == null) return SportType.AMERICAN_FOOTBALL;
        String l = league.toUpperCase();
        if (l.contains("NFL") || l.contains("NCAAF") || l.contains("CFL")) return SportType.AMERICAN_FOOTBALL;
        if (l.contains("NBA") || l.contains("WNBA") || l.contains("NCAAB") || l.contains("BASKETBALL")) return SportType.BASKETBALL;
        if (l.contains("MLB") || l.contains("BASEBALL")) return SportType.BASEBALL;
        if (l.contains("NHL") || l.contains("HOCKEY")) return SportType.HOCKEY;
        if (l.contains("EPL") || l.contains("MLS") || l.contains("SOCCER") || l.contains("UCL")) return SportType.FOOTBALL;
        if (l.contains("TENNIS") || l.contains("ATP") || l.contains("WTA")) return SportType.TENNIS;
        if (l.contains("UFC") || l.contains("MMA")) return SportType.MMA;
        return SportType.AMERICAN_FOOTBALL;
    }
}
