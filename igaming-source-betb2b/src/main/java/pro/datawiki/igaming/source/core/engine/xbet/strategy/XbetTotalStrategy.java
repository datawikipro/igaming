package pro.datawiki.igaming.source.core.engine.xbet.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

import java.util.Set;

@Component
public class XbetTotalStrategy implements XbetFactorStrategy {

    private static final Set<String> SUPPORTED = Set.of("9", "10", "11", "12", "13", "14");

    @Override
    public boolean supports(String factorCode) {
        return factorCode != null && SUPPORTED.contains(factorCode);
    }

    @Override
    public BetType map(String factorCode, Double param) {
        if (param == null) return null;
        return switch (factorCode) {
            case "9"  -> new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, StatType.MATCH);
            case "10" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, StatType.MATCH);
            case "11" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.TEAM1, TotalBet.Direction.OVER, param, false, StatType.MATCH);
            case "12" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.TEAM1, TotalBet.Direction.UNDER, param, false, StatType.MATCH);
            case "13" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.TEAM2, TotalBet.Direction.OVER, param, false, StatType.MATCH);
            case "14" -> new TotalBet(BetScope.FULL_MATCH, BetSubject.TEAM2, TotalBet.Direction.UNDER, param, false, StatType.MATCH);
            default -> null;
        };
    }

    @Override
    public String describe(String factorCode, Double param) {
        return switch (factorCode) {
            case "9"  -> param != null ? "TO(" + param + ")" : "TotalOver";
            case "10" -> param != null ? "TU(" + param + ")" : "TotalUnder";
            case "11" -> param != null ? "IT1_O(" + param + ")" : "IT1_Over";
            case "12" -> param != null ? "IT1_U(" + param + ")" : "IT1_Under";
            case "13" -> param != null ? "IT2_O(" + param + ")" : "IT2_Over";
            case "14" -> param != null ? "IT2_U(" + param + ")" : "IT2_Under";
            default -> "T_" + factorCode;
        };
    }
}
