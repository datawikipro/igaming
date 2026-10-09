package pro.datawiki.igaming.source.betway.service;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.engine.sbtech.service.AbstractSBTechOddsMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

@Component
public class BetwayOddsMapper extends AbstractSBTechOddsMapper {

    public BetwayOddsMapper(SportNormalizationService sportNormalizationService) {
        super(sportNormalizationService);
    }

    @Override
    public String getBookmakerName() {
        return "betway";
    }
}
