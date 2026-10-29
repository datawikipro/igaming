package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.mdm.domain.MdmTeamMergeMap;

import java.util.Optional;

@Repository
public interface MdmTeamMergeMapRepository extends JpaRepository<MdmTeamMergeMap, Long> {
    Optional<MdmTeamMergeMap> findByLocalId(Long localId);
}
