package pro.datawiki.igaming.source.bovada.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BovadaEventDto {
    private String id;
    private String description;
    private String sport;
    private String competitionId;
    private Long startTime;
    private Boolean live;
    private Boolean awayTeamFirst;
    private String link;
    private List<BovadaCompetitorDto> competitors;
    private List<BovadaDisplayGroupDto> displayGroups;
}
