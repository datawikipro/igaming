package pro.datawiki.igaming.source.vaidebet.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeGroupData;

@Slf4j
@Component
@Order(30)
public class VaidebetStatsCardsHandler extends AbstractVaidebetStatsHandler {

    @Override
    public boolean supports(VaidebetStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }

        Long id = group.getId();
        if (id != null && (id == 187L || id == 188L || id == 189L)) {
            return true;
        }

        String name = getGroupName(group).toLowerCase();

        // Exclude corners, red cards, sending offs, esports
        if (name.contains("corner") || name.contains("углов") || name.contains("escanteio") || name.contains("cantos")
                || name.contains("red card") || name.contains("красн") || name.contains("cartão vermelho") || name.contains("cartao vermelho") || name.contains("удален")
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
                || name.contains("cartao")
                || name.contains("cartão")
                || name.contains("cartoes")
                || name.contains("cartões")
                || name.contains("amarelo");
    }

    @Override
    protected StatType getStatType() {
        return StatType.YELLOW_CARDS;
    }
}
