package pro.datawiki.igaming.source.bcgame.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BcgameSportDto {
    private String id;
    private String name;
    private String slug;
    private Integer liveCount;
    private Integer upcomingCount;
}
