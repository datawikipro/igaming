package pro.datawiki.igaming.analyst.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SportMmrRatingRepository extends JpaRepository<SportMmrRatingEntity, SportMmrRatingId> {

    Optional<SportMmrRatingEntity> findByPlayerIdAndSportSlugAndSurface(Long playerId, String sportSlug, String surface);

    List<SportMmrRatingEntity> findByPlayerIdAndSportSlug(Long playerId, String sportSlug);

    @Query("SELECT r FROM SportMmrRatingEntity r WHERE r.sportSlug = :sportSlug AND r.surface = 'ALL' ORDER BY r.mmr DESC LIMIT :limit")
    List<SportMmrRatingEntity> findTopBySportSlug(String sportSlug, int limit);
}
