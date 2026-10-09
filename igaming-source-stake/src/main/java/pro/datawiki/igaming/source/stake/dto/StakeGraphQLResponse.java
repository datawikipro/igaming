package pro.datawiki.igaming.source.stake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StakeGraphQLResponse {
    private DataContainer data;
    private List<Map<String, Object>> errors;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataContainer {
        private SportData sport;
        private List<StakeFixtureDto> fixtures;
        private List<StakeFixtureDto> upcomingFixtures;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SportData {
        private String id;
        private String name;
        private String slug;
        private List<StakeFixtureDto> fixtures;
        private List<TournamentData> tournaments;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TournamentData {
        private String id;
        private String name;
        private String slug;
        private List<StakeFixtureDto> fixtures;
    }
}
