package pro.datawiki.igaming.source.paf.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.engine.kambi.service.AbstractKambiOddsMapper;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

@Component
@Slf4j
public class PafOddsMapper extends AbstractKambiOddsMapper {

    public PafOddsMapper(UnmappedBetService unmappedBetService,
                         SportNormalizationService sportNormalizationService) {
        super(unmappedBetService, sportNormalizationService);
    }

    @Override
    public String getBookmakerName() {
        return "paf";
    }
}