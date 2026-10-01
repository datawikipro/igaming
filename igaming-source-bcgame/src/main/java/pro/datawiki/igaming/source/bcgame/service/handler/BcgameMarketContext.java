package pro.datawiki.igaming.source.bcgame.service.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.dto.SportType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BcgameMarketContext {
    private String eventId;
    private String sportName;
    private SportType sportType;
    private String leagueName;
    private String homeTeam;
    private String awayTeam;
    private Boolean isLive;
}
