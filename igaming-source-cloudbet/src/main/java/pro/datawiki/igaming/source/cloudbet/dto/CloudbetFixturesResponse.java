package pro.datawiki.igaming.source.cloudbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudbetFixturesResponse {
    private List<CloudbetFixtureDto> fixtures;
    private List<CloudbetSportDto> sports;
}
