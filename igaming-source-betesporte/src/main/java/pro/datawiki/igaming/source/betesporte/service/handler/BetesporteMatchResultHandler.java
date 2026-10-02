package pro.datawiki.igaming.source.betesporte.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteStakeData;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteStakeGroupData;
import pro.datawiki.igaming.source.core.domain.MatchCache;

import java.util.List;

/**
 * Handler for regular Match Result markets (1X2, Moneyline, Winner).
 * Covers Full Match and period-level match results with multilingual support.
 * Order 80 — runs last among main market handlers (catch-all for result markets).
 */
@Slf4j
@Component
@Order(80)
public class BetesporteMatchResultHandler extends AbstractBetesporteMarketHandler {

    @Override
    public boolean supports(BetesporteStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }
        Long id = group.getId();
        String name = getGroupName(group).toLowerCase();

        // Exclude markets handled by other dedicated handlers
        if (name.contains("double chance") || name.contains("двойной шанс") || name.contains("dupla chance") || name.contains("dupla hipótese")
                || name.contains("total") || name.contains("тотал") || name.contains("over/under")
                || name.contains("handicap") || name.contains("фора") || name.contains("desvantagem") || name.contains("spread")
                || name.contains("both teams") || name.contains("обе забьют") || name.contains("ambas marcam")
                || name.contains("corner") || name.contains("углов") || name.contains("escanteio") || name.contains("canto")
                || name.contains("card") || name.contains("карточ") || name.contains("cartao") || name.contains("cartão") || name.contains("booking")
                || name.contains("correct score") || name.contains("точный счет") || name.contains("resultado correto")
                || name.contains("btts") || name.contains("обе забьют")) {
            return false;
        }

        // Map and round specific markets are handled by BetesporteEsportsHandler
        if (name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ") || name.contains("mapa ")) {
            return false;
        }

        // Known match result group IDs: 1 (Full Match 1X2), 702 (Esports Match Result), 4 (1st Half 1X2), 7 (2nd Half 1X2)
        if (id != null && (id == 1L || id == 702L || id == 4L || id == 7L)) {
            return true;
        }

        return name.contains("match result") || name.contains("1x2") || name.contains("isход") || name.contains("исход")
                || name.contains("moneyline") || name.contains("winner") || name.contains("победитель")
                || name.contains("vencedor") || name.contains("resultado final")
                || name.contains("draw no bet") || name.contains("победа в матче");
    }

    @Override
    public void handle(BetesporteStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        BetScope scope = resolveScope(group, sportType);
        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);
        String groupName = getGroupName(group);

        for (BetesporteStakeData stake : group.getStakes()) {
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

    private boolean isDrawStake(BetesporteStakeData s) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim().toUpperCase() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim().toUpperCase() : "";
        return "X".equals(en) || "X".equals(ru) || "Х".equals(en) || "Х".equals(ru)
                || "DRAW".equals(en) || "НИЧЬЯ".equals(ru) || "EMPATE".equals(en) || "EMPATE".equals(ru)
                || en.contains("DRAW") || ru.contains("НИЧЬЯ") || en.contains("EMPATE") || ru.contains("EMPATE");
    }

    private boolean isTeam1Stake(BetesporteStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("1".equalsIgnoreCase(en) || "1".equalsIgnoreCase(ru) || "Win1".equalsIgnoreCase(en) || "W1".equalsIgnoreCase(en)
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

    private boolean isTeam2Stake(BetesporteStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("2".equalsIgnoreCase(en) || "2".equalsIgnoreCase(ru) || "Win2".equalsIgnoreCase(en) || "W2".equalsIgnoreCase(en)
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
