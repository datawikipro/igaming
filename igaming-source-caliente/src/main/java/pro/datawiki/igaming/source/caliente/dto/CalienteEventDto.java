package pro.datawiki.igaming.source.caliente.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalienteEventDto {
    private String id;
    private String name;
    private String sportName;
    private String categoryName;
    private String homeTeam;
    private String awayTeam;
    private Long startTime;
    private Boolean isLive;
    private List<CalienteMarketDto> markets;

    public String getSport() {
        return sportName != null ? sportName : "FOOTBALL";
    }

    public String getLeague() {
        return categoryName != null ? categoryName : "General";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CalienteMarketDto {
        private String id;
        private String name;
        private Double line;
        private List<CalienteOutcomeDto> outcomes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CalienteOutcomeDto {
        private String id;
        private String name;
        private Double odds;
        private Double line;
    }
}