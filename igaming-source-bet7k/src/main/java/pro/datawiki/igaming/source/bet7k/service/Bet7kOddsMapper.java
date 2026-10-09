package pro.datawiki.igaming.source.bet7k.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarOddsMapper;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;

@Component
public class Bet7kOddsMapper extends AbstractAltenarOddsMapper {

    @Value("${app.bookmaker.name:bet7k}")
    private String bookmakerName;

    public Bet7kOddsMapper(UnmappedBetService unmappedBetService,
                            SportNormalizationService sportNormalizationService,
                            BetTypeResolverService betTypeResolver) {
        super(unmappedBetService, sportNormalizationService, betTypeResolver);
    }

    @Override
    public String getBookmakerName() {
        return (bookmakerName != null && !bookmakerName.isBlank()) ? bookmakerName : "bet7k";
    }
}