package pro.datawiki.igaming.source.digitain.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

@Slf4j
@Component
@Order(30)
public class DigitainStatsCardsHandler extends AbstractDigitainStatsHandler {

    @Override
    public boolean supports(DigitainStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }

        Long id = group.getId();
        if (id != null && (id == 187L || id == 188L || id == 189L)) {
            return true;
        }

        String name = getGroupName(group).toLowerCase();

        // Exclude corners, red cards, sending offs, esports
        if (name.contains("corner") || name.contains("углов")
                || name.contains("red card") || name.contains("красн") || name.contains("удален")
                || name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ")) {
            return false;
        }

        return name.contains("yellow card")
                || name.contains("booking")
                || name.contains("cards")
                || name.contains("card")
                || name.contains("жк")
                || name.contains("желт")
                || name.contains("карточ");
    }

    @Override
    protected StatType getStatType() {
        return StatType.YELLOW_CARDS;
    }
}
