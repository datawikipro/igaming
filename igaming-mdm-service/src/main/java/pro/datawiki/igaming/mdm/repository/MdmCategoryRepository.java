package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmCategory;

import java.util.Optional;

public interface MdmCategoryRepository extends JpaRepository<MdmCategory, Integer> {
    Optional<MdmCategory> findBySportIdAndSlug(Integer sportId, String slug);
    Optional<MdmCategory> findBySofascoreId(Long sofascoreId);
}
