package pro.datawiki.igaming.source.apuestatotal.service.handler;

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
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

import java.util.List;

@Slf4j
@Component
@Order(40)
public class ApuestatotalBttsHandler extends AbstractApuestatotalMarketHandler {

    @Override
    public boolean supports(ApuestatotalStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }
        Long id = group.getId();
        String name = getGroupName(group).toLowerCase();

        // Exclude markets handled by other dedicated handlers
        if (name.contains("corner") || name.contains("углов") || name.contains("córner") || name.contains("esquina")
                || name.contains("card") || name.contains("карточ") || name.contains("tarjeta") || name.contains("booking")
                || name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ") || name.contains("mapa ")
                || name.contains("total") || name.contains("тотал")
                || name.contains("handicap") || name.contains("фора") || name.contains("hándicap")
                || name.contains("match result") || name.contains("1x2")
                || name.contains("double chance") || name.contains("двойной шанс") || name.contains("doble oportunidad")
                || name.contains("correct score") || name.contains("точный счет") || name.contains("marcador exacto")) {
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
                || name.contains("забьют обе команды")
                || name.contains("ambos equipos anotan")
                || name.contains("ambos anotan")
                || name.contains("ambas marcam")
                || name.contains("marcan ambos equipos");
    }

    @Override
    public void handle(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (ApuestatotalStakeData stake : group.getStakes()) {
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

    private BinaryMarketBet.Outcome resolveOutcome(ApuestatotalStakeData stake) {
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
        if ("YES".equals(text) || "ДА".equals(text) || "SI".equals(text) || "SÍ".equals(text) || "SIM".equals(text) || "Y".equals(text) || "Д".equals(text) || "S".equals(text)) {
            return true;
        }
        if (text.endsWith(" YES") || text.endsWith("-YES") || text.endsWith(": YES") || text.endsWith(":YES")
                || text.startsWith("YES ") || text.startsWith("YES-")
                || text.endsWith(" ДА") || text.endsWith("-ДА") || text.endsWith(": ДА") || text.endsWith(":ДА")
                || text.startsWith("ДА ") || text.startsWith("ДА-")
                || text.endsWith(" SI") || text.endsWith(" SÍ") || text.startsWith("SI ") || text.startsWith("SÍ ")
                || text.endsWith(" SIM") || text.endsWith("-SIM") || text.startsWith("SIM ")) {
            return true;
        }
        return text.matches(".*\\bYES\\b.*") || text.matches(".*\\bДА\\b.*") || text.matches(".*\\bS[IÍ]\\b.*") || text.matches(".*\\bSIM\\b.*");
    }

    private boolean isNo(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if ("NO".equals(text) || "НЕТ".equals(text) || "NÃO".equals(text) || "NAO".equals(text) || "N".equals(text) || "Н".equals(text)) {
            return true;
        }
        if (text.endsWith(" NO") || text.endsWith("-NO") || text.endsWith(": NO") || text.endsWith(":NO")
                || text.startsWith("NO ") || text.startsWith("NO-")
                || text.endsWith(" НЕТ") || text.endsWith("-НЕТ") || text.endsWith(": НЕТ") || text.endsWith(":НЕТ")
                || text.startsWith("НЕТ ") || text.startsWith("НЕТ-")
                || text.endsWith(" NÃO") || text.endsWith(" NAO") || text.startsWith("NÃO ") || text.startsWith("NAO ")) {
            return true;
        }
        return text.matches(".*\\bNO\\b.*") || text.matches(".*\\bНЕТ\\b.*") || text.matches(".*\\bNÃO\\b.*") || text.matches(".*\\bNAO\\b.*");
    }
}
