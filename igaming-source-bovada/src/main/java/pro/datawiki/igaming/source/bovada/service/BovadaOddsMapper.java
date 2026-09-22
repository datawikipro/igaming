package pro.datawiki.igaming.source.bovada.service;

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
import pro.datawiki.igaming.source.bovada.dto.*;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class BovadaOddsMapper extends AbstractBetTypeMapper {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "bovada".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(BovadaEventDto event,
                                                    List<BovadaPathDto> path,
                                                    SportType sportType,
                                                    String sportName,
                                                    String leagueName) {
        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("bovada");
        request.setRegions(List.of(BookmakerRegion.US, BookmakerRegion.GLOBAL));

        String externalEventId = event.getId();
        request.setExternalEventId(externalEventId);
        request.setSportName(sportName);
        request.setSportType(sportType);
        request.setLeagueName(leagueName);

        // Resolve Teams
        String team1 = null; // Home
        String team2 = null; // Away

        if (event.getCompetitors() != null && !event.getCompetitors().isEmpty()) {
            for (BovadaCompetitorDto comp : event.getCompetitors()) {
                if (Boolean.TRUE.equals(comp.getHome())) {
                    team1 = comp.getName();
                } else if (Boolean.FALSE.equals(comp.getHome())) {
                    team2 = comp.getName();
                }
            }
            // If home flags were absent but we have 2 competitors
            if ((team1 == null || team2 == null) && event.getCompetitors().size() >= 2) {
                if (Boolean.TRUE.equals(event.getAwayTeamFirst())) {
                    team2 = event.getCompetitors().get(0).getName();
                    team1 = event.getCompetitors().get(1).getName();
                } else {
                    team1 = event.getCompetitors().get(0).getName();
                    team2 = event.getCompetitors().get(1).getName();
                }
            }
        }

        // Fallback to event description (e.g. "Atlanta Falcons @ Pittsburgh Steelers")
        if ((team1 == null || team2 == null) && event.getDescription() != null) {
            String desc = event.getDescription();
            if (desc.contains(" @ ")) {
                String[] parts = desc.split(" @ ", 2);
                team2 = parts[0].trim(); // Away
                team1 = parts[1].trim(); // Home
            } else if (desc.contains(" vs ")) {
                String[] parts = desc.split(" vs ", 2);
                team1 = parts[0].trim(); // Home
                team2 = parts[1].trim(); // Away
            } else if (desc.contains(" v ")) {
                String[] parts = desc.split(" v ", 2);
                team1 = parts[0].trim(); // Home
                team2 = parts[1].trim(); // Away
            }
        }

        request.setTeam1(team1 != null ? team1 : "Home Team");
        request.setTeam2(team2 != null ? team2 : "Away Team");
        request.setIsLive(Boolean.TRUE.equals(event.getLive()));
        request.setStartTime(event.getStartTime());

        if (event.getLink() != null && !event.getLink().isBlank()) {
            request.setEventUrl("https://www.bovada.lv" + event.getLink());
        } else {
            request.setEventUrl("https://www.bovada.lv");
        }

        List<OddItem> items = new ArrayList<>();

        if (event.getDisplayGroups() != null) {
            for (BovadaDisplayGroupDto dg : event.getDisplayGroups()) {
                if (dg.getMarkets() == null) continue;

                for (BovadaMarketDto market : dg.getMarkets()) {
                    if (!isFullMatchPeriod(market.getPeriod())) {
                        continue;
                    }

                    String marketKey = market.getKey() != null ? market.getKey() : "";
                    String marketDesc = market.getDescription() != null ? market.getDescription() : "";

                    // 1. Moneyline / 1X2 / Match Winner
                    if (isMoneylineMarket(marketKey, marketDesc)) {
                        mapMoneylineOutcomes(items, market, team1, team2);
                    }
                    // 2. Point Spread / Puck Line / Run Line / Handicap
                    else if (isSpreadMarket(marketKey, marketDesc)) {
                        mapSpreadOutcomes(items, market, team1, team2);
                    }
                    // 3. Totals (Over / Under)
                    else if (isTotalMarket(marketKey, marketDesc)) {
                        mapTotalOutcomes(items, market, team1, team2);
                    }
                }
            }
        }

        request.setOdds(items);
        return request;
    }

    private boolean isFullMatchPeriod(BovadaPeriodDto period) {
        if (period == null) return true;
        if (Boolean.TRUE.equals(period.getMain())) return true;
        if ("G".equalsIgnoreCase(period.getAbbreviation())) return true;

        String desc = period.getDescription() != null ? period.getDescription().toLowerCase() : "";
        if (desc.isEmpty() || desc.contains("game") || desc.contains("match") || desc.contains("regulation")) {
            return true;
        }

        // Exclude sub-periods for match-level arbs
        return !desc.contains("half") && !desc.contains("quarter") && !desc.contains("period")
                && !desc.contains("inning") && !desc.contains("set");
    }

    private boolean isMoneylineMarket(String key, String desc) {
        String lowerDesc = desc.toLowerCase();
        if (lowerDesc.contains("half") || lowerDesc.contains("period") || lowerDesc.contains("quarter")
                || lowerDesc.contains("inning") || lowerDesc.contains("set")) {
            return false;
        }
        return "2W-12".equalsIgnoreCase(key) || "3W-1X2".equalsIgnoreCase(key)
                || lowerDesc.equals("moneyline") || lowerDesc.equals("3-way moneyline")
                || lowerDesc.equals("match winner") || lowerDesc.equals("head to head");
    }

    private boolean isSpreadMarket(String key, String desc) {
        String lowerDesc = desc.toLowerCase();
        if (lowerDesc.contains("half") || lowerDesc.contains("period") || lowerDesc.contains("quarter")
                || lowerDesc.contains("inning") || lowerDesc.contains("set")) {
            return false;
        }
        return "2W-HCAP".equalsIgnoreCase(key) || lowerDesc.contains("point spread")
                || lowerDesc.contains("spread") || lowerDesc.contains("puck line")
                || lowerDesc.contains("run line") || lowerDesc.contains("handicap");
    }

    private boolean isTotalMarket(String key, String desc) {
        String lowerDesc = desc.toLowerCase();
        if (lowerDesc.contains("half") || lowerDesc.contains("period") || lowerDesc.contains("quarter")
                || lowerDesc.contains("inning") || lowerDesc.contains("set") || lowerDesc.contains("corner")
                || lowerDesc.contains("booking") || lowerDesc.contains("card")) {
            return false;
        }
        return "2W-OU".equalsIgnoreCase(key) || lowerDesc.contains("total") || lowerDesc.contains("over/under");
    }

    private void mapMoneylineOutcomes(List<OddItem> items, BovadaMarketDto market, String team1, String team2) {
        if (market.getOutcomes() == null) return;

        for (BovadaOutcomeDto outcome : market.getOutcomes()) {
            Double decimal = parseDecimalPrice(outcome.getPrice());
            if (decimal == null || decimal <= 1.0) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String desc = outcome.getDescription() != null ? outcome.getDescription().trim() : "";

            BetType betType = null;
            if ("H".equals(type) || (team1 != null && desc.equalsIgnoreCase(team1))) {
                betType = map1X2Record("1", BetScope.FULL_MATCH, StatType.MATCH);
            } else if ("A".equals(type) || (team2 != null && desc.equalsIgnoreCase(team2))) {
                betType = map1X2Record("2", BetScope.FULL_MATCH, StatType.MATCH);
            } else if ("D".equals(type) || desc.equalsIgnoreCase("Draw") || desc.equalsIgnoreCase("Tie")) {
                betType = map1X2Record("X", BetScope.FULL_MATCH, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, "moneyline", desc.isEmpty() ? type : desc, decimal, betType);
            }
        }
    }

    private void mapSpreadOutcomes(List<OddItem> items, BovadaMarketDto market, String team1, String team2) {
        if (market.getOutcomes() == null) return;

        for (BovadaOutcomeDto outcome : market.getOutcomes()) {
            Double decimal = parseDecimalPrice(outcome.getPrice());
            if (decimal == null || decimal <= 1.0) continue;

            Double hdp = parseHandicap(outcome);
            if (hdp == null) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String desc = outcome.getDescription() != null ? outcome.getDescription().trim() : "";

            BetType betType = null;
            if ("H".equals(type) || (team1 != null && desc.equalsIgnoreCase(team1))) {
                betType = mapHandicapRecord("1", BetScope.FULL_MATCH, StatType.MATCH, false, hdp);
            } else if ("A".equals(type) || (team2 != null && desc.equalsIgnoreCase(team2))) {
                betType = mapHandicapRecord("2", BetScope.FULL_MATCH, StatType.MATCH, false, hdp);
            }

            if (betType != null) {
                String label = desc + " (" + (hdp > 0 ? "+" + hdp : hdp) + ")";
                addOddItem(items, "spread", label, decimal, betType);
            }
        }
    }

    private BetSubject determineTotalSubject(String marketDesc, String team1, String team2) {
        if (marketDesc == null || marketDesc.isBlank()) {
            return BetSubject.MATCH;
        }
        String lower = marketDesc.toLowerCase();

        boolean matchesTeam1 = team1 != null && !team1.isBlank() && lower.contains(team1.toLowerCase());
        boolean matchesTeam2 = team2 != null && !team2.isBlank() && lower.contains(team2.toLowerCase());

        if (matchesTeam1 && !matchesTeam2) {
            return BetSubject.TEAM1;
        }
        if (matchesTeam2 && !matchesTeam1) {
            return BetSubject.TEAM2;
        }

        if (lower.contains("home team") || lower.contains("home total") || lower.startsWith("home ") || lower.endsWith(" home")) {
            return BetSubject.TEAM1;
        }
        if (lower.contains("away team") || lower.contains("away total") || lower.startsWith("away ") || lower.endsWith(" away")) {
            return BetSubject.TEAM2;
        }

        if (lower.contains("team total") || lower.contains("team points") || lower.contains("team goals")) {
            return null; // Ambiguous team total - do not confuse with match total
        }

        return BetSubject.MATCH;
    }

    private void mapTotalOutcomes(List<OddItem> items, BovadaMarketDto market, String team1, String team2) {
        if (market.getOutcomes() == null) return;

        BetSubject subject = determineTotalSubject(market.getDescription(), team1, team2);
        if (subject == null) {
            return;
        }

        for (BovadaOutcomeDto outcome : market.getOutcomes()) {
            Double decimal = parseDecimalPrice(outcome.getPrice());
            if (decimal == null || decimal <= 1.0) continue;

            Double points = parseHandicap(outcome);
            if (points == null) continue;

            String type = outcome.getType() != null ? outcome.getType().toUpperCase() : "";
            String desc = outcome.getDescription() != null ? outcome.getDescription().trim() : "";

            BetType betType = null;
            if ("O".equals(type) || desc.toLowerCase().startsWith("over")) {
                betType = mapTotalRecord("OVER", BetScope.FULL_MATCH, subject, StatType.MATCH, false, points);
            } else if ("U".equals(type) || desc.toLowerCase().startsWith("under")) {
                betType = mapTotalRecord("UNDER", BetScope.FULL_MATCH, subject, StatType.MATCH, false, points);
            }

            if (betType != null) {
                String label = desc + " (" + points + ")";
                String group = (subject == BetSubject.MATCH) ? "total" : ("total_" + subject.name().toLowerCase());
                addOddItem(items, group, label, decimal, betType);
            }
        }
    }

    private Double parseDecimalPrice(BovadaPriceDto price) {
        if (price == null) return null;

        // Primary: decimal
        if (price.getDecimal() != null && !price.getDecimal().isBlank()) {
            try {
                return Double.parseDouble(price.getDecimal().trim());
            } catch (NumberFormatException ignored) {}
        }

        // Secondary fallback: american (e.g. "+240", "-290")
        if (price.getAmerican() != null && !price.getAmerican().isBlank()) {
            try {
                String amStr = price.getAmerican().trim().replace("+", "");
                double american = Double.parseDouble(amStr);
                if (american > 0) {
                    return Math.round(((american / 100.0) + 1.0) * 1000.0) / 1000.0;
                } else if (american < 0) {
                    return Math.round(((100.0 / Math.abs(american)) + 1.0) * 1000.0) / 1000.0;
                }
            } catch (NumberFormatException ignored) {}
        }

        return null;
    }

    private Double parseHandicap(BovadaOutcomeDto outcome) {
        if (outcome.getPrice() != null && outcome.getPrice().getHandicap() != null) {
            try {
                return Double.parseDouble(outcome.getPrice().getHandicap().trim());
            } catch (NumberFormatException ignored) {}
        }

        // Fallback: regex search from outcome description
        if (outcome.getDescription() != null) {
            Matcher m = NUMERIC_PATTERN.matcher(outcome.getDescription());
            if (m.find()) {
                try {
                    return Double.parseDouble(m.group(1));
                } catch (NumberFormatException ignored) {}
            }
        }

        return null;
    }

    private void addOddItem(List<OddItem> items, String groupName, String rawOutcomeName, double value, BetType betType) {
        // Golden Rule #9: NO YIELD CAP! No silent drop of odds!
        if (value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        item.setFactorId(groupName + "_" + rawOutcomeName.replaceAll("[^a-zA-Z0-9_+.-]", "_"));
        item.setGroupName(groupName);
        item.setName(rawOutcomeName);
        item.setValue(value);
        item.setBetType(betType);
        items.add(item);
    }
}
