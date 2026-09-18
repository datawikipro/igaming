package pro.datawiki.igaming.source.bovada.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BovadaCompetitorDto {
    private String id;
    private String name;
    private Boolean home;
}
