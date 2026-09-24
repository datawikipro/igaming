package pro.datawiki.igaming.source.digitain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitainRawMatch {
    private Long id;
    private String name;
    private String homeTeam;
    private String awayTeam;
    private String sportName;
    private String leagueName;
    private Long startTime;
    private Boolean isLive;
}
