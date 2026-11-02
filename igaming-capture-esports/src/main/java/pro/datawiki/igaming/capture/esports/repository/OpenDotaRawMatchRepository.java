package pro.datawiki.igaming.capture.esports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.capture.esports.domain.OpenDotaRawMatchEntity;

import java.util.List;

@Repository
public interface OpenDotaRawMatchRepository extends JpaRepository<OpenDotaRawMatchEntity, Long> {

    List<OpenDotaRawMatchEntity> findByLeagueId(Long leagueId);

    List<OpenDotaRawMatchEntity> findByRadiantTeamOrDireTeam(String radiantTeam, String direTeam);
}
