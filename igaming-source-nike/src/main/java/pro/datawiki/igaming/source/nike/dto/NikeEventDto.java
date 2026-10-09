package pro.datawiki.igaming.source.nike.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NikeEventDto {
    private String id;
    private String name;
    private String homeTeam;
    private String awayTeam;
    private String sport;
    private String league;
    private Long startTime;
    private Boolean isLive;
    private List<NikeMarketDto> markets;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NikeMarketDto {
        private String id;
        private String name;
        private String type;
        private Double line;
        private List<NikeOutcomeDto> outcomes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NikeOutcomeDto {
        private String id;
        private String name;
        private Double odds;
        private Double line;
    }
}
