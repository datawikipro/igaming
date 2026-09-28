package pro.datawiki.igaming.source.core.engine.xbet.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.Set;

@Component
public class XbetHandicapStrategy implements XbetFactorStrategy {

    private static final Set<String> SUPPORTED = Set.of("7", "8", "2698", "2699");

    @Override
    public boolean supports(String factorCode) {
        return factorCode != null && SUPPORTED.contains(factorCode);
    }

    @Override
    public BetType map(String factorCode, Double param) {
        Double p = param != null ? param : 0.0;
        return switch (factorCode) {
            case "7" -> new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, p, false, StatType.MATCH);
            case "8" -> new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, p, false, StatType.MATCH);
            case "2698" -> new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, 0.0, false, StatType.MATCH);
            case "2699" -> new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, 0.0, false, StatType.MATCH);
            default -> null;
        };
    }

    @Override
    public String describe(String factorCode, Double param) {
        return switch (factorCode) {
            case "7" -> param != null ? "H1(" + param + ")" : "H1(0)";
            case "8" -> param != null ? "H2(" + param + ")" : "H2(0)";
            case "2698" -> "DNB_1";
            case "2699" -> "DNB_2";
            default -> "T_" + factorCode;
        };
    }
}
