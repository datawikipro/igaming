package pro.datawiki.igaming.source.esportesdasorte.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeGroupData;

@Slf4j
@Component
@Order(20)
public class EsportesdasorteStatsCornersHandler extends AbstractEsportesdasorteStatsHandler {

    @Override
    public boolean supports(EsportesdasorteStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }

        if (sportType != null && sportType != SportType.FOOTBALL && sportType != SportType.UNKNOWN) {
            return false;
        }

        Long id = group.getId();
        if (id != null && (id == 166L || id == 167L || id == 168L)) {
            return true;
        }

        String name = getGroupName(group).toLowerCase();

        // Exclude other markets that might accidentally contain substrings
        if (name.contains("card") || name.contains("карточ") || name.contains("cartao") || name.contains("cartão") || name.contains("booking")
                || name.contains("map ") || name.contains("карта ") || name.contains("round ") || name.contains("раунд ") || name.contains("mapa ")) {
            return false;
        }

        return name.contains("corner") || name.contains("углов") || name.contains("escanteio") || name.contains("canto");
    }

    @Override
    protected StatType getStatType() {
        return StatType.CORNERS;
    }
}
