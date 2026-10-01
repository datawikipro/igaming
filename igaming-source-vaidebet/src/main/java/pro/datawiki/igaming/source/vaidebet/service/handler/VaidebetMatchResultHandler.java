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
@Order(80)
public class VaidebetMatchResultHandler extends AbstractVaidebetMarketHandler {

    @Override
    public boolean supports(VaidebetStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }
        Long id = group.getId();
        String name = getGroupName(group).toLowerCase();

        // Exclude markets handled by other dedicated handlers
        if (name.contains("double chance") || name.contains("двойной шанс") || name.contains("dupla chance")
                || name.contains("total") || name.contains("тотал")
                || name.contains("handicap") || name.contains("фора")
                || name.contains("both teams") || name.contains("обе забьют") || name.contains("ambas marcam")
                || name.contains("corner") || name.contains("углов") || name.contains("escanteio")
                || name.contains("card") || name.contains("карточ") || name.contains("cartao") || name.contains("cartão")
                || name.contains("booking")
                || name.contains("correct score") || name.contains("точный счет") || name.contains("resultado correto")) {
            return false;
        }

        // Map and round specific markets are handled by VaidebetEsportsHandler
        if (name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ") || name.contains("mapa ")) {
            return false;
        }

        if (id != null && (id == 1L || id == 702L || id == 4L || id == 7L)) {
            return true;
        }

        return name.contains("match result") || name.contains("1x2") || name.contains("исход") || name.contains("resultado final")
                || name.contains("moneyline") || name.contains("winner") || name.contains("победитель") || name.contains("vencedor")
                || name.contains("draw no bet") || name.contains("победа в матче") || name.contains("empate anula");
    }

    @Override
    public void handle(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        BetScope scope = resolveScope(group, sportType);
        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) {
                continue;
            }

            BetType betType = null;
            if (isTeam1Stake(stake, match)) {
                MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                betType = new MatchResultBet(scope, outcome, StatType.MATCH);
            } else if (isDrawStake(stake)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH);
            } else if (isTeam2Stake(stake, match)) {
                MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                betType = new MatchResultBet(scope, outcome, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, stake, groupName, betType, null);
            }
        }
    }

    private boolean isDrawStake(VaidebetStakeData s) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim().toUpperCase() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim().toUpperCase() : "";
        return "X".equals(en) || "X".equals(ru) || "Х".equals(en) || "Х".equals(ru)
                || "DRAW".equals(en) || "НИЧЬЯ".equals(ru) || "EMPATE".equals(en) || "EMPATE".equals(ru)
                || en.contains("DRAW") || ru.contains("НИЧЬЯ") || en.contains("EMPATE") || ru.contains("EMPATE");
    }

    private boolean isTeam1Stake(VaidebetStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("1".equalsIgnoreCase(en) || "Win1".equalsIgnoreCase(en) || "W1".equalsIgnoreCase(en)
                || "П1".equalsIgnoreCase(ru) || "P1".equalsIgnoreCase(en) || "Home".equalsIgnoreCase(en) || "Casa".equalsIgnoreCase(en)
                || "Mandante".equalsIgnoreCase(en) || "Mandante".equalsIgnoreCase(ru)
                || "Победа 1".equalsIgnoreCase(ru) || "Победа1".equalsIgnoreCase(ru)
                || "Team 1".equalsIgnoreCase(en) || "Команда 1".equalsIgnoreCase(ru)
                || "Time 1".equalsIgnoreCase(en) || "Time 1".equalsIgnoreCase(ru)
                || "Equipe 1".equalsIgnoreCase(en) || "Equipe 1".equalsIgnoreCase(ru)) {
            return true;
        }
        if (match != null && match.getTeam1() != null && !match.getTeam1().isBlank()) {
            String t1 = match.getTeam1().trim();
            if (en.equalsIgnoreCase(t1) || ru.equalsIgnoreCase(t1)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTeam2Stake(VaidebetStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("2".equalsIgnoreCase(en) || "Win2".equalsIgnoreCase(en) || "W2".equalsIgnoreCase(en)
                || "П2".equalsIgnoreCase(ru) || "P2".equalsIgnoreCase(en) || "Away".equalsIgnoreCase(en) || "Fora".equalsIgnoreCase(en)
                || "Visitante".equalsIgnoreCase(en) || "Visitante".equalsIgnoreCase(ru)
                || "Победа 2".equalsIgnoreCase(ru) || "Победа2".equalsIgnoreCase(ru)
                || "Team 2".equalsIgnoreCase(en) || "Команда 2".equalsIgnoreCase(ru)
                || "Time 2".equalsIgnoreCase(en) || "Time 2".equalsIgnoreCase(ru)
                || "Equipe 2".equalsIgnoreCase(en) || "Equipe 2".equalsIgnoreCase(ru)) {
            return true;
        }
        if (match != null && match.getTeam2() != null && !match.getTeam2().isBlank()) {
            String t2 = match.getTeam2().trim();
            if (en.equalsIgnoreCase(t2) || ru.equalsIgnoreCase(t2)) {
                return true;
            }
        }
        return false;
    }
}
