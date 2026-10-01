package pro.datawiki.igaming.source.wplay.dto;

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
public class WplayEventDto {
    private String id;
    private String name;
    private String sportName;
    private String leagueName;
    private String homeTeam;
    private String awayTeam;
    private Long startTime;
    private Boolean isLive;
    private String eventUrl;
    @Builder.Default
    private List<WplayMarketDto> markets = new ArrayList<>();
}
