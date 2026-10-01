package pro.datawiki.igaming.source.bcgame.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BcgameSportDto {
    private String id;
    private String name;
    private String slug;
    private Integer liveCount;
    private Integer upcomingCount;

    public BcgameSportDto(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
