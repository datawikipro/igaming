package pro.datawiki.igaming.source.iddaa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IddaaEventDto {
    private String id;
    private String name;
    private String sport;
    private String leagueName;
    private Long startTime;
    private boolean isLive;
    private String homeTeam;
    private String awayTeam;
    private Map<String, Double> odds;
}
