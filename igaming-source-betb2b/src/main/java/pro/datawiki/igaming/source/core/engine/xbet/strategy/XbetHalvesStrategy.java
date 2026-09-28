package pro.datawiki.igaming.source.core.engine.xbet.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.*;

import java.util.Set;

import static pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.*;

@Component
public class XbetHalvesStrategy implements XbetFactorStrategy {

    private static final Set<String> SUPPORTED = Set.of(
            "15", "16", "17", "41", "42", "43", "45", "46", "47", "48",
            "61", "62", "63", "71", "72", "73", "75", "76", "77", "78"
    );

    @Override
    public boolean supports(String factorCode) {
        return factorCode != null && SUPPORTED.contains(factorCode);
    }

    @Override
    public BetType map(String factorCode, Double param) {
        Double p = param != null ? param : 0.0;
        return switch (factorCode) {
            // 1st Half
            case "15" -> new MatchResultBet(BetScope.HALF_1, WIN1, StatType.MATCH);
            case "16" -> new MatchResultBet(BetScope.HALF_1, DRAW, StatType.MATCH);
            case "17" -> new MatchResultBet(BetScope.HALF_1, WIN2, StatType.MATCH);
            case "41" -> new MatchResultBet(BetScope.HALF_1, DC_1X, StatType.MATCH);
            case "42" -> new MatchResultBet(BetScope.HALF_1, DC_12, StatType.MATCH);
            case "43" -> new MatchResultBet(BetScope.HALF_1, DC_X2, StatType.MATCH);
            case "45" -> param != null ? new TotalBet(BetScope.HALF_1, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, StatType.MATCH) : null;
            case "46" -> param != null ? new TotalBet(BetScope.HALF_1, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, StatType.MATCH) : null;
            case "47" -> new HandicapBet(BetScope.HALF_1, HandicapBet.Outcome.TEAM1, p, false, StatType.MATCH);
            case "48" -> new HandicapBet(BetScope.HALF_1, HandicapBet.Outcome.TEAM2, p, false, StatType.MATCH);

            // 2nd Half
            case "61" -> new MatchResultBet(BetScope.HALF_2, WIN1, StatType.MATCH);
            case "62" -> new MatchResultBet(BetScope.HALF_2, DRAW, StatType.MATCH);
            case "63" -> new MatchResultBet(BetScope.HALF_2, WIN2, StatType.MATCH);
            case "71" -> new MatchResultBet(BetScope.HALF_2, DC_1X, StatType.MATCH);
            case "72" -> new MatchResultBet(BetScope.HALF_2, DC_12, StatType.MATCH);
            case "73" -> new MatchResultBet(BetScope.HALF_2, DC_X2, StatType.MATCH);
            case "75" -> param != null ? new TotalBet(BetScope.HALF_2, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, StatType.MATCH) : null;
            case "76" -> param != null ? new TotalBet(BetScope.HALF_2, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, StatType.MATCH) : null;
            case "77" -> new HandicapBet(BetScope.HALF_2, HandicapBet.Outcome.TEAM1, p, false, StatType.MATCH);
            case "78" -> new HandicapBet(BetScope.HALF_2, HandicapBet.Outcome.TEAM2, p, false, StatType.MATCH);

            default -> null;
        };
    }

    @Override
    public String describe(String factorCode, Double param) {
        return switch (factorCode) {
            case "15" -> "1H_W1";
            case "16" -> "1H_X";
            case "17" -> "1H_W2";
            case "41" -> "1H_1X";
            case "42" -> "1H_12";
            case "43" -> "1H_X2";
            case "45" -> param != null ? "1H_TO(" + param + ")" : "1H_TotalOver";
            case "46" -> param != null ? "1H_TU(" + param + ")" : "1H_TotalUnder";
            case "47" -> param != null ? "1H_H1(" + param + ")" : "1H_H1(0)";
            case "48" -> param != null ? "1H_H2(" + param + ")" : "1H_H2(0)";
            case "61" -> "2H_W1";
            case "62" -> "2H_X";
            case "63" -> "2H_W2";
            case "71" -> "2H_1X";
            case "72" -> "2H_12";
            case "73" -> "2H_X2";
            case "75" -> param != null ? "2H_TO(" + param + ")" : "2H_TotalOver";
            case "76" -> param != null ? "2H_TU(" + param + ")" : "2H_TotalUnder";
            case "77" -> param != null ? "2H_H1(" + param + ")" : "2H_H1(0)";
            case "78" -> param != null ? "2H_H2(" + param + ")" : "2H_H2(0)";
            default -> "T_" + factorCode;
        };
    }
}
