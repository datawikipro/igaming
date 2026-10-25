package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmTeamIngestionMap;

import java.util.List;
import java.util.Optional;

public interface MdmTeamIngestionMapRepository extends JpaRepository<MdmTeamIngestionMap, Long> {
    Optional<MdmTeamIngestionMap> findByExternalSystemIdAndRawKey(Integer externalSystemId, String rawKey);

    @Query("SELECT m FROM MdmTeamIngestionMap m JOIN FETCH m.canonicalTeam t JOIN FETCH t.sport LEFT JOIN FETCH t.category WHERE m.externalSystem.id = :externalSystemId")
    List<MdmTeamIngestionMap> findByExternalSystemIdWithFetch(@Param("externalSystemId") Integer externalSystemId);

    List<MdmTeamIngestionMap> findByExternalSystemId(Integer externalSystemId);
}
