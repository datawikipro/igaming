package pro.datawiki.igaming.source.esportesdasorte.service.handler;

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
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeData;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeGroupData;

import java.util.List;

@Slf4j
@Component
@Order(40)
public class EsportesdasorteBttsHandler extends AbstractEsportesdasorteMarketHandler {

    @Override
    public boolean supports(EsportesdasorteStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }
        Long id = group.getId();
        String name = getGroupName(group).toLowerCase();

        // Exclude markets handled by other dedicated handlers
        if (name.contains("corner") || name.contains("углов") || name.contains("escanteio")
                || name.contains("card") || name.contains("карточ") || name.contains("cartao") || name.contains("cartão") || name.contains("booking")
                || name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ") || name.contains("mapa ")
                || name.contains("total") || name.contains("тотал")
                || name.contains("handicap") || name.contains("фора")
                || name.contains("match result") || name.contains("1x2")
                || name.contains("double chance") || name.contains("двойной шанс") || name.contains("dupla chance")
                || name.contains("correct score") || name.contains("точный счет") || name.contains("resultado correto")) {
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
                || name.contains("ambas marcam")
                || name.contains("ambas as equipes marcam");
    }

    @Override
    public void handle(EsportesdasorteStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (EsportesdasorteStakeData stake : group.getStakes()) {
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

    private BinaryMarketBet.Outcome resolveOutcome(EsportesdasorteStakeData stake) {
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
        return text.equals("YES") || text.equals("ДА") || text.equals("SIM")
                || text.startsWith("YES") || text.startsWith("ДА") || text.startsWith("SIM")
                || text.contains("YES") || text.contains("ДА") || text.contains("SIM");
    }

    private boolean isNo(String text) {
        return text.equals("NO") || text.equals("НЕТ") || text.equals("NÃO") || text.equals("NAO")
                || text.startsWith("NO") || text.startsWith("НЕТ") || text.startsWith("NÃO") || text.startsWith("NAO")
                || text.contains("NO") || text.contains("НЕТ") || text.contains("NÃO") || text.contains("NAO");
    }
}
