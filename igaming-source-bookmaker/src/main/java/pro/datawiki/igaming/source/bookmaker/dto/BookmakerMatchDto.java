package pro.datawiki.igaming.source.bookmaker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookmakerMatchDto {
    private String id;
    private String sport;
    private String league;
    private String timeRaw;
    private Long startTime;
    private String team1;
    private String team2;
    private String spread1;
    private String spread2;
    private String total;
    private String ml1;
    private String ml2;
    private String eventUrl;
    private boolean isLive;
}
