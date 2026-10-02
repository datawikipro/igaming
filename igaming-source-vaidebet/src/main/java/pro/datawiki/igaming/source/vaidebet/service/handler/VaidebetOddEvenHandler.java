package pro.datawiki.igaming.source.vaidebet.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeData;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeGroupData;

import java.util.List;

/**
 * Handles Odd/Even (Чёт/Нечёт) markets for Vaidebet.
 *
 * Vaidebet exposes these markets in multilingual groups:
 *  - EN: "Odd/Even", "Odd or Even", "Total Goals Odd/Even"
 *  - RU: "Чёт/Нечёт", "Тотал чёт/нечёт"
 *  - PT: "Par/Ímpar"
 *
 * The handler also covers stat-specific variants (corners, cards) used in
 * statistical total arbitrage. Outcomes are mapped to BinaryMarketBet.Outcome.ODD /
 * BinaryMarketBet.Outcome.EVEN, matching the aggregator SUREBET_COMBINATIONS
 * entry {ODD, EVEN} for cross-bookmaker surebet detection.
 */
@Slf4j
@Component
@Order(45)
public class VaidebetOddEvenHandler extends AbstractVaidebetMarketHandler {

    // ------------------------------------------------------------------ supports

    @Override
    public boolean supports(VaidebetStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }

        // Exclude esports — Odd/Even is not offered for CS2/Dota maps
        if (VaidebetEsportsHandler.isEsports(sportType)) {
            return false;
        }

        Long id = group.getId();
        // Known Vaidebet Odd/Even group IDs: 8001, 8002 (internal platform IDs)
        if (id != null && (id == 8001L || id == 8002L)) {
            return true;
        }

        String name = getGroupName(group).toLowerCase();
        String nameRu = group.getNameRu() != null ? group.getNameRu().toLowerCase() : "";

        // Exact Odd/Even — must not be confused with total/handicap/BTTS
        return isOddEvenName(name) || isOddEvenName(nameRu);
    }

    private boolean isOddEvenName(String name) {
        if (name == null || name.isBlank()) return false;
        // Positive matches
        boolean matches = name.contains("odd/even")
                || name.contains("odd or even")
                || name.contains("odd & even")
                || name.contains("par/ímpar")
                || name.contains("par/impar")
                || name.contains("чёт/нечёт")
                || name.contains("чет/нечет")
                || name.contains("чётн")     // чётное/нечётное
                || name.contains("нечётн");   // нечётное

        if (!matches) return false;

        // Exclusions — do not capture BTTS, totals, handicap, correct-score markets
        return !name.contains("both")
                && !name.contains("btts")
                && !name.contains("total goals")
                && !name.contains("handicap")
                && !name.contains("correct score");
    }

    // ------------------------------------------------------------------ handle

    @Override
    public void handle(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);
        StatType statType = resolveStatType(groupName);

        for (VaidebetStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            BinaryMarketBet.Outcome outcome = resolveOutcome(stake);
            if (outcome == null) continue;

            BinaryMarketBet betType = new BinaryMarketBet(
                    scope,
                    BetSubject.MATCH,
                    BinaryMarketBet.MarketType.ODD_EVEN,
                    outcome,
                    statType
            );
            addOddItem(items, stake, groupName, betType, null);
        }
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Resolves StatType from group name — corners/cards odd-even use distinct types
     * so that the surebet engine can form market-homogeneous arbitrage groups.
     */
    private StatType resolveStatType(String groupName) {
        if (groupName == null) return StatType.MATCH;
        String lower = groupName.toLowerCase();
        if (lower.contains("corner") || lower.contains("углов") || lower.contains("escanteio")) {
            return StatType.CORNERS;
        }
        if (lower.contains("card") || lower.contains("карточ") || lower.contains("cartão") || lower.contains("booking")) {
            return StatType.CARDS;
        }
        return StatType.MATCH;
    }

    /**
     * Resolves ODD or EVEN outcome from stake names (EN / RU / PT).
     */
    private BinaryMarketBet.Outcome resolveOutcome(VaidebetStakeData stake) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim().toUpperCase() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim().toUpperCase() : "";

        if (isOdd(en) || isOdd(ru)) return BinaryMarketBet.Outcome.ODD;
        if (isEven(en) || isEven(ru)) return BinaryMarketBet.Outcome.EVEN;
        return null;
    }

    private boolean isOdd(String text) {
        if (text == null || text.isBlank()) return false;
        return "ODD".equals(text)
                || "НЕЧЁТН".equals(text)
                || "НЕЧЕТ".equals(text)
                || "НЕЧЁТНОЕ".equals(text)
                || "НЕЧЕТНОЕ".equals(text)
                || "ÍMPAR".equals(text)
                || "IMPAR".equals(text)
                || text.startsWith("ODD")
                || text.startsWith("НЕЧЁТ")
                || text.startsWith("НЕЧЕТ")
                || text.startsWith("ÍMPAR")
                || text.startsWith("IMPAR");
    }

    private boolean isEven(String text) {
        if (text == null || text.isBlank()) return false;
        return "EVEN".equals(text)
                || "ЧЁТН".equals(text)
                || "ЧЕТ".equals(text)
                || "ЧЁТНОЕ".equals(text)
                || "ЧЕТНОЕ".equals(text)
                || "PAR".equals(text)
                || text.startsWith("EVEN")
                || text.startsWith("ЧЁТН")
                || text.startsWith("ЧЕТ")
                || text.startsWith("PAR");
    }
}
