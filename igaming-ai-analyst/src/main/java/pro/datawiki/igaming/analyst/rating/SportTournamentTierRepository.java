package pro.datawiki.igaming.analyst.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportTournamentTierRepository extends JpaRepository<SportTournamentTierEntity, Integer> {

    List<SportTournamentTierEntity> findBySportSlugOrderByTierLevelAsc(String sportSlug);
}
