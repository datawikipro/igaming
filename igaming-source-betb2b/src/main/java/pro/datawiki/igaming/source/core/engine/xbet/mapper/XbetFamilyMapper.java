package pro.datawiki.igaming.source.core.engine.xbet.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetFactorStrategy;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class XbetFamilyMapper extends AbstractBetTypeMapper {

    private static final Set<String> SUPPORTED_BOOKMAKERS = Set.of(
            "1xbet", "1x-bet", "1xbit", "22bet", "betwinner", "melbet", "melbet-com", "melbet-ru",
            "megapari", "linebet", "betandyou", "fansport", "888starz", "spinbetter", "jvspin",
            "helabet", "paripesa", "betlabel", "betobet", "mostbet", "sapphirebet"
    );

    private final List<XbetFactorStrategy> strategies;

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        if (bookmaker == null) return false;
        if (sportType == SportType.MMA || sportType == SportType.BOXING) return false;
        String b = bookmaker.toLowerCase().trim();
        return SUPPORTED_BOOKMAKERS.contains(b) ||
               SUPPORTED_BOOKMAKERS.contains(b.replace("-", "")) ||
               SUPPORTED_BOOKMAKERS.contains(b.replace("_", "-"));
    }

    @Override
    public BetType map(String m, String o, Double param) {
        if (o == null) return null;
        return strategies.stream()
                .filter(s -> s.supports(o))
                .findFirst()
                .map(s -> s.map(o, param))
                .orElse(null);
    }
}
