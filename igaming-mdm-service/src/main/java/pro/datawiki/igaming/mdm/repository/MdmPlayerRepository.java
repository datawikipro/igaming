package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmPlayer;

import java.util.Optional;

public interface MdmPlayerRepository extends JpaRepository<MdmPlayer, Long> {
    Optional<MdmPlayer> findBySportIdAndSlug(Integer sportId, String slug);
    Optional<MdmPlayer> findBySofascoreId(Long sofascoreId);
}
