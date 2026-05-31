package pro.datawiki.igaming.source.betb2b.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.domain.MatchFactor;
import pro.datawiki.igaming.source.core.engine.AbstractOddsProcessor;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.betb2b.dto.Betb2bEvent;
import pro.datawiki.igaming.source.betb2b.dto.Betb2bGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class Betb2bOddsProcessor extends AbstractOddsProcessor<Betb2bGame> {

    public Betb2bOddsProcessor(BetTypeResolverService betTypeResolver, UnmappedBetService unmappedBetService) {
        super(betTypeResolver, unmappedBetService);
    }

    @Override
    public List<OddItem> processOdds(MatchCache match, Betb2bGame game, SportType sportType, String bookmakerName) {
        List<OddItem> odds = new ArrayList<>();
        Map<String, OddItem> resolvedOdds = new HashMap<>();

        if (game == null || game.getEvents() == null) return odds;

        for (Betb2bEvent event : game.getEvents()) {
            if (event.getCoefficient() == null) continue;

            Integer typeId = event.getType();
            String rawName = resolveRawName(typeId, event.getParameter());
            Double param = event.getParameter();
            BetType betType = betTypeResolver.resolve(bookmakerName, sportType, rawName,
                    String.valueOf(typeId), param, match.getExternalId());

            if ("UNKNOWN".equals(betType.code())) {
                unmappedBetService.saveAndNotify(bookmakerName, sportType.name(), rawName,
                        "BETB2B_MAIN", match.getExternalId());
                continue;
            }

            OddItem item = buildOddItem(String.valueOf(typeId), rawName, event.getCoefficient(), betType);

            if (isDuplicate(resolvedOdds, item, match.getExternalId())) {
                continue;
            }

            resolvedOdds.put(betType.code(), item);
            odds.add(item);
        }
        return odds;
    }

    /**
     * Build MatchFactor list for fast initial persistence (before full enrichment).
     * Uses standard MatchFactor fields: factorId, name, value.
     */
    public List<MatchFactor> buildFactors(Betb2bGame game) {
        List<MatchFactor> factors = new ArrayList<>();
        if (game == null || game.getEvents() == null) return factors;

        for (Betb2bEvent event : game.getEvents()) {
            if (event.getCoefficient() == null) continue;

            MatchFactor factor = new MatchFactor();
            factor.setFactorId(event.getType() != null ? String.valueOf(event.getType()) : "0");
            factor.setName(resolveRawName(event.getType(), event.getParameter()));
            factor.setValue(event.getCoefficient());
            factors.add(factor);
        }
        return factors;
    }

    /**
     * Maps 1xBet-style numeric type IDs to human-readable bet names.
     * Types 1/2/3 = 1X2, others kept as raw key.
     */
    private String resolveRawName(Integer typeId, Double param) {
        if (typeId == null) return "T_null";
        return switch (typeId) {
            case 1  -> "W1";
            case 2  -> "X";
            case 3  -> "W2";
            case 4  -> "1X";
            case 5  -> "12";
            case 6  -> "X2";
            case 9  -> param != null ? "TM(" + param + ")" : "TotalMore";
            case 10 -> param != null ? "TL(" + param + ")" : "TotalLess";
            case 11 -> param != null ? "HM1(" + param + ")" : "Handicap1";
            case 12 -> param != null ? "HL2(" + param + ")" : "Handicap2";
            default -> "T_" + typeId + (param != null ? "(" + param + ")" : "");
        };
    }
}
