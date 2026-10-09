package pro.datawiki.igaming.source.supersport.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperSportEventDto {
    private String id;
    private String name;
    private String homeTeam;
    private String awayTeam;
    private String sport;
    private String league;
    private Long startTime;
    private Boolean isLive;
    private List<SuperSportMarketDto> markets;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SuperSportMarketDto {
        private String id;
        private String name;
        private String type;
        private Double line;
        private List<SuperSportOutcomeDto> outcomes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SuperSportOutcomeDto {
        private String id;
        private String name;
        private Double odds;
        private Double line;
    }
}
