package pro.datawiki.igaming.source.vaidebet.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeData;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeGroupData;

import java.util.List;

@Slf4j
@Component
@Order(50)
public class VaidebetDoubleChanceHandler extends AbstractVaidebetMarketHandler {

    @Override
    public boolean supports(VaidebetStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }
        Long id = group.getId();
        String name = getGroupName(group).toLowerCase();

        if (name.contains("corner") || name.contains("углов") || name.contains("escanteio")
                || name.contains("card") || name.contains("карточ") || name.contains("cartao") || name.contains("cartão")) {
            return false;
        }

        if (id != null && (id == 992L || id == 993L)) {
            return true;
        }

        return name.contains("double chance") || name.contains("двойной шанс")
                || name.contains("двойной исход") || name.contains("dupla chance") || name.contains("dupla hipótese");
    }

    @Override
    public void handle(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) {
                continue;
            }

            MatchResultBet.Outcome outcome = mapDoubleChanceOutcome(stake);
            if (outcome != null) {
                BetType betType = new MatchResultBet(scope, outcome, StatType.MATCH);
                addOddItem(items, stake, groupName, betType, null);
            }
        }
    }

    private MatchResultBet.Outcome mapDoubleChanceOutcome(VaidebetStakeData stake) {
        if (stake == null) {
            return null;
        }
        String nameEn = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
        String nameRu = stake.getNameRu() != null ? stake.getNameRu().trim() : "";

        // Try built-in mapper first
        BetType mapped = map1X2DCRecord(nameEn, BetScope.FULL_MATCH, StatType.MATCH);
        if (mapped == null && !nameRu.isBlank()) {
            mapped = map1X2DCRecord(nameRu, BetScope.FULL_MATCH, StatType.MATCH);
        }

        if (mapped instanceof MatchResultBet mrb) {
            if (mrb.outcome() == MatchResultBet.Outcome.DC_1X
                    || mrb.outcome() == MatchResultBet.Outcome.DC_12
                    || mrb.outcome() == MatchResultBet.Outcome.DC_X2) {
                return mrb.outcome();
            }
        }

        // Comprehensive normalization fallback
        String combined = (nameEn + " " + nameRu).toUpperCase();
        if (combined.contains("1X") || combined.contains("1-X") || combined.contains("1Х") || combined.contains("1/X")
                || combined.contains("1 OR X") || combined.contains("1 ИЛИ X") || combined.contains("1 ИЛИ Х")
                || combined.contains("1 ИЛИ НИЧЬЯ") || combined.contains("1 OU EMPATE") || combined.contains("1 OU X")
                || combined.contains("HOME/DRAW") || combined.contains("П1Х")) {
            return MatchResultBet.Outcome.DC_1X;
        } else if (combined.contains("12") || combined.contains("1-2") || combined.contains("1/2")
                || combined.contains("1 OR 2") || combined.contains("1 ИЛИ 2") || combined.contains("1 OU 2")
                || combined.contains("HOME/AWAY")) {
            return MatchResultBet.Outcome.DC_12;
        } else if (combined.contains("X2") || combined.contains("X-2") || combined.contains("2X") || combined.contains("Х2")
                || combined.contains("X/2") || combined.contains("Х/2") || combined.contains("2/X")
                || combined.contains("X OR 2") || combined.contains("НИЧЬЯ ИЛИ 2") || combined.contains("EMPATE OU 2")
                || combined.contains("X ИЛИ 2") || combined.contains("Х ИЛИ 2") || combined.contains("X OU 2")
                || combined.contains("DRAW/AWAY") || combined.contains("ПХ2")) {
            return MatchResultBet.Outcome.DC_X2;
        }

        return null;
    }
}
