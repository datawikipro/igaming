package pro.datawiki.igaming.source.bookmaker.service;

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
import pro.datawiki.igaming.source.bookmaker.dto.BookmakerMatchDto;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookmakerOddsMapper extends AbstractBetTypeMapper {

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "bookmaker".equalsIgnoreCase(bookmaker) || "bookmaker.eu".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    public OddsUpdateRequest mapToOddsUpdateRequest(BookmakerMatchDto match, SportType sportType) {
        if (match == null || match.getId() == null) {
            return null;
        }

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("bookmaker");
        request.setRegions(List.of(BookmakerRegion.US, BookmakerRegion.GLOBAL));
        request.setExternalEventId(match.getId());
        request.setSportName(match.getSport());
        request.setSportType(sportType);
        request.setLeagueName(match.getLeague());
        request.setTeam1(match.getTeam1());
        request.setTeam2(match.getTeam2());
        request.setIsLive(match.isLive());
        request.setEventUrl(match.getEventUrl());

        if (match.getStartTime() != null) {
            request.setStartTime(match.getStartTime());
        }

        List<OddItem> items = new ArrayList<>();

        // 1. Moneyline
        mapMoneyline(items, match);

        // 2. Spread (Run Line / Point Spread / Puck Line)
        mapSpread(items, match);

        // 3. Totals (Over / Under)
        mapTotals(items, match);

        request.setOdds(items);
        return request;
    }

    private void mapMoneyline(List<OddItem> items, BookmakerMatchDto match) {
        Double dec1 = parseAmericanOdds(match.getMl1());
        if (dec1 != null && dec1 > 1.0) {
            BetType bt1 = map1X2Record("1", BetScope.FULL_MATCH, StatType.MATCH);
            addOddItem(items, "moneyline", match.getId() + "_ml_1", match.getTeam1(), dec1, bt1);
        }

        Double dec2 = parseAmericanOdds(match.getMl2());
        if (dec2 != null && dec2 > 1.0) {
            BetType bt2 = map1X2Record("2", BetScope.FULL_MATCH, StatType.MATCH);
            addOddItem(items, "moneyline", match.getId() + "_ml_2", match.getTeam2(), dec2, bt2);
        }

        Double decX = parseAmericanOdds(match.getMlX());
        if (decX != null && decX > 1.0) {
            BetType btX = map1X2Record("X", BetScope.FULL_MATCH, StatType.MATCH);
            addOddItem(items, "moneyline", match.getId() + "_ml_x", "Draw", decX, btX);
        }
    }

    private void mapSpread(List<OddItem> items, BookmakerMatchDto match) {
        Double line1 = parseLine(match.getSpread1());
        Double line2 = parseLine(match.getSpread2());

        Double odds1 = parseLineOdds(match.getSpread1(), 1.909);
        Double odds2 = parseLineOdds(match.getSpread2(), 1.909);

        if (line1 != null && odds1 != null && odds1 > 1.0) {
            BetType bt1 = mapHandicapRecord("1", BetScope.FULL_MATCH, StatType.MATCH, false, line1);
            String label = match.getTeam1() + " (" + (line1 > 0 ? "+" + line1 : line1) + ")";
            addOddItem(items, "spread", match.getId() + "_sp_1", label, odds1, bt1);
        }

        if (line2 != null && odds2 != null && odds2 > 1.0) {
            BetType bt2 = mapHandicapRecord("2", BetScope.FULL_MATCH, StatType.MATCH, false, line2);
            String label = match.getTeam2() + " (" + (line2 > 0 ? "+" + line2 : line2) + ")";
            addOddItem(items, "spread", match.getId() + "_sp_2", label, odds2, bt2);
        }
    }

    private void mapTotals(List<OddItem> items, BookmakerMatchDto match) {
        Double totalLine = parseLine(match.getTotal());
        if (totalLine == null || totalLine <= 0) {
            return;
        }

        Double overOdds = parseLineOdds(match.getTotal(), 1.909);
        Double underOdds = parseLineOdds(match.getTotal(), 1.909);

        BetType btOver = mapTotalRecord("OVER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, totalLine);
        BetType btUnder = mapTotalRecord("UNDER", BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, false, totalLine);

        addOddItem(items, "total", match.getId() + "_tot_over", "Over (" + totalLine + ")", overOdds, btOver);
        addOddItem(items, "total", match.getId() + "_tot_under", "Under (" + totalLine + ")", underOdds, btUnder);
    }

    public Double parseAmericanOdds(String raw) {
        if (raw == null || raw.isBlank() || "-".equals(raw.trim())) {
            return null;
        }

        try {
            String clean = raw.replaceAll("[^0-9+-.]", "").trim();
            if (clean.isEmpty() || "-".equals(clean) || "+".equals(clean)) {
                return null;
            }

            double val = Double.parseDouble(clean);
            if (val > 0) {
                return Math.round(((val / 100.0) + 1.0) * 1000.0) / 1000.0;
            } else if (val < 0) {
                return Math.round(((100.0 / Math.abs(val)) + 1.0) * 1000.0) / 1000.0;
            }
        } catch (NumberFormatException ignored) {}

        return null;
    }

    private static final java.util.regex.Pattern AMERICAN_ODDS_PAREN_PATTERN =
            java.util.regex.Pattern.compile("\\(([+-]?\\d+)\\)");

    public Double parseLine(String raw) {
        if (raw == null || raw.isBlank() || "-".equals(raw.trim())) {
            return null;
        }

        try {
            // Replace fraction ½ with .5, ¼ with .25, ¾ with .75 and strip O / U
            String normalized = raw.replace("½", ".5")
                    .replace("¼", ".25")
                    .replace("¾", ".75")
                    .replace("O ", "")
                    .replace("U ", "")
                    .replace("o ", "")
                    .replace("u ", "")
                    .trim();

            // Strip American odds in parentheses e.g. from "+3.5 (-110)" -> "+3.5"
            String withoutOdds = AMERICAN_ODDS_PAREN_PATTERN.matcher(normalized).replaceAll("").trim();
            Double param = pro.datawiki.igaming.source.core.mapper.rule.LineParamParser.parseGeneralParam(withoutOdds);
            if (param != null) return param;
            return pro.datawiki.igaming.source.core.mapper.rule.LineParamParser.parseTotalParam(withoutOdds);
        } catch (Exception ignored) {}

        return null;
    }

    private Double parseLineOdds(String raw, Double defaultOdds) {
        if (raw == null) return defaultOdds;
        java.util.regex.Matcher m = AMERICAN_ODDS_PAREN_PATTERN.matcher(raw);
        if (m.find()) {
            Double am = parseAmericanOdds(m.group(1));
            if (am != null) return am;
        }
        return defaultOdds;
    }

    private void addOddItem(List<OddItem> items, String groupName, String factorId, String rawName, double value, BetType betType) {
        // Golden Rule #9: NO YIELD CAP! No silent drop of odds!
        if (value <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        item.setFactorId(factorId);
        item.setGroupName(groupName);
        item.setName(rawName);
        item.setValue(value);
        item.setBetType(betType);
        items.add(item);
    }
}
