package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmTournament;

import java.util.List;
import java.util.Optional;

public interface MdmTournamentRepository extends JpaRepository<MdmTournament, Long> {
    Optional<MdmTournament> findBySportIdAndCategoryIdAndSlug(Integer sportId, Integer categoryId, String slug);
    Optional<MdmTournament> findFirstBySportIdAndSlug(Integer sportId, String slug);
    Optional<MdmTournament> findBySofascoreId(Long sofascoreId);
    List<MdmTournament> findBySportIdAndIsFeaturedTrue(Integer sportId);
}
