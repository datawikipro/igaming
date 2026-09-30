package pro.datawiki.igaming.source.betano.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetanoEventDto {

    private String id;
    private String name;
    private String title;
    private String sportId;
    private String sportName;
    private String leagueId;
    private String leagueName;
    private String homeTeam;
    private String awayTeam;
    private Long startTime;
    private Boolean isLive;
    private String eventUrl;
    @Builder.Default
    private List<BetanoMarketDto> markets = new ArrayList<>();
}
