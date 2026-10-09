package pro.datawiki.igaming.source.bcgame.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BcgameEventDto {
    private String id;
    private String name;
    private String homeTeam;
    private String awayTeam;
    private String sportId;
    private String sportName;
    private String tournamentId;
    private String tournamentName;
    private Long startTime;
    private Boolean isLive;
    private String status;
    private List<BcgameMarketDto> markets;
}
