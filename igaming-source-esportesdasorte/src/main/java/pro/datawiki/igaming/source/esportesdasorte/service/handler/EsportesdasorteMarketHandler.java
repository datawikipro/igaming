package pro.datawiki.igaming.source.esportesdasorte.service.handler;

import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeGroupData;

import java.util.List;

public interface EsportesdasorteMarketHandler {
    boolean supports(EsportesdasorteStakeGroupData group, SportType sportType);
    void handle(EsportesdasorteStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items);
}
