package pro.datawiki.igaming.source.core.engine.xbet.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetFactorStrategy;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;

import java.util.List;
import java.util.Set;

/**
 * Maps combat sports (MMA, Boxing) markets for the XBet/BetB2B family.
 * Total rounds (factors 9 and 10) represent elapsed fight time (half-round rule: 2:30 mark).
 * Maps strictly to {@link StatType#ROUNDS_TIME}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class XbetCombatFamilyMapper extends AbstractBetTypeMapper {

    private static final Set<String> SUPPORTED_BOOKMAKERS = Set.of(
            "1xbet", "1x-bet", "1xbit", "22bet", "betwinner", "melbet", "melbet-com", "melbet-ru",
            "megapari", "linebet", "betandyou", "fansport", "888starz", "spinbetter", "jvspin",
            "helabet", "paripesa", "betlabel", "betobet", "mostbet", "sapphirebet"
    );

    private final List<XbetFactorStrategy> strategies;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        if (bookmaker == null) return false;
        if (sportType != SportType.MMA && sportType != SportType.BOXING) return false;
        String b = bookmaker.toLowerCase().trim();
        return SUPPORTED_BOOKMAKERS.contains(b) ||
               SUPPORTED_BOOKMAKERS.contains(b.replace("-", "")) ||
               SUPPORTED_BOOKMAKERS.contains(b.replace("_", "-"));
    }

    @Override
    public BetType map(String m, String o, Double param) {
        if (o == null) return null;

        // Combat sports totals: factor 9 (Over) and 10 (Under)
        if ("9".equals(o) && param != null) {
            return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, param, false, StatType.ROUNDS_TIME);
        }
        if ("10".equals(o) && param != null) {
            return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, param, false, StatType.ROUNDS_TIME);
        }

        // Delegate all other factors (1X2, double chance, etc.) to existing strategies
        return strategies.stream()
                .filter(s -> s.supports(o))
                .findFirst()
                .map(s -> s.map(o, param))
                .orElse(null);
    }
}
