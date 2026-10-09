package pro.datawiki.igaming.source.estave.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EstaveEventDto {
    private String id;
    private String name;
    private String homeTeam;
    private String awayTeam;
    private String sport;
    private String league;
    private Long startTime;
    private Boolean isLive;
    private List<EstaveMarketDto> markets;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EstaveMarketDto {
        private String id;
        private String name;
        private String type;
        private Double line;
        private List<EstaveOutcomeDto> outcomes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EstaveOutcomeDto {
        private String id;
        private String name;
        private Double odds;
        private Double line;
    }
}
