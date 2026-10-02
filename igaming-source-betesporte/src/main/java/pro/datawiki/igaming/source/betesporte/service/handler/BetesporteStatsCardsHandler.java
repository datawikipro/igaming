package pro.datawiki.igaming.source.betesporte.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.betesporte.dto.BetesporteStakeGroupData;

/**
 * Handler for yellow card (ЖК / cartões amarelos) markets.
 * Order 30 — runs after Corners handler (order 20).
 */
@Slf4j
@Component
@Order(30)
public class BetesporteStatsCardsHandler extends AbstractBetesporteStatsHandler {

    @Override
    public boolean supports(BetesporteStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }

        // Cards are only meaningful in Football
        if (sportType != null && sportType != SportType.FOOTBALL && sportType != SportType.UNKNOWN) {
            return false;
        }

        Long id = group.getId();
        if (id != null && (id == 187L || id == 188L || id == 189L)) {
            return true;
        }

        String name = getGroupName(group).toLowerCase();

        // Exclude corners, red cards, esports
        if (name.contains("corner") || name.contains("углов") || name.contains("escanteio") || name.contains("cantos")
                || name.contains("red card") || name.contains("красн") || name.contains("cartão vermelho") || name.contains("cartao vermelho") || name.contains("удален")
                || name.contains("expuls") || name.contains("vermelho")
                || name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ") || name.contains("mapa ")) {
            return false;
        }

        return name.contains("yellow card")
                || name.contains("booking")
                || name.contains("cards")
                || name.contains("card")
                || name.contains("жк")
                || name.contains("желт")
                || name.contains("карточ")
                || name.contains("предупрежден")
                || name.contains("cartao")
                || name.contains("cartão")
                || name.contains("cartoes")
                || name.contains("cartões")
                || name.contains("amarelo")
                || name.contains("advertência")
                || name.contains("advertencia");
    }

    @Override
    protected StatType getStatType() {
        return StatType.YELLOW_CARDS;
    }
}
