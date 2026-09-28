package pro.datawiki.igaming.source.core.engine.xbet.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.Set;

@Component
public class XbetBinaryMarketStrategy implements XbetFactorStrategy {

    private static final Set<String> SUPPORTED = Set.of(
            "180", "181", "243", "244", "186", "187", "350", "351"
    );

    @Override
    public boolean supports(String factorCode) {
        return factorCode != null && SUPPORTED.contains(factorCode);
    }

    @Override
    public BetType map(String factorCode, Double param) {
        return switch (factorCode) {
            case "180" -> new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, StatType.MATCH);
            case "181" -> new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, StatType.MATCH);
            case "243" -> new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BOTH_HALVES_BTTS, BinaryMarketBet.Outcome.YES, StatType.MATCH);
            case "244" -> new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.BOTH_HALVES_BTTS, BinaryMarketBet.Outcome.NO, StatType.MATCH);
            case "186", "350" -> new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.ODD_EVEN, BinaryMarketBet.Outcome.EVEN, StatType.MATCH);
            case "187", "351" -> new BinaryMarketBet(BetScope.FULL_MATCH, BetSubject.MATCH, BinaryMarketBet.MarketType.ODD_EVEN, BinaryMarketBet.Outcome.ODD, StatType.MATCH);
            default -> null;
        };
    }

    @Override
    public String describe(String factorCode, Double param) {
        return switch (factorCode) {
            case "180" -> "BTTS_YES";
            case "181" -> "BTTS_NO";
            case "243" -> "BOTH_HALVES_BTTS_YES";
            case "244" -> "BOTH_HALVES_BTTS_NO";
            case "186", "350" -> "TOTAL_EVEN";
            case "187", "351" -> "TOTAL_ODD";
            default -> "T_" + factorCode;
        };
    }
}
