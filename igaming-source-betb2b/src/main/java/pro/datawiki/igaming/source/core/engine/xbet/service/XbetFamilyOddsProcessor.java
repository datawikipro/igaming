package pro.datawiki.igaming.source.core.engine.xbet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.domain.MatchFactor;
import pro.datawiki.igaming.source.core.engine.AbstractOddsProcessor;
import pro.datawiki.igaming.source.core.engine.xbet.dto.XbetFamilyEvent;
import pro.datawiki.igaming.source.core.engine.xbet.dto.XbetFamilyGame;
import pro.datawiki.igaming.source.core.engine.xbet.strategy.XbetFactorStrategy;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class XbetFamilyOddsProcessor extends AbstractOddsProcessor<XbetFamilyGame> {

    private final List<XbetFactorStrategy> strategies;

    public XbetFamilyOddsProcessor(BetTypeResolverService betTypeResolver,
                                   UnmappedBetService unmappedBetService,
                                   List<XbetFactorStrategy> strategies) {
        super(betTypeResolver, unmappedBetService);
        this.strategies = strategies;
    }

    @Override
    public List<OddItem> processOdds(MatchCache match, XbetFamilyGame game, SportType sportType, String bookmakerName) {
        List<OddItem> odds = new ArrayList<>();
        Map<String, OddItem> resolvedOdds = new HashMap<>();

        if (game == null || game.getEvents() == null) return odds;

        for (XbetFamilyEvent event : getDeduplicatedEvents(game)) {
            if (event.getCoefficient() == null || event.getCoefficient() <= 1.0) continue;

            Integer typeId = event.getType();
            String rawName = resolveRawName(typeId, event.getParameter());
            Double param = event.getParameter();
            BetType betType = betTypeResolver.resolve(bookmakerName, sportType, rawName,
                    String.valueOf(typeId), param, match.getExternalId());

            if (betType == null || "UNKNOWN".equals(betType.code())) {
                unmappedBetService.saveAndNotify(bookmakerName, sportType != null ? sportType.name() : "UNKNOWN", rawName,
                        "XBET_FAMILY_MAIN", match.getExternalId());
                continue;
            }

            OddItem item = buildOddItem(String.valueOf(typeId), rawName, event.getCoefficient(),
                    event.getGroup() != null ? String.valueOf(event.getGroup()) : null, betType);

            if (handleDuplicate(resolvedOdds, odds, item, match, bookmakerName, sportType != null ? sportType.name() : null)) {
                continue;
            }

            resolvedOdds.put(getDeduplicationKey(item), item);
            odds.add(item);
        }
        return odds;
    }

    public List<MatchFactor> buildFactors(XbetFamilyGame game) {
        List<MatchFactor> factors = new ArrayList<>();
        if (game == null || game.getEvents() == null) return factors;

        for (XbetFamilyEvent event : getDeduplicatedEvents(game)) {
            if (event.getCoefficient() == null || event.getCoefficient() <= 1.0) continue;

            MatchFactor factor = new MatchFactor();
            factor.setFactorId(event.getType() != null ? String.valueOf(event.getType()) : "0");
            factor.setName(resolveRawName(event.getType(), event.getParameter()));
            factor.setValue(event.getCoefficient());
            factors.add(factor);
        }
        return factors;
    }

    private List<XbetFamilyEvent> getDeduplicatedEvents(XbetFamilyGame game) {
        if (game == null || game.getEvents() == null) return java.util.Collections.emptyList();
        Map<String, XbetFamilyEvent> unique = new java.util.LinkedHashMap<>();
        for (XbetFamilyEvent event : game.getEvents()) {
            if (event.getType() == null || event.getCoefficient() == null || event.getCoefficient() <= 1.0) {
                continue;
            }
            String key = event.getType() + ":" + (event.getParameter() != null ? event.getParameter() : "null");
            XbetFamilyEvent existing = unique.get(key);
            if (existing == null) {
                unique.put(key, event);
            } else {
                // If duplicate event found for the same (type, param), prefer higher precision (e.g. 1.001 vs 1.01)
                if (isMorePrecise(event.getCoefficient(), existing.getCoefficient())) {
                    unique.put(key, event);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    private boolean isMorePrecise(Double v1, Double v2) {
        if (v1 == null) return false;
        if (v2 == null) return true;
        String s1 = String.valueOf(v1);
        String s2 = String.valueOf(v2);
        int d1 = s1.contains(".") ? s1.length() - s1.indexOf('.') - 1 : 0;
        int d2 = s2.contains(".") ? s2.length() - s2.indexOf('.') - 1 : 0;
        return d1 > d2;
    }

    private String resolveRawName(Integer typeId, Double param) {
        if (typeId == null) return "T_null";
        String code = String.valueOf(typeId);
        return strategies.stream()
                .filter(s -> s.supports(code))
                .findFirst()
                .map(s -> s.describe(code, param))
                .orElseGet(() -> "T_" + typeId + (param != null ? "(" + param + ")" : ""));
    }
}
