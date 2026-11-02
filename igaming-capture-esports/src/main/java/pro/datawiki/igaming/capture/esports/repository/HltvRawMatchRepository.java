package pro.datawiki.igaming.capture.esports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.capture.esports.domain.HltvRawMatchEntity;

import java.util.List;

@Repository
public interface HltvRawMatchRepository extends JpaRepository<HltvRawMatchEntity, Long> {

    List<HltvRawMatchEntity> findByTournamentTier(String tournamentTier);

    List<HltvRawMatchEntity> findByTeam1OrTeam2(String team1, String team2);
}
