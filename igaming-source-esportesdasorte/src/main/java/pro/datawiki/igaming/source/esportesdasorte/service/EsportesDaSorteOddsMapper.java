package pro.datawiki.igaming.source.esportesdasorte.service;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarOddsMapper;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

@Component
public class EsportesDaSorteOddsMapper extends AbstractAltenarOddsMapper {

    public EsportesDaSorteOddsMapper(UnmappedBetService unmappedBetService,
                            SportNormalizationService sportNormalizationService,
                            BetTypeResolverService betTypeResolver) {
        super(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Override
    public String getBookmakerName() {
        return "esportesdasorte";
    }
}