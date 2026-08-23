package pro.datawiki.igaming.capture.sofascore.provider;

import java.util.Optional;

public interface MatchResultProvider {

    record MatchMetadata(
            Long id,
            String sportName,
            String team1Name,
            String team2Name,
            Long startTime
    ) {}

    record MatchResultUpdate(
            String score1,
            String score2,
            String matchResult
    ) {}

    boolean supports(String sportName);

    Optional<MatchResultUpdate> fetchResult(MatchMetadata match);
}
