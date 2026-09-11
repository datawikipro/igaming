package pro.datawiki.igaming.capture.liveresult.provider;

import pro.datawiki.igaming.dto.ReferenceFixtureDto;
import pro.datawiki.igaming.dto.TeamProfileDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatchFixtureProvider {

    String getProviderName();

    boolean supportsSport(String sportName);

    List<ReferenceFixtureDto> fetchScheduledFixtures(String sportName, LocalDate date);

    Optional<TeamProfileDto> fetchTeamProfile(String externalTeamId, String sportName);
}
