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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class WplayOddsMapper extends AbstractBetTypeMapper {

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
