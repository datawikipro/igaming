package pro.datawiki.igaming.source.tenbet.service;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.engine.sbtech.service.AbstractSBTechOddsMapper;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;

@Component
public class TenBetOddsMapper extends AbstractSBTechOddsMapper {

    public TenBetOddsMapper(SportNormalizationService sportNormalizationService) {
        super(sportNormalizationService);
    }

    @Override
    public String getBookmakerName() {
        return "10bet";
    }
}
