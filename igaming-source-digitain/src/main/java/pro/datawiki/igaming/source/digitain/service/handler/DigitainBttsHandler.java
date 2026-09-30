package pro.datawiki.igaming.source.digitain.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

import java.util.List;

@Slf4j
@Component
public class DigitainBttsHandler extends AbstractDigitainMarketHandler {

    @Override
    public boolean supports(DigitainStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }
        Long id = group.getId();
        String name = getGroupName(group).toLowerCase();

        // Exclude markets handled by other dedicated handlers
        if (name.contains("corner") || name.contains("углов")
                || name.contains("card") || name.contains("карточ") || name.contains("booking")
                || name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ")
                || name.contains("total") || name.contains("тотал")
                || name.contains("handicap") || name.contains("фора")
                || name.contains("match result") || name.contains("1x2")
                || name.contains("double chance") || name.contains("двойной шанс")
                || name.contains("correct score") || name.contains("точный счет")) {
            return false;
        }

        // Known BTTS group IDs: 46 (Full Match BTTS), 10 (1st Half BTTS)
        if (id != null && (id == 46L || id == 10L)) {
            return true;
        }

        return name.contains("both teams to score")
                || name.contains("both teams score")
                || name.contains("both to score")
                || name.contains("btts")
                || name.contains("обе забьют")
                || name.contains("обе команды забьют")
                || name.contains("забьют обе команды");
    }

    @Override
    public void handle(DigitainStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (DigitainStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) {
                continue;
            }

            BinaryMarketBet.Outcome outcome = resolveOutcome(stake);
            if (outcome == null) {
                continue;
            }

            BinaryMarketBet betType = new BinaryMarketBet(
                    scope,
                    BetSubject.MATCH,
                    BinaryMarketBet.MarketType.BTTS,
                    outcome,
                    StatType.MATCH
            );

            addOddItem(items, stake, groupName, betType, null);
        }
    }

    private BinaryMarketBet.Outcome resolveOutcome(DigitainStakeData stake) {
        if (stake == null) {
            return null;
        }
        String en = stake.getNameEn() != null ? stake.getNameEn().trim().toUpperCase() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim().toUpperCase() : "";

        boolean yes = isYes(en) || isYes(ru);
        boolean no = isNo(en) || isNo(ru);

        if (yes && !no) {
            return BinaryMarketBet.Outcome.YES;
        }
        if (no && !yes) {
            return BinaryMarketBet.Outcome.NO;
        }

        return null;
    }

    private boolean isYes(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if ("YES".equals(text) || "ДА".equals(text) || "Y".equals(text) || "Д".equals(text)) {
            return true;
        }
        if (text.endsWith(" YES") || text.endsWith("-YES") || text.endsWith(": YES") || text.endsWith(":YES")
                || text.startsWith("YES ") || text.startsWith("YES-")
                || text.endsWith(" ДА") || text.endsWith("-ДА") || text.endsWith(": ДА") || text.endsWith(":ДА")
                || text.startsWith("ДА ") || text.startsWith("ДА-")) {
            return true;
        }
        return text.matches(".*\\bYES\\b.*") || text.matches(".*\\bДА\\b.*");
    }

    private boolean isNo(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if ("NO".equals(text) || "НЕТ".equals(text) || "N".equals(text) || "Н".equals(text)) {
            return true;
        }
        if (text.endsWith(" NO") || text.endsWith("-NO") || text.endsWith(": NO") || text.endsWith(":NO")
                || text.startsWith("NO ") || text.startsWith("NO-")
                || text.endsWith(" НЕТ") || text.endsWith("-НЕТ") || text.endsWith(": НЕТ") || text.endsWith(":НЕТ")
                || text.startsWith("НЕТ ") || text.startsWith("НЕТ-")) {
            return true;
        }
        return text.matches(".*\\bNO\\b.*") || text.matches(".*\\bНЕТ\\b.*");
    }
}
