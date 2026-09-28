package pro.datawiki.igaming.source.core.engine.xbet.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.HalfTimeFullTimeBet;

import java.util.Set;

@Component
public class XbetHalfTimeFullTimeStrategy implements XbetFactorStrategy {

    private static final Set<String> SUPPORTED = Set.of("21", "22", "23", "24", "25", "26", "27", "28", "29");

    @Override
    public boolean supports(String factorCode) {
        return factorCode != null && SUPPORTED.contains(factorCode);
    }

    @Override
    public BetType map(String factorCode, Double param) {
        return switch (factorCode) {
            case "21" -> new HalfTimeFullTimeBet(HalfTimeFullTimeBet.Outcome.W1_W1);
            case "22" -> new HalfTimeFullTimeBet(HalfTimeFullTimeBet.Outcome.W1_X);
            case "23" -> new HalfTimeFullTimeBet(HalfTimeFullTimeBet.Outcome.W1_W2);
            case "24" -> new HalfTimeFullTimeBet(HalfTimeFullTimeBet.Outcome.X_W1);
            case "25" -> new HalfTimeFullTimeBet(HalfTimeFullTimeBet.Outcome.X_X);
            case "26" -> new HalfTimeFullTimeBet(HalfTimeFullTimeBet.Outcome.X_W2);
            case "27" -> new HalfTimeFullTimeBet(HalfTimeFullTimeBet.Outcome.W2_W1);
            case "28" -> new HalfTimeFullTimeBet(HalfTimeFullTimeBet.Outcome.W2_X);
            case "29" -> new HalfTimeFullTimeBet(HalfTimeFullTimeBet.Outcome.W2_W2);
            default -> null;
        };
    }

    @Override
    public String describe(String factorCode, Double param) {
        return switch (factorCode) {
            case "21" -> "HTFT_1_1";
            case "22" -> "HTFT_1_X";
            case "23" -> "HTFT_1_2";
            case "24" -> "HTFT_X_1";
            case "25" -> "HTFT_X_X";
            case "26" -> "HTFT_X_2";
            case "27" -> "HTFT_2_1";
            case "28" -> "HTFT_2_X";
            case "29" -> "HTFT_2_2";
            default -> "T_" + factorCode;
        };
    }
}
