package pro.datawiki.igaming.source.core.engine.xbet.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.Set;

import static pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.*;

@Component
public class XbetStatsStrategy implements XbetFactorStrategy {

    private static final Set<String> SUPPORTED = Set.of(
            "1707", "1708", "1709", "1711", "1712",
            "1738", "1739", "1740", "1742", "1743"
    );

    @Override
    public boolean supports(String factorCode) {
        return factorCode != null && SUPPORTED.contains(factorCode);
    }

    @Override
    public BetType map(String factorCode, Double param) {
        return switch (factorCode) {
            // Corners
            case "1707" -> new MatchResultBet(BetScope.FULL_MATCH, WIN1, StatType.CORNERS);
            case "1708" -> new MatchResultBet(BetScope.FULL_MATCH, WIN2, StatType.CORNERS);
            case "1709" -> new MatchResultBet(BetScope.FULL_MATCH, DRAW, StatType.CORNERS);
            case "1711" -> param != null ? new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, StatType.CORNERS) : null;
            case "1712" -> param != null ? new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, StatType.CORNERS) : null;

            // Yellow Cards
            case "1738" -> new MatchResultBet(BetScope.FULL_MATCH, WIN1, StatType.YELLOW_CARDS);
            case "1739" -> new MatchResultBet(BetScope.FULL_MATCH, WIN2, StatType.YELLOW_CARDS);
            case "1740" -> new MatchResultBet(BetScope.FULL_MATCH, DRAW, StatType.YELLOW_CARDS);
            case "1742" -> param != null ? new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, StatType.YELLOW_CARDS) : null;
            case "1743" -> param != null ? new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, StatType.YELLOW_CARDS) : null;

            default -> null;
        };
    }

    @Override
    public String describe(String factorCode, Double param) {
        return switch (factorCode) {
            case "1707" -> "CORNERS_W1";
            case "1708" -> "CORNERS_W2";
            case "1709" -> "CORNERS_X";
            case "1711" -> param != null ? "CORNERS_TO(" + param + ")" : "CORNERS_TotalOver";
            case "1712" -> param != null ? "CORNERS_TU(" + param + ")" : "CORNERS_TotalUnder";
            case "1738" -> "YC_W1";
            case "1739" -> "YC_W2";
            case "1740" -> "YC_X";
            case "1742" -> param != null ? "YC_TO(" + param + ")" : "YC_TotalOver";
            case "1743" -> param != null ? "YC_TU(" + param + ")" : "YC_TotalUnder";
            default -> "T_" + factorCode;
        };
    }
}
