package pro.datawiki.igaming.source.apuestatotal.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

@Slf4j
@Component
@Order(20)
public class ApuestatotalStatsCornersHandler extends AbstractApuestatotalStatsHandler {

    @Override
    public boolean supports(ApuestatotalStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }

        Long id = group.getId();
        if (id != null && (id == 166L || id == 167L || id == 168L)) {
            return true;
        }

        String name = getGroupName(group).toLowerCase();

        // Exclude other markets that might accidentally contain substrings
        if (name.contains("card") || name.contains("карточ") || name.contains("tarjeta") || name.contains("booking")
                || name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ") || name.contains("mapa ")) {
            return false;
        }

        return name.contains("corner") || name.contains("углов") || name.contains("córner") || name.contains("corner") || name.contains("tiros de esquina") || name.contains("esquina");
    }

    @Override
    protected StatType getStatType() {
        return StatType.CORNERS;
    }
}
