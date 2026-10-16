package pro.datawiki.igaming.capture.sofascore.provider;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public interface MatchResultProvider {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MatchMetadata(
            @JsonProperty("id") Long id,
            @JsonProperty("sportName") @JsonAlias("sport_name") String sportName,
            @JsonProperty("team1Name") @JsonAlias("team1_name") String team1Name,
            @JsonProperty("team2Name") @JsonAlias("team2_name") String team2Name,
            @JsonProperty("startTime") @JsonAlias("start_time") Long startTime
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MatchResultUpdate(
            @JsonProperty("score1") String score1,
            @JsonProperty("score2") String score2,
            @JsonProperty("matchResult") @JsonAlias("match_result") String matchResult
    ) {}

    boolean supports(String sportName);

    Optional<MatchResultUpdate> fetchResult(MatchMetadata match);
}
