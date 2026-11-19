package pro.datawiki.igaming.analyst.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportMmrHistoryRepository extends JpaRepository<SportMmrHistoryEntity, Long> {

    List<SportMmrHistoryEntity> findByPlayerIdAndSportSlugOrderByMatchTimeDesc(Long playerId, String sportSlug);

    List<SportMmrHistoryEntity> findByMatchId(Long matchId);

    boolean existsByMatchId(Long matchId);
}
