package pro.datawiki.igaming.source.core.engine.xbet.mapper;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;

import java.util.Set;

import static pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.*;

@Component
public class XbetFamilyMapper extends AbstractBetTypeMapper {

    private static final Set<String> SUPPORTED_BOOKMAKERS = Set.of(
            "1xbet", "melbet", "megapari", "linebet", "betandyou", "fansport", "888starz"
    );

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        if (bookmaker == null) return false;
        String lower = bookmaker.toLowerCase();
        return SUPPORTED_BOOKMAKERS.contains(lower);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        if (o == null) return null;

        return switch (o) {
            // Match Result (1X2)
            case "1" -> new MatchResultBet(BetScope.FULL_MATCH, WIN1, StatType.MATCH);
            case "2" -> new MatchResultBet(BetScope.FULL_MATCH, DRAW, StatType.MATCH);
            case "3" -> new MatchResultBet(BetScope.FULL_MATCH, WIN2, StatType.MATCH);

            // Double Chance
            case "4" -> new MatchResultBet(BetScope.FULL_MATCH, DC_1X, StatType.MATCH);
            case "5" -> new MatchResultBet(BetScope.FULL_MATCH, DC_12, StatType.MATCH);
            case "6" -> new MatchResultBet(BetScope.FULL_MATCH, DC_X2, StatType.MATCH);

            // Individual Total 1 Over/Under
            case "7" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.TEAM1, TotalBet.Direction.OVER, param, false, StatType.MATCH);
            case "8" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.TEAM1, TotalBet.Direction.UNDER, param, false, StatType.MATCH);

            // Match Totals
            case "9" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, StatType.MATCH);
            case "10" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, StatType.MATCH);

            // Handicaps
            case "11" -> new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Team.TEAM1, param, false, StatType.MATCH);
            case "12" -> new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Team.TEAM2, param, false, StatType.MATCH);

            // Individual Total 2 Over/Under
            case "13" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.TEAM2, TotalBet.Direction.OVER, param, false, StatType.MATCH);
            case "14" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.TEAM2, TotalBet.Direction.UNDER, param, false, StatType.MATCH);

            // Both Teams to Score (BTTS)
            case "180" -> new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, StatType.MATCH);
            case "181" -> new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, StatType.MATCH);

            default -> null;
        };
    }
}
