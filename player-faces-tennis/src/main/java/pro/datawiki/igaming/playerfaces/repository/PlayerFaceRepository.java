package pro.datawiki.igaming.playerfaces.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for athlete player face profiles.
 */
@Repository
public interface PlayerFaceRepository extends JpaRepository<PlayerFace, Long> {

    Optional<PlayerFace> findFirstByNormalizedName(String normalizedName);

    List<PlayerFace> findByNormalizedNameIn(Collection<String> normalizedNames);

    List<PlayerFace> findBySport(SportType sport);

    List<PlayerFace> findBySportAndTour(SportType sport, TourType tour);

    List<PlayerFace> findByPlayerNameContainingIgnoreCase(String name);

    List<PlayerFace> findTop50BySportOrderByRankingAsc(SportType sport);

    boolean existsByNormalizedName(String normalizedName);

    @Query("SELECT p FROM PlayerFace p WHERE LOWER(p.normalizedName) = LOWER(:cleanName) OR LOWER(p.aliases) LIKE LOWER(CONCAT('%', :cleanName, '%'))")
    List<PlayerFace> searchByNameOrAlias(@Param("cleanName") String cleanName);
}
