package pro.datawiki.igaming.source.core.engine.xbet.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.Set;

import static pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.*;

@Component
public class XbetMainResultStrategy implements XbetFactorStrategy {

    private static final Set<String> SUPPORTED = Set.of("1", "2", "3", "4", "5", "6", "401", "402");

    @Override
    public boolean supports(String factorCode) {
        return factorCode != null && SUPPORTED.contains(factorCode);
    }

    @Override
    public BetType map(String factorCode, Double param) {
        return switch (factorCode) {
            case "1" -> new MatchResultBet(BetScope.FULL_MATCH, WIN1, StatType.MATCH);
            case "2" -> new MatchResultBet(BetScope.FULL_MATCH, DRAW, StatType.MATCH);
            case "3" -> new MatchResultBet(BetScope.FULL_MATCH, WIN2, StatType.MATCH);
            case "4" -> new MatchResultBet(BetScope.FULL_MATCH, DC_1X, StatType.MATCH);
            case "5" -> new MatchResultBet(BetScope.FULL_MATCH, DC_12, StatType.MATCH);
            case "6" -> new MatchResultBet(BetScope.FULL_MATCH, DC_X2, StatType.MATCH);
            case "401" -> new MatchResultBet(BetScope.FULL_MATCH_INCLUDING_OT, WIN1, StatType.MATCH);
            case "402" -> new MatchResultBet(BetScope.FULL_MATCH_INCLUDING_OT, WIN2, StatType.MATCH);
            default -> null;
        };
    }

    @Override
    public String describe(String factorCode, Double param) {
        return switch (factorCode) {
            case "1" -> "W1";
            case "2" -> "X";
            case "3" -> "W2";
            case "4" -> "1X";
            case "5" -> "12";
            case "6" -> "X2";
            case "401" -> "W1_OT";
            case "402" -> "W2_OT";
            default -> "T_" + factorCode;
        };
    }
}
